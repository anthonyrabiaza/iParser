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

	Map<String,String> filterMap;
	List<Double> values;
	Operation operation;
	String operationStatement;

	public IFilter(String filter, Operation operationEnum, String operationStatement) {
		//Storing filters (regexp)
		
		if(!"".equals(filter)) {
			String[] filterArray = filter.split(",");
			filterMap = new HashMap<String,String>();
			for (int i = 0; i < filterArray.length; i++) {
				String[] filterValue = filterArray[i].split("=");
				if(filterValue.length==2) {
					filterMap.put(filterValue[0], filterValue[1]);
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
					for (Iterator<Entry<String, String>> iterator = filterMap.entrySet().iterator(); iterator.hasNext();) {
						Entry<String, String> entry = iterator.next();
						String filterColumn = entry.getKey();
						String filterValue = entry.getValue();
	
						Pattern pattern = Pattern.compile(filterValue);
						String currentValue = data.get(IParser.excelColumntoInt(filterColumn)).trim().replaceAll("\n", " ");
						Matcher matcher = pattern.matcher(currentValue);
	
						match = matcher.matches();
						//System.out.println("Pattern:" + pattern + ", Value:" + currentValue + ", Match:" + match);
	
						if(!match) {
							break;
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
}
