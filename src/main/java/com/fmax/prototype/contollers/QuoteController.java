package com.fmax.prototype.contollers;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fmax.prototype.components.ExchangeMetadataService;
import com.fmax.prototype.components.SecuritiesMasterService;
import com.fmax.prototype.components.TradeExecutive;
import com.fmax.prototype.components.TradeGovernor;
import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.configuration.TradeExecutiveConfiguration;
import com.fmax.prototype.model.quote.ForeignExchangeQuote;
import com.fmax.prototype.model.quote.StockQuote;



@RestController
@RequestMapping("/quotes")
public class QuoteController {
	@Autowired ExchangeMetadataService exchangeMetadataService;
	@Autowired SecuritiesMasterService  securitiesMasterService;
	
	TradeGovernor tradeMonitor = new TradeGovernor();
	TradeExecutiveConfiguration rbcParameters = initRBCParameters();
	TradeExecutive rbcTradeExecutive;
	

	@PostConstruct 
	private void initialize() {
		rbcTradeExecutive = new TradeExecutive(tradeMonitor, rbcParameters, exchangeMetadataService, securitiesMasterService) ;	
	}
	
	protected TradeExecutiveConfiguration initRBCParameters() {
		TradeExecutiveConfiguration parameters = new TradeExecutiveConfiguration();
		
		parameters.setBuyStockExchange(Exchange.TSE);
		parameters.setSellStockExchange(Exchange.NYSE);
		parameters.setCusip("780087102");
		parameters.setCancelLeewayPerShareCDN( new BigDecimal("0.0090"));
		parameters.setNetProfitPerShareCDN( new BigDecimal("0.0098") );
		parameters.setCancelLeewayPerShareUS(new BigDecimal("0.0080"));
		parameters.setNetProfitPerShareUS(new BigDecimal("0.012"));
		parameters.setMininumCdnBidPostingRatio(new BigDecimal("0.40"));
		parameters.setMaxiumCdnBidPostingRatio(new BigDecimal("0.60"));
		 
		return parameters;
	}
	
	
	@GetMapping(path="/ping")
	String ping() {
		return "Hello from ping.";
	}
	
	@PostMapping(path="/stock", consumes=MediaType.APPLICATION_JSON_VALUE)
	String receiveStockQuote(@RequestBody StockQuote stockQuote) {
		rbcTradeExecutive.push(stockQuote);
		return "received";
	}
	
	@PostMapping(path="/fx", consumes=MediaType.APPLICATION_JSON_VALUE)
	String receiveForeignExchangeQuote(@RequestBody ForeignExchangeQuote foreignExchangeQuote) {
		rbcTradeExecutive.push(foreignExchangeQuote);
		return "received";
	}
}
