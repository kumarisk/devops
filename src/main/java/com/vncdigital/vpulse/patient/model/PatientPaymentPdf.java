package com.vncdigital.vpulse.patient.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Component
@Entity
public class PatientPaymentPdf {
	
		@Id
		@JsonIgnore
	    private String pid;

		@JsonIgnore
	    private String fileName;

	    private String fileuri;

	    @Lob
	    @JsonIgnore
	    private byte[] data;

	    public PatientPaymentPdf() {
			super();
		}

		public PatientPaymentPdf( String fileName, String fileuri, byte[] data) {
			this.fileName = fileName;
			this.fileuri = fileuri;
			this.data = data;
		}
		

	    public String getPid() {
			return pid;
		}

		public void setPid(String pid) {
			this.pid = pid;
		}

		public String getFileName() {
	        return fileName;
	    }

	    public void setFileName(String fileName) {
	        this.fileName = fileName;
	    }
	    
	    public String getFileuri() {
			return fileuri;
		}

		public void setFileuri(String fileuri) {
			this.fileuri = fileuri;
		}

		public byte[] getData() {
	        return data;
	    }

	    public void setData(byte[] data) {
	        this.data = data;
	    }

}
