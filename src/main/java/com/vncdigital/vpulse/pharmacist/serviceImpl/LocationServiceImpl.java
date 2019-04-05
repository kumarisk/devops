package com.vncdigital.vpulse.pharmacist.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.pharmacist.model.Location;
import com.vncdigital.vpulse.pharmacist.repository.LocationRepository;
import com.vncdigital.vpulse.pharmacist.service.LocationService;
@Service
public class LocationServiceImpl implements LocationService 
{
	@Autowired
	LocationRepository locationRepository;
	

	public Location findByLocationName(String name)
	{
		return locationRepository.findByLocationName(name);
	}


}
