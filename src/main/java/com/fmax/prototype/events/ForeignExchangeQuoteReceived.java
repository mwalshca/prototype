package com.fmax.prototype.events;

import com.fmax.prototype.model.quote.IForeignExchangeQuote;

public class ForeignExchangeQuoteReceived extends Event {

	final IForeignExchangeQuote foreignExchangeQuote;
	
	public ForeignExchangeQuoteReceived(IForeignExchangeQuote foreignExchangeQuote) {
		super(EventType.FOREIGN_EXCHANGE_QUOTE_RECEIVED);
		this.foreignExchangeQuote = foreignExchangeQuote;
	}
	
	public IForeignExchangeQuote getForeignExchangeQuote() {
		return foreignExchangeQuote;
	}

	@Override
	public String toString() {
		return "ForeignExchangeQuoteReceived [foreignExchangeQuote=" + foreignExchangeQuote + "]";
	}	
}
