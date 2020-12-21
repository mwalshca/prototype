package com.fmax.prototype.components;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fmax.prototype.common.AsyncLogger;
import com.fmax.prototype.common.BusinessLogRecordFormattter;
import com.fmax.prototype.common.CalculationLogRecord;
import com.fmax.prototype.events.Event;
import com.fmax.prototype.events.ForeignExchangeQuoteReceived;
import com.fmax.prototype.events.StockOrderAccepted;
import com.fmax.prototype.events.StockOrderCompleted;
import com.fmax.prototype.events.StockOrderFilled;
import com.fmax.prototype.events.StockOrderPlaced;
import com.fmax.prototype.events.StockQuoteReceived;
import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ExchangeMetadata;
import com.fmax.prototype.model.Instance;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.model.configuration.TradeExecutiveConfiguration;
import com.fmax.prototype.model.quote.IForeignExchangeQuote;
import com.fmax.prototype.model.quote.IStockQuote;
import com.fmax.prototype.model.trade.BuyOrder;
import com.fmax.prototype.model.trade.SellOrder;
import com.fmax.prototype.model.trade.StockOrder;
import com.fmax.prototype.model.trade.StockOrderType;


/** Current implementation ONLY looks at bids on the from the TSE and asks from the NYSE */
public class TradeExecutive {
	private static final Currency CAD_CURRENCY = Currency.getInstance("CAD");
	private static final Currency US_CURRENCY = Currency.getInstance("USD");
	
	private static final MathContext MATH_CONTEXT_RATIO = new MathContext(2, RoundingMode.DOWN);
	private static final MathContext MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN = new MathContext(0, RoundingMode.DOWN);
	private static final BigDecimal TWO = new BigDecimal("2.00");
	
	private static final AsyncLogger            LOGGER;
	
	static {
		Logger logger = Logger.getLogger("bd." + TradeExecutive.class.getName());
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		
		Handler handler = new  ConsoleHandler();
		handler.setFormatter(new BusinessLogRecordFormattter());
		 
		logger.addHandler(handler);
				
		LOGGER = new AsyncLogger(logger);
	}
	
	//services
	private final TradeGovernor 				tradeGovernor;
	private final OrderManagementService        orderManagementService = new OrderManagementService(this);
	private final SecuritiesMasterService       securitiesMasterService;
	private final TradeCalculationService       tradeCalculationService;
	
	//Queue and thread to receive stock quotes
	private final SynchronousQueue<IStockQuote> stockQuotesMessageQueue = new SynchronousQueue<>();
	private final Thread threadPullStockQuotes;
	  
	// Queue and thread to receive foreign exchange quotes
    private final SynchronousQueue<IForeignExchangeQuote> currencyQuotesMessageQueue = new SynchronousQueue<>();
    private final Thread threadPullForeignExchangeQuotes;
    
    // internal event queue and thread to handle it
    private final LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<>();
    private final Thread threadHandleEvents;
    
    // configuration - set during construction
    private final TradeExecutiveConfiguration   tradeExecutiveConfiguration;
    private final Stock      		stock;
    private final Exchange   		postExchange;  
    private final Exchange   		hedgeExchange; 
    private final BigDecimal 		cdnAveragePostingRatio;
    private final ExchangeMetadata 	tsxMetadata;
	private final ExchangeMetadata 	nyseMetadata;
        
    //state we care about and need to replicate:
    private final EnumMap<Exchange, AtomicReference<IStockQuote>> currentStockQuotes = new EnumMap<Exchange, AtomicReference<IStockQuote>>(Exchange.class);
    private final AtomicReference<IForeignExchangeQuote>          usCadCurrencyQuote = new AtomicReference<>();
	
    private final ConcurrentHashMap<Long,StockOrder>   stockOrdersById = new ConcurrentHashMap<>();
	
    private final ConcurrentHashMap<Long,Instance>	   tradesByStockOrderId = new ConcurrentHashMap<>();
    private final LinkedList<Instance>                 activeTrades = new LinkedList<>();
	private final HashSet<Instance>                    completedTrades = new HashSet<>();
	
