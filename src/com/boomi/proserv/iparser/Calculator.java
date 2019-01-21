package com.boomi.proserv.iparser;

import org.mariuszgromada.math.mxparser.Expression;

public class Calculator {
	public static double calculate(String s) {	 
		return new Expression(s).calculate();
	}
}
