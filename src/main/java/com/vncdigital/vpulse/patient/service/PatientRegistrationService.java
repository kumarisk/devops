package com.vncdigital.vpulse.patient.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;

import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.user.model.User;




public interface PatientRegistrationService 
{
	public PatientRegistration save(PatientRegistration patientRegistration);
	
	public Optional<PatientRegistration> findById(Long id);
	
	public void delte(Long id);
	
	public PatientRegistration update(PatientRegistration patientRegistration);
	
	public String getNextRegId();
	
	List<PatientRegistration> findOnlyInpatient();
	
	List<PatientRegistration> findByPType( String pType);
	
	List<PatientRegistration> expectOutPatientTwoDays(String twoDayBack,String today);
	
	List<PatientRegistration> onlyOutPatientTwoDays(String twoDayBack,String today);

	
	List<PatientRegistration> findByPatientDetails(PatientDetails patientDetails);
	
	PatientRegistration findLatestReg(Long uid);
	
	PatientRegistration findByRegId(String id);
	
	List<PatientRegistration> expectOutPatient();
	
	Iterable<PatientRegistration> findAll();

	List<PatientRegistration> findPatient(String time,String type);
	
	List<PatientRegistration> findOutPatient(String time,String type);
	
	List<PatientRegistration> findByVuserD(User user);
	
	PatientRegistration patientAlredyExists(long pId,String date);
	
	
}
