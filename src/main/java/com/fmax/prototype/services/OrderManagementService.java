package com.fmax.prototype.services;

import java.util.concurrent.SynchronousQueue;

import org.springframework.stereotype.Service;

import com.fmax.prototype.model.trade.BuyOrder;
import com.fmax.prototype.model.trade.SellOrder;
import com.fmax.prototype.services.ib.InteractiveBrokerService;

@Service
public class OrderManagementService {
	InteractiveBrokerService ibs;
	CriticalEventService ces;
	
	//Queue and thread to receive BuyOrders for processing
	private final SynchronousQueue<BuyOrder> buyOrdersQueue = new SynchronousQueue<>();
	private final Thread threadPullBuyOrders = new Thread( this::pullBuys, "oms:pullBuyOrders");
		
	public OrderManagementService(InteractiveBrokerService ibs, CriticalEventService ces) {
		this.ibs = ibs;
		this.ces = ces;
		threadPullBuyOrders.start();
	}
	
	public void push(BuyOrder buyOrder) {
		
	}
	
	public void push(SellOrder sellOrder) {
		
	}
	
	
	private void pullBuys() {
		assert Thread.currentThread().equals(this.threadPullBuyOrders);
		while (true) {
			try {
				BuyOrder order = buyOrdersQueue.take();
				ibs.placeOrder(order);

			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
				continue;
			}
		}

	}
}
