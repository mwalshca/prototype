package com.fmax.prototype.events;

import java.time.LocalDateTime;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public class BestBidSizeReceived extends Event {

	final Exchange exchange;
	final Stock stock;
	final int    bestBidSize;
	final LocalDateTime dateTime;
	
	public BestBidSizeReceived(Exchange exchange, Stock stock, int bestBidSize, LocalDateTime dateTime) {
		super(EventType.BEST_BID_SIZE_RECEIVED);
		this.exchange = exchange;
		this.stock = stock;
		this.bestBidSize = bestBidSize;
		this.dateTime = dateTime;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public Stock getStock() {
		return stock;
	}

	public int getBestBidSize() {
		return bestBidSize;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	@Override
	public String toString() {
		return "BestBidSizeReceived [exchange=" + exchange + ", stock=" + stock + ", bestBidSize=" + bestBidSize
				+ ", dateTime=" + dateTime + "]";
	}
}
