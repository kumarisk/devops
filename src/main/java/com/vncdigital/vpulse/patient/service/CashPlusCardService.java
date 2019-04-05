package com.vncdigital.vpulse.patient.service;

import com.vncdigital.vpulse.patient.model.CashPlusCard;

public interface CashPlusCardService {
	
	CashPlusCard save(CashPlusCard cashPlusCard);
	
	CashPlusCard findByBillNo(String billNo);

}
