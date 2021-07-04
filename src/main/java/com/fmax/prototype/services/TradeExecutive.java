package com.fmax.prototype.services;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.EnumMap;
import java.util.Iterator;
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
import com.fmax.prototype.common.EventLogRecord;
import com.fmax.prototype.events.Event;
import com.fmax.prototype.events.ForeignExchangeQuoteReceived;
import com.fmax.prototype.events.StockOrderAccepted;
import com.fmax.prototype.events.StockOrderCompleted;
import com.fmax.prototype.events.StockOrderFilled;
import com.fmax.prototype.events.StockOrderPlaced;
import com.fmax.prototype.events.StockQuoteReceived;
import com.fmax.prototype.model.Arbitrage;
import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ExchangeMetadata;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.model.configuration.TradeExecutiveConfiguration;
import com.fmax.prototype.model.quote.ForeignExchangeQuote;
import com.fmax.prototype.model.quote.IForeignExchangeQuote;
import com.fmax.prototype.model.quote.IStockQuote;
import com.fmax.prototype.model.quote.StockQuote;
import com.fmax.prototype.model.trade.BuyOrder;
import com.fmax.prototype.model.trade.SellOrder;
import com.fmax.prototype.model.trade.StockOrder;
import com.fmax.prototype.model.trade.StockOrderType;



public class TradeExecutive {
	private static final Currency CAD_CURRENCY = Currency.getInstance("CAD");
	private static final Currency US_CURRENCY = Currency.getInstance("USD");
	
	private static final MathContext MATH_CONTEXT_RATIO = new MathContext(2, RoundingMode.DOWN);
	private static final MathContext MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN = new MathContext(0, RoundingMode.DOWN);
	private static final BigDecimal TWO = new BigDecimal("2.00");
	
	private static final AsyncLogger LOGGER;
	
	static {
		Logger logger = Logger.getLogger("bd." + TradeExecutive.class.getName());
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		
		Handler handler = new  ConsoleHandler();
		handler.setFormatter(new BusinessLogRecordFormattter());
		 
		logger.addHandler(handler);
				
		LOGGER = new AsyncLogger(logger);
	}
	
	// services
	private final TradeGovernor 		     tradeGovernor;
	private final OrderManagementService     orderManagementService;
	private final CalculationService         calculationService;
	private final CriticalEventService       eventService;
	 
	// Queue and thread to receive stock quotes
	private final SynchronousQueue<StockQuote> stockQuotesMessageQueue = new SynchronousQueue<>();
	private final Thread threadPullStockQuotes;
	  
	//  Queue and thread to receive foreign exchange quotes
    private final SynchronousQueue<ForeignExchangeQuote> currencyQuotesMessageQueue = new SynchronousQueue<>();
    private final Thread threadPullForeignExchangeQuotes;
    
    // internal event queue and thread to handle it
    private final LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<>();
    private final Thread threadHandleEvents;
    
    // configuration - set during construction
    private final TradeExecutiveConfiguration   tradeExecutiveConfiguration;
    private Stock postStock;
    private Stock hedgeStock;
    private final Exchange   		postExchange;  
    private final Exchange   		hedgeExchange; 
    private final BigDecimal 		averagePostingRatio;
    private final ExchangeMetadata 	tsxMetadata;
	private final ExchangeMetadata 	nyseMetadata;
        
    // state we care about and need to replicate:
    private final EnumMap<Exchange, AtomicReference<IStockQuote>> currentStockQuotes = new EnumMap<Exchange, AtomicReference<IStockQuote>>(Exchange.class);
    private final AtomicReference<IForeignExchangeQuote>          usCadCurrencyQuote = new AtomicReference<>();
	
