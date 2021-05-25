package com.fmax.prototype.model.quote;


@FunctionalInterface
public interface StockQuoteSink {
	void accept(StockQuote stockQuote);
}
