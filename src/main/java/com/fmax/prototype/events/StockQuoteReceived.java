package com.fmax.prototype.events;

import com.fmax.prototype.model.quote.IStockQuote;

public class StockQuoteReceived extends Event {

	final IStockQuote stockQuote;
	
	public StockQuoteReceived(IStockQuote stockQuote) {
		super(EventType.STOCK_QUOTE_RECEIVED);
		this.stockQuote = stockQuote;
	}

	public IStockQuote getStockQuote() {
		return stockQuote;
	}

	@Override
	public String toString() {
		return "StockQuoteReceived [stockQuote=" + stockQuote + ", eventType=" + eventType + "]";
	}
}
