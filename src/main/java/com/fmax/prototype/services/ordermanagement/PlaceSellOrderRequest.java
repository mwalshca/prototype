package com.fmax.prototype.services.ordermanagement;

import com.fmax.prototype.model.trade.SellOrder;

public class PlaceSellOrderRequest extends OrderManagementRequest {

	private final SellOrder sellOrder;
	
	public PlaceSellOrderRequest(final SellOrder buyOrder) {
		this.sellOrder = buyOrder;
	}

	public SellOrder getSellOrder() {
		return sellOrder;
	}

	@Override
	public String toString() {
		return "PlaceSellOrderRequest [sellOrder=" + sellOrder + "]";
	}
}
