package com.vncdigital.vpulse.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vncdigital.vpulse.laboratory.dto.MasterCheckupServiceDto;
import com.vncdigital.vpulse.laboratory.model.MasterCheckupService;
import com.vncdigital.vpulse.laboratory.serviceImpl.MasterCheckupServiceServiceImpl;

@CrossOrigin(origins="*",maxAge=36000)
@RestController
@RequestMapping("/v1/mastercheckup")

public class MasterCheckUpController {
	
	@Autowired
	MasterCheckupServiceServiceImpl masterCheckupServiceServiceImpl;
	
	
	
	

	@RequestMapping(value="/getid",method=RequestMethod.GET)
	public List<Object> getId() {
		return masterCheckupServiceServiceImpl.pageRefresh();
	}	
	
	@RequestMapping(value="/create",method=RequestMethod.POST)
	public void computeSave(@RequestBody MasterCheckupServiceDto masterCheckupServiceDto,Principal principal) {
		MasterCheckupService masterCheckupService=new MasterCheckupService();
		BeanUtils.copyProperties(masterCheckupServiceDto, masterCheckupService);
		
		masterCheckupServiceServiceImpl.computeSave(masterCheckupService,principal);
	}
	
	
	@RequestMapping(value="/getAll")
	public List<Object> getAll(){
		
		
		return masterCheckupServiceServiceImpl.getAll();
	}

	
	@RequestMapping(value="getServices/{checkupId}")
	public List<MasterCheckupService> getServices(@PathVariable("checkupId") String checkupId){
		
		return masterCheckupServiceServiceImpl.findByCheckupId(checkupId);
	}
	
}