package com.fmax.prototype.services;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;


@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes=CalculationService.class)
public class CalculationServiceTests {
	@Autowired
	CalculationService fixture;
	
	@MockBean
	CriticalEventService criticalEventService;
	
	@MockBean
	ExchangeMetadataService exchangeMetadataService;

	BigDecimal CAD_BEST_BID = new BigDecimal("125.59");
	BigDecimal USD_CAD_FX_ASK = new BigDecimal("1.2500");
	BigDecimal NET_PROFIT_IN_US = new BigDecimal("0.012");
	BigDecimal US_AGGRESSIVE_EXCHANGE_FEE_PER_SHARE = new BigDecimal("0.003");
	BigDecimal US_PASSIVE_EXCHANGE_FEE_PER_SHARE = new BigDecimal("-0.002");
	BigDecimal CAD_AGGRESSIVE_EXCHANGE_FEE_PER_SHARE = new BigDecimal( "0.003885" );
	BigDecimal RS_FEE = new BigDecimal("0.000105");
	
	@Test
	public void testUsAggressivePostingPrice() {

		BigDecimal price = fixture.nyseAggressivePostingPrice(
				100, 
				CAD_BEST_BID, 
				USD_CAD_FX_ASK, 
				NET_PROFIT_IN_US, 
				US_AGGRESSIVE_EXCHANGE_FEE_PER_SHARE, 
				CAD_AGGRESSIVE_EXCHANGE_FEE_PER_SHARE, 
				RS_FEE
				);	
		
		assert price.equals( new BigDecimal("99.84").setScale(2));
	}
	
	@Test
	public void testUsPassivePostingPrice() {

		BigDecimal price = fixture.nysePassivePostingPrice(
				100, 
				CAD_BEST_BID, 
				USD_CAD_FX_ASK, 
				NET_PROFIT_IN_US, 
				US_PASSIVE_EXCHANGE_FEE_PER_SHARE, 
				CAD_AGGRESSIVE_EXCHANGE_FEE_PER_SHARE, 
				RS_FEE
				);	
		
		assert price.equals( new BigDecimal("100.34").setScale(2));
	}
	
	
	@Test 
	public void testUSPostingPriceLessThanUsBestAsk() {
		BigDecimal price = fixture.nysePostingPrice(
				100, 
				new BigDecimal("102.00"),
				CAD_BEST_BID, 
				USD_CAD_FX_ASK, 
				NET_PROFIT_IN_US, 
				US_PASSIVE_EXCHANGE_FEE_PER_SHARE, 
				US_AGGRESSIVE_EXCHANGE_FEE_PER_SHARE,
				CAD_AGGRESSIVE_EXCHANGE_FEE_PER_SHARE, 
				RS_FEE
				);	
	}
	
	@Test 
	public void testUSPostingPriceNotLessThanUsBestAsk() {
		BigDecimal price = fixture.nysePostingPrice(
				100, 
				new BigDecimal("100.10"),
				CAD_BEST_BID, 
				USD_CAD_FX_ASK, 
				NET_PROFIT_IN_US, 
				US_PASSIVE_EXCHANGE_FEE_PER_SHARE, 
				US_AGGRESSIVE_EXCHANGE_FEE_PER_SHARE,
				CAD_AGGRESSIVE_EXCHANGE_FEE_PER_SHARE, 
				RS_FEE
				);	
		//assert price.equals( new BigDecimal("100.34").setScale(2));
	}
}


