package com.vncdigital.vpulse.pharmacist.service;

import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.MedicineQuantity;

public interface MedicineQuantityService {
	
	public MedicineQuantity findByMedicineDetails(MedicineDetails medicineDetails);

}
