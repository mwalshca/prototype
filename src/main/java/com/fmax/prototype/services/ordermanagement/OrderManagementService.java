package com.fmax.prototype.services.ordermanagement;

import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;

import com.fmax.prototype.model.trade.BuyOrder;
import com.fmax.prototype.model.trade.SellOrder;
import com.fmax.prototype.model.trade.StockOrder;
import com.fmax.prototype.services.TradeExecutive;
import com.fmax.prototype.services.ib.InteractiveBrokerService;


public class OrderManagementService {
	private final TradeExecutive tradeExecutive;
	private final InteractiveBrokerService brokerSvc;
	
	private LinkedBlockingQueue<StockOrder> ordersIn = new LinkedBlockingQueue<>();

	
	private Thread threadPullOrders  = new Thread( this::pullOrders, "OrderManagementService.pullOrders");
	
	public OrderManagementService(TradeExecutive tradeExecutive, InteractiveBrokerService brokerSvc) {
		assert tradeExecutive != null;
		this.tradeExecutive = tradeExecutive;
		this.brokerSvc = brokerSvc;
		threadPullOrders.start();
	}

	
	private void pullOrders() {
		while(true) {
			StockOrder order;
			try {
				order = ordersIn.take();
				tradeExecutive.pushOrderPlaced( order.getId() ) ; 	
				tradeExecutive.pushOrderAccepted(order.getId(), LocalDateTime.now() ); 
				
				switch( order.getType()) {
				case BUY:
					assert order instanceof BuyOrder;
					break;
				case SELL:
					assert order instanceof SellOrder;
					break;
				default:
					assert false;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				continue;
			}
			
		}
	}
	
	/** Threadsafe */
	public void push(StockOrder order) {
		boolean put = false;
		do {
			try {
				ordersIn.put(order);
				put = true;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} while (!put);
	}
	
	
	
}
