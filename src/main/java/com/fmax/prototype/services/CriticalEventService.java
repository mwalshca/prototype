package com.fmax.prototype.services;

import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fmax.prototype.common.CalculationLogRecord;
import com.fmax.prototype.events.Event;
import com.fmax.prototype.model.audit.Calculation;
import com.fmax.prototype.model.audit.CalculationVariable;

@Scope("singleton")
@Service
public class CriticalEventService {
	
	private final ApplicationContext appContext;
	
	private LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<>();
	private Thread pullThread = new Thread(this::pullEvents, "CriticalEventService.pullEvents");
	
	private LinkedBlockingQueue<CalculationLogRecord> calculations = new LinkedBlockingQueue<>();
	private Thread pullCalculationsThread = new Thread(this::pullCalculations, "CriticalEventService.pullCalculations");
	
	public CriticalEventService(ApplicationContext appContext) {
		this.appContext = appContext;
		pullThread.start();
		pullCalculationsThread.start();
	}

	public void push(Event event) {
		boolean put = false;
		do {
			try {
				events.put(event);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			}
		} while (!put);
	}

	
	public void push(CalculationLogRecord record) {
		boolean put = false;
		do {
			try {
				calculations.put(record);
				put = true;
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			}
		} while (!put);
	}
	
	
	protected void pullEvents() {
		PersistenceHelper ph = appContext.getBean(PersistenceHelper.class);
		while (true) {
			try {
				Event event = events.take();
				ph.persist(event);
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			} catch (Exception ex) {
				System.err.println(String.format("Error persisting Event. ex=%s", ex));
				ex.printStackTrace(System.err);
			}
		}
	}
	
	
	protected void pullCalculations() {
		PersistenceHelper ph = appContext.getBean(PersistenceHelper.class);
		while (true) {
			try {
				CalculationLogRecord record = calculations.take();
				
				Calculation calculation = new Calculation( record.getEventID(), record.getName(), record.getResult() );
				ph.persist(calculation);
				
				for(String key: record.getVariables().keySet()) {
					CalculationVariable calculationVariable = 
							new CalculationVariable(calculation, key, record.getVariables().get(key));
					ph.persist(calculationVariable);
				}
			} catch (InterruptedException e) {
				Thread.interrupted(); // reset the interrupted flag
			} catch (Exception ex) {
				System.err.println(String.format("Error persisting Calculation. ex=%s", ex));
				ex.printStackTrace(System.err);
				System.err.println("After stack trace.");
			}
		}
	}
}
