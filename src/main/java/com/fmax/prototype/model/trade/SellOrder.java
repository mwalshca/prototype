package com.fmax.prototype.model.trade;


import java.math.BigDecimal;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public class SellOrder extends StockOrder {
	
	public SellOrder(Exchange exchange, Stock stock, int quantityOrdered, BigDecimal postingPrice) {
		super(StockOrderType.SELL,  stock, quantityOrdered, postingPrice);
	}
}
