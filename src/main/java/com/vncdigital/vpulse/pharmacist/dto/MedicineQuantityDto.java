package com.vncdigital.vpulse.pharmacist.dto;

import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;

public class MedicineQuantityDto {

	private long inventoryId;
	
	private float totalQuantity;
	
	
	private long sold;
	
	
	private float balance;
	
	private String medName;
	
	private MedicineDetails medicineId;

	public long getInventoryId() {
		return inventoryId;
	}

	public void setInventoryId(long inventoryId) {
		this.inventoryId = inventoryId;
	}

	

	public long getSold() {
		return sold;
	}

	public void setSold(long sold) {
		this.sold = sold;
	}

	
	public float getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(float totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public float getBalance() {
		return balance;
	}

	public void setBalance(float balance) {
		this.balance = balance;
	}

	public MedicineDetails getMedicineId() {
		return medicineId;
	}

	public void setMedicineId(MedicineDetails medicineId) {
		this.medicineId = medicineId;
	}

	public String getMedName() {
		return medName;
	}

	public void setMedName(String medName) {
		this.medName = medName;
	}
	
	
}

