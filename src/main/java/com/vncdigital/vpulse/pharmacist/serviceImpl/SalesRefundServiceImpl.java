package com.vncdigital.vpulse.pharmacist.serviceImpl;

import java.sql.Timestamp;
import java.util.List;

import org.bouncycastle.jce.provider.JCEKeyGenerator.Salsa20;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.serviceImpl.PatientDetailsServiceImpl;
import com.vncdigital.vpulse.pharmacist.helper.RefSalesReturn;
import com.vncdigital.vpulse.pharmacist.model.Location;
import com.vncdigital.vpulse.pharmacist.model.Sales;
import com.vncdigital.vpulse.pharmacist.model.SalesRefund;
import com.vncdigital.vpulse.pharmacist.model.SalesReturn;
import com.vncdigital.vpulse.pharmacist.repository.SalesRefundRepository;
import com.vncdigital.vpulse.pharmacist.repository.SalesRepository;
import com.vncdigital.vpulse.pharmacist.repository.SalesReturnRepository;
import com.vncdigital.vpulse.pharmacist.service.SalesRefundService;
import com.vncdigital.vpulse.pharmacist.service.SalesReturnService;
@Service
public class SalesRefundServiceImpl implements SalesRefundService {
	
	@Autowired
	SalesReturnRepository salesReturnRepository;
	
	@Autowired
	SalesServiceImpl salesServiceImpl;
	
	@Autowired
	SalesRepository salesRepository;
	
	@Autowired
	SalesRefundRepository salesRefundRepository;
	
	@Autowired
	MedicineDetailsServiceImpl medicineDetailsServiceImpl;
	
	@Autowired
	PatientDetailsServiceImpl patientDetailsServiceImpl;
	
	@Autowired
	LocationServiceImpl locationServiceImpl;

	@Override
	public String getNextReturnId() {
		SalesRefund salesRefund=salesRefundRepository.findFirstByOrderByReturnIdDesc();
		String nextId=null;
		if(salesRefund==null)
		{
			nextId="RS0000001";
		}
		else
		{
			int nextIntId=Integer.parseInt(salesRefund.getReturnId().substring(2));
			nextIntId+=1;
			nextId="RS"+String.format("%07d", nextIntId);
		}
		return nextId;
	}

	public SalesRefund findByReturnId(String id)
	{
		return salesRefundRepository.findByReturnId(id);
	}

	
	

}
