package com.vncdigital.vpulse.pharmacist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.MedicineProcurement;
@Repository
public interface MedicineProcurementRepository extends CrudRepository<MedicineProcurement,String> 
{
	MedicineProcurement findFirstByOrderByMasterProcurementIdDesc();
	
	@Query(value="select * from mygit.v_medicine_procurement_d where item_name=:medicineName",nativeQuery=true)
	List<MedicineProcurement> findByName(@Param("medicineName") String medicineName);
	
	List<MedicineProcurement> findAll();
	
	@Query(value="SELECT * FROM mygit.v_medicine_procurement_d where invoice_no=:inNo and draft not like '%YES%' or draft is null",nativeQuery=true)
	List<MedicineProcurement> findByInvoiceNo(@Param("inNo") String inNo);
	
	List<MedicineProcurement> findByInvoiceNoAndDraft(String inNo,String draft);
	
	MedicineProcurement findByInvoiceNoAndDraftAndMedicineProcurmentMedicineDetails(String inNo,String draft,MedicineDetails medicineDetails);
	
	
	@Query(value="SELECT * FROM mygit.v_medicine_procurement_d where date_of_procurement>=:fromDay and date_of_procurement<=:toDay and draft not like '%YES%' or draft is null order by master_procurement_id desc",nativeQuery=true)
	List<MedicineProcurement> findAllByOrderByMasterProcurementIdDesc(@Param("fromDay") String fromDay,@Param("toDay") String toDay);
	
	MedicineProcurement findByMasterProcurementId(String id);
	
	List<MedicineProcurement> findByProcurementId(String id);
	
	@Query(value="select * from mygit.v_medicine_procurement_d where date_of_procurement>=:fromDate AND date_of_procurement<=:toDate AND status='Approved'",nativeQuery=true)
	List<MedicineProcurement> getItemExpiry(@Param("fromDate") Object fromDate,@Param("toDate") Object toDate);
	
	@Query(value="Select * from mygit.v_medicine_procurement_d where procurement_id=:procId and item_name=:medName",nativeQuery=true)
	MedicineProcurement findMasterProcurementId(@Param("procId") String procId,@Param("medName") String medName);
	
	@Query(value="SELECT * FROM mygit.v_medicine_procurement_d where procurement_id like %:procId%",nativeQuery=true)
	List<MedicineProcurement> getGroupBy(@Param("procId") String procId);
	
	@Query(value="select * from mygit.v_medicine_procurement_d where master_procurement_id=:mId and procurement_id=:pId ",nativeQuery=true)
	MedicineProcurement getOneProcurement(@Param("mId") String mId,@Param("pId") String pId);
	
	@Query(value="select * from mygit.v_medicine_procurement_d where v_medicine_procurement_d.status='Approved'",nativeQuery=true)
	List<MedicineProcurement> findAllApproved();
	
	@Query(value="select * from mygit.v_medicine_procurement_d where v_medicine_procurement_d.status='Approved' and medicine_id=:medId and str_to_date(exp_date, '%Y-%m-%d')>DATE(NOW()+INTERVAL 30 DAY)",nativeQuery=true)
	List<MedicineProcurement> findOneApproved(@Param("medId") MedicineDetails medId);
	
	@Query(value="select sum(amount) from mygit.v_medicine_procurement_d where v_medicine_procurement_d.procurement_id=:id",nativeQuery=true)
	long findSumOfCost(@Param("id") String id);
	
	@Query(value="select distinct master_procurement_id from mygit.v_medicine_procurement_d where master_procurement_id=:id",nativeQuery=true)
	MedicineProcurement	findDistinctProcurementId(@Param("id") String id);
	
	List<MedicineProcurement> findByMedicineProcurmentMedicineDetails(MedicineDetails medicineDetailsInfo);
	
	@Query(value="select * from mygit.v_medicine_procurement_d where v_medicine_procurement_d.batch=:batch and v_medicine_procurement_d.medicine_id=:medicine and status='Approved'",nativeQuery=true)
	List<MedicineProcurement> findByBatchAndMedicine(@Param("batch") String batch,@Param("medicine") String medicine);
	
	List<MedicineProcurement> findByItemName(String name);
	
	List<MedicineProcurement> findByItemNameAndStatus(String name,String status);
	
	
	@Query(value="select * from mygit.v_medicine_procurement_d where date_of_procurement like %:salesDate%",nativeQuery=true)
	List<MedicineProcurement> getProcurementDate(@Param("salesDate") String salesDate);
	

	@Query(value="select * from mygit.v_medicine_procurement_d where date_of_procurement>=:fromDate AND date_of_procurement<=:toDate and status='Approved' order by item_name",nativeQuery=true)
	List<MedicineProcurement> getStockSummary(@Param("fromDate") Object fromDate,@Param("toDate") Object toDate);
	
    @Query(value="select * from mygit.v_medicine_procurement_d where item_name=:medName",nativeQuery=true)
	List<MedicineProcurement> getMedicineName(@Param("medName") String medName);
	


	

}
