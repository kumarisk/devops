package com.vncdigital.vpulse.osp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.osp.model.OspService;

@Repository
public interface OspServiceRepository extends CrudRepository<OspService, String>{
	
OspService findFirstByOrderByMasterOspServiceIdDesc();


List<OspService> findByBillNo(String id);
List<OspService> findByPaymentType(String paymentType);
	
	//List<OspService> findByOspServiceId(String ospServiceId);
	
	@Query(value="select * from mygit.v_ospservice_f where osp_service_id=:ospServiceId",nativeQuery=true)
	List<OspService> findServices(@Param("ospServiceId") String ospServiceId);
	
	@Query(value="select * from mygit.v_ospservice_f where entered_date>=:fromDate AND entered_date<=:toDate AND user_id=:uId",nativeQuery=true)
	List<OspService> findByUserDetails(@Param("fromDate") Object fromDate,@Param("toDate") Object toDate,@Param("uId") String uId);

	
	List<OspService> findByOrderByMasterOspServiceIdDesc();
	
	@Query(value="select * from mygit.v_ospservice_f where entered_date>=:twoDayBack and  entered_date<=:today order by osp_service_id desc",nativeQuery=true)
	List<OspService> ospTwoDays(@Param("twoDayBack") String twoDayBack,@Param("today") String today);
}