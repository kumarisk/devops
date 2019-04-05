package com.vncdigital.vpulse.user.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class SpecUserJoin {
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name="user_id")
	private User userSpec;
	
	@ManyToOne
	@JoinColumn(name="spec_id")
	private DoctorSpecialization docSpec;


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public User getUserSpec() {
		return userSpec;
	}


	public void setUserSpec(User userSpec) {
		this.userSpec = userSpec;
	}


	public DoctorSpecialization getDocSpec() {
		return docSpec;
	}


	public void setDocSpec(DoctorSpecialization docSpec) {
		this.docSpec = docSpec;
	}
	
	
	
	

}

