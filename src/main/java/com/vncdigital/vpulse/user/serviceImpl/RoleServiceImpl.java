package com.vncdigital.vpulse.user.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.user.model.Role;
import com.vncdigital.vpulse.user.repository.RoleRepository;
import com.vncdigital.vpulse.user.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService 
{
	@Autowired
	RoleRepository repo;
	
	public Role save(Role pw)
	{
		return repo.save(pw);
	}
	
	public Role findByRoleName(String name)
	{
		return repo.findByRoleName(name);
		
	}

	@Override
	public Iterable<Role> findAll() {
		return repo.findAll();
	}
	


}
