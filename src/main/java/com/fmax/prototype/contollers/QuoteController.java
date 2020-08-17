package com.fmax.prototype.contollers;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fmax.prototype.components.ExchangeMetadataService;
import com.fmax.prototype.components.TradeExecutive;
import com.fmax.prototype.components.TradeGovernor;
import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.configuration.TradeExecutiveConfiguration;
import com.fmax.prototype.model.quote.StockQuote;



@RestController
@RequestMapping("/bid-ask")
public class QuoteController {
	@Autowired ExchangeMetadataService exchangeMetadataService;
	
	TradeGovernor tradeMonitor = new TradeGovernor();
	TradeExecutiveConfiguration rbcParameters = initRBCParameters();
	
	TradeExecutive rbcTradeExecutive = new TradeExecutive(tradeMonitor, rbcParameters, exchangeMetadataService) ;

	protected TradeExecutiveConfiguration initRBCParameters() {
		TradeExecutiveConfiguration parameters = new TradeExecutiveConfiguration();
		
		parameters.setBuyStockExchange(Exchange.TSE);
		parameters.setSellStockExchange(Exchange.NYSE);
		parameters.setCusip("780087102");
		parameters.setCancelLeewayPerShareCDN( new BigDecimal("0.0025"));
		
		return parameters;
	}
	
	
	@GetMapping(path="/ping")
	String ping() {
		return "Hello from ping.";
	}
	
	@PostMapping(path="/stockquote", consumes=MediaType.APPLICATION_JSON_VALUE)
	String receiveStockQuote(@RequestBody StockQuote stockQuote) {
		stockQuote.setInstantReceived(Instant.now());
		rbcTradeExecutive.receiveQuote(stockQuote);
		return "received";
	}
}
