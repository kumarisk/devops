package com.vncdigital.vpulse.user.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.user.model.PasswordStuff;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.repository.PasswordStuffRepository;
import com.vncdigital.vpulse.user.service.PasswordStuffService;

@Service
public class PasswordStuffServiceImpl implements PasswordStuffService
{
	@Autowired
	PasswordStuffRepository repo;
	
	public PasswordStuff save(PasswordStuff pw)
	{
		return repo.save(pw);
	}

	@Override
	public long count()
	{
		return repo.count();
	}

	@Override
	public String findFirstByOrderByPasswordIdDesc() 
	{
		PasswordStuff passwordStuffLast=repo.findFirstByOrderByPasswordIdDesc();
		String passwordStuffNext=null;
		if(passwordStuffLast==null)
		{
			 passwordStuffNext="UBP00001";
		}
		else
		{
		String passwordStuffLastId=passwordStuffLast.getPasswordId();
		int passwordStuffIntId=Integer.parseInt(passwordStuffLastId.substring(3));
		passwordStuffIntId+=1;
		passwordStuffNext="UBP"+String.format("%05d", passwordStuffIntId);
		}
	
		return passwordStuffNext;
	}
	
	public PasswordStuff findByUser(User id)
	{
		return repo.findByUser(id);
	}
	

	

}
