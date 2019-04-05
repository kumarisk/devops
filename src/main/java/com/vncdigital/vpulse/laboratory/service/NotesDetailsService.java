package com.vncdigital.vpulse.laboratory.service;

import java.util.List;

import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.model.NotesDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

public interface NotesDetailsService {
	
	String getNextNoteId();
	
	
	
	NotesDetails findByPatientRegistrationNotes(PatientRegistration patientRegistration);
	
	
}
