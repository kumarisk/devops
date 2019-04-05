package com.vncdigital.vpulse.ambulance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.ambulance.model.AmbulanceServices;

@Repository
public interface AmbulanceServicesRepository extends CrudRepository<AmbulanceServices, Long>{
	
	
	
	
		@Query(value="select * from mygit.v_ambulance_service_d where status=1",nativeQuery=true)
		List<AmbulanceServices> getAmbulanceStatus();
		
		
		AmbulanceServices findByAmbulanceNO(String ambulanceNo);


}
