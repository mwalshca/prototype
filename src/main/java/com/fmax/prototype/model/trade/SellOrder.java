package com.fmax.prototype.model.trade;


import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public class SellOrder extends StockOrder {
	final int quantityOrdered;
	
	
	public SellOrder(Exchange exchange, Stock security, int quantityOrdered) {
		super(StockOrderType.SELL, exchange, security);
		this.quantityOrdered = quantityOrdered;
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

	@Override
	public String toString() {
		return "SellOrder [quantityOrdered=" + quantityOrdered + ", id=" + id + ", type=" + type + ", exchange="
				+ exchange + ", stock=" + stock + ", status=" + status + "]";
	}
}
