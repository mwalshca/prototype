package com.fmax.prototype.events;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import com.fmax.prototype.model.quote.StockQuote;

@Entity
@DiscriminatorValue("STOCK_QUOTE_RECEIVED")
public class StockQuoteReceived extends Event {

	@Embedded
	StockQuote stockQuote;
	
	protected StockQuoteReceived() {} //for JPA
	
	public StockQuoteReceived(StockQuote stockQuote) {
		super(EventType.STOCK_QUOTE_RECEIVED);
		this.stockQuote = stockQuote;
	}

	public StockQuote getStockQuote() {
		return stockQuote;
	}

	@Override
	public String toString() {
		return "StockQuoteReceived [stockQuote=" + stockQuote + ", eventType=" + eventType + "]";
	}
}
