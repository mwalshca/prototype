package com.fmax.prototype.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fmax.prototype.events.Event;

public interface EventRepository extends JpaRepository<Event,UUID> {

	@Query("select e from Event e where e.eventTime >= :from AND e.eventTime <= :to")
	List<Event> retrieveByDateTimeRange( @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
