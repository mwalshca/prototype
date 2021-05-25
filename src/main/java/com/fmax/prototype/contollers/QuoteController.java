 package com.fmax.prototype.contollers;

import javax.annotation.PostConstruct;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fmax.prototype.model.quote.ForeignExchangeQuote;
import com.fmax.prototype.model.quote.StockQuote;



@RestController
@RequestMapping("/quotes")
public class QuoteController {
	
	

	@PostConstruct 
	private void initialize() {
		
		
	}
	
	
	
	
	@GetMapping(path="/ping")
	String ping() {
		return "Hello from ping.";
	}
	
	@PostMapping(path="/stock", consumes=MediaType.APPLICATION_JSON_VALUE)
	String receiveStockQuote(@RequestBody StockQuote stockQuote) {
		//rbcTradeExecutive.push(stockQuote);
		return "received";
	}
	
	@PostMapping(path="/fx", consumes=MediaType.APPLICATION_JSON_VALUE)
	String receiveForeignExchangeQuote(@RequestBody ForeignExchangeQuote foreignExchangeQuote) {
		//rbcTradeExecutive.push(foreignExchangeQuote);
		return "received";
	}
}
