package com.vncdigital.vpulse.patient.dto;

import java.util.List;

import com.vncdigital.vpulse.patient.model.PatientDetails;


public class MarketingQuestionsDTO {
	private Long qId;
	
	private String question;

	private List<PatientDetails> vPatientDetails;

	public Long getqId() {
		return qId;
	}

	public void setqId(Long qId) {
		this.qId = qId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public List<PatientDetails> getvPatientDetails() {
		return vPatientDetails;
	}

	public void setvPatientDetails(List<PatientDetails> vPatientDetails) {
		this.vPatientDetails = vPatientDetails;
	}

	

	
}
