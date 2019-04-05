package com.vncdigital.vpulse.osp.service;

import java.security.Principal;
import java.util.List;

import com.vncdigital.vpulse.osp.model.OspService;
import com.vncdigital.vpulse.pharmacist.model.SalesPaymentPdf;

public interface OspServiceService {
	
	public List<Object> pageRefrersh();

	SalesPaymentPdf chargeForOspService(OspService ospService, Principal principal);
	
	public List<Object> findAll();

}
