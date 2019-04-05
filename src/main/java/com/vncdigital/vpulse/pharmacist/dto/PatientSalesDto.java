package com.vncdigital.vpulse.pharmacist.dto;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.pharmacist.model.Location;
import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.SalesRefund;
import com.vncdigital.vpulse.pharmacist.model.TaxDetails;
import com.vncdigital.vpulse.user.model.User;

public class PatientSalesDto {

	private String billNo;
	
	private String umr;

	private String salesBillNo;
	
	private String name;
	
	private long mobileNo;
	
	private Timestamp updatedDate;
	
	private String updatedBy;
	
	
	private float amount;
	
	private Timestamp billDate;
	
	private long quantity;
	
	private String expireDate;
	
	private String paid;
	
	private String medicineName;
	
	private String paymentType;

	private float mrp;
	
	
	private String soldBy;

	private String batchNo;
	
	private String shopName;

	private float gst;
	
	private float discount;

	
	private String amtInWords;

	private MedicineDetails patientSalesMedicineDetails;

	private TaxDetails patientSalesTaxDetails;

	private PatientRegistration patientSalesPatientRegistration;

	private Location patientSaleslocation;

	private User patientSalesUser;
	
	
	public MedicineDetails getPatientSalesMedicineDetails() {
		return patientSalesMedicineDetails;
	}

	public void setPatientSalesMedicineDetails(MedicineDetails patientSalesMedicineDetails) {
		this.patientSalesMedicineDetails = patientSalesMedicineDetails;
	}

	public TaxDetails getPatientSalesTaxDetails() {
		return patientSalesTaxDetails;
	}

	public void setPatientSalesTaxDetails(TaxDetails patientSalesTaxDetails) {
		this.patientSalesTaxDetails = patientSalesTaxDetails;
	}

	
	public String getBillNo() {
		return billNo;
	}

	public void setBillNo(String billNo) {
		this.billNo = billNo;
	}

	public long getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(long mobileNo) {
		this.mobileNo = mobileNo;
	}

	public Timestamp getBillDate() {
		return billDate;
	}

	public void setBillDate(Timestamp billDate) {
		this.billDate = billDate;
	}

	public String getMedicineName() {
		return medicineName;
	}

	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}


	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	
	
	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public float getMrp() {
		return mrp;
	}

	public void setMrp(float mrp) {
		this.mrp = mrp;
	}

	public float getGst() {
		return gst;
	}

	public void setGst(float gst) {
		this.gst = gst;
	}

	public float getDiscount() {
		return discount;
	}

	public void setDiscount(float discount) {
		this.discount = discount;
	}

	public String getAmtInWords() {
		return amtInWords;
	}

	public void setAmtInWords(String amtInWords) {
		this.amtInWords = amtInWords;
	}

	

	public PatientRegistration getPatientSalesPatientRegistration() {
		return patientSalesPatientRegistration;
	}

	public void setPatientSalesPatientRegistration(PatientRegistration patientSalesPatientRegistration) {
		this.patientSalesPatientRegistration = patientSalesPatientRegistration;
	}

	public Location getPatientSaleslocation() {
		return patientSaleslocation;
	}

	public void setPatientSaleslocation(Location patientSaleslocation) {
		this.patientSaleslocation = patientSaleslocation;
	}

	public User getPatientSalesUser() {
		return patientSalesUser;
	}

	public void setPatientSalesUser(User patientSalesUser) {
		this.patientSalesUser = patientSalesUser;
	}

	

	public String getUmr() {
		return umr;
	}

	public void setUmr(String umr) {
		this.umr = umr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	

	

	public String getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}

	public String getSoldBy() {
		return soldBy;
	}

	public void setSoldBy(String soldBy) {
		this.soldBy = soldBy;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getSalesBillNo() {
		return salesBillNo;
	}

	public void setSalesBillNo(String salesBillNo) {
		this.salesBillNo = salesBillNo;
	}

	public String getPaid() {
		return paid;
	}

	public void setPaid(String paid) {
		this.paid = paid;
	}

	public Timestamp getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Timestamp updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	

	
}
