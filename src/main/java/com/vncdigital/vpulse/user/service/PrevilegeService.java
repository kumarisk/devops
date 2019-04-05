package com.vncdigital.vpulse.user.service;

import com.vncdigital.vpulse.user.model.Previlege;

public interface PrevilegeService 
{

	public Previlege save(Previlege pw);
	
	public Previlege findByName(String name);

}
