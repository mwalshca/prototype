package com.fmax.prototype.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.fmax.prototype.model.ForeignExchangePair;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.model.quote.ForeignExchangeQuoteSink;
import com.fmax.prototype.model.quote.StockQuoteSink;
import com.fmax.prototype.services.ib.InteractiveBrokerService;
import com.ib.client.Contract;

@Service
public class QuoteService {
	final InteractiveBrokerService ibs;
	final Map<ForeignExchangePair, Contract> foreignExchangePair = new HashMap<>();
	
	public QuoteService(InteractiveBrokerService ibs) {
		this.ibs = Objects.requireNonNull(ibs);
	}
	
	
	public void startStream(Stock stock, StockQuoteSink quoteSink) {
		ibs.reqTickByTickData(stock, quoteSink);
	}
	
	
	public void startStream(ForeignExchangePair fxPair, ForeignExchangeQuoteSink sink) {
        ibs.reqTickByTickData(fxPair, sink);
	}
}
