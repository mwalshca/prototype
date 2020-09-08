package com.fmax.prototype.model.trade;

import java.math.BigDecimal;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public class BuyOrder  extends StockOrder{
	final int quantityOrdered;
	final BigDecimal postingPrice;
	
	public BuyOrder(Exchange exchange, Stock stock, int quantityOrdered, BigDecimal postingPrice) {
		super(StockOrderType.BUY, exchange, stock);
		this.quantityOrdered = quantityOrdered;
		this.postingPrice = postingPrice;
	}
	
	public int getQuantityOrdered() {
		return quantityOrdered;
	}

	public BigDecimal getPostingPrice() {
		return postingPrice;
	}

	@Override
	public String toString() {
		return "BuyOrder [quantityOrdered=" + quantityOrdered + ", postingPrice=" + postingPrice + ", id=" + id
				+ ", type=" + type + ", exchange=" + exchange + ", stock=" + stock + ", status=" + status + "]";
	}
}
