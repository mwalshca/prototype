package com.fmax.prototype.model.configuration;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.fmax.prototype.model.Stock;

@Component
public class TradeExecutiveConfiguration {
	Stock   postStock;
	Stock   hedgeStock;
	
	
	int 	   defaultBidSize                 = 100;
	int 	   defaultAskSize                 = 100;
	BigDecimal netProfitPerShareCDN ;
	BigDecimal cancelLeewayPerShareCDN;
	BigDecimal netProfitPerShareUS;
	BigDecimal cancelLeewayPerShareUS;
	BigDecimal mininumCdnBidPostingRatio;
	BigDecimal maxiumCdnBidPostingRatio;
	BigDecimal mininumNyseBidPostingRatio;
	BigDecimal maximumNysePostingRatio;
	BigDecimal stopLossAmount;
	
	public Stock getPostStock() {
		return postStock;
	}

	public void setPostStock(Stock buyStock) {
		this.postStock = buyStock;
	}

	public Stock getHedgeStock() {
		return hedgeStock;
	}

	public void setHedgeStock(Stock sellStock) {
		this.hedgeStock = sellStock;
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
