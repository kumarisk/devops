package com.vncdigital.vpulse.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vncdigital.vpulse.osp.model.OspService;
import com.vncdigital.vpulse.osp.serviceImpl.OspServiceServiceImpl;
import com.vncdigital.vpulse.ospDto.OspServiceDto;
import com.vncdigital.vpulse.pharmacist.model.SalesPaymentPdf;
import com.vncdigital.vpulse.pharmacist.serviceImpl.SalesPaymentPdfServiceImpl;

@CrossOrigin(origins = "*", maxAge = 36000)
@RestController
@RequestMapping("/v1/osp")

public class OspServiceController {
	
	public static Logger Logger=LoggerFactory.getLogger(OspServiceController.class);
	
	
	@Autowired
	OspServiceServiceImpl ospServiceServiceImpl;
	
	@Autowired
	SalesPaymentPdfServiceImpl salesPaymentPdfServiceImpl;
	
	
	@RequestMapping(value="/create")
	List<Object> pageRefresh(){
		
		return ospServiceServiceImpl.pageRefrersh();
	}
	
	
	// * List of ospfilter (ONLY FOR 2 DAYS, 7 Days,) 
	 
		@RequestMapping(value="/ospfilter/{type}",method=RequestMethod.GET)
		public List<Map<String, String>> ospDetails(@PathVariable String type)
		{
			return ospServiceServiceImpl.ospDetails(type);
			
		}
	
	@RequestMapping(value="/getcost",method=RequestMethod.POST)
	
	public Map<String, String> getServiceCost(@RequestBody Map<String, String> map){
		
		String name=map.get("serviceName");
		return ospServiceServiceImpl.getOspServiceCost(name, "OSP");
	}
	
	@RequestMapping(value="/findAll")
	public List<Object> findAll(){
		
		return ospServiceServiceImpl.findAll();
	}
	
	@RequestMapping(value="/create",method=RequestMethod.POST)
	public SalesPaymentPdf computeOspService(@RequestBody OspServiceDto ospServiceDto,Principal principal){
		
		OspService ospService=new OspService();
		BeanUtils.copyProperties(ospServiceDto, ospService);
		
		
		return ospServiceServiceImpl.chargeForOspService(ospService, principal);
		
	}
	
	
	@RequestMapping(value="/pdf/{ospServiceId}")
	public Map<String, Object> getPdf(@PathVariable("ospServiceId") String ospServiceId){
		
		Map<String, Object> map=new HashMap<>();
		
		
		SalesPaymentPdf salesPaymentPdf=salesPaymentPdfServiceImpl.getOspPdf(ospServiceId);
		
		map.put("ospBill", salesPaymentPdf.getFileuri());
		
		
		return map;
	}

}