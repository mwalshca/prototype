package com.fmax.prototype.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class Stock {
	@Enumerated(EnumType.STRING)
	@Column(name="exchange")
	private Exchange exchange;
	
	
	@Column(name="symbol", length=12)
	private	String   symbol;
	
	@Embedded
	private ISIN     isin;
	
	public Stock(Exchange exchange, String symbol, ISIN isin) {
		this.exchange = Objects.requireNonNull(exchange);
		this.symbol = Objects.requireNonNull(symbol);
		this.isin = Objects.requireNonNull(isin);
	}
	
	//for JPA
	protected Stock() {} 
	
	public Exchange getExchange() {
		return exchange;
	}

	
	public String getSymbol() {
		return symbol;
	}

	
	public ISIN getIsin() {
		return isin;
	}
	

	@Override
	public String toString() {
		return "Stock [exchange=" + exchange + ", symbol=" + symbol + ", isin=" + isin + "]";
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + ((isin == null) ? 0 : isin.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stock other = (Stock) obj;
		if (exchange != other.exchange)
			return false;
		if (isin == null) {
			if (other.isin != null)
				return false;
		} else if (!isin.equals(other.isin))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
}
