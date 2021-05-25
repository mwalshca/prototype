package com.fmax.prototype.model.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public class BuyOrder  extends StockOrder{

	public BuyOrder(Stock stock, int quantityOrdered, BigDecimal postingPrice) {
		super(StockOrderType.BUY, stock, quantityOrdered, postingPrice);
	}
	
	
	public BuyOrder(Exchange exchange, Stock stock, int quantityOrdered, BigDecimal postingPrice, LocalDateTime dttmCreated) {
		super(StockOrderType.BUY, stock, quantityOrdered, postingPrice, dttmCreated);
	}
}
