package com.vncdigital.vpulse.pharmacist.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.data.repository.query.Param;

import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.pharmacist.helper.RefSalesIds;
import com.vncdigital.vpulse.pharmacist.model.Sales;
import com.vncdigital.vpulse.pharmacist.model.SalesPaymentPdf;

public interface SalesService 
{
	String getNextSaleNo();
	
	public String getNextBillNo();
	
	SalesPaymentPdf computeSave(Sales sales,Principal principal);
	
	List<Sales> findByBillNo(String id);
	
	List<Sales> findByPatientRegistration(PatientRegistration patientRegistration);
	
	/*
	 * Modified code for sales
	 */
	public List<Map<String, String>> findMedicineDetailsModified(List<Map<String,String>> medicine);
	
	public RefSalesIds findMedicineDetails(String medicine);
	
	public List<Object> getBillIds();
	
	//new code to get available medicine for next sales
	public List<Sales> findByBatchAndMedicine(String batch,String medicine);
	
	List<Sales> findByName(String medName);
	
	List<Sales> findByPaymentTypeAndPatientRegistration(String payment,PatientRegistration reg);
	
	List<Sales> findByPatientRegistrationAndPaymentType(PatientRegistration patientRegistration,String paymentType);

	
	

	

}
