package com.vncdigital.vpulse.laboratory.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.laboratory.model.NurseLaboratory;


@Repository
public interface NurseLaboratoryRepository extends CrudRepository<NurseLaboratory,String>{

	
	
}
