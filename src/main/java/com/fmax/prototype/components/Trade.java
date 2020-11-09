package com.fmax.prototype.components;

import java.math.BigDecimal;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;

public class Trade {
	final Exchange   buyExchange;
	final Exchange   hedgeExchange;
	final Stock      stock;
	
	final BigDecimal buyPostingPrice;
	final BigDecimal reflexPrice; //a.k.a. the hedge price
	final BigDecimal cancelPrice;
	final int        buyPostingSize;
	

	int   sharesBought;
	int   sharesSold;
	int   sharesHeld;
	
	public Trade(
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
		this.buyPostingSize = buyPostingSize;

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
		return buyPostingSize == sharesSold;
	}
	
	
	protected void checkInvariant() {
		assert sharesHeld >= 0;
		assert sharesHeld <= buyPostingSize;
		
		assert sharesBought >= 0;
		assert sharesBought <= buyPostingSize;
		
		assert sharesSold >= 0;
		assert sharesSold <= sharesBought;
		
	}

	public int getSharesBought() {
		return sharesBought;
	}


	public void setSharesBought(int sharesBought) {
		this.sharesBought = sharesBought;
	}


	public int getSharesSold() {
		return sharesSold;
	}

	public void setSharesSold(int sharesSold) {
		this.sharesSold = sharesSold;
	}


	public int getSharesHeld() {
		return sharesHeld;
	}


	public void setSharesHeld(int sharesHeld) {
		this.sharesHeld = sharesHeld;
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
		return buyPostingSize;
	}	
}
