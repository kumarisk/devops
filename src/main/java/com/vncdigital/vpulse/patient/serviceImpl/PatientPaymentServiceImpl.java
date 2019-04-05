package com.vncdigital.vpulse.patient.serviceImpl;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.repository.PatientPaymentRepository;
import com.vncdigital.vpulse.patient.service.PatientPaymentService;

@Service
public class PatientPaymentServiceImpl implements PatientPaymentService{
	
	@Autowired
	PatientPaymentRepository patientPaymentRepository;
	
	public 	List<PatientPayment> findByModeOfPaymantAndPatientRegistration(String name,PatientRegistration reg)
	{
		return patientPaymentRepository.findByModeOfPaymantAndPatientRegistration(name, reg);
	}
	
	public List<PatientPayment>findDueBill( String regId)
	{
		return patientPaymentRepository.findDueBill(regId);
	}
	
	public 	PatientPayment save(PatientPayment patientPayment)
	{
		return patientPaymentRepository.save(patientPayment);
	}
	
	public Set<PatientPayment> findByPatientRegistration(PatientRegistration reg)
	{
		return patientPaymentRepository.findByPatientRegistration(reg);
	}
	
	public 	PatientPayment findPatientByRegFee(String regId,String typeOfCharge)
	{
		return patientPaymentRepository.findPatientByRegFee(regId,typeOfCharge);
	}
	
	public String findNextBillNo()
	{
		
		PatientPayment patientPayment= patientPaymentRepository.findFirstByOrderByBillNoDesc();
		String billNo="";
		if(patientPayment==null)
		{
			billNo="BL000000001";
		}
		else
		{
			String curntBillNo=patientPayment.getBillNo();
			long intbillNo= Long.parseLong(curntBillNo.substring(2));
			intbillNo+=1;
			 billNo="BL"+String.format("%09d", intbillNo);
			
		}
		return billNo;
	}
	
	public List<PatientPayment> findAll()
	{
		return patientPaymentRepository.findAll();
	}


}
