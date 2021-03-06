package com.fmax.prototype.model;

import java.math.BigDecimal;

import com.fmax.prototype.services.TradeExecutive;

public class Arbitrage {
	final TradeExecutive tradeExecutive;
	final Stock          buyStock;
	final Stock          sellStock;
	
	final BigDecimal buyPostingPrice;
	final long       buyInitialPostingSize;
	
	final BigDecimal reflexPrice; //a.k.a. the hedge price
	final BigDecimal cancelPrice;
	
	long  sharesBought;
	long  sharesSold;
	long  sharesHeld;
	
	public Arbitrage(
			TradeExecutive tradeExecutive,
			Stock      buyStock,
			Stock      sellStock,
			BigDecimal buyPostingPrice,
			BigDecimal reflexPrice, //a.k.a. the hedge price
			BigDecimal cancelPrice,
			long       buyPostingSize) 
	{
		this.tradeExecutive = tradeExecutive;
		this.buyStock = buyStock;
		this.sellStock = sellStock;
		this.buyPostingPrice = buyPostingPrice;
		this.reflexPrice = reflexPrice;
		this.cancelPrice = cancelPrice;
		this.buyInitialPostingSize = buyPostingSize;

		checkInvariant();
	}
	
	
	public void buyFilled(int sharesFilled) {
		checkInvariant();
		sharesBought += sharesFilled;
		sharesHeld   += sharesFilled;
		checkInvariant();
	}
	
	
	public void hedgeFilled(int sharesFilled) {
		checkInvariant();
		sharesSold += sharesFilled;
		sharesHeld -= sharesFilled;
		checkInvariant();
	}
	
	
	public boolean isComplete() {
		return 		sharesBought == buyInitialPostingSize
				 && sharesSold   == buyInitialPostingSize;
				
	}
	
	
	protected void checkInvariant() {
		assert buyStock != null;
		assert sellStock != null;
		assert buyPostingPrice != null;
		assert reflexPrice != null;
		//FIXME assert cancelPrice != null;
		assert buyInitialPostingSize >= 0;
		assert sharesHeld   >= 0;
		assert sharesBought >= 0;
		assert sharesSold   >= 0;
		
		assert sharesHeld <= buyInitialPostingSize;
		assert sharesBought <= buyInitialPostingSize;
		assert sharesSold <= sharesBought;
	}

	
	public long getSharesBought() {
		return sharesBought;
	}

	
	public long getSharesSold() {
		return sharesSold;
	}

	
	public long getSharesHeld() {
		return sharesHeld;
	}

	public BigDecimal getBuyPostingPrice() {
		return buyPostingPrice;
	}


	public BigDecimal getReflexPrice() {
		return reflexPrice;
	}


	public BigDecimal getCancelPrice() {
		return cancelPrice;
	}


	public long getBuyPostingSize() {
		return buyInitialPostingSize;
	}


	public Stock getBuyStock() {
		return buyStock;
	}


	public Stock getSellStock() {
		return sellStock;
	}


	public long getBuyInitialPostingSize() {
		return buyInitialPostingSize;
	}	
}
