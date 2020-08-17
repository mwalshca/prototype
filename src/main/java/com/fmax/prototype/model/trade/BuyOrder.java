package com.fmax.prototype.model.trade;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Security;

public class BuyOrder  extends StockOrder{
	final int quantityOrdered;
	
	public BuyOrder(Exchange exchange, Security security, int quantityOrdered) {
		super(StockOrderType.BUY, exchange, security);
		this.quantityOrdered = quantityOrdered;
	}
	
	public int getQuantityOrdered() {
		return quantityOrdered;
	}
}