	// A priority queue contain all active BuyOrders, ordered by DttmCreated, descending i.e. newest orders are first
	private final PriorityQueue<BuyOrder>              activeBuyOrdersByDttmCreatedDescending;
	
	
	public TradeExecutive(TradeGovernor tradeGovernor, 
			              TradeExecutiveConfiguration tradeExecutiveConfiguration,
			              ExchangeMetadataService exchangeMetadataService,
			              SecuritiesMasterService securitiesMasterService,
			              TradeCalculationService tradeCalculationService) 
	{
		this.tradeGovernor = tradeGovernor;
		this.tradeExecutiveConfiguration = tradeExecutiveConfiguration;
		this.securitiesMasterService = securitiesMasterService;
		this.tradeCalculationService = tradeCalculationService;
		
		stock = this.securitiesMasterService.getStock( tradeExecutiveConfiguration.getCusip() );
		assert stock != null; // TODO fatal error
				
		postExchange = tradeExecutiveConfiguration.getBuyStockExchange();
		hedgeExchange = tradeExecutiveConfiguration.getSellStockExchange();
		
		currentStockQuotes.put( postExchange, new AtomicReference<IStockQuote>() );
		currentStockQuotes.put( hedgeExchange, new AtomicReference<IStockQuote>() );
		
		
		cdnAveragePostingRatio = tradeExecutiveConfiguration.getMininumCdnBidPostingRatio()
				                 .add( tradeExecutiveConfiguration.getMaxiumCdnBidPostingRatio())
				                 .divide(TWO, TradeExecutive.MATH_CONTEXT_RATIO);
		
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("cdnAveragePostingRatio");
		record.setVariable("tradeExecutiveConfiguration.getMininumCdnBidPostingRatio()", tradeExecutiveConfiguration.getMininumCdnBidPostingRatio());
		record.setVariable("tradeExecutiveConfiguration.getMaxiumCdnBidPostingRatio",  tradeExecutiveConfiguration.getMaxiumCdnBidPostingRatio());
		record.setResult( cdnAveragePostingRatio);
		LOGGER.log(record);
					
		tsxMetadata = exchangeMetadataService.get(Exchange.TSE);
		nyseMetadata = exchangeMetadataService.get(Exchange.NYSE );
		
		activeBuyOrdersByDttmCreatedDescending = new PriorityQueue<>();
		
		// initialiase threads
		threadHandleEvents               = new Thread(this::pullEvents,  String.format("TradeExecutive-event-handler-%s", tradeExecutiveConfiguration.getCusip()) );
		threadPullStockQuotes            = new Thread(this::pullStockQuote, "TradeExecutive-event-handler-stock-quotes");
		threadPullForeignExchangeQuotes  = new Thread(this::pullForeignExchangeQuotes, "TradeExecutive-event-handler-foreign-exchange-quotes");
		
		LOGGER.info( String.format("TradeExecutive initialized. Configuration:\n\t%s\n", tradeExecutiveConfiguration));
		
		
		// start the threads
		threadHandleEvents.start();
		threadPullStockQuotes.start();
		threadPullForeignExchangeQuotes.start();
	}
	
	
	public static AsyncLogger getLogger() {
		return LOGGER;
	}

	
	// infinite loop by design. Intended to be run by a single thread.
	private void pullEvents() {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be run in the event-handling thread
		
		while (true) {
			try {
				Event event = events.take();
				LOGGER.info( String.format("\n----------------------------\nBegin processing event:%s\n",event) );
				
				switch (event.getEventType()) {
				case STOCK_QUOTE_RECEIVED:
					assert event instanceof StockQuoteReceived;
					handle( (StockQuoteReceived) event) ;
					break;
				
				case FOREIGN_EXCHANGE_QUOTE_RECEIVED:
					assert event instanceof ForeignExchangeQuoteReceived;
					handle( (ForeignExchangeQuoteReceived) event);
					break;
				
				case STOCK_ORDER_PLACED:
					assert event instanceof StockOrderPlaced;
					handle( (StockOrderPlaced) event);
					break;
					
				case STOCK_ORDER_ACCEPTED:
					assert event instanceof StockOrderAccepted;
					handle( (StockOrderAccepted) event);
					break;
					
				case STOCK_ORDER_FILLED:
					assert event instanceof StockOrderFilled;
					handle( (StockOrderFilled) event);
					break;
				
				case STOCK_ORDER_COMPLETED:
					assert event instanceof StockOrderCompleted;
					handle( (StockOrderCompleted) event);
					break;
					
				default:
					LOGGER.info(String.format("Unsupported event:%s", event.getEventType()));
					break;
				}
				LOGGER.info(String.format("\nEnd processing event:%s\n----------------------------\n\n",event.getEventType()) );
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the thread's interupted flag
			} catch (RuntimeException ex) {
				System.out.println("Danger Will Robinson!");
			}
		}
	}

	
	private void handle(StockQuoteReceived event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		//TODO BR-0004
		//TODO BR-0005
		// Verify correct stock quote
		IStockQuote stockQuote = event.getStockQuote();
		orderManagementService.push(stockQuote);
		
		boolean hedgeBestBidSizeChanged = false;
		
		if(stockQuote.getExchange().equals(hedgeExchange)) {
			IStockQuote currentHedgeQuote = currentStockQuotes.get(hedgeExchange).get();
			if(null == currentHedgeQuote)
				hedgeBestBidSizeChanged = true;
			else
				hedgeBestBidSizeChanged = stockQuote.getBidSize() != currentHedgeQuote.getBidSize();
			LOGGER.info( String.format( "Did hedgeBestBidSize change:%s", hedgeBestBidSizeChanged ) );
		}
		
		currentStockQuotes.get( stockQuote.getExchange()).set( stockQuote );
		
		if(hedgeBestBidSizeChanged) {
			checkAndAdjustParticipation();
		} 
	}

	
	private void handle(ForeignExchangeQuoteReceived event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		IForeignExchangeQuote quote = event.getForeignExchangeQuote();
		//TODO BR-0002 bid must be less than ask
		if(    US_CURRENCY.equals( quote.getBaseCurrency( ) ) 
		   && CAD_CURRENCY.equals( quote.getQuoteCurrency() ) ) 
		{
			usCadCurrencyQuote.set(quote);
		}
	}
	
	
	private void handle( StockOrderFilled event ) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		long id = event.getOrderId();
		StockOrder order = stockOrdersById.get(id);
		if(null==order) {
			//FIXME alarm
		}
		assert order != null;
		
