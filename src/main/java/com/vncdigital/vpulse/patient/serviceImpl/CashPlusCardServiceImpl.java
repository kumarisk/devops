package com.vncdigital.vpulse.patient.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.patient.model.CashPlusCard;
import com.vncdigital.vpulse.patient.repository.CashPlusCardRepository;
import com.vncdigital.vpulse.patient.service.CashPlusCardService;

@Service
public class CashPlusCardServiceImpl implements CashPlusCardService{
	
	@Autowired
	CashPlusCardRepository cashPlusCardRepository;
	
	public 	CashPlusCard save(CashPlusCard cashPlusCard)
	{
		return cashPlusCardRepository.save(cashPlusCard);
	}

	@Override
	public CashPlusCard findByBillNo(String billNo) {
		return cashPlusCardRepository.findByBillNo(billNo);
		
		
	}
	
	public List<CashPlusCard> findByBillNoAndDescriptions(String fromDate,String toDate,String uId) {
		return cashPlusCardRepository.findByBillNoAndDescriptions(fromDate,toDate,uId);
	}



}
