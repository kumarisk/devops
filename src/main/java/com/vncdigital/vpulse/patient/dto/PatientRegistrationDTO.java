package com.vncdigital.vpulse.patient.dto;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vncdigital.vpulse.ambulance.model.AmbulancePatientDetails;
import com.vncdigital.vpulse.bed.model.RoomBookingDetails;
import com.vncdigital.vpulse.bill.model.ChargeBill;
import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.model.NotesDetails;
import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientTypes;
import com.vncdigital.vpulse.pharmacist.model.PatientSales;
import com.vncdigital.vpulse.pharmacist.model.Sales;
import com.vncdigital.vpulse.pharmacist.model.SalesRefund;
import com.vncdigital.vpulse.pharmacist.model.SalesReturn;
import com.vncdigital.vpulse.user.model.User;



public class PatientRegistrationDTO {

	private String regId;
	
	private String appNo;
	
	private Timestamp regDate;
	
	private String regValidity;
	
	private String pType;
	
	private long advanceAmount;
	
	private String createdBy;
	
	private long estimationAmount;
	
	private transient List<Map<String,String>> multimode;
	
	private transient String rePatientType;
	
	private transient Map<String,String> refName;
	
	private transient String referenceNumber;
	
	private transient String reConsultant;
	
	private Timestamp createdAt;
	
	private Timestamp deletedAt;
	
	private Timestamp dateOfJoining;
	
	private List<ChargeBill> chargeBill;
	
	private User vuserD;
	
	private PatientDetails patientDetails;
	
	private String procedureName;
	
	
	private List<Sales> sales;
	
	private List<RoomBookingDetails> roomBookingDetails ;
	
	private List<SalesReturn> salesReturns;
	
	private List<SalesRefund> salesRefunds;
	
	 
	private List<LaboratoryRegistration> laboratoryRegistration;
	
	
	private List<PatientSales> patientSales;
	
	private PatientTypes patientType;
	
	private List<NotesDetails> notesDetails;
	
	private List<AmbulancePatientDetails> ambulancePatientDetail;
	
	private Set<PatientPayment> patientPayment;

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}
	
	

	public String getAppNo() {
		return appNo;
	}

	
	public void setAppNo(String appNo) {
		this.appNo = appNo;
	}

	public User getVuserD() {
		return vuserD;
	}

	public void setVuserD(User vuserD) {
		this.vuserD = vuserD;
	}

	

	

	public Timestamp getRegDate() {
		return regDate;
	}

	public void setRegDate(Timestamp regDate) {
		this.regDate = regDate;
	}

	

	public PatientTypes getPatientType() {
		return patientType;
	}

	public void setPatientType(PatientTypes patientType) {
		this.patientType = patientType;
	}

	public String getRegValidity() {
		return regValidity;
	}

	public void setRegValidity(String regValidity) {
		this.regValidity = regValidity;
	}

	public String getpType() {
		return pType;
	}

	public void setpType(String pType) {
		this.pType = pType;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Timestamp deletedAt) {
		this.deletedAt = deletedAt;
	}

	public Timestamp getDateOfJoining() {
		return dateOfJoining;
	}

	public void setDateOfJoining(Timestamp dateOfJoining) {
		this.dateOfJoining = dateOfJoining;
	}

	
	public PatientTypes getpatientType() {
		return patientType;
	}

	public void setpatientType(PatientTypes patientType) {
		this.patientType = patientType;
	}

	public Set<PatientPayment> getPatientPayment() {
		return patientPayment;
	}

	public void setPatientPayment(Set<PatientPayment> patientPayment) {
		this.patientPayment = patientPayment;
	}

	public String getRePatientType() {
		return rePatientType;
	}

	public void setRePatientType(String rePatientType) {
		this.rePatientType = rePatientType;
	}

	public String getReConsultant() {
		return reConsultant;
	}

	public void setReConsultant(String reConsultant) {
		this.reConsultant = reConsultant;
	}

	public List<Sales> getSales() {
		return sales;
	}

	public void setSales(List<Sales> sales) {
		this.sales = sales;
	}

	public List<SalesReturn> getSalesReturns() {
		return salesReturns;
	}

	public void setSalesReturns(List<SalesReturn> salesReturns) {
		this.salesReturns = salesReturns;
	}

	public List<SalesRefund> getSalesRefunds() {
		return salesRefunds;
	}

	public void setSalesRefunds(List<SalesRefund> salesRefunds) {
		this.salesRefunds = salesRefunds;
	}

	public PatientDetails getPatientDetails() {
		return patientDetails;
	}

	public void setPatientDetails(PatientDetails patientDetails) {
		this.patientDetails = patientDetails;
	}

	public List<PatientSales> getPatientSales() {
		return patientSales;
	}

	public void setPatientSales(List<PatientSales> patientSales) {
		this.patientSales = patientSales;
	}

	public List<LaboratoryRegistration> getLaboratoryRegistration() {
		return laboratoryRegistration;
	}

	public void setLaboratoryRegistration(List<LaboratoryRegistration> laboratoryRegistration) {
		this.laboratoryRegistration = laboratoryRegistration;
	}

	public List<ChargeBill> getChargeBill() {
		return chargeBill;
	}

	public void setChargeBill(List<ChargeBill> chargeBill) {
		this.chargeBill = chargeBill;
	}

	public List<NotesDetails> getNotesDetails() {
		return notesDetails;
	}

	public void setNotesDetails(List<NotesDetails> notesDetails) {
		this.notesDetails = notesDetails;
	}

	public List<RoomBookingDetails> getRoomBookingDetails() {
		return roomBookingDetails;
	}

	public void setRoomBookingDetails(List<RoomBookingDetails> roomBookingDetails) {
		this.roomBookingDetails = roomBookingDetails;
	}

	public long getAdvanceAmount() {
		return advanceAmount;
	}

	public void setAdvanceAmount(long advanceAmount) {
		this.advanceAmount = advanceAmount;
	}

	public long getEstimationAmount() {
		return estimationAmount;
	}

	public void setEstimationAmount(long estimationAmount) {
		this.estimationAmount = estimationAmount;
	}

	public List<AmbulancePatientDetails> getAmbulancePatientDetail() {
		return ambulancePatientDetail;
	}

	public void setAmbulancePatientDetail(List<AmbulancePatientDetails> ambulancePatientDetail) {
		this.ambulancePatientDetail = ambulancePatientDetail;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Map<String, String> getRefName() {
		return refName;
	}

	public void setRefName(Map<String, String> refName) {
		this.refName = refName;
	}

	public List<Map<String, String>> getMultimode() {
		return multimode;
	}

	public void setMultimode(List<Map<String, String>> multimode) {
		this.multimode = multimode;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	
	
	
	
	
	
	
}