		final Instance trade = tradesByStockOrderId.get(order.getId());
		assert trade != null;
		assert activeTrades.contains(trade);
		
		if(order.getExchange().equals(postExchange)) {
			LOGGER.info( String.format("Buy stock order filled order id:%d # of shares filled: %d", event.getOrderId(),event.getnFilled() ));	
			trade.buyFilled( event.getnFilled() );
			placeHedgeOrder( trade, event.getnFilled());
		} 
		else if(order.getExchange().equals(hedgeExchange)) {
			LOGGER.info( String.format("Hedge  stock order filled order id:%d # of shares filled: %d", event.getOrderId(),event.getnFilled() ));	
			trade.hedgeFilled( event.getnFilled( ));
		}
		else
			assert false;
	
		if( trade.isComplete() ) {
			LOGGER.info( String.format("Trade completed:%s", trade) );
			completedTrades.add( trade );
			boolean removed = activeTrades.remove( trade );
			assert removed;
		}
	}

	
	private void handle( StockOrderPlaced event ) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		long id = event.getOrderId();
		StockOrder order = stockOrdersById.get(id);
		if(null==order) {
			//TODO alarm
			return;
		}
		assert order != null;
		order.placed();
		
		LOGGER.info( String.format("Stock order placed: %s", order.toString()));
	}
	
	
	private void handle( StockOrderAccepted event ) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		long id = event.getOrderId();
		StockOrder order = stockOrdersById.get(id);
		if(null==order) {
			//TODO alarm
			return;
		}
		assert order != null;
		
		
		order.accepted( event.getDttmAccepted() );
		
		LOGGER.info( String.format("Stock order accepted: %s", order));
	}
	
	
	private void handle( StockOrderCompleted event ) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		long id = event.getOrderId();
		StockOrder order = stockOrdersById.get(id);
		if(null==order) {
			//TODO alarm
		}
		assert order != null;
		
		order.completed();
		
		tradesByStockOrderId.remove(id);
		stockOrdersById.remove(id);
		
		if( order.getType() == StockOrderType.BUY) {
			boolean removed = activeBuyOrdersByDttmCreatedDescending.remove(order);
			assert removed;
		}
		
		LOGGER.info( String.format("Stock order completed. Order: %s", order));	
	}
	
	
	private void checkAndAdjustParticipation() {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		int buySharesOutstanding = tradeCalculationService.getBuySharesOutstanding( activeTrades );
		BigDecimal currentMarketParticipationRatio = tradeCalculationService.getMarketParticipationRatio(buySharesOutstanding, currentStockQuotes.get(hedgeExchange).get().getBidSize() );
		
		if( shouldReduceParticipation( currentMarketParticipationRatio ) ) {
			LOGGER.info("Decision: reduce participation.\n");
			reduceParticipation(buySharesOutstanding);
			return;
		} else
			LOGGER.info("Decision: do not reduce participation.\n");
		
		if(shouldIncreaseParticipation(currentMarketParticipationRatio)) {
			LOGGER.info("Decision: increase participation.\n");
			increaseParticipation();
		} else
			LOGGER.info("Decision: do not increase participation.\n");
	}
	
	
	
	
	
	private boolean shouldReduceParticipation(BigDecimal currentMarketParticipationRatio) {
		CalculationLogRecord blr = new CalculationLogRecord();
		blr.setName("shouldReduceParticipation");
		blr.setVariable( "currentMarketParticipationRatio", currentMarketParticipationRatio);
		
		boolean shouldReduceParticipation = true; // until proven otherwise
		
		switch(hedgeExchange) {
		case NYSE:
			shouldReduceParticipation &= currentMarketParticipationRatio.compareTo( tradeExecutiveConfiguration.getMaxiumCdnBidPostingRatio() ) > 0;
			break;
			
		default:
			System.out.println( String.format("Unsupported hedge exchange:%s", hedgeExchange) );
			shouldReduceParticipation = false;
		}  
		
		blr.setResult(shouldReduceParticipation);
		LOGGER.log(blr);
		
		return shouldReduceParticipation;
	}
	
	
	private void reduceParticipation(int buySharesOutstanding) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread	 
		assert Exchange.TSE.equals(postExchange);
		assert Exchange.NYSE.equals(hedgeExchange);
		
		BigDecimal bdBestBidSize = new BigDecimal( currentStockQuotes.get(hedgeExchange).get().getBidSize() );
		int targetBuySharesOutstanding =  cdnAveragePostingRatio.multiply(bdBestBidSize, MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN).intValue()	;	
		int sharesToReduce = buySharesOutstanding - targetBuySharesOutstanding;
		
		if(sharesToReduce <0) {
			LOGGER.info("reduceParticipation() logical error: sharesToReduce<0"); //TODO alarm
			return;
		} else if(sharesToReduce > buySharesOutstanding) {
			LOGGER.info("reduceParticipation() logical error: sharesToReduce>buySharesOutstanding");
		}
		
		LOGGER.info( String.format("Shares to reduce on buy-side:%d", sharesToReduce));
		
		int sharesLeftToReduce = sharesToReduce;
	}
	
	
	boolean shouldIncreaseParticipation(BigDecimal currentMarketParticipationRatio) {
		CalculationLogRecord blr = new CalculationLogRecord();
		blr.setName("shouldIncreaseParticipation");

		boolean shouldIncreaseParticpation = true; // until proven otherwise

		// don't increase participation if we don't have enough data to trade
		boolean enoughDataToTrade = isEnoughDataToTrade();
		blr.setVariable("enoughDataToTrade", enoughDataToTrade);
		
		shouldIncreaseParticpation &= enoughDataToTrade;

		//check participation
		if (shouldIncreaseParticpation) {
			shouldIncreaseParticpation &= currentMarketParticipationRatio.compareTo(tradeExecutiveConfiguration.getMininumCdnBidPostingRatio()) < 0; //TODO genericiz
			blr.setVariable("mininumPostingRatio", tradeExecutiveConfiguration.getMininumCdnBidPostingRatio());
			blr.setVariable("currentMarketParticipationRatio", currentMarketParticipationRatio);
		}
		
		blr.setResult(shouldIncreaseParticpation);
		LOGGER.log(blr);
		
		return shouldIncreaseParticpation;
	}
	
	
	private boolean increaseParticipation() {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread	 
		assert Exchange.TSE.equals(postExchange);
		assert Exchange.NYSE.equals(hedgeExchange);
		
		BigDecimal buyPostingPrice = cadPostingPrice();
		int buyPostingSize = cadPostingSize();	//TODO make generic
		if(0 == buyPostingSize){
			LOGGER.info("Buy posting size is zero. Decision: do not place an initating order.");
			return false;
		}
		
		Instance newTrade = new Instance(
				postExchange,
				hedgeExchange,
				stock,
				buyPostingPrice,
				currentStockQuotes.get(hedgeExchange).get().getBid(),// reflexPrice, a.k.a. the hedge price
				null, //TODO cancelPrice,
				buyPostingSize
				);
		activeTrades.push( newTrade );
		
		LOGGER.info( String.format("Created new Instance:%s", newTrade));
		
	    // place the order
	    BuyOrder buyOrder = new BuyOrder(postExchange, stock, buyPostingSize, buyPostingPrice); 
	    buyOrder.designed();
	    stockOrdersById.put( buyOrder.getId(), buyOrder);
	    tradesByStockOrderId.put( buyOrder.getId(), newTrade);
	    orderManagementService.push(buyOrder);
	    activeBuyOrdersByDttmCreatedDescending.add(buyOrder);
	    
	    return true;
	}
	
	
	
	private void placeHedgeOrder(Instance trade, int nShares) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		SellOrder hedgeOrder = new SellOrder( hedgeExchange, stock, nShares, currentStockQuotes.get(hedgeExchange).get().getBid() );
		hedgeOrder.designed();
		stockOrdersById.put( hedgeOrder.getId(), hedgeOrder);
		tradesByStockOrderId.put(hedgeOrder.getId(), trade);
		orderManagementService.push(hedgeOrder);
	}
	
	
	// TODO refactor to generic
    int cadPostingSize() {
		IStockQuote hedgeExchangeQuote = currentStockQuotes.get(hedgeExchange).get();
		
		CalculationLogRecord record = new CalculationLogRecord();
		record.setVariable("hedge exchange StockQuote", hedgeExchangeQuote);

		if(null == hedgeExchangeQuote) { 
			record.setResult(0);
			LOGGER.log(record);	
			return 0; 
		}	
		assert hedgeExchangeQuote != null;
		
		BigDecimal bdNyseBestBidSize = new BigDecimal( hedgeExchangeQuote.getBidSize(), MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN);
		int buySharesOutstanding = tradeCalculationService.getBuySharesOutstanding( activeTrades );
		int cadPostingSize = cdnAveragePostingRatio.multiply(bdNyseBestBidSize, MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN).intValue() - buySharesOutstanding;
		if( cadPostingSize <0) {
			cadPostingSize = 0;
		}
		cadPostingSize = (cadPostingSize / 100) * 100; //round down to nearest board lot size; //TODO remove hard coding
		
		record.setName("cadPostingSize");
		record.setVariable("cdnAveragePostingRatio", cdnAveragePostingRatio);
		record.setVariable( "buySharesOutstanding", buySharesOutstanding);
		record.setResult( cadPostingSize );
		LOGGER.log(record);
		
		return cadPostingSize;
	}

	
	
	private BigDecimal cadPostingPrice() {
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("cadPostingPrice");
		
		IStockQuote tseStockQuote = currentStockQuotes.get(Exchange.TSE).get();
		// calculate the posting price
		final BigDecimal passivePostingPrice = cadPassivePostingPrice();
		if (passivePostingPrice.compareTo(tseStockQuote.getAsk()) == -1) {
			record.setVariable("passivePostingPrice", passivePostingPrice);
			record.setVariable("tseStockQuote", tseStockQuote);
			record.setResult(passivePostingPrice);
			LOGGER.log(record);
			return passivePostingPrice;
		} else {
			final BigDecimal aggressivePostingPrice = cadAggressivePostingPrice();
			record.setVariable("aggressivePostingPrice", aggressivePostingPrice);
			record.setVariable("tseStockQuote", tseStockQuote);
			record.setResult(aggressivePostingPrice);
			LOGGER.log(record);
			return aggressivePostingPrice;
		}
	}
	
	protected BigDecimal cadPassivePostingPrice() {
		BigDecimal usBestBid     =  currentStockQuotes.get(Exchange.NYSE).get().getBid();
		BigDecimal usdCadFxBid   = usCadCurrencyQuote.get().getBid();
		BigDecimal netProfitInCA = tradeExecutiveConfiguration.getNetProfitPerShareCDN();
		BigDecimal canadianPassiveEchangeFee = tsxMetadata.getPassiveExchangeFeePerShare();
		BigDecimal canadianRSFee             = tsxMetadata.getRoutedExchangeFeePerShare();
		BigDecimal projectedInitiationCosts  = canadianPassiveEchangeFee.add(canadianRSFee);
		BigDecimal projectedHedgeCosts       = nyseMetadata.getAgressiveExchangeFeePerShare(); //TODO add SEC fee
		
		//CA posting price = round down to nearest tick (US best bid * USD/CAD FX bid - (Net profit in CA + Projected initiation costs + (Projected hedge costs * USD/CAD FX ask)))
		BigDecimal cadPostingPrice =  usBestBid.multiply( usdCadFxBid )
											   .subtract( netProfitInCA )
				                               .subtract( projectedInitiationCosts )
				                               .subtract( projectedHedgeCosts.multiply(usdCadFxBid) )                                                            
		 									   .setScale(2,  RoundingMode.DOWN);
		/*
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("CA passive posting price");
		record.setVariable("usBestBid", usBestBid);
		record.setVariable("usdCadFxBid", usdCadFxBid);
		record.setVariable("netProfitInCA", netProfitInCA);
		record.setVariable("canadianPassiveEchangeFee", canadianPassiveEchangeFee);
		record.setVariable("canadianRSFee", canadianRSFee);
		record.setVariable("projectedInitiationCosts", projectedInitiationCosts);
		record.setVariable("projectedHedgeCosts", projectedHedgeCosts);
		record.setResult(cadPostingPrice);
		LOGGER.log(record);
		*/
		return cadPostingPrice;
	}

	
	protected BigDecimal cadAggressivePostingPrice() {
		BigDecimal usBestBid     = currentStockQuotes.get(Exchange.NYSE).get().getBid();
		BigDecimal usdCadFxBid   = usCadCurrencyQuote.get().getBid();
		BigDecimal netProfitInCA = tradeExecutiveConfiguration.getNetProfitPerShareCDN();
		BigDecimal canadianAggressiveEchangeFee = tsxMetadata.getAgressiveExchangeFeePerShare();
		BigDecimal canadianRSFee                = tsxMetadata.getRoutedExchangeFeePerShare();
		BigDecimal projectedInitiationCosts     = canadianAggressiveEchangeFee.add(canadianRSFee);
		BigDecimal projectedHedgeCosts          = nyseMetadata.getAgressiveExchangeFeePerShare(); //TODO add SEC fee
		
		
		//CA posting price = round down to nearest tick (US best bid * USD/CAD FX bid - (Net profit in CA + Projected initiation costs + (Projected hedge costs * USD/CAD FX ask)))
		BigDecimal cadAggressivePostingPrice = usBestBid.multiply( usdCadFxBid )
											  .subtract( netProfitInCA )
											  .subtract( projectedInitiationCosts )
											  .subtract( projectedHedgeCosts.multiply(usdCadFxBid) )
											  .setScale(2,  RoundingMode.DOWN); 
		

		/*
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("CA agressive posting price");
		record.setVariable("usBestBid", usBestBid);
		record.setVariable("usdCadFxBid", usdCadFxBid);
		record.setVariable("netProfitInCA", netProfitInCA);
		record.setVariable("canadianAggressiveEchangeFee", canadianAggressiveEchangeFee);
		record.setVariable("canadianRSFee", canadianRSFee);
		record.setVariable("projectedInitiationCosts", projectedInitiationCosts);
		record.setVariable("projectedHedgeCosts", projectedHedgeCosts);
		record.setResult(cadPostingPrice);
		LOGGER.log(record);
		*/
		return cadAggressivePostingPrice;
	}
	
	
	private boolean isEnoughDataToTrade() {
		/* CalculationLogRecord blr = new CalculationLogRecord();	
		blr.setName("isEnoughDataToTrade");
		blr.setVariable("tseStockQuote", tseStockQuote.get() );
		blr.setVariable("nyseStockQuote", nyseStockQuote.get() );
		blr.setVariable("usCadCurrencyQuote", usCadCurrencyQuote.get() );
		*/
		boolean isEnough = 
				    currentStockQuotes.get(postExchange).get() != null
				&&  currentStockQuotes.get(hedgeExchange).get() != null
				&&  usCadCurrencyQuote.get() != null;
		
	//	blr.setResult( isEnough );
	//	LOGGER.log(blr);
		
		return isEnough;
	}
	
	
	public void push(IStockQuote quote) {
		Objects.requireNonNull(quote);

		boolean put = false;
		do {
			try {
				stockQuotesMessageQueue.put(quote);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			}  catch (RuntimeException ex) {
				// for now, do nothing
				// TODO log?
			}
		} while (!put);
	}
	
	
	private void pullStockQuote() {
		assert Thread.currentThread().equals(this.threadPullStockQuotes);
		while(true) {
			StockQuoteReceived event;
			try {
				IStockQuote quote = stockQuotesMessageQueue.take();
				event = new StockQuoteReceived(quote);
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
				continue;
			}
			assert event != null;
			
			boolean put = false;
			while(!put) {
				try {
					events.put(event);
					put = true;
				} catch (InterruptedException e) {
					Thread.interrupted(); // reset the interrupted flag
				}
			}
		}
	}
		
	public void push(IForeignExchangeQuote quote) {
		boolean put = false;
		do {
			try {
				currencyQuotesMessageQueue.put(quote);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			} catch (RuntimeException ex) {
				// for now, do nothing
				// TODO log?
			}
		} while (!put);
	}
	
	
	private void pullForeignExchangeQuotes() {
		assert Thread.currentThread().equals(this.threadPullForeignExchangeQuotes);
		while(true) {
			IForeignExchangeQuote quote = null;
			try {
				 quote = currencyQuotesMessageQueue.take();
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
				continue;
			}
			assert quote != null;
			
			ForeignExchangeQuoteReceived event = new ForeignExchangeQuoteReceived(quote);
			boolean put = false;
			while(!put) {
				try {
					events.put(event);
					put = true;
				} catch (InterruptedException e) {
					Thread.interrupted(); // reset the interrupted flag
				}
			}
		}
	}
	
	
	public void pushOrderPlaced(long orderId) {
		boolean put = false;
		do {
			try {
				StockOrderPlaced event = new StockOrderPlaced(orderId);
				events.put(event);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			} catch (RuntimeException ex) {
				// for now, do nothing
				// TODO log?
			}
		} while (!put);	
	}
	

	public void pushOrderCompleted(long orderId) {
		boolean put = false;
		do {
			try {
				StockOrderCompleted event = new StockOrderCompleted(orderId);
				events.put(event);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			} catch (RuntimeException ex) {
				// for now, do nothing
				// TODO log?
			}
		} while (!put);	
	}
	
	
	public void pushOrderAccepted(long orderId,  LocalDateTime dttmAccepted) {
		boolean put = false;
		do {
			try {
				StockOrderAccepted event = new StockOrderAccepted(orderId, dttmAccepted);
				events.put(event);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			} catch (RuntimeException ex) {
				// for now, do nothing
				// TODO log?
			}
		} while (!put);	
	}
	
	
	public void pushOrderFilled(long orderId, int unitsFilled) {
		boolean put = false;
		do {
			try {
				StockOrderFilled event = new StockOrderFilled(orderId, unitsFilled);
				events.put(event);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			} catch (RuntimeException ex) {
				// for now, do nothing
				// TODO log?
			}
		} while (!put);	
	}
}
