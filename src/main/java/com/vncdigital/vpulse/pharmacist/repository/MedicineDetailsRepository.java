package com.vncdigital.vpulse.pharmacist.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
@Repository
public interface MedicineDetailsRepository extends CrudRepository<MedicineDetails,String> 
{
	MedicineDetails findFirstByOrderByMedicineIdDesc();
	
	MedicineDetails findByMedicineId(String id);
	
	MedicineDetails findByName(String name);
	
	List<MedicineDetails> findAll();

}
