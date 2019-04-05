package com.vncdigital.vpulse.pharmacist.service;

import org.springframework.data.repository.query.Param;

import com.vncdigital.vpulse.pharmacist.model.PatientSales;

public interface PatientSalesService 
{
	String getNextBillNo();
	
	PatientSales findOneBill(String billno, String name,String batch);
	
	PatientSales save(PatientSales patientSales);

}
