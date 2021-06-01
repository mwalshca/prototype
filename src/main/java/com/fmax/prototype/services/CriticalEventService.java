package com.fmax.prototype.services;

import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fmax.prototype.events.Event;

@Scope("singleton")
@Service
public class CriticalEventService {
	
	private LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<>();
	private Thread pullThread = new Thread(this::pull, "CriticalEventService.pull");
	private final ApplicationContext appContext;
	
	public CriticalEventService(ApplicationContext appContext) {
		this.appContext = appContext;
		pullThread.start();
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

	
	protected void pull() {
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
}
