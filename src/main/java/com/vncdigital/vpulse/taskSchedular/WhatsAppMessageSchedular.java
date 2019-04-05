package com.vncdigital.vpulse.taskSchedular;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010
.account.Message;
import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.repository.LaboratoryRegistrationRepository;
import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.repository.PatientPaymentRepository;
import com.vncdigital.vpulse.patient.repository.PatientRegistrationRepository;
import com.vncdigital.vpulse.pharmacist.model.Sales;
import com.vncdigital.vpulse.pharmacist.repository.SalesRepository;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;

@Component
public class WhatsAppMessageSchedular {
	public static final Logger log=LoggerFactory.getLogger(WhatsAppMessageSchedular.class);
	static SimpleDateFormat format=null;
	static Timestamp today=null;
	static List<String> containsList=new ArrayList<>();
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.a");
	static boolean flag; 
	public static final  String ACCOUNT_SID = "ACca1eaee475da6191f33c2be2441406cb";
	public static final String AUTH_TOKEN = "84db222c9559df34a87d28bf37364270";
	static Message message = null;
	static	float opamt=0;
	static float ipamt=0;
	static	float otheramt=0;
	static float saleamt=0;
	
	static	float labamt=0;
	

	
	
	

	@Autowired
	UserServiceImpl userServiceImpl;
	
	@Autowired
	PatientRegistrationRepository patientRegistrationRepository;
	
	@Autowired
	LaboratoryRegistrationRepository laboratoryRegistrationRepository;
	
	@Autowired
	PatientPaymentRepository patientPaymentRepository;
	
	@Autowired
	SalesRepository salesRepository;
	
	

//	Cron expression
//	is represented
//	by six fields:
//
//	(*     *      *    *            *       *)
//	second,minute,hour,day of month,month,day(s) of week
//
//		* "0 0 * * * *" = the top of every hour of every day.
//		* "*/10 * * * * *" = every ten seconds.
//		* "0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.
//		* "0 0 8,10 * * *" = 8 and 10 o'clock of every day.
//		* "0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.
//		* "0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays
//		* "0 0 0 25 12 ?" = every Christmas Day at midnight
//
//		(*) means match any
//
//		*/X means "every X"
//
//		? ("no specific value") 
//
//		useful when you need to specify something in one of the two fields in which the character is allowed,
//		but not the other. For example, if I want my trigger to fire on a particular day of the month (say, the 10th), 
//		but I don't care what day of the week that happens to be, I would put "10" in the day-of-month field and "?" in the day-of-week field.

	@Scheduled(cron = "0 03 6 * * ?") 
	public void test()
	{
		String triggerHour=dateTimeFormatter.format(LocalDateTime.now());
    	int todayMin=Integer.parseInt(triggerHour.substring(3, 5));
    	int todayHour=Integer.parseInt(triggerHour.substring(0, 2));
    	
    	Date date = Calendar.getInstance().getTime();
		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
		String today = formatter.format(date).substring(0,11);
		
//     	if(todayHour==11 && todayMin>=0 )
//    	{
//    	System.out.println(flag);
//    	if(!flag)                 
//		{
//		flag=true;
//		
    	executeTask();
//		}
//    	else
//    	{
//    		flag=false;
//    	}
//     }
	}

	
	
