package com.fmax.prototype.components;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fmax.prototype.common.CalculationLogRecord;

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
	
	protected BigDecimal cadPassivePostingPrice(
			BigDecimal usBestBid,
			BigDecimal usdCadFxBid,
			BigDecimal netProfitPerShareInCA,
			BigDecimal canadianPassiveEchangeFee,
			BigDecimal canadianRSFee,
			BigDecimal usAggressiveExchangeFeePerShare) 
	{
		BigDecimal projectedInitiationCosts = canadianPassiveEchangeFee.add(canadianRSFee);
		BigDecimal projectedHedgeCosts = usAggressiveExchangeFeePerShare; //TODO add SEC fee
		
		//CA posting price = round down to nearest tick (US best bid * USD/CAD FX bid - (Net profit in CA + Projected initiation costs + (Projected hedge costs * USD/CAD FX ask)))
		BigDecimal cadPostingPrice =  usBestBid.multiply( usdCadFxBid )
											   .subtract( netProfitPerShareInCA )
				                               .subtract( projectedInitiationCosts )
				                               .subtract( projectedHedgeCosts.multiply(usdCadFxBid) )                                                            
		 									   .setScale(2,  RoundingMode.DOWN);
		
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("CA passive posting price");
		record.setVariable("usBestBid", usBestBid);
		record.setVariable("usdCadFxBid", usdCadFxBid);
		record.setVariable("netProfitInCA", netProfitPerShareInCA);
		record.setVariable("canadianPassiveEchangeFee", canadianPassiveEchangeFee);
		record.setVariable("canadianRSFee", canadianRSFee);
		record.setVariable("projectedInitiationCosts", projectedInitiationCosts);
		record.setVariable("projectedHedgeCosts", projectedHedgeCosts);
		record.setResult(cadPostingPrice);
		//TODO LOGGER.log(record);
		
		return cadPostingPrice;
	}
}
