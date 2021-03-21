package com.fmax.prototype.model.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public class BuyOrder  extends StockOrder{

	public BuyOrder(Exchange exchange, Stock stock, int quantityOrdered, BigDecimal postingPrice) {
		super(StockOrderType.BUY, exchange, stock, quantityOrdered, postingPrice);
	}
	
	
	public BuyOrder(Exchange exchange, Stock stock, int quantityOrdered, BigDecimal postingPrice, LocalDateTime dttmCreated) {
		super(StockOrderType.BUY, exchange, stock, quantityOrdered, postingPrice, dttmCreated);
	}


	@Override
	public String toString() {
		return "BuyOrder [id=" + id + ", quantityOrdered=" + quantityOrdered + ", postingPrice=" + postingPrice
				+ ", type=" + type + ", exchange=" + exchange + ", stock=" + stock + ", dttmCreated=" + dttmCreated
				+ ", dttmAccepted=" + dttmAccepted + ", status=" + status + "]";
	}
}
