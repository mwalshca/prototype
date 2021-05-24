package com.fmax.prototype.services.ib;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.quote.QuoteSink;
import com.fmax.prototype.model.quote.StockQuote;
import com.ib.client.Bar;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDescription;
import com.ib.client.ContractDetails;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.DepthMktDataDescription;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.FamilyCode;
import com.ib.client.HistogramEntry;
import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;
import com.ib.client.NewsProvider;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.PriceIncrement;
import com.ib.client.SoftDollarTier;
import com.ib.client.TickAttrib;
import com.ib.client.TickAttribBidAsk;
import com.ib.client.TickAttribLast;

@Scope("singleton")
@Service
public class InteractiveBrokerService  implements EWrapper {
	
	
	public static final int CLIENT_ID = 1964;
	
	private Thread readerThread = null;
	
	private EReaderSignal readerSignal = new EJavaSignal();;
    private EClientSocket client;
    private EReader reader = null;
    
    private int nextValidID = -1;
    private int requestId = 1942;
    
    private Map<Integer, ContractDetailsScratchPad> contractDetailsInProgress = new HashMap<>();
   
    private Map<Integer, TickByTickHandler> tickByTickHandlersByRequestID = new HashMap<>();
    
    public InteractiveBrokerService() {
    	 client = new EClientSocket(this, readerSignal);
    	 client.eConnect("127.0.0.1", 7497, CLIENT_ID);
    }
      

	@Override
	public void connectAck() {
		System.out.println("connectAck");
		client.startAPI();
		
		reader = new EReader(client, readerSignal); 
		readerThread = new Thread( this::readerLoop, "IB-read-loop");
		readerThread.start();
		reader.start();
	}
	
	
	private void readerLoop() {
		while (client.isConnected()) {
			readerSignal.waitForSignal();
			try {
				reader.processMsgs();
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		}
	}
    @Override
	public void error(Exception arg0) {
		System.out.println("error()"+ arg0);
	}

    
	@Override
	public void error(String arg0) {
		System.out.println("error()"+ arg0);
	}

	
	@Override
	public void error(int id, int errorCode, String errorMessage) {
		if(-1 != id)
			System.out.println("Error:" + errorMessage);
	}


	private static class ContractDetailsScratchPad {
		CompletableFuture<List<ContractDetails>> future = new CompletableFuture<>();
		List<ContractDetails> contractDetailsList = new ArrayList<>();
	}
	
    public Future<List<ContractDetails>> getContractDetails(Contract contract) {
    	int reqID = requestId++;
    	
    	ContractDetailsScratchPad cdsp = new ContractDetailsScratchPad();
    	assert !contractDetailsInProgress.containsKey(reqID);
    	
    	contractDetailsInProgress.put( reqID, cdsp);
    	client.reqContractDetails(reqID, contract);
    	
    	return cdsp.future;
    }
    
    
    @Override
   	public void contractDetails(int requestID, ContractDetails cd) {
    	ContractDetailsScratchPad cdsp = contractDetailsInProgress.get(requestID);
       	if(null == cdsp) {
       		System.err.println( String.format("ContractDetailsScratchPad not found for requestID: %d", requestID));
       		return;
       	}
   		cdsp.contractDetailsList.add(cd);
   	}

       
   	@Override
   	public void contractDetailsEnd(int requestID) {
   		ContractDetailsScratchPad cdsp = contractDetailsInProgress.get(requestID);
       	if(null == cdsp) {
       		System.err.println( String.format("ContractDetailsScratchPad not found for requestID: %d", requestID));
       		return;
       	}
       	contractDetailsInProgress.remove(requestID);
        cdsp.future.complete(cdsp.contractDetailsList);
   	}
    
   
    private static class TickByTickHandler {
    	QuoteSink quoteSink;
    	Exchange  exchange;
		String    symbol;
    }

    
    public void reqTickByTickData(Contract 	contract, QuoteSink quoteSink) throws IllegalArgumentException
    {
    	final int myRequestId = requestId++;	
    	final TickByTickHandler handler = new TickByTickHandler();
    	
    	handler.quoteSink = quoteSink;
	    handler.exchange = Exchange.valueOf( contract.exchange() );
	    handler.symbol = contract.localSymbol();
	    
    	tickByTickHandlersByRequestID.put( myRequestId, handler);
    	
    	client.reqTickByTickData(myRequestId, 
    			                 contract, 
    			                 "BidAsk", 
    			                 16,
    			                 true);
    }
    
    @Override
	public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
    	System.out.println("\nHistorical ticksBidAsk\n");
    	
    	TickByTickHandler handler = tickByTickHandlersByRequestID.get(reqId);
    	if(null==handler) {
    		System.out.println("Info: handler not found for historicalTicksBidAsk request ID:" + reqId);
    		return;
    	}
    	
    	for(HistoricalTickBidAsk tick:ticks) {
    		StockQuote quote = new StockQuote(
    		        handler.exchange, 
                    handler.symbol,
                    BigDecimal.valueOf(tick.priceBid()),
                    BigDecimal.valueOf(tick.priceAsk()),
                    tick.sizeBid(),
                    tick.sizeAsk(),
                    LocalDateTime.ofInstant( new Date(tick.time()*1000).toInstant(), ZoneId.systemDefault() )
                    ); 
    		handler.quoteSink.accept(quote);
    	}
    	
    }
    
