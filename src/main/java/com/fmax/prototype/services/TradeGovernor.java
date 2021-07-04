package com.fmax.prototype.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ForeignExchangePair;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.model.configuration.TradeExecutiveConfiguration;
import com.fmax.prototype.model.quote.ForeignExchangeQuote;


@Scope("prototype")
@Service
public class TradeGovernor {
	List<TradeExecutive> tradeExecutives = new ArrayList<>();
	QuoteService quoteService;

	public TradeGovernor(
			QuoteService quoteService,
			CalculationService tradeCalculationService,
			OrderManagementService orderManagementService,
            ExchangeMetadataService exchangeMetadataService,
            SecuritiesMasterService securityMasterService,
            CriticalEventService criticalEventService) {
		this.quoteService = quoteService;
		
		// setup the FX stream
		ForeignExchangePair fxPair = new ForeignExchangePair( ForeignExchangePair.CURRENCY_US, ForeignExchangePair.CURRENCY_CAD);
		quoteService.startStream(fxPair, this::accept);
		
		
		// hardcoded - get the stocks
		Stock buyStock = securityMasterService.getStock(Exchange.NYSE, "RY");
		Stock sellStock = securityMasterService.getStock(Exchange.TSE, "RY");
		
		
		// set up the TradeExecutive
		TradeExecutive tx = new TradeExecutive(
				this,
				init(buyStock, sellStock),
				tradeCalculationService,
				orderManagementService,
				exchangeMetadataService,
				securityMasterService,
				quoteService,
				criticalEventService
				);
		
		tradeExecutives.add(tx);
	}
	
	
	public void accept(ForeignExchangeQuote fxQuote) {
		System.out.println("Quote received:" + fxQuote.toString());
		tradeExecutives.stream().forEach( (tx)-> tx.push(fxQuote) );
	}
	
	
	private TradeExecutiveConfiguration init(Stock buyStock, Stock sellStock) {
		TradeExecutiveConfiguration parameters = new TradeExecutiveConfiguration();
		
		parameters.setPostStock(buyStock);
		parameters.setHedgeStock(sellStock);

		parameters.setCancelLeewayPerShareCDN(new BigDecimal("0.0090"));
		parameters.setNetProfitPerShareCDN(new BigDecimal("0.0098"));
		parameters.setCancelLeewayPerShareUS(new BigDecimal("0.0080"));
		parameters.setNetProfitPerShareUS(new BigDecimal("0.012"));
		parameters.setMininumCdnBidPostingRatio(new BigDecimal("0.40"));
		parameters.setMaxiumCdnBidPostingRatio(new BigDecimal("0.60"));
		parameters.setMininumNyseBidPostingRatio(new BigDecimal("0.40"));
		parameters.setMaximumNysePostingRatio( new BigDecimal("0.60") );

		return parameters;
	}
}
