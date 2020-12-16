package com.fmax.prototype.common;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class BusinessLogRecordFormattter extends Formatter {

	@Override
	public String format(LogRecord record) {
		if(record instanceof CalculationLogRecord)
			return format( (CalculationLogRecord) record);
		return super.formatMessage(record);
	}
	
	protected String format(CalculationLogRecord record) {
		StringBuilder sb = new StringBuilder("\nCalculation: ");
		sb.append( record.getName() );
		sb.append('\n');
		sb.append("Variables:\n");
		
		for(String key:record.getVariables().keySet()) {
			sb.append("\t");
			sb.append( key );
			sb.append("\t\t");
			sb.append( record.getVariables().get(key));
			sb.append('\n');
		}
		sb.append("Result: ");
		sb.append( record.getResult() );
		sb.append("\n\n");
		return sb.toString();
	}

}
