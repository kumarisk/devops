package com.vncdigital.vpulse.user.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vncdigital.vpulse.ambulance.model.AmbulancePatientDetails;
import com.vncdigital.vpulse.ambulance.model.AmbulanceServices;
import com.vncdigital.vpulse.appointment.model.DoctorAppointment;
import com.vncdigital.vpulse.bill.model.ChargeBill;
import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.model.MasterCheckupService;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.pharmacist.model.Vendors;
import com.vncdigital.vpulse.pharmacist.model.VendorsInvoice;
import com.vncdigital.vpulse.voucher.model.Voucher;

@Entity
@Table(name = "v_user_d",uniqueConstraints = @UniqueConstraint(columnNames ="user_name"))
public class User implements Serializable{
	
	@Id
	@Column(name = "user_id")
	/*@GenericGenerator(name = "sequence_imei_id", strategy = "com.example.test.testingHMS.user.model.ImeiIdGenerator")
    @GeneratedValue(generator = "sequence_imei_id")*/
	private String userId;
	
	@NotNull
	@Size(min=2,max=50)
	@Column(name = "first_name")
	private String firstName;
	
	@Size(max=50)
	@Column(name = "middle_name")
	private String middleName;
	
	@Size(min=1,max=50)
	@Column(name = "last_name")
	private String lastName;
	
	@NotNull
	@Column(name = "personal_contactnumber")
	private long personalContactNumber;
	
	@Column(name = "work_contactnumber")
	private long workContactNumber;
	
	@Size(min=6,max=50)
	@Column(name = "user_name",unique=true)
	private String userName;
	
	@Column(name = "pin")
	private String pin;
	
