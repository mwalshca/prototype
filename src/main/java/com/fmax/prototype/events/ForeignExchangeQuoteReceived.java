package com.fmax.prototype.events;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import com.fmax.prototype.model.quote.ForeignExchangeQuote;

@Entity
@DiscriminatorValue("FOREIGN_EXCHANGE_QUOTE_RECEIVED")
public class ForeignExchangeQuoteReceived extends Event {

	@Embedded
	ForeignExchangeQuote foreignExchangeQuote;
	
	public ForeignExchangeQuoteReceived(ForeignExchangeQuote foreignExchangeQuote) {
		super(EventType.FOREIGN_EXCHANGE_QUOTE_RECEIVED);
		this.foreignExchangeQuote = foreignExchangeQuote;
	}
	
	public ForeignExchangeQuote getForeignExchangeQuote() {
		return foreignExchangeQuote;
	}

	@Override
	public String toString() {
		return "ForeignExchangeQuoteReceived [foreignExchangeQuote=" + foreignExchangeQuote + "]";
	}	
}
