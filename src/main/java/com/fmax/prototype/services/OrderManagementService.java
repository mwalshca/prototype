package com.fmax.prototype.services;

import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Service;

import com.fmax.prototype.model.trade.BuyOrder;
import com.fmax.prototype.model.trade.SellOrder;
import com.fmax.prototype.services.ib.InteractiveBrokerService;

@Service
public class OrderManagementService {
	InteractiveBrokerService ibs;
	CriticalEventService ces;
	
	//Queue and thread to receive BuyOrders for processing
	private final LinkedBlockingQueue<BuyOrder> buyOrders= new LinkedBlockingQueue<>();
	private final Thread threadPullBuyOrders = new Thread( this::pullBuys, "oms:pullBuyOrders");
	
	//Queue and thread to recevie SellOrders for processing
	private final LinkedBlockingQueue<SellOrder> sellOrders= new LinkedBlockingQueue<>();
	private final Thread threadPullSellOrders = new Thread( this::pullSells, "oms:pullSellOrders");
	
	public OrderManagementService(InteractiveBrokerService ibs, CriticalEventService ces) {
		this.ibs = ibs;
		this.ces = ces;
		threadPullBuyOrders.start();
	}
	
	public void push(BuyOrder buyOrder) {
		boolean put = false;
		do {
			try {
				buyOrders.put(buyOrder);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			}
		} while (!put);	
	}
	
	public void push(SellOrder sellOrder) {
		boolean put = false;
		do {
			try {
				sellOrders.put(sellOrder);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			}
		} while (!put); //FIXME infinite loop
	}
	
	
	private void pullBuys() {
		assert Thread.currentThread().equals(this.threadPullBuyOrders);
		while (true) {
			try {
				BuyOrder order = buyOrders.take();
				ibs.placeOrder(order);
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
				continue;
			}
		}

	}
	
	private void pullSells() {
		assert Thread.currentThread().equals(this.threadPullSellOrders);
		while (true) {
			try {
				SellOrder order = sellOrders.take();
				ibs.placeOrder(order);
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
				continue;
			}
		}

	}
}
