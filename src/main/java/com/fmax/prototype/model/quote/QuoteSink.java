package com.fmax.prototype.model.quote;


@FunctionalInterface
public interface QuoteSink {
	void accept(StockQuote stockQuote);
}
