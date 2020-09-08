package com.fmax.prototype.events;

public abstract class Event {

	protected Event(EventType eventType) {
		this.eventType = eventType;
	}
	
	EventType eventType;

	public EventType getEventType() {
		return eventType;
	}

	@Override
	public String toString() {
		return "Event [eventType=" + eventType + ", getEventType()=" + getEventType() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	} 
}
