package com.vncdigital.vpulse.user.service;

import java.util.List;

import com.vncdigital.vpulse.user.model.Role;
import com.vncdigital.vpulse.user.model.RolePrivilegationMapping;


public interface RolePrivilegationMappingService 
{
	public RolePrivilegationMapping save(RolePrivilegationMapping pw);
	public List<RolePrivilegationMapping> findByRole(List<Role> id);
	
	
	
}
