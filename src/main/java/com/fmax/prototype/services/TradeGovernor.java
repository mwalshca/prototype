package com.fmax.prototype.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ForeignExchangePair;
import com.fmax.prototype.model.ISIN;
import com.fmax.prototype.model.configuration.TradeExecutiveConfiguration;
import com.fmax.prototype.model.quote.ForeignExchangeQuote;
import com.fmax.prototype.services.ordermanagement.OrderManagementService;

@Service
public class TradeGovernor {
	// RY ISN = CA7800871021
	
	List<TradeExecutive> tradeExecutives = new ArrayList<>();
	QuoteService quoteService;
	
	public TradeGovernor(
			QuoteService quoteService,
			TradeCalculationService tradeCalculationService,
			OrderManagementService orderManagementService,
            ExchangeMetadataService exchangeMetadataService,
            SecuritiesMasterService securityMasterService) {
		this.quoteService = quoteService;
		
		// setup the FX stream
		ForeignExchangePair fxPair = new ForeignExchangePair( ForeignExchangePair.CURRENCY_US, ForeignExchangePair.CURRENCY_CAD);
		quoteService.startStream(fxPair, this::accept);
		
		// set up the TradeExecutive
		TradeExecutive tx = new TradeExecutive(
				this,
				init(),
				tradeCalculationService,
				orderManagementService,
				exchangeMetadataService,
				securityMasterService
				);
		
		tradeExecutives.add(tx);
	}
	
	
	public void accept(ForeignExchangeQuote fxQuote) {
		System.out.println("Quote received:" + fxQuote.toString());
		tradeExecutives.stream().forEach( (tx)-> tx.push(fxQuote) );
	}
	
	
	private TradeExecutiveConfiguration init() {
		TradeExecutiveConfiguration parameters = new TradeExecutiveConfiguration();

		parameters.setISIN(new ISIN("CA7800871021"));
		parameters.setBuyStockExchange(Exchange.TSE);
		parameters.setSellStockExchange(Exchange.NYSE);

		parameters.setCancelLeewayPerShareCDN(new BigDecimal("0.0090"));
		parameters.setNetProfitPerShareCDN(new BigDecimal("0.0098"));
		parameters.setCancelLeewayPerShareUS(new BigDecimal("0.0080"));
		parameters.setNetProfitPerShareUS(new BigDecimal("0.012"));
		parameters.setMininumCdnBidPostingRatio(new BigDecimal("0.40"));
		parameters.setMaxiumCdnBidPostingRatio(new BigDecimal("0.60"));

		return parameters;
	}
}
