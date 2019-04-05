package com.vncdigital.vpulse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class VpulseApplication {

	public static Logger Logger=LoggerFactory.getLogger(VpulseApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(VpulseApplication.class, args);
		
		
	}
}
