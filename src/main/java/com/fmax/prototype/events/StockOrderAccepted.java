package com.fmax.prototype.events;

import java.time.LocalDateTime;
import java.util.Objects;

public class StockOrderAccepted extends Event {
	private final long orderId;
	private final LocalDateTime dttmAccepted;
	
	public StockOrderAccepted(long orderId, LocalDateTime dttmAccepted) {
		super(EventType.STOCK_ORDER_ACCEPTED);
		this.orderId = orderId;
		this.dttmAccepted = Objects.requireNonNull( dttmAccepted );
	}
	
	public long getOrderId() {
		return orderId;
	}

	public LocalDateTime getDttmAccepted() {
		return dttmAccepted;
	}

	@Override
	public String toString() {
		return "StockOrderAccepted [orderId=" + orderId + ", dttmAccepted=" + dttmAccepted + ", eventType=" + eventType
				+ "]";
	}
}
