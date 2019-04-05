package com.vncdigital.vpulse.finalBilling.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.finalBilling.model.FinalBilling;
import com.vncdigital.vpulse.finalBilling.repository.FinalBillingRepository;
import com.vncdigital.vpulse.finalBilling.service.FinalBillingService;

@Service
public class FinalBillingServiceImpl implements FinalBillingService {

	@Autowired
	FinalBillingRepository finalBillingRepository;
	
	@Override
	public void computeSave(FinalBilling finalBilling) {
		finalBillingRepository.save(finalBilling);
	}

	@Override
	public FinalBilling findByBillTypeAndBillNoAndRegNo(String billtype, String billNo, String regNo) {
		return finalBillingRepository.findByBillTypeAndBillNoAndRegNo(billtype, billNo, regNo);
	}



}
