package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fmax.prototype.model.ForeignExchangePair;

public class ForeignExchangeQuote implements IForeignExchangeQuote {
	ForeignExchangePair fxPair;
	BigDecimal bid;         // how much of the quote currency you need to get one unit of the base currency
	BigDecimal ask;        //  how of the quote currency you will get by selling one unit of the base currency
	LocalDateTime dateTime;
	
	public ForeignExchangeQuote(ForeignExchangePair fxPair, BigDecimal bid, BigDecimal ask, LocalDateTime dateTime){
		this.fxPair = fxPair;
		this.bid = bid;
		this.ask = ask;
		this.dateTime = dateTime;
	}

	@Override
	public ForeignExchangePair getForeignExchangePair() {
		return fxPair;
	}

	@Override
	public BigDecimal getBid() {
		return bid;
	}

	@Override
	public BigDecimal getAsk() {
		return ask;
	}

	public ForeignExchangePair getFxPair() {
		return fxPair;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	@Override
	public String toString() {
		return "ForeignExchangeQuote [fxPair=" + fxPair + ", bid=" + bid + ", ask=" + ask + ", dateTime=" + dateTime
				+ "]";
	}
}
