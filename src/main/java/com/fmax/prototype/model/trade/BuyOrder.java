package com.fmax.prototype.model.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Arbitrage;
import com.fmax.prototype.model.Stock;

public class BuyOrder  extends StockOrder{

	public BuyOrder(Arbitrage instance, Stock stock, int quantityOrdered, BigDecimal postingPrice) {
		super(instance, StockOrderType.BUY, stock, quantityOrdered, postingPrice);
	}
	
	
	public BuyOrder(Arbitrage instance, Exchange exchange, Stock stock, int quantityOrdered, BigDecimal postingPrice, LocalDateTime dttmCreated) {
		super(instance, StockOrderType.BUY, stock, quantityOrdered, postingPrice, dttmCreated);
	}
}
