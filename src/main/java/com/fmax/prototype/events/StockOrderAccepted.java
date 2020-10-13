package com.fmax.prototype.events;

public class StockOrderAccepted extends Event {

	public StockOrderAccepted(long orderId) {
		super(EventType.STOCK_ORDER_ACCEPTED);
		this.orderId = orderId;
	}
	
	private final long orderId;

	public long getOrderId() {
		return orderId;
	}

	@Override
	public String toString() {
		return "StockOrderAccepted [orderId=" + orderId + ", eventType=" + eventType + "]";
	}
}
