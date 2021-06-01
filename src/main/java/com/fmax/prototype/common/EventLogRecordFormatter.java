package com.fmax.prototype.common;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class EventLogRecordFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		if(record instanceof EventLogRecord)
			return format( (EventLogRecord) record);
		return super.formatMessage(record);
	}

	protected String format(EventLogRecord record) {
		return new StringBuilder("Event: ").append( record.getEvent() ).toString();
	}
}
