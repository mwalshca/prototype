package com.fmax.prototype.services.ordermanagement;

import com.fmax.prototype.model.trade.StockOrder;

public class ChangeOrderRequest extends OrderManagementRequest {

	final StockOrder originalOrder;
	
	public ChangeOrderRequest(StockOrder originalOrder, int newSize) {
            this.originalOrder = originalOrder;
	}
}
