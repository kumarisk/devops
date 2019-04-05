package com.vncdigital.vpulse.pharmacist.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.pharmacist.model.Vendors;
@Repository
public interface VendorsRepository extends CrudRepository<Vendors,String> 
{
	Vendors findFirstByOrderByVendorIdDesc();
	
	Vendors findByVendorName(String name);
	
	List<Vendors> findAll();
	
	Vendors findByVendorId(String id);
	
	List<Vendors> findAllByOrderByVendorIdDesc();

}
