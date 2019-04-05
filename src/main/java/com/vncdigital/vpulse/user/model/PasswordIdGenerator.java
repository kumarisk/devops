package com.vncdigital.vpulse.user.model;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class PasswordIdGenerator implements IdentifierGenerator {
	
	public static int counter;

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException 
	{
		counter+=1;
		String name="VNC_P";
		return name+"_"+counter;
	}
	
}