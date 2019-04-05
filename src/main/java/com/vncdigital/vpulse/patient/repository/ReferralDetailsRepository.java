package com.vncdigital.vpulse.patient.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.patient.model.ReferralDetails;



@Repository
public interface ReferralDetailsRepository extends CrudRepository<ReferralDetails,Long>
{
	List<ReferralDetails> findAll();
	
	@Query(value="SELECT * FROM mygit.v_referral_details_d where source=:name and ref_name is not null",nativeQuery=true)
	List<ReferralDetails> findBySource(@Param("name") String name);
	
	@Query(value="SELECT * from mygit.v_referral_details_d GROUP BY source;",nativeQuery=true)
	List<ReferralDetails> findDistinct();
	
	ReferralDetails findBySourceAndRefName(String source,String refName);
	

}
