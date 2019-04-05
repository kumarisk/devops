package com.vncdigital.vpulse.bed.service;

import org.springframework.data.repository.query.Param;

import com.vncdigital.vpulse.bed.model.RoomBookingDetails;
import com.vncdigital.vpulse.bed.model.RoomDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

public interface RoomBookingDetailsService {

	String getNextBookingId();
	
	RoomBookingDetails getroomStatus(String id);
	
	void save(RoomBookingDetails roomBookingDetails);
	
	RoomBookingDetails findByPatientRegistrationBooking(PatientRegistration patientRegistration);
	
	RoomBookingDetails findByPatientRegistrationBookingAndStatus(PatientRegistration patientRegistration,int status);

	


}
