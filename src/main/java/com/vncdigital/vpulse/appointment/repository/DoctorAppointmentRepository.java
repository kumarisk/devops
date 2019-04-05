package com.vncdigital.vpulse.appointment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vncdigital.vpulse.appointment.model.DoctorAppointment;

@Repository
public interface DoctorAppointmentRepository extends CrudRepository<DoctorAppointment, String>{
	
	DoctorAppointment findFirstByOrderByAppointmentIdDesc();

	List<DoctorAppointment> findAll();

	@Query(value = "select * from mygit.v_doctor_appointment_f where doctor_name=:doctorName  AND shift=:shift AND appointment_date like %:appointmentDate% ", nativeQuery = true)
	List<DoctorAppointment> getAllAppiontments(@Param("doctorName") String doctorName, @Param("shift") String shift,
			@Param("appointmentDate") String appointmentDate);
	
	
	@Query(value = "select * from mygit.v_doctor_appointment_f ad, mygit.v_slottimings_d sd where doctor_name=:doctorName  AND shift=:shift AND appointment_date like %:appointmentDate% AND ad.slot_id=sd.slot_id AND ad.shift=sd.slot", nativeQuery = true)
	List<DoctorAppointment> getNewAppointments(@Param("doctorName") String doctorName, @Param("shift") String shift,
			@Param("appointmentDate") String appointmentDate);
}