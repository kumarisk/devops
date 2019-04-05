package com.vncdigital.vpulse.nurse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.vncdigital.vpulse.nurse.model.PrescriptionDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

public interface PrescriptionDetailsRepository extends CrudRepository<PrescriptionDetails,String>
{
	PrescriptionDetails findFirstByOrderByPrescriptionIdDesc();
	
	PrescriptionDetails findByPrescriptionId(String id);
	
	PrescriptionDetails findByRegId(String regId);
	
	PrescriptionDetails findByPatientRegistration(PatientRegistration patientRegistration);
	
	@Query(value="SELECT * FROM mygit.v_patient_prescription_details_f where file_namee like %:regId%",nativeQuery=true)
	List<PrescriptionDetails> getAllReport(@Param("regId") String regId);
	
}
