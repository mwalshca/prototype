package com.fmax.prototype.components;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import org.springframework.stereotype.Service;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ExchangeMetadata;

@Service
public class ExchangeMetadataService {

		public ExchangeMetadata get(Exchange exchange) {
			switch(exchange) {
				case TSE:
					return tsx;
				case NYSE:
					return tsx;
				default:
					throw new IllegalArgumentException( String.format("Unsupported exchange: %s", exchange.toString()));
			}
		}
		
		private static final ExchangeMetadata tsx;
		private static final ExchangeMetadata nyse;
		
		static {
			tsx = new ExchangeMetadata();
			tsx.setAgressiveExchangeFeePerShare( new BigDecimal("0.003885") );
			tsx.setPassiveExchangeFeePerShare( new BigDecimal("0.003045") );
			tsx.setTradeCurrency(Currency.getInstance("CAD"));
			tsx.setRoutedExchangeFeePerShare( new BigDecimal("0.000000") );
			tsx.setTradingStartTime( LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0));
			tsx.setTradingStopTime(  LocalDateTime.now().withHour(16).withMinute(0).withSecond(0).withNano(0));
			
			nyse = new ExchangeMetadata();
			nyse.setAgressiveExchangeFeePerShare( new BigDecimal("0.003885") );
			nyse.setPassiveExchangeFeePerShare( new BigDecimal("0.003045") );
			nyse.setTradeCurrency(Currency.getInstance("CAD"));
			nyse.setRoutedExchangeFeePerShare( new BigDecimal("0.000000") );
			nyse.setTradingStartTime( LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0));
			nyse.setTradingStopTime(  LocalDateTime.now().withHour(16).withMinute(0).withSecond(0).withNano(0));
		}
	
}
