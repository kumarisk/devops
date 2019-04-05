package com.vncdigital.vpulse.pharmacist.service;

import com.vncdigital.vpulse.pharmacist.model.SalesRefund;
import com.vncdigital.vpulse.pharmacist.model.SalesReturn;

public interface SalesRefundService 
{
	String getNextReturnId();
	
	SalesRefund findByReturnId(String id);
	
}
