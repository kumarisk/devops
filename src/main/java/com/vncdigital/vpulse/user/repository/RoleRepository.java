package com.vncdigital.vpulse.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.user.model.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role,Long>{

	Role findByRoleName(String name);
	
	Iterable<Role> findAll();
	
}
