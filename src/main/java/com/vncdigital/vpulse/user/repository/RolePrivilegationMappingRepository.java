package com.vncdigital.vpulse.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.user.model.Role;
import com.vncdigital.vpulse.user.model.RolePrivilegationMapping;

@Repository
public interface RolePrivilegationMappingRepository extends CrudRepository<RolePrivilegationMapping,Long>{
	
	public List<RolePrivilegationMapping> findByRole(List<Role> id);
	
}
