package com.vncdigital.vpulse.laboratory.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.vncdigital.vpulse.laboratory.model.MasterCheckupService;

public interface MasterCheckupServiceService {

	List<Object> pageRefresh();

	void computeSave(MasterCheckupService masterCheckupService, Principal principal);

	List<Object> getAll();
	
	List<MasterCheckupService> findByCheckupId(String checkupId);

}