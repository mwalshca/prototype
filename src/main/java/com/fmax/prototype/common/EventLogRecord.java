package com.fmax.prototype.common;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.fmax.prototype.events.Event;

public class EventLogRecord extends LogRecord {
	private static final long serialVersionUID = 1L;
	
	Event event;
	
	public EventLogRecord(Event event) {
		super(Level.INFO, "");
	}

	public Event getEvent() {
		return event;
	}
}
