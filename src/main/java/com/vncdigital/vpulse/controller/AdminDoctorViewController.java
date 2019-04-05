package com.vncdigital.vpulse.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.repository.LaboratoryRegistrationRepository;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.repository.PatientRegistrationRepository;
import com.vncdigital.vpulse.user.model.SpecUserJoin;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;

@CrossOrigin(origins = "*", maxAge = 36000)
@RestController
@RequestMapping("/v1/admin")

public class AdminDoctorViewController {
	
	public static Logger Logger=LoggerFactory.getLogger(AdminDoctorViewController.class);
	
	
	@Autowired
	PatientRegistrationRepository patientRegistrationRepository;
	
	@Autowired
	LaboratoryRegistrationRepository laboratoryRegistrationRepository;
	
	@Autowired
	UserServiceImpl userServiceImpl;

	@RequestMapping(value="/getlist")
	public List<Object> getDoctorLIst(){
		List<Object> list=new ArrayList<>();
		
		
		List<User> user=userServiceImpl.findByRole("DOCTOR");
		
		String specilization=null;
		String userName = null;
		String ufn = null;
		String umn = null;
		String uln = null;
		String joinDate=null;
		int labCount=0;
		int count=0;
		for(User userInfo:user){
			Map<String, String> map=new HashMap<>();
			
			List<SpecUserJoin> spec=userInfo.getSpecUserJoin();
			
			
			for(SpecUserJoin specInfo:spec){
				 specilization=specInfo.getDocSpec().getSpecName();
				 List<LaboratoryRegistration> laboratoryRegistrations=laboratoryRegistrationRepository.findLabCount(userInfo.getUserId());
					
					
					
					labCount=laboratoryRegistrations.size();


				
			}
			List<PatientRegistration> patientRegistration=patientRegistrationRepository.findPatientCount(userInfo.getUserId());
			
			count=patientRegistration.size();
			
			joinDate=userInfo.getDoctorDetails().getCreatedDate().toString().substring(0, 10);
			
			
			try {
				SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat toFormat = new SimpleDateFormat("dd-MMM-yyyy");
				joinDate = toFormat.format(fromFormat.parse(joinDate));
			} catch (Exception e) {
				Logger.error(e.getMessage());
			}

			
			
			if (userInfo.getFirstName() == null) {
				ufn = " ";
			} else {
				ufn = userInfo.getFirstName();
			}
			if (userInfo.getMiddleName() == null) {
				umn = "";
			} else {
				umn = userInfo.getMiddleName();
			}
			if (userInfo.getLastName() == null) {
				uln = " ";
			} else {
				uln = userInfo.getLastName();
			}
			if (umn.equalsIgnoreCase("")) {
				userName =userInfo.getFirstName() + " " + userInfo.getLastName();
			} else {
				userName =  userInfo.getFirstName() + " " + userInfo.getMiddleName() + " "
						+ userInfo.getLastName();
			}
			
			//List<DoctorSpecialization> spec=userInfo.getDoctorSpecialization();
			map.put("labCount", String.valueOf(labCount));
			map.put("userId", userInfo.getUserId());
			map.put("userName", userName);
			map.put("Department", userInfo.getDoctorDetails().getSpecilization());
			map.put("joinDate", joinDate);
			map.put("count",String.valueOf(count));
			map.put("specilization", specilization);
			list.add(map);
		}
		
		
		
		
		
		return list;
	}
	
	@RequestMapping(value="/getyears")
	public List<Object> getYears(){
		
		List<Object> list=new ArrayList<>();
		
		LocalDate today = LocalDate.now();    
	     LocalDate userday = LocalDate.of(2015, Month.JANUARY, 01); 
	     Period diff = Period.between(userday, today); 
	     System.out.println("\nDifference between "+ userday +" and "+ today +": " 
	     + diff.getYears() +" Year(s) and "+ diff.getMonths() +" Month()s\n");
	     
	     
	     int i=0;
	     for( i=0;i<=diff.getYears();i++){
	    	 Map<String, String> map=new HashMap<>();
	     map.put("years", String.valueOf(2015+i));
	     
	     list.add(map);
	     }
	     return list;
	}
	
@RequestMapping(value="/getmonthwise/{userId}",method=RequestMethod.POST)
	
	public List<Object> getPatientCountByMonth(@PathVariable("userId") String userId,@RequestBody Map<String, String> map){
		
		
		String userName = null;
		String ufn = null;
		String umn = null;
		String uln = null;

		List<Object> list=new ArrayList<>();
		
		User userInfo=userServiceImpl.findOneByUserId(userId);
		
		
		if (userInfo.getFirstName() == null) {
			ufn = " ";
		} else {
			ufn = userInfo.getFirstName();
		}
		if (userInfo.getMiddleName() == null) {
			umn = "";
		} else {
			umn = userInfo.getMiddleName();
		}
		if (userInfo.getLastName() == null) {
			uln = " ";
		} else {
			uln = userInfo.getLastName();
		}
		if (umn.equalsIgnoreCase("")) {
			userName = userInfo.getFirstName() + " " + userInfo.getLastName();
		} else {
			userName = userInfo.getFirstName() + " " + userInfo.getMiddleName() + " " + userInfo.getLastName();
		}

		
		
		
		String year=map.get("year");
		
		
		List<PatientRegistration> firstMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "01", userId);
		int janCount=firstMonth.size();
		
