package com.fmax.prototype.components;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ExchangeMetadata;
import com.fmax.prototype.model.configuration.TradeExecutiveConfiguration;
import com.fmax.prototype.model.quote.ForeignExchangeQuote;
import com.fmax.prototype.model.quote.StockQuote;
import com.fmax.prototype.model.trade.StockOrder;


/** Current implementation ONLY looks at bids on the from the TSE and asks from the NYSE */
public class TradeExecutive {
	private static final Currency CAD_CURRENCY = Currency.getInstance("CAD");
	private static final Currency US_CURRENCY = Currency.getInstance("USD");
	
	private final TradeGovernor 				tradeGovernor;
	private final TradeExecutiveConfiguration   tradeExecutiveConfiguration;
	private final ExecutorService               executorService = Executors.newFixedThreadPool(32);
	
	private final LinkedBlockingDeque<StockQuote>           nyseStockQuotes = new LinkedBlockingDeque<>();
	private final LinkedBlockingDeque<StockQuote>           tsxStockQuotes = new LinkedBlockingDeque<>();
    private final LinkedBlockingDeque<ForeignExchangeQuote> cadUsCurrencyQuotes = new LinkedBlockingDeque<>();
    private final LinkedBlockingDeque<ForeignExchangeQuote> usCadCurrencyQuotes = new LinkedBlockingDeque<>();
    
    private final Thread threadHandleStockQuotes;
	private final Thread threadHandleForeignExchangeQuotes;
	
	
	private ExchangeMetadata tsxMetadata;
    private ExchangeMetadata nyseMetadata;
    
	/* variables used to track state */
	private final OrderManagementService orderManagementService = new OrderManagementService(this);
	private StockQuote lastTsxQuote;  //the ONYL method that sets this value is handleTSEStockQuote
	private StockQuote lastNyseQuote;
	private Map<Long,StockOrder> stockOrdersById = new HashMap<>();
	
	public TradeExecutive(TradeGovernor tradeGovernor, 
			              TradeExecutiveConfiguration tradeExecutiveConfiguration,
			              ExchangeMetadataService exchangeMetadataService) 
	{
		this.tradeGovernor = tradeGovernor;
		this.tradeExecutiveConfiguration = tradeExecutiveConfiguration;
		

		tsxMetadata = exchangeMetadataService.get(Exchange.TSE);
		nyseMetadata = exchangeMetadataService.get(Exchange.NYSE );
		
		
		this.threadHandleStockQuotes = new Thread(this::handleTSEStockQuote, "stockQuoteHandler");
		this.threadHandleForeignExchangeQuotes  = new Thread(this::handleForeignExchangeQuote, "foreignExchangeQuoteHandler");;
		
		
		threadHandleStockQuotes.start();
		threadHandleForeignExchangeQuotes.start();
	}
	
	
	/** Re-entrant, blocking */
	public void receiveQuote(StockQuote quote) {
		Objects.requireNonNull(quote);
		switch( quote.getExchange()) {
		case TSE:
			tsxStockQuotes.addLast(quote);
			break;
		case NYSE:
			nyseStockQuotes.addLast(quote);
			break;
		default:
			throw new IllegalArgumentException(String.format("Unsupported exchange:%s", quote.getExchange().toString()));
		}
	}
	
	
	/** Re-entrant, blocking */
	public void receiveQuote(ForeignExchangeQuote quote) {
		if(    quote.getBaseCurrency().equals(CAD_CURRENCY)
			&& quote.getQuoteCurrency().equals(US_CURRENCY)) {
			cadUsCurrencyQuotes.add(quote);
		} else if(   quote.getBaseCurrency().equals(US_CURRENCY)
				 && quote.getQuoteCurrency().equals(CAD_CURRENCY)){
			usCadCurrencyQuotes.add(quote);
		}
	}
	
	
	/**
	 * By design, intended to be running as an infinite loop in its own thread.
	 */
	private void handleTSEStockQuote() {
		do {
			try {
				lastTsxQuote = tsxStockQuotes.take();
				//TODO BR-0004
				assert lastTsxQuote != null;
				evaluateStockPriceChange();
			} catch (InterruptedException e) {
				Thread.interrupted(); //reset the thread's interrupted flag.
			}  
		} while( true );
	}
	
	/**
	 * By design, intended to be running as an infinite loop in its own thread.
	 */
	private void handleNYSEStockQuote() {
		do {
			try {
				lastNyseQuote = nyseStockQuotes.take();
				//TODO BR-0005
				assert lastNyseQuote != null;
				evaluateStockPriceChange();
			} catch (InterruptedException e) {
				Thread.interrupted(); //reset the thread's interrupted flag.
			}  
		} while( true );
	}
	
	
	private void evaluateStockPriceChange() {
		if( 	null ==lastTsxQuote
			||  null == lastNyseQuote) { // if we don't have quotes on both sides, we haven't started trading and we don't/can't trade yet
			//TODO log something?
			return;
		}	
		
		if(stockOrdersById.isEmpty()) { // we don't have orders out there; should we place some?
			
		} else { // we have orders out there - let's assess how the latest quote affects us 
			
	    }
	}
	

	/**
	 * By design, intended to be running as an infinite loop in its own thread.
	 */
	private void handleForeignExchangeQuote() {
		do {
			try {
				ForeignExchangeQuote foreignExchangeQuote = cadUsCurrencyQuotes.take();
			} catch (InterruptedException e) {
				Thread.interrupted(); //reset the thread's interrupted flag.
			}  
		} while( true );
	}
	

	public void orderPlaced(StockOrder order) {	
	}
	
	
	
		
	public static class ExchangeSymbol{
		public ExchangeSymbol( Exchange exchange, String symbol) {
			this.exchange = exchange;
			this.symbol = symbol;
		}
		public final Exchange exchange;
		public final String symbol;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
			result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExchangeSymbol other = (ExchangeSymbol) obj;
			if (exchange != other.exchange)
				return false;
			if (symbol == null) {
				if (other.symbol != null)
					return false;
			} else if (!symbol.equals(other.symbol))
				return false;
			return true;
		}
	}
	
	
	private Map<ExchangeSymbol, String> cusipByExchangeSymbol = initializeCusipByExchangeSymbol();
	
	private Map<ExchangeSymbol, String> initializeCusipByExchangeSymbol() {
		Map<ExchangeSymbol, String> map = new HashMap<>();
	
		map.put( new ExchangeSymbol(Exchange.TSE, "RY.TO"), "780087102");
		map.put( new ExchangeSymbol(Exchange.NYSE, "RY"), "780087102");
		
		return map;
	}
	
	private  void logPerf(String name, long nanos) {
		executorService.execute( () -> { System.out.println(String.format("%s %d", name, nanos));});			
	}
}
