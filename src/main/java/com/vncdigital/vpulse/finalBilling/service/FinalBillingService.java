package com.vncdigital.vpulse.finalBilling.service;

import com.vncdigital.vpulse.finalBilling.model.FinalBilling;

public interface FinalBillingService {
	
	void computeSave(FinalBilling finalBilling);
	
	FinalBilling findByBillTypeAndBillNoAndRegNo(String billtype,String billNo,String regNo);


}