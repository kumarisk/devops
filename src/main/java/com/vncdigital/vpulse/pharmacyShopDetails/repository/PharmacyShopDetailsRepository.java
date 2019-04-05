package com.vncdigital.vpulse.pharmacyShopDetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.pharmacyShopDetails.model.PharmacyShopDetails;

@Repository
public interface PharmacyShopDetailsRepository extends CrudRepository<PharmacyShopDetails, String>{

	PharmacyShopDetails findByShopLocation(String shopLocation);
}