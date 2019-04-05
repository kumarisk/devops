package com.vncdigital.vpulse.nurse.service;

import java.util.List;

import com.vncdigital.vpulse.nurse.model.PrescriptionDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

public interface PrescriptionDetailsService
{
	PrescriptionDetails save(PrescriptionDetails prescriptionDetails);
	
	void computeSave(PrescriptionDetails prescriptionDetails);
	
	public String generatePrescriptionId();
	
	Iterable<PrescriptionDetails> findAll();
	
	public PrescriptionDetails getFile(String id);
	
	PrescriptionDetails findByRegId(String regId);
	
	PrescriptionDetails findByPatientRegistration(PatientRegistration patientRegistration);
	

}
