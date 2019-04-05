package com.vncdigital.vpulse.laboratory.service;

import java.util.List;

import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.model.NotesPdf;

public interface NotesPdfService {
	
	String getNextLabId();
	
	NotesPdf findByNid(String id);
	
	NotesPdf findByRegId(String id);
	
	
	
}
