package com.vncdigital.vpulse.ambulance.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.ambulance.model.AmbulancePatientDetails;

@Repository
public interface AmbulancePatientDetailsRepository extends CrudRepository<AmbulancePatientDetails, String>{

	
	AmbulancePatientDetails findFirstByOrderByPatAmbulanceIdDesc();
	
	
	public AmbulancePatientDetails findByPatAmbulanceId(String id);
	
	
	public List<AmbulancePatientDetails> findAll();
	
	}