    private final ConcurrentHashMap<Long,StockOrder> stockOrdersById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long,Arbitrage>	 arbitragesByStockOrderId = new ConcurrentHashMap<>();
    private final LinkedList<Arbitrage>              activeArbitrages = new LinkedList<>();
	private long                                     postSharesOutstanding = 0;
	
	
	// A priority queue contain all active BuyOrders, ordered by DttmCreated, descending i.e. newest orders are first
	private final PriorityQueue<BuyOrder>              activeBuyOrdersByDttmCreatedDescending =  new PriorityQueue<>();;
	
	
	public TradeExecutive(TradeGovernor tradeGovernor, 
			              TradeExecutiveConfiguration tradeExecutiveConfiguration,
			              CalculationService tradeCalculationService,
			              OrderManagementService orderManagementService,
			              ExchangeMetadataService exchangeMetadataService,
			              SecuritiesMasterService securityMasterService,
			              QuoteService quoteService,
			              CriticalEventService ces) 
	{
		this.calculationService = tradeCalculationService;
		this.orderManagementService = orderManagementService;
		this.eventService = ces;
		this.tradeGovernor = tradeGovernor;
		this.tradeExecutiveConfiguration = tradeExecutiveConfiguration;
		
		this.postStock = Objects.requireNonNull(tradeExecutiveConfiguration.getPostStock());
		this.hedgeStock = Objects.requireNonNull(tradeExecutiveConfiguration.getHedgeStock());
		
		if(!postStock.getIsin().equals(hedgeStock.getIsin())) {
			System.err.println("TradeExecutive: ISIN's don't match");
			throw new IllegalArgumentException("TradeExecutive: ISIN's don't match");
		}
		
		this.tsxMetadata = exchangeMetadataService.get(Exchange.TSE);
		this.nyseMetadata = exchangeMetadataService.get(Exchange.NYSE );
		
		this.postExchange = tradeExecutiveConfiguration.getPostStock().getExchange();
		this.hedgeExchange = tradeExecutiveConfiguration.getHedgeStock().getExchange();
		
		this.currentStockQuotes.put( postExchange, new AtomicReference<IStockQuote>() );
		this.currentStockQuotes.put( hedgeExchange, new AtomicReference<IStockQuote>() );
		
		this.averagePostingRatio = tradeExecutiveConfiguration.getMininumCdnBidPostingRatio()
				                 .add( tradeExecutiveConfiguration.getMaxiumCdnBidPostingRatio())
				                 .divide(TWO, TradeExecutive.MATH_CONTEXT_RATIO);
		
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("averagePostingRatio");
		record.setVariable("tradeExecutiveConfiguration.getMininumCdnBidPostingRatio()", tradeExecutiveConfiguration.getMininumCdnBidPostingRatio());
		record.setVariable("tradeExecutiveConfiguration.getMaxiumCdnBidPostingRatio",  tradeExecutiveConfiguration.getMaxiumCdnBidPostingRatio());
		record.setResult( averagePostingRatio);
		LOGGER.log(record);
		ces.push(record);			
		
		// initialize threads
		threadHandleEvents               = new Thread(this::pullEvents,  String.format("TradeExecutive-event-handler-%s-%s",postStock.getExchange(), postStock.getSymbol() ) );
		threadPullStockQuotes            = new Thread(this::pullStockQuote, "TradeExecutive-event-handler-stock-quotes");
		threadPullForeignExchangeQuotes  = new Thread(this::pullForeignExchangeQuotes, "TradeExecutive-event-handler-foreign-exchange-quotes");
		
		LOGGER.info( String.format("TradeExecutive initialized. Configuration:\n\t%s\n", tradeExecutiveConfiguration));
	
		// start the threads
		threadHandleEvents.start();
		threadPullStockQuotes.start();
		threadPullForeignExchangeQuotes.start();
		
		//start the stock streams
		quoteService.startStream(postStock,  this::push);
		quoteService.startStream(hedgeStock, this::push);
		
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
				LOGGER.log( new EventLogRecord(event) );
				eventService.push(event);
				
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
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the thread's interrupted flag
			} catch (RuntimeException ex) {
				System.out.println("Danger Will Robinson!");
				ex.printStackTrace();
			}
		}
	}

	
	private void handle(StockQuoteReceived event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		//TODO BR-0004
		//TODO BR-0005
		// Verify correct stock quote
	
		StockQuote stockQuote = event.getStockQuote();
		IStockQuote currentHedgeQuote = currentStockQuotes.get(hedgeExchange).get();
		currentStockQuotes.get( stockQuote.getStock().getExchange()).set( stockQuote );
		
		boolean hedgeBestBidSizeChanged = false;
		
		if(stockQuote.getStock().getExchange().equals(hedgeExchange)) {
			if(null == currentHedgeQuote)
				hedgeBestBidSizeChanged = true;
			else
				hedgeBestBidSizeChanged = stockQuote.getBidSize() != currentHedgeQuote.getBidSize();
			LOGGER.info( String.format( "Did hedgeBestBidSize change:%s", hedgeBestBidSizeChanged ) );
		}
		
		if(hedgeBestBidSizeChanged) {
			checkAndAdjustParticipation();
		} 
		assert stockQuote.equals(currentStockQuotes.get(stockQuote.getStock().getExchange()).get());
	}

	
	private void handle(ForeignExchangeQuoteReceived event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		IForeignExchangeQuote quote = event.getForeignExchangeQuote();
		//TODO BR-0002 bid must be less than ask
		if(    US_CURRENCY.equals( quote.getForeignExchangePair().getBaseCurrency() ) 
		   && CAD_CURRENCY.equals( quote.getForeignExchangePair().getQuoteCurrency() ) ) 
		{
			usCadCurrencyQuote.set(quote);
		}
	}
	
	
	private void handle( StockOrderFilled event ) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		postSharesOutstanding -= event.getnFilled();
		
		long id = event.getOrderId();
		StockOrder order = stockOrdersById.get(id);
		if(null==order) {
			return;
			//FIXME alarm
		}
		assert order != null;
		
		final Arbitrage arbitage = arbitragesByStockOrderId.get(order.getId());
		assert arbitage != null;
		assert activeArbitrages.contains(arbitage);
		
		postSharesOutstanding -= event.getnFilled();
		if(order.getStock().getExchange().equals(postExchange)) {	
			arbitage.buyFilled( event.getnFilled() );
			placeHedgeOrder( arbitage, event.getnFilled());
		} 
		else if(order.getStock().getExchange().equals(hedgeExchange)) {
			arbitage.hedgeFilled( event.getnFilled( ));
		}
		else
			assert false;
	
		if( arbitage.isComplete() ) {
			LOGGER.info( String.format("Arbitrage completed:%s", arbitage) );
			boolean removed = activeArbitrages.remove( arbitage );
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
		
		arbitragesByStockOrderId.remove(id);
		stockOrdersById.remove(id);
		
		if( order.getType() == StockOrderType.BUY) {
			boolean removed = activeBuyOrdersByDttmCreatedDescending.remove(order);
			assert removed;
		}
	}
	
	
	private void checkAndAdjustParticipation() {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		int buySharesOutstanding = calculationService.getBuySharesOutstanding( activeArbitrages );
		BigDecimal currentMarketParticipationRatio = calculationService.marketParticipationRatio(buySharesOutstanding, currentStockQuotes.get(hedgeExchange).get().getBidSize() );
		
		if( shouldReduceParticipation( currentMarketParticipationRatio ) ) {
			LOGGER.info("Decision: reduce participation.\n");
			reduceParticipation(buySharesOutstanding);
			return;
		} else
			LOGGER.info("Decision: do not reduce participation.\n");
		
		if(shouldIncreaseParticipation(currentMarketParticipationRatio)) {
			LOGGER.info("Decision: increase participation.\n");
			try {
				increaseParticipation();
			} catch (UnsupportedExchangeException e) {
				// FIXME Danger Will Robinson
				e.printStackTrace();
			}
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
			shouldReduceParticipation &= currentMarketParticipationRatio.compareTo( tradeExecutiveConfiguration.getMaximumNysePostingRatio() ) > 0;
			break;
		case TSE:
			shouldReduceParticipation &= currentMarketParticipationRatio.compareTo( tradeExecutiveConfiguration.getMaxiumCdnBidPostingRatio() ) > 0;
			break;
		default:
			System.out.println( String.format("Unsupported hedge exchange:%s", hedgeExchange) );
			shouldReduceParticipation = false;
		}  
		
		blr.setResult(shouldReduceParticipation);
		LOGGER.log(blr);
		eventService.push(blr);
		
		return shouldReduceParticipation;
	}
	
	
	private void reduceParticipation(int buySharesOutstanding) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread	 
		assert Exchange.TSE.equals(postExchange);
		assert Exchange.NYSE.equals(hedgeExchange);
		
		BigDecimal bdBestBidSize = new BigDecimal( currentStockQuotes.get(hedgeExchange).get().getBidSize() );
		int targetBuySharesOutstanding =  averagePostingRatio.multiply(bdBestBidSize, MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN).intValue()	;	
		int sharesToReduce = buySharesOutstanding - targetBuySharesOutstanding;
		
		if(sharesToReduce <0) {
			LOGGER.info("reduceParticipation() logical error: sharesToReduce<0"); //TODO alarm
			return;
		} else if(sharesToReduce > buySharesOutstanding) {
			LOGGER.info("reduceParticipation() logical error: sharesToReduce>buySharesOutstanding"); //TODO alarm
			return;
		}
		
		LOGGER.info( String.format("Shares to reduce on buy-side:%d", sharesToReduce));
		
		int sharesLeftToReduce = sharesToReduce;
		Iterator<BuyOrder> iBuyOrders = this.activeBuyOrdersByDttmCreatedDescending.iterator();
		while( sharesLeftToReduce>0 && iBuyOrders.hasNext() ) {
			StockOrder buyOrder = iBuyOrders.next();
			assert buyOrder != null;
			
			Arbitrage trade = arbitragesByStockOrderId.get( buyOrder.getId() );
			assert trade != null;
			if(buyOrder.getQuantityOrdered() > trade.getSharesBought()) {
				
			}
		}
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
		eventService.push(blr);
		
		return shouldIncreaseParticpation;
	}
	
	
	private boolean increaseParticipation() throws UnsupportedExchangeException {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread	 
		assert currentStockQuotes.get( hedgeExchange ) != null;
		BigDecimal postingPrice;
		long postingSize;
		
		if(Exchange.TSE == postExchange) {
			postingPrice = cadPostingPrice();
			postingSize = cadPostingSize();
			if(0 == postingSize){
				LOGGER.info("Buy posting size is zero. Decision: do not place an initating order.");
				return false;
			}
		} else if(Exchange.NYSE == postExchange) {
			IStockQuote hedgeQuote = currentStockQuotes.get( hedgeExchange ).get();
			long bestBidSize = hedgeQuote.getBidSize();
			
			postingSize = calculationService.nysePostingSize(
					bestBidSize,
					postSharesOutstanding,
					averagePostingRatio);
			
			postingPrice = calculationService.nysePostingPrice(
					postingSize,
					currentStockQuotes.get(Exchange.NYSE).get().getAsk(), 
					currentStockQuotes.get(Exchange.TSE).get().getBid(), 
					usCadCurrencyQuote.get().getAsk(),
					tradeExecutiveConfiguration.getNetProfitPerShareUS(),
					nyseMetadata.getPassiveExchangeFeePerShare(),
					nyseMetadata.getAgressiveExchangeFeePerShare(),
					tsxMetadata.getAgressiveExchangeFeePerShare(),
					tsxMetadata.getRoutedExchangeFeePerShare() ); 
		} else {
			throw new UnsupportedExchangeException();
		}
		
		Arbitrage arbitrage = new Arbitrage(
				this,
				postStock, 
				hedgeStock,
				postingPrice,
				currentStockQuotes.get(hedgeExchange).get().getBid(),// reflexPrice, a.k.a. the hedge price
				null, //TODO cancelPrice,
				postingSize
				);
		activeArbitrages.push( arbitrage );
		
		LOGGER.info( String.format("Created new Arbitrage:%s", arbitrage));
		
	    // place the order
	    BuyOrder buyOrder = new BuyOrder(arbitrage, postStock, postingSize, postingPrice); 
	    orderManagementService.push( buyOrder ); //TODO handle exception
	    
	    postSharesOutstanding += postingSize;
	    stockOrdersById.put( buyOrder.getId(), buyOrder);
	    arbitragesByStockOrderId.put( buyOrder.getId(), arbitrage);
	    activeBuyOrdersByDttmCreatedDescending.add( buyOrder );
	    
	    return true;
	}
	
	
	//FIXME
	private void placeHedgeOrder(Arbitrage trade, int nShares) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		SellOrder hedgeOrder = new SellOrder(trade, hedgeExchange, hedgeStock, nShares, currentStockQuotes.get(hedgeExchange).get().getBid() );
		stockOrdersById.put( hedgeOrder.getId(), hedgeOrder);
		arbitragesByStockOrderId.put(hedgeOrder.getId(), trade);
		orderManagementService.push(hedgeOrder);
	}
	
	
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
		int buySharesOutstanding = calculationService.getBuySharesOutstanding( activeArbitrages );
		int cadPostingSize = averagePostingRatio.multiply(bdNyseBestBidSize, MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN).intValue() - buySharesOutstanding;
		if( cadPostingSize <0) {
			cadPostingSize = 0;
		}
		cadPostingSize = (cadPostingSize / 100) * 100; //round down to nearest board lot size; //TODO remove hard coding
		
		record.setName("cadPostingSize");
		record.setVariable("averagePostingRatio", averagePostingRatio);
		record.setVariable( "buySharesOutstanding", buySharesOutstanding);
		record.setResult( cadPostingSize );
		LOGGER.log(record);
		eventService.push(record);
		
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
		//eventService.push(record);	
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
		
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("CA agressive posting price");
		record.setVariable("usBestBid", usBestBid);
		record.setVariable("usdCadFxBid", usdCadFxBid);
		record.setVariable("netProfitInCA", netProfitInCA);
		record.setVariable("canadianAggressiveEchangeFee", canadianAggressiveEchangeFee);
		record.setVariable("canadianRSFee", canadianRSFee);
		record.setVariable("projectedInitiationCosts", projectedInitiationCosts);
		record.setVariable("projectedHedgeCosts", projectedHedgeCosts);
		record.setResult(cadAggressivePostingPrice);
		LOGGER.log(record);
		eventService.push(record);
		
		return cadAggressivePostingPrice;
	}
	
	
	private boolean isEnoughDataToTrade() {
		CalculationLogRecord blr = new CalculationLogRecord();	
		blr.setName("isEnoughDataToTrade");
		blr.setVariable("postExchangeStockQuote", currentStockQuotes.get(postExchange).get());
		blr.setVariable("hedgeExchangeStockQuote", currentStockQuotes.get(hedgeExchange).get() );
		blr.setVariable("usCadCurrencyQuote", usCadCurrencyQuote.get() );
		
		boolean isEnough = 
				    currentStockQuotes.get(postExchange).get() != null
				&&  currentStockQuotes.get(hedgeExchange).get() != null
				&&  usCadCurrencyQuote.get() != null;
		
		blr.setResult( isEnough );
		LOGGER.log(blr);
		eventService.push(blr);
		
		return isEnough;
	}
	
	
	public void push(StockQuote quote) {
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
				StockQuote quote = stockQuotesMessageQueue.take();
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
		
	public void push(ForeignExchangeQuote quote) {
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
			ForeignExchangeQuote quote = null;
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
			} 
		} while (!put);	
	}
}
