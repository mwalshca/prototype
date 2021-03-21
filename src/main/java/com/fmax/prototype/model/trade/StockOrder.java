package com.fmax.prototype.model.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public abstract class StockOrder {
	final long id = ThreadLocalRandom.current().nextLong();
	
	int quantityOrdered;
	final BigDecimal postingPrice;
	final StockOrderType type;
	final Exchange exchange;
	final Stock stock;
	final LocalDateTime dttmCreated;
	LocalDateTime dttmAccepted;
	
	OrderStatus status = OrderStatus.TO_BE_PLACED;
	
	protected StockOrder(StockOrderType type, Exchange exchange, Stock security, int quantityOrdered, BigDecimal postingPrice) {
		this.type = type;
		this.exchange = exchange;
		this.stock = security;
		this.dttmCreated = LocalDateTime.now();
		this.quantityOrdered = quantityOrdered;
		this.postingPrice = postingPrice;
	}
	
	/** mostly for unit tests */
	protected StockOrder(StockOrderType type, Exchange exchange, Stock security, int quantityOrdered, BigDecimal postingPrice, LocalDateTime dttmCreated) {
		this.type = type;
		this.exchange = exchange;
		this.stock = security;
		this.dttmCreated = LocalDateTime.now();
		this.quantityOrdered = quantityOrdered;
		this.postingPrice = postingPrice;
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

	public int getQuantityOrdered() {
		return quantityOrdered;
	}

	public BigDecimal getPostingPrice() {
		return postingPrice;
	}

	@Override
	public String toString() {
		return "StockOrder [id=" + id + ", quantityOrdered=" + quantityOrdered + ", postingPrice=" + postingPrice
				+ ", type=" + type + ", exchange=" + exchange + ", stock=" + stock + ", dttmCreated=" + dttmCreated
				+ ", dttmAccepted=" + dttmAccepted + ", status=" + status + "]";
	}
}
