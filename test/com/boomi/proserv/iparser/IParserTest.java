package com.boomi.proserv.iparser;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

class IParserTest {

	@Test
	void test0() {
		IParser parser = new IParser();
		parser.setDebug(false);
		String result = "";
		try {
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
		parser.setDebug(false);
		String result = "";
		try {
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

}
