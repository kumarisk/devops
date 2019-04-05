package com.vncdigital.vpulse.laboratory.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.laboratory.model.RaisePharmacy;


@Repository
public interface RaisePharmacyRepository extends CrudRepository<RaisePharmacy,String>{

	
	
}
