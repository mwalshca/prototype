package com.fmax.prototype.events;

public class StockOrderCompleted extends Event {

	public StockOrderCompleted(long orderId) {
		super(EventType.STOCK_ORDER_COMPLETED);
		this.orderId = orderId;
	}
	
	private final long orderId;

	public long getOrderId() {
		return orderId;
	}

	@Override
	public String toString() {
		return "StockOrderCompleted [orderId=" + orderId + ", eventType=" + eventType + "]";
	}

}
