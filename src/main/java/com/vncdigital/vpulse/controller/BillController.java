package com.vncdigital.vpulse.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vncdigital.vpulse.MoneyToWords.NumberToWordsConverter;
import com.vncdigital.vpulse.bed.model.RoomBookingDetails;
import com.vncdigital.vpulse.bed.model.RoomDetails;
import com.vncdigital.vpulse.bed.repository.RoomBookingDetailsRepository;
import com.vncdigital.vpulse.bill.dto.ChargeBillDto;
import com.vncdigital.vpulse.bill.helper.RefBillDetails;
import com.vncdigital.vpulse.bill.helper.RefBillids;
import com.vncdigital.vpulse.bill.model.ChargeBill;
import com.vncdigital.vpulse.bill.repository.ChargeBillRepository;
import com.vncdigital.vpulse.bill.serviceImpl.ChargeBillServiceImpl;
import com.vncdigital.vpulse.config.ConstantValues;
import com.vncdigital.vpulse.finalBilling.model.FinalBilling;
import com.vncdigital.vpulse.finalBilling.serviceImpl.FinalBillingServiceImpl;
import com.vncdigital.vpulse.laboratory.model.LabServices;
import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.repository.LabServicesRepository;
import com.vncdigital.vpulse.laboratory.repository.LaboratoryRegistrationRepository;
import com.vncdigital.vpulse.laboratory.serviceImpl.LabServicesServiceImpl;
import com.vncdigital.vpulse.laboratory.serviceImpl.LaboratoryRegistrationServiceImpl;
import com.vncdigital.vpulse.patient.model.CashPlusCard;
import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.serviceImpl.CashPlusCardServiceImpl;
import com.vncdigital.vpulse.patient.serviceImpl.PatientPaymentServiceImpl;
import com.vncdigital.vpulse.patient.serviceImpl.PatientRegistrationServiceImpl;
import com.vncdigital.vpulse.pharmacist.model.PatientSales;
import com.vncdigital.vpulse.pharmacist.model.Sales;
import com.vncdigital.vpulse.pharmacist.model.SalesPaymentPdf;
import com.vncdigital.vpulse.pharmacist.serviceImpl.PatientSalesServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.SalesPaymentPdfServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.SalesServiceImpl;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;

@CrossOrigin(origins="*",maxAge=36000)
@RestController
@RequestMapping("/v1/bill")
public class BillController {
	
	public static Logger Logger=LoggerFactory.getLogger(BillController.class);
	
	private static final String TYPE_OF_CHARGE_SETTLED_AMOUNT="SETTLED AMOUNT";
	
	
	@Autowired
	PatientRegistrationServiceImpl patientRegistrationServiceImpl;
	
	@Autowired
	SalesPaymentPdf salesPaymentPdf;
	
	@Autowired
	PatientSalesServiceImpl patientSalesServiceImpl;
	
	@Autowired
	PatientPaymentServiceImpl patientPaymentServiceImpl;
	
	@Autowired
	SalesPaymentPdfServiceImpl salesPaymentPdfServiceImpl;
	
	@Autowired
	ResourceLoader resourceLoader;
	
	@Autowired
	CashPlusCardServiceImpl cashPlusCardServiceImpl;
	
	@Autowired
	RoomBookingDetailsRepository roomBookingDetailsRepository;
	
	@Autowired
	UserServiceImpl userServiceImpl;
	
	@Autowired
	LaboratoryRegistrationServiceImpl laboratoryRegistrationServiceImpl;
	
	@Autowired
	NumberToWordsConverter numberToWordsConverter;
	
	@Autowired
	ChargeBillServiceImpl chargeBillServiceImpl;
	
	@Autowired
	ChargeBillRepository chargeBillRepository;
	
	@Autowired
	SalesServiceImpl salesServiceImpl;
	
	@Autowired
	LaboratoryRegistrationRepository laboratoryRegistrationRepository;
	
	@Autowired
	RefBillDetails refBillDetails;
	
	@Autowired
	RefBillids refBillids;
	
	@Autowired
	LabServicesServiceImpl labServicesServiceImpl;
	
	@Autowired
	LabServicesRepository labServicesRepository;
	
	@Autowired
	FinalBillingServiceImpl finalBillingServcieImpl;
	
	
	/*
	 * Sending Data after page loaded 
	 */
	 
	@RequestMapping(value="/create/{regId}",method=RequestMethod.GET)
	public List<Object> create(@PathVariable String regId)
	{
		PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(regId);
		
		if(patientRegistration.getpType().equalsIgnoreCase(ConstantValues.OUTPATIENT))
		{
			throw new RuntimeException(ConstantValues.OUTPATIENT_NOT_ALLOWED_ERROR_MSG);
		}
		
		List<ChargeBill> chargeBill=chargeBillServiceImpl.findByPatRegIdAndNetAmountNot(patientRegistration, 0);
		List<Object> display=new ArrayList<>();
		List<Object> refBill=new ArrayList<>();
		
		try
		{

		String doj=patientRegistration.getDateOfJoining().toString().substring(0,10);
		SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat toFormat = new SimpleDateFormat("dd-MMM-yyyy");
		doj=toFormat.format(fromFormat.parse(doj));
		refBillids.setProcedureName(patientRegistration.getProcedureName());
		refBillids.setDoj(doj);
		refBillids.setpType(patientRegistration.getpType());
		refBillids.setPatientName(patientRegistration.getPatientDetails().getFirstName()+" "+patientRegistration.getPatientDetails().getLastName());
		refBillids.setDoc(patientRegistration.getPatientDetails().getConsultant());
		
		String dob=patientRegistration.getPatientDetails().getDob().toString().substring(0,10);
		 fromFormat = new SimpleDateFormat("yyyy-MM-dd");
		 toFormat = new SimpleDateFormat("dd-MMM-yyyy");
		dob=toFormat.format(fromFormat.parse(dob));
		refBillids.setDob(dob);
		refBillids.setUmr(patientRegistration.getPatientDetails().getUmr());
		refBill.add(refBillids);
		display.add(refBill);
		if(chargeBill!=null)
		{
			for(ChargeBill chargeBillInfo:chargeBill)
			{
				if(chargeBillInfo.getLabId()!=null)
				{
					chargeBillInfo.setServiceName(chargeBillInfo.getLabId().getServiceName());
				}
				else if(chargeBillInfo.getSaleId()!=null)
				{
					chargeBillInfo.setServiceName("Medicine "+chargeBillInfo.getSaleId().getMedicineName());
				}
				else if(chargeBillInfo.getServiceId()!=null)
				{
					chargeBillInfo.setServiceName(chargeBillInfo.getServiceId().getServiceName());
				}
			}
			display.add(chargeBill);
		}
		}
		catch(Exception e)
		{
			Logger.error(e.getMessage());
		}
		//display.add(labServicesRepository.findAllByPatientTypeAndServiceType(patientRegistration.getpType(),"Other"));
		
		List<LabServices> accordingToPatientService=labServicesRepository.findAccoringToPatient(patientRegistration.getRoomBookingDetails().get(0).getRoomDetails().getRoomType());
		
		
		display.add(accordingToPatientService);
		List<Object> advanceList=new ArrayList<>();
		Map<String, String> advanceMap=new HashMap<>();
		
		advanceMap.put("advanceAmount", String.valueOf(patientRegistration.getAdvanceAmount()));
		
		advanceList.add(advanceMap);
		
		display.add(advanceList);
		return display;
	}

	/*
	 * get cost for particular service
	 */
	@RequestMapping(value="/service/getCost/{name}/{regId}",method=RequestMethod.GET)
	public Map<String,String> getServiceName(@PathVariable String name,@PathVariable String regId)
	{
		LabServices labServices=null;
		Map<String,String> display=new HashMap<>();
		PatientRegistration patientRegistration= patientRegistrationServiceImpl.findByRegId(regId);
		String roomType=patientRegistration.getRoomBookingDetails().get(0).getRoomDetails().getRoomType();
		String patientType=patientRegistration.getpType();
		
		
		 labServices=labServicesRepository.findPriceByType(name,patientType,roomType);
		
		//Charge charge=chargeServiceImpl.findByName(name);
		display.put("name", name);
		display.put("cost",String.valueOf(labServices.getCost()));
	
		return display;
	}
	
	/*
	 *To add services for billing 
	 */
	@Transactional
	@RequestMapping(value="/charge",method=RequestMethod.POST)
	public void charge(@RequestBody ChargeBillDto chargeBillDto)
	{
		ChargeBill chargeBill=new ChargeBill();
		BeanUtils.copyProperties(chargeBillDto, chargeBill);
	
		PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(chargeBill.getRegId());
		chargeBill.setPatRegId(patientRegistration);
		
		String patientName=patientRegistration.getPatientDetails().getFirstName()+"%20"+patientRegistration.getPatientDetails().getLastName();
		float amount=chargeBill.getNetAmount();	
		long mob=patientRegistration.getPatientDetails().getMobile();
		
		List<ChargeBill> chargeBillList=chargeBillServiceImpl.findByPatRegId(patientRegistration);
		if(chargeBillList!=null)
		{
			for(ChargeBill chargeBillInfo:chargeBillList)
			{
				chargeBillInfo.setPaid("YES");
			}
		}
		// for sms
		
		try {
	        
		 String msg="Dear,%20"+patientName+"%20your%20final%20bill%20amount%20is%20Rs."+amount+"%20generated.%20Thank%20for%20visiting%20Udbhava%20Hospital.";
        	URL url = new URL("https://smsapi.engineeringtgr.com/send/?Mobile=9019438586&Password=N3236Q&Key=nikhilfI0ahSMOQkcb6uJ&Message="+msg+"&To="+mob);
        	URLConnection urlcon = url.openConnection();
            InputStream stream = urlcon.getInputStream();
            int i;
            String response="";
            while ((i = stream.read()) != -1) {
                response+=(char)i;
            } 
            if(response.contains("success")){
                System.out.println("Successfully send SMS");
            }else{
                System.out.println(response);
            }
        } catch (IOException e) {
        	Logger.error(e.getMessage());
        }
		
		List<RefBillDetails> refBillDetails=chargeBill.getRefBillDetails();
		for(RefBillDetails refBillDetailsInfo:refBillDetails)
		{
			chargeBill.setChargeBillId(chargeBillServiceImpl.getNextId());
			chargeBill.setAmount(refBillDetailsInfo.getAmount());
			chargeBill.setQuantity(refBillDetailsInfo.getQuantity());
			chargeBill.setDiscount(refBillDetailsInfo.getDiscount());
			chargeBill.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			LabServices labServices=labServicesServiceImpl.findByServiceNameAndPatientType(refBillDetailsInfo.getChargeName(), patientRegistration.getpType());
//			Charge charge=chargeServiceImpl.findByName(refBillDetailsInfo.getChargeName());
			chargeBill.setServiceId(labServices);
			chargeBill.setPaid("YES");

			
			chargeBillServiceImpl.save(chargeBill);
		}
		
		
	}
	
	public static PdfPCell createCell(String content, float borderWidth, int colspan, int alignment, Font redFont) 
	{
	    PdfPCell cell = new PdfPCell(new Phrase(content));
	    cell.setBorderWidth(borderWidth);
	    cell.setColspan(colspan);
	    cell.setHorizontalAlignment(alignment);
	    return cell;
	}
	
	/*
	 * final billing
	 */
	@Transactional
	@RequestMapping(value="/charge/pay/{id}",method=RequestMethod.POST)
	public void chargePay(@RequestBody ChargeBillDto chargeBillDto,@PathVariable String id,Principal principal)
	{
		//createdBy Security
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		
		
		float amount=0;
		float discount=0;
		float netAmount=0;
		
		String billNoo=null;
		String serviceType="";
		
		//Adding service 
		ChargeBill chargeBill=new ChargeBill();
		BeanUtils.copyProperties(chargeBillDto, chargeBill);

		
		PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(chargeBill.getRegId());
		if(patientRegistration.getpType().equalsIgnoreCase(ConstantValues.OUTPATIENT))
		{
			throw new RuntimeException(ConstantValues.OUTPATIENT_NOT_ALLOWED_ERROR_MSG);
			
		}
		
		

		chargeBill.setPatRegId(patientRegistration);
		patientRegistration.setProcedureName(chargeBill.getProcedure());

		//Room booking details
		List<RoomBookingDetails> roomBookingDetailsList=patientRegistration.getRoomBookingDetails();
		for(RoomBookingDetails roomBookingDetailsInfo:roomBookingDetailsList)
		{
			roomBookingDetailsInfo.setStatus(0);
		}
		
		chargeBill.setUserChargeBillId(userSecurity);
		
		
		//---------##-------------Lab Service-------##------------------------
		LaboratoryRegistration laboratoryRegistration=new LaboratoryRegistration();
		laboratoryRegistration.setLaboratoryPatientRegistration(patientRegistration);
		laboratoryRegistration.setMobile(patientRegistration.getPatientDetails().getMobile());
		laboratoryRegistration.setEnteredDate(new Timestamp(System.currentTimeMillis()));
		laboratoryRegistration.setPatientName(patientRegistration.getPatientDetails().getFirstName()+" "+patientRegistration.getPatientDetails().getLastName());
		User user= userServiceImpl.findOneByUserId(patientRegistration.getVuserD().getUserId());
		laboratoryRegistration.setRefferedById(user.getUserId());
	
		laboratoryRegistration.setInvoiceNo(laboratoryRegistrationServiceImpl.getNextInvoice());
		laboratoryRegistration.setBillNo(laboratoryRegistrationServiceImpl.getNextBillNo());
		laboratoryRegistration.setEnteredBy(userSecurity.getUserId());
		
		String roomType="";
		
		
			List<RoomBookingDetails> roomBookingDetails=patientRegistration.getRoomBookingDetails();
			 roomType=roomBookingDetails.get(0).getRoomDetails().getRoomType();
		
		
	
		//---------------## End of lab Service--------##------------


		List<RefBillDetails> refBillDetails=chargeBill.getRefBillDetails();
		if(!refBillDetails.isEmpty())
		{
		for(RefBillDetails refBillDetailsInfo:refBillDetails)
		{
			List<LabServices> labServiceName=labServicesServiceImpl.findByServiceName(refBillDetailsInfo.getChargeName());
			
			serviceType=labServiceName.get(0).getServiceType();
			
			// --------------##--------- Start Lab Service------##---------------------
			
			
			laboratoryRegistration.setLabRegId(laboratoryRegistrationServiceImpl.getNextLabId());
			LabServices patientLabServices=labServicesServiceImpl.findPriceByType(refBillDetailsInfo.getChargeName(), patientRegistration.getpType(),roomType);
			laboratoryRegistration.setLabServices(patientLabServices);	
			
			if(laboratoryRegistration.getLabServices()==null)
			{
				laboratoryRegistration.setLabServices(labServicesServiceImpl.findPriceByType(refBillDetailsInfo.getChargeName(), patientRegistration.getpType(),roomType));	
			}
			
			
			if(laboratoryRegistration.getLabServices()==null)
			{
				throw new RuntimeException("Not Inserted");
			}
			
		
			// Discount Number
			 amount = refBillDetailsInfo.getAmount();  //total amount
			netAmount=refBillDetailsInfo.getAmount()*refBillDetailsInfo.getQuantity();
			discount=refBillDetailsInfo.getDiscount(); //discount
			netAmount=netAmount-(discount);    // total amount after applying discount
			
			laboratoryRegistration.setDiscount(refBillDetailsInfo.getDiscount());
			laboratoryRegistration.setNetAmount(netAmount);
			laboratoryRegistration.setQuantity(refBillDetailsInfo.getQuantity());
			laboratoryRegistration.setPrice(amount);
			laboratoryRegistration.setServiceName(refBillDetailsInfo.getChargeName());
			
			laboratoryRegistration.setPaid(ConstantValues.NO);
			laboratoryRegistration.setStatus(ConstantValues.COMPLETED);
			laboratoryRegistrationRepository.save(laboratoryRegistration);
			
			//--------##----end of lab Service----##--------------
			
			
			chargeBill.setChargeBillId(chargeBillServiceImpl.getNextId());
			chargeBill.setAmount(refBillDetailsInfo.getAmount());
			chargeBill.setQuantity(refBillDetailsInfo.getQuantity());
			chargeBill.setDiscount(refBillDetailsInfo.getDiscount());
			chargeBill.setNetAmount(refBillDetailsInfo.getNetAmount());
			List<ChargeBill> chargeBillList=chargeBillServiceImpl.findByPatRegId(patientRegistration);
			if(chargeBillList.isEmpty())
			{
				if(chargeBillRepository.findMaxBill()!=null)
				{
					billNoo=chargeBillRepository.findMaxBill();
					int intbillNoo=Integer.parseInt(billNoo.substring(2));
					intbillNoo+=1;
					billNoo="BL"+String.format("%07d",intbillNoo);
				}
				else
				{
					billNoo=chargeBillServiceImpl.getNextBillNo();
				}
				chargeBill.setBillNo(billNoo);
			}
			else
			{
				chargeBill.setBillNo(chargeBillList.get(0).getBillNo());
			}
			chargeBill.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			 
			LabServices	labServices=labServicesServiceImpl.findPriceByType(refBillDetailsInfo.getChargeName(),  patientRegistration.getpType(), (patientRegistration.getRoomBookingDetails()!=null) ? patientRegistration.getRoomBookingDetails().get(0).getRoomDetails().getRoomType() : "NA");
			
			//Charge charge=chargeServiceImpl.findByName(refBillDetailsInfo.getChargeName());
			
			if(serviceType.equalsIgnoreCase(ConstantValues.LAB))
			{
				chargeBill.setLabId(laboratoryRegistration);
			}
			else if(!labServices.getServiceType().equalsIgnoreCase("Lab"))
			{
				chargeBill.setServiceId(labServices);
			}
			
			chargeBill.setMrp(refBillDetailsInfo.getMrp());
			chargeBill.setPaid("NO");
			chargeBill.setPaymentType("Due");
			chargeBill.setInsertedBy(userSecurity.getUserId());
			
			chargeBillServiceImpl.save(chargeBill);
		}
		}
			
	}
	


