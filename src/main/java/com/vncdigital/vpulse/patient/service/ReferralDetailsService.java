package com.vncdigital.vpulse.patient.service;

import java.util.List;
import java.util.Optional;

import com.vncdigital.vpulse.patient.model.ReferralDetails;



public interface ReferralDetailsService 
{	
	public ReferralDetails save(ReferralDetails referralDetails);
	
	public Optional<ReferralDetails> findById(Long id);
	
	public void delte(Long id);
	
	public ReferralDetails update(ReferralDetails referralDetails);
	
	public List<ReferralDetails> findAll();
	
	public List<ReferralDetails> findBySource(String name);
	
	public List<ReferralDetails> findDistinct();
	
	public ReferralDetails findBySourceAndRefName(String source,String refName);
	
}
