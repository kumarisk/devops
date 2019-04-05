package com.vncdigital.vpulse.pharmacist.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.MedicineQuantity;
import com.vncdigital.vpulse.pharmacist.repository.MedicineQuantityRepository;
import com.vncdigital.vpulse.pharmacist.service.MedicineQuantityService;

@Service
public class MedicineQuantityServiceImpl implements MedicineQuantityService{
	
	@Autowired
	MedicineQuantityRepository medicineQuantityRepository ;

	@Override
	public MedicineQuantity findByMedicineDetails(MedicineDetails medicineDetails) {
		
		return medicineQuantityRepository.findByMedicineDetails(medicineDetails);
	}
	
	

}