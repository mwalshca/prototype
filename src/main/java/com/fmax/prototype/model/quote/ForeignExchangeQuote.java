package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.fmax.prototype.model.ForeignExchangePair;

@Embeddable
public class ForeignExchangeQuote implements IForeignExchangeQuote {
	@Embedded
	ForeignExchangePair fxPair;
	
	@Column(name="fx_bid", scale=4, precision=10)
	BigDecimal bid;         // how much of the quote currency you need to get one unit of the base currency
	
	@Column(name="fx_ask", scale=4, precision=10)
	BigDecimal ask;        //  how of the quote currency you will get by selling one unit of the base currency
	
	
	@Column(name="fx_date_time", columnDefinition="DateTime")
	LocalDateTime dateTime;
	
	public ForeignExchangeQuote(ForeignExchangePair fxPair, BigDecimal bid, BigDecimal ask, LocalDateTime dateTime){
		this.fxPair = fxPair;
		this.bid = bid;
		this.ask = ask;
		this.dateTime = dateTime;
	}

	protected ForeignExchangeQuote() {} // for JPA
	
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
