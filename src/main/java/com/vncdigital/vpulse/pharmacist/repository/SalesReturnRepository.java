package com.vncdigital.vpulse.pharmacist.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.pharmacist.model.SalesReturn;
@Repository
public interface SalesReturnRepository extends CrudRepository<SalesReturn,String> 
{
	SalesReturn findFirstByOrderBySaleNoDesc();
	
	List<SalesReturn> findByMasterSaleNo(String id);
	
	List<SalesReturn> findByBillNoAndMedicineName(String billNo,String medName);
	

	@Query(value="select * from mygit.v_sales_return where date>=:fromdate  AND date<=:todate AND raised_by=:soldBy",nativeQuery=true)
	List<SalesReturn> findTheUserWiseDetails1(@Param("fromdate") Object fromdate,@Param("todate") Object todate,@Param("soldBy") Object soldBy);

	@Query(value="select * from mygit.v_sales_return where date>=:fromdate  AND date<=:todate",nativeQuery=true)
	List<SalesReturn> findTheUserWiseDetails(@Param("fromdate") Timestamp fromdate,@Param("todate") Timestamp todate);

	@Query(value="select * from mygit.v_sales_return where date>=:fromdate  AND date <= :todate",nativeQuery=true)
	List<SalesReturn> findTheReturnList(@Param("fromdate") String fromdate,@Param("todate") String todate);
	
	@Query(value="select * from mygit.v_sales_return where bill_no=:bill1",nativeQuery=true)
	List<SalesReturn> getByBill(@Param("bill1") String bill1);
	
	@Query(value="select * from  mygit.v_sales_return where date>=:fromdate  AND date <= :todate AND raised_by=:soldBy ",nativeQuery=true)
	List<SalesReturn> findTheUserWiseShiftReturns(@Param("fromdate") Object fromdate,@Param("todate") Object todate,@Param("soldBy") Object soldBy);

	//List<SalesReturn> findByName(String name);

	@Query(value="select * from mygit.v_sales_return where medicine_name=:medName",nativeQuery=true)
	List<SalesReturn> findByName(@Param("medName") String medName);
	
	@Query(value="select * from mygit.v_sales_return where date like %:returnDate%",nativeQuery=true)
	List<SalesReturn> getBillReturnDate(@Param("returnDate") String returnDate);
	
	 @Query(value="select * from mygit.v_sales_return where date>=:fromDate AND date<=:toDate AND medicine_name=:medName",nativeQuery=true)
		List<SalesReturn> getStockDetails(@Param("fromDate") Object fromDate,@Param("toDate") Object toDate,@Param("medName") Object medName);
	
}
