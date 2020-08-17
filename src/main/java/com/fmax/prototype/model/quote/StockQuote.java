package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fmax.prototype.model.Event;
import com.fmax.prototype.model.Exchange;

public class StockQuote extends Event {
	Exchange   exchange;
	String     symbol;
	BigDecimal bid;
	BigDecimal ask;
	long       sharesAvailable;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateTime;
   
	Instant    instantReceived;
	
	public Exchange getExchange() {
		return exchange;
	}
	
	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	
	public BigDecimal getBid() {
		return bid;
	}
	
	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}
	
	public BigDecimal getAsk() {
		return ask;
	}
	
	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	public Instant getInstantReceived() {
		return instantReceived;
	}

	public void setInstantReceived(Instant instantReceived) {
		this.instantReceived = instantReceived;
	}

	public long getSharesAvailable() {
		return sharesAvailable;
	}

	public void setSharesAvailable(long sharesAvailable) {
		this.sharesAvailable = sharesAvailable;
	}
}
