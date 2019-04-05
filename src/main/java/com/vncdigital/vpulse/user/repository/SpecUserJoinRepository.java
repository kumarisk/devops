package com.vncdigital.vpulse.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.user.model.DoctorSpecialization;
import com.vncdigital.vpulse.user.model.SpecUserJoin;
import com.vncdigital.vpulse.user.model.User;

@Repository
public interface SpecUserJoinRepository extends CrudRepository<SpecUserJoin,Long> 
{
	
	List<SpecUserJoin> findByUserSpec(User user);
	List<SpecUserJoin> findByDocSpec(DoctorSpecialization spec);
	SpecUserJoin findByDocSpecAndUserSpec(DoctorSpecialization spec,User user);

}