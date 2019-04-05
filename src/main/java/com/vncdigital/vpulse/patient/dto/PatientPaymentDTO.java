package com.vncdigital.vpulse.patient.dto;

import java.security.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

public class PatientPaymentDTO 
{
	private long paymentId;
	
	private long amount;
	
	private Timestamp insertedDate;
	
	private Timestamp modifiedDate;
	
	private String typeOfCharge;
	
	private String modeOfPaymant;
	
	private String referenceNumber;
	
	private String billNo;
	
	private String paid;
	
	private String raisedById;
	
	private String description;
	
	private PatientRegistration patientRegistration ;
	
	public long getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(long paymentId) {
		this.paymentId = paymentId;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public Timestamp getInsertedDate() {
		return insertedDate;
	}

	public void setInsertedDate(Timestamp insertedDate) {
		this.insertedDate = insertedDate;
	}

	public Timestamp getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Timestamp modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getTypeOfCharge() {
		return typeOfCharge;
	}

	public void setTypeOfCharge(String typeOfCharge) {
		this.typeOfCharge = typeOfCharge;
	}

	public String getModeOfPaymant() {
		return modeOfPaymant;
	}

	public void setModeOfPaymant(String modeOfPaymant) {
		this.modeOfPaymant = modeOfPaymant;
	}

	public PatientRegistration getPatientRegistration() {
		return patientRegistration;
	}

	public void setPatientRegistration(PatientRegistration patientRegistration) {
		this.patientRegistration = patientRegistration;
	}

	public String getPaid() {
		return paid;
	}

	public void setPaid(String paid) {
		this.paid = paid;
	}

	public String getRaisedById() {
		return raisedById;
	}

	public void setRaisedById(String raisedById) {
		this.raisedById = raisedById;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBillNo() {
		return billNo;
	}

	public void setBillNo(String billNo) {
		this.billNo = billNo;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}
	
	

}
