package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.util.Currency;

public interface IForeignExchangeQuote {

	/** currency being bought */
	Currency getBaseCurrency();

	/** currency used to buy the base currency */
	Currency getQuoteCurrency();

	/**  how much of the quote currency you need to get one unit of the base currency */
	BigDecimal getBid();
	
    /** how of the quote currency you will get by selling one unit of the base currency */
	BigDecimal getAsk();

}