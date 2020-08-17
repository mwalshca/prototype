package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fmax.prototype.model.Exchange;

public class Bid {
	Exchange   exchange;
	String     symbol;
	int        size;
	BigDecimal price;
	Instant    techInstantReceived;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	LocalDateTime bidDateTime;

	public int getBidSize() {
		return size;
	}

	public void setBidSize(int bidSize) {
		this.size = bidSize;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public LocalDateTime getBidDateTime() {
		return bidDateTime;
	}

	public void setBidDateTime(LocalDateTime bidDateTime) {
		this.bidDateTime = bidDateTime;
	
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	public Instant getTechInstantReceived() {
		return techInstantReceived;
	}

	public void setTechInstantReceived(Instant techInstantReceived) {
		this.techInstantReceived = techInstantReceived;
	}	
}