    @Override
	public void tickByTickBidAsk(
			int reqId, 
			long time, 
			double bidPrice, 
			double askPrice, 
			int bidSize, 
			int askSize, 
			TickAttribBidAsk tickAttribBidAsk) {
    	
    	TickByTickHandler handler = tickByTickHandlersByRequestID.get(reqId);
    	if(null==handler) {
    		System.err.println("Error: handler not found for tickByTick request ID:" + reqId);
    		return;
    	}
    	
    	StockQuote quote = new StockQuote(handler.exchange, 
    			                          handler.symbol,
    			                          BigDecimal.valueOf(bidPrice),
    			                          BigDecimal.valueOf(askPrice),
    			                          bidSize,
    			                          askSize,
    			                          LocalDateTime.ofInstant( new Date(time*1000).toInstant(), ZoneId.systemDefault() )
    			                          ); 
    	handler.quoteSink.accept(quote);
	}
    
    
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {
		System.out.println( String.format("tickPrice(), tickerID:%d, field: %d price:%f \n\n", tickerId, field, price) );
		System.out.flush();
	}
	
	@Override
	public void tickSize( int tickerId, int field, int size) {
		System.out.println( String.format("tickSize(), tickerId:%d, field:%d size:%d", tickerId, field, size) );
	}
	
	@Override
	public void tickString(int tickerId, int tickType, String value) {
		System.out.printf("tickString() tickType:%d value:%s", tickType, value);
	}
	
	
    public void shutdown() {
    	client.eDisconnect();
    }
    

	@Override
	public void connectionClosed() {
		System.out.println("connecClosed");
	}

	
	@Override
	public void accountDownloadEnd(String arg0) {		
	}

	@Override
	public void accountSummary(int arg0, String arg1, String arg2, String arg3, String arg4) {		
	}

	@Override
	public void accountSummaryEnd(int arg0) {
	}

