package com.fmax.prototype.events;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("STOCK_ORDER_PLACED")
public class StockOrderPlaced extends StockOrderEvent {
	
	public StockOrderPlaced(long orderId) {
		super(EventType.STOCK_ORDER_PLACED, orderId);
	}
	
	protected StockOrderPlaced() {} //for JPA

	@Override
	public String toString() {
		return "StockOrderPlaced [orderId=" + orderId + "]";
	}
}
