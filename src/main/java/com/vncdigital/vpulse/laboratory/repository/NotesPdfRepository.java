package com.vncdigital.vpulse.laboratory.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.model.NotesPdf;

@Repository
public interface NotesPdfRepository extends CrudRepository<NotesPdf,String>{

	NotesPdf findFirstByOrderByNidDesc();
	
	NotesPdf findByNid(String id);
	
	NotesPdf findByRegId(String id);
	
	
}
