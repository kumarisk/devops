package com.vncdigital.vpulse.laboratory.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.laboratory.model.NotesPdf;
import com.vncdigital.vpulse.laboratory.repository.NotesPdfRepository;
import com.vncdigital.vpulse.laboratory.service.NotesPdfService;

@Service
public class NotesPdfServiceImpl implements NotesPdfService
{
	public static Logger Logger=LoggerFactory.getLogger(NotesPdfServiceImpl.class);
	
	
	
	@Autowired
	NotesPdfRepository notesPdfRepository;
	
	
	@Override
	public String getNextLabId() {
		NotesPdf notesPdf=notesPdfRepository.findFirstByOrderByNidDesc();
		String nextId=null;
		if(notesPdf==null)
		{
			nextId="NPDF0000001";
		}
		else
		{
			int nextIntId=Integer.parseInt(notesPdf.getNid().substring(4));
			nextIntId+=1;
			nextId="NPDF"+String.format("%07d", nextIntId);
		}
		return nextId;
	}
	
	public NotesPdf findByNid(String id)
	{
		return notesPdfRepository.findByNid(id);
	}
	
	public NotesPdf findByRegId(String id)
	{
		return notesPdfRepository.findByRegId(id);
	}

}
