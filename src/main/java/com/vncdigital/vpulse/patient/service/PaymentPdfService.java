package com.vncdigital.vpulse.patient.service;

import org.springframework.data.repository.query.Param;

import com.vncdigital.vpulse.patient.model.PatientPaymentPdf;

public interface PaymentPdfService {
	
	void save(PatientPaymentPdf pdf);
	
	PatientPaymentPdf getBlankPdf(String regId);
	
	PatientPaymentPdf findById(String id);
	
	public String getNextPdfId();
	

}
