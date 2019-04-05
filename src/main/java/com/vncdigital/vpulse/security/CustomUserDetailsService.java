package com.vncdigital.vpulse.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.service.UserPrincipal;
import com.vncdigital.vpulse.user.service.UserService;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	
    @Autowired
    UserService userService;
    
    @Autowired
    UserServiceImpl userServiceImpl;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {
    	User user = null;
    			

		user=userServiceImpl.findByUserName(usernameOrEmail);
    /*
    	if(user==null)
    	{
    		user=userService.findOneByUserId(usernameOrEmail);
    		System.out.println(usernameOrEmail);
    	}
    */	
    	return UserPrincipal.create(user);
    }
}
