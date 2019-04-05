package com.vncdigital.vpulse.laboratory.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.laboratory.model.MasterCheckupService;


@Repository
public interface MasterCheckupServiceRepository extends CrudRepository<MasterCheckupService, String> {
	
	MasterCheckupService findFirstByOrderByMasterCheckupIdDesc();
	
	List<MasterCheckupService> findAll();
	
	List<MasterCheckupService> findByCheckupId(String checkupId);

}