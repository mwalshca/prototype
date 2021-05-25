package com.fmax.prototype.model;

import java.math.BigDecimal;

public class Instance {
	final Stock      buyStock;
	final Stock      sellStock;
	
	final BigDecimal buyPostingPrice;
	final int        buyInitialPostingSize;
	
	final BigDecimal reflexPrice; //a.k.a. the hedge price
	final BigDecimal cancelPrice;
	
	int   sharesBought;
	int   sharesSold;
	int   sharesHeld;
	
	public Instance(
			Stock      buyStock,
			Stock      sellStock,
			BigDecimal buyPostingPrice,
			BigDecimal reflexPrice, //a.k.a. the hedge price
			BigDecimal cancelPrice,
			int        buyPostingSize) 
	{
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

	
	public int getSharesBought() {
		return sharesBought;
	}

	
	public int getSharesSold() {
		return sharesSold;
	}

	
	public int getSharesHeld() {
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


	public int getBuyPostingSize() {
		return buyInitialPostingSize;
	}


	public Stock getBuyStock() {
		return buyStock;
	}


	public Stock getSellStock() {
		return sellStock;
	}


	public int getBuyInitialPostingSize() {
		return buyInitialPostingSize;
	}	
}
