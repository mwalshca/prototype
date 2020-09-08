package com.fmax.prototype.model;

public class Stock {
	public final String cusip;	
	public final String tseSymbol;
	public final String nyseSymbol;
	
	public Stock(String cusip, String tseSymbol, String nyseSymbol) {
		assert cusip != null;
		this.cusip = cusip;
		this.tseSymbol = tseSymbol;
		this.nyseSymbol = nyseSymbol;
	}

	@Override
	public String toString() {
		return "Stock [cusip=" + cusip + ", tseSymbol=" + tseSymbol + ", nyseSymbol=" + nyseSymbol + "]";
	}
}
