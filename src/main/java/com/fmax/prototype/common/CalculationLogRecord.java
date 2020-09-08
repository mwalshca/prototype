package com.fmax.prototype.common;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class CalculationLogRecord extends LogRecord {
	private static final long serialVersionUID = 1L;
	
	String name;
	Object result;
	
	HashMap<String, Object> variables = new HashMap<>();
	
	
	public CalculationLogRecord() {
		super(Level.INFO, "");
	}
	
	public void setVariable(String name, Object value) {
		variables.put(name, value);
	}
	
	public HashMap<String, Object> getVariables(){
		return variables;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public void setVariables(HashMap<String, Object> variables) {
		this.variables = variables;
	}
	
}
