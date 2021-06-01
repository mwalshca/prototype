package com.fmax.prototype.events;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("STOCK_ORDER_COMPLETED")
public class StockOrderCompleted extends StockOrderEvent {
	
	public StockOrderCompleted(long orderId) {
		super(EventType.STOCK_ORDER_COMPLETED, orderId);
	}
	
	protected StockOrderCompleted() {} //for JPA
	
	
	@Override
	public String toString() {
		return "StockOrderCompleted [orderId=" + orderId + ", eventType=" + eventType + "]";
	}

}
