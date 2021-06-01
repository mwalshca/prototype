package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fmax.prototype.model.Stock;

@Embeddable
public class StockQuote implements IStockQuote {
	
	@Embedded
	Stock       stock;
	
	@Column(name="stk_bid", scale=4, precision=10)
	BigDecimal  bid;
	
	@Column(name="stk_ask", scale=4, precision=10)
	BigDecimal  ask;
	
	@Column(name="stk_ask_size")
	long        askSize;
	
	@Column(name="st_bid_size")
	long        bidSize;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Column(name="stk_qt_dttm", columnDefinition="DateTime")
    LocalDateTime dateTime;
   
	protected StockQuote(){} //for JPA
	
	
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
