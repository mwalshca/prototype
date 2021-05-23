package com.fmax.prototype.services.ib;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.quote.StockQuote;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.TagValue;
import com.ib.client.Types.SecType;

public class Driver {
	//TODO remove
		public static void main(String[] args) throws Exception {
			InteractiveBrokerService ibs = new InteractiveBrokerService();
			startStream(ibs, "RY", "NYSE", "RY");
			
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
			Contract usContract = cadCD.contract();
			dump( future.get() );
			ibs.reqTickByTickData(usContract, Driver::accept);
			
			
		}
		
		public static void accept(StockQuote stockQuote) {
			System.out.println("Quote received:" + stockQuote.toString());
		}
		
		public static void dump(List<ContractDetails> cds) {
			
			for(ContractDetails cd:cds) {
				for(TagValue tv: cd.secIdList())
					System.out.println(" iD:" + tv.m_tag + " " + tv.m_value);
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