		Map<String, String> firstMap=new HashMap<>();
		firstMap.put("doctorName",userName );
		firstMap.put("year", year);
		firstMap.put("month", "Jan");
		firstMap.put("monthNo", "01");
		firstMap.put("count", String.valueOf(janCount));
		
		
		

		List<PatientRegistration> secondMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "02", userId);
		int febCount=secondMonth.size();
		
		Map<String, String> secondMap=new HashMap<>();
		secondMap.put("doctorName",userName );
		secondMap.put("year", year);
		secondMap.put("month", "Feb");
		secondMap.put("monthNo", "02");
		secondMap.put("count", String.valueOf(febCount));
		
		

		List<PatientRegistration> thirdMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "03", userId);
		int marchCount=thirdMonth.size();
		
		Map<String, String> thirdMap=new HashMap<>();
		thirdMap.put("doctorName",userName );
		thirdMap.put("year", year);
		thirdMap.put("month", "March");
		thirdMap.put("monthNo", "03");
		thirdMap.put("count", String.valueOf(marchCount));
		
		
		
		List<PatientRegistration> fourthMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "04", userId);
		int aprilCount=fourthMonth.size();
		
		Map<String, String> fourthMap=new HashMap<>();
		fourthMap.put("doctorName",userName );
		fourthMap.put("year", year);
		fourthMap.put("month", "April");
		fourthMap.put("monthNo", "04");
		fourthMap.put("count", String.valueOf(aprilCount));
		
		
		List<PatientRegistration> fifthMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "05", userId);
		int mayCount=fifthMonth.size();
		
		Map<String, String> fifthMap=new HashMap<>();
		fifthMap.put("doctorName",userName );
		fifthMap.put("year", year);
		fifthMap.put("month", "May");
		fifthMap.put("monthNo", "05");
		fifthMap.put("count", String.valueOf(mayCount));
		
		
		List<PatientRegistration> sixthMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "06", userId);
		int juneCount=sixthMonth.size();
		
		Map<String, String> sixthMap=new HashMap<>();
		sixthMap.put("doctorName",userName );
		sixthMap.put("year", year);
		sixthMap.put("month", "June");
		sixthMap.put("monthNo", "06");
		sixthMap.put("count", String.valueOf(juneCount));
		
		
		List<PatientRegistration> seventhMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "07", userId);
		int julyCount=seventhMonth.size();
		
		Map<String, String> sevenMap=new HashMap<>();
		sevenMap.put("doctorName",userName );
		sevenMap.put("year", year);
		sevenMap.put("month", "June");
		sevenMap.put("monthNo", "07");
		sevenMap.put("count", String.valueOf(julyCount));
		
		List<PatientRegistration> eightMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "08", userId);
		int augCount=eightMonth.size();
		
		Map<String, String> eightMap=new HashMap<>();
		eightMap.put("doctorName",userName );
		eightMap.put("year", year);
		eightMap.put("month", "Aug");
		eightMap.put("monthNo", "08");
		eightMap.put("count", String.valueOf(augCount));
		
		List<PatientRegistration> ninthMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "09", userId);
		int septCount=ninthMonth.size();
		
		Map<String, String> ninthMap=new HashMap<>();
		ninthMap.put("doctorName",userName );
		ninthMap.put("year", year);
		ninthMap.put("month", "Sep");
		ninthMap.put("monthNo", "09");
		ninthMap.put("count", String.valueOf(septCount));
		
		List<PatientRegistration> tenthMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "10", userId);
		int octCount=tenthMonth.size();
		
		Map<String, String> tenthMap=new HashMap<>();
		tenthMap.put("doctorName",userName );
		tenthMap.put("year", year);
		tenthMap.put("month", "Oct");
		tenthMap.put("monthNo", "10");
		tenthMap.put("count", String.valueOf(octCount));

		List<PatientRegistration> elventhMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "11", userId);
		int novCount=elventhMonth.size();
		
		Map<String, String> eleventhMap=new HashMap<>();
		eleventhMap.put("doctorName",userName );
		eleventhMap.put("year", year);
		eleventhMap.put("month", "Nov");
		eleventhMap.put("monthNo", "11");
		eleventhMap.put("count", String.valueOf(novCount));
		
		List<PatientRegistration> twelveMonth=patientRegistrationRepository.getPatientCountMonthWise(year, "12", userId);
		int decCount=twelveMonth.size();
		
		Map<String, String> twelveMap=new HashMap<>();
		twelveMap.put("doctorName",userName );
		twelveMap.put("year", year);
		twelveMap.put("month", "Dec");
		twelveMap.put("monthNo", "12");
		twelveMap.put("count", String.valueOf(decCount));
		
		list.add(firstMap);
		list.add(secondMap);
		list.add(thirdMap);
		list.add(fourthMap);
		list.add(fifthMap);
		list.add(sixthMap);
		list.add(sevenMap);
		list.add(eightMap);
		list.add(ninthMap);
		list.add(tenthMap);
		list.add(eleventhMap);
		list.add(twelveMap);
		return list;
	}	
	@RequestMapping(value="/getPatientpost",method=RequestMethod.POST)
	public List<Map<String, String>> getPatientListInPostMethod(@RequestBody Map<String, String> map){
		
		
		String userId=map.get("userId");
		String year=map.get("year");
		String month=map.get("month");
		
		List<PatientRegistration> patientRegistration=patientRegistrationRepository.getPatientCountMonthWise(year, month, userId);
		
		List<Map<String,String>> display=new ArrayList<>();
		String regDate=null;
		String inpatient=null;
		String outpatient=null;
		
		 
		for(PatientRegistration patientRegistrationInfo:patientRegistration)
		{
			Map<String,String> displayInfo=new HashMap<>();
			
			long payment=0;
			
			displayInfo.put("name",patientRegistrationInfo.getPatientDetails().getTitle()+". "+patientRegistrationInfo.getPatientDetails().getFirstName()+" "+patientRegistrationInfo.getPatientDetails().getLastName());
			
			displayInfo.put("patType",patientRegistrationInfo.getpType());
			
			displayInfo.put("umr",patientRegistrationInfo.getPatientDetails().getUmr());
			
			displayInfo.put("doctor",patientRegistrationInfo.getPatientDetails().getConsultant());

			displayInfo.put("DOJ",String.valueOf(patientRegistrationInfo.getDateOfJoining()).substring(0,10));

			displayInfo.put("regId", patientRegistrationInfo.getRegId());
		
			display.add(displayInfo);
			
		}
		
		
		return display;
		
		
		
		
	}
	
	@RequestMapping(value = "/getmonthwisenew/{userId}", method = RequestMethod.GET)
	public List<Object> getPatientNewCount(@PathVariable("userId") String userId) {

		String userName = null;
		String ufn = null;
		String umn = null;
		String uln = null;

		User userInfo = userServiceImpl.findOneByUserId(userId);

		if (userInfo.getFirstName() == null) {
			ufn = " ";
		} else {
			ufn = userInfo.getFirstName();
		}
		if (userInfo.getMiddleName() == null) {
			umn = "";
		} else {
			umn = userInfo.getMiddleName();
		}
		if (userInfo.getLastName() == null) {
			uln = " ";
		} else {
			uln = userInfo.getLastName();
		}
		if (umn.equalsIgnoreCase("")) {
			userName = userInfo.getFirstName() + " " + userInfo.getLastName();
		} else {
			userName = userInfo.getFirstName() + " " + userInfo.getMiddleName() + " " + userInfo.getLastName();
		}

		List<Object> list = new ArrayList<>();
		Map<String, String> map = new HashMap<>();

		String regYear = null;
		String regMonth = null;

				List<PatientRegistration> patientRegistrations = patientRegistrationRepository.findNewPatient(userId);
				List<String> list1 = new ArrayList<>();

		for (PatientRegistration patientRegistrationsInfo : patientRegistrations) {
						List<Object> list2 = new ArrayList<>();
			

			regYear = patientRegistrationsInfo.getRegDate().toString().substring(0, 4);
			regMonth = patientRegistrationsInfo.getRegDate().toString().substring(5, 7);
			
			System.out.println("------outer loop-----" + regYear+"-------"+regMonth);

			if (!list1.contains(regYear+regMonth))  {
				
								System.out.println("------inside if loop-----" + regYear+"-------"+regMonth);
				List<PatientRegistration> patient = patientRegistrationRepository.getPatientCountMonthWise(regYear,
						regMonth, userId);


				map = new HashMap<>();
								
				System.out.println("----------Year--------");
				//for (PatientRegistration pReg : patient) {
					
					int count = patient.size();

					String from = patientRegistrationsInfo.getRegDate().toString();

					Timestamp timestamp = Timestamp.valueOf(from);
					DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");

					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(timestamp.getTime());

					String from1 = dateFormat.format(calendar.getTime());
					
					
					
					map.put("month", from1.substring(3, 6));
					map.put("year", regYear);
					map.put("monthNo", regMonth);
					map.put("userName", userName);
					map.put("count", String.valueOf(count));

					/*System.out.println("------inner loop-----" + pReg.getRegId());
					map.put("month", from1.substring(3, 6));
					map.put("year", pReg.getRegDate().toString().substring(0, 4));
					map.put("monthNo", pReg.getRegDate().toString().substring(5, 7));
					map.put("userName", userName);
					map.put("count", String.valueOf(count));*/
					// map.put("count",String.valueOf(count) );
				
										
				//}
					
					list1.add(String.valueOf(regYear+regMonth));
					//list1.add(String.valueOf(201811));
					
					//System.out.println(list1.get(0));
				
				list.add(map);
				

			}
			
			
		}
		

		return list;
	}
	
	
	
}