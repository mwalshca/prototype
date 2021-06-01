package com.fmax.prototype.events;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("STOCK_ORDER_FILLED")
public class StockOrderFilled extends StockOrderEvent {
	@Column(name="filled")
	private int nFilled;
	
	public StockOrderFilled(long orderId, int nFilled) {
		super(EventType.STOCK_ORDER_FILLED, orderId);
		this.nFilled = nFilled;
	}
	
	protected StockOrderFilled() {} // for JPA
	

	public int getnFilled() {
		return nFilled;
	}


	@Override
	public String toString() {
		return "StockOrderFilled [orderId=" + orderId + ", nFilled=" + nFilled + ", eventType=" + eventType + "]";
	}

}
