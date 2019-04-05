package com.vncdigital.vpulse.finalBilling.dto;

import com.vncdigital.vpulse.user.model.User;

public class FinalBillingDto {
	
private long billId;
	
	private String billNo;
	
	private String umrNo;
	
	private String regNo;
	
	private String name;
	
	private float totalAmount;
	
	private float cashAmount;
	
	private float chequeAmount;
	
	private float returnAmount;
	
	
	private float dueAmount;
	
	private float discAmount;
	
	private float finalAmountPaid;
	
	private String billType;
	
	private String paymentType;
	

	private User finalBillUser;


	public String getPaymentType() {
		return paymentType;
	}


	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}


	public long getBillId() {
		return billId;
	}


	public void setBillId(long billId) {
		this.billId = billId;
	}


	public String getBillNo() {
		return billNo;
	}


	public void setBillNo(String billNo) {
		this.billNo = billNo;
	}


	public String getUmrNo() {
		return umrNo;
	}


	public void setUmrNo(String umrNo) {
		this.umrNo = umrNo;
	}


	public String getRegNo() {
		return regNo;
	}


	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}


	public float getTotalAmount() {
		return totalAmount;
	}


	public void setTotalAmount(float totalAmount) {
		this.totalAmount = totalAmount;
	}


	public float getCashAmount() {
		return cashAmount;
	}


	public void setCashAmount(float cashAmount) {
		this.cashAmount = cashAmount;
	}


	public float getChequeAmount() {
		return chequeAmount;
	}


	public void setChequeAmount(float chequeAmount) {
		this.chequeAmount = chequeAmount;
	}


	public float getDueAmount() {
		return dueAmount;
	}


	public void setDueAmount(float dueAmount) {
		this.dueAmount = dueAmount;
	}


	public float getDiscAmount() {
		return discAmount;
	}


	public void setDiscAmount(float discAmount) {
		this.discAmount = discAmount;
	}


	public float getFinalAmountPaid() {
		return finalAmountPaid;
	}


	public void setFinalAmountPaid(float finalAmountPaid) {
		this.finalAmountPaid = finalAmountPaid;
	}


	public String getBillType() {
		return billType;
	}


	public void setBillType(String billType) {
		this.billType = billType;
	}


	public User getFinalBillUser() {
		return finalBillUser;
	}


	public void setFinalBillUser(User finalBillUser) {
		this.finalBillUser = finalBillUser;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public float getReturnAmount() {
		return returnAmount;
	}


	public void setReturnAmount(float returnAmount) {
		this.returnAmount = returnAmount;
	}

	

	
}