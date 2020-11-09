package com.fmax.prototype.model.trade;

public enum OrderStatus {
	IN_DESIGN,
	TO_BE_PLACED,
	PLACED,
	ACCEPTED,
	COMPLETED; //TODO handle cancel scenario
}
