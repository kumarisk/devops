package com.vncdigital.vpulse.patient.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

@Repository
public interface PatientPaymentRepository extends CrudRepository<PatientPayment,Long>
{
PatientPayment findFirstByOrderByBillNoDesc();
	
	List<PatientPayment> findAll();
	
	@Query(value="SELECT * FROM mygit.v_patient_payment_f where reg_id=:regId",nativeQuery=true)
	List<PatientPayment> findByPatientRegistrationId(@Param("regId") String regId);
	
	List<PatientPayment> findByModeOfPaymantAndPatientRegistration(String name,PatientRegistration reg);
	
	Set<PatientPayment> findByPatientRegistration(PatientRegistration reg);
	
	PatientPayment save(PatientPayment patientPayment);
	
	@Query(value="SELECT * FROM mygit.v_patient_payment_f where reg_id=:regId and paid=:status",nativeQuery=true)
	List<PatientPayment> findByPatientRegistration(@Param("regId") String regId,@Param("status") String status);
	
	@Query(value="select * from mygit.v_patient_payment_f where reg_id=:regId and type_of_charge=:typeOfCharge",nativeQuery=true)
	PatientPayment findPatientByRegFee(@Param("regId") String regId,@Param("typeOfCharge") String typeOfCharge);
	
	@Query(value="select * from mygit.v_patient_payment_f where reg_id=:regId and mode_of_payment='due'",nativeQuery=true)
	List<PatientPayment>findDueBill(@Param("regId") String regId);
	
	@Query(value="select * from mygit.v_patient_payment_f where inserted_date>=:fromDate AND inserted_date<=:toDate AND raised_by_id=:uId and type_of_charge='ADVANCE'",nativeQuery=true)
	List<PatientPayment> findUserWiseIpOpDetailed(@Param("fromDate") Object fromDate, @Param("toDate") Object toDate, @Param("uId") String uId);
	
	@Query(value="SELECT * FROM mygit.v_patient_payment_f where raised_by_id=:createdBy and reg_id=:regId and inserted_date>=:fromDate AND inserted_date<=:toDate and type_of_charge='ADVANCE'",nativeQuery=true)
	List<PatientPayment> getPaticularPatientForScroll(@Param("createdBy") String createdBy,@Param("regId") String regId,@Param("fromDate") Object fromDate,@Param("toDate") Object toDate);

	// For whatsapp msg
	@Query(value="SELECT * FROM mygit.v_patient_payment_f WHERE reg_id=:regId and inserted_date like %:date%",nativeQuery=true)
	PatientPayment findByOpSumPatientRegistration(@Param("regId") String regId,@Param("date") String date);
	// For whatsapp msg
	@Query(value="SELECT * FROM mygit.v_patient_payment_f WHERE reg_id=:regId and inserted_date like %:date%",nativeQuery=true)
	PatientPayment findByIpSumPatientRegistration(@Param("regId") String regId,@Param("date") String date);
	// For whatsapp msg
	@Query(value="SELECT * FROM mygit.v_patient_payment_f WHERE reg_id=:regId and inserted_date like %:date% ",nativeQuery=true)
	PatientPayment findByOtherSumPatientRegistration(@Param("regId") String regId,@Param("date") String date);
}
