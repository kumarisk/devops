package com.vncdigital.vpulse.user.service;

import com.vncdigital.vpulse.user.model.PasswordStuff;
import com.vncdigital.vpulse.user.model.User;


public interface PasswordStuffService 
{
	
	public PasswordStuff save(PasswordStuff pw);
	
	public long count();
	
	public String findFirstByOrderByPasswordIdDesc();

	PasswordStuff findByUser(User id);

}
