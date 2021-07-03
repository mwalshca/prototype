 package com.fmax.prototype.contollers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fmax.prototype.events.Event;
import com.fmax.prototype.persistence.EventRepository;



@RestController
@RequestMapping("/ui")
@Transactional
public class UIController {
	private static final LocalTime midnight = LocalTime.of(0, 0);
	private EventRepository eventRepo;
	
	UIController(EventRepository eventRepo){
		this.eventRepo = eventRepo;
	}
	
	@GetMapping(path="/events")
	List<Event> getEvents() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		
		LocalDateTime midnightToday =  LocalDateTime.of(today, midnight);
		LocalDateTime midnightTomorrow = LocalDateTime.of(tomorrow, midnight);
		
		return eventRepo.retrieveByDateTimeRange(midnightToday, midnightTomorrow);
	}

}
