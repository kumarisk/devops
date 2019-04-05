package com.vncdigital.vpulse.pharmacist.model;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.user.model.User;

@Entity
@Table(name = "v_patient_sales_f")
public class PatientSales {

	@Id
	@Column(name = "bill_no")
	private String billNo;
	
	@Column(name = "umr")
	private String umr;

	@Column(name = "sales_bill_no")
	private String salesBillNo;
	
	@Column(name = "name")
	private String name;
	
	@Column(name="mobile_no")
	private long mobileNo;
	
	@Column(name = "amount")
	private float amount;
	
	@Column(name = "bill_date")
	private Timestamp billDate;
	
	@Column(name = "updatdd_date")
	private Timestamp updatedDate;
	
	@Column(name = "updatdd_by")
	private String updatedBy;
	
	@Column(name = "quantity")
	private long quantity;
	
	@Column(name = "expire_date")
	private String expireDate;
	
	@Column(name = "paid")
	private String paid;
	
	@Column(name="medicine_Name")
	private String medicineName;
	
	@Column(name="payment_type")
	private String paymentType;

	@Column(name="mrp")
	private float mrp;
	
	
	@Column(name = "sold_by")
	private String soldBy;

	@Column(name = "batch_no")
	private String batchNo;
	
	@Column(name = "shop_name")
	private String shopName;

	@Column(name="gst")
	private float gst;
	
	@Column(name="discount")
	private float discount;

	
	@Column(name="amt_in_words")
	private String amtInWords;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "medicine_id")
	private MedicineDetails patientSalesMedicineDetails;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "tax_id")
	private TaxDetails patientSalesTaxDetails;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "p_reg_id")
	private PatientRegistration patientSalesPatientRegistration;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "location_id")
	private Location patientSaleslocation;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "user_Id")
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