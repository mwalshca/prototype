package com.fmax.prototype.model.quote;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fmax.prototype.model.Stock;

public interface IStockQuote {

	Stock getStock();

	LocalDateTime getDateTime();

	BigDecimal getBid();

	BigDecimal getAsk();

	long getBidSize();

}