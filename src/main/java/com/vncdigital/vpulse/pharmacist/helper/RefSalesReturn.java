package com.vncdigital.vpulse.pharmacist.helper;

import org.springframework.stereotype.Component;

/*
 * Helper for sales return management
 */

@Component
public class RefSalesReturn {
	
	private String medicineName;

	private long mrp;
	
	private String batchNo;
	
	private String expDate;
	
	private float discount;
	
	private long quantity;

	private float amount;
	
	private float gst;

	public String getMedicineName() {
		return medicineName;
	}

	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}

	public long getMrp() {
		return mrp;
	}

	public void setMrp(long mrp) {
		this.mrp = mrp;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public float getDiscount() {
		return discount;
	}

	public void setDiscount(float discount) {
		this.discount = discount;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public float getGst() {
		return gst;
	}

	public void setGst(float gst) {
		this.gst = gst;
	}

	public String getExpDate() {
		return expDate;
	}

	public void setExpDate(String expDate) {
		this.expDate = expDate;
	}
	

	
	
	
}
