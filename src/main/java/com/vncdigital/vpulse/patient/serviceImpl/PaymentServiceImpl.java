package com.vncdigital.vpulse.patient.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.repository.PaymentRepository;
import com.vncdigital.vpulse.patient.service.PaymentService;


@Service
public class PaymentServiceImpl implements PaymentService
{
	@Autowired
	PaymentRepository paymentRepository;

	@Override
	public List<PatientPayment> findByPatientRegistration(String regId,String status) {
		return paymentRepository.findByPatientRegistration(regId,status);
	}
	
	
	
}
