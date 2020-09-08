package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fmax.prototype.model.Exchange;

public class StockQuote implements IStockQuote {
	volatile Exchange   exchange;
	volatile String     symbol;
	volatile BigDecimal bid;
	volatile BigDecimal ask;
	long       sharesAvailable;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateTime;
   
	public StockQuote() {
	}
	
	
	public StockQuote(
		Exchange   exchange,
		String     symbol,
		BigDecimal bid,
		BigDecimal ask,
		long       sharesAvailable,
		LocalDateTime dateTime){
		this.ask = ask;
		this.bid = bid;
		this.exchange = exchange;
		this.sharesAvailable = sharesAvailable;
		this.symbol = symbol;
		this.dateTime = dateTime;
		}
	

	@Override
	public Exchange getExchange() {
		return exchange;
	}
	
	@Override
	public String getSymbol() {
		return symbol;
	}
	
	@Override
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	
	@Override
	public BigDecimal getBid() {
		return bid;
	}
	
	@Override
	public BigDecimal getAsk() {
		return ask;
	}
	
	@Override
	public long getSharesAvailable() {
		return sharesAvailable;
	}

	@Override
	public String toString() {
		return "StockQuote [exchange=" + exchange + ", symbol=" + symbol + ", bid=" + bid + ", ask=" + ask + "]";
	}
}
