package com.boomi.proserv.iparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVRecord;

public class IFilter {

	Map<String,FilterValue> filterMap;
	List<Double> values;
	Operation operation;
	String operationStatement;

	public IFilter(String filter, Operation operationEnum, String operationStatement) throws Exception {
		//Storing filters (regexp)

		if(!"".equals(filter)) {
			String[] filterArray = filter.split(",");
			filterMap = new HashMap<String,FilterValue>();
			for (int i = 0; i < filterArray.length; i++) {
				String[] filterValue = filterArray[i].split(">=|<=|=|>|<",2);
				if(filterValue.length==2) {
					String key = filterValue[0];
					String value = filterValue[1];
					String operator = filterArray[i].substring(key.length(), filterArray[i].length() - value.length());
					filterMap.put(key, new FilterValue(operator, value));
				} else {
					System.err.println(" Incorrect IFilter: \"" + filterArray[i] +"\"");
				}
			}
		} else {
			filterMap = null;
		}

		//Storing Operation
		operation = operationEnum;
		//List to store all values
		values = new ArrayList<Double>();
		this.operationStatement = operationStatement;
	}

	public void applyFilter(CSVRecord data, int dataStart, int dataEnd) {
		//int sum = 0;
		for(int i=dataStart;i<=dataEnd;i++) {

			String currentData = data.get(i);

			if(!"".equals(currentData)) {
				boolean match = false;

				if(filterMap!=null) {
					for (Iterator<Entry<String, FilterValue>> iterator = filterMap.entrySet().iterator(); iterator.hasNext();) {
						Entry<String, FilterValue> entry = iterator.next();
						String filterColumn = entry.getKey();
						String filterValue  = entry.getValue().getValue();
						String currentValue = data.get(IParser.excelColumntoInt(filterColumn)).trim().replaceAll("\n", " ");

						if(entry.getValue().getOperator().equals(Operator.equals)) {
							Pattern pattern = Pattern.compile(filterValue);

							Matcher matcher = pattern.matcher(currentValue);
							match = matcher.matches();
							//System.out.println("Pattern:" + pattern + ", Value:" + currentValue + ", Match:" + match);

							if(!match) {
								break;
							}
						} else {
							match = Operator.match(filterValue, entry.getValue().getOperator(), currentValue);//FIXME: check
						}
					}
				} else {
					//No filter, always match
					match = true;
				}

				if(match) {
					double currentInt = Double.parseDouble(currentData);
					values.add(currentInt);
					//System.out.println(currentInt + "+");
				}
			}
		}
	}

	public double getFinalResult() {
		switch(operation) {
		case sum:
		{
			Double sum = 0.0;
			for (Iterator<Double> iterator = values.iterator(); iterator.hasNext();) {
				Double value = iterator.next();
				sum += value;
			}
			return sum;
		}
		case div:
			return 0;
		case avg:
		{
			Double sum = 0.0;
			for (Iterator<Double> iterator = values.iterator(); iterator.hasNext();) {
				Double value = iterator.next();
				sum += value;
			}
			if(values.size()==0) {
				return 0;
			} else {
				return sum/values.size();
			}
		}
		case min:
		{
			if(values.size()==0) {
				return 0.0;
			} else {
				Double min = values.get(0);
				for (Iterator<Double> iterator = values.iterator(); iterator.hasNext();) {
					Double value = iterator.next();
					if(value < min) {
						min = value;
					}
				}
				return min;
			}
		}
		case max:
		{
			if(values.size()==0) {
				return 0;
			} else {
				Double max = values.get(0);
				for (Iterator<Double> iterator = values.iterator(); iterator.hasNext();) {
					Double value = iterator.next();
					if(value > max) {
						max = value;
					}
				}
				return max;
			}
		}
		case count:
			if(values.size()==0) {
				return 0;
			} else {
				return values.size();
			}
		default:
			return -1;
		}
	}

	enum Operator {
		equals,
		greaterThan,
		greaterOrEqualsthan,
		lowerThan,
		lowerOrEqualsthan;

		public static Operator getOperator(String value) throws Exception {
			switch (value) {
			case "=":
				return Operator.equals;
			case ">":
				return Operator.greaterThan;
			case ">=":
				return Operator.greaterOrEqualsthan;
			case "<":
				return Operator.lowerThan;
			case "<=":
				return Operator.lowerOrEqualsthan;
			default:
				throw new Exception("Error " + value + " is not a valid operator");
			}
		}

		public static boolean match(String filterValue, Operator operator, String dataValue) {
			Long filterValueLong 	= Long.valueOf(filterValue);
			Long dataValueLong 			= Long.valueOf(dataValue);
			switch(operator) {
			case equals:
				return dataValueLong == filterValueLong;
			case greaterThan:
				return dataValueLong > filterValueLong;
			case greaterOrEqualsthan:
				return dataValueLong >= filterValueLong;
			case lowerThan:
				return dataValueLong < filterValueLong;
			case lowerOrEqualsthan:
				return dataValueLong <= filterValueLong;
			default:
				return false;
			}
		}
	}

	class FilterValue {
		private Operator operator;
		private String value;

		public Operator getOperator() {
			return operator;
		}
		public String getValue() {
			return value;
		}

		public FilterValue(String operator, String value) throws Exception {
			this.operator = Operator.getOperator(operator);
			this.value = value;
		}
	}
}
