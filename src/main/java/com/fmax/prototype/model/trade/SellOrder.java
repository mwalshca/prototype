package com.fmax.prototype.model.trade;


import java.math.BigDecimal;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public class SellOrder extends StockOrder {
	final int quantityOrdered;
	final BigDecimal postingPrice;
	
	public SellOrder(Exchange exchange, Stock security, int quantityOrdered, BigDecimal postingPrice) {
		super(StockOrderType.SELL, exchange, security);
		this.quantityOrdered = quantityOrdered;
		this.postingPrice = postingPrice;
	}
	
	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public Stock getSecurity() {
		return stock;
	}

	public int getQuantityOrdered() {
		return quantityOrdered;
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

	public BigDecimal getPostingPrice() {
		return postingPrice;
	}

	@Override
	public String toString() {
		return "SellOrder [quantityOrdered=" + quantityOrdered + ", postingPrice=" + postingPrice + ", id=" + id
				+ ", type=" + type + ", exchange=" + exchange + ", stock=" + stock + ", dttmCreated=" + dttmCreated
				+ ", dttmAccepted=" + dttmAccepted + ", status=" + status + "]";
	}
}