	/*
	 * Dicharge 
	 */
	@Transactional
	@RequestMapping(value="/charge/discharge/{id}",method=RequestMethod.POST)
	public SalesPaymentPdf dischargePatient(@RequestBody ChargeBillDto chargeBillDto,@PathVariable String id,Principal principal)
	{
		
		String EMPTY_SPACE=" ";
		ChargeBill chargeBill=new ChargeBill();
		BeanUtils.copyProperties(chargeBillDto, chargeBill);
		//createdBy Security
		
		String billNo=null;
				User userSecurity=userServiceImpl.findByUserName(principal.getName());
				String createdBy=userSecurity.getUserId();
				String createdName=userSecurity.getFirstName()+EMPTY_SPACE+userSecurity.getLastName();
				
				
		
		
		PatientPayment patientPaymentPaid=new PatientPayment();
		PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(id);
		if(patientRegistration.getpType().equalsIgnoreCase("OUTPATIENT"))
		{
			throw new RuntimeException("OUTPATIENT NOT ALLOWED");
		}
		
		
		
		if(chargeBill.getPaymentType()==null)
		{
			throw new RuntimeException("Plz Enter Payment Type");
		}
		

		//for finding of paidAmount
		float totaPaidAmount=0;
		
		List<ChargeBill> chargePaidAmount=chargeBillRepository.findByPatRegIdAndPaid(patientRegistrationServiceImpl.findByRegId(id), "YES");
		for(ChargeBill chargePaidAmountInfo:chargePaidAmount) {
			
			totaPaidAmount+=chargePaidAmountInfo.getNetAmount();
		}
		
		String modeOfPayment=chargeBill.getPaymentType();                        //paymentInfo.get("mode");
		
		if(modeOfPayment.equalsIgnoreCase("Card")||modeOfPayment.equalsIgnoreCase("Credit Card")||
				modeOfPayment.equalsIgnoreCase("Debit Card")||modeOfPayment.equalsIgnoreCase("Cash+Card")) {
			
			patientPaymentPaid.setReferenceNumber(chargeBill.getReferenceNumber());
			
		}
		
		
		
		long amount=(long) (chargeBill.getNetAmount());
		
		if(modeOfPayment.equalsIgnoreCase("Due")) {
			
			patientPaymentPaid.setPaid("No");
		}else {
			
			patientPaymentPaid.setPaid("YES");
			
		}
		
		
		if(!modeOfPayment.equalsIgnoreCase("Due"))
		{
			patientPaymentPaid.setTypeOfCharge("SETTLED AMOUNT");
			patientRegistration.setAdvanceAmount(patientRegistration.getAdvanceAmount());
			patientPaymentPaid.setAmount(amount);
			patientPaymentPaid.setModeOfPaymant(modeOfPayment);
			patientPaymentPaid.setPatientRegistration(patientRegistration);
			patientPaymentPaid.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			patientPaymentPaid.setRaisedById(createdBy);	
		patientPaymentServiceImpl.save(patientPaymentPaid);
		}
		
		
		
		String pdfBill=null;
		String regId=id;
		
		
		
		String mn=userSecurity.getMiddleName();
		
		if(mn==null)
		{
			createdName=userSecurity.getFirstName()+" "+userSecurity.getLastName();
		}
		else
		{
			createdName=userSecurity.getFirstName()+" "+userSecurity.getMiddleName()+" "+userSecurity.getLastName();
		}
		List<ChargeBill> chargeBillList=chargeBillServiceImpl.findByPatRegId(patientRegistrationServiceImpl.findByRegId(id));

		List<ChargeBill> chargeBillListLab=chargeBillList.stream().filter((s) -> s.getLabId()!=null).collect(Collectors.toList());
		List<ChargeBill> chargeBillListService=chargeBillList.stream().filter((s) -> s.getServiceId()!=null).collect(Collectors.toList());
		List<ChargeBill> chargeBillListSale=chargeBillList.stream().filter((s) -> s.getSaleId()!=null).collect(Collectors.toList());
		
		
		//Adding service 
	//	ChargeBill chargeBill=new ChargeBill();
		//BeanUtils.copyProperties(chargeBillDto, chargeBill);
		
		patientRegistration.getPatientDetails().setDischarged("Yes");
		//chargeBill.setPatRegId(patientRegistration);

		//Room booking details
		List<RoomBookingDetails> roomBookingDetailsList=patientRegistration.getRoomBookingDetails();
		for(RoomBookingDetails roomBookingDetailsInfo:roomBookingDetailsList)
		{
			// 0 means free
			roomBookingDetailsInfo.setStatus(0);
		}
		
		String patientName=patientRegistration.getPatientDetails().getFirstName()+"%20"+patientRegistration.getPatientDetails().getLastName();
//		long amount=chargeBill.getNetAmount();	
		long mob=patientRegistration.getPatientDetails().getMobile();
		//chargeBill.setUserChargeBillId(userSecurity);
		List<ChargeBill> chargeBillListq=chargeBillServiceImpl.findByPatRegId(patientRegistration);
		
		
		
		long paidSum=0;
		for(ChargeBill chargeBillInfo:chargeBillListq)
		{
			
			billNo=chargeBillInfo.getBillNo();
			if(chargeBillInfo.getPaid().equalsIgnoreCase("Yes"))
			{
				paidSum+=chargeBillInfo.getNetAmount();
			}
		}
		
		pdfBill=chargeBillListq.get(0).getBillNo();
		if(chargeBillListq!=null)
		{
			if(modeOfPayment.equalsIgnoreCase("Due"))
			{
				for(ChargeBill chargeBillInfo:chargeBillListq)
				{
					if(!chargeBillInfo.getPaid().equalsIgnoreCase("Yes"))
					{
						chargeBillInfo.setPaymentType(modeOfPayment);
						chargeBillInfo.setUserChargeBillId(userSecurity);
						chargeBillInfo.setDichargedDate(new Timestamp(System.currentTimeMillis()));
					}
					else
					{
						chargeBillInfo.setUserChargeBillId(userSecurity);
						chargeBillInfo.setDichargedDate(new Timestamp(System.currentTimeMillis()));
					}
				}
			}
			else
			{
				for(ChargeBill chargeBillInfo:chargeBillListq)
				{
					if(chargeBillInfo.getPaymentType().equalsIgnoreCase("Due")) {
					chargeBillInfo.setPaid("YES");
					chargeBillInfo.setPaymentType(modeOfPayment);
					chargeBillInfo.setUserChargeBillId(userSecurity);
					chargeBillInfo.setDichargedDate(new Timestamp(System.currentTimeMillis()));
					
					Sales sale=chargeBillInfo.getSaleId();
					if(sale!=null) {
						sale.setPaid("YES");
						sale.setPaymentType(modeOfPayment);
						sale.setPatientSalesUser(userSecurity);
						sale.setUpdatedBy(userSecurity.getUserId());
						sale.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
					}
					
					
					
					LaboratoryRegistration laboratoryRegistration=chargeBillInfo.getLabId();
					
					if(laboratoryRegistration!=null) {
						laboratoryRegistration.setPaid("YES");
						laboratoryRegistration.setPaymentType(modeOfPayment);
						laboratoryRegistration.setEnteredDate(new Timestamp(System.currentTimeMillis()));
						laboratoryRegistration.setEnteredBy(userSecurity.getUserId());
						laboratoryRegistration.setUserLaboratoryRegistration(userSecurity);
					}

					}//
					
				}
			}
		}
		
		
		//finalbilling
		
		
				FinalBilling finalBilling=new FinalBilling();
				float finalCash=0; 		
				float finalCard=0; 
				float finalCheque=0; 
				
				String regNo=id;
				PatientDetails patientDetails=patientRegistration.getPatientDetails();
				String name=patientDetails.getTitle()+patientDetails.getFirstName()+patientDetails.getLastName();
				String umr=patientDetails.getUmr();
				
				if(modeOfPayment.equalsIgnoreCase("Cash"))
				{
					finalCash=amount;
				}
				else if(modeOfPayment.equalsIgnoreCase("Card"))
				{
					finalCard=amount;
				}
				else if(modeOfPayment.equalsIgnoreCase("Cheque"))
				{
					finalCheque=amount;
				}
				
				if(modeOfPayment.equalsIgnoreCase("Cash+Card"))
				{
					int cashAmount=0;
					int cardAmount=0;
					int chequeAmount=0;
					CashPlusCard cashPlusCardLab=new CashPlusCard();
					cashPlusCardLab.setInsertedBy(userSecurity.getUserId());
					cashPlusCardLab.setInsertedDate(new Timestamp(System.currentTimeMillis()));
					List<Map<String,String>> multiMode=chargeBill.getMultimode();
					for(Map<String,String> multiModeInfo:multiMode)
					{
						if(multiModeInfo.get("mode").equalsIgnoreCase("Cash"))
						{
							cashAmount=Integer.parseInt(multiModeInfo.get("amount"));
							finalCash=Long.parseLong(multiModeInfo.get("amount"));
						}
						else if(multiModeInfo.get("mode").equalsIgnoreCase("Card"))
						{
							cardAmount=Integer.parseInt(multiModeInfo.get("amount"));
							finalCard=Long.parseLong(multiModeInfo.get("amount"));
						}
						else if(multiModeInfo.get("mode").equalsIgnoreCase("Cheque"))
						{
							chequeAmount=Integer.parseInt(multiModeInfo.get("amount"));
							finalCheque=Long.parseLong(multiModeInfo.get("amount"));
						}
						
					}
					cashPlusCardLab.setBillNo(billNo);
					
					cashPlusCardLab.setDescription("Ip Final Bill");
					cashPlusCardLab.setPatientRegistrationCashCard(patientRegistration);
					cashPlusCardLab.setCardAmount(cardAmount);
					cashPlusCardLab.setCashAmount(cashAmount);
					cashPlusCardLab.setChequeAmount(chequeAmount);
					cashPlusCardServiceImpl.save(cashPlusCardLab);
					
					
					
				}
				
				 finalBilling.setBillNo(billNo);
				 finalBilling.setBillType("Ip Final Billing");
				 finalBilling.setCardAmount(finalCard);
				 finalBilling.setCashAmount(finalCash);
				 finalBilling.setChequeAmount(finalCheque);
				 finalBilling.setFinalAmountPaid(amount);
				 finalBilling.setTotalAmount(chargeBill.getAmount());
				 finalBilling.setDiscAmount(chargeBill.getDiscount());
				 finalBilling.setFinalBillUser(userSecurity);
				 finalBilling.setReturnAmount(chargeBill.getReturnAmount());
				 finalBilling.setName(name);
				 finalBilling.setRegNo(regNo);
				 finalBilling.setPaymentType(modeOfPayment);
				 finalBilling.setUmrNo(umr);
				finalBillingServcieImpl.computeSave(finalBilling);

		
		
		// for sms
		
		try {
	        
		 String msg="Dear,%20"+patientName+"%20Thank%20for%20visiting%20Udbhava%20Hospital.";
        	URL url = new URL("https://smsapi.engineeringtgr.com/send/?Mobile=9019438586&Password=N3236Q&Key=nikhilfI0ahSMOQkcb6uJ&Message="+msg+"&To="+mob);
        	URLConnection urlcon = url.openConnection();
            InputStream stream = urlcon.getInputStream();
            int i;
            String response="";
            while ((i = stream.read()) != -1) {
                response+=(char)i;
            } 
            if(response.contains("success")){
                System.out.println("Successfully send SMS");
            }else{
                System.out.println(response);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
		String billNoo=null;
		/*List<RefBillDetails> refBillDetails=chargeBill.getRefBillDetails();
		System.out.println(refBillDetails.isEmpty());
		if(!refBillDetails.isEmpty())
		{
		for(RefBillDetails refBillDetailsInfo:refBillDetails)
		{
			chargeBill.setChargeBillId(chargeBillServiceImpl.getNextId());
			chargeBill.setAmount(refBillDetailsInfo.getAmount());
			chargeBill.setQuantity(refBillDetailsInfo.getQuantity());
			chargeBill.setDiscount(refBillDetailsInfo.getDiscount());
			chargeBill.setNetAmount(refBillDetailsInfo.getNetAmount());
			
			List<ChargeBill> chargeBillList=chargeBillServiceImpl.findByPatRegId(patientRegistration);
			if(chargeBillList.isEmpty())
			{
				if(chargeBillRepository.findMaxBill()!=null)
				{
					billNoo=chargeBillRepository.findMaxBill();
					int intbillNoo=Integer.parseInt(billNoo.substring(2));
					intbillNoo+=1;
					billNoo="BL"+String.format("%07d",intbillNoo);
				}
				else
				{
					billNoo=chargeBillServiceImpl.getNextBillNo();
				}
				chargeBill.setBillNo(billNoo);
			}
			else
			{
				chargeBill.setBillNo(chargeBillList.get(0).getBillNo());
			}
			chargeBill.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			Charge charge=chargeServiceImpl.findByName(refBillDetailsInfo.getChargeName());
			chargeBill.setChargeId(charge);
			chargeBill.setPaid("NO");

			
			chargeBillServiceImpl.save(chargeBill);
		}
		}*/
		//*************************//
		
		Font redFont2 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
		Font redFont3 = new Font(Font.FontFamily.HELVETICA, 12, Font.UNDERLINE);
		Font redFont4 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
		

			Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
			Font redFont9 = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
			

			Font redFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

			Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
			

			//shantharam addr
		String newAddress="";
		
		
		
		
		patientName=null;
		String tokenNo=null;
		
		
			for(ChargeBill chargeBillInfo:chargeBillList)
			{
	    		patientName=chargeBillInfo.getPatRegId().getPatientDetails().getFirstName()+" "+chargeBillInfo.getPatRegId().getPatientDetails().getLastName();
	    		tokenNo=chargeBillInfo.getPatRegId().getRegId().substring(2);
	    		billNo=chargeBillInfo.getBillNo();
	    		chargeBillServiceImpl.save(chargeBillInfo);
	    	}
			
			Set<PatientPayment> patientPayment = patientRegistration.getPatientPayment();
			
			long totalRecieptAmt=0;

			
			//for room details
			String admittedWard = null;

			float salestAmount = 0;
			float salesnetAmount = 0;
			float salesDiscount = 0;
			long salesQuantity = 0;
			
			List<RoomBookingDetails> roomBookingDetails = patientRegistration.getRoomBookingDetails();

			for (RoomBookingDetails roomBookingDetailsInfo : roomBookingDetails) {
				RoomDetails roomDetails = roomBookingDetailsInfo.getRoomDetails();
				admittedWard = roomDetails.getRoomType();
			}
			
			String adWrd=null;
			if(admittedWard!=null)
			{
				adWrd=admittedWard;
			}
			else
			{
				adWrd="";
			}
			
			Date date = Calendar.getInstance().getTime();
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
			String today = formatter.format(date).toString();

			
			Date date1 = Calendar.getInstance().getTime();
			DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");
			String advDate = formatter1.format(date1).toString();

			//for department
			String dpt=null;

			if(!patientRegistration.getVuserD().getSpecUserJoin().isEmpty())
			{
				dpt=patientRegistration.getVuserD().getSpecUserJoin().get(0).getDocSpec().getSpecName();
				
			}
			else
			{
				dpt="";
			}
			
			
	//final advance reciept-----------------------------
			
			
			
			
			Date date2 =patientRegistration.getDateOfJoining();
			DateFormat formatter2 = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
			String admissionDate = formatter2.format(date2).toString();

			
			
			
			byte[] pdfBytes = null;
			ByteArrayOutputStream byteArrayOutputStream = null;

			//for patientname
			String patientFirstName=patientRegistration.getPatientDetails().getFirstName();
			String patientMiddleName=patientRegistration.getPatientDetails().getMiddleName();
			String patientLastName=patientRegistration.getPatientDetails().getLastName();
			
			
			if(!patientMiddleName.isEmpty())
			{
				patientName=patientFirstName+EMPTY_SPACE+patientMiddleName+EMPTY_SPACE+patientLastName;
			}
			else
			{
				patientName=patientFirstName+EMPTY_SPACE+patientLastName;
			}
			
			//PATIENT other details
			umr=patientRegistration.getPatientDetails().getUmr();
			String age=patientRegistration.getPatientDetails().getAge();
			String gender=patientRegistration.getPatientDetails().getGender();
			long contactNo=patientRegistration.getPatientDetails().getMobile();
			String patientFatherName=patientRegistration.getPatientDetails().getMotherName();
			String consultant=patientRegistration.getPatientDetails().getConsultant();
			String city=patientRegistration.getPatientDetails().getCity();
			String state=patientRegistration.getPatientDetails().getState();
			
			String address=patientRegistration.getPatientDetails().getAddress();
			
			//shantharam addr

			//for advance reciept
			String addr=" ";
			
			//for consultation reciept
			String addrss="";
			
			
		  SalesPaymentPdf salesPaymentPdf=null;
		 byteArrayOutputStream = new ByteArrayOutputStream();



			//-----------------new pdf code-------------------------------

			
			try {
				 Document document = new Document(PageSize.A4_LANDSCAPE);

				
				Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
				PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

				Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");
				
				
				Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());
				
				document.open();
				PdfPTable table = new PdfPTable(2);

				img.scaleAbsolute(75, 95);
				table.setWidthPercentage(105);

				Phrase pq = new Phrase(new Chunk(img, 0, -80));
				pq.add(new Chunk(addrss,redFont));
				PdfPCell cellp = new PdfPCell(pq);
				PdfPCell cell1 = new PdfPCell();
				
				// for header Bold
				PdfPTable table96 = new PdfPTable(1);
				table96.setWidths(new float[] { 5f });
				table96.setSpacingBefore(10);

				PdfPCell hcell96;
				hcell96 = new PdfPCell(new Phrase(ConstantValues.HOSPITAL_NAME, headFont1));
				hcell96.setBorder(Rectangle.NO_BORDER);
				hcell96.setHorizontalAlignment(Element.ALIGN_CENTER);
				hcell96.setPaddingTop(5f);
				hcell96.setPaddingLeft(52f);

				table96.addCell(hcell96);
				cell1.addElement(table96);

				// for header end

				
				cell1.addElement(pq);
				cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell1);

				
				
				PdfPCell cell3 = new PdfPCell();
				
				PdfPTable table99 = new PdfPTable(3);
				table99.setWidths(new float[]{3f,1f,5f});
				table99.setSpacingBefore(10);
		        
		        PdfPCell hcell90;
		        hcell90 = new PdfPCell(new Phrase("Patient",redFont));
		        hcell90.setBorder(Rectangle.NO_BORDER);
		        hcell90.setPaddingBottom(-7f);
		        hcell90.setPaddingLeft(-25f);
		        table99.addCell(hcell90);
		        
		        hcell90 = new PdfPCell(new Phrase(":",redFont));
		        hcell90.setBorder(Rectangle.NO_BORDER);
		        hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell90.setPaddingLeft(-45f);
		        table99.addCell(hcell90);
		        
		        hcell90 = new PdfPCell(new Phrase(patientName,redFont));
		        hcell90.setBorder(Rectangle.NO_BORDER);
		        hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell90.setPaddingBottom(-7f);
		        hcell90.setPaddingLeft(-60f);
		        table99.addCell(hcell90);
		        
		        cell3.addElement(table99);
				
				PdfPTable table2 = new PdfPTable(6);
			    table2.setWidths(new float[]{3f,1f,5f,3f,1f,4f});
			    table2.setSpacingBefore(10);
			    
			    PdfPCell hcell1;
		        hcell1 = new PdfPCell(new Phrase("UMR NO",redFont));
		        hcell1.setBorder(Rectangle.NO_BORDER);
		        hcell1.setPaddingLeft(-25f);
		        hcell1.setPaddingTop(-5f);
		        table2.addCell(hcell1);
		        
		        hcell1 = new PdfPCell(new Phrase(":",redFont));
		        hcell1.setBorder(Rectangle.NO_BORDER);
		        hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell1.setPaddingLeft(-10f);
		        hcell1.setPaddingTop(-5f);
		        table2.addCell(hcell1);
		        
		        hcell1 = new PdfPCell(new Phrase(umr,redFont));
		        hcell1.setBorder(Rectangle.NO_BORDER);
		        hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell1.setPaddingLeft(-15f);
		        hcell1.setPaddingTop(-5f);
		        table2.addCell(hcell1);
		        
		        hcell1 = new PdfPCell(new Phrase("",redFont));
		        hcell1.setBorder(Rectangle.NO_BORDER);
		        hcell1.setPaddingRight(34f);
		        hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        table2.addCell(hcell1);
		        
		        hcell1 = new PdfPCell(new Phrase("",redFont));
		        hcell1.setBorder(Rectangle.NO_BORDER);
		        hcell1.setPaddingRight(34f);
		        hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        table2.addCell(hcell1);
		        
		        hcell1 = new PdfPCell(new Phrase("",redFont));
		        hcell1.setBorder(Rectangle.NO_BORDER);
		        hcell1.setPaddingRight(34f);
		        hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        table2.addCell(hcell1);
		        
		        PdfPCell hcell4;
		        hcell4 = new PdfPCell(new Phrase("Rect.No" ,redFont));
		        hcell4.setBorder(Rectangle.NO_BORDER);
		        hcell4.setPaddingLeft(-25f);
		        //hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table2.addCell(hcell4);
		        
		        hcell4 = new PdfPCell(new Phrase(":",redFont));
		        hcell4.setBorder(Rectangle.NO_BORDER);
		        hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell4.setPaddingLeft(-10f);
		        table2.addCell(hcell4);
		        
		        hcell4 = new PdfPCell(new Phrase(regId,redFont));
		        hcell4.setBorder(Rectangle.NO_BORDER);
		        hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell4.setPaddingLeft(-15f);
		        table2.addCell(hcell4);
		        
		        hcell4 = new PdfPCell(new Phrase("Rect.Dt" ,redFont));
		        hcell4.setBorder(Rectangle.NO_BORDER);
		        hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell1.setPaddingRight(-27.5f);
		        table2.addCell(hcell4);
		        
		        hcell4 = new PdfPCell(new Phrase(":",redFont));
		        hcell4.setBorder(Rectangle.NO_BORDER);
		        hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        hcell4.setPaddingRight(-0.1f);
		        table2.addCell(hcell4);
		        
		        hcell4 = new PdfPCell(new Phrase(advDate,redFont));
		        hcell4.setBorder(Rectangle.NO_BORDER);
		        hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell4.setPaddingRight(-20.5f);
		        table2.addCell(hcell4);
		        
		        PdfPCell hcell15;
		        hcell15 = new PdfPCell(new Phrase("Age/Sex" ,redFont));
		        hcell15.setBorder(Rectangle.NO_BORDER);
		        hcell15.setPaddingLeft(-25f);
		        //hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table2.addCell(hcell15);
		        
		        hcell15 = new PdfPCell(new Phrase(":",redFont));
		        hcell15.setBorder(Rectangle.NO_BORDER);
		        hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell15.setPaddingLeft(-10f);
		        table2.addCell(hcell15);
		        
		        hcell15 = new PdfPCell(new Phrase(age+"/"+gender,redFont));
		        hcell15.setBorder(Rectangle.NO_BORDER);
		        hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell15.setPaddingLeft(-15f);
		        table2.addCell(hcell15);
		        
		        hcell15 = new PdfPCell(new Phrase("Phone" ,redFont));
		        hcell15.setBorder(Rectangle.NO_BORDER);
		        hcell15.setPaddingRight(-27.5f);
		        hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table2.addCell(hcell15);
		        
		        hcell4 = new PdfPCell(new Phrase(":",redFont));
		        hcell4.setBorder(Rectangle.NO_BORDER);
		        hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        hcell4.setPaddingRight(-0.1f);
		        table2.addCell(hcell4);
		        
		        hcell4 = new PdfPCell(new Phrase(String.valueOf(contactNo),redFont));
		        hcell4.setBorder(Rectangle.NO_BORDER);
		        hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell4.setPaddingRight(-20.5f);
		        table2.addCell(hcell4);
		   
		        cell3.setFixedHeight(110f);
		        cell3.setColspan(2);
		        cell3.addElement(table2);
		 
		      table.addCell(cell3);
		     
		      
		      PdfPCell cell19 = new PdfPCell();

				PdfPTable table21 = new PdfPTable(1);
				table21.setWidths(new float[] { 4f });
				table21.setSpacingBefore(10);

				PdfPCell hcell19;
				hcell19 = new PdfPCell(new Phrase("Final Settled Receipt", headFont1));
				hcell19.setBorder(Rectangle.NO_BORDER);
				hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
				//hcell19.setPaddingLeft(-70f);
				table21.addCell(hcell19);

			
				cell19.setFixedHeight(20f);
				cell19.setColspan(2);
				cell19.addElement(table21);
				table.addCell(cell19);

				PdfPCell cell4 = new PdfPCell();
				
				PdfPTable table3 = new PdfPTable(6);
			    table3.setWidths(new float[]{5f,1f,6f,5f,1f,6f});
			    table3.setSpacingBefore(10);
			  
		        PdfPCell hcell;
		        hcell = new PdfPCell(new Phrase("Father",redFont));
		        hcell.setBorder(Rectangle.NO_BORDER);
		        hcell.setPaddingLeft(-50f);
		        table3.addCell(hcell);
		        
		        hcell = new PdfPCell(new Phrase(":",redFont));
		        hcell.setBorder(Rectangle.NO_BORDER);
		        hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell.setPaddingLeft(-80f);
		        table3.addCell(hcell);
		        
		        hcell = new PdfPCell(new Phrase(patientFatherName,redFont));
		        hcell.setBorder(Rectangle.NO_BORDER);
		        hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell.setPaddingLeft(-80f);
		        table3.addCell(hcell);
		       
		        Font redFont5 = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
				
		        
		        hcell = new PdfPCell(new Phrase("Admitted Ward",redFont5));
		        hcell.setBorder(Rectangle.NO_BORDER);
		        hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell);
		        
		        hcell = new PdfPCell(new Phrase(":",redFont));
		        hcell.setBorder(Rectangle.NO_BORDER);
		        hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell.setPaddingLeft(-15f);
		        table3.addCell(hcell);
		        
		        hcell = new PdfPCell(new Phrase(admittedWard,redFont));
		        hcell.setBorder(Rectangle.NO_BORDER);
		        hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell.setPaddingLeft(-20f);
		        table3.addCell(hcell);
		        
		        PdfPCell hcell11;
		        hcell11 = new PdfPCell(new Phrase("Admn.Dt",redFont));
		        hcell11.setBorder(Rectangle.NO_BORDER);
		        hcell11.setPaddingLeft(-50f);
		        table3.addCell(hcell11);
		        
		        hcell11 = new PdfPCell(new Phrase(":",redFont));
		        hcell11.setBorder(Rectangle.NO_BORDER);
		        hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell11.setPaddingLeft(-80f);
		        table3.addCell(hcell11);
		        
		        hcell11 = new PdfPCell(new Phrase(admissionDate,redFont));
		        hcell11.setBorder(Rectangle.NO_BORDER);
		        hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell11.setPaddingLeft(-80f);
		        table3.addCell(hcell11);
		        
		        
		        hcell11 = new PdfPCell(new Phrase("Department",redFont));
		        hcell11.setBorder(Rectangle.NO_BORDER);
		        hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell11);
		        
		        hcell11 = new PdfPCell(new Phrase(":",redFont));
		        hcell11.setBorder(Rectangle.NO_BORDER);
		        hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell11.setPaddingLeft(-15f);
		        table3.addCell(hcell11);
		        
		        
		      
		        hcell11 = new PdfPCell(new Phrase(dpt,redFont5));
		        hcell11.setBorder(Rectangle.NO_BORDER);
		        hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell11.setPaddingLeft(-20f);
		        table3.addCell(hcell11);
		        
		        PdfPCell hcell14;
		        hcell14 = new PdfPCell(new Phrase("Consultant",redFont));
		        hcell14.setBorder(Rectangle.NO_BORDER);
		        hcell14.setPaddingLeft(-50f);
		        table3.addCell(hcell14);
		        
		        hcell14 = new PdfPCell(new Phrase(":",redFont));
		        hcell14.setBorder(Rectangle.NO_BORDER);
		        hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell14.setPaddingLeft(-80f);
		        table3.addCell(hcell14);
		        
