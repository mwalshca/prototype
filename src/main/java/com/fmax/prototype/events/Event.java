package com.fmax.prototype.events;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.fmax.prototype.persistence.UUIDConverter;

@Entity
@Table(name="event")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="dsc", discriminatorType = DiscriminatorType.STRING, length=64)
public abstract class Event {
	@Id
	@Column(name="id", columnDefinition="varbinary(16)")
	@Convert(converter=UUIDConverter.class)
	UUID eventID;
	
	
	@Column(name="event_type", nullable=false, length=64)
	@Enumerated(EnumType.STRING)
	EventType eventType;
	
	@Column(name="event_dttm", columnDefinition="DateTime")
	LocalDateTime eventTime;
	
	@Column(name="dttm_persisted", columnDefinition="DateTime")
	LocalDateTime dttmPersisted;
	
	protected Event(EventType eventType) {
		this.eventType = eventType;
		this.eventID= UUID.randomUUID();
		this.eventTime = LocalDateTime.now();
	}
	
	
	protected Event() {}  //for JPA	
	
	
	@PrePersist
	void setDateTimePersisted() {
		dttmPersisted = LocalDateTime.now();
	}
	
	
	public EventType getEventType() {
		return eventType;
	}

	
	public UUID getEventID() {
		return eventID;
	}

	
	public LocalDateTime getEventTime() {
		return eventTime;
	}	
}
