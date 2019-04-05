package com.vncdigital.vpulse.user.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.user.model.PasswordStuff;
import com.vncdigital.vpulse.user.model.User;

@Repository
public interface PasswordStuffRepository extends CrudRepository<PasswordStuff,Long>{
	
	PasswordStuff findFirstByOrderByPasswordIdDesc();
	
	PasswordStuff findByUser(User id);
	

}
