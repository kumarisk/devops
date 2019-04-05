package com.vncdigital.vpulse.pharmacist.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.pharmacist.model.Sales;

@Repository
public interface SalesRepository extends CrudRepository<Sales,String> 
{
	Sales findFirstByOrderBySaleNoDesc();
	
	List<Sales> findByBillNo(String id);
	
	List<Sales> findByPaymentType(String  paymentType );
	
	@Query(value="select * from mygit.v_sales_f where bill_no=:billno and medicine_name=:name and batch_no=:batch",nativeQuery=true)
	Sales findOneBill(@Param("billno") String billno,@Param("name") String name,@Param("batch") String batch);
	
	List<Sales> findByPatientRegistration(PatientRegistration patientRegistration);
	
	Sales findBySaleNo(String saleId);
	
	// For whatsApp Message
	@Query(value="SELECT * FROM mygit.v_patient_payment_f WHERE inserted_date like %:date%",nativeQuery=true)
	List<Sales> findPerDayPharmacySalesValue(@Param("date") String date);
	
	@Query(value="select * from mygit.v_sales_f where bill_date BETWEEN CURDATE() - INTERVAL 1 DAY AND CURDATE()",nativeQuery=true)
	List<Sales> getPreviousDaySales();
	
	@Query(value="select * from mygit.v_sales_f where bill_date>=:fromdate  AND bill_date <= :todate AND sold_by=:soldBy and payment_type not like '%Due%'",nativeQuery=true)
	List<Sales> findTheUserWiseShiftAndNotDue(@Param("fromdate") Object fromdate,@Param("todate") Object todate,@Param("soldBy") Object soldBy);
	
	
	
	@Query(value="select * from mygit.v_sales_f where updated_by >=:fromdate  AND updated_by <= :todate AND updated_by=:soldBy ",nativeQuery=true)
	List<Sales> findTheUserWiseShift(@Param("fromdate") Object fromdate,@Param("todate") Object todate,@Param("soldBy") Object soldBy);

	@Query(value="select * from mygit.v_sales_f where bill_date>=:fromdate  AND bill_date <= :todate",nativeQuery=true)
	List<Sales> findTheUserWiseDetails(@Param("fromdate") Object fromdate,@Param("todate") Object todate);
	
	@Query(value="select * from mygit.v_sales_f where bill_date>=:fromdate  AND bill_date <= :todate AND medicine_name=:medName AND batch_no=:batch",nativeQuery=true)
	List<Sales> findExpiryDetails(@Param("fromdate") Object fromdate, @Param("todate") Object todate, @Param("medName") String medName,@Param("batch") String batch);

	List<Sales> findByPaymentTypeAndUmr(String paymentType,String umrNo);

	@Query(value="select * from mygit.v_sales_f where bill_date like %:salesDate%",nativeQuery=true)
	List<Sales> getBillDate(@Param("salesDate") String salesDate);
	

	@Query(value="select * from mygit.v_sales_f where medicine_name=:medName",nativeQuery=true)
	List<Sales> findByName(@Param("medName") String medName);

	List<Sales> findByMedicineName(String medName);

	@Query(value="select * from mygit.v_sales_f where batch_no=:batch and medicine_id=:medicine",nativeQuery=true)
	List<Sales> findByBatchAndMedicine(@Param("batch") String batch,@Param("medicine") String medicine);
	
	 @Query(value="select * from mygit.v_sales_f where bill_date>=:fromdate  AND bill_date <= :todate AND medicine_name=:medName",nativeQuery=true)
	List<Sales> findStockDetails(@Param("fromdate") Object fromdate, @Param("todate") Object todate, @Param("medName") String medName);
	
	 List<Sales> findByBillNoAndPaymentType(String billno,String payType);
	 
	// Find due Bill
	List<Sales> findByPatientRegistrationAndPaymentType(PatientRegistration patientRegistration,String paymentType);
	
	
	 List<Sales> findByPaymentTypeAndPatientRegistration(String payment,PatientRegistration reg);
	//List<Sales> findByName(String name);
	 
	 
	 @Query(value="select * from mygit.v_sales_f where bill_date>=:fromdate  AND bill_date <= :todate AND sold_by=:soldBy and payment_type not like '%Due%' and payment_type='Cash+Card'",nativeQuery=true)
		List<Sales> findByPaymentType(@Param("fromdate") Object fromdate,@Param("todate") Object todate,@Param("soldBy") Object soldBy);
	
}
