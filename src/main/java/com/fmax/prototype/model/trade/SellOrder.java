package com.fmax.prototype.model.trade;


import java.math.BigDecimal;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public class SellOrder extends StockOrder {
	
	public SellOrder(Exchange exchange, Stock security, int quantityOrdered, BigDecimal postingPrice) {
		super(StockOrderType.SELL, exchange, security, quantityOrdered, postingPrice);
	}

	@Override
	public String toString() {
		return "SellOrder [id=" + id + ", quantityOrdered=" + quantityOrdered + ", postingPrice=" + postingPrice
				+ ", type=" + type + ", exchange=" + exchange + ", stock=" + stock + ", dttmCreated=" + dttmCreated
				+ ", dttmAccepted=" + dttmAccepted + ", status=" + status + "]";
	}
}
