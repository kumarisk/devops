package com.vncdigital.vpulse.laboratory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.laboratory.model.ServicePdf;

@Repository
public interface ServicePdfRepository extends CrudRepository<ServicePdf,String>{

	ServicePdf findFirstByOrderBySidDesc();
	
	ServicePdf findBySid(String id);
	
	List<ServicePdf> findByRegId(String id);
	
	@Query(value="SELECT * FROM mygit.service_pdf where reg_id=:regId and file_name=:name order by sid desc",nativeQuery=true)
	List<ServicePdf> findByRegAndMeasureName(@Param("regId") String regId,@Param("name") String name);
}
