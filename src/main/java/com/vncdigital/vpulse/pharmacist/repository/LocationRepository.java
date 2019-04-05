package com.vncdigital.vpulse.pharmacist.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.pharmacist.model.Location;
@Repository
public interface LocationRepository extends CrudRepository<Location,String> 
{
	
	Location findByLocationName(String name);

}
