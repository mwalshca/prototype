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
public class CalculationService {
	private static final AsyncLogger            LOGGER;
	private final CriticalEventService criticalEventService;
	
	public CalculationService(CriticalEventService criticalEventService) {
		this.criticalEventService = criticalEventService;
	}
	
	static {
		Logger logger = Logger.getLogger("bd." + CalculationService.class.getName());
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
    
    
	public BigDecimal marketParticipationRatio(int buySharesOutstanding, long hedgeMarketBestBidSize) {	
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
	public BigDecimal tseCancelPrice(
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
	
	public BigDecimal tsePassivePostingPrice(
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
		criticalEventService.push(record);
		
		return cadPostingPrice;
	}
	
	
	//If US posting price < US Best Ask	US posting price =
	//	round down to nearest tick 
	//			(CA best bid / USD/CAD FX ask 
	//	        - (Net profit in US + Projected initiation costs
	//           + (Projected hedge costs / USD/CAD FX bid)))
	
	//If US posting price >= US Best Ask 
	//US posting price = 
	// round down to nearest tick (CA best bid / USD/CAD FX ask 
	// - (Net profit in US + Projected initiation costs + (Projected hedge costs / USD/CAD FX bid)))
	//
	// Projected initiation costs = US passive exchange fee per share
	public BigDecimal nysePostingPrice(
			long       numberOfUnits,
			BigDecimal usBestAsk, 
			BigDecimal cdnBestBid,  
			BigDecimal usdCadFxAsk, 
			BigDecimal netProfitInUS,
			BigDecimal usPassiveExchangeFeePerShare,
			BigDecimal usAggressiveExchangeFeePerShare,
			BigDecimal cadAgressiveExchangeFeePerShare,
			BigDecimal rsFee) {
		
		BigDecimal usPassivePostingPrice = nysePassivePostingPrice(
				numberOfUnits,
				cdnBestBid,  
				usdCadFxAsk, 
				netProfitInUS,
				usPassiveExchangeFeePerShare,
				cadAgressiveExchangeFeePerShare,
				rsFee);
		
		if( usPassivePostingPrice.compareTo(usBestAsk) == -1) {
			CalculationLogRecord record = new CalculationLogRecord();
			record.setName("usPostingPrice");
			record.setVariable("usPassivePostingPrice", usPassivePostingPrice);
			record.setVariable("usBestAsk", usBestAsk);
			record.setResult( usPassivePostingPrice );
			LOGGER.log(record);
			
			return usPassivePostingPrice;	
		} 
		
		// return the aggressive posting price
		BigDecimal usAggressivePostingPrice  = nyseAggressivePostingPrice(
				numberOfUnits,
				cdnBestBid,  
				usdCadFxAsk, 
				netProfitInUS,
				usAggressiveExchangeFeePerShare,
			    cadAgressiveExchangeFeePerShare,
				rsFee); 
		
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("usPostingPrice");
		record.setVariable("usPassivePostingPrice", usPassivePostingPrice);
		record.setVariable("usAggressivePostingPrice", usAggressivePostingPrice);
		record.setVariable("usBestAsk", usBestAsk);
		record.setResult( usAggressivePostingPrice );
		LOGGER.log(record);
		
		return usAggressivePostingPrice;
	}
	
	public long nysePostingSize(
		    long hedgeBestBidSize,
			long postSharesOutsanding,
			BigDecimal averagePostingRatio) {
		
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("nysePostingSize");
		record.setVariable("hedge exchange StockQuote", hedgeBestBidSize);
		record.setVariable("postSharesOutstanding", postSharesOutsanding);
		record.setVariable("averagePostingRatio", averagePostingRatio);
		
		
		BigDecimal postExchangeBestBidSize = new BigDecimal( hedgeBestBidSize, RPMMath.MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN);		
		long postingSize = averagePostingRatio.multiply(postExchangeBestBidSize, RPMMath.MATH_CONTEXT_RATIO).longValue() - postSharesOutsanding;
		if( postingSize <0) {
			postingSize = 0;
		}
		postingSize = (postingSize / 100) * 100; //round down to nearest board lot size; //TODO remove hard coding
		record.setVariable("postingSize", postingSize);
		LOGGER.log(record);
	//	TODO eventService.push(record);
		
		return postingSize;
	}
	
	
	// US (passive) posting price = round down to nearest tick (CA best bid / USD/CAD FX ask - (Net profit in US + Projected initiation costs + (Projected hedge costs / USD/CAD FX bid)))
	// Projected initiation costs = US passive exchange fee per share
    // Projected hedge costs = CA aggressive exchange fee per share + RS fee
	public BigDecimal nysePassivePostingPrice(
			long       numberOfUnits,
			BigDecimal cdnBestBid,  
			BigDecimal usdCadFxAsk, 
			BigDecimal netProfitInUS,
			BigDecimal usPassiveExchangeFeePerShare,
			BigDecimal cadAgressiveExchangeFeePerShare,
			BigDecimal rsFee) {
		BigDecimal bdNumberOfUnits = new BigDecimal( numberOfUnits ) ;
		
		BigDecimal projectedInitiationCosts = usPassiveExchangeFeePerShare.multiply(bdNumberOfUnits);
		
		BigDecimal projectedHedgeCostsInUSD = cadAgressiveExchangeFeePerShare.multiply( bdNumberOfUnits )
										      	.add( rsFee )
										      	.divide( usdCadFxAsk );
		
		 BigDecimal price = 
				 	cdnBestBid.divide( usdCadFxAsk )
				   		.subtract( netProfitInUS )
				   		.subtract( projectedInitiationCosts )
				   		.subtract( projectedHedgeCostsInUSD )
				   		.setScale(2, RoundingMode.DOWN); 
		

		 //TODO do this only if logging enabled for calculation
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("usPassivePostingPrice");
		record.setVariable("numberOfUnits", numberOfUnits);
		record.setVariable("cdnBestBid", cdnBestBid);
		record.setVariable("usdCadFxAsk", usdCadFxAsk);
		record.setVariable("netProfitInUS", netProfitInUS);
		record.setVariable("usPassiveExchangeFeePerShare", usPassiveExchangeFeePerShare);
		record.setVariable("cadAgressiveExchangeFeePerShare", cadAgressiveExchangeFeePerShare);
		record.setVariable("rsFee", rsFee);
		record.setResult( price );
		LOGGER.log(record);
		
		return price;
	}
	
	//US (aggressive) posting price = round down to nearest tick (CA best bid / USD/CAD FX ask - (Net profit in US + Projected initiation costs + (Projected hedge costs / USD/CAD FX bid)))
	// Projected initiation costs = US aggressive exchange fee per share
	// Projected hedge costs = CA aggressive exchange fee per share + RS fee
	public BigDecimal nyseAggressivePostingPrice(
			long       numberOfUnits,
			BigDecimal cdnBestBid,  
			BigDecimal usdCadFxAsk, 
			BigDecimal netProfitInUS,
			BigDecimal usAggressiveExchangeFeePerShare,
			BigDecimal cadAgressiveExchangeFeePerShare,
			BigDecimal rsFee) {
		BigDecimal bdNumberOfUnits = new BigDecimal( numberOfUnits ) ;
		
		BigDecimal projectedInitiationCosts = usAggressiveExchangeFeePerShare.multiply(bdNumberOfUnits);
		
		BigDecimal projectedHedgeCostsInUSD = cadAgressiveExchangeFeePerShare.multiply( bdNumberOfUnits )
										      	.add( rsFee )
										      	.divide( usdCadFxAsk );
		
		BigDecimal price = 
				 	cdnBestBid.divide( usdCadFxAsk )
				   		.subtract( netProfitInUS )
				   		.subtract( projectedInitiationCosts )
				   		.subtract( projectedHedgeCostsInUSD )
				   		.setScale(2, RoundingMode.DOWN);
				   		
		 			
		
		 //TODO do this only if logging enabled for calculation
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("usAggressivePostingPrice");
		record.setVariable("numberOfUnits", numberOfUnits);
		record.setVariable("cdnBestBid", cdnBestBid);
		record.setVariable("usdCadFxAsk", usdCadFxAsk);
		record.setVariable("netProfitInUS", netProfitInUS);
		record.setVariable("usAggressiveExchangeFeePerShare", usAggressiveExchangeFeePerShare);
		record.setVariable("cadAgressiveExchangeFeePerShare", cadAgressiveExchangeFeePerShare);
		record.setVariable("rsFee", rsFee);
		record.setResult( price );
		LOGGER.log(record);
			
		return price;
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
