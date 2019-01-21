package com.boomi.proserv.iparser;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

class IParserTest {

	@Test
	void test0() {
		IParser parser = new IParser();
		parser.setDebug(true);
		String result = "";
		try {
			String data = parser.readFixedLines(IParser.readFile("D:\\_Downloads\\work\\sembcorp\\CbCr Test V2 10102018.csv", Charset.defaultCharset()), 1, 10);
			result = parser.processFile(
					"G:\\My Drive\\MyDev\\eclipse-oxygen\\iParser\\test\\iParser_Configuration_Headcount_Indicators.csv",
					IParser.readFile("G:\\My Drive\\MyDev\\eclipse-oxygen\\iParser\\test\\data.csv", Charset.defaultCharset()),
					"D",
					"E"
					);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("##################################################################################################");
		System.out.println(result);
	}
	
	@Test
	void test1() {
		IParser parser = new IParser();
		parser.setDebug(true);
		String result = "";
		try {
			String data = parser.readFixedLines(IParser.readFile("D:\\_Downloads\\work\\sembcorp\\CbCr Test V2 10102018.csv", Charset.defaultCharset()), 1, 10);
			result = parser.processFileGroupBy(
					"G:\\My Drive\\MyDev\\eclipse-oxygen\\iParser\\test\\iParser_Configuration_Headcount_Indicators.csv",
					IParser.readFile("G:\\My Drive\\MyDev\\eclipse-oxygen\\iParser\\test\\data.csv", Charset.defaultCharset()),
					"D",
					"E",
					"C"
					);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("##################################################################################################");
		System.out.println(result);
	}
	
//	@Test
//	void test10() {
//		IParser parser = new IParser();
//		try {
//			String data = parser.readFixedLines(IParser.readFile("D:\\_Downloads\\work\\sembcorp\\CbCr Test V2 10102018.csv", Charset.defaultCharset()), 1, 10);
//			System.out.println("##################################################################################################");
//			System.out.println(data);
//			System.out.println("##################################################################################################");
//			data = parser.transpose(data);
//			System.out.println(data);
//
//			data = parser.readFixedLines(IParser.readFile("D:\\_Downloads\\work\\sembcorp\\CbCr Test V2 10102018.csv", Charset.defaultCharset()), 14, 30);
//			System.out.println("##################################################################################################");
//			System.out.println(data);
//			System.out.println("##################################################################################################");
//			data = parser.transpose(data);
//			System.out.println(data);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	@Test
//	void test11() {
//		IParser parser = new IParser();
//		//parser.setDebug(true);
//		String result = "";
//		try {
//			result = parser.processFile(
//					"D:\\_Downloads\\work\\sembcorp\\iParser_Configuration_HR_Headcount_Indicators.csv",
//					IParser.readFile("D:\\_Downloads\\work\\sembcorp\\Enablon Report.csv", Charset.defaultCharset()),
//					"I",
//					"W"
//					);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("##################################################################################################");
//		System.out.println(result);
//	}
	
//	@Test
//	void test12() {
//		IParser parser = new IParser();
//		//parser.setDebug(true);
//		String result = "";
//		try {
//			result = parser.processFileGroupBy(
//					"D:\\_Downloads\\work\\sembcorp\\iParser_Configuration_HR_Headcount_Indicators.csv",
//					IParser.readFile("D:\\_Downloads\\work\\sembcorp\\Enablon Report.csv", Charset.defaultCharset()),
//					"I",
//					"W",
//					"C"
//					);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("##################################################################################################");
//		System.out.println(result);
//	}


}
