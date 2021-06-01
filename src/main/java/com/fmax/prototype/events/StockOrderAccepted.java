package com.fmax.prototype.events;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("STOCK_ORDER_ACCEPTED")
public class StockOrderAccepted extends StockOrderEvent {
	
	
	@Column(name="stk_evnt_dttm", columnDefinition = "TIMESTAMP")
	private LocalDateTime dttmAccepted;
	
	public StockOrderAccepted(long orderId, LocalDateTime dttmAccepted) {
		super(EventType.STOCK_ORDER_ACCEPTED, orderId);
		this.dttmAccepted = Objects.requireNonNull( dttmAccepted );
	}
	
	protected StockOrderAccepted() {} // for JPA
	

	public LocalDateTime getDttmAccepted() {
		return dttmAccepted;
	}

	@Override
	public String toString() {
		return "StockOrderAccepted [dttmAccepted=" + dttmAccepted + ", orderId=" + orderId + ", eventID=" + eventID
				+ ", eventType=" + eventType + "]";
	}
}
