package com.vncdigital.vpulse.user.service;

import java.util.List;

import com.vncdigital.vpulse.user.model.Role;


public interface RoleService 
{

	public Role save(Role pw);
	
	public Role findByRoleName(String name);
	
	public Iterable<Role> findAll();
	

}
