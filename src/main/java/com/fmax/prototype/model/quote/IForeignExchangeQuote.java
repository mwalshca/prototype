package com.fmax.prototype.model.quote;

import java.math.BigDecimal;

import com.fmax.prototype.model.ForeignExchangePair;

public interface IForeignExchangeQuote {

	ForeignExchangePair getForeignExchangePair();

	/**  how much of the quote currency you need to get one unit of the base currency */
	BigDecimal getBid();
	
    /** how of the quote currency you will get by selling one unit of the base currency */
	BigDecimal getAsk();

}