	@Transactional
	public void executeTask()
	{
		
		//Date date = Calendar.getInstance().getTime();
		//DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
		//String today = formatter.format(date).substring(0,11);
		
		
		Calendar cal = Calendar.getInstance();
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	System.out.println("Today's date is "+dateFormat.format(cal.getTime()));
    	cal.add(Calendar.DATE, -1);
    	System.out.println("Yesterday's date was "+dateFormat.format(cal.getTime()));
    	String today = dateFormat.format(cal.getTime());
		
		//long opPay=0;
		
		//format=new SimpleDateFormat("HH:mm:ss");
		//today=new Timestamp(System.currentTimeMillis());
		
		
		String triggerHour=dateTimeFormatter.format(LocalDateTime.now());
    	int todayMin=Integer.parseInt(triggerHour.substring(3, 5));
    	int todayHour=Integer.parseInt(triggerHour.substring(0, 2));
    	
    	int min=Integer.parseInt("52");
    	String hr="23";
    	
    	if(hr.equals(String.valueOf(todayHour)) && todayMin>min)
    	{
    		if(!flag)
    		{
    		flag=true;
    		containsList=new ArrayList<>();
    		}
    		
    	}
    	else
    	{
    		flag=false;
    	}
		
		
    	Iterable<User> user=userServiceImpl.findByRole("DOCTOR");
    	
    	for(User u:user)
    	{
    		String id=u.getUserId();
    		
    		List<String> phno=Arrays.asList("919019438586","919579913431");
    		if(id!=null)
    		{	
    		long mobileNo=u.getPersonalContactNumber();
    		
    		List<PatientRegistration> patientRegistration = patientRegistrationRepository.getPatientWiseopCount(id,today);
    		int op=patientRegistration.size();
    		System.out.println("-----outpatientlist"+op);
    		for(PatientRegistration reg:patientRegistration)
    		{
    		System.out.println("shilpi");	
    		System.out.println(reg.getRegId());
    		PatientPayment  pay=patientPaymentRepository.findByOpSumPatientRegistration(reg.getRegId(),today);
    		opamt+=pay.getAmount();
    		
    		}
    		
    		List<PatientRegistration> patientRegistrations = patientRegistrationRepository.getPatientWiseipCount(id,today);
    		int ip=patientRegistrations.size();
    		for(PatientRegistration reg:patientRegistrations)
    		{
    		System.out.println("shilpi");	
    		System.out.println(reg.getRegId());
    		PatientPayment  pay=patientPaymentRepository.findByIpSumPatientRegistration(reg.getRegId(),today);
    		ipamt+=pay.getAmount();
    		
    		}
    		
    		List<PatientRegistration> patientRegistrationother = patientRegistrationRepository.getPatientWiseOtherCount(id,today);
    		int other=patientRegistrationother.size();
    		for(PatientRegistration reg:patientRegistrationother)
    		{
    		System.out.println("shilpi");	
    		System.out.println(reg.getRegId());
    		PatientPayment  pay=patientPaymentRepository.findByOtherSumPatientRegistration(reg.getRegId(),today);
    		otheramt+=pay.getAmount();
    		
    		}
    		
    		
    		List<LaboratoryRegistration>patientRegistrationlabservicescount=laboratoryRegistrationRepository.findLabCountServices(id,today);
    		int labservices=patientRegistrationlabservicescount.size();
    		for(LaboratoryRegistration reg:patientRegistrationlabservicescount)
    		{
    		System.out.println("shilpi");	
    		System.out.println(reg.getRefferedById());
    		LaboratoryRegistration  pay=laboratoryRegistrationRepository.findByLabRegistration(reg.getRefferedById(),today);
    		labamt+=pay.getPrice();
    		}
    		
    		List<Sales>sales=salesRepository.findPerDayPharmacySalesValue(today);
    		for(Sales sale :sales)
    		{
    			saleamt+=sale.getAmount();
    		}
    		
    		
    		
    		
    		System.out.println("-----inpatient list"+ip);
    		 Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    		
    	//	 phno.forEach( (s) -> {
			  message = Message.creator(new com.twilio.type.PhoneNumber("whatsapp:"+"+"+mobileNo), new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
							"Hi Dr ,"+"\n"
							+ "Here are the statistics as on  "+today+"\n"
							+"O/P patients-"+ip+"/"+"Rs"+opamt+"\n"
							+"I/P patients-"+op+"/"+"+Rs"+ipamt+"\n"
							+"Lab Services-"+labservices+"/"+"Rs"+labamt+"\n"
							+"Pharmacy Sales Value-"+"Rs"+saleamt+"\n"
							+"OTHER patients-"+other+"/"+"Rs"+otheramt)
					        
					.create();
    		// }
			// );
			System.out.println(message.getSid());
			System.out.println("shilpiwhtapp msg");
    		
    		}
    		
    		
    		
        	
    		
    	}
    	
    
		
	}


}
