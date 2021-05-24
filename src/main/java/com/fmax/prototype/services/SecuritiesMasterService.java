package com.fmax.prototype.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fmax.prototype.model.Exchange;
import com.fmax.prototype.model.ISAN;
import com.fmax.prototype.model.Stock;
import com.fmax.prototype.services.ib.InteractiveBrokerService;

@Service
public class SecuritiesMasterService {
	static final Stock rbc = new Stock("780087102", "RY.TO", "RY");
	
	InteractiveBrokerService ibs;
	Map<StockKey, Stock> stocksByStockKey = new HashMap<>();
	
	public SecuritiesMasterService(InteractiveBrokerService ibs) {
		this.ibs = ibs;
	}
	
	public Stock getStock(Exchange exchange, ISAN isan) {
		Stock stock = stocksByStockKey.get( new StockKey(exchange, isan));
		if(null==stock) {
			stock = getStockFromProvider(exchange, isan);
		}
		return null;
	}

	private Stock getStockFromProvider(Exchange exchange, ISAN isan) {
		return null;
	}
	
	private static class StockKey {
		Exchange exchange;
		ISAN  isan;
		
		StockKey(Exchange exchange,ISAN isan){
			this.exchange = exchange;
			this.isan = isan;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
			result = prime * result + ((isan == null) ? 0 : isan.hashCode());
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
			if (isan == null) {
				if (other.isan != null)
					return false;
			} else if (!isan.equals(other.isan))
				return false;
			return true;
		}
	}
}
