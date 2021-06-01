package com.fmax.prototype.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.fmax.prototype.common.AsyncLogger;
import com.fmax.prototype.common.BusinessLogRecordFormattter;
import com.fmax.prototype.common.CalculationLogRecord;
import com.fmax.prototype.common.RPMMath;
import com.fmax.prototype.model.Arbitrage;
import com.fmax.prototype.model.trade.StockOrder;

@Service
public class TradeCalculationService {
	private static final AsyncLogger            LOGGER;
	
	static {
		Logger logger = Logger.getLogger("bd." + TradeCalculationService.class.getName());
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		
		Handler handler = new  ConsoleHandler();
		handler.setFormatter(new BusinessLogRecordFormattter());
		 
		logger.addHandler(handler);
				
		LOGGER = new AsyncLogger(logger);
	}
	
    public int getBuySharesOutstanding(Collection<Arbitrage> activeTrades) {    	
    	int buySharesOutstanding = 0;
		for(Arbitrage activeTrade: activeTrades) {
			buySharesOutstanding += activeTrade.getBuyPostingSize() - activeTrade.getSharesBought();
		}
		return buySharesOutstanding;
    }
    
    
	public BigDecimal getMarketParticipationRatio(int buySharesOutstanding, long hedgeMarketBestBidSize) {	
		if(0 == buySharesOutstanding) { //optimization - avoid division if numerator is zero
			BigDecimal result = BigDecimal.ZERO.setScale(2);
					//record.setResult(result);
					//LOGGER.log(record);
			return result;			
		}
		assert buySharesOutstanding != 0;
		
		if(0 == hedgeMarketBestBidSize) { //prevent divide-by-zero error
			BigDecimal result = BigDecimal.ZERO.setScale(2);
					//record.setResult(result);
					//LOGGER.log(record);
			return result;			
		}
		
		BigDecimal bdBuySharesOutstanding = new BigDecimal(buySharesOutstanding);
		BigDecimal bdHedgeMarketBestBidSize = new BigDecimal( hedgeMarketBestBidSize);
		
		BigDecimal bdMarketParticipationRatio = bdBuySharesOutstanding.divide( bdHedgeMarketBestBidSize, RPMMath.MATH_CONTEXT_RATIO) ;
		return bdMarketParticipationRatio; 	
    }
	
	
	//	CA cancel price = round down to nearest tick (US best bid * USD/CAD FX bid - (Cancel leeway in CA + Projected initiation costs + (Projected hedge costs * USD/CAD FX ask)))
	public BigDecimal getCanadianCancelPrice(
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
		
		LOGGER.log(record);
		
		return cadPostingPrice;
	}
	
	public static int compateBuyOrderByDttmCreatedDescending(StockOrder lhs, StockOrder rhs) {
		if(null==lhs) {
			if(null==rhs)
				return 0; //null equal
			assert rhs != null;
			    return 1;         
		} else {
			assert lhs != null;
			if(null==rhs)
				return -1; 
			assert rhs != null;
			return rhs.getDttmCreated().compareTo(lhs.getDttmCreated());
		}
	}
}
