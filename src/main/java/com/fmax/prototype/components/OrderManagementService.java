package com.fmax.prototype.components;

import org.springframework.stereotype.Component;

import com.fmax.prototype.model.trade.StockOrder;

@Component
public class OrderManagementService {

	final TradeExecutive tradeExecutive;
	
	public OrderManagementService(TradeExecutive tradeExecutive) {
		assert tradeExecutive != null;
		this.tradeExecutive = tradeExecutive;
	}
	
	
	
	/** reentrant */
	private void place(StockOrder order) {
		// TODO implement place the order
		order.placed();
		tradeExecutive.orderPlaced(order);
	}
}
