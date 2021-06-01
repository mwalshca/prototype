package com.fmax.prototype.model;

import java.util.Currency;
import java.util.Objects;

import javax.persistence.Convert;
import javax.persistence.Embeddable;

import com.fmax.prototype.persistence.CurrencyConverter;

@Embeddable
public class ForeignExchangePair {
	public static final Currency CURRENCY_CAD = Currency.getInstance("CAD");
	public static final Currency CURRENCY_US = Currency.getInstance("USD");
	
	@Convert(converter=CurrencyConverter.class)
	Currency baseCurrency;  // currency being bought
	
	@Convert(converter=CurrencyConverter.class)
	Currency quoteCurrency; // currency used to buy the base currency
	
	public ForeignExchangePair(Currency baseCurrency, Currency quoteCurrency) {
		this.baseCurrency = Objects.requireNonNull(baseCurrency);
		this.quoteCurrency = Objects.requireNonNull(quoteCurrency);
	}
	
	
	protected ForeignExchangePair() {} // for JPA
	
	
	public Currency getBaseCurrency() {
		return baseCurrency;
	}
	
	
	public Currency getQuoteCurrency() {
		return quoteCurrency;
	}
	
	
	@Override
	public String toString() {
		return "ForeignExchangePair [baseCurrency=" + baseCurrency + ", quoteCurrency=" + quoteCurrency + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseCurrency == null) ? 0 : baseCurrency.hashCode());
		result = prime * result + ((quoteCurrency == null) ? 0 : quoteCurrency.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForeignExchangePair other = (ForeignExchangePair) obj;
		if (baseCurrency == null) {
			if (other.baseCurrency != null)
				return false;
		} else if (!baseCurrency.equals(other.baseCurrency))
			return false;
		if (quoteCurrency == null) {
			if (other.quoteCurrency != null)
				return false;
		} else if (!quoteCurrency.equals(other.quoteCurrency))
			return false;
		return true;
	}
}
