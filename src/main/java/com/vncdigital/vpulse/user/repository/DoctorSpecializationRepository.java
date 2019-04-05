package com.vncdigital.vpulse.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.user.model.DoctorSpecialization;

@Repository
public interface DoctorSpecializationRepository extends CrudRepository<DoctorSpecialization, Long>{
	
	DoctorSpecialization findBySpecName(String name);
	
	List<DoctorSpecialization> findAll();

}
