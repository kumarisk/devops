package com.vncdigital.vpulse.user.service;

import java.util.List;

import com.vncdigital.vpulse.user.model.DoctorSpecialization;

public interface DoctorSpecializationService {
	
	DoctorSpecialization findBySpecName(String name);
	
	List<DoctorSpecialization> findAll();



}
