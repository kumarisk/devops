package com.vncdigital.vpulse.appointment.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.appointment.model.SlotTiming;
import com.vncdigital.vpulse.appointment.repository.SlotTimingRepository;
import com.vncdigital.vpulse.appointment.service.SlotTimingService;

@Service
public class SlotTimingServiceImpl implements SlotTimingService{

	@Autowired
	SlotTimingRepository slotTimingRepository;
	
	@Override
	public List<SlotTiming> findBySlot(String slot) {
		
		return slotTimingRepository.findBySlot(slot);
	}

	@Override
	public SlotTiming findByFromTimeAndToTime(String fromTime, String toTime) {
		
		return slotTimingRepository.findByFromTimeAndToTime(fromTime, toTime);
	}

}