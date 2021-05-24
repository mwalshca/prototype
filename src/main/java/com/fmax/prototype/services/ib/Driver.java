package com.fmax.prototype.services.ib;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ISIN;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.model.quote.StockQuote;
import com.fmax.prototype.services.SecuritiesMasterService;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.TagValue;
import com.ib.client.Types.SecType;

//TODO remove
public class Driver {
		// ISIN's:
		// RBC CA7800871021
		public static void main(String[] args) throws Exception {
			InteractiveBrokerService ibs = new InteractiveBrokerService();
			SecuritiesMasterService sms = new SecuritiesMasterService(ibs);
			
			//Stock ryTSE = sms.getStock(Exchange.TSE, "RY");
			Stock ryNYSE = sms.getStock(Exchange.NYSE, "RY");
			
			//	Stock rbcTSEStock = sms.getStock(Echange, null)
			// startStream(ibs, "RY", "NYSE", "RY");
			//startFxStream(ibs);
			
		//    startStream(ibs, "TLRY", "SMART", "TLRY");
		  //  startStream(ibs, "BLDP", "SMART", "BLDP");
		   // startStream(ibs, "ABX", "SMART", "GOLD");
		   // startStream(ibs, "SHOP", "SMART", "SHOP");
		   // startStream(ibs, "BTO", "SMART", "BTG");
		}
		
		public static void startStream(InteractiveBrokerService ibs,  String cdnSymbol, String usExchange, String usSymbol) throws Exception {
			Contract contract = new Contract();
			contract.currency("CAD");
			contract.symbol(cdnSymbol);
			contract.exchange(Exchange.TSE.toString());
			contract.secType(SecType.STK);
			
			Future<List<ContractDetails>> future  = ibs.getContractDetails(contract);
			ContractDetails cadCD = future.get(30, TimeUnit.SECONDS).get(0);
			Contract cadContract = cadCD.contract();
			dump( future.get() );
			ibs.reqTickByTickData(cadContract, Driver::accept);
			
			contract = new Contract();
			contract.currency("USD");
			contract.symbol(usSymbol);
			contract.exchange(usExchange);
			contract.secType(SecType.STK);
			
			future = ibs.getContractDetails(contract);
			ContractDetails usCD = future.get(30, TimeUnit.SECONDS).get(0);
			Contract usContract = usCD.contract();
			dump( future.get() );
			ibs.reqTickByTickData(usContract, Driver::accept);
		}
		
		
		public static void accept(StockQuote stockQuote) {
			System.out.println("Quote received:" + stockQuote.toString());
		}
		
		
		public static void startFxStream(InteractiveBrokerService ibs) throws Exception{
			Contract contract = new Contract();
			contract = new Contract();
			contract.symbol("USD");
			contract.secType(SecType.CASH);
			contract.exchange("IDEALPRO");
			contract.currency("CAD");
			
			Future<List<ContractDetails>> future = ibs.getContractDetails(contract);	
			dump( future.get(30, TimeUnit.SECONDS) );
		}
		
		
		public static void dump(List<ContractDetails> cds) {
			if(null==cds)
				System.out.println("\n null contract details\n");
			
			for(ContractDetails cd:cds) {
				if(cd.secIdList() != null) {
					for(TagValue tv: cd.secIdList())
						System.out.println(" iD:" + tv.m_tag + " " + tv.m_value);
				}
				System.out.println("contract details toString():");
				System.out.println(cd);
				
			}	
		}
		
		public static void fx() {
			
			/*
			try {
				ibs.requestMarketData( future.get(30, TimeUnit.SECONDS).get(0).contract() );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				System.out.println("Timeout trying to get streaming data for:" + contract.toString());
			}	
			*/
			
			/*
			try {
				ibs.requestMarketData( future.get(30, TimeUnit.SECONDS).get(0).contract() );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				System.out.println("Timeout trying to get streaming data for:" + contract.toString());
			}	*/
			
			Contract contract = new Contract();
			contract = new Contract();
			contract.symbol("USD");
			contract.secType(SecType.CASH);
			contract.exchange("IDEALPRO");
			contract.currency("CAD");
			//Future<List<ContractDetails>> cad = ibs.getContractDetails(contract);

		}
}
