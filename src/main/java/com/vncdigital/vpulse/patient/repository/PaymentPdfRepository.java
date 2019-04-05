package com.vncdigital.vpulse.patient.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.patient.model.PatientPaymentPdf;


@Repository
public interface PaymentPdfRepository extends CrudRepository<PatientPaymentPdf,String>
{
	PatientPaymentPdf findFirstByOrderByPidDesc();
	
	PatientPaymentPdf findByPid(String id);
	
	@Query(value="SELECT * FROM mygit.patient_payment_pdf where file_name like %:regId%",nativeQuery=true)
	List<PatientPaymentPdf> getAllReport(@Param("regId") String regId);
	
	@Query(value="SELECT * FROM mygit.patient_payment_pdf where file_name like %:regId%",nativeQuery=true)
	PatientPaymentPdf getBlankPdf(@Param("regId") String regId);

}
