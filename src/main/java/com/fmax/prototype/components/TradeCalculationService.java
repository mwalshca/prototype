package com.fmax.prototype.components;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TradeCalculationService {

//	CA cancel price = round down to nearest tick (US best bid * USD/CAD FX bid - (Cancel leeway in CA + Projected initiation costs + (Projected hedge costs * USD/CAD FX ask)))
	
	BigDecimal getCanadianCancelPrice(
			BigDecimal usBestBid,
			BigDecimal usdCadBid,
			BigDecimal cdnCancelLeeway,
			BigDecimal projectedInitiationCosts,
			BigDecimal projectedHedgeCosts,
			BigDecimal usdCadAsk) {
		
		BigDecimal canadianCancelPrice = 
				usBestBid
					.multiply(usdCadBid)
					.subtract(cdnCancelLeeway)
					.subtract(projectedInitiationCosts)
					.subtract( projectedHedgeCosts.multiply(usdCadAsk))
					.setScale(2,  RoundingMode.DOWN);
		
		return canadianCancelPrice; 
	}
}
