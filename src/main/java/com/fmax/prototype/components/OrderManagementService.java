package com.fmax.prototype.components;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import com.fmax.prototype.model.quote.IStockQuote;
import com.fmax.prototype.model.trade.BuyOrder;
import com.fmax.prototype.model.trade.SellOrder;
import com.fmax.prototype.model.trade.StockOrder;


/** Note: This is a simulator (mock) implementation */
public class OrderManagementService {

	private final AtomicReference<IStockQuote> currentQuote = new AtomicReference<>();
	
	private final TradeExecutive tradeExecutive;
	
	private LinkedBlockingQueue<IStockQuote> quotesIn = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<StockOrder> ordersIn = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<BuyOrder> buyOrders  = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<SellOrder> sellOrders  = new LinkedBlockingQueue<>();
	
	private Thread threadPullOrders  = new Thread( this::pullOrders, "OrderManagementService.pullOrders");
	private Thread threadPullQuotes  = new Thread( this::pullQuotes, "OrderManagementService.pullQuotes");
	
	public OrderManagementService(TradeExecutive tradeExecutive) {
		assert tradeExecutive != null;
		this.tradeExecutive = tradeExecutive;
		threadPullOrders.start();
		threadPullQuotes.start();
	}

	
	private void pullOrders() {
		while(true) {
			StockOrder order;
			try {
				order = ordersIn.take();
				
				Thread.sleep(1000); 
				tradeExecutive.pushOrderPlaced( order.getId() ) ; 	
				
				Thread.sleep(1000); 
				tradeExecutive.pushOrderAccepted(order.getId(), LocalDateTime.now() ); 
				
				switch( order.getType()) {
				case BUY:
					assert order instanceof BuyOrder;
					buyOrders.add( (BuyOrder) order);
					if(currentQuote.get() != null)
						handleQuoteReceived( currentQuote.get() );
					break;
				case SELL:
					assert order instanceof SellOrder;
					sellOrders.add( (SellOrder) order);
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
	
	
	private void pullQuotes() {
		while(true) {
			try {
				IStockQuote quote = quotesIn.take(); 
				currentQuote.set(quote);
				handleQuoteReceived(quote);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				continue;
			}
		}
	}
	
	
	private void handleQuoteReceived(IStockQuote quote) {
		switch(quote.getExchange()) {
		case TSE:
			//logic hardcoded that all buys are on TSE
			List<BuyOrder> toBeRemoved = new ArrayList<BuyOrder>();
			for(BuyOrder order:buyOrders) {
				assert quote.getAsk() !=  null;
				if( quote.getAsk().compareTo(order.getPostingPrice()) <= 0)  { //ask less than our bid, so fill it!
					toBeRemoved.add(order);
					tradeExecutive.pushOrderFilled( order.getId(), order.getQuantityOrdered() ); //hard-coded - one fill, with quanitty equal to quantity ordered
					tradeExecutive.pushOrderCompleted( order.getId() );
				}
			}
			buyOrders.removeAll( toBeRemoved) ;
			break;
			
		case NYSE:
			//logic hardcoded that all sells are on NYSE
			List<SellOrder> sellsToBeRemoved = new ArrayList<SellOrder>();
			for(SellOrder order:sellOrders) {
				assert quote.getBid() !=  null;
				if( quote.getBid().compareTo(order.getPostingPrice()) >= 0)  { //bid greater than or equal to our ask, so fill it!
					sellsToBeRemoved.add(order);
					tradeExecutive.pushOrderFilled( order.getId(), order.getQuantityOrdered() ); //hard-coded - one fill, with quanity equal to quantity ordered
					tradeExecutive.pushOrderCompleted( order.getId() );
				}
			}
			sellOrders.removeAll( sellsToBeRemoved) ;
			break;
			
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
	
	
	/** Threadsafe */
	public void push(IStockQuote quote) {
		boolean put = false;
		do {
			try {
				quotesIn.put(quote);
				put = true;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} while (!put);
	}
	
}
