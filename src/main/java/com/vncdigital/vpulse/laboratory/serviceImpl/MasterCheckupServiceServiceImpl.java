package com.vncdigital.vpulse.laboratory.serviceImpl;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.laboratory.model.LabServices;
import com.vncdigital.vpulse.laboratory.model.MasterCheckupService;
import com.vncdigital.vpulse.laboratory.repository.LabServicesRepository;
import com.vncdigital.vpulse.laboratory.repository.MasterCheckupServiceRepository;
import com.vncdigital.vpulse.laboratory.service.MasterCheckupServiceService;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;

@Service
public class MasterCheckupServiceServiceImpl implements MasterCheckupServiceService{
	
	@Autowired
	MasterCheckupServiceRepository masterCheckupServiceRepository;
	
	
	@Autowired
	LabServicesRepository labServicesRepository;
	
	@Autowired
	UserServiceImpl userServiceImpl;
	
	@Autowired
	LabServicesServiceImpl labServicesServiceImpl;
	
	public String getNexMasterId()
	{
		MasterCheckupService masterCheckupService=masterCheckupServiceRepository.findFirstByOrderByMasterCheckupIdDesc();
		String nextId=null;
		if(masterCheckupService==null)
		{
			nextId="MCI0000001";
		}
		else
		{
			int nextIntId=Integer.parseInt(masterCheckupService.getMasterCheckupId().substring(3));
			nextIntId+=1;
			nextId="MCI"+String.format("%07d", nextIntId);
		}
		return nextId;
	}


	public String getNextId()
	{
		MasterCheckupService masterCheckupService=masterCheckupServiceRepository.findFirstByOrderByMasterCheckupIdDesc();
		String nextId=null;
		if(masterCheckupService==null)
		{
			nextId="CI0000001";
		}
		else
		{
			int nextIntId=Integer.parseInt(masterCheckupService.getCheckupId().substring(2));
			nextIntId+=1;
			nextId="CI"+String.format("%07d", nextIntId);
		}
		return nextId;
	}

    @Override
	public List<Object> pageRefresh() {
    	
    	String serviceName=null;
    	List<Object> list=new ArrayList<Object>();
    	List<Object> serviceList=new ArrayList<Object>();
    	Map<String, String> map=new HashMap<String, String>();
    	map.put("checkupId", getNextId());
    	List<LabServices> labServices=labServicesRepository.findAll();
		List<String> serviceNameInfo=new ArrayList<>();
		for(LabServices labServicesInfo:labServices) {
			serviceName=labServicesInfo.getServiceName();
			if(!serviceNameInfo.contains(serviceName)) {
			
				Map<String, String> mapInfo=new HashMap<>();
				mapInfo.put("serviceName", serviceName);
				serviceNameInfo.add(serviceName);
				serviceList.add(mapInfo);
			}
		}
		list.add(map);
		list.add(serviceList);
    	
    	
    	
		return list;
	}

    @Transactional
    @Override
	public void computeSave(MasterCheckupService masterCheckupService, Principal principal) {
    	
   Timestamp timestamp=new Timestamp(System.currentTimeMillis()); 	
   //for Security 	
   User user=userServiceImpl.findByUserName(principal.getName());
 
   masterCheckupService.setCreatedBy(user.getUserId());
   masterCheckupService.setMasterCheckUpUser(user);
   masterCheckupService.setCreatedDate(timestamp);
   masterCheckupService.setCheckupId(getNextId());
   List<Map<String, String>> addCheckUp=masterCheckupService.getAddCheckUp();
   
   for(Map<String, String> addCheckUpInfo:addCheckUp) {
	   String serviceName=addCheckUpInfo.get("serviceName");
	   List<LabServices> labServices=labServicesServiceImpl.findByServiceName(serviceName);
	   masterCheckupService.setMasterServiceId(labServices.get(0).getMasterServiceId());
	   masterCheckupService.setMasterCheckupId(getNexMasterId());
	   masterCheckupService.setServiceName(serviceName);
	   masterCheckupServiceRepository.save(masterCheckupService);
	   
   }
   }

    

    @Override
	public List<Object> getAll() {
    	
    	String checkUpId=null;
    	List<Object> list=new ArrayList<Object>();
    	List<String> checkUpList=new ArrayList<String>();
    	 
    	List<MasterCheckupService> masterCheckupServices=masterCheckupServiceRepository.findAll();
    	for(MasterCheckupService masterCheckupServicesInfo:masterCheckupServices) {
    		checkUpId=masterCheckupServicesInfo.getCheckupId();
    		Map<String, String> map=new HashMap<String, String>();
    		if(!checkUpList.contains(checkUpId)) {
    			map.put("checkupId", checkUpId);
    			map.put("serviceName", masterCheckupServicesInfo.getMasterServiceName());
    			map.put("cost", String.valueOf(masterCheckupServicesInfo.getCost()));
    			checkUpList.add(checkUpId);
    			list.add(map);
    			
    			
    		}
    		
    	}
    	
    	
		return list;
	}


	@Override
	public List<MasterCheckupService> findByCheckupId(String checkupId) {
		return masterCheckupServiceRepository.findByCheckupId(checkupId);
	}


}