package com.fmax.prototype;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.model.quote.ForeignExchangeQuote;
import com.fmax.prototype.model.quote.StockQuote;
import com.fmax.prototype.services.QuoteService;
import com.fmax.prototype.services.SecuritiesMasterService;
import com.fmax.prototype.services.TradeGovernor;

@SpringBootApplication
public class Driver {
	
	public static void main(String[] args) {
		SpringApplication.run(Driver.class, args);
	}
	
	
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			QuoteService qs = ctx.getBean(QuoteService.class);
			SecuritiesMasterService sms = ctx.getBean(SecuritiesMasterService.class);

			//TradeGovernor tg = ctx.getBean(TradeGovernor.class);
			
			//ForeignExchangePair fxPair = new ForeignExchangePair( ForeignExchangePair.CURRENCY_US, ForeignExchangePair.CURRENCY_CAD);
	    	//qs.startStream(fxPair, Driver::accept);
			
			
			Stock ryTSE = sms.getStock(Exchange.TSE, "RY");
		    Stock ryNYSE = sms.getStock(Exchange.NYSE, "RY");
		    System.out.println(ryTSE.getIsin());
		    System.out.println(ryNYSE);
			//if(null==ryTSE)
			//	throw new RuntimeException();
			//(null==ryNYSE)
				//throw new RuntimeException();
			qs.startStream(ryNYSE, Driver::accept);
			qs.startStream(ryTSE, Driver::accept);
		};
	}

	
	public static void accept(StockQuote stockQuote) {
		System.out.println("Quote received:" + stockQuote.toString());
	}
	
	public static void accept(ForeignExchangeQuote fxQuote) {
		System.out.println("Quote received:" + fxQuote.toString());
	}



	//    startStream(ibs, "TLRY", "SMART", "TLRY");
	//  startStream(ibs, "BLDP", "SMART", "BLDP");
	// startStream(ibs, "ABX", "SMART", "GOLD");
	// startStream(ibs, "SHOP", "SMART", "SHOP");
	// startStream(ibs, "BTO", "SMART", "BTG");				
}
