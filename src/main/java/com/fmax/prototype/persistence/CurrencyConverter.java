package com.fmax.prototype.persistence;

import java.util.Currency;

import javax.persistence.AttributeConverter;

public class CurrencyConverter implements AttributeConverter<Currency,String> {

	//TODO make unit test
	public static void main(String[] args) {
		CurrencyConverter converter = new CurrencyConverter();
		Currency original = Currency.getInstance("CAD");
		
		Currency processed =  converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));
		
		System.out.println( original.equals(processed));
	}

	
	@Override
	public String convertToDatabaseColumn(Currency attribute) {
		if(attribute != null)
		   return attribute.toString();
		return null;
	}

	
	@Override
	public Currency convertToEntityAttribute(String dbData) {
		if(dbData != null)
			return Currency.getInstance(dbData);
		return null;
	}
}
