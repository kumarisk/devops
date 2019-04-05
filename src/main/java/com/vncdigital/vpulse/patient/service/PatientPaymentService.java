package com.vncdigital.vpulse.patient.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.query.Param;

import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

public interface PatientPaymentService {
	
	List<PatientPayment> findByModeOfPaymantAndPatientRegistration(String name,PatientRegistration reg);
	
	List<PatientPayment>findDueBill(String regId);
	
	String findNextBillNo();
	
	List<PatientPayment> findAll();

	
	PatientPayment save(PatientPayment patientPayment);
	
	Set<PatientPayment> findByPatientRegistration(PatientRegistration reg);
	
	PatientPayment findPatientByRegFee(String regId,String typeOfCharge);

}
