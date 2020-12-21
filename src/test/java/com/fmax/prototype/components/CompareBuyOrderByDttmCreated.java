package com.fmax.prototype.components;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.model.trade.BuyOrder;

//TODO make Spring-compatible
class CompareBuyOrderByDttmCreated {
	
	@Test
	void bothNull() {
		BuyOrder lhs = null;
		BuyOrder rhs = null;
		
		int results = TradeCalculationService.compateBuyOrderByDttmCreatedDescending(lhs, rhs);
		org.junit.jupiter.api.Assertions.assertEquals(0, results);
	}

	@Test
	void lhsNotNullRhsNull() {
		BuyOrder lhs = new BuyOrder( Exchange.TSE, 
				                     new Stock("780087102", "RY.TO", "RY"),
				                     100,
				                     new BigDecimal("10.00")
				                   );
		BuyOrder rhs = null;
		
		int results = TradeCalculationService.compateBuyOrderByDttmCreatedDescending(lhs, rhs);
		org.junit.jupiter.api.Assertions.assertEquals(-1, results); 
	}
	

	@Test
	void lhsNullRhsNotNull() {
		BuyOrder lhs = null;
		
		BuyOrder rhs = new BuyOrder( Exchange.TSE, 
				                     new Stock("780087102", "RY.TO", "RY"),
				                     100,
				                     new BigDecimal("10.00")
				                   );
		
		
		int results = TradeCalculationService.compateBuyOrderByDttmCreatedDescending(lhs, rhs);
		org.junit.jupiter.api.Assertions.assertEquals(1, results); 
	}
	
	@Test
	void rhsAfterLhs() {
		BuyOrder lhs = new BuyOrder( Exchange.TSE, 
                new Stock("780087102", "RY.TO", "RY"),
                100,
                new BigDecimal("10.00")
              );

		BuyOrder rhs = new BuyOrder( Exchange.TSE, 
				                     new Stock("780087102", "RY.TO", "RY"),
				                     100,
				                     new BigDecimal("10.00"),
				                     lhs.getDttmCreated().plus(10, ChronoUnit.SECONDS)
				                   );
		
		assert lhs.getDttmCreated().compareTo(rhs.getDttmCreated()) == -1;
		
		int results = TradeCalculationService.compateBuyOrderByDttmCreatedDescending(lhs, rhs);
		org.junit.jupiter.api.Assertions.assertEquals(1, results); 
	}
	

	@Test
	void rhsBeforelhs() {
		BuyOrder lhs = new BuyOrder( Exchange.TSE, 
                new Stock("780087102", "RY.TO", "RY"),
                100,
                new BigDecimal("10.00")
              );

		BuyOrder rhs = new BuyOrder( Exchange.TSE, 
				                     new Stock("780087102", "RY.TO", "RY"),
				                     100,
				                     new BigDecimal("10.00"),
				                     lhs.getDttmCreated().minus(10, ChronoUnit.SECONDS)
				                   );
		
		assert lhs.getDttmCreated().compareTo(rhs.getDttmCreated()) == -1;
		
		int results = TradeCalculationService.compateBuyOrderByDttmCreatedDescending(lhs, rhs);
		org.junit.jupiter.api.Assertions.assertEquals(1, results); 
	}
}
