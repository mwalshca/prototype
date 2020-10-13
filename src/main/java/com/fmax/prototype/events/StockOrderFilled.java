package com.fmax.prototype.events;

public class StockOrderFilled extends Event {

	public StockOrderFilled(long orderId, int nFilled) {
		super(EventType.STOCK_ORDER_FILLED);
		this.orderId = orderId;
		this.nFilled = nFilled;
	}
	
	private final long orderId;
	private final int nFilled;
	
	public long getOrderId() {
		return orderId;
	}

	
	public int getnFilled() {
		return nFilled;
	}


	@Override
	public String toString() {
		return "StockOrderFilled [orderId=" + orderId + ", nFilled=" + nFilled + ", eventType=" + eventType + "]";
	}

}
