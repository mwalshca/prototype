package com.fmax.prototype.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Objects;

public class ExchangeMetadata {
	private static final BigDecimal tseLowTickPrice = new BigDecimal("0.50");
	private static final BigDecimal tseLowTickSize   = new BigDecimal("0.005");
	private static final BigDecimal tseStandardTickSize   = new BigDecimal("0.01");
	private static final BigDecimal nyseStandardTickSize  = new BigDecimal("0.01");
	
	private Exchange      exchange;
	private LocalDateTime tradingStartTime;
	private LocalDateTime tradingStopTime;
	private Currency      tradeCurrency;
	private BigDecimal    passiveExchangeFeePerShare;
	private BigDecimal    agressiveExchangeFeePerShare;
	private BigDecimal    routedExchangeFeePerShare;

	//hard-coded logic. At least it's encapsulated
	public BigDecimal standardTradingPriceIncrement(BigDecimal price) {
		Objects.requireNonNull(exchange);
		Objects.requireNonNull(price);
		
		switch(exchange) {
		case TSE: 
			if( price.compareTo(tseLowTickPrice) == -1)
				return tseLowTickSize;
			return tseStandardTickSize;
		case NYSE:
			return nyseStandardTickSize;
		default:
			throw new IllegalArgumentException(String.format("Unsupported exchange:%s", exchange.toString()));
		}
	}

	
	public Currency getTradeCurrency() {
		return tradeCurrency;
	}

	
	public void setTradeCurrency(Currency tradeCurrency) {
		this.tradeCurrency = tradeCurrency;
	}

	
	public BigDecimal getPassiveExchangeFeePerShare() {
		return passiveExchangeFeePerShare;
	}

	
	public void setPassiveExchangeFeePerShare(BigDecimal passiveExchangeFeePerShare) {
		this.passiveExchangeFeePerShare = passiveExchangeFeePerShare;
	}

	
	public BigDecimal getAgressiveExchangeFeePerShare() {
		return agressiveExchangeFeePerShare;
	}

	
	public void setAgressiveExchangeFeePerShare(BigDecimal agressiveExchangeFeePerShare) {
		this.agressiveExchangeFeePerShare = agressiveExchangeFeePerShare;
	}

	public BigDecimal getRoutedExchangeFeePerShare() {
		return routedExchangeFeePerShare;
	}

	public void setRoutedExchangeFeePerShare(BigDecimal routedExchangeFeePerShare) {
		this.routedExchangeFeePerShare = routedExchangeFeePerShare;
	}
	
	public LocalDateTime getTradingStartTime() {
		return tradingStartTime;
	}

	public void setTradingStartTime(LocalDateTime tradingStartTime) {
		this.tradingStartTime = tradingStartTime;
	}

	public LocalDateTime getTradingStopTime() {
		return tradingStopTime;
	}

	public void setTradingStopTime(LocalDateTime tradingStopTime) {
		this.tradingStopTime = tradingStopTime;
	}


	public Exchange getExchange() {
		return exchange;
	}


	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}
}
