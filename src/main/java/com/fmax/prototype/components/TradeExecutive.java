package com.fmax.prototype.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
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
import com.fmax.prototype.events.StockOrderPlaced;
import com.fmax.prototype.events.StockQuoteReceived;
import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ExchangeMetadata;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.model.configuration.TradeExecutiveConfiguration;
import com.fmax.prototype.model.quote.IForeignExchangeQuote;
import com.fmax.prototype.model.quote.IStockQuote;
import com.fmax.prototype.model.trade.BuyOrder;
import com.fmax.prototype.model.trade.StockOrder;


/** Current implementation ONLY looks at bids on the from the TSE and asks from the NYSE */
public class TradeExecutive {
	private static final Currency CAD_CURRENCY = Currency.getInstance("CAD");
	private static final Currency US_CURRENCY = Currency.getInstance("USD");
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
    final Exchange buyExchange;  // TODO make the code able to switch the buy and sell exchanges if the market changes   
    final Exchange sellExchange; 
    
    //state we care about and need to replicate:
    private final AtomicReference<IStockQuote> 			 nyseStockQuote = new AtomicReference<>();
    private final AtomicReference<IStockQuote> 			 tseStockQuote = new AtomicReference<>();
    private final AtomicReference<IForeignExchangeQuote> cadUsCurrencyQuote = new AtomicReference<>();
    private final AtomicReference<IForeignExchangeQuote> usCadCurrencyQuote = new AtomicReference<>();
	private final ConcurrentHashMap<Long,StockOrder>     stockOrdersById = new ConcurrentHashMap<>();
	
	
	public TradeExecutive(TradeGovernor tradeGovernor, 
			              TradeExecutiveConfiguration tradeExecutiveConfiguration,
			              ExchangeMetadataService exchangeMetadataService,
			              SecuritiesMasterService securitiesMasterService) 
	{
		this.tradeGovernor = tradeGovernor;
		this.tradeExecutiveConfiguration = tradeExecutiveConfiguration;
		this.securitiesMasterService = securitiesMasterService;
		
		stock = this.securitiesMasterService.getStock( tradeExecutiveConfiguration.getCusip() );
		assert stock != null; // TODO fatal error
				
		buyExchange = tradeExecutiveConfiguration.getBuyStockExchange();
		sellExchange = tradeExecutiveConfiguration.getSellStockExchange();
		
		tsxMetadata = exchangeMetadataService.get(Exchange.TSE);
		nyseMetadata = exchangeMetadataService.get(Exchange.NYSE );
		
		threadHandleEvents               = new Thread(this::pullEvents,  String.format("TradeExecutive-event-handler-%s", tradeExecutiveConfiguration.getCusip()) );
		threadPullStockQuotes            = new Thread(this::pullStockQuote, "TradeExecutive-event-handler-stock-quotes");
		threadPullForeignExchangeQuotes  = new Thread(this::pullForeignExchangeQuotes, "TradeExecutive-event-handler-foreign-exchange-quotes");
		
		threadHandleEvents.start();
		threadPullStockQuotes.start();
		threadPullForeignExchangeQuotes.start();
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
					
				case STOCK_ORDER_FILLED:
					break;
				
				default:
					break; // TODO log unsupported event

				}
				LOGGER.info(String.format("\nEnd processing event:%s\n----------------------------\n\n",event.getEventType()) );
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the thread's interupted flag
			} catch (RuntimeException ex) {
			}
		}
	}

	
	private void handle(StockQuoteReceived event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		//TODO BR
		IStockQuote stockQuote = event.getStockQuote();
		if(stockQuote.getExchange().equals(Exchange.TSE)) {
			//TODO BR-0004
			// TODO verify correct stock
			tseStockQuote.set(stockQuote);
		} else if(stockQuote.getExchange().equals(Exchange.NYSE)) {
			//TODO BR-0005
			// Verify correct stock quote
			nyseStockQuote.set(stockQuote);
		}
		conditionallyPlaceBuyOrder();
	}
	
	
	private void handle(ForeignExchangeQuoteReceived event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		
		IForeignExchangeQuote quote = event.getForeignExchangeQuote();
		//TODO BR-0002 bid must be less than ask
		if(     CAD_CURRENCY.equals(quote.getBaseCurrency()) 
			 && US_CURRENCY.equals( quote.getQuoteCurrency() )
		) 
		{
			cadUsCurrencyQuote.set(quote);
		}
		else if(    US_CURRENCY.equals(quote.getBaseCurrency()) 
				 && CAD_CURRENCY.equals( quote.getQuoteCurrency() )
				) 
		{
			usCadCurrencyQuote.set(quote);
		}
		conditionallyPlaceBuyOrder();
	}
	
	private void handle(StockOrderPlaced event) {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread
		long id = event.getOrderId();
		StockOrder order = stockOrdersById.get(id);
		
		if(null==order) {
			//TODO alarm
		}
		assert order != null;
		order.placed();
		
		LOGGER.info( String.format("Stock order placed:%s", order.toString()));
		
	}
	
	
   private  void conditionallyPlaceBuyOrder() {
		if(shouldPlaceBuyOrder()) {
			LOGGER.info("Decision: place buy order.");
			placeBuyOrder();
		}
		else 
			LOGGER.info("Decision: do not place buy order.");
	}
	
   
	boolean shouldPlaceBuyOrder() {
		boolean buyOrdersOutstanding = !stockOrdersById.isEmpty(); //TOOD replace with participation logic
		boolean enoughDataToTrade =  isEnoughDataToTrade();
		boolean shouldPlaceBuyOrder = !buyOrdersOutstanding && enoughDataToTrade;
		
		
		CalculationLogRecord blr = new CalculationLogRecord();	
		blr.setName("shouldPlaceBuyOrder");
		blr.setVariable("buyOrdersOutstanding.isEmpty()", buyOrdersOutstanding );
		blr.setVariable("enoughDataToTrade", enoughDataToTrade );
		blr.setResult( shouldPlaceBuyOrder );
		LOGGER.log(blr);
		
		return shouldPlaceBuyOrder;
	}
	
	
	boolean cancelUnprofitableOrders() {
		return false;
	}
	
	
	private void placeBuyOrder() {
		assert Thread.currentThread().equals(this.threadHandleEvents); // this method should ONLY be called by the event-handling thread	 
		
		// place the order
		BigDecimal buyPostingPrice = cadPostingPrice();
		int buyPostingSize = cadPostingSize();	
		
	    BuyOrder order = new BuyOrder(Exchange.TSE, stock, buyPostingSize, buyPostingPrice); 
	    order.designed();
	    stockOrdersById.put( order.getId(), order);
	    orderManagementService.push(order);
	}
	
	
	private int cadPostingSize() {
		return 10_000; //TODO implement
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
		blr.setVariable("cadUsCurrencyQuote", cadUsCurrencyQuote.get() );
		
		boolean isEnough = 
				   tseStockQuote.get() != null
				&& nyseStockQuote.get() != null
				&& cadUsCurrencyQuote.get() != null;
		
		blr.setResult( isEnough );
		LOGGER.log(blr);
		
		return isEnough;
	}
	
	
	protected BigDecimal cadPassivePostingPrice() {
		BigDecimal usBestBid = this.nyseStockQuote.get().getBid();
		BigDecimal usdCadFxBid = this.cadUsCurrencyQuote.get().getBid();
		BigDecimal netProfitInCA = this.tradeExecutiveConfiguration.getNetProfitPerShareCDN();
		BigDecimal canadianPassiveEchangeFee = this.tsxMetadata.getPassiveExchangeFeePerShare();
		BigDecimal canadianRSFee = this.tsxMetadata.getRoutedExchangeFeePerShare();
		BigDecimal projectedInitiationCosts = canadianPassiveEchangeFee.add(canadianRSFee);
		BigDecimal projectedHedgeCosts = this.nyseMetadata.getAgressiveExchangeFeePerShare(); //TODO add SEC fee
		
		//CA posting price = round down to nearest tick (US best bid * USD/CAD FX bid - (Net profit in CA + Projected initiation costs + (Projected hedge costs * USD/CAD FX ask)))
		BigDecimal cadPostingPrice =  usBestBid.multiply( usdCadFxBid )
											   .subtract( netProfitInCA )
				                               .subtract( projectedInitiationCosts )
				                               .subtract( projectedHedgeCosts.multiply(usdCadFxBid) ) //TODO should be usd/cad FX Ask 
		 									   .setScale(2,  RoundingMode.DOWN);
		CalculationLogRecord record = new CalculationLogRecord();
		record.setName("CA passive posting price");
		record.setVariable("usBestBid", usBestBid);
		record.setVariable("usdCadFxBid", usdCadFxBid);
		record.setVariable("netProfitInCA", netProfitInCA);
		record.setVariable("canadianPassiveEchangeFee", canadianPassiveEchangeFee);
		record.setVariable("canadianRSFee", canadianRSFee);
		record.setVariable("projectedInitiationCosts", projectedInitiationCosts);
		record.setVariable("projectedHedgeCosts", null);
		record.setResult(cadPostingPrice);
		LOGGER.log(record);
		
		return cadPostingPrice;
	}

	
	protected BigDecimal cadAggressivePostingPrice() {
		BigDecimal usBestBid = this.nyseStockQuote.get().getBid();
		BigDecimal usdCadFxBid = this.cadUsCurrencyQuote.get().getBid();
		BigDecimal netProfitInCA = this.tradeExecutiveConfiguration.getNetProfitPerShareCDN();
		BigDecimal canadianAggressiveEchangeFee = this.tsxMetadata.getAgressiveExchangeFeePerShare();
		BigDecimal canadianRSFee = this.tsxMetadata.getRoutedExchangeFeePerShare();
		BigDecimal projectedInitiationCosts = canadianAggressiveEchangeFee.add(canadianRSFee);
		BigDecimal projectedHedgeCosts = this.nyseMetadata.getAgressiveExchangeFeePerShare(); //TODO add SEC fee
		
		//CA posting price = round down to nearest tick (US best bid * USD/CAD FX bid - (Net profit in CA + Projected initiation costs + (Projected hedge costs * USD/CAD FX ask)))
		BigDecimal cadPostingPrice = usBestBid.multiply( usdCadFxBid )
											  .subtract( netProfitInCA )
											  .subtract( projectedInitiationCosts )
											  .subtract( projectedHedgeCosts.multiply(usdCadFxBid) )//TODO should be usd/cad FX Ask 
											  .setScale(2,  RoundingMode.DOWN); // Down so that reduce the profit margin
		return cadPostingPrice;
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
	
	/** threadsafe, , blocking */
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
}
