package com.fmax.prototype.common;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class AsyncLogger {
	private LinkedBlockingQueue<LogRecord> records = new LinkedBlockingQueue<>();
	private Logger logger;
	private Thread logLoopThread = new Thread( this::logLoop, "AsyncLoggerLoggingThread");
	
	public AsyncLogger(Logger logger) {
		Objects.requireNonNull(logger);
		this.logger = logger;
		logLoopThread.start();
	}
	

	public void info(String message) {
		LogRecord record = new LogRecord(Level.INFO, message);
		log(record);
	}
	
	
	public void log(LogRecord logRecord) {
		records.add(logRecord);
	}
	
	
	public void logLoop() {
		while(true) {
			try {
				LogRecord record = records.take();
				logger.log(record);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); //reset flag
			}
		}
	}
	
	
}
