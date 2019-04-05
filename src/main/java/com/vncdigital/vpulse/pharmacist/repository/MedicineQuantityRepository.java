package com.vncdigital.vpulse.pharmacist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.MedicineQuantity;

public interface MedicineQuantityRepository extends CrudRepository<MedicineQuantity, Long>{
	
	public MedicineQuantity findByMedicineDetails(MedicineDetails medicineDetails);
	
	@Query(value="select * from mygit.v_medicine_quantity where balance<=100 order by balance, med_name",nativeQuery=true)
	public List<MedicineQuantity> findAll();

}
