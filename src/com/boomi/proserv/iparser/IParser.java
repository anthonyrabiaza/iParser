package com.boomi.proserv.iparser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class IParser {

	private static final String VARIABLE_REGEXP = "[a-zA-Z_0-9\\.]+";
	private static final String SEPARATOR 		= ",";
	private static final String DELIMITER 		= "\"";
	private static final String EOL 			= "\n";
	
	private boolean debug = false;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	protected void debug(String str) {
		if(debug) {
			System.out.println(str);
		}
	}
	
	public InputStream processFile(String fileConfiguration, InputStream data, String dataStartAt, String dataEndAt) throws Exception {
		return stringToInputStream(processFile(fileConfiguration, inputStreamToString(data), dataStartAt, dataEndAt));
	}

	public String processFile(String fileConfiguration, String data, String dataStartAt, String dataEndAt) throws Exception {
		return processFile(fileConfiguration, data, dataStartAt, dataEndAt, null);
	}
	
	public String processFile(String fileConfiguration, String data, String dataStartAt, String dataEndAt, String additionalFilter) throws Exception {

		StringBuffer output 			= new StringBuffer();
		Map<String,Double> nameValue	= new HashMap<String,Double>();

		File configurationFile 			= new File(fileConfiguration);
		CSVParser configurationParser 	= CSVParser.parse(configurationFile, Charset.defaultCharset(), CSVFormat.EXCEL);

		CSVParser dataParser;

		int dataStart 	= excelColumntoInt(dataStartAt);
		int dataEnd 	= excelColumntoInt(dataEndAt);

		//For each line of configuration look through the data
		int configurationLineNumber = 0;
		for (CSVRecord configurationRecord : configurationParser) {
			if(configurationLineNumber>0) {
				String type 				= configurationRecord.get(0);
				String name 				= configurationRecord.get(1);
				String filter 				= configurationRecord.get(3);
				String operation 			= configurationRecord.get(4);
				Operation operationEnum;
				String operationParam		= null;
				String operationStatement	= configurationRecord.get(5);
				
				double value				= -1;
				
				if(additionalFilter!=null) {
					if("".equals(filter)) {
						filter = additionalFilter;
					} else {
						filter = filter + "," + additionalFilter;
					}
				}

				debug("##################################################################################################");
				debug(" Type: " + type);
				debug(" Name: " + name);
				debug(" Filter: " + filter);
				debug(" Operation String: " + operation);

				if("".equals(operation)) {
					continue;
				}

				output.append(DELIMITER + type + DELIMITER + SEPARATOR + DELIMITER + name + DELIMITER + SEPARATOR);

				if(operation.contains("(") && operation.contains(")")) {
					operationParam 	= operation.substring(operation.indexOf("(")+1, operation.indexOf(")"));
					operation 		= operation.substring(0, operation.indexOf("("));
				}

				operationEnum = Operation.valueOf(operation);

				debug("\t Operation: " + operationEnum);
				debug("\t Operation Parameter: " + operationParam); 

				if(Operation.compute.equals(operationEnum)) {
					debug(" Formula: " + operationStatement);
					//Assuming that all the Name with value are already present, finding element starting with Letter
					Pattern pattern = Pattern.compile(VARIABLE_REGEXP);
					Matcher matcher = pattern.matcher(operationStatement);
					while(matcher.find()) {
						String currentVariable = matcher.group();
						//Variable is a number
						if(currentVariable.matches("-?\\d+")) {
							//Nothing
						} else {
							//Variable is virtual value
							operationStatement = operationStatement.replaceAll(currentVariable, String.valueOf(nameValue.get(currentVariable)));
						}
					}

					debug("\t Updated Formula: " + operationStatement);
					value = Calculator.calculate(operationStatement);
				} else {

					//Preparing Filters
					IFilter ifilter = new IFilter(filter, operationEnum, operationStatement);

					//Read the data file
					dataParser = CSVParser.parse(data, CSVFormat.EXCEL);

					//For each Record
					int dataLineNumber = 0;
					for (CSVRecord dataRecord : dataParser) {
						if(dataLineNumber==0) {
							//Header
						} else {
							//int currentValue;
							if(operationParam==null) {
								ifilter.applyFilter(dataRecord, dataStart, dataEnd);
							} else {
								int dataIndex = excelColumntoInt(operationParam);
								ifilter.applyFilter(dataRecord, dataIndex, dataIndex);
							}
						}
						dataLineNumber++;
					}

					value = ifilter.getFinalResult();
				}

				//Storing the value associated with the name
				nameValue.put(name, value);

				if(additionalFilter!=null) {
					output.append(DELIMITER + additionalFilter + DELIMITER + SEPARATOR);
				}
				
				debug(" Value: " + value);
				output.append(value);
				
				
				output.append(EOL);
			}
			configurationLineNumber++;
		}
		debug("##################################################################################################");

		return output.toString();
	}

	public InputStream processFileGroupBy(String fileConfiguration, InputStream is, String dataStartAt, String dataEndAt, String groupByColumn) throws Exception {
		return stringToInputStream(processFileGroupBy(fileConfiguration, inputStreamToString(is), dataStartAt, dataEndAt, groupByColumn));
	}
	
	public String processFileGroupBy(String fileConfiguration, String data, String dataStartAt, String dataEndAt, String groupByColumn) throws Exception {
		StringBuffer output 	= new StringBuffer();
		Set<String> valuesSet 	= getPossiblesValues(data, groupByColumn);
		
		for (Iterator<String> iterator = valuesSet.iterator(); iterator.hasNext();) {
			String currentColumnValue = iterator.next();
			String filter =  groupByColumn + "=" + currentColumnValue;
			output.append(processFile(fileConfiguration, data, dataStartAt, dataEndAt, filter));
		}

		return output.toString();
	}
	
	public InputStream transpose(InputStream is) throws Exception {
		return stringToInputStream(transpose(inputStreamToString(is)));
	}
	
	public String transpose(String data) throws Exception {
		CSVParser dataParser = CSVParser.parse(data, CSVFormat.EXCEL);
		Map<String, String> transposedData = new LinkedHashMap<String, String>();
		StringBuffer buffer = new StringBuffer();

		//int dataLineNumber = 0;
		for (CSVRecord dataRecord : dataParser) {
			if(dataRecord.size()>=2) {
				transposedData.put(dataRecord.get(0), dataRecord.get(1));
			}
			//dataLineNumber++;
		}

		//Displaying keys
		for (Iterator<String> iterator = transposedData.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			buffer.append(DELIMITER + key + DELIMITER);
			if(iterator.hasNext()) {
				buffer.append(SEPARATOR);
			}
		}

		buffer.append(EOL);

		//Displaying values
		for (Iterator<String> iterator = transposedData.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			buffer.append(DELIMITER + transposedData.get(key) + DELIMITER);
			if(iterator.hasNext()) {
				buffer.append(SEPARATOR);
			}
		}
		return buffer.toString();
	}

	public InputStream readFixedLines(InputStream is, int start, int end) throws Exception {
		return stringToInputStream(readFixedLines(inputStreamToString(is), start, end));
	}

	public String readFixedLines(String data, int start, int end) throws Exception {
		CSVParser dataParser = CSVParser.parse(data, CSVFormat.EXCEL);
		StringBuffer buffer = new StringBuffer();

		int dataLineNumber = 0;
		for (CSVRecord dataRecord : dataParser) {
			if(dataLineNumber>=start && dataLineNumber<=end) {
				for(int i=0; i<dataRecord.size(); i++) {
					String currentRecord = dataRecord.get(i);
					currentRecord = currentRecord.replaceAll("[\r\n]", "\t");
					buffer.append(DELIMITER + currentRecord + DELIMITER);
					if(i<dataRecord.size()-1) {
						buffer.append(SEPARATOR);
					}
				}
				buffer.append(EOL);
			}
			dataLineNumber++;
		}
		return buffer.toString();
	}
 
	public Set<String> getPossiblesValues(InputStream data, String column) throws Exception {
		return getPossiblesValues(IParser.inputStreamToString(data), column);
	}
	
	public Set<String> getPossiblesValues(String data, String column) throws Exception {
		Set<String> values = new HashSet<String>();
		CSVParser dataParser = CSVParser.parse(data, CSVFormat.EXCEL);
		
		int dataLineNumber = 0;
		for (CSVRecord configurationRecord : dataParser) {
			if(dataLineNumber>0) {
				values.add(configurationRecord.get(excelColumntoInt(column)));
			}
			dataLineNumber++;
		}
		
		return values;
	}
	
	/*--------------------------------------------------------------------------------------------------------------*/
	
	
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	static int excelColumntoInt(String excelColumn) {
		char column = excelColumn.charAt(0);
		int ascii = column;
		int value = ascii - 65;
		return value;
	}

	/**
	 * Utility to convert InputStream to String
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String inputStreamToString(InputStream is) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}

	/**
	 * Utility to convert String to InputStream
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static InputStream stringToInputStream(String str) throws IOException {
		return new ByteArrayInputStream(str.getBytes());
	}

	public static void main(String[] args) {
		IParser parser = new IParser();
		String result;
		
		if("true".equals(System.getProperty("debug"))){
			parser.setDebug(true);
		}

		if(args.length==4) {
			try {
				result = parser.processFile(args[0], readFile(args[1], Charset.defaultCharset()), args[2], args[3]);
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Command:       	java -jar iparser-runnable<version>.jar configuration_file.csv data.csv datacolumnStart datacolumnEnd");
			System.err.println(" for instance: 	java -jar iparser-runnable-0.1.jar \"Boomi SAP iParser Configuration HR Headcount Indicators.csv\" \"Enablon Report.csv\" I W");
			System.err.println(" with debugging java -Ddebug=true -jar iparser-runnable-0.1.jar \"Boomi SAP iParser Configuration HR Headcount Indicators.csv\" \"Enablon Report.csv\" I W");
		}
	}

	//parser.removeFirstLines(CbCr Test V2 10102018.csv

}
