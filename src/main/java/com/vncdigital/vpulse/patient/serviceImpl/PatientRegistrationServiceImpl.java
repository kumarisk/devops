package com.vncdigital.vpulse.patient.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.repository.PatientRegistrationRepository;
import com.vncdigital.vpulse.patient.service.PatientRegistrationService;
import com.vncdigital.vpulse.user.model.User;


@Service
public class PatientRegistrationServiceImpl implements PatientRegistrationService 
{
	@Autowired
	PatientRegistrationRepository repo;
	
	@Override
	public PatientRegistration save(PatientRegistration patientRegistration)
	{
		return repo.save(patientRegistration);
	}
	
	@Override
	public Optional<PatientRegistration> findById(Long id)
	{
		return repo.findById(id);
	}
	
	@Override
	public void delte(Long id)
	{
		 repo.deleteById(id);
	}
	
	@Override
	public PatientRegistration update(PatientRegistration patientRegistration)
	{
		return repo.save(patientRegistration);
	}

	@Override
	public String getNextRegId() {
		PatientRegistration pr= repo.findFirstByOrderByRegIdDesc();
		String nextReg=null;
		if(pr==null)
		{
			nextReg="PR00000001";
		}
		else
		{
			String lastUmr=pr.getRegId();
			
			int regIntId=Integer.parseInt(lastUmr.substring(3));
			regIntId+=1;
			nextReg="PR"+String.format("%08d",regIntId );
		}
		return nextReg;
	}
	
	public List<PatientRegistration> findByVuserD(User user)
	{
		return repo.findByVuserD(user);
	}
	
	public List<PatientRegistration> findByPatientDetails(PatientDetails patientDetails)
	{
		return repo.findByPatientDetails(patientDetails);
	}
	
	public PatientRegistration findLatestReg(Long uid)
	{
		return repo.findLatestReg(uid);
	}
	
	public PatientRegistration findByRegId(String id)
	{
		return repo.findByRegId(id);
	}
	
	public List<PatientRegistration> findAll()
	{
		return repo.findAll();
	}
	
	public List<PatientRegistration> findByPType(String pType) {
		
		return repo.findByPType(pType);
	}

	@Override
	public List<PatientRegistration> findPatient(String time,String type) {
		return repo.findPatient(time,type);
	}

	@Override
	public List<PatientRegistration> findOutPatient(String time, String type) {
		return repo.findOutPatient(time, type);
	}

	@Override
	public List<PatientRegistration> expectOutPatient() {
		return repo.expectOutPatient();
	}
	
	public PatientRegistration patientAlredyExists(long pId,String date)
	{
		return repo.patientAlredyExists(pId, date);
	}
	
	public 	List<PatientRegistration> findOnlyInpatient()
	{
		return repo.findOnlyInpatient();
	}

	@Override
	public List<PatientRegistration> expectOutPatientTwoDays(String twoDayBack, String today) {
		return repo.expectOutPatientTwoDays(twoDayBack, today);
	}

	@Override
	public List<PatientRegistration> onlyOutPatientTwoDays(String twoDayBack, String today) {
		return repo.onlyOutPatientTwoDays(twoDayBack, today);
	}

	
}
