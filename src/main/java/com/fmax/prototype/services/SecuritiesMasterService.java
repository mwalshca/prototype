package com.fmax.prototype.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ISIN;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.services.ib.InteractiveBrokerService;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Types.SecType;

@Service
public class SecuritiesMasterService {
	InteractiveBrokerService ibs;
	Map<StockKey, Stock> stocksByStockKey = new HashMap<>();
	
	public SecuritiesMasterService(InteractiveBrokerService ibs) {
		this.ibs = ibs;
	}
	
	
	public Stock getStock(Exchange exchange, String symbol) {
		final StockKey key = new StockKey(exchange, symbol);
		Stock stock = stocksByStockKey.get(key);
		if( stock != null)
			return stock;
		
		Optional<Stock>  oStock = getStockFromProvider(exchange, symbol);
		if(oStock.isPresent() ) {
			stock = oStock.get();
			assert stock != null;
			stocksByStockKey.put(key, stock);
		}
		return stock;
	}

	
	public Optional<Stock> getStockFromProvider(Exchange exchange, String symbol) {
		
		Contract contract = new Contract();
		
		contract.secType(SecType.STK);		
		contract.exchange(exchange.toString());
		contract.symbol(symbol);
		contract.currency(exchange.currency());
		
		Future<List<ContractDetails>> future = ibs.getContractDetails(contract);
		
		List<ContractDetails> cds = null;
		try {
			cds = future.get(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.interrupted();
		} catch (ExecutionException e) {
			System.err.println("ibs.getStockFromProvider() Execution exception.");
		} catch (TimeoutException e) {
			System.err.println("ibs.getStockFromProvider() timeout on get.");
		}
		
		if(cds != null && !cds.isEmpty()) {
			ContractDetails cd = cds.get(0);
			if(cd.secIdList() !=null ) {
				Optional<Stock> stock =
						cd
					.secIdList()
					.stream()
					.filter( (tagValue)-> "ISIN".equals(tagValue.m_tag) )
					.map( (tagValue)-> tagValue.m_value)
					.findFirst()
					.map( (sIsin) -> new Stock(exchange, symbol, new ISIN(sIsin)) );
					return stock;
			}
		} 
		return Optional.empty();
	}
	
	private static class StockKey {
		Exchange exchange;
		String   symbol;
		
		StockKey(Exchange exchange, String symbol){
			this.exchange = exchange;
			this.symbol = symbol;
		}

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
			StockKey other = (StockKey) obj;
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
}
