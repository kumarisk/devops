package com.vncdigital.vpulse.bill.service;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.vncdigital.vpulse.bill.model.ChargeBill;
import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.pharmacist.model.Sales;

public interface ChargeBillService {
	
	String getNextId();
	
	String getNextBillNo();
	
	ChargeBill findBySaleId(Sales sale);
	
	List<ChargeBill> findByPatRegIdAndNetAmountNot(PatientRegistration patientRegistration,float amt);
	
	void save(ChargeBill bill);
	
	ChargeBill findByChargeBillId(String id);
	
	List<ChargeBill> findByPatRegIdAndPaid(PatientRegistration patientRegistration,String paid);


	List<ChargeBill> findByPatRegId(PatientRegistration patientRegistration);
	
	ChargeBill findByLabId(LaboratoryRegistration laboratoryRegistration);

	List<ChargeBill> findAllLab(String regId);
	
	List<ChargeBill> findDueBill(String regId);

}