	@Size(min=6,max=50)
	@Column(name = "email")
	private String email;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "department")
	private String department;
	
	@Column(name = "timezone")
	private String timeZone;
	
	@Column(name = "deleted_at")
	private Timestamp deletedAt;
	
	@Column(name = "created_at")
	private Timestamp createdAt;
	
	@Column(name = "ref_name")
	private String refName;
	
	@Column(name = "address")
	private String location;
	
	@Size(min=0,max=50)
	@Column(name = "description")
	private String description;
	
	@Column(name="role_name")
	private String role;
	
	@JsonIgnore
	private transient String roleName;
	
	@JsonIgnore
	private transient String uId;
	
	private transient List<Map<String,String>> doctorSpecialization;
	
	
	
	@JsonIgnore
	@OneToMany(mappedBy="masterCheckUpUser",cascade=CascadeType.ALL)
	private List<MasterCheckupService> masterCheckupService;
	
	
	//@JsonIgnore
	@OneToOne(mappedBy="doctorUser",cascade=CascadeType.ALL)
	private DoctorDetails doctorDetails;
	
	@JsonIgnore
	@OneToMany(mappedBy="appointmentUser",cascade=CascadeType.ALL)
	private List<DoctorAppointment> doctorAppointment;

	@OneToOne(mappedBy = "user",cascade=CascadeType.ALL)
	private PasswordStuff passwordStuff;
	
	@JsonIgnore
	@OneToMany(mappedBy="vuserD",cascade=CascadeType.ALL)
	private List<PatientRegistration> patientRegistrations;

	@JsonIgnore
	@OneToMany(mappedBy="userVoucher",cascade=CascadeType.ALL)
	private List<Voucher> voucher; 
	
	@JsonIgnore
	@OneToMany(mappedBy="vendorUser",cascade=CascadeType.ALL)
	private List<Vendors> vendors ;
	
	@JsonIgnore
	@OneToMany(mappedBy="userLaboratoryRegistration",cascade=CascadeType.ALL)
	private List<LaboratoryRegistration>  laboratoryRegistration;
	
	@JsonIgnore
	@OneToMany(mappedBy="userChargeBillId",cascade=CascadeType.ALL)
	private List<ChargeBill> chargeBill ;
	
	@JsonIgnore
	@OneToMany(mappedBy="ambulanceUser",cascade=CascadeType.ALL)
	private List<AmbulanceServices> ambulanceServices;
	
	@JsonIgnore
	@OneToMany(mappedBy="ambulancePatientUser",cascade=CascadeType.ALL)
	private List<AmbulancePatientDetails> ambulancePatientDetails;
	
	
	@JsonIgnore
	@OneToMany(mappedBy="vendorInvoiceUser",cascade=CascadeType.ALL)
	private List<VendorsInvoice> vendorsInvoice ;

	
	@JsonIgnore
	@ManyToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	@JoinColumn(name = "role_id")
	private Role userRole;
	
	@JsonIgnore
	@OneToMany(mappedBy="userSpec")
	private List<SpecUserJoin> SpecUserJoin;

	public User() {
	}


	public PasswordStuff getPasswordStuff() {
		return passwordStuff;
	}

	public void setPasswordStuff(PasswordStuff passwordStuff) {
		this.passwordStuff = passwordStuff;
	}


	public Role getUserRole() {
		return userRole;
	}


	public void setUserRole(Role userRole) {
		this.userRole = userRole;
	}


	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	

	
	public long getPersonalContactNumber() {
		return personalContactNumber;
	}


	public void setPersonalContactNumber(long personalContactNumber) {
		this.personalContactNumber = personalContactNumber;
	}


	public long getWorkContactNumber() {
		return workContactNumber;
	}


	public void setWorkContactNumber(long workContactNumber) {
		this.workContactNumber = workContactNumber;
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	

	

	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public Timestamp getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Timestamp deletedAt) {
		this.deletedAt = deletedAt;
	}


	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getuId() {
		return uId;
	}

	public void setuId(String uId) {
		this.uId = uId;
	}


	public String getMiddleName() {
		return middleName;
	}


	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}


	public List<PatientRegistration> getPatientRegistrations() {
		return patientRegistrations;
	}


	public void setPatientRegistrations(List<PatientRegistration> patientRegistrations) {
		this.patientRegistrations = patientRegistrations;
	}


	public String getRole() {
		return role;
	}


	public void setRole(String role) {
		this.role = role;
	}
	
	


	public List<Vendors> getVendors() {
		return vendors;
	}


	public void setVendors(List<Vendors> vendors) {
		this.vendors = vendors;
	}
	
	


	public List<VendorsInvoice> getVendorsInvoice() {
		return vendorsInvoice;
	}


	public void setVendorsInvoice(List<VendorsInvoice> vendorsInvoice) {
		this.vendorsInvoice = vendorsInvoice;
	}

	public List<LaboratoryRegistration> getLaboratoryRegistration() {
		return laboratoryRegistration;
	}


	public void setLaboratoryRegistration(List<LaboratoryRegistration> laboratoryRegistration) {
		this.laboratoryRegistration = laboratoryRegistration;
	}


	public DoctorDetails getDoctorDetails() {
		return doctorDetails;
	}


	public void setDoctorDetails(DoctorDetails doctorDetails) {
		this.doctorDetails = doctorDetails;
	}


	public List<ChargeBill> getChargeBill() {
		return chargeBill;
	}


	public void setChargeBill(List<ChargeBill> chargeBill) {
		this.chargeBill = chargeBill;
	}


	public String getDepartment() {
		return department;
	}


	public void setDepartment(String department) {
		this.department = department;
	}


	public List<Voucher> getVoucher() {
		return voucher;
	}


	public void setVoucher(List<Voucher> voucher) {
		this.voucher = voucher;
	}


	public List<AmbulanceServices> getAmbulanceServices() {
		return ambulanceServices;
	}


	public void setAmbulanceServices(List<AmbulanceServices> ambulanceServices) {
		this.ambulanceServices = ambulanceServices;
	}


	public List<AmbulancePatientDetails> getAmbulancePatientDetails() {
		return ambulancePatientDetails;
	}


	public void setAmbulancePatientDetails(List<AmbulancePatientDetails> ambulancePatientDetails) {
		this.ambulancePatientDetails = ambulancePatientDetails;
	}


	public List<DoctorAppointment> getDoctorAppointment() {
		return doctorAppointment;
	}


	public void setDoctorAppointment(List<DoctorAppointment> doctorAppointment) {
		this.doctorAppointment = doctorAppointment;
	}


	public List<SpecUserJoin> getSpecUserJoin() {
		return SpecUserJoin;
	}


	public void setSpecUserJoin(List<SpecUserJoin> specUserJoin) {
		SpecUserJoin = specUserJoin;
	}


	public List<Map<String, String>> getDoctorSpecialization() {
		return doctorSpecialization;
	}


	public void setDoctorSpecialization(List<Map<String, String>> doctorSpecialization) {
		this.doctorSpecialization = doctorSpecialization;
	}


	public List<MasterCheckupService> getMasterCheckupService() {
		return masterCheckupService;
	}


	public void setMasterCheckupService(List<MasterCheckupService> masterCheckupService) {
		this.masterCheckupService = masterCheckupService;
	}


	
	
	
	
	
	

	
	
	
	

}
