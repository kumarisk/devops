package com.vncdigital.vpulse.pharmacist.service;

import java.security.Principal;
import java.util.List;

import com.vncdigital.vpulse.pharmacist.model.SalesPaymentPdf;
import com.vncdigital.vpulse.pharmacist.model.SalesReturn;

public interface SalesReturnService 
{
	String getNextReturnSaleNo();
	
	
	String getNextReturnMasterSaleNo();
	
	SalesPaymentPdf computeSave(SalesReturn salesReturn,Principal principal);
	

	List<SalesReturn> findByMasterSaleNo(String id);


	List<Object> displaySalesReturnList(int days);


}
