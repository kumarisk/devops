package com.vncdigital.vpulse.due.helper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DueHelper {
	
	private String mode;
	
	private String dueFor;
	
	private  List<Map<String,String>> multimode; 
	
	private float amount;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}



	public String getDueFor() {
		return dueFor;
	}

	public void setDueFor(String dueFor) {
		this.dueFor = dueFor;
	}

	public List<Map<String, String>> getMultimode() {
		return multimode;
	}

	public void setMultimode(List<Map<String, String>> multimode) {
		this.multimode = multimode;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	
	
	

}