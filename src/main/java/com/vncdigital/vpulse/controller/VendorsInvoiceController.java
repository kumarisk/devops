package com.vncdigital.vpulse.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vncdigital.vpulse.pharmacist.dto.VendorsInvoiceDto;
import com.vncdigital.vpulse.pharmacist.helper.RefApprovedListHelper;
import com.vncdigital.vpulse.pharmacist.helper.RefInvoiceIds;
import com.vncdigital.vpulse.pharmacist.model.MedicineProcurement;
import com.vncdigital.vpulse.pharmacist.model.VendorsInvoice;
import com.vncdigital.vpulse.pharmacist.repository.MedicineProcurementRepository;
import com.vncdigital.vpulse.pharmacist.serviceImpl.MedicineProcurementServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.VendorsInvoiceServiceImpl;

@CrossOrigin(origins="*",maxAge=36000)
@RestController
@RequestMapping("/v1/pharmacist")
public class VendorsInvoiceController {
	
	public static Logger Logger=LoggerFactory.getLogger(VendorsInvoiceController.class);
	
	
	@Autowired
	VendorsInvoiceServiceImpl vendorsInvoiceServiceImpl;
	
	@Autowired
	MedicineProcurementServiceImpl  medicineProcurementServiceImpl;
	
	@Autowired
	MedicineProcurementRepository  medicineProcurementRepository;
	
	@Autowired
	RefApprovedListHelper refApprovedListHelper;
	
	@Autowired
	RefInvoiceIds refInvoiceIds;
	
	
	@RequestMapping(value="/invoice/getApproved")
	public List<RefApprovedListHelper> getApprovedProcurement()
	{
		
		return vendorsInvoiceServiceImpl.getApprovedProcurement();
	}
	
	@RequestMapping(value="/invoice/pay",method=RequestMethod.GET)
	public RefInvoiceIds getInvoiceId()
	{
		 refInvoiceIds.setInvoiceId(vendorsInvoiceServiceImpl.getNextInvoice());
		return refInvoiceIds;
	}
	
	@RequestMapping(value="/invoice/pay/{procurementId}",method=RequestMethod.POST)
	public void raisePayment(@RequestBody VendorsInvoiceDto vendorsInvoiceDto,@PathVariable String procurementId)
	{
		VendorsInvoice vendorsInvoice=new VendorsInvoice();
		BeanUtils.copyProperties(vendorsInvoiceDto, vendorsInvoice);
		vendorsInvoice.setVendorInvoiceMedicineProcurement(procurementId);
		long nextBalance=vendorsInvoice.getBalanceAmount()-vendorsInvoice.getPaid_amount();
		System.out.println(vendorsInvoice.getBalanceAmount());
		System.out.println(vendorsInvoice.getPaid_amount());
		System.out.println(nextBalance);
		
		vendorsInvoice.setBalanceAmount(nextBalance);
		vendorsInvoice.setPaymentDate(new Timestamp(System.currentTimeMillis()));
		vendorsInvoice.setDueAmount(vendorsInvoice.getBalanceAmount());
		vendorsInvoiceServiceImpl.computeSave(vendorsInvoice);
		
	}
}
