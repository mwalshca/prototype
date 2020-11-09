package com.fmax.prototype.components;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.model.configuration.TradeExecutiveConfiguration;
import com.fmax.prototype.model.quote.IForeignExchangeQuote;
import com.fmax.prototype.model.quote.IStockQuote;
import com.fmax.prototype.model.trade.BuyOrder;
import com.fmax.prototype.model.trade.SellOrder;
import com.fmax.prototype.model.trade.StockOrder;


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
		
		System.out.println( "nHandlers:" + logger.getHandlers().length);
		System.out.flush();
		
		LOGGER = new AsyncLogger(logger);
	}
	
	private final TradeGovernor 				tradeGovernor;
	private final TradeExecutiveConfiguration   tradeExecutiveConfiguration;
	private final ExchangeMetadata 				tsxMetadata;
	private final ExchangeMetadata 				nyseMetadata;
	    
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
    
    // set during construction
    final Stock    stock;
    final Exchange buyExchange;  
    final Exchange sellExchange; 
    final BigDecimal cdnAveragePostingRatio;
    
    
    //state we care about and need to replicate:
    private final AtomicReference<IStockQuote> 			 nyseStockQuote = new AtomicReference<>();
    private final AtomicReference<IStockQuote> 			 tseStockQuote = new AtomicReference<>();
    private final AtomicReference<IForeignExchangeQuote> usCadCurrencyQuote = new AtomicReference<>();
	
    
    private final ConcurrentHashMap<Long,StockOrder>     stockOrdersById = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long,Trade>			 tradesByStockOrderId = new ConcurrentHashMap<>();
    private Trade                                        activeTrade;
	private final List<Trade>                            completedTrades = new LinkedList<>();
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
				
		buyExchange = tradeExecutiveConfiguration.getBuyStockExchange();
		sellExchange = tradeExecutiveConfiguration.getSellStockExchange();
		
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
		
		threadHandleEvents               = new Thread(this::pullEvents,  String.format("TradeExecutive-event-handler-%s", tradeExecutiveConfiguration.getCusip()) );
		threadPullStockQuotes            = new Thread(this::pullStockQuote, "TradeExecutive-event-handler-stock-quotes");
		threadPullForeignExchangeQuotes  = new Thread(this::pullForeignExchangeQuotes, "TradeExecutive-event-handler-foreign-exchange-quotes");
		
		LOGGER.info( String.format("TradeExecutive initialized. Configuration:\n\t%s\n", tradeExecutiveConfiguration));
		
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
				default:
					break; // TODO log unsupported event
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

		IStockQuote stockQuote = event.getStockQuote();
		if(stockQuote.getExchange().equals(Exchange.TSE)) {
			//TODO BR-0004
			// TODO verify correct stock
			tseStockQuote.set(stockQuote);
			conditionallyInitiateTrade();
		} else if(stockQuote.getExchange().equals(Exchange.NYSE)) {
			//TODO BR-0005
			// Verify correct stock quote
			nyseStockQuote.set(stockQuote);
			conditionallyInitiateTrade();
		}
		
		orderManagementService.push(stockQuote);//TODO for SIMULATOR only - remove later
	}

	
	private void handle(ForeignExchangeQuoteReceived event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		IForeignExchangeQuote quote = event.getForeignExchangeQuote();
		//TODO BR-0002 bid must be less than ask
		if(     CAD_CURRENCY.equals(quote.getBaseCurrency()) 
			 && US_CURRENCY.equals( quote.getQuoteCurrency() )
		) 
		{
			// TODO right now, do nothing
		}
		else if(    US_CURRENCY.equals(quote.getBaseCurrency()) 
				 && CAD_CURRENCY.equals( quote.getQuoteCurrency() )
				) 
		{
			usCadCurrencyQuote.set(quote);
			conditionallyInitiateTrade();
		}
		
	}
	
	
	private void handle(StockOrderPlaced event) {
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
	
	
	private void handle(StockOrderAccepted event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		long id = event.getOrderId();
		StockOrder order = stockOrdersById.get(id);
		
		if(null==order) {
			//TODO alarm
			return;
		}
		assert order != null;
		order.accepted();
		
		LOGGER.info( String.format("Stock order accepted: %s", order.toString()));
	}
	
	
	private void handle(StockOrderFilled event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		long id = event.getOrderId();
		StockOrder order = stockOrdersById.get(id);
		
		if(null==order) {
			//TODO alarm
		}
		assert order != null;
		
		
		
		final Trade trade = tradesByStockOrderId.get(order.getId());
		assert trade != null;
		assert trade == activeTrade;
		
		if(order.getExchange().equals(buyExchange)) {
			LOGGER.info( String.format("Buy stock order filled order id:%d # of shares filled: %d"));	
			trade.buyFilled(event.getnFilled());
			placeHedgeOrder( trade, event.getnFilled());
		} 
		else if(order.getExchange().equals(sellExchange)) {
			LOGGER.info( String.format("Hedge  stock order filled order id:%d # of shares filled: %d"));	
			trade.hedgeFilled(event.getnFilled());
			if( trade.isComplete() ) {
				LOGGER.info( String.format("Trade completed:%s", trade) );
				completedTrades.add(trade);
				activeTrade = null;
				if(shouldInitiateTrade()) {
					initateTrade();
				}
			}
		}
		else
			assert false;
	}

	
	private void handle(StockOrderCompleted event) {
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
		
		LOGGER.info( String.format("Stock order completed. Order: %s", order));	
	}
	
	
	private void conditionallyInitiateTrade() {
		if (shouldInitiateTrade()) {
			LOGGER.info("Decision: initiate a Trade");
			initateTrade();
		} else
			LOGGER.info("Decision: do not initiate a Trade.");
	}
	

	boolean shouldInitiateTrade() {
		CalculationLogRecord blr = new CalculationLogRecord();
		blr.setName("shouldInitiateTrade");

		boolean shouldIntiateTrade = true; // until proven otherwise

		// for now, we don't initiate a trade if we already have one
		shouldIntiateTrade &= null==activeTrade;
		blr.setVariable("Trade already active", activeTrade != null);
		if (!shouldIntiateTrade) {
			blr.setResult(shouldIntiateTrade);
			LOGGER.log(blr);
			return shouldIntiateTrade;
		}
		assert shouldIntiateTrade;
		
		// don't initiate trade if we don't have enough data to trade
		boolean enoughDataToTrade = isEnoughDataToTrade();
		blr.setVariable("enoughDataToTrade", enoughDataToTrade);
		shouldIntiateTrade &= enoughDataToTrade;

		blr.setResult(shouldIntiateTrade);
		LOGGER.log(blr);
		return shouldIntiateTrade;
	}
	
	
	private void initateTrade() {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread	 
		assert Exchange.TSE.equals(buyExchange);
		assert Exchange.NYSE.equals(sellExchange);
		
		BigDecimal buyPostingPrice = cadPostingPrice();
		int buyPostingSize = cadPostingSize();	
	
		activeTrade = new Trade(
				buyExchange,
				sellExchange,
				stock,
				buyPostingPrice,
				nyseStockQuote.get().getBid(), // reflexPrice, a.k.a. the hedge price
				null, //TODO cancelPrice,
				buyPostingSize
				) ;
	
		// place the order
	    BuyOrder order = new BuyOrder(buyExchange, stock, buyPostingSize, buyPostingPrice); 
	    order.designed();
	    stockOrdersById.put( order.getId(), order);
	    tradesByStockOrderId.put( order.getId(), activeTrade);
	    orderManagementService.push(order);
	}
	
	
	
	private void placeHedgeOrder(Trade strategy, int nShares) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		SellOrder hedgeOrder = new SellOrder( sellExchange, stock, nShares, nyseStockQuote.get().getBid());
		hedgeOrder.designed();
		stockOrdersById.put( hedgeOrder.getId(), hedgeOrder);
		this.orderManagementService.push(hedgeOrder);
	}
	
	
	
	private int cadPostingSize() {
		BigDecimal usBestBidSize = new BigDecimal( this.nyseStockQuote.get().getBidSize());
		int cadPostingSize = cdnAveragePostingRatio.multiply(usBestBidSize, MATH_CONTEXT_WHOLE_NUMBER_ROUND_DOWN).intValue();
		
		//Initial posting size = Rounddown((minimum posting percentage + maximum posting percentage)/2 * US best bid size)
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("cadPostingSize");
		record.setVariable("usBestBidSize", usBestBidSize);
		record.setVariable("cdnAveragePostingRatio", cdnAveragePostingRatio);
		record.setResult( cadPostingSize );
		LOGGER.log(record);
		
		
		return cadPostingSize;
	}

	
	private BigDecimal cadPostingPrice() {
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("cadPostingPrice");
		
		// calculate the posting price
		final BigDecimal passivePostingPrice = cadPassivePostingPrice();
		if (passivePostingPrice.compareTo(tseStockQuote.get().getAsk()) == -1) {
			record.setVariable("passivePostingPrice", passivePostingPrice);
			record.setVariable("tseStockQuote", tseStockQuote);
			record.setResult(passivePostingPrice);
			LOGGER.log(record);
			return passivePostingPrice;
		} else {
			final BigDecimal aggressivePostingPrice = this.cadAggressivePostingPrice();
			record.setVariable("aggressivePostingPrice", aggressivePostingPrice);
			record.setVariable("tseStockQuote", tseStockQuote);
			LOGGER.log(record);
			return aggressivePostingPrice;
		}
	}
	
	
	private boolean isEnoughDataToTrade() {
		CalculationLogRecord blr = new CalculationLogRecord();	
		blr.setName("isEnoughDataToTrade");
		blr.setVariable("tseStockQuote", tseStockQuote.get() );
		blr.setVariable("nyseStockQuote", nyseStockQuote.get() );
		blr.setVariable("usCadCurrencyQuote", usCadCurrencyQuote.get() );
		
		boolean isEnough = 
				   tseStockQuote.get() != null
				&& nyseStockQuote.get() != null
				&& usCadCurrencyQuote.get() != null;
		
		blr.setResult( isEnough );
		LOGGER.log(blr);
		
		return isEnough;
	}
	
	
	private BigDecimal currentCadParticipationRatio() {
		BigDecimal result = null;
		
		int cdnBuySharesOutstanding = 0;
		if( activeTrade != null) {
			cdnBuySharesOutstanding = activeTrade.getBuyPostingSize() - activeTrade.getSharesBought();
		}
		
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("currentCadParticipationRatio");
		record.setVariable("cdnBuySharesOutstanding", cdnBuySharesOutstanding);
			
		if(0 == cdnBuySharesOutstanding) {
			result = BigDecimal.ZERO.setScale(2);
			record.setResult(result);
			LOGGER.log(record);
			return result;
		}
		assert cdnBuySharesOutstanding > 0;
		
		assert nyseStockQuote.get() != null;
		assert nyseStockQuote.get().getBidSize() >= 0;
		
		record.setVariable("US bid size", nyseStockQuote.get().getBidSize()) ;
		result = new BigDecimal(cdnBuySharesOutstanding).divide( new BigDecimal(nyseStockQuote.get().getBidSize()), TradeExecutive.MATH_CONTEXT_RATIO);
		
		record.setResult(result);
		LOGGER.log(record);

		return result;
	}
	

	protected BigDecimal cadPassivePostingPrice() {
		BigDecimal usBestBid = this.nyseStockQuote.get().getBid();
		BigDecimal usdCadFxBid = this.usCadCurrencyQuote.get().getBid();
		BigDecimal netProfitInCA = this.tradeExecutiveConfiguration.getNetProfitPerShareCDN();
		BigDecimal canadianPassiveEchangeFee = this.tsxMetadata.getPassiveExchangeFeePerShare();
		BigDecimal canadianRSFee = this.tsxMetadata.getRoutedExchangeFeePerShare();
		BigDecimal projectedInitiationCosts = canadianPassiveEchangeFee.add(canadianRSFee);
		BigDecimal projectedHedgeCosts = this.nyseMetadata.getAgressiveExchangeFeePerShare(); //TODO add SEC fee
		
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
		
		return cadPostingPrice;
	}

	
	protected BigDecimal cadAggressivePostingPrice() {
		BigDecimal usBestBid = this.nyseStockQuote.get().getBid();
		BigDecimal usdCadFxBid = this.usCadCurrencyQuote.get().getBid();
		BigDecimal netProfitInCA = this.tradeExecutiveConfiguration.getNetProfitPerShareCDN();
		BigDecimal canadianAggressiveEchangeFee = this.tsxMetadata.getAgressiveExchangeFeePerShare();
		BigDecimal canadianRSFee = this.tsxMetadata.getRoutedExchangeFeePerShare();
		BigDecimal projectedInitiationCosts = canadianAggressiveEchangeFee.add(canadianRSFee);
		BigDecimal projectedHedgeCosts = this.nyseMetadata.getAgressiveExchangeFeePerShare(); //TODO add SEC fee
		
		
		//CA posting price = round down to nearest tick (US best bid * USD/CAD FX bid - (Net profit in CA + Projected initiation costs + (Projected hedge costs * USD/CAD FX ask)))
		BigDecimal cadPostingPrice = usBestBid.multiply( usdCadFxBid )
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
		record.setResult(cadPostingPrice);
		LOGGER.log(record);
		
		return cadPostingPrice;
	}
	
	

	private boolean shouldPlaceBuyOrder() {
		boolean shouldPlaceBuyOrder = true; //until proven otherwise
		
		CalculationLogRecord blr = new CalculationLogRecord();
		blr.setName("shouldPlaceBuyOrder");
		
		boolean isActiveStrategy = activeTrade != null;
		blr.setVariable("isActiveStrategy", isActiveStrategy);
		shouldPlaceBuyOrder &= isActiveStrategy;
		
		if( !shouldPlaceBuyOrder ) {
			blr.setResult( shouldPlaceBuyOrder );
			LOGGER.log(blr);
			return shouldPlaceBuyOrder;
		}
			
		BigDecimal participation = currentCadParticipationRatio();
		BigDecimal minCdnPostingRatio = tradeExecutiveConfiguration.getMininumCdnBidPostingRatio();
		
		blr.setVariable("currentCadParticipationRatio", participation);
		blr.setVariable("minCdnPostingRatio", minCdnPostingRatio);

		shouldPlaceBuyOrder &= participation.compareTo(minCdnPostingRatio) < 0;

		blr.setResult(shouldPlaceBuyOrder);
		LOGGER.log(blr);
		
		return shouldPlaceBuyOrder;
	}
	
	
	/** threadsafe, blocking */
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
	
	
	/** threadsafe, , blocking */
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
	
	/** threadsafe,  blocking */
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
	

	/** threadsafe,  blocking */
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
	
	/** threadsafe, , blocking */
	public void pushOrderAccepted(long orderId) {
		boolean put = false;
		do {
			try {
				StockOrderAccepted event = new StockOrderAccepted(orderId);
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
