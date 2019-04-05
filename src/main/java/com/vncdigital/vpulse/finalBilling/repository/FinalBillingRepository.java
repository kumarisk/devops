package com.vncdigital.vpulse.finalBilling.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.finalBilling.model.FinalBilling;

@Repository
public interface FinalBillingRepository extends CrudRepository<FinalBilling, Long>{

	FinalBilling save(FinalBilling finalBilling);
	
	FinalBilling findByBillTypeAndBillNoAndRegNo(String billtype,String billNo,String regNo); //Finding sales
	
}