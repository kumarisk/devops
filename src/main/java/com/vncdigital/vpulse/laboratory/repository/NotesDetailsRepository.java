package com.vncdigital.vpulse.laboratory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.laboratory.model.NotesDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;


@Repository
public interface NotesDetailsRepository extends CrudRepository<NotesDetails,String>{

	NotesDetails findFirstByOrderByNoteIdDesc();
	
	NotesDetails findByPatientRegistrationNotes(PatientRegistration patientRegistration);
	
	@Query(value="select * from mygit.v_note_f where p_reg_id=:regId",nativeQuery=true)
	List<NotesDetails> getAllReport(@Param("regId") String regId);
	
	
}