		        hcell14 = new PdfPCell(new Phrase(consultant,redFont));
		        hcell14.setBorder(Rectangle.NO_BORDER);
		        hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell14.setPaddingLeft(-80f);
		        table3.addCell(hcell14);
		        
		       
		        //final setted amount
		        hcell14 = new PdfPCell(new Phrase("Final Settled.Amt",redFont));
		        hcell14.setBorder(Rectangle.NO_BORDER);
		        hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell14);
		        
		        hcell14 = new PdfPCell(new Phrase(":",redFont));
		        hcell14.setBorder(Rectangle.NO_BORDER);
		        hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell14.setPaddingLeft(-15f);
		        table3.addCell(hcell14);
		        
		        hcell14 = new PdfPCell(new Phrase(String.valueOf(amount),redFont));
		        hcell14.setBorder(Rectangle.NO_BORDER);
		        hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell14.setPaddingLeft(-20f);
		        table3.addCell(hcell14);
		        
		        PdfPCell hcell16;
		        hcell16 = new PdfPCell(new Phrase("Org.",redFont));
		        hcell16.setBorder(Rectangle.NO_BORDER);
		        hcell16.setPaddingLeft(-50f);
		        //hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell16);
		        
		        hcell16 = new PdfPCell(new Phrase(":",redFont));
		        hcell16.setBorder(Rectangle.NO_BORDER);
		        hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell16.setPaddingLeft(-80f);
		        table3.addCell(hcell16);
		        
		        hcell16 = new PdfPCell(new Phrase(ConstantValues.HOSPITAL_NAME,redFont));
		        hcell16.setBorder(Rectangle.NO_BORDER);
		        hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell16.setPaddingLeft(-80f);
		        table3.addCell(hcell16);
		        
		        
		        hcell16 = new PdfPCell(new Phrase("Amt Recieved",redFont));
		        hcell16.setBorder(Rectangle.NO_BORDER);
		        hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell16);
		        
		        hcell16 = new PdfPCell(new Phrase(":",redFont));
		        hcell16.setBorder(Rectangle.NO_BORDER);
		        hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell16.setPaddingLeft(-15f);
		        table3.addCell(hcell16);
		        
		        hcell16 = new PdfPCell(new Phrase(String.valueOf(amount),redFont));
		        hcell16.setBorder(Rectangle.NO_BORDER);
		        hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell16.setPaddingLeft(-20f);
		        table3.addCell(hcell16);
		        
		        
		        PdfPCell hcell17;
		        hcell17 = new PdfPCell(new Phrase("Address",redFont));
		        hcell17.setBorder(Rectangle.NO_BORDER); 
		        hcell17.setPaddingLeft(-50f);
		        table3.addCell(hcell17);
		        
		        hcell17 = new PdfPCell(new Phrase(":",redFont));
		        hcell17.setBorder(Rectangle.NO_BORDER);
		        hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell17.setPaddingLeft(-80f);
		        table3.addCell(hcell17);
		        
		        hcell17 = new PdfPCell(new Phrase(address,redFont));
		        hcell17.setBorder(Rectangle.NO_BORDER);
		        hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell17.setPaddingLeft(-80f);
		        table3.addCell(hcell17);
		        
		       
		        hcell17 = new PdfPCell(new Phrase(EMPTY_SPACE,redFont));
		        hcell17.setBorder(Rectangle.NO_BORDER);
		        hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell17);
		        
		        hcell17 = new PdfPCell(new Phrase(EMPTY_SPACE,redFont));
		        hcell17.setBorder(Rectangle.NO_BORDER);
		        hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell17.setPaddingLeft(-15f);
		        table3.addCell(hcell17);
		        
		        hcell17 = new PdfPCell(new Phrase(EMPTY_SPACE,redFont));
		        hcell17.setBorder(Rectangle.NO_BORDER);
		        hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell17.setPaddingLeft(-20f);
		        table3.addCell(hcell17);
		       
		       
		        
		        PdfPCell hcell18;
		        hcell18 = new PdfPCell(new Phrase("City",redFont));
		        hcell18.setBorder(Rectangle.NO_BORDER);
		        hcell18.setPaddingLeft(-50f);
		        table3.addCell(hcell18);
		        
		        hcell18 = new PdfPCell(new Phrase(":",redFont));
		        hcell18.setBorder(Rectangle.NO_BORDER);
		        hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell18.setPaddingLeft(-80f);
		        table3.addCell(hcell18);
		        
		        hcell18 = new PdfPCell(new Phrase(city,redFont));
		        hcell18.setBorder(Rectangle.NO_BORDER);
		        hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell18.setPaddingLeft(-80f);
		        table3.addCell(hcell18);
		         
		        hcell18 = new PdfPCell(new Phrase(EMPTY_SPACE,redFont));
		        hcell18.setBorder(Rectangle.NO_BORDER);
		        hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell18);
		        
		        hcell18 = new PdfPCell(new Phrase(EMPTY_SPACE,redFont));
		        hcell18.setBorder(Rectangle.NO_BORDER);
		        hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell18.setPaddingLeft(-15f);
		        table3.addCell(hcell18);
		        
		        hcell18 = new PdfPCell(new Phrase(EMPTY_SPACE,redFont));
		        hcell18.setBorder(Rectangle.NO_BORDER);
		        hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell18.setPaddingLeft(-20f);
		        table3.addCell(hcell18);
		        
		        
		        PdfPCell hcell20;
		        hcell20 = new PdfPCell(new Phrase("State",redFont));
		        hcell20.setBorder(Rectangle.NO_BORDER);
		        hcell20.setPaddingLeft(-50f);
		        table3.addCell(hcell20);
		        
		        hcell20 = new PdfPCell(new Phrase(":",redFont));
		        hcell20.setBorder(Rectangle.NO_BORDER);
		        hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell20.setPaddingLeft(-80f);
		        table3.addCell(hcell20);
		        
		        hcell20 = new PdfPCell(new Phrase(state,redFont));
		        hcell20.setBorder(Rectangle.NO_BORDER);
		        hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
		        hcell20.setPaddingLeft(-80f);
		        table3.addCell(hcell20);
		        
		        hcell20 = new PdfPCell(new Phrase(EMPTY_SPACE,redFont));
		        hcell20.setBorder(Rectangle.NO_BORDER);
		        hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell20);
		        
		        hcell20 = new PdfPCell(new Phrase(EMPTY_SPACE,redFont));
		        hcell20.setBorder(Rectangle.NO_BORDER);
		        hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell20);
		        
		        hcell20 = new PdfPCell(new Phrase(EMPTY_SPACE,redFont));
		        hcell20.setBorder(Rectangle.NO_BORDER);
		        hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
		        table3.addCell(hcell20);
		        
		        PdfPTable table91 = new PdfPTable(1);
		        table91.setWidths(new float[]{5f});
		        table91.setSpacingBefore(10);
			    
		        cell4.setFixedHeight(170f);
		        cell4.setColspan(2);
		        cell4.addElement(table3);
		        
		        PdfPCell hcell98;
		        hcell98 = new PdfPCell(new Phrase("Received with thanks from "+patientName+", "+"A sum of Rs. "+amount+"\n\n"+"In Words Rupees "+numberToWordsConverter.convert((int)amount),redFont));
		        hcell98.setBorder(Rectangle.NO_BORDER);
		        hcell98.setPaddingLeft(-50f);
		        hcell98.setPaddingTop(3);
		        table91.addCell(hcell98);
		        cell4.addElement(table91);
				table.addCell(cell4);
				
				PdfPCell cell5 = new PdfPCell();
				
				PdfPTable table35 = new PdfPTable(2);
				table35.setWidths(new float[]{5f, 4f});
				table35.setSpacingBefore(10);
					    
				
				
				 PdfPCell hcell21;
				 hcell21 = new PdfPCell(new Phrase("*"+umr+"*",headFont1));
				 hcell21.setBorder(Rectangle.NO_BORDER);
				 hcell21.setPaddingLeft(-50f);
			        table35.addCell(hcell21);
			       
			        hcell21 = new PdfPCell(new Phrase("*"+regId+"*",headFont1));
			        hcell21.setBorder(Rectangle.NO_BORDER);
			        hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
			        table35.addCell(hcell21);
			     
				        PdfPCell hcell12;
				        hcell12 = new PdfPCell(new Phrase("Created By    : "+createdName,redFont));
				        hcell12.setBorder(Rectangle.NO_BORDER);
				        hcell12.setPaddingTop(10f);
				        hcell12.setPaddingLeft(-50f);
				        table35.addCell(hcell12);
				       
				        hcell12 = new PdfPCell(new Phrase("Created Dt   :   "+today,redFont));
				        hcell12.setBorder(Rectangle.NO_BORDER);
				        hcell12.setPaddingTop(10f);
				        hcell12.setHorizontalAlignment(Element.ALIGN_RIGHT);
				        table35.addCell(hcell12);
				        
				        PdfPCell hcell13;
				        hcell13 = new PdfPCell(new Phrase("Printed By     : "+createdName,redFont));
				        hcell13.setBorder(Rectangle.NO_BORDER);
				        hcell13.setPaddingLeft(-50f);
				        table35.addCell(hcell13);
				        
				        hcell13 = new PdfPCell(new Phrase("Print Dt       :   "+today,redFont));
				        hcell13.setBorder(Rectangle.NO_BORDER);
				        hcell13.setPaddingRight(3f);
				        hcell13.setHorizontalAlignment(Element.ALIGN_RIGHT);
				        table35.addCell(hcell13);
				        
				        PdfPCell hcell23;
				        hcell23 = new PdfPCell(new Phrase(""));
				        hcell23.setBorder(Rectangle.NO_BORDER);
				        table35.addCell(hcell23);
				        
				        hcell23 = new PdfPCell(new Phrase("(Authorized Signature)",headFont));
				        hcell23.setBorder(Rectangle.NO_BORDER);
				        hcell23.setPaddingTop(25f);
				        hcell23.setHorizontalAlignment(Element.ALIGN_RIGHT);
				        table35.addCell(hcell23);
				        
				        
				        cell5.setFixedHeight(105f);
				        cell5.setColspan(2);
				        cell5.addElement(table35);
						table.addCell(cell5);
						

				
		        document.add(table);
		       
				

				document.close();		
					System.out.println("finished");

					pdfBytes = byteArrayOutputStream.toByteArray();
					String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
							.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();

					salesPaymentPdf = new SalesPaymentPdf();
					salesPaymentPdf.setFileName(regId+EMPTY_SPACE+"Final Advance Reciept");
					salesPaymentPdf.setFileuri(uri);
					salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
					salesPaymentPdf.setData(pdfBytes);
					System.out.println(salesPaymentPdf);
					// System.out.println("------------------------End--------------------------------");
					// System.out.println(totalBillAmount);
					salesPaymentPdfServiceImpl.save(salesPaymentPdf);

				} catch (Exception e) {
					Logger.error(e.getMessage());
				}

			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			//final bill-------------------------------
			
			byte[] pdfByte = null;
			ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();

		try {

				Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");

				Document document = new Document(PageSize.A4_LANDSCAPE);
				PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream1);

				
					document.open();
					PdfPTable table = new PdfPTable(2);

					Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());

					img.scaleAbsolute(60, 87);

					table.setWidthPercentage(105f);

					Phrase pq = new Phrase(new Chunk(img, 5, -63));

					pq.add(new Chunk(newAddress,redFont));
					
		                    
					PdfPCell cellp = new PdfPCell(pq);
					PdfPCell cell1 = new PdfPCell();
					cell1.setBorder(0);

					//cell1.setFixedHeight(107f);
					
					
					PdfPTable table351 = new PdfPTable(1);
					table351.setWidths(new float[] {5f});
					table351.setSpacingBefore(10);
					table351.setWidthPercentage(100f);
					
					PdfPCell hcell351;
					hcell351 = new PdfPCell(new Phrase("UDBHAVA HOSPITALS", redFont4));
					hcell351.setBorder(Rectangle.NO_BORDER);
					hcell351.setHorizontalAlignment(Element.ALIGN_CENTER);
					table351.addCell(hcell351);
					
					
					cell1.addElement(table351);
					cell1.addElement(pq);
					cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell1.setColspan(2);
				
					//table.addCell(cell1);
					
				/*	PdfPCell cell35 = new PdfPCell();
					cell35.setBorder(Rectangle.NO_BORDER);
				*/
					
					
					PdfPTable table35 = new PdfPTable(1);
					table35.setWidths(new float[] {5f});
					table35.setSpacingBefore(10);
					table35.setWidthPercentage(100f);
					
					PdfPCell hcell35;
					hcell35 = new PdfPCell(new Phrase("INPATIENT FINAL BILL", headFont));
					hcell35.setBorder(Rectangle.NO_BORDER);
					hcell35.setHorizontalAlignment(Element.ALIGN_CENTER);
					table35.addCell(hcell35);
					
					/*cell35.setColspan(2);
					cell35.addElement(table35);
					table.addCell(cell35);*/
					
					cell1.addElement(table35);
				

					//PdfPCell cell19 = new PdfPCell();

					PdfPTable table3 = new PdfPTable(6);
					table3.setWidths(new float[] { 5f,1f, 5f,5f,1f, 5f });
					table3.setSpacingBefore(10);

					PdfPCell hcell1;
					hcell1 = new PdfPCell(new Phrase("Bill Date", redFont1));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell1.setPaddingLeft(-50f);
					table3.addCell(hcell1);
					
					hcell1 = new PdfPCell(new Phrase(":", redFont1));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell1.setPaddingLeft(-50f);
					table3.addCell(hcell1);
					
					hcell1 = new PdfPCell(new Phrase(today, redFont1));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell1.setPaddingLeft(-50f);
					table3.addCell(hcell1);

										
					PdfPCell hcell10;
					hcell10 = new PdfPCell(new Phrase("Bill No" , redFont1));
					hcell10.setBorder(Rectangle.NO_BORDER);
					//hcell10.setPaddingLeft(40f);
					table3.addCell(hcell10);

					hcell10 = new PdfPCell(new Phrase(":" , redFont1));
					hcell10.setBorder(Rectangle.NO_BORDER);
					//hcell10.setPaddingLeft(40f);
					table3.addCell(hcell10);

					hcell10 = new PdfPCell(new Phrase(pdfBill , redFont1));
					hcell10.setBorder(Rectangle.NO_BORDER);
					//hcell10.setPaddingLeft(40f);
					table3.addCell(hcell10);

					
					
					PdfPCell hcell2;
					hcell2 = new PdfPCell(new Phrase("Admission No" , redFont1));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table3.addCell(hcell2);

					
					hcell2 = new PdfPCell(new Phrase(":", redFont1));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table3.addCell(hcell2);

					hcell2 = new PdfPCell(new Phrase(patientRegistration.getRegId(), redFont1));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table3.addCell(hcell2);

					
					
					PdfPCell hcell20;
					hcell20 = new PdfPCell(new Phrase("UMR No", redFont1));
					hcell20.setBorder(Rectangle.NO_BORDER);
					//hcell20.setPaddingLeft(40f);
					table3.addCell(hcell20);
					
					hcell20 = new PdfPCell(new Phrase(":", redFont1));
					hcell20.setBorder(Rectangle.NO_BORDER);
					//hcell20.setPaddingLeft(40f);
					table3.addCell(hcell20);
					
					hcell20 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getUmr(), redFont1));
					hcell20.setBorder(Rectangle.NO_BORDER);
					//hcell20.setPaddingLeft(40f);
					table3.addCell(hcell20);

					

					PdfPCell hcell3;
					hcell3 = new PdfPCell(new Phrase("Patient Name", redFont1));
					hcell3.setBorder(Rectangle.NO_BORDER);
					hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell3.setPaddingLeft(-50f);
					table3.addCell(hcell3);

					hcell3 = new PdfPCell(new Phrase(":", redFont1));
					hcell3.setBorder(Rectangle.NO_BORDER);
					hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell3.setPaddingLeft(-50f);
					table3.addCell(hcell3);

					
					hcell3 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getFirstName()+" "+patientRegistration.getPatientDetails().getLastName(), redFont1));
					hcell3.setBorder(Rectangle.NO_BORDER);
					hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell3.setPaddingLeft(-50f);
					table3.addCell(hcell3);

					
					
					PdfPCell hcell30;
					hcell30 = new PdfPCell(new Phrase("S-W-B-D/O", redFont1));
					hcell30.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell30);

					
					hcell30 = new PdfPCell(new Phrase(":", redFont1));
					hcell30.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell30);

					
					hcell30 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getMotherName(), redFont1));//mother name if father name
					hcell30.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell30);

					
					
					//for DOA
					Timestamp timestamp2 = patientRegistration.getDateOfJoining();
					DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa ");

					Calendar calendar2 = Calendar.getInstance();
					calendar2.setTimeInMillis(timestamp2.getTime());

					String doa = dateFormat2.format(calendar2.getTime());
					
					
					
					
					PdfPCell hcell4;
					hcell4 = new PdfPCell(new Phrase("DOA", redFont1));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell4.setPaddingLeft(-50f);
					table3.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(":", redFont1));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell4.setPaddingLeft(-50f);
					table3.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(String.valueOf(doa), redFont1));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell4.setPaddingLeft(-50f);
					table3.addCell(hcell4);

				
		
					PdfPCell hcell40;
					hcell40 = new PdfPCell(new Phrase("Admitted Ward", redFont1));
					hcell40.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell40);
					
					hcell40 = new PdfPCell(new Phrase(":", redFont1));
					hcell40.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell40);
					
					hcell40 = new PdfPCell(new Phrase(admittedWard, redFont1));
					hcell40.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell40);

					

					PdfPCell hcell5;
					hcell5 = new PdfPCell(new Phrase("Consultant", redFont1));
					hcell5.setBorder(Rectangle.NO_BORDER);
					hcell5.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell5.setPaddingLeft(-50f);
					table3.addCell(hcell5);
					
					hcell5 = new PdfPCell(new Phrase(":", redFont1));
					hcell5.setBorder(Rectangle.NO_BORDER);
					hcell5.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell5.setPaddingLeft(-50f);
					table3.addCell(hcell5);
					
					hcell5 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getConsultant(), redFont1));
					hcell5.setBorder(Rectangle.NO_BORDER);
					hcell5.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell5.setPaddingLeft(-50f);
					table3.addCell(hcell5);

					
					
					PdfPCell hcell50;
					hcell50 = new PdfPCell(new Phrase("Department", redFont1));
					hcell50.setBorder(Rectangle.NO_BORDER);
					//hcell50.setPaddingLeft(40f);
					table3.addCell(hcell50);
					
					hcell50 = new PdfPCell(new Phrase(":", redFont1));
					hcell50.setBorder(Rectangle.NO_BORDER);
					//hcell50.setPaddingLeft(40f);
					table3.addCell(hcell50);
					
					hcell50 = new PdfPCell(new Phrase(dpt, redFont1));
					hcell50.setBorder(Rectangle.NO_BORDER);
					//hcell50.setPaddingLeft(40f);
					table3.addCell(hcell50);

					
					
					PdfPCell hcell6;
					hcell6 = new PdfPCell(new Phrase("Registration No", redFont1));
					hcell6.setBorder(Rectangle.NO_BORDER);
					hcell6.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell6.setPaddingLeft(-50f);
					table3.addCell(hcell6);
					
					hcell6 = new PdfPCell(new Phrase(":", redFont1));
					hcell6.setBorder(Rectangle.NO_BORDER);
					hcell6.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell6.setPaddingLeft(-50f);
					table3.addCell(hcell6);
					
					hcell6 = new PdfPCell(new Phrase(patientRegistration.getRegId(), redFont1));
					hcell6.setBorder(Rectangle.NO_BORDER);
					hcell6.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell6.setPaddingLeft(-50f);
					table3.addCell(hcell6);

					
					
					PdfPCell hcell60;
					hcell60 = new PdfPCell(new Phrase("Age/Sex", redFont1));
					hcell60.setBorder(Rectangle.NO_BORDER);
					//hcell60.setPaddingLeft(40f);
					table3.addCell(hcell60);
					
					hcell60 = new PdfPCell(new Phrase(":", redFont1));
					hcell60.setBorder(Rectangle.NO_BORDER);
					//hcell60.setPaddingLeft(40f);
					table3.addCell(hcell60);
					
					hcell60 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getAge()+"/"+patientRegistration.getPatientDetails().getGender(), redFont1));
					hcell60.setBorder(Rectangle.NO_BORDER);
					//hcell60.setPaddingLeft(40f);
					table3.addCell(hcell60);

					
					PdfPCell hcell7;
					hcell7 = new PdfPCell(new Phrase("Address", redFont1));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell7.setPaddingLeft(-50f);
					table3.addCell(hcell7);
					
					hcell7 = new PdfPCell(new Phrase(":", redFont1));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell7.setPaddingLeft(-50f);
					table3.addCell(hcell7);
					
					hcell7 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getAddress() , redFont1));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell7.setPaddingLeft(-50f);
					table3.addCell(hcell7);

					String refName=null;
					if(patientRegistration.getPatientDetails().getvRefferalDetails()==null)
					{
						refName="";
					}
					else
					{
						refName=patientRegistration.getPatientDetails().getvRefferalDetails().getRefName();
					}
					
					PdfPCell hcell70;
					hcell70 = new PdfPCell(new Phrase("Referal Name", redFont1));
					hcell70.setBorder(Rectangle.NO_BORDER);
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell70);

					
					hcell70 = new PdfPCell(new Phrase(":", redFont1));
					hcell70.setBorder(Rectangle.NO_BORDER);
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell70);

					
					hcell70 = new PdfPCell(new Phrase(refName, redFont1));
					hcell70.setBorder(Rectangle.NO_BORDER);
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell70);

					
					PdfPCell hcell701;
					hcell701 = new PdfPCell(new Phrase("Discharge Date", redFont1));
					hcell701.setBorder(Rectangle.NO_BORDER);
					hcell701.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell701.setPaddingLeft(-50f);
					
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell701);
					
					hcell701 = new PdfPCell(new Phrase(":", redFont1));
					hcell701.setBorder(Rectangle.NO_BORDER);
					hcell701.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell701.setPaddingLeft(-50f);
					
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell701);
					
					hcell701 = new PdfPCell(new Phrase(today, redFont1));
					hcell701.setBorder(Rectangle.NO_BORDER);
					hcell701.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell701.setPaddingLeft(-50f);
					
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell701);

				
					
					PdfPCell hcell7011;
					hcell7011 = new PdfPCell(new Phrase("Procedure", redFont1));
					hcell7011.setBorder(Rectangle.NO_BORDER);
					//hcell7011.setHorizontalAlignment(Element.ALIGN_LEFT);
				//	hcell7011.setPaddingLeft(-50f);
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell7011);

					hcell7011 = new PdfPCell(new Phrase(":", redFont1));
					hcell7011.setBorder(Rectangle.NO_BORDER);
					//hcell7011.setHorizontalAlignment(Element.ALIGN_LEFT);
				//	hcell7011.setPaddingLeft(-50f);
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell7011);

					hcell7011 = new PdfPCell(new Phrase(chargeBill.getProcedure(), redFont1));
					hcell7011.setBorder(Rectangle.NO_BORDER);
					//hcell7011.setHorizontalAlignment(Element.ALIGN_LEFT);
				//	hcell7011.setPaddingLeft(-50f);
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell7011);

					/*cell19.setColspan(2);
					cell19.addElement(table3);
					table.addCell(cell19);
					*/
					cell1.addElement(table3);
					
					/*
					PdfPCell cell20 = new PdfPCell();
					cell20.setBorder(Rectangle.NO_BORDER);
					cell20.setPaddingTop(10f);*/

					
					PdfPTable table1 = new PdfPTable(7);
					table1.setWidths(new float[] { 4f, 10f,3f, 2.5f,3f,3f,3f });
					table1.setSpacingBefore(10);
					table1.setWidthPercentage(105f);

					PdfPCell hcell;
					
					hcell = new PdfPCell(new Phrase("Service Code", headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("Service/Investigation" , headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(hcell);
					
					hcell = new PdfPCell(new Phrase("Date", headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(7);
					table1.addCell(hcell);
					
					
					
					
					hcell = new PdfPCell(new Phrase("Qty", headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(18);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("Rate" , headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(15);
					table1.addCell(hcell);
					

					hcell = new PdfPCell(new Phrase("Disc" , headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(15);
					table1.addCell(hcell);
					
					hcell = new PdfPCell(new Phrase("Amt" , headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(30);
					table1.addCell(hcell);
					
		
					
					table1.addCell(hcell);
					
					
					/*cell20.setColspan(2);
					cell20.addElement(table1);
					table.addCell(cell20);*/
					cell1.addElement(table1);
					
					/*PdfPCell cell21 = new PdfPCell();
					cell21.setBorder(Rectangle.NO_BORDER);*/

					// Calculating total
					float total = 0;
					float totalMed=0;
					float totalServiceAmt=0;
					float totalAccmAmt=0;
					float totalEqAmt=0;
					float totalconAmt=0;
					for (ChargeBill chargeBillInfo : chargeBillListLab) 
					{
						if (chargeBillInfo.getLabId() != null) 
						{
							
							if(chargeBillInfo.getNetAmount()!=0) {
							chargeBillInfo.setServiceName(chargeBillInfo.getLabId().getServiceName());
							
							total+=chargeBillInfo.getNetAmount();
							}
							
							}
											}
					
					for (ChargeBill chargeMedicine : chargeBillListSale) 
					{
						if (chargeMedicine.getSaleId() != null) 
						{
							
							if(chargeMedicine.getNetAmount()!=0) {
								totalMed+=chargeMedicine.getNetAmount();
							}
							
						}
						
						
					}
					
					for (ChargeBill chargeBillInfo : chargeBillListService) 
					{

						
							if (chargeBillInfo.getServiceId() != null) 
						{
								if(chargeBillInfo.getServiceId().getServiceType().equalsIgnoreCase("OTHER"))
								{
							if(chargeBillInfo.getNetAmount()!=0) {	

								totalServiceAmt+=chargeBillInfo.getNetAmount();
							}
								}
								if(chargeBillInfo.getServiceId().getServiceType().equalsIgnoreCase("Lab"))
								{
									totalServiceAmt+=chargeBillInfo.getNetAmount();
									
								}
								}
						}
					
					
					for (ChargeBill chargeBillInfo1 : chargeBillListService) 
					{

						
							if (chargeBillInfo1.getServiceId() != null) 
						{
								if(chargeBillInfo1.getServiceId().getServiceType().equalsIgnoreCase("WARD CHARGES"))
								{
							if(chargeBillInfo1.getNetAmount()!=0)
							{	
								totalAccmAmt+=chargeBillInfo1.getNetAmount();
							}
								}
						}
					}
					
					for (ChargeBill chargeBillInfo1 : chargeBillListService) 
					{

						
							if (chargeBillInfo1.getServiceId() != null) 
						{
								if(chargeBillInfo1.getServiceId().getServiceType().equalsIgnoreCase("EQUIPMENT CHARGES"))
								{
							if(chargeBillInfo1.getNetAmount()!=0)
							{	
								totalEqAmt+=chargeBillInfo1.getNetAmount();
							}
								}
						}
					}
					
					

					
					for (ChargeBill chargeBillInfo1 : chargeBillListService) 
					{

						
							if (chargeBillInfo1.getServiceId() != null) 
						{
								if(chargeBillInfo1.getServiceId().getServiceType().equalsIgnoreCase("CONSULTATION CHARGES"))
								{
							if(chargeBillInfo1.getNetAmount()!=0)
							{	
								totalconAmt+=chargeBillInfo1.getNetAmount();
							}
								}
						}
					}
					
					
					PdfPTable table11 = new PdfPTable(7);
					table11.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f});
					table11.setSpacingBefore(10);
					table11.setWidthPercentage(105f);

					PdfPCell hcell01;
					if(!chargeBillListLab.isEmpty())
					{
					hcell01 = new PdfPCell(new Phrase("LAB CHARGES", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					
					hcell01 = new PdfPCell(new Phrase(String.valueOf(total), headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell01.setPaddingRight(10);
					table11.addCell(hcell01);
					
					PdfPCell cell;
					for (ChargeBill chargeBillInfo : chargeBillListLab) 
					{
					
					if (chargeBillInfo.getLabId() != null) 
					
					{
						if(chargeBillInfo.getAmount()!=0) {
						
						
						chargeBillInfo.setServiceName(chargeBillInfo.getLabId().getServiceName());
						
						
						
						
						String from =chargeBillInfo.getLabId().getEnteredDate().toString(); 
						Timestamp timestamp = Timestamp.valueOf(from);
						DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(timestamp.getTime());

						String labDate = dateFormat.format(calendar.getTime());

						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getLabId().getLabServices().getServiceId()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(cell);

						cell = new PdfPCell(new Phrase(chargeBillInfo.getServiceName(), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(cell);
						
						cell = new PdfPCell(new Phrase(labDate , redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getQuantity()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table11.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getAmount()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table11.addCell(cell);
						
						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getDiscount()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table11.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getNetAmount()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setPaddingRight(10);
						table11.addCell(cell);
						
						

						//total += chargeBillInfo.getNetAmount();
						}
					} 					 

					}
					
					
					
					}
					else
					{
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell01.setPaddingRight(10);
						table11.addCell(hcell01);
						
					}
					
					
					
					
					
					int count = 0;
					/*cell21.setColspan(2);
					cell21.addElement(table11);
					table.addCell(cell21);*/
					
					cell1.addElement(table11);
					
					/*PdfPCell cell22 = new PdfPCell();
					cell22.setBorder(Rectangle.NO_BORDER);*/
					
					PdfPTable table12 = new PdfPTable(7);
					table12.setWidths(new float[] {7f, 10f,3f, 2.5f,3f,3f,3f});
					table12.setSpacingBefore(10);
					table12.setWidthPercentage(105f);
					
					PdfPTable table22 = new PdfPTable(7);
					table22.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f});
					table22.setSpacingBefore(10);
					table22.setWidthPercentage(105f);

					PdfPCell hcell011;
					if(!chargeBillListSale.isEmpty())
					{
					hcell011 = new PdfPCell(new Phrase("MEDICINE CHARGES", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
			
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase(String.valueOf(Math.round(totalMed)), headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell011.setPaddingRight(10);
					table12.addCell(hcell011);
					
					

					//charge bill
					for (ChargeBill chargeMedicine : chargeBillListSale) 
					{
						if (chargeMedicine.getSaleId() != null) 
						{
							
							
							if(chargeMedicine.getAmount()!=0) {

							/*salestAmount += chargeMedicine.getSaleId().getCostPrice();
							salesnetAmount += chargeMedicine.getNetAmount();
							salesDiscount = chargeMedicine.getSaleId().getDiscount();
							salesQuantity += chargeMedicine.getSaleId().getQuantity();*/
							
							
							salesnetAmount += chargeMedicine.getNetAmount();
							String from =chargeMedicine.getSaleId().getBillDate().toString(); 
							Timestamp timestamp = Timestamp.valueOf(from);
							DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(timestamp.getTime());

							String medicineDate = dateFormat.format(calendar.getTime());

							
							

							PdfPCell hcell0;
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(chargeMedicine.getSaleId().getPatientSalesMedicineDetails().getMedicineId()), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_LEFT);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(chargeMedicine.getSaleId().getMedicineName()), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_LEFT);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(medicineDate, redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_LEFT);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(chargeMedicine.getSaleId().getQuantity()), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(Math.round(chargeMedicine.getSaleId().getMrp())), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(Math.round(chargeMedicine.getSaleId().getDiscount())), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(Math.round(chargeMedicine.getNetAmount())), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell0.setPaddingRight(10);
							table22.addCell(hcell0);
				
							}

						}
					}
					
					
					}
					else
					{
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
				
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell011.setPaddingRight(10);
						table12.addCell(hcell011);
				
					}
					
					
					cell1.addElement(table12);
					
					
					cell1.addElement(table22);
														
				/*	PdfPCell cell23 = new PdfPCell();
					cell23.setBorder(Rectangle.NO_BORDER);*/
					
					
					PdfPTable table28 = new PdfPTable(7);
					table28.setWidths(new float[] {7f, 10f,3f, 2.5f,3f,3f,3f});
					table28.setSpacingBefore(10);
					table28.setWidthPercentage(105f);
					
					PdfPTable table13 = new PdfPTable(7);
					table13.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f});
					table13.setSpacingBefore(10);
					table13.setWidthPercentage(105f);
				
					
					

					PdfPCell hcell0111;
					if(!chargeBillListService.isEmpty())
					{
						if(chargeBillListService.stream().filter((s) -> s.getServiceId().getServiceType().equalsIgnoreCase("OTHER")).count()>0)
					{
					hcell0111 = new PdfPCell(new Phrase("SERVICE CHARGES", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_LEFT);
					table28.addCell(hcell0111);
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table28.addCell(hcell0111);
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table28.addCell(hcell0111);
					hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table28.addCell(hcell0111);
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					table28.addCell(hcell0111);
		
					
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					table28.addCell(hcell0111);
					
					hcell0111 = new PdfPCell(new Phrase(String.valueOf(totalServiceAmt), headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell0111.setPaddingRight(10);
					table28.addCell(hcell0111);
					
					
					for (ChargeBill chargeBillInfo : chargeBillListService) 
					{

						
							if (chargeBillInfo.getServiceId() != null && chargeBillInfo.getServiceId().getServiceType().equalsIgnoreCase("OTHER") || chargeBillInfo.getServiceId().getServiceType().equalsIgnoreCase("LAB")) 
						{
								
								
								if(chargeBillInfo.getAmount()!=0) {

								String from =chargeBillInfo.getInsertedDate().toString(); 
								Timestamp timestamp = Timestamp.valueOf(from);
								DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(timestamp.getTime());

								String serviceDate = dateFormat.format(calendar.getTime());

								

								
								PdfPCell cell11;
							chargeBillInfo.setServiceName(chargeBillInfo.getServiceId().getServiceName());

							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getServiceId().getServiceId()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table13.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(chargeBillInfo.getServiceName(), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table13.addCell(cell11);
							
							

							cell11 = new PdfPCell(new Phrase(serviceDate, redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table13.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getQuantity()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table13.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getMrp()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table13.addCell(cell11);
							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getDiscount()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table13.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getNetAmount()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell11.setPaddingRight(10);
							
							table13.addCell(cell11);

							

			
							//total += chargeBillInfo.getNetAmount();
							
								}
								}
						
						else 
						{
							chargeBillInfo.setServiceName("NOT APPLICABLE");
						}

					}
					
					}
					}
					else
					{
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_LEFT);
						table28.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table28.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table28.addCell(hcell0111);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table28.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						table28.addCell(hcell0111);
			
						
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						table28.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell0111.setPaddingRight(10);
						table28.addCell(hcell0111);

					}
					
					
				
					
					/*cell23.setColspan(2);
					cell23.addElement(table13);
					table.addCell(cell23);*/
					
					cell1.addElement(table28);
					cell1.addElement(table13);

					//----------------------------------
					PdfPTable table131 = new PdfPTable(7);
					table131.setWidths(new float[] {10f, 7f,3f,2.5f,3f,3f,3f});
					table131.setSpacingBefore(10);
					table131.setWidthPercentage(105f);
					
					PdfPTable table281 = new PdfPTable(7);
					table281.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f});
					table281.setSpacingBefore(10);
					table281.setWidthPercentage(105f);
					

					PdfPCell hcell01111;
					if(!chargeBillListService.isEmpty())
					{
					if(chargeBillListService.stream().filter((s) -> s.getServiceId().getServiceType().equalsIgnoreCase("WARD CHARGES")).count()>0)
					{
					hcell01111 = new PdfPCell(new Phrase("WARD CHARGES", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_LEFT);
					table131.addCell(hcell01111);
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table131.addCell(hcell01111);
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table131.addCell(hcell01111);
					hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table131.addCell(hcell01111);
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					table131.addCell(hcell01111);
		
					
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					table131.addCell(hcell01111);
					
					hcell01111 = new PdfPCell(new Phrase(String.valueOf(totalAccmAmt), headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell01111.setPaddingRight(10);
					table131.addCell(hcell01111);
					
					for (ChargeBill chargeBillInfo3 : chargeBillListService) 
					{

						
							if (chargeBillInfo3.getServiceId() != null) 
						{
								if(chargeBillInfo3.getServiceId().getServiceType().equalsIgnoreCase("WARD CHARGES"))
								{
							if(chargeBillInfo3.getNetAmount()!=0) {	

								String from =chargeBillInfo3.getInsertedDate().toString(); 
								Timestamp timestamp = Timestamp.valueOf(from);
								DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(timestamp.getTime());

								String serviceDate = dateFormat.format(calendar.getTime());

								

								
								PdfPCell cell11;
							chargeBillInfo3.setServiceName(chargeBillInfo3.getServiceId().getServiceName());

							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getServiceId().getServiceId()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table281.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(chargeBillInfo3.getServiceName(), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table281.addCell(cell11);
							
							

							cell11 = new PdfPCell(new Phrase(serviceDate, redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table281.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getQuantity()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table281.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getMrp()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table281.addCell(cell11);
							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getDiscount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table281.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getNetAmount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell11.setPaddingRight(10);
							
							table281.addCell(cell11);

							

			
						//	total += chargeBillInfo3.getNetAmount();
							}
								}
						} 
						else 
						{
							chargeBillInfo3.setServiceName("NOT APPLICABLE");
						}

					}
					}
					
					}
					else
					{
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_LEFT);
						table131.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table131.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table131.addCell(hcell01111);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table131.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						table131.addCell(hcell01111);
			
						
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						table131.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell01111.setPaddingRight(10);
						table131.addCell(hcell01111);
			
					}
				
					
					
					/*cell23.setColspan(2);
					cell23.addElement(table13);
					table.addCell(cell23);*/

					cell1.addElement(table131);
					cell1.addElement(table281);
					
					
					PdfPTable table132 = new PdfPTable(7);
					table132.setWidths(new float[] {10f, 7f,3f,2.5f,3f,3f,3f});
					table132.setSpacingBefore(10);
					table132.setWidthPercentage(105f);
					
					PdfPTable table282 = new PdfPTable(7);
					table282.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f});
					table282.setSpacingBefore(10);
					table282.setWidthPercentage(105f);

					PdfPCell hcell01112;
					if(!chargeBillListService.isEmpty())
					{
					if(chargeBillListService.stream().filter((s) -> s.getServiceId().getServiceType().equalsIgnoreCase("EQUIPMENT CHARGES")).count()>0)
					{
					hcell01112 = new PdfPCell(new Phrase("EQUIPMENT CHARGES", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_LEFT);
					table132.addCell(hcell01112);
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
					table132.addCell(hcell01112);
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
					table132.addCell(hcell01112);
					hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
					table132.addCell(hcell01112);
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					table132.addCell(hcell01112);
		
					
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					table132.addCell(hcell01112);
					
					hcell01112 = new PdfPCell(new Phrase(String.valueOf(totalEqAmt), headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell01112.setPaddingRight(10);
					table132.addCell(hcell01112);
					
					

					for (ChargeBill chargeBillInfo3 : chargeBillListService) 
					{

						
							if (chargeBillInfo3.getServiceId() != null) 
						{
								if(chargeBillInfo3.getServiceId().getServiceType().equalsIgnoreCase("EQUIPMENT CHARGES"))
								{
							if(chargeBillInfo3.getNetAmount()!=0) {	

								String from =chargeBillInfo3.getInsertedDate().toString(); 
								Timestamp timestamp = Timestamp.valueOf(from);
								DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(timestamp.getTime());

								String serviceDate = dateFormat.format(calendar.getTime());

								

								
								PdfPCell cell11;
							chargeBillInfo3.setServiceName(chargeBillInfo3.getServiceId().getServiceName());

							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getServiceId().getServiceId()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table282.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(chargeBillInfo3.getServiceName(), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table282.addCell(cell11);
							
							

							cell11 = new PdfPCell(new Phrase(serviceDate, redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table282.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getQuantity()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table282.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getMrp()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table282.addCell(cell11);
							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getDiscount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table282.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getNetAmount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell11.setPaddingRight(10);
							
							table282.addCell(cell11);

							

			
						//	total += chargeBillInfo3.getNetAmount();
							}
								}
						} 
						else 
						{
							chargeBillInfo3.setServiceName("NOT APPLICABLE");
						}

					}

					}
					}
					else
					{
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_LEFT);
						table132.addCell(hcell01112);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
						table132.addCell(hcell01112);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
						table132.addCell(hcell01112);
						hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
						table132.addCell(hcell01112);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						table132.addCell(hcell01112);
			
						
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						table132.addCell(hcell01112);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell01112.setPaddingRight(10);
						table132.addCell(hcell01112);

					}
				//-----------------------------------------
					
					
					
					/*cell23.setColspan(2);
					cell23.addElement(table13);
					table.addCell(cell23);*/

					cell1.addElement(table132);
					cell1.addElement(table282);

					PdfPTable table121 = new PdfPTable(7);
					table121.setWidths(new float[] {10f, 7f,3f,2.5f,3f,3f,3f});
					table121.setSpacingBefore(10);
					table121.setWidthPercentage(105f);
					
					PdfPTable table21 = new PdfPTable(7);
					table21.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f});
					table21.setSpacingBefore(10);
					table21.setWidthPercentage(105f);

					PdfPCell hcell21;
					if(!chargeBillListService.isEmpty())
					{
					if(chargeBillListService.stream().filter((s) -> s.getServiceId().getServiceType().equalsIgnoreCase("CONSULTATION CHARGES")).count()>0)
					{
					hcell21 = new PdfPCell(new Phrase("CONSULTATION CHARGES", headFont));
					hcell21.setBorder(Rectangle.NO_BORDER);
					hcell21.setHorizontalAlignment(Element.ALIGN_LEFT);
					table121.addCell(hcell21);
					
					hcell21 = new PdfPCell(new Phrase("", headFont));
					hcell21.setBorder(Rectangle.NO_BORDER);
					hcell21.setHorizontalAlignment(Element.ALIGN_CENTER);
					table121.addCell(hcell21);
					
					hcell21 = new PdfPCell(new Phrase("", headFont));
					hcell21.setBorder(Rectangle.NO_BORDER);
					hcell21.setHorizontalAlignment(Element.ALIGN_CENTER);
					table121.addCell(hcell21);
					hcell21.setHorizontalAlignment(Element.ALIGN_CENTER);
					
					hcell21 = new PdfPCell(new Phrase("", headFont));
					hcell21.setBorder(Rectangle.NO_BORDER);
					hcell21.setHorizontalAlignment(Element.ALIGN_CENTER);
					table121.addCell(hcell21);
					
					hcell21 = new PdfPCell(new Phrase("", headFont));
					hcell21.setBorder(Rectangle.NO_BORDER);
					table121.addCell(hcell21);
		
					
					
					hcell21 = new PdfPCell(new Phrase("", headFont));
					hcell21.setBorder(Rectangle.NO_BORDER);
					table121.addCell(hcell21);
					
					hcell21 = new PdfPCell(new Phrase(String.valueOf(totalconAmt), headFont));
					hcell21.setBorder(Rectangle.NO_BORDER);
					hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell21.setPaddingRight(10);
					table121.addCell(hcell21);
					
					

					for (ChargeBill chargeBillInfo3 : chargeBillListService) 
					{

						
							if (chargeBillInfo3.getServiceId() != null) 
						{
								if(chargeBillInfo3.getServiceId().getServiceType().equalsIgnoreCase("CONSULTATION CHARGES"))
								{
							if(chargeBillInfo3.getNetAmount()!=0) {	

								String from =chargeBillInfo3.getInsertedDate().toString(); 
								Timestamp timestamp = Timestamp.valueOf(from);
								DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(timestamp.getTime());

								String serviceDate = dateFormat.format(calendar.getTime());

								

								
								PdfPCell cell11;
							chargeBillInfo3.setServiceName(chargeBillInfo3.getServiceId().getServiceName());

							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getServiceId().getServiceId()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table21.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(chargeBillInfo3.getServiceName(), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table21.addCell(cell11);
							
							

							cell11 = new PdfPCell(new Phrase(serviceDate, redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table21.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getQuantity()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table21.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getMrp()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table21.addCell(cell11);
							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getDiscount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table21.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getNetAmount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell11.setPaddingRight(10);
							
							table21.addCell(cell11);

							

			
						//	total += chargeBillInfo3.getNetAmount();
							}
								}
						} 
						else 
						{
							chargeBillInfo3.setServiceName("NOT APPLICABLE");
						}

					}

					}
					}
					else
					{
						hcell21 = new PdfPCell(new Phrase("", headFont));
						hcell21.setBorder(Rectangle.NO_BORDER);
						hcell21.setHorizontalAlignment(Element.ALIGN_LEFT);
						table121.addCell(hcell21);
						
						hcell21 = new PdfPCell(new Phrase("", headFont));
						hcell21.setBorder(Rectangle.NO_BORDER);
						hcell21.setHorizontalAlignment(Element.ALIGN_CENTER);
						table121.addCell(hcell21);
						
						hcell21 = new PdfPCell(new Phrase("", headFont));
						hcell21.setBorder(Rectangle.NO_BORDER);
						hcell21.setHorizontalAlignment(Element.ALIGN_CENTER);
						table121.addCell(hcell21);
						hcell21.setHorizontalAlignment(Element.ALIGN_CENTER);
						
						hcell21 = new PdfPCell(new Phrase("", headFont));
						hcell21.setBorder(Rectangle.NO_BORDER);
						hcell21.setHorizontalAlignment(Element.ALIGN_CENTER);
						table121.addCell(hcell21);
						
						hcell21 = new PdfPCell(new Phrase("", headFont));
						hcell21.setBorder(Rectangle.NO_BORDER);
						table121.addCell(hcell21);
			
						
						
						hcell21 = new PdfPCell(new Phrase("", headFont));
						hcell21.setBorder(Rectangle.NO_BORDER);
						table121.addCell(hcell21);
						
						hcell21 = new PdfPCell(new Phrase("", headFont));
						hcell21.setBorder(Rectangle.NO_BORDER);
						hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell21.setPaddingRight(10);
						table121.addCell(hcell21);

					}
					
					
					
					
				//-----------------------------------------
					
					
					
					/*cell23.setColspan(2);
					cell23.addElement(table13);
					table.addCell(cell23);*/

					cell1.addElement(table121);
					cell1.addElement(table21);

					
					
					PdfPTable table182 = new PdfPTable(1);
					table182.setWidths(new float[] {5f});
					table182.setSpacingBefore(10);
					table182.setWidthPercentage(100f);
					
					PdfPCell hcell072;
					hcell072 = new PdfPCell(new Phrase("_________________________________________________________________________", headFont));
					hcell072.setBorder(Rectangle.NO_BORDER);
					hcell072.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table182.addCell(hcell072);
					
					float totalAmount=0;
					totalAmount=total+totalMed+totalServiceAmt+totalAccmAmt+totalEqAmt+totalconAmt;
					
					/*PdfPCell cell24 = new PdfPCell();
					cell24.setBorder(Rectangle.NO_BORDER);
					*/
					PdfPTable table14 = new PdfPTable(5);
					table14.setWidths(new float[] {2f, 1f,10f, 5f,5f});
					table14.setSpacingBefore(10);
					table14.setWidthPercentage(100f);
					
					
				
					

					PdfPCell hcell02;
					
					hcell02 = new PdfPCell(new Phrase("", headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell02);
					
					hcell02 = new PdfPCell(new Phrase("", headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell02);
					
					hcell02 = new PdfPCell(new Phrase("NET AMOUNT", headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_RIGHT);
				    hcell02.setPaddingRight(-51f);
					table14.addCell(hcell02);
					
					hcell02 = new PdfPCell(new Phrase(":", headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell02);
					
					hcell02 = new PdfPCell(new Phrase(String.valueOf(Math.round(totalAmount)), headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell02);
					
					
					PdfPCell hcell021;
					
					hcell021 = new PdfPCell(new Phrase("", headFont));
					hcell021.setBorder(Rectangle.NO_BORDER);
					hcell021.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell021);
					
					hcell021 = new PdfPCell(new Phrase("", headFont));
					hcell021.setBorder(Rectangle.NO_BORDER);
					hcell021.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell021);
					
					hcell021 = new PdfPCell(new Phrase("PAID AMOUNT", headFont));
					hcell021.setBorder(Rectangle.NO_BORDER);
					hcell021.setHorizontalAlignment(Element.ALIGN_RIGHT);
				    hcell021.setPaddingRight(-55f);
					table14.addCell(hcell021);
					
					hcell021 = new PdfPCell(new Phrase(":", headFont));
					hcell021.setBorder(Rectangle.NO_BORDER);
					hcell021.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell021);
					
					hcell021 = new PdfPCell(new Phrase(String.valueOf(Math.round(totaPaidAmount)), headFont));
					hcell021.setBorder(Rectangle.NO_BORDER);
					hcell021.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell021);
	
					
					
					PdfPCell hcell03;
					
					hcell03 = new PdfPCell(new Phrase("", headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell03);
					
					hcell03 = new PdfPCell(new Phrase("", headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell03);
					
					hcell03 = new PdfPCell(new Phrase("ADVANCE AMOUNT", headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell03.setPaddingRight(-81f);
					table14.addCell(hcell03);
					
					hcell03 = new PdfPCell(new Phrase(":", headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell03);
					
					hcell03 = new PdfPCell(new Phrase(String.valueOf(patientRegistration.getAdvanceAmount()), headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell03);
					
					
                    PdfPCell hcell031;
					
					hcell031 = new PdfPCell(new Phrase("", headFont));
					hcell031.setBorder(Rectangle.NO_BORDER);
					hcell031.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell031);
					
					hcell031 = new PdfPCell(new Phrase("", headFont));
					hcell031.setBorder(Rectangle.NO_BORDER);
					hcell031.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell031);
					
					hcell031 = new PdfPCell(new Phrase("SETTLED AMOUNT", headFont));
					hcell031.setBorder(Rectangle.NO_BORDER);
					hcell031.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell031.setPaddingRight(-77f);
					table14.addCell(hcell031);
					
					hcell031 = new PdfPCell(new Phrase(":", headFont));
					hcell031.setBorder(Rectangle.NO_BORDER);
					hcell031.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell031);
					
					hcell031 = new PdfPCell(new Phrase(String.valueOf(amount), headFont));
					hcell031.setBorder(Rectangle.NO_BORDER);
					hcell031.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell031);

					
                   PdfPCell hcell0311;
					
					hcell0311 = new PdfPCell(new Phrase("", headFont));
					hcell0311.setBorder(Rectangle.NO_BORDER);
					hcell0311.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell0311);
					
					hcell0311 = new PdfPCell(new Phrase("", headFont));
					hcell0311.setBorder(Rectangle.NO_BORDER);
					hcell0311.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell0311);
					
					hcell0311 = new PdfPCell(new Phrase("DISCOUNT AMOUNT", headFont));
					hcell0311.setBorder(Rectangle.NO_BORDER);
					hcell0311.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell0311.setPaddingRight(-83f);
					table14.addCell(hcell0311);
					
					hcell0311 = new PdfPCell(new Phrase(":", headFont));
					hcell0311.setBorder(Rectangle.NO_BORDER);
					hcell0311.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell0311);
					
					hcell0311 = new PdfPCell(new Phrase(String.valueOf(chargeBill.getDiscount()), headFont));
					hcell0311.setBorder(Rectangle.NO_BORDER);
					hcell0311.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell0311);
					

					 PdfPCell hcell03111;
						
						hcell03111 = new PdfPCell(new Phrase("", headFont));
						hcell03111.setBorder(Rectangle.NO_BORDER);
						hcell03111.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell03111);
						
						hcell03111 = new PdfPCell(new Phrase("", headFont));
						hcell03111.setBorder(Rectangle.NO_BORDER);
						hcell03111.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell03111);
						
						hcell03111 = new PdfPCell(new Phrase("RETURN AMOUNT", headFont));
						hcell03111.setBorder(Rectangle.NO_BORDER);
						hcell03111.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell03111.setPaddingRight(-72f);
						table14.addCell(hcell03111);
						
						hcell03111 = new PdfPCell(new Phrase(":", headFont));
						hcell03111.setBorder(Rectangle.NO_BORDER);
						hcell03111.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table14.addCell(hcell03111);
						
						hcell03111 = new PdfPCell(new Phrase(String.valueOf(chargeBill.getReturnAmount()), headFont));
						hcell03111.setBorder(Rectangle.NO_BORDER);
						hcell03111.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table14.addCell(hcell03111);
					
				//	float dueAmt = total - patientRegistration.getAdvanceAmount()+salesnetAmount;
					float dueAmt = totalAmount- patientRegistration.getAdvanceAmount()-chargeBill.getDiscount()+chargeBill.getReturnAmount()-totaPaidAmount-amount;
					
					
					PdfPCell hcell04;
					
					hcell04 = new PdfPCell(new Phrase("", headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase("", headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase("DUE AMOUNT", headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell04.setPaddingRight(-51f);
					table14.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase(":", headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(dueAmt)), headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell04);
					
					/*cell24.setColspan(2);
					cell24.addElement(table182);
					cell24.addElement(table14);
					table.addCell(cell24);*/
					
					cell1.addElement(table182);
					cell1.addElement(table14);
					
					/*PdfPCell cell25 = new PdfPCell();
					cell25.setBorder(Rectangle.NO_BORDER);*/
				
					PdfPTable table15 = new PdfPTable(1);
					table15.setWidths(new float[] {5f});
					table15.setSpacingBefore(10);
					table15.setWidthPercentage(100f);
					
					PdfPCell hcell05;
					hcell05 = new PdfPCell(new Phrase("\n \nADVANCE RECEIPT DETAILS", headFont));
					hcell05.setBorder(Rectangle.NO_BORDER);
					table15.addCell(hcell05);
					
					/*cell25.setColspan(2);
					cell25.addElement(table15);
					table.addCell(cell25);*/
					
					cell1.addElement(table15);
					
					/*PdfPCell cell26 = new PdfPCell();
					cell26.setBorder(Rectangle.NO_BORDER);*/
					
					PdfPTable table16 = new PdfPTable(6);
					table16.setWidths(new float[] {5f,6f,5f,6f,4f,4f});
					table16.setSpacingBefore(10);
					table16.setWidthPercentage(100f);
					
					PdfPCell hcell06;
					hcell06 = new PdfPCell(new Phrase("Receipt No", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					hcell06 = new PdfPCell(new Phrase("Receipt Date", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					hcell06 = new PdfPCell(new Phrase("Receipt Name", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					hcell06 = new PdfPCell(new Phrase("Mode Of Payment", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
				
					hcell06 = new PdfPCell(new Phrase("Receipt Amt", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					hcell06 = new PdfPCell(new Phrase("Remarks", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					for (PatientPayment patientPaymentInfo : patientPayment) 
					{
						
						Timestamp timestamp =patientPaymentInfo.getInsertedDate(); 
						DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(timestamp.getTime());
					    String from=  dateFormat.format(calendar.getTime());
					   
						if (patientPaymentInfo.getTypeOfCharge().equalsIgnoreCase("ADVANCE")||patientPaymentInfo.getTypeOfCharge().equalsIgnoreCase(TYPE_OF_CHARGE_SETTLED_AMOUNT)) 
						{
							PdfPCell cell11;

							cell11 = new PdfPCell(new Phrase(String.valueOf(patientPaymentInfo.getPaymentId()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table16.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(from, redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table16.addCell(cell11);
	
							cell11 = new PdfPCell(new Phrase(patientPaymentInfo.getTypeOfCharge(), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table16.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(patientPaymentInfo.getModeOfPaymant()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table16.addCell(cell11);
	
							cell11 = new PdfPCell(new Phrase(String.valueOf(patientPaymentInfo.getAmount()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell11.setPaddingRight(23f);
							table16.addCell(cell11);
						
							cell11 = new PdfPCell(new Phrase("", redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table16.addCell(cell11);

							totalRecieptAmt=totalRecieptAmt+patientPaymentInfo.getAmount();
						}

					}
					/*cell26.setColspan(2);
					cell26.addElement(table16);
					table.addCell(cell26);
					*/
					cell1.addElement(table16);
					
					PdfPTable table18 = new PdfPTable(1);
					table18.setWidths(new float[] {5f});
					table18.setSpacingBefore(10);
					table18.setWidthPercentage(100f);
					
					PdfPCell hcell071;
					hcell071 = new PdfPCell(new Phrase("_________________________________________________________________________", headFont));
					hcell071.setBorder(Rectangle.NO_BORDER);
					hcell071.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table18.addCell(hcell071);
					
					
					
					/*PdfPCell cell27 = new PdfPCell();
					cell27.setBorder(Rectangle.NO_BORDER);*/
					
					PdfPTable table17 = new PdfPTable(6);
					table17.setWidths(new float[] {5f,6f,5f,6f,4f,4f});
					table17.setSpacingBefore(10);
					table17.setWidthPercentage(100f);
					
					PdfPCell hcell07;
					hcell07 = new PdfPCell(new Phrase("", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);
					
					hcell07 = new PdfPCell(new Phrase("", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);
					
					hcell07 = new PdfPCell(new Phrase("Total : ", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);
					
					hcell07 = new PdfPCell(new Phrase("", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);
					
					hcell07 = new PdfPCell(new Phrase(String.valueOf(totalRecieptAmt), headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					hcell07.setHorizontalAlignment(Element.ALIGN_CENTER);
					table17.addCell(hcell07);
				
					
					hcell07 = new PdfPCell(new Phrase("", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);

					/*cell27.setColspan(2);
					cell27.addElement(table18);
					cell27.addElement(table17);
					table.addCell(cell27);
					*/
					cell1.addElement(table18);
					cell1.addElement(table17);
					
					/*PdfPCell cell28 = new PdfPCell();
					cell28.setBorder(Rectangle.NO_BORDER);*/
				
					PdfPTable table181 = new PdfPTable(2);
					table181.setWidths(new float[] {8f,8f});
					table181.setSpacingBefore(10);
					table181.setWidthPercentage(100f);
					
					PdfPCell hcell08;
					hcell08 = new PdfPCell(new Phrase("Total Received Amount In Words : ", headFont));
					hcell08.setBorder(Rectangle.NO_BORDER);
					hcell08.setPaddingTop(10f);
					table181.addCell(hcell08);
					
					hcell08 = new PdfPCell(new Phrase(numberToWordsConverter.convert(Math.round(totalAmount)) + " Rupees Only", redFont1));
					hcell08.setBorder(Rectangle.NO_BORDER);
					hcell08.setPaddingTop(10f);
					hcell08.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell08.setPaddingLeft(-50f);
					table181.addCell(hcell08);
					
					
					PdfPCell hcell081;
					hcell081 = new PdfPCell(new Phrase("Net Amount In Words : ", headFont));
					hcell081.setBorder(Rectangle.NO_BORDER);
					hcell081.setPaddingTop(10f);
					table181.addCell(hcell081);
					
					hcell081 = new PdfPCell(new Phrase(numberToWordsConverter.convert(Math.round(totalAmount)) + " Rupees Only", redFont1));
					
					hcell081.setBorder(Rectangle.NO_BORDER);
					hcell081.setPaddingTop(10f);
					hcell081.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell081.setPaddingLeft(-50f);
					table181.addCell(hcell081);
					
				/*	cell28.setColspan(2);
					cell28.addElement(table181);
					table.addCell(cell28);*/
					
					cell1.addElement(table181);
					
					
					/*
					PdfPCell cell30 = new PdfPCell();
					cell30.setBorder(Rectangle.NO_BORDER);
				*/
					PdfPTable table20 = new PdfPTable(1);
					table20.setWidths(new float[] {8f});
					table20.setSpacingBefore(10);
					table20.setWidthPercentage(100f);
					
					PdfPCell hcell09;
					hcell09 = new PdfPCell(new Phrase("(Authorized Signature)", headFont));
					hcell09.setBorder(Rectangle.NO_BORDER);
					hcell09.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell09.setPaddingTop(20f);
					table20.addCell(hcell09);
					
					/*cell30.setColspan(2);
					cell30.addElement(table20);
					table.addCell(cell30);*/
					
					cell1.addElement(table20);
				
					
					/*PdfPCell cell29 = new PdfPCell();
					cell29.setBorder(Rectangle.NO_BORDER);*/
					
					PdfPTable table33 = new PdfPTable(4);
					table33.setWidthPercentage(100.0f);
					table33.setWidths(new int[] {3, 4, 3, 4});
					table33.setSpacingBefore(10);

					
					PdfPCell hcell16;
					hcell16 = new PdfPCell(new Phrase("Created By                    :  ", headFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setPaddingLeft(-10f);
					table33.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(createdName , redFont1));
					hcell16.setBorder(Rectangle.NO_BORDER);
					//hcell16.setPaddingRight(-70f);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					table33.addCell(hcell16);
					
					hcell16 = new PdfPCell(new Phrase("Created Dt                   : " , headFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setPaddingLeft(-20f);
					table33.addCell(hcell16);
					
					hcell16 = new PdfPCell(new Phrase(today , redFont1));
					hcell16.setBorder(Rectangle.NO_BORDER);
					//hcell16.setPaddingRight(-70f);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					table33.addCell(hcell16);
					
					
					
					PdfPCell hcell161;
					hcell161 = new PdfPCell(new Phrase("Printed By                     :  ", headFont));
					hcell161.setBorder(Rectangle.NO_BORDER);
					hcell161.setPaddingLeft(-10f);
					table33.addCell(hcell161);

					hcell161 = new PdfPCell(new Phrase(createdName , redFont1));
					hcell161.setBorder(Rectangle.NO_BORDER);
					//hcell161.setPaddingRight(-70f);
					hcell161.setHorizontalAlignment(Element.ALIGN_LEFT);
					table33.addCell(hcell161);
					
					hcell161 = new PdfPCell(new Phrase("Printed Dt                    : " , headFont));
					hcell161.setBorder(Rectangle.NO_BORDER);
					hcell161.setPaddingLeft(-20f);
					table33.addCell(hcell161);
					
					hcell161 = new PdfPCell(new Phrase(today , redFont1));
					hcell161.setBorder(Rectangle.NO_BORDER);
					//hcell161.setPaddingRight(-70f);
					hcell161.setHorizontalAlignment(Element.ALIGN_LEFT);
					table33.addCell(hcell161);

					
					/*cell29.setColspan(2);
					cell29.addElement(table33);
					table.addCell(cell29);*/
					
					cell1.addElement(table33);
					table.addCell(cell1);
					
					document.add(table);

					document.close();

				
				System.out.println("finished");

				pdfByte = byteArrayOutputStream1.toByteArray();
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
						.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();

				salesPaymentPdf = new SalesPaymentPdf();
				salesPaymentPdf.setFileName(regId+EMPTY_SPACE+"Final Bill");
				salesPaymentPdf.setFileuri(uri);
				salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
				salesPaymentPdf.setData(pdfByte);
				salesPaymentPdfServiceImpl.save(salesPaymentPdf);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			
		
			
			
		return salesPaymentPdf;	
		
	
	}
	
	
	/*
	 * Approximate bill 
	 * 	  
	 */
	@Transactional
	@RequestMapping(value="/approximate/{id}",method=RequestMethod.POST)
	public SalesPaymentPdf approximateBill(@PathVariable String id,Principal principal)
	{
		PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(id);
		if(patientRegistration.getpType().equalsIgnoreCase(ConstantValues.OUTPATIENT))
		{
			throw new RuntimeException(ConstantValues.OUTPATIENT_NOT_ALLOWED_ERROR_MSG);
		}
		String pdfBill=null;
		String regId=id;
		//createdBy Security
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		String createdBy=userSecurity.getUserId();
		String createdName=null;
		
		String mn=userSecurity.getMiddleName();
		
		if(mn==null)
		{
			createdName=userSecurity.getFirstName()+" "+userSecurity.getLastName();
		}
		else
		{
			createdName=userSecurity.getFirstName()+" "+userSecurity.getMiddleName()+" "+userSecurity.getLastName();
		}
	
		
		long mob=patientRegistration.getPatientDetails().getMobile();
		List<ChargeBill> chargeBillListq=chargeBillServiceImpl.findByPatRegId(patientRegistration);
		pdfBill=chargeBillListq.get(0).getBillNo();
		String billNoo=null;
			//*************************//
		
		//shantharam addr
		String newAddress="";
		
		List<ChargeBill> chargeBillList=chargeBillServiceImpl.findByPatRegIdAndPaid(patientRegistrationServiceImpl.findByRegId(id),"NO");
	
		
		
		String billNo=null;
		String patientName=null;
		String tokenNo=null;
		long paidSum=0;
		
		
			for(ChargeBill chargeBillInfo:chargeBillList)
			{
	    		//chargeBillInfo.setPaid("YES");
	    		patientName=chargeBillInfo.getPatRegId().getPatientDetails().getFirstName()+" "+chargeBillInfo.getPatRegId().getPatientDetails().getLastName();
	    		tokenNo=chargeBillInfo.getPatRegId().getRegId().substring(2);
	    		billNo=chargeBillInfo.getBillNo();
	    		if(chargeBillInfo.getPaid().equalsIgnoreCase("Yes"))
	    		{
	    			paidSum+=chargeBillInfo.getNetAmount();
	    		}
	    	//	chargeBillServiceImpl.save(chargeBillInfo);
	    	}
			
			Set<PatientPayment> patientPayment = patientRegistration.getPatientPayment();
			
			long totalRecieptAmt=0;

			
			//for room details
			String admittedWard = null;

			float salestAmount = 0;
			float salesnetAmount = 0;
			float salesDiscount = 0;
			long salesQuantity = 0;
			
			List<RoomBookingDetails> roomBookingDetails = patientRegistration.getRoomBookingDetails();

			for (RoomBookingDetails roomBookingDetailsInfo : roomBookingDetails) {
				RoomDetails roomDetails = roomBookingDetailsInfo.getRoomDetails();
				admittedWard = roomDetails.getRoomType();
			}
			
			String adWrd=null;
			if(admittedWard!=null)
			{
				adWrd=admittedWard;
			}
			else
			{
				adWrd="";
			}
			
			Date date = Calendar.getInstance().getTime();
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
			String today = formatter.format(date).toString();

			//for department
			String dpt=null;

			if(!patientRegistration.getVuserD().getSpecUserJoin().isEmpty())
			{
				dpt=patientRegistration.getVuserD().getSpecUserJoin().get(0).getDocSpec().getSpecName();
				
			}
			else
			{
				dpt="";
			}
			
			byte[] pdfByte = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			Font redFont2 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
			Font redFont3 = new Font(Font.FontFamily.HELVETICA, 12, Font.UNDERLINE);
			Font redFont4 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
			try {

				Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");

				Document document = new Document(PageSize.A4_LANDSCAPE);
				PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);


					Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
					Font redFont9 = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);

					Font redFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

					Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
					

					document.open();
					PdfPTable table = new PdfPTable(2);

					Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());

					img.scaleAbsolute(60, 87);

					table.setWidthPercentage(105f);

					Phrase pq = new Phrase(new Chunk(img, 5, -63));

					pq.add(new Chunk(newAddress,redFont));
					
		                    
					PdfPCell cellp = new PdfPCell(pq);
					PdfPCell cell1 = new PdfPCell();
					cell1.setBorder(0);

					//cell1.setFixedHeight(107f);
					
					
					PdfPTable table351 = new PdfPTable(1);
					table351.setWidths(new float[] {5f});
					table351.setSpacingBefore(10);
					table351.setWidthPercentage(100f);
					
					PdfPCell hcell351;
					hcell351 = new PdfPCell(new Phrase("UDBHAVA HOSPITALS", redFont4));
					hcell351.setBorder(Rectangle.NO_BORDER);
					hcell351.setHorizontalAlignment(Element.ALIGN_CENTER);
					table351.addCell(hcell351);
					
					
					cell1.addElement(table351);
					cell1.addElement(pq);
					cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell1.setColspan(2);
				
					//table.addCell(cell1);
					
				/*	PdfPCell cell35 = new PdfPCell();
					cell35.setBorder(Rectangle.NO_BORDER);
				*/
					
					
					PdfPTable table35 = new PdfPTable(1);
					table35.setWidths(new float[] {5f});
					table35.setSpacingBefore(10);
					table35.setWidthPercentage(100f);
					
					PdfPCell hcell35;
					hcell35 = new PdfPCell(new Phrase("INPATIENT APPROXIMATE BILL", headFont));
					hcell35.setBorder(Rectangle.NO_BORDER);
					hcell35.setHorizontalAlignment(Element.ALIGN_CENTER);
					table35.addCell(hcell35);
					
					cell1.addElement(table35);
					/*cell35.setColspan(2);
					cell35.addElement(table35);
					table.addCell(cell35);*/
				

				//	PdfPCell cell19 = new PdfPCell();

					PdfPTable table3 = new PdfPTable(6);
					table3.setWidths(new float[] { 5f,1f,5f,5f, 1f,5f });
					table3.setSpacingBefore(10);

					PdfPCell hcell1;
					hcell1 = new PdfPCell(new Phrase("App.Bill Date", redFont1));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell1.setPaddingLeft(-50f);
					table3.addCell(hcell1);
					
					hcell1 = new PdfPCell(new Phrase(":", redFont1));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell1.setPaddingLeft(-50f);
					table3.addCell(hcell1);
					
					hcell1 = new PdfPCell(new Phrase(today, redFont1));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell1.setPaddingLeft(-50f);
					table3.addCell(hcell1);

				
					PdfPCell hcell10;
					hcell10 = new PdfPCell(new Phrase("App. Bill No" , redFont1));
					hcell10.setBorder(Rectangle.NO_BORDER);
					//hcell10.setPaddingLeft(40f);
					table3.addCell(hcell10);
					
					hcell10 = new PdfPCell(new Phrase(":" , redFont1));
					hcell10.setBorder(Rectangle.NO_BORDER);
					//hcell10.setPaddingLeft(40f);
					table3.addCell(hcell10);
					
					hcell10 = new PdfPCell(new Phrase(pdfBill , redFont1));
					hcell10.setBorder(Rectangle.NO_BORDER);
					//hcell10.setPaddingLeft(40f);
					table3.addCell(hcell10);

				
					
					PdfPCell hcell2;
					hcell2 = new PdfPCell(new Phrase("Admission No" , redFont1));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table3.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(":" , redFont1));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table3.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(patientRegistration.getRegId(), redFont1));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table3.addCell(hcell2);

				
					
					PdfPCell hcell20;
					hcell20 = new PdfPCell(new Phrase("UMR No", redFont1));
					hcell20.setBorder(Rectangle.NO_BORDER);
					//hcell20.setPaddingLeft(40f);
					table3.addCell(hcell20);
					
					hcell20 = new PdfPCell(new Phrase(":", redFont1));
					hcell20.setBorder(Rectangle.NO_BORDER);
					//hcell20.setPaddingLeft(40f);
					table3.addCell(hcell20);
					
					hcell20 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getUmr(), redFont1));
					hcell20.setBorder(Rectangle.NO_BORDER);
					//hcell20.setPaddingLeft(40f);
					table3.addCell(hcell20);

					

					PdfPCell hcell3;
					hcell3 = new PdfPCell(new Phrase("Patient Name", redFont1));
					hcell3.setBorder(Rectangle.NO_BORDER);
					hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell3.setPaddingLeft(-50f);
					table3.addCell(hcell3);
					
					hcell3 = new PdfPCell(new Phrase(":", redFont1));
					hcell3.setBorder(Rectangle.NO_BORDER);
					hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell3.setPaddingLeft(-50f);	
					table3.addCell(hcell3);
					
					hcell3 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getTitle()+". "+patientRegistration.getPatientDetails().getFirstName()+" "+patientRegistration.getPatientDetails().getLastName(), redFont1));
					hcell3.setBorder(Rectangle.NO_BORDER);
					hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell3.setPaddingLeft(-50f);
					table3.addCell(hcell3);

				
					
					PdfPCell hcell30;
					hcell30 = new PdfPCell(new Phrase("S-W-B-D/O", redFont1));
					hcell30.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell30);
					
					hcell30 = new PdfPCell(new Phrase(":", redFont1));
					hcell30.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell30);
					
					hcell30 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getMotherName(), redFont1)); //mother name if father name
					hcell30.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell30);

					
					
					Timestamp timestamp2 = patientRegistration.getDateOfJoining();
					DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa ");

					Calendar calendar2 = Calendar.getInstance();
					calendar2.setTimeInMillis(timestamp2.getTime());

					String doa = dateFormat2.format(calendar2.getTime());
					
					
					
					
					PdfPCell hcell4;
					hcell4 = new PdfPCell(new Phrase("DOA", redFont1));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell4.setPaddingLeft(-50f);
					table3.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(":", redFont1));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell4.setPaddingLeft(-50f);
					table3.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(String.valueOf(doa), redFont1));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell4.setPaddingLeft(-50f);
					table3.addCell(hcell4);

					
		
					PdfPCell hcell40;
					hcell40 = new PdfPCell(new Phrase("Admitted Ward", redFont1));
					hcell40.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell40);

					hcell40 = new PdfPCell(new Phrase(":", redFont1));
					hcell40.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell40);

					hcell40 = new PdfPCell(new Phrase(admittedWard, redFont1));
					hcell40.setBorder(Rectangle.NO_BORDER);
					table3.addCell(hcell40);


					PdfPCell hcell5;
					hcell5 = new PdfPCell(new Phrase("Consultant", redFont1));
					hcell5.setBorder(Rectangle.NO_BORDER);
					hcell5.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell5.setPaddingLeft(-50f);
					table3.addCell(hcell5);
					
					hcell5 = new PdfPCell(new Phrase(":", redFont1));
					hcell5.setBorder(Rectangle.NO_BORDER);
					hcell5.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell5.setPaddingLeft(-50f);
					table3.addCell(hcell5);
					
					hcell5 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getConsultant(), redFont1));
					hcell5.setBorder(Rectangle.NO_BORDER);
					hcell5.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell5.setPaddingLeft(-50f);
					table3.addCell(hcell5);

					
					
					PdfPCell hcell50;
					hcell50 = new PdfPCell(new Phrase("Department:", redFont1));
					hcell50.setBorder(Rectangle.NO_BORDER);
					//hcell50.setPaddingLeft(40f);
					table3.addCell(hcell50);
					
					hcell50 = new PdfPCell(new Phrase(":", redFont1));
					hcell50.setBorder(Rectangle.NO_BORDER);
					//hcell50.setPaddingLeft(40f);
					table3.addCell(hcell50);
					
					hcell50 = new PdfPCell(new Phrase(dpt, redFont1));
					hcell50.setBorder(Rectangle.NO_BORDER);
					//hcell50.setPaddingLeft(40f);
					table3.addCell(hcell50);

					
					PdfPCell hcell6;
					hcell6 = new PdfPCell(new Phrase("Registration No", redFont1));
					hcell6.setBorder(Rectangle.NO_BORDER);
					hcell6.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell6.setPaddingLeft(-50f);
					table3.addCell(hcell6);
					
					hcell6 = new PdfPCell(new Phrase(":", redFont1));
					hcell6.setBorder(Rectangle.NO_BORDER);
					hcell6.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell6.setPaddingLeft(-50f);
					table3.addCell(hcell6);
					
					hcell6 = new PdfPCell(new Phrase(patientRegistration.getRegId(), redFont1));
					hcell6.setBorder(Rectangle.NO_BORDER);
					hcell6.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell6.setPaddingLeft(-50f);
					table3.addCell(hcell6);

					
					
					PdfPCell hcell60;
					hcell60 = new PdfPCell(new Phrase("Age/Sex", redFont1));
					hcell60.setBorder(Rectangle.NO_BORDER);
					//hcell60.setPaddingLeft(40f);
					table3.addCell(hcell60);
					
					hcell60 = new PdfPCell(new Phrase(":", redFont1));
					hcell60.setBorder(Rectangle.NO_BORDER);
					//hcell60.setPaddingLeft(40f);
					table3.addCell(hcell60);
					
					hcell60 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getAge()+"/"+patientRegistration.getPatientDetails().getGender(), redFont1));
					hcell60.setBorder(Rectangle.NO_BORDER);
					//hcell60.setPaddingLeft(40f);
					table3.addCell(hcell60);

					
					
					PdfPCell hcell7;
					hcell7 = new PdfPCell(new Phrase("Address", redFont1));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell7.setPaddingLeft(-50f);
					table3.addCell(hcell7);

					hcell7 = new PdfPCell(new Phrase(":", redFont1));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell7.setPaddingLeft(-50f);
					table3.addCell(hcell7);

					hcell7 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getAddress(), redFont1));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell7.setPaddingLeft(-50f);
					table3.addCell(hcell7);

					
					String refName=null;
					if(patientRegistration.getPatientDetails().getvRefferalDetails()==null)
					{
						refName="";
					}
					else
					{
						refName=patientRegistration.getPatientDetails().getvRefferalDetails().getRefName();
					}
					
					PdfPCell hcell70;
					hcell70 = new PdfPCell(new Phrase("Referal Name", redFont1));
					hcell70.setBorder(Rectangle.NO_BORDER);
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell70);
					
					hcell70 = new PdfPCell(new Phrase(":", redFont1));
					hcell70.setBorder(Rectangle.NO_BORDER);
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell70);
					
					hcell70 = new PdfPCell(new Phrase(refName, redFont1));
					hcell70.setBorder(Rectangle.NO_BORDER);
				//	hcell70.setPaddingLeft(40f);
					table3.addCell(hcell70);

					
					
					cell1.addElement(table3);
					
					
					/*cell19.setColspan(2);
					cell19.addElement(table3);
					table.addCell(cell19);*/
					
					/*PdfPCell cell20 = new PdfPCell();
					cell20.setBorder(Rectangle.NO_BORDER);
					cell20.setPaddingTop(10f);*/

					
					PdfPTable table1 = new PdfPTable(7);
					table1.setWidths(new float[] { 4f, 10f,3f, 2.5f,3f,3f,3f });
					table1.setSpacingBefore(10);
					table1.setWidthPercentage(105f);

					PdfPCell hcell;
					
					hcell = new PdfPCell(new Phrase("Service Code", headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("Service/Investigation" , headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(hcell);
					
					hcell = new PdfPCell(new Phrase("Date", headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(7);
					table1.addCell(hcell);
					
					
					
					
					hcell = new PdfPCell(new Phrase("Qty", headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(18);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("Rate" , headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(15);
					table1.addCell(hcell);
					

					hcell = new PdfPCell(new Phrase("Disc" , headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(15);
					table1.addCell(hcell);
					
					hcell = new PdfPCell(new Phrase("Amt" , headFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(30);
					table1.addCell(hcell);
					
		
					
					table1.addCell(hcell);
					
					
					/*cell20.setColspan(2);
					cell20.addElement(table1);
					table.addCell(cell20);*/
					cell1.addElement(table1);
					
					/*PdfPCell cell21 = new PdfPCell();
					cell21.setBorder(Rectangle.NO_BORDER);*/

					
					// Calculating total
					float total = 0;
					float totalMed=0;
					float totalServiceAmt=0;
					float totalAccmAmt=0;
					float totalEqAmt=0;
					for (ChargeBill chargeBillInfo : chargeBillList) 
					{
						if (chargeBillInfo.getLabId() != null) 
						{
							
							if(chargeBillInfo.getNetAmount()!=0) {
							chargeBillInfo.setServiceName(chargeBillInfo.getLabId().getServiceName());
							
							total+=chargeBillInfo.getNetAmount();
							}
							
							}
											}
					
					for (ChargeBill chargeMedicine : chargeBillList) 
					{
						if (chargeMedicine.getSaleId() != null) 
						{
							
							if(chargeMedicine.getNetAmount()!=0) {
								totalMed+=chargeMedicine.getNetAmount();
							}
							
						}
						
						
					}
					
					for (ChargeBill chargeBillInfo : chargeBillList) 
					{

						
							if (chargeBillInfo.getServiceId() != null) 
						{
								if(chargeBillInfo.getServiceId().getServiceType().equalsIgnoreCase("OTHER"))
								{
							if(chargeBillInfo.getNetAmount()!=0) {	

								totalServiceAmt+=chargeBillInfo.getNetAmount();
							}
								}}
						}
					
					
					for (ChargeBill chargeBillInfo1 : chargeBillList) 
					{

						
							if (chargeBillInfo1.getServiceId() != null) 
						{
								if(chargeBillInfo1.getServiceId().getServiceType().equalsIgnoreCase("EQUIPMENT CHARGES"))
								{
							if(chargeBillInfo1.getNetAmount()!=0)
							{	
								totalEqAmt+=chargeBillInfo1.getNetAmount();
							}
								}
						}
					}
					
					for (ChargeBill chargeBillInfo1 : chargeBillList) 
					{

						
							if (chargeBillInfo1.getServiceId() != null) 
						{
								if(chargeBillInfo1.getServiceId().getServiceType().equalsIgnoreCase("WARD CHARGES"))
								{
							if(chargeBillInfo1.getNetAmount()!=0)
							{	
								totalAccmAmt+=chargeBillInfo1.getNetAmount();
							}
								}
						}
					}
					
					
					
					
					
					
					PdfPTable table11 = new PdfPTable(7);
					table11.setWidths(new float[] {8f, 5f,3f, 2.5f,3f,3f,3f});
					table11.setSpacingBefore(10);
					table11.setWidthPercentage(105f);

					
					PdfPTable table111 = new PdfPTable(7);
					table111.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f});
					table111.setSpacingBefore(10);
					table111.setWidthPercentage(105f);
					
					
					PdfPCell hcell01;
					
					if(chargeBillList.get(0).getLabId() != null)
					{
					hcell01 = new PdfPCell(new Phrase("LAB CHARGES", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					hcell01 = new PdfPCell(new Phrase("", headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
					table11.addCell(hcell01);
					
					
					hcell01 = new PdfPCell(new Phrase(String.valueOf(total), headFont));
					hcell01.setBorder(Rectangle.NO_BORDER);
					hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell01.setPaddingRight(10f);
					table11.addCell(hcell01);
					
					}
					else
					{
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell01.setPaddingRight(10f);
						table11.addCell(hcell01);
					}
					
					PdfPCell cell;
					for (ChargeBill chargeBillInfo : chargeBillList) 
					{
						System.out.println("hello hoiiiiiii----------------------------------");
						
					if (chargeBillInfo.getLabId() != null) 
					{
						System.out.println("hello----------------------------------");
						
						if(chargeBillInfo.getNetAmount()!=0) {
						chargeBillInfo.setServiceName(chargeBillInfo.getLabId().getServiceName());
						
						
						
						
						String from =chargeBillInfo.getLabId().getEnteredDate().toString(); 
						Timestamp timestamp = Timestamp.valueOf(from);
						DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(timestamp.getTime());

						String labDate = dateFormat.format(calendar.getTime());

						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getLabId().getLabServices().getServiceId()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table111.addCell(cell);

						cell = new PdfPCell(new Phrase(chargeBillInfo.getServiceName(), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table111.addCell(cell);
						
						cell = new PdfPCell(new Phrase(labDate , redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table111.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getQuantity()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table111.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getAmount()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table111.addCell(cell);
						
						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getDiscount()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table111.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getNetAmount()), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setPaddingRight(10);
						table111.addCell(cell);
						
						

						//total += chargeBillInfo.getNetAmount();
					
						}
					} 					 

					}
					
					
					int count = 0;
					/*cell21.setColspan(2);
					cell21.addElement(table11);
					table.addCell(cell21);
					*/
					cell1.addElement(table11);
					cell1.addElement(table111);
					
					/*PdfPCell cell22 = new PdfPCell();
					cell22.setBorder(Rectangle.NO_BORDER);*/
					
					PdfPTable table12 = new PdfPTable(7);
					table12.setWidths(new float[] {7f,5f,3f, 2.5f,3f,3f,3f});
					table12.setSpacingBefore(10);
					table12.setWidthPercentage(105f);

					PdfPCell hcell011;
					
					if(chargeBillList.get(0).getSaleId()!=null)
					{
					hcell011 = new PdfPCell(new Phrase("MEDICINE CHARGES", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
			
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase("", headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
					table12.addCell(hcell011);
					
					hcell011 = new PdfPCell(new Phrase(String.valueOf(Math.round(totalMed)), headFont));
					hcell011.setBorder(Rectangle.NO_BORDER);
					hcell011.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell011.setPaddingRight(10);
					table12.addCell(hcell011);
					}
					else
					{
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
				
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell011.setPaddingRight(10);
						table12.addCell(hcell011);
					}
					/*cell22.setColspan(2);
					cell22.addElement(table12);
					table.addCell(cell22);*/
					
					cell1.addElement(table12);
					
					/*PdfPCell cell33 = new PdfPCell();
					cell33.setBorder(Rectangle.NO_BORDER);*/
					
					PdfPTable table22 = new PdfPTable(7);
					table22.setWidths(new float[] {4f,10f,3f, 2.5f,3f,3f,3f});
					table22.setSpacingBefore(10);
					table22.setWidthPercentage(105f);

					//charge bill
					for (ChargeBill chargeMedicine : chargeBillList) 
					{
						if (chargeMedicine.getSaleId() != null) 
						{
							
							if(chargeMedicine.getNetAmount()!=0) {

							/*salestAmount += chargeMedicine.getSaleId().getCostPrice();
							salesnetAmount += chargeMedicine.getNetAmount();
							salesDiscount = chargeMedicine.getSaleId().getDiscount();
							salesQuantity += chargeMedicine.getSaleId().getQuantity();*/
							
							
							salesnetAmount += chargeMedicine.getNetAmount();
							String from =chargeMedicine.getSaleId().getBillDate().toString(); 
							Timestamp timestamp = Timestamp.valueOf(from);
							DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(timestamp.getTime());

							String medicineDate = dateFormat.format(calendar.getTime());

							
							

							PdfPCell hcell0;
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(chargeMedicine.getSaleId().getPatientSalesMedicineDetails().getMedicineId()), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_LEFT);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(chargeMedicine.getSaleId().getMedicineName()), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_LEFT);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(medicineDate, redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_LEFT);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(chargeMedicine.getSaleId().getQuantity()), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(Math.round(chargeMedicine.getSaleId().getMrp())), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(Math.round(chargeMedicine.getSaleId().getDiscount())), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
							table22.addCell(hcell0);
							
							hcell0 = new PdfPCell(new Phrase(String.valueOf(Math.round(chargeMedicine.getNetAmount())), redFont1));
							hcell0.setBorder(Rectangle.NO_BORDER);
							hcell0.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell0.setPaddingRight(10);
							table22.addCell(hcell0);
				

							}
						}
					}
					
					/*cell33.setColspan(2);
					cell33.addElement(table22);
					table.addCell(cell33);*/
					
					cell1.addElement(table22);
														
					/*PdfPCell cell23 = new PdfPCell();
					cell23.setBorder(Rectangle.NO_BORDER);*/
					
					PdfPTable table13 = new PdfPTable(7);
					table13.setWidths(new float[] {8f,5f,3f, 2.5f,3f,3f,3f});
					table13.setSpacingBefore(10);
					table13.setWidthPercentage(105f);

					PdfPCell hcell0111;
					if(chargeBillList.get(0).getServiceId()!=null &&  chargeBillList.get(0).getServiceId().getServiceType().equalsIgnoreCase("OTHER"))
					{
					hcell0111 = new PdfPCell(new Phrase("SERVICE CHARGES", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_LEFT);
					table13.addCell(hcell0111);
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table13.addCell(hcell0111);
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table13.addCell(hcell0111);
					hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table13.addCell(hcell0111);
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					table13.addCell(hcell0111);
		
					
					
					hcell0111 = new PdfPCell(new Phrase("", headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					table13.addCell(hcell0111);
					
					hcell0111 = new PdfPCell(new Phrase(String.valueOf(totalServiceAmt), headFont));
					hcell0111.setBorder(Rectangle.NO_BORDER);
					hcell0111.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell0111.setPaddingRight(10);
					table13.addCell(hcell0111);
					}
					else
					{
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_LEFT);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table13.addCell(hcell0111);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						table13.addCell(hcell0111);
			
						
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell0111.setPaddingRight(10);
						table13.addCell(hcell0111);
					}
					
				
					PdfPTable table28 = new PdfPTable(7);
					table28.setWidths(new float[] {4f,10f,3f, 2.5f,3f,3f,3f});
					table28.setSpacingBefore(10);
					table28.setWidthPercentage(105f);
					
					for (ChargeBill chargeBillInfo : chargeBillList) 
					{

						
							if (chargeBillInfo.getServiceId() != null && chargeBillInfo.getServiceId().getServiceType().equalsIgnoreCase("OTHER")) 
						{
								
								
							if(chargeBillInfo.getNetAmount()!=0) {	

								String from =chargeBillInfo.getInsertedDate().toString(); 
								Timestamp timestamp = Timestamp.valueOf(from);
								DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(timestamp.getTime());

								String serviceDate = dateFormat.format(calendar.getTime());

								

								
								PdfPCell cell11;
							chargeBillInfo.setServiceName(chargeBillInfo.getServiceId().getServiceName());

							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getServiceId().getServiceId()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table28.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(chargeBillInfo.getServiceName(), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table28.addCell(cell11);
							
							

							cell11 = new PdfPCell(new Phrase(serviceDate, redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table28.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getQuantity()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table28.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getMrp()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table28.addCell(cell11);
							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getDiscount()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table28.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getNetAmount()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell11.setPaddingRight(10);
							
							table28.addCell(cell11);

							

			
							//total += chargeBillInfo.getNetAmount();
							}
								}
						
						else 
						{
							chargeBillInfo.setServiceName("NOT APPLICABLE");
						}

					}
					
					/*cell23.setColspan(2);
					cell23.addElement(table13);
					table.addCell(cell23);*/

					cell1.addElement(table13);
					cell1.addElement(table28);
					
					
					//----------------------------------
					PdfPTable table131 = new PdfPTable(7);
					table131.setWidths(new float[] {10f, 7f,3f,2.5f,3f,3f,3f});
					table131.setSpacingBefore(10);
					table131.setWidthPercentage(105f);

					PdfPCell hcell01111;
					if(chargeBillList.get(0).getServiceId()!=null && chargeBillList.get(0).getServiceId().getServiceType().equalsIgnoreCase("WARD CHARGES"))
					{
					hcell01111 = new PdfPCell(new Phrase("WARD CHARGES", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_LEFT);
					table131.addCell(hcell01111);
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table131.addCell(hcell01111);
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table131.addCell(hcell01111);
					hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
					table131.addCell(hcell01111);
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					table131.addCell(hcell01111);
		
					
					
					hcell01111 = new PdfPCell(new Phrase("", headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					table131.addCell(hcell01111);
					
					hcell01111 = new PdfPCell(new Phrase(String.valueOf(totalAccmAmt), headFont));
					hcell01111.setBorder(Rectangle.NO_BORDER);
					hcell01111.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell01111.setPaddingRight(10);
					table131.addCell(hcell01111);
					}
					else
					{
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_LEFT);
						table131.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table131.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table131.addCell(hcell01111);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table131.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						table131.addCell(hcell01111);
			
						
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						table131.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell01111.setPaddingRight(10);
						table131.addCell(hcell01111);
					}
					
				
					PdfPTable table281 = new PdfPTable(7);
					table281.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f});
					table281.setSpacingBefore(10);
					table281.setWidthPercentage(105f);
					
					for (ChargeBill chargeBillInfo3 : chargeBillList) 
					{

						
							if (chargeBillInfo3.getServiceId() != null) 
						{
								if(chargeBillInfo3.getServiceId().getServiceType().equalsIgnoreCase("WARD CHARGES"))
								{
							if(chargeBillInfo3.getNetAmount()!=0) {	

								String from =chargeBillInfo3.getInsertedDate().toString(); 
								Timestamp timestamp = Timestamp.valueOf(from);
								DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(timestamp.getTime());

								String serviceDate = dateFormat.format(calendar.getTime());

								

								
								PdfPCell cell11;
							chargeBillInfo3.setServiceName(chargeBillInfo3.getServiceId().getServiceName());

							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getServiceId().getServiceId()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table281.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(chargeBillInfo3.getServiceName(), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table281.addCell(cell11);
							
							

							cell11 = new PdfPCell(new Phrase(serviceDate, redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table281.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getQuantity()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table281.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getMrp()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table281.addCell(cell11);
							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getDiscount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table281.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getNetAmount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell11.setPaddingRight(10);
							
							table281.addCell(cell11);

							

			
							//total += chargeBillInfo3.getNetAmount();
							}
								}
						} 
						else 
						{
							chargeBillInfo3.setServiceName("NOT APPLICABLE");
						}

					}
					
					/*cell23.setColspan(2);
					cell23.addElement(table13);
					table.addCell(cell23);*/

					cell1.addElement(table131);
					cell1.addElement(table281);
					
					PdfPTable table1312 = new PdfPTable(7);
					table1312.setWidths(new float[] {10f, 7f,3f,2.5f,3f,3f,3f});
					table1312.setSpacingBefore(10);
					table1312.setWidthPercentage(105f);

					PdfPCell hcell01112;
					if(chargeBillList.get(0).getServiceId()!=null && chargeBillList.get(0).getServiceId().getServiceType().equalsIgnoreCase("EQUIPMENT CHARGES"))
					{
					hcell01112 = new PdfPCell(new Phrase("EQUIPMENT CHARGES", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1312.addCell(hcell01112);
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1312.addCell(hcell01112);
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1312.addCell(hcell01112);
					hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1312.addCell(hcell01112);
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					table1312.addCell(hcell01112);
		
					
					
					hcell01112 = new PdfPCell(new Phrase("", headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					table1312.addCell(hcell01112);
					
					hcell01112 = new PdfPCell(new Phrase(String.valueOf(totalEqAmt), headFont));
					hcell01112.setBorder(Rectangle.NO_BORDER);
					hcell01112.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell01112.setPaddingRight(10);
					table1312.addCell(hcell01112);
					}
					else
					{
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_LEFT);
						table1312.addCell(hcell01112);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1312.addCell(hcell01112);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1312.addCell(hcell01112);
						hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1312.addCell(hcell01112);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						table1312.addCell(hcell01112);
			
						
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						table1312.addCell(hcell01112);
						
						hcell01112 = new PdfPCell(new Phrase("", headFont));
						hcell01112.setBorder(Rectangle.NO_BORDER);
						hcell01112.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell01112.setPaddingRight(10);
						table1312.addCell(hcell01112);
			
					}
				
					PdfPTable table282 = new PdfPTable(7);
					table282.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f});
					table282.setSpacingBefore(10);
					table282.setWidthPercentage(105f);
					
					for (ChargeBill chargeBillInfo3 : chargeBillList) 
					{

						
							if (chargeBillInfo3.getServiceId() != null) 
						{
								if(chargeBillInfo3.getServiceId().getServiceType().equalsIgnoreCase("EQUIPMENT CHARGES"))
								{
							if(chargeBillInfo3.getNetAmount()!=0) {	

								String from =chargeBillInfo3.getInsertedDate().toString(); 
								Timestamp timestamp = Timestamp.valueOf(from);
								DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(timestamp.getTime());

								String serviceDate = dateFormat.format(calendar.getTime());

								

								
								PdfPCell cell11;
							chargeBillInfo3.setServiceName(chargeBillInfo3.getServiceId().getServiceName());

							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getServiceId().getServiceId()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table282.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(chargeBillInfo3.getServiceName(), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table282.addCell(cell11);
							
							

							cell11 = new PdfPCell(new Phrase(serviceDate, redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table282.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getQuantity()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table282.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getMrp()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table282.addCell(cell11);
							
							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getDiscount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
							table282.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getNetAmount()), redFont9));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell11.setPaddingRight(10);
							
							table282.addCell(cell11);

							

			
							//total += chargeBillInfo3.getNetAmount();
							}
								}
						} 
						else 
						{
							chargeBillInfo3.setServiceName("NOT APPLICABLE");
						}

					}
					
					/*cell23.setColspan(2);
					cell23.addElement(table13);
					table.addCell(cell23);*/

					cell1.addElement(table1312);
					cell1.addElement(table282);
					
					
					
					PdfPTable table182 = new PdfPTable(1);
					table182.setWidths(new float[] {5f});
					table182.setSpacingBefore(10);
					table182.setWidthPercentage(100f);
					
					PdfPCell hcell072;
					hcell072 = new PdfPCell(new Phrase("_________________________________________________________________________", headFont));
					hcell072.setBorder(Rectangle.NO_BORDER);
					hcell072.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table182.addCell(hcell072);
					
					
				/*	PdfPCell cell24 = new PdfPCell();
					cell24.setBorder(Rectangle.NO_BORDER);
					*/
					
					float totalAmount=0;
					totalAmount=total+totalMed+totalServiceAmt+totalAccmAmt+totalEqAmt;
					
					PdfPTable table14 = new PdfPTable(5);
					table14.setWidths(new float[] {2f, 1f,10f, 5f,5f});
					table14.setSpacingBefore(10);
					table14.setWidthPercentage(100f);

					PdfPCell hcell02;
					
					hcell02 = new PdfPCell(new Phrase("", headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell02);
					
					hcell02 = new PdfPCell(new Phrase("", headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell02);
					
					hcell02 = new PdfPCell(new Phrase("NET AMOUNT", headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_RIGHT);
				    hcell02.setPaddingRight(-55f);
					table14.addCell(hcell02);
					
					hcell02 = new PdfPCell(new Phrase(":", headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell02);
					
					hcell02 = new PdfPCell(new Phrase(String.valueOf(Math.round(totalAmount)), headFont));
					hcell02.setBorder(Rectangle.NO_BORDER);
					hcell02.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell02);
					
	
					
					PdfPCell hcell03;
					
					hcell03 = new PdfPCell(new Phrase("", headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell03);
					
					hcell03 = new PdfPCell(new Phrase("", headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell03);
					
					hcell03 = new PdfPCell(new Phrase("ADVANCE AMOUNT", headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell03.setPaddingRight(-83f);
					table14.addCell(hcell03);
					
					hcell03 = new PdfPCell(new Phrase(":", headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell03);
					
					hcell03 = new PdfPCell(new Phrase(String.valueOf(patientRegistration.getAdvanceAmount()), headFont));
					hcell03.setBorder(Rectangle.NO_BORDER);
					hcell03.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell03);
					
					float dueAmt = totalAmount - patientRegistration.getAdvanceAmount();
					PdfPCell hcell04;
					
					hcell04 = new PdfPCell(new Phrase("", headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase("", headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
					table14.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase("DUE AMOUNT", headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell04.setPaddingRight(-55f);
					table14.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase(":", headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(dueAmt)), headFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table14.addCell(hcell04);
					
					/*cell24.setColspan(2);
					cell24.addElement(table182);
					cell24.addElement(table14);
					table.addCell(cell24);
*/
					cell1.addElement(table182);
					cell1.addElement(table14);
					
					
					/*PdfPCell cell25 = new PdfPCell();
					cell25.setBorder(Rectangle.NO_BORDER);*/
				
					PdfPTable table15 = new PdfPTable(1);
					table15.setWidths(new float[] {5f});
					table15.setSpacingBefore(10);
					table15.setWidthPercentage(100f);
					
					PdfPCell hcell05;
					hcell05 = new PdfPCell(new Phrase("\n RECEIPT DETAILS", headFont));
					hcell05.setBorder(Rectangle.NO_BORDER);
					table15.addCell(hcell05);
					
					/*cell25.setColspan(2);
					cell25.addElement(table15);
					table.addCell(cell25);*/
					
					cell1.addElement(table15);
					
				/*	PdfPCell cell26 = new PdfPCell();
					cell26.setBorder(Rectangle.NO_BORDER);
					*/
					PdfPTable table16 = new PdfPTable(6);
					table16.setWidths(new float[] {5f,6f,5f,6f,4f,4f});
					table16.setSpacingBefore(10);
					table16.setWidthPercentage(100f);
					
					PdfPCell hcell06;
					hcell06 = new PdfPCell(new Phrase("Receipt No", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					hcell06 = new PdfPCell(new Phrase("Receipt Date", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					hcell06 = new PdfPCell(new Phrase("Receipt Name", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					hcell06 = new PdfPCell(new Phrase("Mode Of Payment", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
				
					hcell06 = new PdfPCell(new Phrase("Receipt Amt", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					hcell06 = new PdfPCell(new Phrase("Remarks", headFont));
					hcell06.setBorder(Rectangle.NO_BORDER);
					hcell06.setBackgroundColor(BaseColor.GRAY);
					table16.addCell(hcell06);
					
					for (PatientPayment patientPaymentInfo : patientPayment) 
					{
						
						Timestamp timestamp =patientPaymentInfo.getInsertedDate(); 
						DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(timestamp.getTime());
					    String from=  dateFormat.format(calendar.getTime());
					   
						if (patientPaymentInfo.getTypeOfCharge().equalsIgnoreCase("ADVANCE")) 
						{
							PdfPCell cell11;

							cell11 = new PdfPCell(new Phrase(String.valueOf(patientPaymentInfo.getPaymentId()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table16.addCell(cell11);

							cell11 = new PdfPCell(new Phrase(from, redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table16.addCell(cell11);
	
							cell11 = new PdfPCell(new Phrase(patientPaymentInfo.getTypeOfCharge(), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table16.addCell(cell11);

							if(patientPaymentInfo.getModeOfPaymant()!=null)
							{
							cell11 = new PdfPCell(new Phrase(String.valueOf(patientPaymentInfo.getModeOfPaymant()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
							table16.addCell(cell11);
							}
							else
							{
								cell11 = new PdfPCell(new Phrase(" ", redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table16.addCell(cell11);
			
							}
							cell11 = new PdfPCell(new Phrase(String.valueOf(patientPaymentInfo.getAmount()), redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell11.setPaddingRight(23);
							table16.addCell(cell11);
						
							cell11 = new PdfPCell(new Phrase("", redFont));
							cell11.setBorder(Rectangle.NO_BORDER);
							cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table16.addCell(cell11);

							totalRecieptAmt=totalRecieptAmt+patientPaymentInfo.getAmount();
						}

					}
					/*cell26.setColspan(2);
					cell26.addElement(table16);
					table.addCell(cell26);*/
					
					cell1.addElement(table16);
					
					
					
					PdfPTable table18 = new PdfPTable(1);
					table18.setWidths(new float[] {5f});
					table18.setSpacingBefore(10);
					table18.setWidthPercentage(100f);
					
					PdfPCell hcell071;
					hcell071 = new PdfPCell(new Phrase("_________________________________________________________________________", headFont));
					hcell071.setBorder(Rectangle.NO_BORDER);
					hcell071.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table18.addCell(hcell071);
					
					
					
					/*PdfPCell cell27 = new PdfPCell();
					cell27.setBorder(Rectangle.NO_BORDER);*/
					
					PdfPTable table17 = new PdfPTable(6);
					table17.setWidths(new float[] {5f,6f,5f,6f,4f,4f});
					table17.setSpacingBefore(10);
					table17.setWidthPercentage(100f);
					
					PdfPCell hcell07;
					hcell07 = new PdfPCell(new Phrase("", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);
					
					hcell07 = new PdfPCell(new Phrase("", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);
					
					hcell07 = new PdfPCell(new Phrase("Total : ", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);
					
					hcell07 = new PdfPCell(new Phrase("", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);
					
					hcell07 = new PdfPCell(new Phrase(String.valueOf(totalRecieptAmt), headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					hcell07.setHorizontalAlignment(Element.ALIGN_CENTER);
					table17.addCell(hcell07);
				
					
					hcell07 = new PdfPCell(new Phrase("", headFont));
					hcell07.setBorder(Rectangle.NO_BORDER);
					table17.addCell(hcell07);

					/*cell27.setColspan(2);
					cell27.addElement(table18);
					cell27.addElement(table17);
					table.addCell(cell27);*/
					
					cell1.addElement(table18);
					cell1.addElement(table17);
					
					/*PdfPCell cell28 = new PdfPCell();
					cell28.setBorder(Rectangle.NO_BORDER);*/
				
					PdfPTable table181 = new PdfPTable(2);
					table181.setWidths(new float[] {8f,8f});
					table181.setSpacingBefore(10);
					table181.setWidthPercentage(100f);
					
					PdfPCell hcell08;
					hcell08 = new PdfPCell(new Phrase("Total Received Amount In Words : ", headFont));
					hcell08.setBorder(Rectangle.NO_BORDER);
					hcell08.setPaddingTop(10f);
					table181.addCell(hcell08);
					
					hcell08 = new PdfPCell(new Phrase(numberToWordsConverter.convert(totalRecieptAmt) + " Rupees Only", redFont1));
					hcell08.setBorder(Rectangle.NO_BORDER);
					hcell08.setPaddingTop(10f);
					hcell08.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell08.setPaddingLeft(-50f);
					table181.addCell(hcell08);
					
					
					PdfPCell hcell081;
					hcell081 = new PdfPCell(new Phrase("Net Amount In Words : ", headFont));
					hcell081.setBorder(Rectangle.NO_BORDER);
					hcell081.setPaddingTop(10f);
					table181.addCell(hcell081);
					
					hcell081 = new PdfPCell(new Phrase(numberToWordsConverter.convert(Math.round(total + salesnetAmount)) + " Rupees Only", redFont1));
					
					hcell081.setBorder(Rectangle.NO_BORDER);
					hcell081.setPaddingTop(10f);
					hcell081.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell081.setPaddingLeft(-50f);
					table181.addCell(hcell081);
					
				/*	cell28.setColspan(2);
					cell28.addElement(table181);
					table.addCell(cell28);*/
					
					cell1.addElement(table181);
					
					
					
				/*	PdfPCell cell30 = new PdfPCell();
					cell30.setBorder(Rectangle.NO_BORDER);*/
				
					PdfPTable table20 = new PdfPTable(1);
					table20.setWidths(new float[] {8f});
					table20.setSpacingBefore(10);
					table20.setWidthPercentage(100f);
					
					PdfPCell hcell09;
					hcell09 = new PdfPCell(new Phrase("(Authorized Signature)", headFont));
					hcell09.setBorder(Rectangle.NO_BORDER);
					hcell09.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell09.setPaddingTop(20f);
					table20.addCell(hcell09);
					
				/*	cell30.setColspan(2);
					cell30.addElement(table20);
					table.addCell(cell30);*/
					
					cell1.addElement(table20);
				
					
					/*PdfPCell cell29 = new PdfPCell();
					cell29.setBorder(Rectangle.NO_BORDER);*/
					
					PdfPTable table33 = new PdfPTable(4);
					table33.setWidthPercentage(100.0f);
					table33.setWidths(new int[] {3, 4, 3, 4});
					table33.setSpacingBefore(10);

					
					PdfPCell hcell16;
					hcell16 = new PdfPCell(new Phrase("Created By                    :  ", headFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setPaddingLeft(-10f);
					table33.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(createdName , redFont1));
					hcell16.setBorder(Rectangle.NO_BORDER);
					//hcell16.setPaddingRight(-70f);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					table33.addCell(hcell16);
					
					hcell16 = new PdfPCell(new Phrase("Created Dt                   : " , headFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setPaddingLeft(-20f);
					table33.addCell(hcell16);
					
					hcell16 = new PdfPCell(new Phrase(today , redFont1));
					hcell16.setBorder(Rectangle.NO_BORDER);
					//hcell16.setPaddingRight(-70f);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					table33.addCell(hcell16);
					
					
					
					PdfPCell hcell161;
					hcell161 = new PdfPCell(new Phrase("Printed By                     :  ", headFont));
					hcell161.setBorder(Rectangle.NO_BORDER);
					hcell161.setPaddingLeft(-10f);
					table33.addCell(hcell161);

					hcell161 = new PdfPCell(new Phrase(createdName , redFont1));
					hcell161.setBorder(Rectangle.NO_BORDER);
					//hcell161.setPaddingRight(-70f);
					hcell161.setHorizontalAlignment(Element.ALIGN_LEFT);
					table33.addCell(hcell161);
					
					hcell161 = new PdfPCell(new Phrase("Printed Dt                    : " , headFont));
					hcell161.setBorder(Rectangle.NO_BORDER);
					hcell161.setPaddingLeft(-20f);
					table33.addCell(hcell161);
					
					hcell161 = new PdfPCell(new Phrase(today , redFont1));
					hcell161.setBorder(Rectangle.NO_BORDER);
					//hcell161.setPaddingRight(-70f);
					hcell161.setHorizontalAlignment(Element.ALIGN_LEFT);
					table33.addCell(hcell161);
					
					/*cell29.setColspan(2);
					cell29.addElement(table33);
					table.addCell(cell29);
					*/
					
					cell1.addElement(table33);
					
					table.addCell(cell1);
					
					
					document.add(table);

					document.close();
				
				System.out.println("finished");

				pdfByte = byteArrayOutputStream.toByteArray();
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
						.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();

				salesPaymentPdf = new SalesPaymentPdf();
				salesPaymentPdf.setFileName(regId+" Approximate Bill");
				salesPaymentPdf.setFileuri(uri);
				salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
				salesPaymentPdf.setData(pdfByte);
				salesPaymentPdfServiceImpl.save(salesPaymentPdf);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		return salesPaymentPdf;	
		
	}
	/*
	 * Detailed Final bill
	 */
	@Transactional
	@RequestMapping(value="/detailed/{id}",method=RequestMethod.GET)
	public SalesPaymentPdf detailedBill(@PathVariable String id,Principal principal)
	{
			PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(id);
			if(patientRegistration.getpType().equalsIgnoreCase("OUTPATIENT"))
			{
				throw new RuntimeException(ConstantValues.OUTPATIENT_NOT_ALLOWED_ERROR_MSG);
			}
			String pdfBill=null;
			String regId=id;
			//createdBy Security
			User userSecurity=userServiceImpl.findByUserName(principal.getName());
			String createdBy=userSecurity.getUserId();
			String createdName=null;
			
			String mn=userSecurity.getMiddleName();
			
			if(mn==null)
			{
				createdName=userSecurity.getFirstName()+" "+userSecurity.getLastName();
			}
			else
			{
				createdName=userSecurity.getFirstName()+" "+userSecurity.getMiddleName()+" "+userSecurity.getLastName();
			}
			
			long mob=patientRegistration.getPatientDetails().getMobile();
			List<ChargeBill> chargeBillListq=chargeBillServiceImpl.findByPatRegId(patientRegistration);
			pdfBill=chargeBillListq.get(0).getBillNo();
			String billNoo=null;
			
			//shantharam addr
			String newAddress="";
			
			List<ChargeBill> chargeBillList=chargeBillServiceImpl.findByPatRegId(patientRegistrationServiceImpl.findByRegId(id));
			
			List<ChargeBill> chargeBillListLab=chargeBillList.stream().filter((s) -> s.getLabId()!=null).collect(Collectors.toList());
			List<ChargeBill> chargeBillListService=chargeBillList.stream().filter((s) -> s.getServiceId()!=null).collect(Collectors.toList());
			List<ChargeBill> chargeBillListSale=chargeBillList.stream().filter((s) -> s.getSaleId()!=null).collect(Collectors.toList());
			
			for(ChargeBill charge:chargeBillListService)
			{
				System.out.println("chargebill list-----------------"+charge.getServiceId().getServiceId());
			}
			String billNo=null;
			String patientName=null;
			String tokenNo=null;
			long paidSum=0;
			
			
				for(ChargeBill chargeBillInfo:chargeBillList)
				{
		    		//chargeBillInfo.setPaid("YES");
		    		patientName=chargeBillInfo.getPatRegId().getPatientDetails().getFirstName()+" "+chargeBillInfo.getPatRegId().getPatientDetails().getLastName();
		    		tokenNo=chargeBillInfo.getPatRegId().getRegId().substring(2);
		    		billNo=chargeBillInfo.getBillNo();
		    		if(chargeBillInfo.getPaid().equalsIgnoreCase("Yes"))
		    		{
		    			paidSum+=chargeBillInfo.getNetAmount();
		    		}
		    	}
				
				Set<PatientPayment> patientPayment = patientRegistration.getPatientPayment();
				
				long totalRecieptAmt=0;

				
				String admittedWard = null;

				float salestAmount = 0;
				float salesnetAmount = 0;
				float salesDiscount = 0;
				long salesQuantity = 0;
				
				List<RoomBookingDetails> roomBookingDetails = patientRegistration.getRoomBookingDetails();

				for (RoomBookingDetails roomBookingDetailsInfo : roomBookingDetails) {
					RoomDetails roomDetails = roomBookingDetailsInfo.getRoomDetails();
					admittedWard = roomDetails.getRoomType();
				}
				
				String adWrd=null;
				if(admittedWard!=null)
				{
					adWrd=admittedWard;
				}
				else
				{
					adWrd="";
				}
				
				Date date = Calendar.getInstance().getTime();
				DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
				String today = formatter.format(date).toString();

				//for department
				String dpt=null;

				if(!patientRegistration.getVuserD().getSpecUserJoin().isEmpty())
				{
					dpt=patientRegistration.getVuserD().getSpecUserJoin().get(0).getDocSpec().getSpecName();
					
				}
				else
				{
					dpt="";
				}
				
				byte[] pdfByte = null;
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

				Font redFont2 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
				Font redFont3 = new Font(Font.FontFamily.HELVETICA, 12, Font.UNDERLINE);
				Font redFont4 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
				try {

					Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");

					Document document = new Document(PageSize.A4_LANDSCAPE);
					PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);


						Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
						Font redFont9 = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);

						Font redFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

						Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
						

						document.open();
						PdfPTable table = new PdfPTable(2);

						Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());

						img.scaleAbsolute(60, 87);

						table.setWidthPercentage(105f);

						Phrase pq = new Phrase(new Chunk(img, 5, -63));

						pq.add(new Chunk(newAddress,redFont));
						
			                    
						PdfPCell cellp = new PdfPCell(pq);
						PdfPCell cell1 = new PdfPCell();
						cell1.setBorder(0);

						PdfPTable table351 = new PdfPTable(1);
						table351.setWidths(new float[] {5f});
						table351.setSpacingBefore(10);
						table351.setWidthPercentage(100f);
						
						PdfPCell hcell351;
						hcell351 = new PdfPCell(new Phrase("UDBHAVA HOSPITALS", redFont4));
						hcell351.setBorder(Rectangle.NO_BORDER);
						hcell351.setHorizontalAlignment(Element.ALIGN_CENTER);
						table351.addCell(hcell351);
						
						
						cell1.addElement(table351);
						cell1.addElement(pq);
						cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell1.setColspan(2);
					
						
						PdfPTable table35 = new PdfPTable(1);
						table35.setWidths(new float[] {5f});
						table35.setSpacingBefore(10);
						table35.setWidthPercentage(100f);
						
						PdfPCell hcell35;
						hcell35 = new PdfPCell(new Phrase("INPATIENT DETAILED BILL", headFont));
						hcell35.setBorder(Rectangle.NO_BORDER);
						hcell35.setHorizontalAlignment(Element.ALIGN_CENTER);
						table35.addCell(hcell35);
						
						cell1.addElement(table35);
					


						PdfPTable table3 = new PdfPTable(6);
						table3.setWidths(new float[] { 5f,1f, 5f,5f,1f, 5f });
						table3.setSpacingBefore(10);

						PdfPCell hcell1;
						hcell1 = new PdfPCell(new Phrase("Bill Date", redFont1));
						hcell1.setBorder(Rectangle.NO_BORDER);
						hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell1.setPaddingLeft(-50f);
						table3.addCell(hcell1);
						
						hcell1 = new PdfPCell(new Phrase(":", redFont1));
						hcell1.setBorder(Rectangle.NO_BORDER);
						hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell1.setPaddingLeft(-50f);
						table3.addCell(hcell1);
						
						hcell1 = new PdfPCell(new Phrase(today, redFont1));
						hcell1.setBorder(Rectangle.NO_BORDER);
						hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell1.setPaddingLeft(-50f);
						table3.addCell(hcell1);

											
						PdfPCell hcell10;
						hcell10 = new PdfPCell(new Phrase("Bill No" , redFont1));
						hcell10.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell10);

						hcell10 = new PdfPCell(new Phrase(":" , redFont1));
						hcell10.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell10);

						hcell10 = new PdfPCell(new Phrase(pdfBill , redFont1));
						hcell10.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell10);
						
						PdfPCell hcell2;
						hcell2 = new PdfPCell(new Phrase("Admission No" , redFont1));
						hcell2.setBorder(Rectangle.NO_BORDER);
						hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell2.setPaddingLeft(-50f);
						table3.addCell(hcell2);

						
						hcell2 = new PdfPCell(new Phrase(":", redFont1));
						hcell2.setBorder(Rectangle.NO_BORDER);
						hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell2.setPaddingLeft(-50f);
						table3.addCell(hcell2);

						hcell2 = new PdfPCell(new Phrase(patientRegistration.getRegId(), redFont1));
						hcell2.setBorder(Rectangle.NO_BORDER);
						hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell2.setPaddingLeft(-50f);
						table3.addCell(hcell2);

						
						
						PdfPCell hcell20;
						hcell20 = new PdfPCell(new Phrase("UMR No", redFont1));
						hcell20.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell20);
						
						hcell20 = new PdfPCell(new Phrase(":", redFont1));
						hcell20.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell20);
						
						hcell20 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getUmr(), redFont1));
						hcell20.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell20);

						

						PdfPCell hcell3;
						hcell3 = new PdfPCell(new Phrase("Patient Name", redFont1));
						hcell3.setBorder(Rectangle.NO_BORDER);
						hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell3.setPaddingLeft(-50f);
						table3.addCell(hcell3);

						hcell3 = new PdfPCell(new Phrase(":", redFont1));
						hcell3.setBorder(Rectangle.NO_BORDER);
						hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell3.setPaddingLeft(-50f);
						table3.addCell(hcell3);

						
						hcell3 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getFirstName()+" "+patientRegistration.getPatientDetails().getLastName(), redFont1));
						hcell3.setBorder(Rectangle.NO_BORDER);
						hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell3.setPaddingLeft(-50f);
						table3.addCell(hcell3);

						
						
						PdfPCell hcell30;
						hcell30 = new PdfPCell(new Phrase("S-W-B-D/O", redFont1));
						hcell30.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell30);

						
						hcell30 = new PdfPCell(new Phrase(":", redFont1));
						hcell30.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell30);

						
						hcell30 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getMotherName(), redFont1));//mother name if father name
						hcell30.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell30);

						
						
						//for DOA
						Timestamp timestamp2 = patientRegistration.getDateOfJoining();
						DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa ");

						Calendar calendar2 = Calendar.getInstance();
						calendar2.setTimeInMillis(timestamp2.getTime());

						String doa = dateFormat2.format(calendar2.getTime());
						
						PdfPCell hcell4;
						hcell4 = new PdfPCell(new Phrase("DOA", redFont1));
						hcell4.setBorder(Rectangle.NO_BORDER);
						hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell4.setPaddingLeft(-50f);
						table3.addCell(hcell4);
						
						hcell4 = new PdfPCell(new Phrase(":", redFont1));
						hcell4.setBorder(Rectangle.NO_BORDER);
						hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell4.setPaddingLeft(-50f);
						table3.addCell(hcell4);
						
						hcell4 = new PdfPCell(new Phrase(String.valueOf(doa), redFont1));
						hcell4.setBorder(Rectangle.NO_BORDER);
						hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell4.setPaddingLeft(-50f);
						table3.addCell(hcell4);

					
			
						PdfPCell hcell40;
						hcell40 = new PdfPCell(new Phrase("Admitted Ward", redFont1));
						hcell40.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell40);
						
						hcell40 = new PdfPCell(new Phrase(":", redFont1));
						hcell40.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell40);
						
						hcell40 = new PdfPCell(new Phrase(admittedWard, redFont1));
						hcell40.setBorder(Rectangle.NO_BORDER);
						table3.addCell(hcell40);

						

						PdfPCell hcell5;
						hcell5 = new PdfPCell(new Phrase("Consultant", redFont1));
						hcell5.setBorder(Rectangle.NO_BORDER);
						hcell5.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell5.setPaddingLeft(-50f);
						table3.addCell(hcell5);
						
						hcell5 = new PdfPCell(new Phrase(":", redFont1));
						hcell5.setBorder(Rectangle.NO_BORDER);
						hcell5.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell5.setPaddingLeft(-50f);
						table3.addCell(hcell5);
						
						hcell5 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getConsultant(), redFont1));
						hcell5.setBorder(Rectangle.NO_BORDER);
						hcell5.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell5.setPaddingLeft(-50f);
						table3.addCell(hcell5);

						
						
						PdfPCell hcell50;
						hcell50 = new PdfPCell(new Phrase("Department", redFont1));
						hcell50.setBorder(Rectangle.NO_BORDER);
						//hcell50.setPaddingLeft(40f);
						table3.addCell(hcell50);
						
						hcell50 = new PdfPCell(new Phrase(":", redFont1));
						hcell50.setBorder(Rectangle.NO_BORDER);
						//hcell50.setPaddingLeft(40f);
						table3.addCell(hcell50);
						
						hcell50 = new PdfPCell(new Phrase(dpt, redFont1));
						hcell50.setBorder(Rectangle.NO_BORDER);
						//hcell50.setPaddingLeft(40f);
						table3.addCell(hcell50);

						
						
						PdfPCell hcell6;
						hcell6 = new PdfPCell(new Phrase("Registration No", redFont1));
						hcell6.setBorder(Rectangle.NO_BORDER);
						hcell6.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell6.setPaddingLeft(-50f);
						table3.addCell(hcell6);
						
						hcell6 = new PdfPCell(new Phrase(":", redFont1));
						hcell6.setBorder(Rectangle.NO_BORDER);
						hcell6.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell6.setPaddingLeft(-50f);
						table3.addCell(hcell6);
						
						hcell6 = new PdfPCell(new Phrase(patientRegistration.getRegId(), redFont1));
						hcell6.setBorder(Rectangle.NO_BORDER);
						hcell6.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell6.setPaddingLeft(-50f);
						table3.addCell(hcell6);

						
						
						PdfPCell hcell60;
						hcell60 = new PdfPCell(new Phrase("Age/Sex", redFont1));
						hcell60.setBorder(Rectangle.NO_BORDER);
						//hcell60.setPaddingLeft(40f);
						table3.addCell(hcell60);
						
						hcell60 = new PdfPCell(new Phrase(":", redFont1));
						hcell60.setBorder(Rectangle.NO_BORDER);
						//hcell60.setPaddingLeft(40f);
						table3.addCell(hcell60);
						
						hcell60 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getAge()+"/"+patientRegistration.getPatientDetails().getGender(), redFont1));
						hcell60.setBorder(Rectangle.NO_BORDER);
						//hcell60.setPaddingLeft(40f);
						table3.addCell(hcell60);

						
						PdfPCell hcell7;
						hcell7 = new PdfPCell(new Phrase("Address", redFont1));
						hcell7.setBorder(Rectangle.NO_BORDER);
						hcell7.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell7.setPaddingLeft(-50f);
						table3.addCell(hcell7);
						
						hcell7 = new PdfPCell(new Phrase(":", redFont1));
						hcell7.setBorder(Rectangle.NO_BORDER);
						hcell7.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell7.setPaddingLeft(-50f);
						table3.addCell(hcell7);
						
						hcell7 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getAddress() , redFont1));
						hcell7.setBorder(Rectangle.NO_BORDER);
						hcell7.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell7.setPaddingLeft(-50f);
						table3.addCell(hcell7);

						String refName=null;
						if(patientRegistration.getPatientDetails().getvRefferalDetails()==null)
						{
							refName="";
						}
						else
						{
							refName=patientRegistration.getPatientDetails().getvRefferalDetails().getRefName();
						}
						
						PdfPCell hcell70;
						hcell70 = new PdfPCell(new Phrase("Referal Name", redFont1));
						hcell70.setBorder(Rectangle.NO_BORDER);
					//	hcell70.setPaddingLeft(40f);
						table3.addCell(hcell70);

						
						hcell70 = new PdfPCell(new Phrase(":", redFont1));
						hcell70.setBorder(Rectangle.NO_BORDER);
					//	hcell70.setPaddingLeft(40f);
						table3.addCell(hcell70);

						
						hcell70 = new PdfPCell(new Phrase(refName, redFont1));
						hcell70.setBorder(Rectangle.NO_BORDER);
					//	hcell70.setPaddingLeft(40f);
						table3.addCell(hcell70);
						
						cell1.addElement(table3);

						
						PdfPTable table1 = new PdfPTable(8);
						table1.setWidths(new float[] { 4f, 10f,3f, 2.5f,3f,3f,3f,3f });
						table1.setSpacingBefore(10);
						table1.setWidthPercentage(105f);

						PdfPCell hcell;
						
						hcell = new PdfPCell(new Phrase("Service Code", headFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table1.addCell(hcell);

						hcell = new PdfPCell(new Phrase("Service/Investigation" , headFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table1.addCell(hcell);
						
						hcell = new PdfPCell(new Phrase("Date", headFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell.setPaddingLeft(7);
						table1.addCell(hcell);
						
						hcell = new PdfPCell(new Phrase("Qty", headFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell.setPaddingLeft(18);
						table1.addCell(hcell);

						hcell = new PdfPCell(new Phrase("Rate" , headFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell.setPaddingLeft(15);
						table1.addCell(hcell);
						

						hcell = new PdfPCell(new Phrase("Disc" , headFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell.setPaddingLeft(18);
						table1.addCell(hcell);
						
						hcell = new PdfPCell(new Phrase("Amt" , headFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell.setPaddingLeft(25);
						table1.addCell(hcell);
						

						hcell = new PdfPCell(new Phrase("Paid" , headFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell.setPaddingLeft(25);
						table1.addCell(hcell);
						
			
						
						table1.addCell(hcell);
						
						cell1.addElement(table1);
						
						// Calculating total
						float total = 0;
						float totalMed=0;
						float totalServiceAmt=0;
						float totalAccmAmt=0;
						float totalEqAmt=0;
						for (ChargeBill chargeBillInfo : chargeBillListLab) 
						{
							if (chargeBillInfo.getLabId() != null) 
							{
								
								if(chargeBillInfo.getNetAmount()!=0) {
								chargeBillInfo.setServiceName(chargeBillInfo.getLabId().getServiceName());
								
								total+=chargeBillInfo.getNetAmount();
								}
								
								}
												}
						
						for (ChargeBill chargeMedicine : chargeBillListSale) 
						{
							if (chargeMedicine.getSaleId() != null) 
							{
								
								if(chargeMedicine.getNetAmount()!=0) {
									totalMed+=chargeMedicine.getNetAmount();
								}
								
							}
							
							
						}
						
						for (ChargeBill chargeBillInfo3 : chargeBillListService) 
						{

							
								if (chargeBillInfo3.getServiceId() != null) 
							{
									if(chargeBillInfo3.getServiceId().getServiceType().equalsIgnoreCase("OTHER"))
									{
								if(chargeBillInfo3.getNetAmount()!=0) {	

									totalServiceAmt+=chargeBillInfo3.getNetAmount();
								}
								
									}
									if(chargeBillInfo3.getServiceId().getServiceType().equalsIgnoreCase("Lab"))
									{
										totalServiceAmt+=chargeBillInfo3.getNetAmount();
										
									}}
							}
						
						
						for (ChargeBill chargeBillInfo1 : chargeBillListService) 
						{

							
								if (chargeBillInfo1.getServiceId() != null) 
							{
									if(chargeBillInfo1.getServiceId().getServiceType().equalsIgnoreCase("WARD CHARGES"))
									{
								if(chargeBillInfo1.getNetAmount()!=0)
								{	
									totalAccmAmt+=chargeBillInfo1.getNetAmount();
								}
									}
							}
						}
						
						for (ChargeBill chargeBillInfo1 : chargeBillListService) 
						{

							
								if (chargeBillInfo1.getServiceId() != null) 
							{
									if(chargeBillInfo1.getServiceId().getServiceType().equalsIgnoreCase("EQUIPMENT CHARGES"))
									{
								if(chargeBillInfo1.getNetAmount()!=0)
								{	
									totalEqAmt+=chargeBillInfo1.getNetAmount();
								}
									}
							}
						}


						PdfPTable table11 = new PdfPTable(8);
						table11.setWidths(new float[] {10f, 4f,3f,2.5f, 3f,3f,3f,3f});
						table11.setSpacingBefore(10);
						table11.setWidthPercentage(105f);
						
						PdfPTable table111 = new PdfPTable(8);
						table111.setWidths(new float[] {4f, 10f,3f,2.5f, 3f,3f,3f,3f});
						table111.setSpacingBefore(10);
						table111.setWidthPercentage(105f);

						PdfPCell hcell01;
						if(!chargeBillListLab.isEmpty())
						{
						
						hcell01 = new PdfPCell(new Phrase("LAB CHARGES", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						hcell01 = new PdfPCell(new Phrase(String.valueOf(total), headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell01.setPaddingRight(10);
						table11.addCell(hcell01);
						
						
						hcell01 = new PdfPCell(new Phrase("", headFont));
						hcell01.setBorder(Rectangle.NO_BORDER);
						hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
						table11.addCell(hcell01);
						
						PdfPCell cell;
						for (ChargeBill chargeBillInfo : chargeBillListLab) 
						{
						
						if (chargeBillInfo.getLabId() != null) 
							
						{
							
							if(chargeBillInfo.getNetAmount()!=0) {
							chargeBillInfo.setServiceName(chargeBillInfo.getLabId().getServiceName());
							
							
							
							
							String from =chargeBillInfo.getLabId().getEnteredDate().toString(); 
							Timestamp timestamp = Timestamp.valueOf(from);
							DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(timestamp.getTime());

							String labDate = dateFormat.format(calendar.getTime());

							cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getLabId().getLabServices().getServiceId()), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							table111.addCell(cell);

							cell = new PdfPCell(new Phrase(chargeBillInfo.getServiceName(), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							table111.addCell(cell);
							
							cell = new PdfPCell(new Phrase(labDate , redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							table111.addCell(cell);

							cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getQuantity()), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table111.addCell(cell);

							cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getAmount()), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table111.addCell(cell);
							
							cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getDiscount()), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table111.addCell(cell);

							cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getNetAmount()), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell.setPaddingRight(10);
							table111.addCell(cell);
							
							cell = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getPaid()), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell.setPaddingRight(10);
							table111.addCell(cell);
							
							
							}

						} 					 

						}

						}
						
						else
						{
							hcell01 = new PdfPCell(new Phrase("", headFont));
							hcell01.setBorder(Rectangle.NO_BORDER);
							hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
							table11.addCell(hcell01);
							
							hcell01 = new PdfPCell(new Phrase("", headFont));
							hcell01.setBorder(Rectangle.NO_BORDER);
							hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
							table11.addCell(hcell01);
							
							hcell01 = new PdfPCell(new Phrase("", headFont));
							hcell01.setBorder(Rectangle.NO_BORDER);
							hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
							table11.addCell(hcell01);
							
							hcell01 = new PdfPCell(new Phrase("", headFont));
							hcell01.setBorder(Rectangle.NO_BORDER);
							hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
							table11.addCell(hcell01);
							
							hcell01 = new PdfPCell(new Phrase("", headFont));
							hcell01.setBorder(Rectangle.NO_BORDER);
							hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
							table11.addCell(hcell01);
							
							
							hcell01 = new PdfPCell(new Phrase("", headFont));
							hcell01.setBorder(Rectangle.NO_BORDER);
							hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
							table11.addCell(hcell01);
							
							hcell01 = new PdfPCell(new Phrase("", headFont));
							hcell01.setBorder(Rectangle.NO_BORDER);
							hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell01.setPaddingRight(10);
							table11.addCell(hcell01);
							
							
							hcell01 = new PdfPCell(new Phrase("", headFont));
							hcell01.setBorder(Rectangle.NO_BORDER);
							hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
							table11.addCell(hcell01);
						}
						
										
						
						int count = 0;
						cell1.addElement(table11);
						cell1.addElement(table111);
						
						PdfPTable table12 = new PdfPTable(8);
						table12.setWidths(new float[] {10f, 4f,3f,2.5f, 3f,3f,3f,3f});
						table12.setSpacingBefore(10);
						table12.setWidthPercentage(105f);

						PdfPTable table22 = new PdfPTable(8);
						table22.setWidths(new float[] {4f, 10f,3f,2.5f, 3f,3f,3f,3f});
						table22.setSpacingBefore(10);
						table22.setWidthPercentage(105f);
						
						PdfPCell hcell011;
						if(!chargeBillListSale.isEmpty())
						{
						
							
						hcell011 = new PdfPCell(new Phrase("MEDICINE CHARGES", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
				
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase(String.valueOf(Math.round(totalMed)), headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell011.setPaddingRight(10);
						table12.addCell(hcell011);
						
						hcell011 = new PdfPCell(new Phrase("", headFont));
						hcell011.setBorder(Rectangle.NO_BORDER);
						hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
						table12.addCell(hcell011);
						//charge bill
						
						
						for (ChargeBill chargeMedicine : chargeBillListSale) 
						{
							if (chargeMedicine.getSaleId() != null) 
							{
								if(chargeMedicine.getNetAmount()!=0) {

								salesnetAmount += chargeMedicine.getNetAmount();
								String from =chargeMedicine.getSaleId().getBillDate().toString(); 
								Timestamp timestamp = Timestamp.valueOf(from);
								DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(timestamp.getTime());

								String medicineDate = dateFormat.format(calendar.getTime());

								PdfPCell hcell0;
								
								hcell0 = new PdfPCell(new Phrase(String.valueOf(chargeMedicine.getSaleId().getPatientSalesMedicineDetails().getMedicineId()), redFont1));
								hcell0.setBorder(Rectangle.NO_BORDER);
								hcell0.setHorizontalAlignment(Element.ALIGN_LEFT);
								table22.addCell(hcell0);
								
								hcell0 = new PdfPCell(new Phrase(String.valueOf(chargeMedicine.getSaleId().getMedicineName()), redFont1));
								hcell0.setBorder(Rectangle.NO_BORDER);
								hcell0.setHorizontalAlignment(Element.ALIGN_LEFT);
								table22.addCell(hcell0);
								
								hcell0 = new PdfPCell(new Phrase(medicineDate, redFont1));
								hcell0.setBorder(Rectangle.NO_BORDER);
								hcell0.setHorizontalAlignment(Element.ALIGN_LEFT);
								table22.addCell(hcell0);
								
								hcell0 = new PdfPCell(new Phrase(String.valueOf(chargeMedicine.getSaleId().getQuantity()), redFont1));
								hcell0.setBorder(Rectangle.NO_BORDER);
								hcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
								table22.addCell(hcell0);
								
								hcell0 = new PdfPCell(new Phrase(String.valueOf(Math.round(chargeMedicine.getSaleId().getMrp())), redFont1));
								hcell0.setBorder(Rectangle.NO_BORDER);
								hcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
								table22.addCell(hcell0);
								
								hcell0 = new PdfPCell(new Phrase(String.valueOf(Math.round(chargeMedicine.getSaleId().getDiscount())), redFont1));
								hcell0.setBorder(Rectangle.NO_BORDER);
								hcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
								table22.addCell(hcell0);
								
								hcell0 = new PdfPCell(new Phrase(String.valueOf(Math.round(chargeMedicine.getNetAmount())), redFont1));
								hcell0.setBorder(Rectangle.NO_BORDER);
								hcell0.setHorizontalAlignment(Element.ALIGN_RIGHT);
								hcell0.setPaddingRight(10);
								table22.addCell(hcell0);
								
								hcell0 = new PdfPCell(new Phrase((chargeMedicine.getPaid()), redFont1));
								hcell0.setBorder(Rectangle.NO_BORDER);
								hcell0.setHorizontalAlignment(Element.ALIGN_RIGHT);
								hcell0.setPaddingRight(10);
								table22.addCell(hcell0);
					
								}

							}
						}
						
						
						
						
						}
						else
						{
							hcell011 = new PdfPCell(new Phrase("", headFont));
							hcell011.setBorder(Rectangle.NO_BORDER);
							hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
							table12.addCell(hcell011);
							
							hcell011 = new PdfPCell(new Phrase("", headFont));
							hcell011.setBorder(Rectangle.NO_BORDER);
							hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
							table12.addCell(hcell011);
							
							hcell011 = new PdfPCell(new Phrase("", headFont));
							hcell011.setBorder(Rectangle.NO_BORDER);
							hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
							table12.addCell(hcell011);
							
							hcell011 = new PdfPCell(new Phrase("", headFont));
							hcell011.setBorder(Rectangle.NO_BORDER);
							hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
							table12.addCell(hcell011);
							
					
							hcell011 = new PdfPCell(new Phrase("", headFont));
							hcell011.setBorder(Rectangle.NO_BORDER);
							hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
							table12.addCell(hcell011);
							
							hcell011 = new PdfPCell(new Phrase("", headFont));
							hcell011.setBorder(Rectangle.NO_BORDER);
							hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
							table12.addCell(hcell011);
							
							hcell011 = new PdfPCell(new Phrase("", headFont));
							hcell011.setBorder(Rectangle.NO_BORDER);
							hcell011.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell011.setPaddingRight(10);
							table12.addCell(hcell011);
							
							hcell011 = new PdfPCell(new Phrase("", headFont));
							hcell011.setBorder(Rectangle.NO_BORDER);
							hcell011.setHorizontalAlignment(Element.ALIGN_LEFT);
							table12.addCell(hcell011);

						}
					
						cell1.addElement(table12);
						
						cell1.addElement(table22);
															
						//----------------------SERVICES------------------------
						
						PdfPTable table13 = new PdfPTable(8);
						table13.setWidths(new float[] {10f, 4f,3f,2.5f, 3f,3f,3f,3f});
						table13.setSpacingBefore(10);
						table13.setWidthPercentage(105f);
						
						PdfPTable table131 = new PdfPTable(8);
						table131.setWidths(new float[] {4f, 10f,3f,2.5f, 3f,3f,3f,3f});
						table131.setSpacingBefore(10);
						table131.setWidthPercentage(105f);

						PdfPCell hcell0111;
						if(!chargeBillListService.isEmpty())
						{
							if(chargeBillListService.stream().filter((s) -> s.getServiceId().getServiceType().equalsIgnoreCase("OTHER")).count()>0)
						{
						hcell0111 = new PdfPCell(new Phrase("SERVICE CHARGES", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_LEFT);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						table13.addCell(hcell0111);
			
						
						
						hcell0111 = new PdfPCell(new Phrase(String.valueOf(totalServiceAmt), headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						hcell0111.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell0111.setPaddingRight(10);
						table13.addCell(hcell0111);
						
						hcell0111 = new PdfPCell(new Phrase("", headFont));
						hcell0111.setBorder(Rectangle.NO_BORDER);
						table13.addCell(hcell0111);
						
						

						for (ChargeBill chargeBillInfo : chargeBillListService) 
						{

							
								if (chargeBillInfo.getServiceId() != null && chargeBillInfo.getServiceId().getServiceType().equalsIgnoreCase("OTHER") || chargeBillInfo.getServiceId().getServiceType().equalsIgnoreCase("LAB")) 
							{
									
								if(chargeBillInfo.getNetAmount()!=0) {	
									

									String from =chargeBillInfo.getInsertedDate().toString(); 
									Timestamp timestamp = Timestamp.valueOf(from);
									DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

									Calendar calendar = Calendar.getInstance();
									calendar.setTimeInMillis(timestamp.getTime());

									String serviceDate = dateFormat.format(calendar.getTime());

									

									
									PdfPCell cell11;
								chargeBillInfo.setServiceName(chargeBillInfo.getServiceId().getServiceName());

								
								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getServiceId().getServiceId()), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table131.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(chargeBillInfo.getServiceName(), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table131.addCell(cell11);
								
								

								cell11 = new PdfPCell(new Phrase(serviceDate, redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table131.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getQuantity()), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
								table131.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getMrp()), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
								table131.addCell(cell11);
								
								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getDiscount()), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
								table131.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo.getNetAmount()), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell11.setPaddingRight(10);
								table131.addCell(cell11);
								
								cell11 = new PdfPCell(new Phrase(chargeBillInfo.getPaid(), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell11.setPaddingRight(10);
								
								table131.addCell(cell11);

								
								}
									}
							
							else 
							{
								chargeBillInfo.setServiceName("NOT APPLICABLE");
							}

						}
						
						}

						}
						else
						{
							hcell0111 = new PdfPCell(new Phrase("", headFont));
							hcell0111.setBorder(Rectangle.NO_BORDER);
							hcell0111.setHorizontalAlignment(Element.ALIGN_LEFT);
							table13.addCell(hcell0111);
							
							hcell0111 = new PdfPCell(new Phrase("", headFont));
							hcell0111.setBorder(Rectangle.NO_BORDER);
							hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
							table13.addCell(hcell0111);
							
							hcell0111 = new PdfPCell(new Phrase("", headFont));
							hcell0111.setBorder(Rectangle.NO_BORDER);
							hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
							table13.addCell(hcell0111);
							
							hcell0111 = new PdfPCell(new Phrase("", headFont));
							hcell0111.setBorder(Rectangle.NO_BORDER);
							hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
							table13.addCell(hcell0111);
							
							hcell0111 = new PdfPCell(new Phrase("", headFont));
							hcell0111.setBorder(Rectangle.NO_BORDER);
							hcell0111.setHorizontalAlignment(Element.ALIGN_CENTER);
							table13.addCell(hcell0111);
							
							hcell0111 = new PdfPCell(new Phrase("", headFont));
							hcell0111.setBorder(Rectangle.NO_BORDER);
							table13.addCell(hcell0111);
				
							
							
							hcell0111 = new PdfPCell(new Phrase("", headFont));
							hcell0111.setBorder(Rectangle.NO_BORDER);
							hcell0111.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell0111.setPaddingRight(10);
							table13.addCell(hcell0111);
							
							hcell0111 = new PdfPCell(new Phrase("", headFont));
							hcell0111.setBorder(Rectangle.NO_BORDER);
							table13.addCell(hcell0111);
						}
							

						

						cell1.addElement(table13);
						cell1.addElement(table131);
						
						
						//----------------------------------
						PdfPTable table132 = new PdfPTable(8);
						table132.setWidths(new float[] {10f, 4f,3f,2.5f, 3f,3f,3f,3f});
						table132.setSpacingBefore(10);
						table132.setWidthPercentage(105f);
						
						PdfPTable table281 = new PdfPTable(8);
						table281.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f,3f});
						table281.setSpacingBefore(10);
						table281.setWidthPercentage(105f);
						

						PdfPCell hcell01111;
						if(!chargeBillListService.isEmpty())
						{
							System.out.println("INSIDE ward charge");
					
						if(chargeBillListService.stream().filter((s) -> s.getServiceId().getServiceType().equalsIgnoreCase("WARD CHARGES")).count()>0)
						{
								System.out.println("INSIDE ward charges of if");
						hcell01111 = new PdfPCell(new Phrase("WARD CHARGES", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_LEFT);
						table132.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table132.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table132.addCell(hcell01111);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
						table132.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						table132.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						table132.addCell(hcell01111);
			
						hcell01111 = new PdfPCell(new Phrase(String.valueOf(totalAccmAmt), headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						hcell01111.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell01111.setPaddingRight(10);
						table132.addCell(hcell01111);
						
						hcell01111 = new PdfPCell(new Phrase("", headFont));
						hcell01111.setBorder(Rectangle.NO_BORDER);
						table132.addCell(hcell01111);
						

						for (ChargeBill chargeBillInfo3 : chargeBillListService) 
						{

							
								if (chargeBillInfo3.getServiceId() != null) 
							{
									if(chargeBillInfo3.getServiceId().getServiceType().equalsIgnoreCase("WARD CHARGES"))
									{
								if(chargeBillInfo3.getNetAmount()!=0) {	

									String from =chargeBillInfo3.getInsertedDate().toString(); 
									Timestamp timestamp = Timestamp.valueOf(from);
									DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

									Calendar calendar = Calendar.getInstance();
									calendar.setTimeInMillis(timestamp.getTime());

									String serviceDate = dateFormat.format(calendar.getTime());

									

									
									PdfPCell cell11;
								chargeBillInfo3.setServiceName(chargeBillInfo3.getServiceId().getServiceName());

								
								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getServiceId().getServiceId()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table281.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(chargeBillInfo3.getServiceName(), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table281.addCell(cell11);
								
								

								cell11 = new PdfPCell(new Phrase(serviceDate, redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table281.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getQuantity()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
								table281.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getMrp()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
								table281.addCell(cell11);
								
								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getDiscount()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
								table281.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getNetAmount()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell11.setPaddingRight(10);
								table281.addCell(cell11);
								
								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getPaid()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell11.setPaddingRight(10);
								table281.addCell(cell11);
								
								
				
								}
									}
							} 
							else 
							{
								chargeBillInfo3.setServiceName("NOT APPLICABLE");
							}

						}
						

						
						}
						}
						else
						{
							System.out.println("OUTSIDE ward charge");
							hcell01111 = new PdfPCell(new Phrase("", headFont));
							hcell01111.setBorder(Rectangle.NO_BORDER);
							hcell01111.setHorizontalAlignment(Element.ALIGN_LEFT);
							table132.addCell(hcell01111);
							
							hcell01111 = new PdfPCell(new Phrase("", headFont));
							hcell01111.setBorder(Rectangle.NO_BORDER);
							hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
							table132.addCell(hcell01111);
							
							hcell01111 = new PdfPCell(new Phrase("", headFont));
							hcell01111.setBorder(Rectangle.NO_BORDER);
							hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
							table132.addCell(hcell01111);
							
							hcell01111 = new PdfPCell(new Phrase("", headFont));
							hcell01111.setBorder(Rectangle.NO_BORDER);
							hcell01111.setHorizontalAlignment(Element.ALIGN_CENTER);
							table132.addCell(hcell01111);
							
							hcell01111 = new PdfPCell(new Phrase("", headFont));
							hcell01111.setBorder(Rectangle.NO_BORDER);
							table132.addCell(hcell01111);
							
							hcell01111 = new PdfPCell(new Phrase("", headFont));
							hcell01111.setBorder(Rectangle.NO_BORDER);
							table132.addCell(hcell01111);
				
							hcell01111 = new PdfPCell(new Phrase("", headFont));
							hcell01111.setBorder(Rectangle.NO_BORDER);
							hcell01111.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell01111.setPaddingRight(10);
							table132.addCell(hcell01111);
							
							hcell01111 = new PdfPCell(new Phrase("", headFont));
							hcell01111.setBorder(Rectangle.NO_BORDER);
							table132.addCell(hcell01111);

						}
						
						
					
					
						cell1.addElement(table132);
						cell1.addElement(table281);
						
						
						PdfPTable table133 = new PdfPTable(8);
						table133.setWidths(new float[] {10f, 4f,3f,2.5f, 3f,3f,3f,3f});
						table133.setSpacingBefore(10);
						table133.setWidthPercentage(105f);
						
						PdfPTable table282 = new PdfPTable(8);
						table282.setWidths(new float[] {4f, 10f,3f, 2.5f,3f,3f,3f,3f});
						table282.setSpacingBefore(10);
						table282.setWidthPercentage(105f);

						PdfPCell hcell013;
						if(!chargeBillListService.isEmpty())
						{
							if(chargeBillListService.stream().filter((s) -> s.getServiceId().getServiceType().equalsIgnoreCase("EQUIPMENT CHARGES")).count()>0)
						{
							
						hcell013 = new PdfPCell(new Phrase("EQUIPMENT CHARGES", headFont));
						hcell013.setBorder(Rectangle.NO_BORDER);
						hcell013.setHorizontalAlignment(Element.ALIGN_LEFT);
						table133.addCell(hcell013);
						
						hcell013 = new PdfPCell(new Phrase("", headFont));
						hcell013.setBorder(Rectangle.NO_BORDER);
						hcell013.setHorizontalAlignment(Element.ALIGN_CENTER);
						table133.addCell(hcell013);
						
						hcell013 = new PdfPCell(new Phrase("", headFont));
						hcell013.setBorder(Rectangle.NO_BORDER);
						hcell013.setHorizontalAlignment(Element.ALIGN_CENTER);
						table133.addCell(hcell013);
						hcell013.setHorizontalAlignment(Element.ALIGN_CENTER);
						
						hcell013 = new PdfPCell(new Phrase("", headFont));
						hcell013.setBorder(Rectangle.NO_BORDER);
						hcell013.setHorizontalAlignment(Element.ALIGN_CENTER);
						table133.addCell(hcell013);
						
						hcell013 = new PdfPCell(new Phrase("", headFont));
						hcell013.setBorder(Rectangle.NO_BORDER);
						table133.addCell(hcell013);
						
						hcell013 = new PdfPCell(new Phrase("", headFont));
						hcell013.setBorder(Rectangle.NO_BORDER);
						table133.addCell(hcell013);
			
						hcell013 = new PdfPCell(new Phrase(String.valueOf(totalEqAmt), headFont));
						hcell013.setBorder(Rectangle.NO_BORDER);
						hcell013.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell013.setPaddingRight(10);
						table133.addCell(hcell013);
						
						hcell013 = new PdfPCell(new Phrase("", headFont));
						hcell013.setBorder(Rectangle.NO_BORDER);
						table133.addCell(hcell013);
						

						for (ChargeBill chargeBillInfo3 : chargeBillListService) 
						{

							
								if (chargeBillInfo3.getServiceId() != null) 
							{
									if(chargeBillInfo3.getServiceId().getServiceType().equalsIgnoreCase("EQUIPMENT CHARGES"))
									{
								if(chargeBillInfo3.getNetAmount()!=0) {	

									String from =chargeBillInfo3.getInsertedDate().toString(); 
									Timestamp timestamp = Timestamp.valueOf(from);
									DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

									Calendar calendar = Calendar.getInstance();
									calendar.setTimeInMillis(timestamp.getTime());

									String serviceDate = dateFormat.format(calendar.getTime());

									

									
									PdfPCell cell11;
								chargeBillInfo3.setServiceName(chargeBillInfo3.getServiceId().getServiceName());

								
								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getServiceId().getServiceId()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table282.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(chargeBillInfo3.getServiceName(), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table282.addCell(cell11);
								
								

								cell11 = new PdfPCell(new Phrase(serviceDate, redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table282.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getQuantity()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
								table282.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getMrp()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
								table282.addCell(cell11);
								
								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getDiscount()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
								table282.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getNetAmount()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell11.setPaddingRight(10);
								table282.addCell(cell11);
								
								cell11 = new PdfPCell(new Phrase(String.valueOf(chargeBillInfo3.getPaid()), redFont9));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell11.setPaddingRight(10);
								table282.addCell(cell11);
								
								
				
								}
									}
							} 
							else 
							{
								chargeBillInfo3.setServiceName("NOT APPLICABLE");
							}

						}
						
						
						}
						}
						else
						{
							hcell013 = new PdfPCell(new Phrase("", headFont));
							hcell013.setBorder(Rectangle.NO_BORDER);
							hcell013.setHorizontalAlignment(Element.ALIGN_LEFT);
							table133.addCell(hcell013);
							
							hcell013 = new PdfPCell(new Phrase("", headFont));
							hcell013.setBorder(Rectangle.NO_BORDER);
							hcell013.setHorizontalAlignment(Element.ALIGN_CENTER);
							table133.addCell(hcell013);
							
							hcell013 = new PdfPCell(new Phrase("", headFont));
							hcell013.setBorder(Rectangle.NO_BORDER);
							hcell013.setHorizontalAlignment(Element.ALIGN_CENTER);
							table133.addCell(hcell013);
							hcell013.setHorizontalAlignment(Element.ALIGN_CENTER);
							
							hcell013 = new PdfPCell(new Phrase("", headFont));
							hcell013.setBorder(Rectangle.NO_BORDER);
							hcell013.setHorizontalAlignment(Element.ALIGN_CENTER);
							table133.addCell(hcell013);
							
							hcell013 = new PdfPCell(new Phrase("", headFont));
							hcell013.setBorder(Rectangle.NO_BORDER);
							table133.addCell(hcell013);
							
							hcell013 = new PdfPCell(new Phrase("", headFont));
							hcell013.setBorder(Rectangle.NO_BORDER);
							table133.addCell(hcell013);
				
							hcell013 = new PdfPCell(new Phrase("", headFont));
							hcell013.setBorder(Rectangle.NO_BORDER);
							hcell013.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell013.setPaddingRight(10);
							table133.addCell(hcell013);
							
							hcell013 = new PdfPCell(new Phrase("", headFont));
							hcell013.setBorder(Rectangle.NO_BORDER);
							table133.addCell(hcell013);
				
						}
						
					
						
						

						cell1.addElement(table133);
						cell1.addElement(table282);
						
						PdfPTable table182 = new PdfPTable(1);
						table182.setWidths(new float[] {5f});
						table182.setSpacingBefore(10);
						table182.setWidthPercentage(100f);
						
						PdfPCell hcell072;
						hcell072 = new PdfPCell(new Phrase("_________________________________________________________________________", headFont));
						hcell072.setBorder(Rectangle.NO_BORDER);
						hcell072.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table182.addCell(hcell072);
						
						float totalAmount=0;
						totalAmount=total+totalMed+totalServiceAmt+totalAccmAmt+totalEqAmt;
						
						PdfPTable table14 = new PdfPTable(5);
						table14.setWidths(new float[] {2f, 1f,10f, 5f,5f});
						table14.setSpacingBefore(10);
						table14.setWidthPercentage(100f);

						PdfPCell hcell02;
						
						hcell02 = new PdfPCell(new Phrase("", headFont));
						hcell02.setBorder(Rectangle.NO_BORDER);
						hcell02.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell02);
						
						hcell02 = new PdfPCell(new Phrase("", headFont));
						hcell02.setBorder(Rectangle.NO_BORDER);
						hcell02.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell02);
						
						hcell02 = new PdfPCell(new Phrase("NET AMOUNT", headFont));
						hcell02.setBorder(Rectangle.NO_BORDER);
						hcell02.setHorizontalAlignment(Element.ALIGN_RIGHT);
					    hcell02.setPaddingRight(-55f);
						table14.addCell(hcell02);
						
						hcell02 = new PdfPCell(new Phrase(":", headFont));
						hcell02.setBorder(Rectangle.NO_BORDER);
						hcell02.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table14.addCell(hcell02);
						
						hcell02 = new PdfPCell(new Phrase(String.valueOf(Math.round(totalAmount)), headFont));
						hcell02.setBorder(Rectangle.NO_BORDER);
						hcell02.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell02.setPaddingRight(50);
						table14.addCell(hcell02);
						
		
						PdfPCell hcell021;
						
						hcell021 = new PdfPCell(new Phrase("", headFont));
						hcell021.setBorder(Rectangle.NO_BORDER);
						hcell021.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell021);
						
						hcell021 = new PdfPCell(new Phrase("", headFont));
						hcell021.setBorder(Rectangle.NO_BORDER);
						hcell021.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell021);
						
						hcell021 = new PdfPCell(new Phrase("PAID AMOUNT", headFont));
						hcell021.setBorder(Rectangle.NO_BORDER);
						hcell021.setHorizontalAlignment(Element.ALIGN_RIGHT);
					    hcell021.setPaddingRight(-55f);
						table14.addCell(hcell021);
						
						hcell021 = new PdfPCell(new Phrase(":", headFont));
						hcell021.setBorder(Rectangle.NO_BORDER);
						hcell021.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table14.addCell(hcell021);
						
						hcell021 = new PdfPCell(new Phrase(String.valueOf(Math.round(paidSum)), headFont));
						hcell021.setBorder(Rectangle.NO_BORDER);
						hcell021.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell021.setPaddingRight(50);
						table14.addCell(hcell021);
						
		
						
						PdfPCell hcell03;
						
						hcell03 = new PdfPCell(new Phrase("", headFont));
						hcell03.setBorder(Rectangle.NO_BORDER);
						hcell03.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell03);
						
						hcell03 = new PdfPCell(new Phrase("", headFont));
						hcell03.setBorder(Rectangle.NO_BORDER);
						hcell03.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell03);
						
						hcell03 = new PdfPCell(new Phrase("ADVANCE AMOUNT", headFont));
						hcell03.setBorder(Rectangle.NO_BORDER);
						hcell03.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell03.setPaddingRight(-83f);
						table14.addCell(hcell03);
						
						hcell03 = new PdfPCell(new Phrase(":", headFont));
						hcell03.setBorder(Rectangle.NO_BORDER);
						hcell03.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table14.addCell(hcell03);
						
						hcell03 = new PdfPCell(new Phrase(String.valueOf(patientRegistration.getAdvanceAmount()), headFont));
						hcell03.setBorder(Rectangle.NO_BORDER);
						hcell03.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell03.setPaddingRight(50);
						table14.addCell(hcell03);
						
						float dueAmt = totalAmount-paidSum - patientRegistration.getAdvanceAmount();
						PdfPCell hcell04;
						
						hcell04 = new PdfPCell(new Phrase("", headFont));
						hcell04.setBorder(Rectangle.NO_BORDER);
						hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell04);
						
						hcell04 = new PdfPCell(new Phrase("", headFont));
						hcell04.setBorder(Rectangle.NO_BORDER);
						hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
						table14.addCell(hcell04);
						
						hcell04 = new PdfPCell(new Phrase("DUE AMOUNT", headFont));
						hcell04.setBorder(Rectangle.NO_BORDER);
						hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell04.setPaddingRight(-55f);
						table14.addCell(hcell04);
						
						hcell04 = new PdfPCell(new Phrase(":", headFont));
						hcell04.setBorder(Rectangle.NO_BORDER);
						hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table14.addCell(hcell04);
						
						hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(dueAmt)), headFont));
						hcell04.setBorder(Rectangle.NO_BORDER);
						hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell04.setPaddingRight(50);
						table14.addCell(hcell04);
						
						cell1.addElement(table182);
						cell1.addElement(table14);
					
						PdfPTable table15 = new PdfPTable(1);
						table15.setWidths(new float[] {5f});
						table15.setSpacingBefore(10);
						table15.setWidthPercentage(100f);
						
						PdfPCell hcell05;
						hcell05 = new PdfPCell(new Phrase("\n RECEIPT DETAILS", headFont));
						hcell05.setBorder(Rectangle.NO_BORDER);
						table15.addCell(hcell05);
						
						
						cell1.addElement(table15);
						
						
						PdfPTable table16 = new PdfPTable(6);
						table16.setWidths(new float[] {5f,6f,5f,6f,4f,4f});
						table16.setSpacingBefore(10);
						table16.setWidthPercentage(100f);
						
						PdfPCell hcell06;
						hcell06 = new PdfPCell(new Phrase("Receipt No", headFont));
						hcell06.setBorder(Rectangle.NO_BORDER);
						hcell06.setBackgroundColor(BaseColor.GRAY);
						table16.addCell(hcell06);
						
						hcell06 = new PdfPCell(new Phrase("Receipt Date", headFont));
						hcell06.setBorder(Rectangle.NO_BORDER);
						hcell06.setBackgroundColor(BaseColor.GRAY);
						table16.addCell(hcell06);
						
						hcell06 = new PdfPCell(new Phrase("Receipt Name", headFont));
						hcell06.setBorder(Rectangle.NO_BORDER);
						hcell06.setBackgroundColor(BaseColor.GRAY);
						table16.addCell(hcell06);
						
						hcell06 = new PdfPCell(new Phrase("Mode Of Payment", headFont));
						hcell06.setBorder(Rectangle.NO_BORDER);
						hcell06.setBackgroundColor(BaseColor.GRAY);
						table16.addCell(hcell06);
					
						hcell06 = new PdfPCell(new Phrase("Receipt Amt", headFont));
						hcell06.setBorder(Rectangle.NO_BORDER);
						hcell06.setBackgroundColor(BaseColor.GRAY);
						table16.addCell(hcell06);
						
						hcell06 = new PdfPCell(new Phrase("Remarks", headFont));
						hcell06.setBorder(Rectangle.NO_BORDER);
						hcell06.setBackgroundColor(BaseColor.GRAY);
						table16.addCell(hcell06);
						
						for (PatientPayment patientPaymentInfo : patientPayment) 
						{
							
							Timestamp timestamp =patientPaymentInfo.getInsertedDate(); 
							DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");
							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(timestamp.getTime());
						    String from=  dateFormat.format(calendar.getTime());
						   
							if (patientPaymentInfo.getTypeOfCharge().equalsIgnoreCase("ADVANCE")) 
							{
								PdfPCell cell11;

								cell11 = new PdfPCell(new Phrase(String.valueOf(patientPaymentInfo.getPaymentId()), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table16.addCell(cell11);

								cell11 = new PdfPCell(new Phrase(from, redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table16.addCell(cell11);
		
								cell11 = new PdfPCell(new Phrase(patientPaymentInfo.getTypeOfCharge(), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table16.addCell(cell11);

								if(patientPaymentInfo.getModeOfPaymant()!=null)
								{
								cell11 = new PdfPCell(new Phrase(String.valueOf(patientPaymentInfo.getModeOfPaymant()), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
								table16.addCell(cell11);
								}
								else
								{
									cell11 = new PdfPCell(new Phrase(" ", redFont));
									cell11.setBorder(Rectangle.NO_BORDER);
									cell11.setHorizontalAlignment(Element.ALIGN_LEFT);
									table16.addCell(cell11);
				
								}
								cell11 = new PdfPCell(new Phrase(String.valueOf(patientPaymentInfo.getAmount()), redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell11.setPaddingRight(23);
								table16.addCell(cell11);
							
								cell11 = new PdfPCell(new Phrase("", redFont));
								cell11.setBorder(Rectangle.NO_BORDER);
								cell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
								table16.addCell(cell11);

								totalRecieptAmt=totalRecieptAmt+patientPaymentInfo.getAmount();
							}

						}
						
						cell1.addElement(table16);
						
						
						
						PdfPTable table18 = new PdfPTable(1);
						table18.setWidths(new float[] {5f});
						table18.setSpacingBefore(10);
						table18.setWidthPercentage(100f);
						
						PdfPCell hcell071;
						hcell071 = new PdfPCell(new Phrase("_________________________________________________________________________", headFont));
						hcell071.setBorder(Rectangle.NO_BORDER);
						hcell071.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table18.addCell(hcell071);
						
						
						
						PdfPTable table17 = new PdfPTable(6);
						table17.setWidths(new float[] {5f,6f,5f,6f,4f,4f});
						table17.setSpacingBefore(10);
						table17.setWidthPercentage(100f);
						
						PdfPCell hcell07;
						hcell07 = new PdfPCell(new Phrase("", headFont));
						hcell07.setBorder(Rectangle.NO_BORDER);
						table17.addCell(hcell07);
						
						hcell07 = new PdfPCell(new Phrase("", headFont));
						hcell07.setBorder(Rectangle.NO_BORDER);
						table17.addCell(hcell07);
						
						hcell07 = new PdfPCell(new Phrase("Total : ", headFont));
						hcell07.setBorder(Rectangle.NO_BORDER);
						table17.addCell(hcell07);
						
						hcell07 = new PdfPCell(new Phrase("", headFont));
						hcell07.setBorder(Rectangle.NO_BORDER);
						table17.addCell(hcell07);
						
						hcell07 = new PdfPCell(new Phrase(String.valueOf(totalRecieptAmt), headFont));
						hcell07.setBorder(Rectangle.NO_BORDER);
						hcell07.setHorizontalAlignment(Element.ALIGN_CENTER);
						table17.addCell(hcell07);
					
						
						hcell07 = new PdfPCell(new Phrase("", headFont));
						hcell07.setBorder(Rectangle.NO_BORDER);
						table17.addCell(hcell07);

						
						cell1.addElement(table18);
						cell1.addElement(table17);
						
						
						PdfPTable table181 = new PdfPTable(2);
						table181.setWidths(new float[] {8f,8f});
						table181.setSpacingBefore(10);
						table181.setWidthPercentage(100f);
						
						PdfPCell hcell08;
						hcell08 = new PdfPCell(new Phrase("Total Received Amount In Words : ", headFont));
						hcell08.setBorder(Rectangle.NO_BORDER);
						hcell08.setPaddingTop(10f);
						table181.addCell(hcell08);
						
						hcell08 = new PdfPCell(new Phrase(numberToWordsConverter.convert(totalRecieptAmt) + " Rupees Only", redFont1));
						hcell08.setBorder(Rectangle.NO_BORDER);
						hcell08.setPaddingTop(10f);
						hcell08.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell08.setPaddingLeft(-50f);
						table181.addCell(hcell08);
						
						
						PdfPCell hcell081;
						hcell081 = new PdfPCell(new Phrase("Net Amount In Words : ", headFont));
						hcell081.setBorder(Rectangle.NO_BORDER);
						hcell081.setPaddingTop(10f);
						table181.addCell(hcell081);
						
						hcell081 = new PdfPCell(new Phrase(numberToWordsConverter.convert(Math.round(total + salesnetAmount)) + " Rupees Only", redFont1));
						
						hcell081.setBorder(Rectangle.NO_BORDER);
						hcell081.setPaddingTop(10f);
						hcell081.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell081.setPaddingLeft(-50f);
						table181.addCell(hcell081);
						
						cell1.addElement(table181);
						
					
						PdfPTable table20 = new PdfPTable(1);
						table20.setWidths(new float[] {8f});
						table20.setSpacingBefore(10);
						table20.setWidthPercentage(100f);
						
						PdfPCell hcell09;
						hcell09 = new PdfPCell(new Phrase("(Authorized Signature)", headFont));
						hcell09.setBorder(Rectangle.NO_BORDER);
						hcell09.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell09.setPaddingTop(20f);
						table20.addCell(hcell09);
						
				 
						cell1.addElement(table20);
						
						
						PdfPTable table33 = new PdfPTable(4);
						table33.setWidthPercentage(100.0f);
						table33.setWidths(new int[] {3, 4, 3, 4});
						table33.setSpacingBefore(10);

						
						PdfPCell hcell16;
						hcell16 = new PdfPCell(new Phrase("Created By                    :  ", headFont));
						hcell16.setBorder(Rectangle.NO_BORDER);
						hcell16.setPaddingLeft(-10f);
						table33.addCell(hcell16);

						hcell16 = new PdfPCell(new Phrase(createdName , redFont1));
						hcell16.setBorder(Rectangle.NO_BORDER);
						//hcell16.setPaddingRight(-70f);
						hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
						table33.addCell(hcell16);
						
						hcell16 = new PdfPCell(new Phrase("Created Dt                   : " , headFont));
						hcell16.setBorder(Rectangle.NO_BORDER);
						hcell16.setPaddingLeft(-20f);
						table33.addCell(hcell16);
						
						hcell16 = new PdfPCell(new Phrase(today , redFont1));
						hcell16.setBorder(Rectangle.NO_BORDER);
						//hcell16.setPaddingRight(-70f);
						hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
						table33.addCell(hcell16);
						
						
						
						PdfPCell hcell161;
						hcell161 = new PdfPCell(new Phrase("Printed By                     :  ", headFont));
						hcell161.setBorder(Rectangle.NO_BORDER);
						hcell161.setPaddingLeft(-10f);
						table33.addCell(hcell161);

						hcell161 = new PdfPCell(new Phrase(createdName , redFont1));
						hcell161.setBorder(Rectangle.NO_BORDER);
						//hcell161.setPaddingRight(-70f);
						hcell161.setHorizontalAlignment(Element.ALIGN_LEFT);
						table33.addCell(hcell161);
						
						hcell161 = new PdfPCell(new Phrase("Printed Dt                    : " , headFont));
						hcell161.setBorder(Rectangle.NO_BORDER);
						hcell161.setPaddingLeft(-20f);
						table33.addCell(hcell161);
						
						hcell161 = new PdfPCell(new Phrase(today , redFont1));
						hcell161.setBorder(Rectangle.NO_BORDER);
						//hcell161.setPaddingRight(-70f);
						hcell161.setHorizontalAlignment(Element.ALIGN_LEFT);
						table33.addCell(hcell161);
						
						
						cell1.addElement(table33);

						table.addCell(cell1);
						document.add(table);

						document.close();
					
					System.out.println("finished");

					pdfByte = byteArrayOutputStream.toByteArray();
					String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
							.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();

					salesPaymentPdf = new SalesPaymentPdf();
					salesPaymentPdf.setFileName(regId+" Approximate Bill");
					salesPaymentPdf.setFileuri(uri);
					salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
					salesPaymentPdf.setData(pdfByte);
					salesPaymentPdfServiceImpl.save(salesPaymentPdf);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

			return salesPaymentPdf;	
			
		
			
		}
			
	/*
	 * For OUTPATIENT OP BILL RECEIPT
	 */
	@Transactional
	@RequestMapping(value="/opbill/{regId}",method=RequestMethod.GET)
	public SalesPaymentPdf opBillReceipt(@PathVariable String regId,Principal principal) throws Exception{
		
		PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(regId);
		if(patientRegistration.getpType().equalsIgnoreCase("INPATIENT"))
		{
			throw new RuntimeException("INPATIENT NOT ALLOWED");
		}
		return chargeBillServiceImpl.getOpBillReceipt(regId,principal);
		
	}
	
	/*
	 * To get due bill for patient pharmacy
	 */
	@Transactional
	@RequestMapping(value="/due/{regId}",method=RequestMethod.GET)
	public List<Object> dueBill(@PathVariable String regId)
	{
		List<Object> display=new ArrayList<>();
		
		//List<PatientPayment> patientPayments = patientPaymentServiceImpl.findDueBill(regId);
		//display.add(patientPayments);
		//List<ChargeBill> chargeBills = chargeBillServiceImpl.findDueBill(regId);
		//display.add(chargeBills);
		
		PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(regId);
		
		List<Sales> dueBill= salesServiceImpl.findByPatientRegistrationAndPaymentType(patientRegistration, "due");
		
		if(!dueBill.isEmpty())
		{
			display.add(dueBill);
		}
		return display;
	}
	
	/*
	 * To pay for due bill pharmacy
	 */
	@Transactional
	@RequestMapping(value="/due/pay/{regId}",method=RequestMethod.POST)
	public void payDue(@RequestBody Map<String,String> info,@PathVariable String regId)
	{
		 String paymentType=info.get("type");
		 
		
		
		List<ChargeBill> chargeBills = chargeBillServiceImpl.findDueBill(regId);
		for(ChargeBill chargeBillsInfo:chargeBills)
		{
			if(chargeBillsInfo.getSaleId()!=null)
			{
			chargeBillsInfo.getSaleId().setPaid("Yes");
			chargeBillsInfo.getSaleId().setPaymentType(paymentType);
			chargeBillsInfo.setPaid("Yes");
			chargeBillsInfo.setPaymentType(paymentType);
			PatientSales patientSales= patientSalesServiceImpl.findOneBill(chargeBillsInfo.getSaleId().getBillNo(), chargeBillsInfo.getSaleId().getMedicineName(),chargeBillsInfo.getSaleId().getBatchNo());
				if(patientSales!=null)
				{
					patientSales.setPaid("Yes");
					patientSales.setPaymentType(paymentType);
					patientSalesServiceImpl.save(patientSales);
				}
			}
			
		}
		 
	
	}
	
	
	// Discharge slip 
	@RequestMapping(value="/dischargeslip/{regId}",method=RequestMethod.POST)
	public SalesPaymentPdf dischargeSlip(@PathVariable String regId,Principal principal){
		
		
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		String createdBy=userSecurity.getUserId();
		String createdName=userSecurity.getFirstName()+" "+userSecurity.getMiddleName()+" "+userSecurity.getLastName();

		
		PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(regId);
		
		
		String admittedWard = null;
		String bedName=null;

		List<RoomBookingDetails> roomBookingDetails = patientRegistration.getRoomBookingDetails();

		for (RoomBookingDetails roomBookingDetailsInfo : roomBookingDetails) {
			RoomDetails roomDetails = roomBookingDetailsInfo.getRoomDetails();
			admittedWard = roomDetails.getRoomType();
			
			bedName=roomDetails.getBedName();
		}

		String department=patientRegistration.getVuserD().getDoctorDetails().getSpecilization();
		
		 String dpt=null;
	        if(department!=null)
	        {
	        	dpt=department;
	        }
	        else
	        {
	        	dpt="";
	        }
		
		
		
		PatientDetails patient=patientRegistration.getPatientDetails();
		String patientName=null;

		String pfn=null;
		String pmn=null;
		String pln=null;
		if(patient.getFirstName()==null)
		{
			pfn=" ";
		}
		else
		{
			pfn=patient.getFirstName();
		}
		if(patient.getMiddleName()==null)
		{
			pmn="";
		}
		else
		{
			pmn=patient.getMiddleName();
		}
		if(patient.getLastName()==null)
		{
			pln=" ";
		}
		else
		{
			pln=patient.getLastName();
		}
		if(pmn.equalsIgnoreCase(""))
		{
		patientName=patient.getTitle()+". "+patient.getFirstName()+" "+patient.getLastName();
		}
		else
		{
			patientName=patient.getTitle()+". "+patient.getFirstName()+" "+patient.getMiddleName()+" "+patient.getLastName();
		}
		
		//shantharam addr
		String newAddress="";
		
		 try {
	            byte[] pdfByte = null;
	            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	            Document document = new Document(PageSize.A4_LANDSCAPE);
	            Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
	            Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
	            Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
	            Font headFont11 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD|Font.UNDERLINE);
	            PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
	            Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");
	            document.open();
	            PdfPTable table = new PdfPTable(2);
	            Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());
	            img.scaleAbsolute(75, 95);
	            table.setWidthPercentage(105f);
	            Phrase pq = new Phrase(new Chunk(img, 5, -80));
	            pq.add(new Chunk(newAddress,
	                    redFont));
	            
	            
	            PdfPTable table212 = new PdfPTable(1);
	            table212.setWidths(new float[] { 4f });
	            table212.setSpacingBefore(10);
	            PdfPCell hcell192;
	            hcell192 = new PdfPCell(new Phrase("UDBHAVA HOSPITALS", headFont1));
	            hcell192.setBorder(Rectangle.NO_BORDER);
	            hcell192.setHorizontalAlignment(Element.ALIGN_CENTER);
	            table212.addCell(hcell192);
	           // Phrase pq = new Phrase(new Chunk(img, 0, -80));
				//pq.add(new Chunk(addrss,redFont));
	            PdfPCell cellp = new PdfPCell(pq);
	            
	            cellp.setPaddingBottom(-10f);
	            PdfPCell cell1 = new PdfPCell();
	            
	            //cell1.setBorder(Rectangle.NO_BORDER);
	            //cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
	            //cell1.setColspan(2);
	            cell1.addElement(table212);
	            cell1.addElement(pq);
	            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
	            cell1.setColspan(2);
	            PdfPTable table21 = new PdfPTable(1);
	            table21.setWidths(new float[] { 4f });
	            table21.setSpacingBefore(10);
	            PdfPCell hcell19;
	            hcell19 = new PdfPCell(new Phrase("Discharge Slip", headFont11));
	            hcell19.setBorder(Rectangle.NO_BORDER);
	            hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
	            table21.addCell(hcell19);
	            
	                      // Display a date in day, month, year format
	            Date date = Calendar.getInstance().getTime();
	            DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
	            String today = formatter.format(date).toString();
	                     
	            PdfPTable table3 = new PdfPTable(6);
	            table3.setWidths(new float[] { 4f,2f, 4f,4f,2f, 4f });
	            table3.setSpacingBefore(10);
	            
	            PdfPCell hcell111;
	            
	            hcell111 = new PdfPCell(new Phrase("UMR No", headFont));
	            hcell111.setBorder(Rectangle.NO_BORDER);
	            hcell111.setPaddingLeft(-50f);
	            table3.addCell(hcell111);
	            
	            hcell111 = new PdfPCell(new Phrase(":", redFont));
	            hcell111.setBorder(Rectangle.NO_BORDER);
	            hcell111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell111.setPaddingLeft(-90f);
	            table3.addCell(hcell111);
	            
	            hcell111 = new PdfPCell(new Phrase(patient.getUmr(), redFont));
	            hcell111.setBorder(Rectangle.NO_BORDER);
	            hcell111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell111.setPaddingLeft(-120f);
	            table3.addCell(hcell111);
	            
	            hcell111 = new PdfPCell(new Phrase("Discharge Date", headFont));
	            hcell111.setBorder(Rectangle.NO_BORDER);
	            //hcell111.setPaddingRight(-70f);
	            hcell111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            table3.addCell(hcell111);
	            
	            hcell111 = new PdfPCell(new Phrase(":", redFont));
	            hcell111.setBorder(Rectangle.NO_BORDER);
	            hcell111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            //hcell111.setPaddingLeft(-70f);
	            table3.addCell(hcell111);
	            
	            hcell111 = new PdfPCell(new Phrase(today, redFont));
	            hcell111.setBorder(Rectangle.NO_BORDER);
	            hcell111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell111.setPaddingLeft(-30f);
	            table3.addCell(hcell111);
	            
	            PdfPCell hcell1;
	            
	            hcell1 = new PdfPCell(new Phrase("Adm. No",headFont));
	            hcell1.setBorder(Rectangle.NO_BORDER);
	            hcell1.setPaddingLeft(-50f);
	            table3.addCell(hcell1);
	            
	            hcell1 = new PdfPCell(new Phrase(":", redFont));
	            hcell1.setBorder(Rectangle.NO_BORDER);
	            hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell1.setPaddingLeft(-90f);
	            table3.addCell(hcell1);
	            
	            hcell1 = new PdfPCell(new Phrase(patientRegistration.getRegId(), redFont));
	            hcell1.setBorder(Rectangle.NO_BORDER);
	            hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell1.setPaddingLeft(-120f);
	            table3.addCell(hcell1);
	            
	            hcell1 = new PdfPCell(new Phrase("Ward Name",headFont));
	            hcell1.setBorder(Rectangle.NO_BORDER);
	            //hcell111.setPaddingRight(-70f);
	            hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
	            table3.addCell(hcell1);
	            
	            hcell1 = new PdfPCell(new Phrase(":", redFont));
	            hcell1.setBorder(Rectangle.NO_BORDER);
	            hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
	            //hcell111.setPaddingLeft(-70f);
	            table3.addCell(hcell1);
	            
	            hcell1 = new PdfPCell(new Phrase(admittedWard, redFont));
	            hcell1.setBorder(Rectangle.NO_BORDER);
	            hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell1.setPaddingLeft(-30f);
	            table3.addCell(hcell1);
	            
	            PdfPCell hcell2;
	            
	            hcell2 = new PdfPCell(new Phrase("", redFont));
	            hcell2.setBorder(Rectangle.NO_BORDER);
	            hcell2.setPaddingLeft(-50f);
	            table3.addCell(hcell2);
	            
	            hcell2 = new PdfPCell(new Phrase("", redFont));
	            hcell2.setBorder(Rectangle.NO_BORDER);
	            hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell2.setPaddingLeft(-90f);
	            table3.addCell(hcell2);
	            
	            hcell2 = new PdfPCell(new Phrase("", redFont));
	            hcell2.setBorder(Rectangle.NO_BORDER);
	            hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell2.setPaddingLeft(-120f);
	            table3.addCell(hcell2);
	            
	            hcell2 = new PdfPCell(new Phrase("Bed No.",headFont));
	            hcell2.setBorder(Rectangle.NO_BORDER);
	            //hcell111.setPaddingRight(-70f);
	            hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
	            table3.addCell(hcell2);
	            
	            hcell2 = new PdfPCell(new Phrase(":", redFont));
	            hcell2.setBorder(Rectangle.NO_BORDER);
	            hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
	            //hcell111.setPaddingLeft(-70f);
	            table3.addCell(hcell2);
	            
	            hcell2 = new PdfPCell(new Phrase(bedName, redFont));
	            hcell2.setBorder(Rectangle.NO_BORDER);
	            hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell2.setPaddingLeft(-30f);
	            table3.addCell(hcell2);
	            
	            
	            
	            cell1.addElement(table21);
	            cell1.addElement(table3);
	            
	            PdfPTable table111 = new PdfPTable(3);
	            table111.setWidths(new float[] {5f,2f,8f});
	            
	            PdfPCell hcell12;
	            hcell12 = new PdfPCell(new Phrase("Patient Name",headFont));
	            hcell12.setBorder(Rectangle.NO_BORDER);
	            hcell12.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell12.setPaddingLeft(-50f);
	            hcell12.setPaddingTop(10f);
	            table111.addCell(hcell12);
	            
	            hcell12 = new PdfPCell(new Phrase(":",redFont));
	            hcell12.setBorder(Rectangle.NO_BORDER);
	            hcell12.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell12.setPaddingLeft(-90f);
	            hcell12.setPaddingTop(10f);
	            table111.addCell(hcell12);
	            
	            hcell12 = new PdfPCell(new Phrase(patientName,redFont));
	            hcell12.setBorder(Rectangle.NO_BORDER);
	            hcell12.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell12.setPaddingLeft(-90f);
	            hcell12.setPaddingTop(10f);
	            table111.addCell(hcell12);
	            
	            PdfPCell hcell121;
	            hcell121 = new PdfPCell(new Phrase("Doctor Name",headFont));
	            hcell121.setBorder(Rectangle.NO_BORDER);
	            hcell121.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell121.setPaddingLeft(-50f);
	            hcell121.setPaddingTop(5f);
	            table111.addCell(hcell121);
	            
	            hcell121 = new PdfPCell(new Phrase(":",redFont));
	            hcell121.setBorder(Rectangle.NO_BORDER);
	            hcell121.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell121.setPaddingLeft(-90f);
	            hcell121.setPaddingTop(5f);
	            table111.addCell(hcell121);
	            
	            hcell121 = new PdfPCell(new Phrase(patient.getConsultant(),redFont));
	            hcell121.setBorder(Rectangle.NO_BORDER);
	            hcell121.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell121.setPaddingLeft(-90f);
	            hcell121.setPaddingTop(5f);
	            table111.addCell(hcell121);
	            
	            
	            PdfPCell hcell1211;
	            hcell1211 = new PdfPCell(new Phrase("Department",headFont));
	            hcell1211.setBorder(Rectangle.NO_BORDER);
	            hcell1211.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell1211.setPaddingLeft(-50f);
	            hcell1211.setPaddingTop(5f);
	            table111.addCell(hcell1211);
	            
	            hcell1211 = new PdfPCell(new Phrase(":",redFont));
	            hcell1211.setBorder(Rectangle.NO_BORDER);
	            hcell1211.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell1211.setPaddingLeft(-90f);
	            hcell1211.setPaddingTop(5f);
	            table111.addCell(hcell1211);
	            
	            hcell1211 = new PdfPCell(new Phrase(dpt,redFont));
	            hcell1211.setBorder(Rectangle.NO_BORDER);
	            hcell1211.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell1211.setPaddingLeft(-90f);
	            hcell1211.setPaddingTop(5f);
	            table111.addCell(hcell1211);
	            
	            
	            PdfPCell hcell12111;
	            hcell12111 = new PdfPCell(new Phrase("Remarks",headFont));
	            hcell12111.setBorder(Rectangle.NO_BORDER);
	            hcell12111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell12111.setPaddingLeft(-50f);
	            hcell12111.setPaddingTop(5f);
	            table111.addCell(hcell12111);
	            
	            hcell12111 = new PdfPCell(new Phrase(":",redFont));
	            hcell12111.setBorder(Rectangle.NO_BORDER);
	            hcell12111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell12111.setPaddingLeft(-90f);
	            hcell12111.setPaddingTop(5f);
	            table111.addCell(hcell12111);
	            
	            hcell12111 = new PdfPCell(new Phrase("remarks",redFont));
	            hcell12111.setBorder(Rectangle.NO_BORDER);
	            hcell12111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell12111.setPaddingLeft(-90f);
	            hcell12111.setPaddingTop(5f);
	            table111.addCell(hcell12111);
	            
	            

	            PdfPCell hcell121111;
	            hcell121111 = new PdfPCell(new Phrase("**Discharge Without Bill",headFont));
	            hcell121111.setBorder(Rectangle.NO_BORDER);
	            hcell121111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell121111.setPaddingLeft(-50f);
	            hcell121111.setPaddingTop(15f);
	            table111.addCell(hcell121111);
	            
	            hcell121111 = new PdfPCell(new Phrase("",redFont));
	            hcell121111.setBorder(Rectangle.NO_BORDER);
	            hcell121111.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell121111.setPaddingLeft(-90f);
	            hcell121111.setPaddingTop(15f);
	            table111.addCell(hcell121111);
	            
	            hcell121111 = new PdfPCell(new Phrase("Signature",headFont));
	            hcell121111.setBorder(Rectangle.NO_BORDER);
	            hcell121111.setHorizontalAlignment(Element.ALIGN_RIGHT);
	            //hcell121111.setPaddingLeft(-90f);
	            hcell121111.setPaddingTop(15f);
	            table111.addCell(hcell121111);

	            cell1.addElement(table111);
	            
	            PdfPCell cell2 = new PdfPCell();
	            cell2.setColspan(2);
	            
	            PdfPTable table1 = new PdfPTable(6);
	            table1.setWidths(new float[] { 4f,2f,4f,4f,2f,4f});
	            
	            PdfPCell hcell;
	            
	            hcell = new PdfPCell(new Phrase("Prepared By",headFont));
	            hcell.setBorder(Rectangle.NO_BORDER);
	            hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell.setPaddingLeft(-50f);
	            hcell.setPaddingTop(20f);
	            table1.addCell(hcell);
	            
	            hcell = new PdfPCell(new Phrase(":", redFont));
	            hcell.setBorder(Rectangle.NO_BORDER);
	            hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell.setPaddingLeft(-70f);
	            hcell.setPaddingTop(20f);
	            table1.addCell(hcell);
	            
	            hcell = new PdfPCell(new Phrase(createdName, redFont));
	            hcell.setBorder(Rectangle.NO_BORDER);
	            hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell.setPaddingLeft(-90f);
	            hcell.setPaddingTop(20f);
	            table1.addCell(hcell);
	            
	            hcell = new PdfPCell(new Phrase("Prepared On",headFont));
	            hcell.setBorder(Rectangle.NO_BORDER);
	            hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell.setPaddingTop(20f);
	            table1.addCell(hcell);
	            
	            hcell = new PdfPCell(new Phrase(":", redFont));
	            hcell.setBorder(Rectangle.NO_BORDER);
	            hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell.setPaddingTop(20f);
	            table1.addCell(hcell);
	            
	            hcell = new PdfPCell(new Phrase(today, redFont));
	            hcell.setBorder(Rectangle.NO_BORDER);
	            hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell.setPaddingLeft(-30f);
	            hcell.setPaddingTop(20f);
	            table1.addCell(hcell);
	            
	            PdfPCell hcell11;
	            
	            hcell11 = new PdfPCell(new Phrase("Printed By",headFont));
	            hcell11.setBorder(Rectangle.NO_BORDER);
	            hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell11.setPaddingLeft(-50f);
	            table1.addCell(hcell11);
	            
	            hcell11 = new PdfPCell(new Phrase(":", redFont));
	            hcell11.setBorder(Rectangle.NO_BORDER);
	            hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell11.setPaddingLeft(-70f);
	            table1.addCell(hcell11);
	            
	            hcell11 = new PdfPCell(new Phrase(createdName, redFont));
	            hcell11.setBorder(Rectangle.NO_BORDER);
	            hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell11.setPaddingLeft(-90f);
	            table1.addCell(hcell11);
	            
	            hcell11 = new PdfPCell(new Phrase("Printed On",headFont));
	            hcell11.setBorder(Rectangle.NO_BORDER);
	            hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
	            table1.addCell(hcell11);
	            
	            hcell11 = new PdfPCell(new Phrase(":", redFont));
	            hcell11.setBorder(Rectangle.NO_BORDER);
	            hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
	            table1.addCell(hcell11);
	            
	            hcell11 = new PdfPCell(new Phrase(today, redFont));
	            hcell11.setBorder(Rectangle.NO_BORDER);
	            hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
	            hcell11.setPaddingLeft(-30f);
	            table1.addCell(hcell11);
	            
	            cell2.addElement(table1);
	            
	           // table.addCell(cell2);
	          // cell1.addElement(cell2);
	            
	            
	             
	            table.addCell(cell1);
	         table.addCell(cell2);
	            document.add(table);
	            document.close();

				


				
				System.out.println("finished");

				pdfByte = byteArrayOutputStream.toByteArray();
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
						.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();

				salesPaymentPdf = new SalesPaymentPdf();
				salesPaymentPdf.setFileName(regId+" Discharge slip");
				salesPaymentPdf.setFileuri(uri);
				salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
				salesPaymentPdf.setData(pdfByte);
				salesPaymentPdfServiceImpl.save(salesPaymentPdf);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		return salesPaymentPdf;	
		
	}
	
	/*
	 * update final billing 
	 */
	@Transactional
	@RequestMapping(value="/charge/update/{id}",method=RequestMethod.POST)
	public List<Object> UpdateFinalBilling(@RequestBody ChargeBillDto cahrgeBillDto,@PathVariable String id,Principal principal)
	{
		ChargeBill chargeBill=new ChargeBill();
		
		BeanUtils.copyProperties(cahrgeBillDto, chargeBill);
		PatientRegistration patientRegistration=patientRegistrationServiceImpl.findByRegId(id);
		if(patientRegistration.getpType().equalsIgnoreCase(ConstantValues.OUTPATIENT))
		{
			throw new RuntimeException(ConstantValues.OUTPATIENT_NOT_ALLOWED_ERROR_MSG);
		}
		//String modeOfPayment=paymentInfo.get("mode");
		
		
		String regId=id;
		//createdBy Security
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		String createdBy=userSecurity.getUserId();
		String createdName=null;
		
		String mn=userSecurity.getMiddleName();
		
		if(mn==null)
		{
			createdName=userSecurity.getFirstName()+" "+userSecurity.getLastName();
		}
		else
		{
			createdName=userSecurity.getFirstName()+" "+userSecurity.getMiddleName()+" "+userSecurity.getLastName();
		}
		
			
			List<Map<String,String>> updateCharge=chargeBill.getUpdateCharge();		
		
			String room=patientRegistration.getRoomBookingDetails().get(0).getRoomDetails().getRoomType();
		for(Map<String,String> updateChargeInfo:updateCharge ){
			
			String serviceName=updateChargeInfo.get("chargeName");
			String chargeBillId=updateChargeInfo.get("chargeBillId");
		//	LabServices labServices=labServicesServiceImpl.findPriceByType(serviceName,patientRegistration.getpType(),room);
			
			ChargeBill chargeInfo=chargeBillServiceImpl.findByChargeBillId(chargeBillId);
			chargeInfo.setChargeBillId(chargeBillId);
				chargeInfo.setBillNo(chargeInfo.getBillNo());
				chargeInfo.setNetAmount(Float.parseFloat(updateChargeInfo.get("netAmount")));
				chargeInfo.setAmount(Float.parseFloat(updateChargeInfo.get("amount")));
				chargeInfo.setMrp(Float.parseFloat(updateChargeInfo.get("mrp")));
				chargeInfo.setQuantity(Long.parseLong(updateChargeInfo.get("quantity")));
				chargeInfo.setDiscount(Float.parseFloat(updateChargeInfo.get("discount")));
				chargeInfo.setUserChargeBillId(userSecurity);
			chargeBillServiceImpl.save(chargeInfo);
		}
		List<ChargeBill> chargeBill1=chargeBillServiceImpl.findByPatRegId(patientRegistration);
		List<Object> display=new ArrayList<>();
		List<Object> refBill=new ArrayList<>();
		
		try
		{

		String doj=patientRegistration.getDateOfJoining().toString().substring(0,10);
		SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat toFormat = new SimpleDateFormat("dd-MMM-yyyy");
		doj=toFormat.format(fromFormat.parse(doj));
		
		refBillids.setDoj(doj);
		refBillids.setpType(patientRegistration.getpType());
		refBillids.setPatientName(patientRegistration.getPatientDetails().getFirstName()+" "+patientRegistration.getPatientDetails().getLastName());
		refBillids.setDoc(patientRegistration.getPatientDetails().getConsultant());
		
		String dob=patientRegistration.getPatientDetails().getDob().toString().substring(0,10);
		 fromFormat = new SimpleDateFormat("yyyy-MM-dd");
		 toFormat = new SimpleDateFormat("dd-MMM-yyyy");
		dob=toFormat.format(fromFormat.parse(dob));
		refBillids.setDob(dob);
		refBillids.setUmr(patientRegistration.getPatientDetails().getUmr());
		refBill.add(refBillids);
		display.add(refBill);
		
		
				}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//display.add(labServicesRepository.findAllByPatientTypeAndServiceType(patientRegistration.getpType(),"Other"));
		
		display.add(chargeBill1);
		return display;
	}
	
	// Charge bill
	@RequestMapping(value="/removeService")
	public void addEmail()
	{
		List<ChargeBill> allService=chargeBillRepository.getAllServices();
		
		allService.forEach((s) ->{
			s.setEmail(s.getServiceId().getServiceName());
			s.setServiceId(null);
			chargeBillServiceImpl.save(s); 
		});
		
	}
	
	// Charge bill
	@RequestMapping(value="/addService")
	public void addServiceEmail()
	{
		List<ChargeBill> allService=chargeBillRepository.getAllServicesNull();
		
		allService.forEach((s) -> {
			String roomtype=s.getPatRegId().getRoomBookingDetails().get(s.getPatRegId().getRoomBookingDetails().size()-1).getRoomDetails().getRoomType();
			s.setServiceId(labServicesServiceImpl.findPriceByType(s.getEmail(),s.getPatRegId().getpType(), (s.getPatRegId().getpType().equalsIgnoreCase("INPATIENT")) ? roomtype : "NA"));
			chargeBillServiceImpl.save(s);
		});
		
	}
	

/*
 * To get advance Pdf 
 */
@RequestMapping(value="/advancefinalbill/{regId}")
public Map<String, Object> advanceFinalBill(@PathVariable("regId") String regId){
	
	Map<String, Object> map=new HashMap<>();
	
	
	SalesPaymentPdf salesPaymentPdf=salesPaymentPdfServiceImpl.getFinalAdvancePdf(regId);
	
	map.put("regId", salesPaymentPdf.getFileuri());
	
	
	return map;
}

	
	
}
