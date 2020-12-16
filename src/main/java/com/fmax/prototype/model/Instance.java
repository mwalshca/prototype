package com.fmax.prototype.model;

import java.math.BigDecimal;

public class Instance {
	final Exchange   buyExchange;
	final Exchange   hedgeExchange;
	final Stock      stock;
	
	final BigDecimal buyPostingPrice;
	final int        buyInitialPostingSize;
	
	final BigDecimal reflexPrice; //a.k.a. the hedge price
	final BigDecimal cancelPrice;
	
	int   sharesBought;
	int   sharesSold;
	int   sharesHeld;
	
	public Instance(
			Exchange   buyExchange,
			Exchange   hedgeExchange,
			Stock      stock,
			BigDecimal buyPostingPrice,
			BigDecimal reflexPrice, //a.k.a. the hedge price
			BigDecimal cancelPrice,
			int        buyPostingSize) 
	{
		this.buyExchange = buyExchange;
		this.hedgeExchange = hedgeExchange;
		this.stock = stock;
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
		assert buyExchange != null;
		assert hedgeExchange != null;
		assert stock != null;
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

	
	public Exchange getBuyExchange() {
		return buyExchange;
	}


	public Exchange getHedgeExchange() {
		return hedgeExchange;
	}


	public Stock getStock() {
		return stock;
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


	@Override
	public String toString() {
		return "Instance [buyExchange=" + buyExchange + ", hedgeExchange=" + hedgeExchange + ", stock=" + stock
				+ ", buyPostingPrice=" + buyPostingPrice + ", buyInitialPostingSize=" + buyInitialPostingSize
				+ ", reflexPrice=" + reflexPrice + ", cancelPrice=" + cancelPrice + ", sharesBought=" + sharesBought
				+ ", sharesSold=" + sharesSold + ", sharesHeld=" + sharesHeld + "]";
	}
	
	
}
