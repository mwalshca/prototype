package com.fmax.prototype.events;

public class StockOrderPlaced extends Event {

	public StockOrderPlaced(long orderId) {
		super(EventType.STOCK_ORDER_PLACED);
		this.orderId = orderId;
	}
	
	private final long orderId;

	public long getOrderId() {
		return orderId;
	}

	@Override
	public String toString() {
		return "StockOrderPlaced [orderId=" + orderId + "]";
	}
}
