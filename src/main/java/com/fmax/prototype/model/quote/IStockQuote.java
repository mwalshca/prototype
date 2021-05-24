package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fmax.prototype.model.Exchange;

public interface IStockQuote {

	Exchange getExchange();

	String getSymbol();

	LocalDateTime getDateTime();

	BigDecimal getBid();

	BigDecimal getAsk();

	long getBidSize();

}