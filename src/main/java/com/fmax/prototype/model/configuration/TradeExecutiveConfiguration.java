package com.fmax.prototype.model.configuration;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ISIN;

@Component
public class TradeExecutiveConfiguration {
	Exchange   buyStockExchange;
	Exchange   sellStockExchange;
	
	ISIN       isin;
	int 	   defaultBidSize                 = 100;
	int 	   defaultAskSize                 = 100;
	BigDecimal netProfitPerShareCDN ;
	BigDecimal cancelLeewayPerShareCDN;
	BigDecimal netProfitPerShareUS;
	BigDecimal cancelLeewayPerShareUS;
	BigDecimal mininumCdnBidPostingRatio;
	BigDecimal maxiumCdnBidPostingRatio;
	BigDecimal stopLossAmount;
	
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

	public BigDecimal getNetProfitPerShareUS() {
		return netProfitPerShareUS;
	}

	public void setNetProfitPerShareUS(BigDecimal netProfitPerShareUS) {
		this.netProfitPerShareUS = netProfitPerShareUS;
	}

	public BigDecimal getCancelLeewayPerShareUS() {
		return cancelLeewayPerShareUS;
	}

	public void setCancelLeewayPerShareUS(BigDecimal cancelLeewayPerShareUS) {
		this.cancelLeewayPerShareUS = cancelLeewayPerShareUS;
	}

	public BigDecimal getMininumCdnBidPostingRatio() {
		return mininumCdnBidPostingRatio;
	}

	public void setMininumCdnBidPostingRatio(BigDecimal mininumCdnBidPostingRatio) {
		this.mininumCdnBidPostingRatio = mininumCdnBidPostingRatio;
	}

	public BigDecimal getMaxiumCdnBidPostingRatio() {
		return maxiumCdnBidPostingRatio;
	}

	public void setMaxiumCdnBidPostingRatio(BigDecimal maxiumCdnBidPostingRatio) {
		this.maxiumCdnBidPostingRatio = maxiumCdnBidPostingRatio;
	}

	public BigDecimal getStopLossAmount() {
		return stopLossAmount;
	}

	public void setStopLossAmount(BigDecimal stopLossAmount) {
		this.stopLossAmount = stopLossAmount;
	}

	public ISIN getISIN() {
		return isin;
	}

	public void setISIN(ISIN isin) {
		this.isin = isin;
	}

	@Override
	public String toString() {
		return "TradeExecutiveConfiguration [buyStockExchange=" + buyStockExchange + ", sellStockExchange="
				+ sellStockExchange + ", isin=" + isin + ", defaultBidSize=" + defaultBidSize + ", defaultAskSize="
				+ defaultAskSize + ", netProfitPerShareCDN=" + netProfitPerShareCDN + ", cancelLeewayPerShareCDN="
				+ cancelLeewayPerShareCDN + ", netProfitPerShareUS=" + netProfitPerShareUS + ", cancelLeewayPerShareUS="
				+ cancelLeewayPerShareUS + ", mininumCdnBidPostingRatio=" + mininumCdnBidPostingRatio
				+ ", maxiumCdnBidPostingRatio=" + maxiumCdnBidPostingRatio + ", stopLossAmount=" + stopLossAmount + "]";
	}
}
