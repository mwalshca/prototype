package com.fmax.prototype.tasks;

import java.util.List;

import com.fmax.prototype.model.trade.BuyOrder;

public class OrderManager {

	final StrategyExecutor tradingMaster;
	
	public OrderManager(StrategyExecutor tradingMaster) {
		assert tradingMaster != null;
		this.tradingMaster = tradingMaster;
	}
	
	/** reentrant */
	public void execute(List<BuyOrder> orders) {
		assert orders != null;
		orders.parallelStream().forEach( this::placeOrder );
	}
	
	/** reentrant */
	private void placeOrder(BuyOrder order) {
		// TODO implement plce the order
		order.placed();
		tradingMaster.orderPlaced(order);
	}
}
