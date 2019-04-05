package com.vncdigital.vpulse.user.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.user.model.Previlege;
import com.vncdigital.vpulse.user.repository.PrevilegeRepository;
import com.vncdigital.vpulse.user.service.PrevilegeService;

@Service
public class PrevilegeServiceImpl implements PrevilegeService
{
	@Autowired
	PrevilegeRepository repo;
	
	public Previlege save(Previlege pw)
	{
		return repo.save(pw);
	}
	
	public Previlege findByName(String name)
	{
		return repo.findByName(name);
	}
	
	

}
