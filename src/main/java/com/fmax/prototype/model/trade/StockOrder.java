package com.fmax.prototype.model.trade;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public abstract class StockOrder {
	final long id = ThreadLocalRandom.current().nextLong();
	final StockOrderType type;
	final Exchange exchange;
	final Stock stock;
	final LocalDateTime dttmCreated;
	LocalDateTime dttmAccepted;
	
	OrderStatus status = OrderStatus.IN_DESIGN;
	
	protected StockOrder(StockOrderType type, Exchange exchange, Stock security) {
		this.type = type;
		this.exchange = exchange;
		this.stock = security;
		this.dttmCreated = LocalDateTime.now();
	}
	
	/** mostly for unit tests */
	protected StockOrder(StockOrderType type, Exchange exchange, Stock security, LocalDateTime dttmCreated) {
		this.type = type;
		this.exchange = exchange;
		this.stock = security;
		this.dttmCreated = LocalDateTime.now();
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

	public Exchange getExchange() {
		return exchange;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public void designed() {
		assert OrderStatus.IN_DESIGN.equals(status);
		status = OrderStatus.TO_BE_PLACED;
	}

	public void placed() {
		assert OrderStatus.TO_BE_PLACED.equals(status);
		status = OrderStatus.PLACED;
	}

	public void accepted(LocalDateTime dttmAccepted) {
		assert OrderStatus.PLACED.equals(status);
		status = OrderStatus.ACCEPTED;
		this.dttmAccepted = Objects.requireNonNull( dttmAccepted );
	}

	public void completed() {
		assert OrderStatus.ACCEPTED.equals(status);
		status = OrderStatus.COMPLETED;
	}

	
	public LocalDateTime getDttmAccepted() {
		return dttmAccepted;
	}

	public LocalDateTime getDttmCreated() {
		return dttmCreated;
	}

	@Override
	public String toString() {
		return "StockOrder [id=" + id + ", type=" + type + ", exchange=" + exchange + ", stock=" + stock
				+ ", dttmCreated=" + dttmCreated + ", dttmAccepted=" + dttmAccepted + ", status=" + status + "]";
	}

}
