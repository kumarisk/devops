package com.vncdigital.vpulse.appointment.service;

import java.util.List;

import com.vncdigital.vpulse.appointment.model.SlotTiming;

public interface SlotTimingService {
	public List<SlotTiming> findBySlot(String slot);
	public SlotTiming findByFromTimeAndToTime(String fromTime,String toTime);

}