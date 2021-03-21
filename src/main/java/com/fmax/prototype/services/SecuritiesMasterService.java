package com.fmax.prototype.services;

import org.springframework.stereotype.Service;

import com.fmax.prototype.model.Stock;

@Service
public class SecuritiesMasterService {
	static final Stock rbc = new Stock("780087102", "RY.TO", "RY");
	
	public Stock getStock(String cusip) {
		return rbc;
	}

}