	@Override
	public void accountUpdateMulti(int arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {	
	}

	@Override
	public void accountUpdateMultiEnd(int arg0) {
	}

	@Override
	public void bondContractDetails(int arg0, ContractDetails arg1) {
	}

	@Override
	public void commissionReport(CommissionReport arg0) {
	}

	@Override
	public void completedOrder(Contract arg0, Order arg1, OrderState arg2) {	
	}

	@Override
	public void completedOrdersEnd() {
	}

	@Override
	public void currentTime(long arg0) {
	}

	@Override
	public void deltaNeutralValidation(int arg0, DeltaNeutralContract arg1) {
	}

	@Override
	public void displayGroupList(int arg0, String arg1) {
	}

	@Override
	public void displayGroupUpdated(int arg0, String arg1) {
	}

	
	@Override
	public void familyCodes(FamilyCode[] arg0) {	
	}

	@Override
	public void fundamentalData(int arg0, String arg1) {	
	}

	@Override
	public void headTimestamp(int arg0, String arg1) {
	}

	@Override
	public void histogramData(int arg0, List<HistogramEntry> arg1) {
	}

	@Override
	public void historicalData(int arg0, Bar arg1) {
	}

	@Override
	public void historicalDataEnd(int arg0, String arg1, String arg2) {
	}

	@Override
	public void historicalDataUpdate(int arg0, Bar arg1) {
	}

	@Override
	public void historicalNews(int arg0, String arg1, String arg2, String arg3, String arg4) {
	}

	@Override
	public void historicalNewsEnd(int arg0, boolean arg1) {
	}

	@Override
	public void historicalTicks(int arg0, List<HistoricalTick> arg1, boolean arg2) {
	}

	

	@Override
	public void historicalTicksLast(int arg0, List<HistoricalTickLast> arg1, boolean arg2) {
	}

	@Override
	public void managedAccounts(String arg0) {
	}

	@Override
	public void marketDataType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void marketRule(int arg0, PriceIncrement[] arg1) {
	}

	@Override
	public void mktDepthExchanges(DepthMktDataDescription[] arg0) {
	}

	@Override
	public void newsArticle(int arg0, int arg1, String arg2) {
	}

	@Override
	public void newsProviders(NewsProvider[] arg0) {
	}

	@Override
	public void nextValidId(int arg0) {
		System.out.println("nextValidId()"+ arg0);
		nextValidID = arg0;
	}

	@Override
	public void openOrder( int orderId, Contract contract, Order order, OrderState orderState) {
		
	}

	@Override
	public void openOrderEnd() {	
		// TODO Auto-generated method stub
	}

	@Override
	public void orderBound(long arg0, int arg1, int arg2) {
	}

	@Override
	public void orderStatus( 
			int orderId, 
			String status, 
			double filled, 
			double remaining,
            double avgFillPrice, 
            int permId, 
            int parentId, 
            double lastFillPrice,
            int clientId, 
            String whyHeld, 
            double mktCapPrice) {
		
	}
	

	public void execDetails( int reqId, Contract contract, Execution execution) {	
	}

	@Override
	public void execDetailsEnd( int reqId) {	
	}
	

	@Override
	public void pnl(int arg0, double arg1, double arg2, double arg3) {
	}

	@Override
	public void pnlSingle(int arg0, int arg1, double arg2, double arg3, double arg4, double arg5) {
	}

	@Override
	public void position(String arg0, Contract arg1, double arg2, double arg3) {
	}

	@Override
	public void positionEnd() {
	}

	@Override
	public void positionMulti(int arg0, String arg1, String arg2, Contract arg3, double arg4, double arg5) {
	}

	@Override
	public void positionMultiEnd(int arg0) {
	}

	@Override
	public void realtimeBar(int arg0, long arg1, double arg2, double arg3, double arg4, double arg5, long arg6,
			double arg7, int arg8) {
	}

	@Override
	public void receiveFA(int arg0, String arg1) {
	}

	@Override
	public void rerouteMktDataReq(int arg0, int arg1, String arg2) {
	}

	@Override
	public void rerouteMktDepthReq(int arg0, int arg1, String arg2) {
	}

	@Override
	public void scannerData(int arg0, int arg1, ContractDetails arg2, String arg3, String arg4, String arg5,
			String arg6) {
	}

	@Override
	public void scannerDataEnd(int arg0) {
	}

	@Override
	public void scannerParameters(String arg0) {
	}

	@Override
	public void securityDefinitionOptionalParameter(int arg0, String arg1, int arg2, String arg3, String arg4,
			Set<String> arg5, Set<Double> arg6) {
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int arg0) {
	}

	@Override
	public void smartComponents(int arg0, Map<Integer, Entry<String, Character>> arg1) {
	}

	@Override
	public void softDollarTiers(int arg0, SoftDollarTier[] arg1) {
	}

	@Override
	public void symbolSamples(int arg0, ContractDescription[] contractDescriptions) {
	}

	@Override
	public void tickByTickAllLast(int arg0, int arg1, long arg2, double arg3, int arg4, TickAttribLast arg5,
			String arg6, String arg7) {		
	}

	

	@Override
	public void tickByTickMidPoint(int arg0, long arg1, double arg2) {
	}

	@Override
	public void tickEFP(int arg0, int arg1, double arg2, String arg3, double arg4, int arg5, String arg6, double arg7,
			double arg8) {
	}

	@Override
	public void tickGeneric(int arg0, int arg1, double arg2) {
	}

	@Override
	public void tickNews(int arg0, long arg1, String arg2, String arg3, String arg4, String arg5) {
	}

	@Override
	public void tickOptionComputation(int arg0, int arg1, double arg2, double arg3, double arg4, double arg5,
			double arg6, double arg7, double arg8, double arg9) {
	}

	

	@Override
	public void tickReqParams(int arg0, double arg1, String arg2, int arg3) {
	}


	@Override
	public void tickSnapshotEnd(int arg0) {
	}

	
	@Override
	public void updateAccountTime(String arg0) {
	}

	@Override
	public void updateAccountValue(String arg0, String arg1, String arg2, String arg3) {
	}

	@Override
	public void updateMktDepth(int arg0, int arg1, int arg2, int arg3, double arg4, int arg5) {
	}

	@Override
	public void updateMktDepthL2(int arg0, int arg1, String arg2, int arg3, int arg4, double arg5, int arg6,
			boolean arg7) {
	}

	@Override
	public void updateNewsBulletin(int arg0, int arg1, String arg2, String arg3) {
	}

	@Override
	public void updatePortfolio(Contract arg0, double arg1, double arg2, double arg3, double arg4, double arg5,
			double arg6, String arg7) {
	}

	@Override
	public void verifyAndAuthCompleted(boolean arg0, String arg1) {
	}

	@Override
	public void verifyAndAuthMessageAPI(String arg0, String arg1) {
	}

	@Override
	public void verifyCompleted(boolean arg0, String arg1) {
	}

	@Override
	public void verifyMessageAPI(String arg0) {
	}

}
