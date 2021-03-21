package com.fmax.prototype.services.ordermanagement;

import com.fmax.prototype.model.trade.BuyOrder;

public class PlaceBuyOrderRequest extends OrderManagementRequest {

	private final BuyOrder buyOrder;
	
	public PlaceBuyOrderRequest(final BuyOrder buyOrder) {
		this.buyOrder = buyOrder;
	}

	public BuyOrder getBuyOrder() {
		return buyOrder;
	}

	@Override
	public String toString() {
		return "BuyOrderRequest [buyOrder=" + buyOrder + "]";
	}
}
