package com.fmax.prototype.model.quote;


@FunctionalInterface
public interface ForeignExchangeQuoteSink {
	void accept(ForeignExchangeQuote stockQuote);
}
