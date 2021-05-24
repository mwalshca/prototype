package com.fmax.prototype.model;

import java.util.Objects;

public class Stock {
	private Exchange exchange;
	private	String   symbol;
	private ISIN     isin;
	
	public Stock(Exchange exchange, String symbol, ISIN isin) {
		this.exchange = Objects.requireNonNull(exchange);
		this.symbol = Objects.requireNonNull(symbol);
		this.isin = Objects.requireNonNull(isin);
	}

	
	public Exchange getExchange() {
		return exchange;
	}

	
	public String getSymbol() {
		return symbol;
	}

	
	public ISIN getIsin() {
		return isin;
	}
}
