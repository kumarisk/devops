package com.vncdigital.vpulse.security.helper;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.vncdigital.vpulse.user.model.PasswordStuff;

public class PasswordEncodeUtil {
	
	
	public static void encryptedPasswordStuff(PasswordEncoder passwordEncoder,PasswordStuff passwordStuff) {
		passwordStuff.setPassword(passwordEncoder.encode(passwordStuff.getPassword()));
		passwordStuff.setConfirmPassword(passwordEncoder.encode(passwordStuff.getConfirmPassword()));
		//passwordStuff.setTxnPassword(passwordEncoder.encode(passwordStuff.getTxnPassword()));
		//passwordStuff.setConfirmTxnPassword(passwordEncoder.encode(passwordStuff.getConfirmTxnPassword()));
		
		
	}
}
