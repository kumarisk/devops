package com.vncdigital.vpulse.bed.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.bed.model.RoomBookingDetails;
import com.vncdigital.vpulse.bed.model.RoomDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;

@Repository
public interface RoomBookingDetailsRepository extends CrudRepository<RoomBookingDetails, String>{


	RoomBookingDetails findFirstByOrderByBookingIdDesc();
	
	@Query(value="select * from mygit.v_room_booking_details_f where bed_id=:id and status=1",nativeQuery=true)
	RoomBookingDetails getroomStatus(@Param("id") String id);
	
	RoomBookingDetails findByPatientRegistrationBooking(PatientRegistration patientRegistration);
	
	RoomBookingDetails findByPatientRegistrationBookingAndStatus(PatientRegistration patientRegistration,int status);
	
}
