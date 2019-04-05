package com.vncdigital.vpulse.pharmacist.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.PatientSales;
@Repository
public interface PatientSalesRepository extends CrudRepository<PatientSales,String> 
{
	PatientSales findFirstByOrderByBillNoDesc();
	
	PatientSales save(PatientSales patientSales);
	
	@Query(value="select * from mygit.v_patient_sales_f where sales_bill_no=:billno and medicine_name=:name and batch_no=:batch",nativeQuery=true)
	PatientSales findOneBill(@Param("billno") String billno,@Param("name") String name,@Param("batch") String batch);
	
	 PatientSales findBySalesBillNoAndPatientSalesMedicineDetails(String billNo, MedicineDetails patientSalesMedicineDetails);


}
