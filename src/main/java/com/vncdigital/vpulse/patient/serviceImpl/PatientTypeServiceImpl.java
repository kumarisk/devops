package com.vncdigital.vpulse.patient.serviceImpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.patient.model.PatientTypes;
import com.vncdigital.vpulse.patient.repository.PatientTypeRepository;
import com.vncdigital.vpulse.patient.service.PatientTypeService;


@Service
public class PatientTypeServiceImpl implements PatientTypeService 
{
	@Autowired
	PatientTypeRepository repo;
	
	public PatientTypes save(PatientTypes patientTypes)
	{
		return repo.save(patientTypes);
	}
	
	public Optional<PatientTypes> findById(Long id)
	{
		return repo.findById(id);
	}
	
	public void delte(Long id)
	{
		 repo.deleteById(id);
	}
	
	public PatientTypes update(PatientTypes patientTypes)
	{
		return repo.save(patientTypes);
	}
	
	public PatientTypes findByPType(String name)
	{
		return repo.findByPType(name);
	}

	@Override
	public Iterable<PatientTypes> findAll() 
	{
		return repo.findAll();
	}
	
}
