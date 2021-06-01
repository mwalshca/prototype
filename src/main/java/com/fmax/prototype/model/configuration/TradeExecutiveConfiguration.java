package com.fmax.prototype.model.configuration;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.fmax.prototype.model.Stock;

@Component
public class TradeExecutiveConfiguration {
	Stock   buyStock;
	Stock   sellStock;
	
	
	int 	   defaultBidSize                 = 100;
	int 	   defaultAskSize                 = 100;
	BigDecimal netProfitPerShareCDN ;
	BigDecimal cancelLeewayPerShareCDN;
	BigDecimal netProfitPerShareUS;
	BigDecimal cancelLeewayPerShareUS;
	BigDecimal mininumCdnBidPostingRatio;
	BigDecimal maxiumCdnBidPostingRatio;
	BigDecimal stopLossAmount;
	
	
	public Stock getBuyStock() {
		return buyStock;
	}

	public void setBuyStock(Stock buyStock) {
		this.buyStock = buyStock;
	}

	public Stock getSellStock() {
		return sellStock;
	}

	public void setSellStock(Stock sellStock) {
		this.sellStock = sellStock;
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

	

}
