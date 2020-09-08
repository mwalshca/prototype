package com.fmax.prototype.components;

import java.util.concurrent.LinkedBlockingQueue;

import com.fmax.prototype.model.trade.StockOrder;

public class OrderManagementService {

	final TradeExecutive tradeExecutive;
	private LinkedBlockingQueue<StockOrder> orders = new LinkedBlockingQueue<>();
	private Thread threadPullOrders  = new Thread( this::pullOrders, "OrderManagementService.pullOrders");
	
	public OrderManagementService(TradeExecutive tradeExecutive) {
		assert tradeExecutive != null;
		this.tradeExecutive = tradeExecutive;
		threadPullOrders.start();
	}
	
	
	/** reentrant */
	public void push(StockOrder order) {
		boolean put = false;
		do {
			try {
				orders.put(order);
				put = true;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} while (!put);
	}
	
	private void pullOrders() {
		while(true) {
			StockOrder order;
			try {
				order = orders.take();
				Thread.sleep(1000); // TODO simulated delay - remove
				tradeExecutive.pushOrderPlaced( order.getId() ) ; 	// TODO actually place the order
				Thread.sleep(1000); // TODO simulated delay - remove
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				continue;
			}
			
		}
	}
}
