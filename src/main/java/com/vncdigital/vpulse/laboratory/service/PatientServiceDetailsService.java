package com.vncdigital.vpulse.laboratory.service;

import java.util.List;

import com.vncdigital.vpulse.laboratory.model.PatientServiceDetails;

public interface PatientServiceDetailsService {
	
	String getNextId();
	
	PatientServiceDetails findByRegId(String id);
	
	void save(PatientServiceDetails patientServiceDetails);
	
	List<PatientServiceDetails> findByPatientServiceAndPatientLabService(String regId,String serviceId);
	
	
}
