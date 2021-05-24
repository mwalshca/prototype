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
	long                askSize;
	long                bidSize;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateTime;
   
	public StockQuote() {
	}
	
	
	public StockQuote(
		Exchange   exchange,
		String     symbol,
		BigDecimal bid,
		BigDecimal ask,
		long       bidSize,
		long       askSize,
		LocalDateTime dateTime)
	{
		this.exchange = exchange;
		this.symbol = symbol;
		this.bid = bid;
		this.ask = ask;
		this.bidSize = bidSize;
		this.askSize = askSize;
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
	public long getBidSize() {
		return bidSize;
	}


	public long getAskSize() {
		return askSize;
	}


	public void setAskSize(long askSize) {
		this.askSize = askSize;
	}


	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}


	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}


	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}


	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}


	public void setBidSize(long bidSize) {
		this.bidSize = bidSize;
	}


	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}


	@Override
	public String toString() {
		return "StockQuote [exchange=" + exchange + ", isan=" + symbol + ", bid=" + bid + ", ask=" + ask
				+ ", askSize=" + askSize + ", bidSize=" + bidSize + ", dateTime=" + dateTime + "]";
	}	
}
