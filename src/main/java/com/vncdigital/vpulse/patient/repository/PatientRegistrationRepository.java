package com.vncdigital.vpulse.patient.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.user.model.User;



@Repository
public interface PatientRegistrationRepository extends CrudRepository<PatientRegistration,Long>
{
	PatientRegistration findFirstByOrderByRegIdDesc();
	
	List<PatientRegistration> findByPatientDetails(PatientDetails patientDetails);
	
	//Inner join for patient Registration/details
	@Query(value="SELECT * FROM mygit.v_patient_registration_f "
			+ "INNER JOIN mygit.v_patient_details_d "
			+ "ON mygit.v_patient_registration_f.p_id=mygit.v_patient_details_d.patient_id "
			+ "where  mygit.v_patient_registration_f.p_type='INPATIENT' "
			+ "and mygit.v_patient_details_d.discharged not like '%Yes%';",nativeQuery=true)
	List<PatientRegistration> findOnlyInpatient();
	
	@Query(value="select * from mygit.v_patient_registration_f where year(reg_date)=:year and month(reg_date)=:month and user_id=:userId",nativeQuery=true)
	List<PatientRegistration> getPatientCountMonthWise(@Param("year") String year,@Param("month") String month,@Param("userId") String userId);
	
	@Query(value="select * from mygit.v_patient_registration_f where p_id=:pid",nativeQuery=true)
	List<PatientRegistration> getRegids(@Param("pid") Long pid );
	
	@Query(value="SELECT * FROM mygit.v_patient_registration_f where user_id=:userId " ,nativeQuery=true)
	List<PatientRegistration> findPatientCount(@Param("userId") String userId);
	
	@Query(value="select * from mygit.v_patient_registration_f where reg_date>=:fromDate AND reg_date<=:toDate",nativeQuery=true)
	List<PatientRegistration> findByDate(@Param("fromDate") Object fromDate, @Param("toDate") Object toDate);
	
	
	@Query(value="select * from mygit.v_patient_registration_f "
			+ "where reg_date = (SELECT MAX(reg_date) FROM mygit.v_patient_registration_f WHERE mygit.v_patient_registration_f.p_id = :pid) and mygit.v_patient_registration_f.p_id =:pid",nativeQuery=true)
	PatientRegistration findLatestReg(@Param("pid") Long pid);
	
	PatientRegistration findByRegId(String id);
	
	@Query(value="SELECT * FROM mygit.v_patient_registration_f where user_id=:userId and reg_date like %:date% and p_type=:pType" ,nativeQuery=true)
	List<PatientRegistration> findPatientListByConsultant(@Param("userId") String userId,@Param("date") String date,@Param("pType") String pType);
	
	List<PatientRegistration> findByPType( String pType);
	
	List<PatientRegistration> findAll();
	
	List<PatientRegistration> findByVuserD(User user);
	

	@Query(value="select * from mygit.v_patient_registration_f where reg_date>=:time and p_type=:type",nativeQuery=true)
	List<PatientRegistration> findPatient(@Param("time") String time,@Param("type") String type);
	
	@Query(value="select * from mygit.v_patient_registration_f where reg_date>=:time and p_type <> :type",nativeQuery=true)
	List<PatientRegistration> findOutPatient(@Param("time") String time,@Param("type") String type);
	
	@Query(value="select * from mygit.v_patient_registration_f where p_type not like '%OUTPATIENT%'",nativeQuery=true)
	List<PatientRegistration> expectOutPatient();
	
	@Query(value="select * from mygit.v_patient_registration_f where p_type not like '%OUTPATIENT%' and created_at>=:twoDayBack and created_at<=:today ",nativeQuery=true)
	List<PatientRegistration> expectOutPatientTwoDays(@Param("twoDayBack") String twoDayBack,@Param("today") String today);

	@Query(value="select * from mygit.v_patient_registration_f where p_type like '%OUTPATIENT%' and created_at>=:twoDayBack and created_at<=:today ",nativeQuery=true)
	List<PatientRegistration> onlyOutPatientTwoDays(@Param("twoDayBack") String twoDayBack,@Param("today") String today);

	
	
	@Query(value="SELECT * FROM mygit.v_patient_registration_f where p_id=:pId and reg_date like %:date%",nativeQuery=true)
	PatientRegistration patientAlredyExists(@Param("pId") long pId,@Param("date") String date);
	
	@Query(value="select * from mygit.v_patient_registration_f where reg_date>=:fromDate AND reg_date<=:toDate AND created_by=:uId",nativeQuery=true)
	List<PatientRegistration> findUserWiseIpOpDetailed(@Param("fromDate") Object fromDate, @Param("toDate") Object toDate, @Param("uId") String uId);
	
	@Query(value="select * from mygit.v_patient_registration_f where user_id=:userId",nativeQuery=true)
	List<PatientRegistration> findNewPatient(@Param("userId") String userId);
	
	
	// For whatsapp msg
	@Query(value="select * from mygit.v_patient_registration_f where reg_date like %:date% and p_type='OTHER' and user_id=:userId",nativeQuery=true)
	List<PatientRegistration> getPatientWiseOtherCount(@Param("userId")String userId,@Param("date") String date);
	// For whatsapp msg
    @Query(value="select * from mygit.v_patient_registration_f where reg_date like %:date%  and p_type='OUTPATIENT' and user_id=:userId",nativeQuery=true)
	List<PatientRegistration> getPatientWiseopCount(@Param("userId")String userId,@Param("date") String date);
    // For whatsapp msg
    @Query(value="select * from mygit.v_patient_registration_f where reg_date like %:date% and p_type='INPATIENT' and user_id=:userId",nativeQuery=true)
	List<PatientRegistration> getPatientWiseipCount(@Param("userId")String userId,@Param("date") String date);
    
    List<PatientRegistration> findByRegDateGreaterThanEqualAndRegDateLessThanEqual(Timestamp t,Timestamp o);
}
