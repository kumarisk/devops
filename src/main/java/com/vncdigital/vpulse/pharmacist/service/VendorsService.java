package com.vncdigital.vpulse.pharmacist.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vncdigital.vpulse.pharmacist.model.Vendors;

public interface VendorsService 
{
	void computeSave(Vendors vendors);
	
	void computeUpdate(Vendors vendors);
	
	//String getNextRegId();
	
	String getNextVendorId();
	
	
	Vendors findByVendorName(String name);
	
	List<Vendors> findAll();
	
	Vendors findByVendorId(String id);

}
