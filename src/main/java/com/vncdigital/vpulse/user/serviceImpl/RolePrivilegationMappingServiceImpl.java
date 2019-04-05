package com.vncdigital.vpulse.user.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.user.model.Role;
import com.vncdigital.vpulse.user.model.RolePrivilegationMapping;
import com.vncdigital.vpulse.user.repository.RolePrivilegationMappingRepository;
import com.vncdigital.vpulse.user.service.RolePrivilegationMappingService;

@Service
public class RolePrivilegationMappingServiceImpl implements RolePrivilegationMappingService
{
	@Autowired
	RolePrivilegationMappingRepository repo;
	
	public RolePrivilegationMapping save(RolePrivilegationMapping pw)
	{
		return repo.save(pw);
	}

	

	@Override
	public List<RolePrivilegationMapping> findByRole(List<Role> id) {
		
		return repo.findByRole(id);
	}

}
