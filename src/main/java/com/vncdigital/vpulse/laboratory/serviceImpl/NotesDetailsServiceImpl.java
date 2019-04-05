package com.vncdigital.vpulse.laboratory.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.laboratory.model.NotesDetails;
import com.vncdigital.vpulse.laboratory.repository.NotesDetailsRepository;
import com.vncdigital.vpulse.laboratory.service.NotesDetailsService;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

@Service
public class NotesDetailsServiceImpl implements NotesDetailsService {
	
	public static Logger Logger=LoggerFactory.getLogger(NotesDetailsServiceImpl.class);
	
	
	@Autowired
	NotesDetailsRepository notesDetailsRepository;
	
	public String getNextNoteId()
	{
		NotesDetails notesDetails=notesDetailsRepository.findFirstByOrderByNoteIdDesc();
		String nextId=null;
		if(notesDetails==null)
		{
			nextId="NID0000001";
		}
		else
		{
			int nextIntId=Integer.parseInt(notesDetails.getNoteId().substring(3));
			nextIntId+=1;
			nextId="NID"+String.format("%07d", nextIntId);
		}
		return nextId;
	}
	
	
	
	public NotesDetails findByPatientRegistrationNotes(PatientRegistration patientRegistration)
	{
		return notesDetailsRepository.findByPatientRegistrationNotes(patientRegistration);
	}
}
