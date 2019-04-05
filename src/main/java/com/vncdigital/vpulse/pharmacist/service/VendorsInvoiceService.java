package com.vncdigital.vpulse.pharmacist.service;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vncdigital.vpulse.pharmacist.helper.RefApprovedListHelper;
import com.vncdigital.vpulse.pharmacist.model.VendorsInvoice;

public interface VendorsInvoiceService  
{
	List<VendorsInvoice> findByVendorInvoiceMedicineProcurement(String id);
	
	List<String> findOneInvoice(String id);
	
	String getNextInvoice();
	
	void computeSave(VendorsInvoice vendorsInvoice);

	long findSumOfPaidAmount(String pid);
	
	public List<RefApprovedListHelper> getApprovedProcurement();
	
}
