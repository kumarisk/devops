package com.vncdigital.vpulse.laboratory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

@Repository
public interface LaboratoryRegistrationRepository extends CrudRepository<LaboratoryRegistration,String>{

	LaboratoryRegistration findFirstByOrderByLabRegIdDesc();
	
	LaboratoryRegistration findFirstByOrderByBillNoDesc();
	
	List<LaboratoryRegistration> findAll();
	
	LaboratoryRegistration findByLabRegId(String id);
	
	
//	List<LaboratoryRegistration> findByInvoiceNo(String id);
	List<LaboratoryRegistration> findByBillNo(String id);
	

	
	// For whatsApp Msg
	@Query(value = "SELECT * FROM mygit.v_laboratory_registration_f where reffered_by_id=:userId and entered_date like %:date%", nativeQuery = true)
	List<LaboratoryRegistration> findLabCountServices(@Param("userId") String userId,@Param("date") String date);
	// For whatsApp Msg
	@Query(value="SELECT * FROM mygit.v_laboratory_registration_f WHERE reg_id=:regId and entered_date like %:date%",nativeQuery=true)
	LaboratoryRegistration findByLabRegistration(@Param("regId") String refferedById,@Param("date") String date);

	@Query(value="select * from mygit.v_laboratory_registration_f where p_reg_id=:lRegId and invoice_no=:invNo",nativeQuery=true)
	List<LaboratoryRegistration> findByLabRegIdsAndInvoiceNo(@Param("lRegId") String lRegId,@Param("invNo") String invoiceNo);
	
	@Query(value="select * from mygit.v_laboratory_registration_f where p_reg_id=:lRegId",nativeQuery=true)
	List<LaboratoryRegistration> findByLabRegIds(@Param("lRegId") String lRegId);
	
	@Query(value = "SELECT * FROM mygit.v_laboratory_registration_f where reffered_by_id=:userId ", nativeQuery = true)
	List<LaboratoryRegistration> findLabCount(@Param("userId") String userId);
	
	@Query(value="select * from mygit.v_laboratory_registration_f where p_reg_id=:regId and invoice_no=:invoice",nativeQuery=true)
	List<LaboratoryRegistration> findBill(@Param("regId") String regId,@Param("invoice") String invoice);
	
	List<LaboratoryRegistration> findByLaboratoryPatientRegistration(PatientRegistration patientRegistration);
	
	@Query(value="\n" + 
			"select * from mygit.v_laboratory_registration_f where entered_date>=:fromDate and entered_date<=:toDate and entered_by=:userName and service_id in (select service_id from mygit.v_lab_services_d where service_type='Lab')",nativeQuery=true)
	List<LaboratoryRegistration> findUserWiseIpOpDetailedCheck(@Param("fromDate")Object fromDate,@Param("toDate") Object toDate,@Param("userName") String userName);
	
	// Used for scroll for grouping patients
	@Query(value="select * from mygit.v_laboratory_registration_f where entered_date>=:fromDate and entered_date<=:toDate AND entered_by=:userName and p_reg_id=:regId",nativeQuery=true)
	List<LaboratoryRegistration> particularPatientRecordScroll(@Param("fromDate")Object fromDate,@Param("toDate") Object toDate,@Param("userName") String userName,@Param("regId") String regId);
		
	
	@Query(value="select * from mygit.v_laboratory_registration_f where entered_date>=:fromDate and entered_date<=:toDate and entered_by=:userName and service_id in(select service_id from mygit.v_lab_services_d where (service_type='Other' or service_type='ward charges' or service_type='equipment charges') and patient_type='OUTPATIENT')",nativeQuery=true)
	List<LaboratoryRegistration> findUserWiseIpOpDetailedOther(@Param("fromDate")Object fromDate,@Param("toDate") Object toDate,@Param("userName") String userName);

	

	@Query(value="select * from mygit.v_laboratory_registration_f where entered_date>=:fromDate and entered_date<=:toDate and entered_by=:userName and service_id in(select service_id from mygit.v_lab_services_d where (service_type='Other' or service_type='ward charges' or service_type='equipment charges') and patient_type='INPATIENT')",nativeQuery=true)
	List<LaboratoryRegistration> findUserWiseIpOpDetailedOtherIp(@Param("fromDate")Object fromDate,@Param("toDate") Object toDate,@Param("userName") String userName);

	
	@Query(value="select * from mygit.v_laboratory_registration_f where entered_date>=:fromDate and entered_date<=:toDate and entered_by=:userName and p_reg_id=:lRegId and service_id in(select service_id from mygit.v_lab_services_d where (service_type='Other' or service_type='ward charges' or service_type='equipment charges') and patient_type='OUTPATIENT')",nativeQuery=true)
	List<LaboratoryRegistration> findUserWiseIpOpDetailedOtherRegId(@Param("fromDate")Object fromDate,@Param("toDate") Object toDate,@Param("userName") String userName,@Param("lRegId") String lRegId);

	
	
	@Query(value="select * from mygit.v_laboratory_registration_f where entered_date>=:fromDate and entered_date<=:toDate and entered_by=:userName and p_reg_id=:lRegId and service_id in(select service_id from mygit.v_lab_services_d where (service_type='Other' or service_type='ward charges' or service_type='equipment charges') and patient_type='INPATIENT')",nativeQuery=true)
	List<LaboratoryRegistration> findUserWiseIpOpDetailedOtherRegIdIp(@Param("fromDate")Object fromDate,@Param("toDate") Object toDate,@Param("userName") String userName,@Param("lRegId") String lRegId);
	

	
	@Query(value="select * from mygit.v_laboratory_registration_f where entered_date>=:fromDate and entered_date<=:toDate AND entered_by=:userName AND paid=:status",nativeQuery=true)
	List<LaboratoryRegistration> findUserWiseIpOpDetailed(@Param("fromDate")Object fromDate,@Param("toDate") Object toDate,@Param("userName") String userName,@Param("status") String status);
	
	@Query(value="select * from mygit.v_laboratory_registration_f where payment_type=:paymentType",nativeQuery=true)
	List<LaboratoryRegistration> getDueRegistrationlist(@Param("paymentType") String paymentType);
	
	
	List<LaboratoryRegistration> findByInvoiceNo(String id);
	
	List<LaboratoryRegistration> findByPaymentTypeAndLaboratoryPatientRegistration(String payment,PatientRegistration reg);
	
	@Query(value="select * from mygit.v_laboratory_registration_f where p_reg_id=:regId and payment_type=:paymentType",nativeQuery=true)
	List<LaboratoryRegistration> getDueRegistration(@Param("regId") String regId,@Param("paymentType") String paymentType);
}
