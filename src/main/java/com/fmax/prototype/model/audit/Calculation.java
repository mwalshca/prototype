package com.fmax.prototype.model.audit;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fmax.prototype.persistence.UUIDConverter;

@Entity
@Table(name="calculation")
public class Calculation {
	@Id
	@GeneratedValue
	long id;
	
	@Column(name="eventID", columnDefinition="varbinary(16)", nullable=true)
	@Convert(converter=UUIDConverter.class)
	UUID eventID;
	
	@Column(name="name", length=255, nullable=false)
	String name;
	
	@Column(name="result", length=40, nullable=true)
	String result;

	
	public Calculation() {} //for JPA
	
	public Calculation(UUID eventID, String name, Object result) {
		this.eventID = eventID;
		this.name = Objects.requireNonNull(name);
		this.result = (null == result) ? "null":result.toString();
	}
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public UUID getEventID() {
		return eventID;
	}

	
	public void setEventID(UUID eventID) {
		this.eventID = eventID;
	}

	
	public String getName() {
		return name;
	}

	
	public void setName(String name) {
		this.name = name;
	}

	
	public String getResult() {
		return result;
	}

	
	public void setResult(String result) {
		this.result = result;
	}
}
