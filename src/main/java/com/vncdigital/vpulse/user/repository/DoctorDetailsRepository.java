package com.vncdigital.vpulse.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.user.model.DoctorDetails;

@Repository
public interface DoctorDetailsRepository extends CrudRepository<DoctorDetails,String>
{
	DoctorDetails findFirstByOrderByDoctorIdDesc();
	
	List<DoctorDetails> findBySpecilization(String specialization);
	
	DoctorDetails findByDrRegistrationo(String regNo);

}
