package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.util.Currency;

public class ForeignExchangeQuote implements IForeignExchangeQuote {
	Currency baseCurrency;  //currency being bought
	Currency quoteCurrency; //currency used to buy the base currency
	BigDecimal bid;         // how much of the quote currency you need to get one unit of the base currency
	BigDecimal ask;        //  how of the quote currency you will get by selling one unit of the base currency
	
	
	@Override
	public Currency getBaseCurrency() {
		return baseCurrency;
	}
	
	public void setBaseCurrency(Currency baseCurrency) {
		this.baseCurrency = baseCurrency;
	}
	
	@Override
	public Currency getQuoteCurrency() {
		return quoteCurrency;
	}
	
	public void setQuoteCurrency(Currency quoteCurrency) {
		this.quoteCurrency = quoteCurrency;
	}
	
	@Override
	public BigDecimal getBid() {
		return bid;
	}
	
	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}
	
	@Override
	public BigDecimal getAsk() {
		return ask;
	}
	
	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	@Override
	public String toString() {
		return "ForeignExchangeQuote [baseCurrency=" + baseCurrency + ", quoteCurrency=" + quoteCurrency + ", bid="
				+ bid + ", ask=" + ask + "]";
	}	
}
