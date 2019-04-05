package com.vncdigital.vpulse.pharmacist.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.pharmacist.model.PatientSales;
import com.vncdigital.vpulse.pharmacist.repository.PatientSalesRepository;
import com.vncdigital.vpulse.pharmacist.service.PatientSalesService;
@Service
public class PatientSalesServiceImpl implements PatientSalesService 
{
	@Autowired
	PatientSalesRepository patientSalesRepository;
	
	public String getNextBillNo()
	{
		PatientSales patientSales=patientSalesRepository.findFirstByOrderByBillNoDesc();
		String nextId=null;
		if(patientSales==null)
		{
			nextId="PSB0000001";
		}
		else
		{
			int nextIntId=Integer.parseInt(patientSales.getBillNo().substring(3));
			nextIntId+=1;
			nextId="PSB"+String.format("%07d", nextIntId);
		}
		return nextId;
	}
	
	public 	PatientSales findOneBill(String billno, String name,String batch)
	{
		return patientSalesRepository.findOneBill(billno, name,batch);
	}
	
	public PatientSales save(PatientSales patientSales)
	{
		return patientSalesRepository.save(patientSales);
	}

	

}
