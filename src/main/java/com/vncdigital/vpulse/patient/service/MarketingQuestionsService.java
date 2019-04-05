package com.vncdigital.vpulse.patient.service;

import java.util.Optional;

import com.vncdigital.vpulse.patient.model.MarketingQuestions;




public interface MarketingQuestionsService 
{
	
	public MarketingQuestions save(MarketingQuestions marketingQuestions);
	
	public Optional<MarketingQuestions> findById(Long id);
	
	public void delte(Long id);
	
	public MarketingQuestions update(MarketingQuestions marketingQuestions);
	
	public MarketingQuestions findByQuestion(String name);
}
