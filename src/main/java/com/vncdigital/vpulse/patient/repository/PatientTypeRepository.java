package com.vncdigital.vpulse.patient.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.patient.model.PatientTypes;



@Repository
public interface PatientTypeRepository extends CrudRepository<PatientTypes,Long>
{
	
	PatientTypes findByPType(String name);

	Iterable<PatientTypes> findAll();
}
