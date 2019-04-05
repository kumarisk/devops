package com.vncdigital.vpulse.appointment.service;

import java.security.Principal;
import java.util.List;

import com.vncdigital.vpulse.appointment.model.DoctorAppointment;

public interface DoctorAppoinmentService {
	
	public List<Object> pageLoad(String slot);
	
	public List<Object> pageRefresh();

	void computeSave(DoctorAppointment doctorAppointment,Principal principal);
	
	List<DoctorAppointment> findAll();

}