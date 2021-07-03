package com.fmax.prototype.common;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public abstract class RPMMath {

	public static final MathContext MATH_CONTEXT_RATIO = new MathContext(2, RoundingMode.DOWN);
	public static final MathContext PRECISION_SEVEN_ROUND_DOWN = new MathContext(7, RoundingMode.DOWN);
	public static final MathContext MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN = new MathContext(0, RoundingMode.DOWN);
	public static final BigDecimal TWO = new BigDecimal("2.00");
	
}
