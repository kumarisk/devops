package com.vncdigital.vpulse.pharmacist.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.pharmacist.model.TaxDetails;
@Repository
public interface TaxDetailsRepository extends CrudRepository<TaxDetails,String> 
{
	

}
