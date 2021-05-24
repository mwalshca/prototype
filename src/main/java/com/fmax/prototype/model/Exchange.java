package com.fmax.prototype.model;

public enum Exchange {
	TSE("CAD"), 
	NYSE("USD");
	
	Exchange(String currency){
		this.currency = currency;
	}
	
	String currency;
	
	public String currency() {
		return currency;
	}
}
