package com.fmax.prototype.model.configuration;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.fmax.prototype.model.Exchange;

@Component
public class TradeExecutiveConfiguration {
	Exchange   buyStockExchange;
	Exchange   sellStockExchange;
	
	String     cusip;
	int 	   defaultBidSize                 = 100;
	int 	   defaultAskSize                 = 100;
	BigDecimal netProfitPerShareCDN ;
	BigDecimal cancelLeewayPerShareCDN;
	BigDecimal netProfitPerShareUS;
	BigDecimal cancelLeewayPerShareUS;
	
	
	public Exchange getBuyStockExchange() {
		return buyStockExchange;
	}

	public void setBuyStockExchange(Exchange buyStockExchange) {
		this.buyStockExchange = buyStockExchange;
	}

	public Exchange getSellStockExchange() {
		return sellStockExchange;
	}

	public void setSellStockExchange(Exchange sellStockExchange) {
		this.sellStockExchange = sellStockExchange;
	}

	public String getCusip() {
		return cusip;
	}

	public void setCusip(String cusip) {
		this.cusip = cusip;
	}

	public int getDefaultBidSize() {
		return defaultBidSize;
	}
	
	public void setDefaultBidSize(int defaultBidSize) {
		this.defaultBidSize = defaultBidSize;
	}
	
	public int getDefaultAskSize() {
		return defaultAskSize;
	}
	
	public void setDefaultAskSize(int defaultAskSize) {
		this.defaultAskSize = defaultAskSize;
	}
	
	public BigDecimal getNetProfitPerShareCDN() {
		return netProfitPerShareCDN;
	}
	
	public void setNetProfitPerShareCDN(BigDecimal netProfitPerShareCDN) {
		this.netProfitPerShareCDN = netProfitPerShareCDN;
	}
	
	public BigDecimal getCancelLeewayPerShareCDN() {
		return cancelLeewayPerShareCDN;
	}
	
	public void setCancelLeewayPerShareCDN(BigDecimal cancelLeewayPerShareCDN) {
		this.cancelLeewayPerShareCDN = cancelLeewayPerShareCDN;
	}
}
