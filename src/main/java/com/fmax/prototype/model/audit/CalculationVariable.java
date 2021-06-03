package com.fmax.prototype.model.audit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="CalculationVariable")
public class CalculationVariable {
	@Id
	@GeneratedValue
	long id;
	
	@Column(name="name", length= 255, nullable=false)
	String name;
	
	@Column(name="value", length=1024, nullable=true)
	String value;

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name="calculation_id", nullable=false)
	Calculation calculation;

	
	public CalculationVariable() {} //for JPA
	
	public CalculationVariable(Calculation calculation, String name, Object value) {
		this.calculation = calculation;
		this.name = name;
		this.value = (null==value) ? "null": value.toString();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Calculation getCalculation() {
		return calculation;
	}

	public void setCalculation(Calculation calculation) {
		this.calculation = calculation;
	}
}
