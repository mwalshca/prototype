package com.fmax.prototype.model.trade;

import java.util.concurrent.ThreadLocalRandom;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Security;

public abstract class StockOrder {
	final long id = ThreadLocalRandom.current().nextLong();
	final StockOrderType type;
	final Exchange exchange;
	final Security security;
	
	OrderStatus status = OrderStatus.IN_DESIGN;
	
	protected StockOrder(StockOrderType type, Exchange exchange, Security security) {
		this.type = type;
		this.exchange = exchange;
		this.security = security;
	}

	public long getId() {
		return id;
	}

	public StockOrderType getType() {
		return type;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void designed() {
		assert OrderStatus.IN_DESIGN.equals(status);
		status = OrderStatus.TO_BE_PLACED;
	}

	public void placed() {
		assert OrderStatus.TO_BE_PLACED.equals(status);
		status = OrderStatus.PLACED;
	}

	public void accepted() {
		assert OrderStatus.PLACED.equals(status);
		status = OrderStatus.ACCEPTED;
	}

	public void completed() {
		assert OrderStatus.ACCEPTED.equals(status);
		status = OrderStatus.COMPLETED;
	}
}
