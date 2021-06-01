package com.fmax.prototype.events;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class StockOrderEvent extends Event {
	@Column(name="order_id")
	protected long orderId;
	
	protected StockOrderEvent(EventType eventType, long orderID) {
		super(eventType);
	}
	
	protected StockOrderEvent() {} //for JPA

	public long getOrderId() {
		return orderId;
	}
}
