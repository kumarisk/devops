
package com.vncdigital.vpulse.patient.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientPaymentPdf;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.model.ReferralDetails;




public interface PatientDetailsService 
{
	
	public PatientDetails save(PatientDetails patientDetails);
	
	public Optional<PatientDetails> findById(Long id);
	
	public List<PatientDetails> findAll();
	
	public void delte(Long id);
	
	public PatientDetails update(PatientDetails patientDetails);
	
	PatientDetails findByMobile(long mobile);
	
	public String getNextUmr();
	
	PatientDetails getPatientByUmr(String umr);
	
	List<PatientDetails> findAllByOrderByPatientIdDesc();
	
	List<Object> pageLoad();
	
	public List<Object> getAllPd();
	
	List<PatientDetails> patientAlreadyExists(
			String mobile,
			String fname,
			String lname);
	
	
	public PatientDetails updatePatient( PatientDetails patientDetails, String umr);
	
	public PatientPaymentPdf saveInfo(PatientDetails referralDetails,Principal p) throws Exception;
	
	public PatientPaymentPdf newRegistration(PatientRegistration patientRegistration,String umr,Principal p);
	
	public List<Map<String, String>> patientDetails(String type);
	
	public List<Map<String, String>> inPatientDetails();
	
	public List<Map<String, String>> outPatientDetails(String type);
	
	
	public PatientPaymentPdf advanceAmount(Map<String,Object> info, String umr,Principal principal);
	
	public PatientPaymentPdf blankPrescription(String regId);
	
	public PatientPaymentPdf  admnWiseSales(String regId,Principal principal);
	
	
	
	
	
	
	
}
