package com.vncdigital.vpulse.patient.service;

import java.util.List;

import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientRegistration;




public interface PaymentService 
{
	List<PatientPayment> findByPatientRegistration(String regId,String status);


}
