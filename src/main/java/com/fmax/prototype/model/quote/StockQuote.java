package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fmax.prototype.model.Stock;

public class StockQuote implements IStockQuote {
	Stock               stock;
	volatile BigDecimal bid;
	volatile BigDecimal ask;
	long                askSize;
	long                bidSize;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateTime;
   
	public StockQuote() {
	}
	
	
	public StockQuote(
		Stock stock,
		BigDecimal bid,
		BigDecimal ask,
		long       bidSize,
		long       askSize,
		LocalDateTime dateTime)
	{
		this.stock = Objects.requireNonNull(stock);
		this.bid = Objects.requireNonNull(bid);
		this.ask = Objects.requireNonNull(ask);
		this.bidSize = bidSize;
		this.askSize = askSize;
		this.dateTime = Objects.requireNonNull(dateTime);
	}


	public Stock getStock() {
		return stock;
	}


	public BigDecimal getBid() {
		return bid;
	}


	public BigDecimal getAsk() {
		return ask;
	}


	public long getAskSize() {
		return askSize;
	}


	public long getBidSize() {
		return bidSize;
	}


	public LocalDateTime getDateTime() {
		return dateTime;
	}


	@Override
	public String toString() {
		return "StockQuote [stock=" + stock + ", bid=" + bid + ", ask=" + ask + ", askSize=" + askSize + ", bidSize="
				+ bidSize + ", dateTime=" + dateTime + "]";
	}
}
