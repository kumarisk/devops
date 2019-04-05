package com.vncdigital.vpulse.user.service;

import java.security.Principal;
import java.util.List;

import com.vncdigital.vpulse.doctor.dto.RefDoctorDetails;
import com.vncdigital.vpulse.doctor.model.RefPrescription;
import com.vncdigital.vpulse.laboratory.model.NotesDetails;
import com.vncdigital.vpulse.laboratory.model.NotesPdf;
import com.vncdigital.vpulse.nurse.model.PrescriptionDetails;
import com.vncdigital.vpulse.user.model.DoctorDetails;


public interface DoctorDetailsService 
{
	String getNextId();
	
	public List<RefDoctorDetails> getAll(Principal principal);
	
	public void createNotes(NotesDetails notesDetails);
	
	public void createPrescriptionNotes(NotesDetails notesDetails);
	
	public NotesPdf getNotes(String regId);
	
	public PrescriptionDetails create(RefPrescription refPrescription);
	
	public List<DoctorDetails> findBySpecilization(String specialization);
	
	DoctorDetails findByDrRegistrationo(String regNo);
}
