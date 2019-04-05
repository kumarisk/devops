package com.vncdigital.vpulse.user.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.user.model.Previlege;

@Repository
public interface PrevilegeRepository extends CrudRepository<Previlege,Long>
{
	Previlege findByName(String name);

}
