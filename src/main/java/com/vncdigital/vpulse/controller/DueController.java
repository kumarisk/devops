package com.vncdigital.vpulse.controller;

import java.io.ByteArrayOutputStream;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
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
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.vncdigital.vpulse.MoneyToWords.NumberToWordsConverter;
import com.vncdigital.vpulse.bill.model.ChargeBill;
import com.vncdigital.vpulse.bill.repository.ChargeBillRepository;
import com.vncdigital.vpulse.due.helper.DueHelper;
import com.vncdigital.vpulse.finalBilling.model.FinalBilling;
import com.vncdigital.vpulse.finalBilling.serviceImpl.FinalBillingServiceImpl;
import com.vncdigital.vpulse.laboratory.model.LaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.repository.LaboratoryRegistrationRepository;
import com.vncdigital.vpulse.laboratory.serviceImpl.LaboratoryRegistrationServiceImpl;
import com.vncdigital.vpulse.osp.model.OspService;
import com.vncdigital.vpulse.osp.repository.OspServiceRepository;
import com.vncdigital.vpulse.patient.model.CashPlusCard;
import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.repository.PatientDetailsRepository;
import com.vncdigital.vpulse.patient.repository.PatientPaymentRepository;
import com.vncdigital.vpulse.patient.repository.PatientRegistrationRepository;
import com.vncdigital.vpulse.patient.serviceImpl.CashPlusCardServiceImpl;
import com.vncdigital.vpulse.patient.serviceImpl.PatientRegistrationServiceImpl;
import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.MedicineProcurement;
import com.vncdigital.vpulse.pharmacist.model.PatientSales;
import com.vncdigital.vpulse.pharmacist.model.Sales;
import com.vncdigital.vpulse.pharmacist.model.SalesPaymentPdf;
import com.vncdigital.vpulse.pharmacist.model.SalesReturn;
import com.vncdigital.vpulse.pharmacist.repository.MedicineDetailsRepository;
import com.vncdigital.vpulse.pharmacist.repository.PatientSalesRepository;
import com.vncdigital.vpulse.pharmacist.repository.SalesRepository;
import com.vncdigital.vpulse.pharmacist.repository.SalesReturnRepository;
import com.vncdigital.vpulse.pharmacist.serviceImpl.MedicineDetailsServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.MedicineProcurementServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.MedicineQuantityServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.SalesPaymentPdfServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.SalesServiceImpl;
import com.vncdigital.vpulse.pharmacyShopDetails.model.PharmacyShopDetails;
import com.vncdigital.vpulse.pharmacyShopDetails.repository.PharmacyShopDetailsRepository;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.repository.UserRepository;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;

@CrossOrigin(origins = "*", maxAge = 36000)
@RestController
@RequestMapping("/v1/due")
public class DueController {

	public static Logger Logger = LoggerFactory.getLogger(DueController.class);

	
	@Autowired
	NumberToWordsConverter numberToWordsConverter;
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	LaboratoryRegistrationServiceImpl laboratoryRegistrationServiceImpl;

	@Autowired
	LaboratoryRegistrationRepository laboratoryRegistrationRepository;

	@Autowired
	PatientSalesRepository patientSalesRepository;

	@Autowired
	PharmacyShopDetailsRepository pharmacyShopDetailsRepository;

	@Autowired
	MedicineDetailsServiceImpl medicineDetailsServiceImpl;

	@Autowired
	SalesPaymentPdfServiceImpl salesPaymentPdfServiceImpl;

	@Autowired
	PatientRegistrationRepository patientRegistrationRepository;

	@Autowired
	SalesRepository salesRepository;

	@Autowired
	SalesReturnRepository salesReturnRepository;

	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	MedicineDetailsRepository medicineDetailsRepository;

	@Autowired
	PatientRegistrationServiceImpl patientRegistrationServiceImpl;

	@Autowired
	MedicineProcurementServiceImpl medicineProcurementServiceImpl;

	@Autowired
	SalesServiceImpl salesServiceImpl;

	@Autowired
	UserServiceImpl userServiceImpl;

	@Autowired
	MedicineQuantityServiceImpl medicineQuantityServiceImpl;

	@Autowired
	PatientDetailsRepository patientDetailsRepository;
	@Autowired
	ChargeBillRepository chargeBillRepository;
	
	
	@Autowired
	CashPlusCardServiceImpl cashPlusCardServiceImpl;
	
	@Autowired
	FinalBillingServiceImpl finalBillingServcieImpl;
	
	@Autowired
	OspServiceRepository ospServiceRepository;
	@Autowired
	PatientPaymentRepository patientPaymentRepository;
	
	
	//filter by duetype
	@RequestMapping(value="/create",method=RequestMethod.POST)
	public List<Object> getDueBillsBasedOnType(@RequestBody Map<String, String> mapInfo){
		
			String dueType=mapInfo.get("dueType");
			List<Object> Allllist = new ArrayList<>();

		
			if(dueType.equalsIgnoreCase("Pharmacy")) {
				
				List<String> billList=new ArrayList<>();
				
				List<Sales> sales1 = salesRepository.findByPaymentType("Due");
				Map<String, String> pmap = new HashMap<>();
				String umr=null;
				String prno=null;
				String billno=null;
				String patientName=null;
				String billno1=null;
				float amount=0;
				
				
				for(Sales s:sales1)
				{
					billno=s.getBillNo();
					 if(!billList.contains(billno)){
				
					Map<String, String> saleMap = new HashMap<>();
					
					List<Sales> s1= salesRepository.findByBillNo(billno);
					 for(Sales s2:s1)
					 {
						 amount+=s2.getAmount();
					 }
					 
					 if(s.getPatientRegistration()!=null) {
						  PatientDetails patientDetails=patientDetailsRepository.findByUmr( umr);
						  umr=s.getUmr(); 
						   prno=s.getPatientRegistration().getRegId();
						  
						   if(patientDetails!=null) {
						  patientName=(patientDetails.getMiddleName()!=null)?patientDetails.getTitle()
						  +" "+patientDetails.getFirstName()+" "+patientDetails.getMiddleName()+" "
						  +patientDetails.getLastName():patientDetails.getTitle()+" "+patientDetails.
						  getFirstName()+" "+patientDetails.getLastName();
						  }
						  }
						  
						 
						saleMap.put("umr", umr);
						saleMap.put("patientregid", prno);
						saleMap.put("dueFor", "Pharmacy");
						saleMap.put("billno", billno);
						saleMap.put("amount",String.valueOf(amount));
						billList.add(billno);
						 Allllist .add(saleMap);
				}
				}

			}
			
			else if(dueType.equalsIgnoreCase("lab")) {
				
				List<String> labList=new ArrayList<>();
				List<LaboratoryRegistration> laboratoryRegistrations=laboratoryRegistrationRepository.getDueRegistrationlist("due");
			
			         String prno1=null;
			         String umr1=null;
			         String patientName1 =null;
			         String invoiceno1=null;
			         float amount1=0;
			     	
					
			     
						for(LaboratoryRegistration labInfo:laboratoryRegistrations){
							
							invoiceno1=labInfo.getInvoiceNo();
							 if(!labList.contains(invoiceno1)){
							Map<String, String> labMap = new HashMap<>();
							umr1=labInfo.getLaboratoryPatientRegistration().getPatientDetails().getUmr();
							PatientDetails patientDetails=labInfo.getLaboratoryPatientRegistration().getPatientDetails();							
							patientName1=(patientDetails.getMiddleName()!=null)?patientDetails.getTitle()+" "+patientDetails.getFirstName()+" "+patientDetails.getMiddleName()+" "+patientDetails.getLastName():patientDetails.getTitle()+" "+patientDetails.getFirstName()+" "+patientDetails.getLastName();
							prno1=	labInfo.getLaboratoryPatientRegistration().getRegId();
							
							List<LaboratoryRegistration> lab=laboratoryRegistrationRepository.findByInvoiceNo(invoiceno1);
							
							for(LaboratoryRegistration l:lab) {
								amount1+=l.getNetAmount();
							}
							
							
							labMap.put("patientregid", prno1);
							labMap.put("dueFor", "Lab");
							labMap.put("amount", String.valueOf(amount1));
							labMap.put("umr",umr1);
							labMap.put("patientName",patientName1);
							labMap.put("billno",invoiceno1);
							labList.add(invoiceno1);
							Allllist.add(labMap);
							 
							amount1=0;
									
							 }
						}
		
				
				
			}
		
			else if(dueType.equalsIgnoreCase("osp")) {
				
				List<String> ospList=new ArrayList<>();
				List<OspService> osplist=ospServiceRepository.findByPaymentType("Due");
				
				String patientname4=null;
				String preg4=null;
				float amount4=0;
				String billno=null;
				for(OspService os: osplist)
				{
					billno=os.getBillNo();
					 if(! ospList.contains(billno)){
							
							Map<String, String> ospMap = new HashMap<>();
							float amount3=0;
						List<OspService>ospinfo=ospServiceRepository.findByBillNo(billno);
						for(OspService os1:ospinfo) {
							 amount4+=os1.getNetAmount();
							
						}
						patientname4=os.getPatientName();
						preg4=os.getOspServiceId();
						
						ospMap.put("umr", "");
						ospMap.put("amount",String.valueOf(amount4) );
						ospMap.put("patientregid", preg4);
						ospMap.put("patientName", patientname4);
						ospMap.put("dueFor","osp");
						ospMap.put("billno",billno);
						ospList.add(billno);
						Allllist.add( ospMap );
						amount4=0;
						}
				
				}
				
			}
			
		
			
			return Allllist;
		}
	
	
	
	
	
	
	
	
		//for getting all the due list
		@RequestMapping(value="/get/duelist",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
		public List<Object> getInfo()
		{   
			
			List<Object> Allllist = new ArrayList<>();
			
			List<String> billList=new ArrayList<>();
			
			List<Sales> sales1 = salesRepository.findByPaymentType("Due");
			Map<String, String> pmap = new HashMap<>();
			String  umr=null;
			String prno=null;
			String billno=null;
			String patientName=null;
			float amount=0;
			
			
		
		  for(Sales s:sales1) { 
		  billno=s.getBillNo(); 
		  if(!billList.contains(billno)){
		  
		  Map<String, String> saleMap = new HashMap<>();
		  
		  //System.out.println("----------regid---------"+s.getPatientRegistration().
		   
		  
		  
		  List<Sales> s1= salesRepository.findByBillNo(billno);
		   for(Sales s2:s1) {
		  amount+=s2.getAmount(); 
		  }
		   patientName=s.getName();
		  if(s.getPatientRegistration()!=null) {
		 // PatientDetails patientDetails=patientDetailsRepository.findByUmr( umr);
		  umr=s.getUmr(); 
		   prno=s.getPatientRegistration().getRegId();
		  
		  
		  }
		  
		  saleMap.put("patientName",patientName ); 
		  saleMap.put("umr", umr);
		  saleMap.put("patientregid", prno); 
		  saleMap.put("dueFor", "Pharmacy");
		  saleMap.put("billno", billno); 
		  saleMap.put("amount",String.valueOf(amount));
		  billList.add(billno); 
		  Allllist .add(saleMap); }
		  
		  
		  }
		  
		 
			
			        List<String> labList=new ArrayList<>();
						List<LaboratoryRegistration> laboratoryRegistrations=laboratoryRegistrationRepository.getDueRegistrationlist("due");
				
				         String prno1=null;
				         String umr1=null;
				         String patientName1 =null;
				         String invoiceno1=null;
				         float amount1=0;
				     	
						
				     
							for(LaboratoryRegistration labInfo:laboratoryRegistrations){
								
								invoiceno1=labInfo.getInvoiceNo();
								 if(!labList.contains(invoiceno1)){
								Map<String, String> labMap = new HashMap<>();
								umr1=labInfo.getLaboratoryPatientRegistration().getPatientDetails().getUmr();
								PatientDetails patientDetails=labInfo.getLaboratoryPatientRegistration().getPatientDetails();							
								patientName1=(patientDetails.getMiddleName()!=null)?patientDetails.getTitle()+" "+patientDetails.getFirstName()+" "+patientDetails.getMiddleName()+" "+patientDetails.getLastName():patientDetails.getTitle()+" "+patientDetails.getFirstName()+" "+patientDetails.getLastName();
								prno1=	labInfo.getLaboratoryPatientRegistration().getRegId();
								
								List<LaboratoryRegistration> lab=laboratoryRegistrationRepository.findByInvoiceNo(invoiceno1);
								
								for(LaboratoryRegistration l:lab) {
									amount1+=l.getNetAmount();
								}
								
								
								labMap.put("patientregid", prno1);
								labMap.put("dueFor", "Lab");
								labMap.put("amount", String.valueOf(amount1));
								labMap.put("umr",umr1);
								labMap.put("patientName",patientName1);
								labMap.put("billno",invoiceno1);
								labList.add(invoiceno1);
								Allllist.add(labMap);
								 
								amount1=0;
										
								 }
							}
							
						
							List<String> ospList=new ArrayList<>();
							List<OspService> osplist=ospServiceRepository.findByPaymentType("Due");
							
							String patientname4=null;
							String preg4=null;
							float amount4=0;
							for(OspService os: osplist)
							{
								billno=os.getBillNo();
								 if(! ospList.contains(billno)){
										
										Map<String, String> ospMap = new HashMap<>();
										float amount3=0;
									List<OspService>ospinfo=ospServiceRepository.findByBillNo(billno);
									for(OspService os1:ospinfo) {
										 amount4+=os1.getNetAmount();
										
									}
									patientname4=os.getPatientName();
									preg4=os.getOspServiceId();
									
									ospMap.put("umr", "");
									ospMap.put("amount",String.valueOf(amount4) );
									ospMap.put("patientregid", preg4);
									ospMap.put("patientName", patientname4);
									ospMap.put("dueFor","osp");
									ospMap.put("billno",billno);
									ospList.add(billno);
									Allllist.add( ospMap );
									amount4=0;
									}
							
							
							}
							
											
			return Allllist ;
		}
	
	
	

	@RequestMapping(value = "/get/{umrNo}")
	public List<Object> getPatientDetails(@PathVariable("umrNo") String umrNo) {

		List<Object> patientdts = new ArrayList<>();

		PatientDetails patientDetails = patientDetailsRepository.findByUmr(umrNo);

		long pid = patientDetails.getPatientId();

		List<PatientRegistration> patientRegistrations = patientRegistrationRepository.getRegids(pid);

		String patientName = null;
		patientName=(patientDetails.getMiddleName()!=null)?patientDetails.getTitle()+" "+patientDetails.getFirstName()+" "+patientDetails.getMiddleName()+" "+
				patientDetails.getLastName():patientDetails.getTitle()+" "+patientDetails.getFirstName()+" "+patientDetails.getLastName();
		
		Map<String, String> pmap = new HashMap<>();
		pmap.put("mobileNo", String.valueOf(patientDetails.getMobile()));
		pmap.put("pName", patientName);

		patientdts.add(pmap);
		return patientdts;

	}


	//@RequestBody Map<String, String> due	
	@RequestMapping(value = "/duepay/{billNo}", method = RequestMethod.POST)
	public SalesPaymentPdf getPharmacySettlementPdf(@RequestBody DueHelper dueHelper,
			@PathVariable("billNo") String billNo, Principal principal) {
		
		
		float finalCash=0; //final billing
		float finalCard=0; //final billing
		float finalCheque=0; //final billing
		float finalDue=0; //final billing
		float netAmount=0;
		float finalNetAmount=0;

		// CreatedBy (Security)
		User userSecurity = userServiceImpl.findByUserName(principal.getName());
		String createdBy = userSecurity.getFirstName() + " " + userSecurity.getLastName();
		String createdid=userSecurity.getUserId();

		SalesPaymentPdf salesPaymentPdf = new SalesPaymentPdf();
		
		String modeOfPayment=dueHelper.getMode();
		String dueType=dueHelper.getDueFor();
		String regNo = null;
		List<Object> list = new ArrayList<>();

		if (dueType.equalsIgnoreCase("Pharmacy")) {

			List<Sales> sales1 = salesRepository.findByBillNoAndPaymentType(billNo, "due");

			List<Map<String, String>> show = new ArrayList<>();
			String medName = null;
			String batch = null;
			String mrp = null;
			long quantity = 0;
			long returnQuantity = 0;
			long netQuantity = 0;

			float saleAmt = 0;
			float returnAmt = 0;

			float totalNetAmt = 0;

			for (Sales salesInfo : sales1) {
				Map<String, String> map = new HashMap<>();
				if (!modeOfPayment.equalsIgnoreCase("due")) {
					
					
				/*	
					if(modeOfPayment.equalsIgnoreCase("Card")||modeOfPayment.equalsIgnoreCase("Credit Card")||
							modeOfPayment.equalsIgnoreCase("Debit Card")||modeOfPayment.equalsIgnoreCase("Cash+Card")) {
								
						salesInfo.setReferenceNumber(dueHelper.getReferenceNumber());
							}
*/
					PatientSales patientSales = patientSalesRepository.findOneBill(billNo, salesInfo.getMedicineName(),salesInfo.getBatchNo());

					patientSales.setPaid("Yes");
					patientSales.setUpdatedBy(createdid);
					patientSales.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
					//patientSales.setPaymentType(due.get("mode"));
					patientSales.setPaymentType(modeOfPayment);
					List<ChargeBill> chargebill = salesInfo.getChargeBill();

					for (ChargeBill chargebillInfo : chargebill) {

						chargebillInfo.setPaid("Yes");
						chargebillInfo.setUpdatedBy(createdid);
						chargebillInfo.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
						chargebillInfo.setPaymentType(modeOfPayment);
					}

					salesInfo.setPaid("Yes");
					salesInfo.setUpdatedBy(createdBy);
					salesInfo.setPaymentType(modeOfPayment);
					salesInfo.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
					regNo = salesInfo.getPatientRegistration().getRegId();
					medName = salesInfo.getMedicineName();
					batch = salesInfo.getBatchNo();
					netQuantity = salesInfo.getQuantity();
					mrp = String.valueOf(salesInfo.getMrp());

					List<SalesReturn> salesReturns = salesReturnRepository.findByBillNoAndMedicineName(billNo, medName);

					// List<SalesReturn>
					// salesReturns=salesReturnRepository.findByName(medicineName);
					if (!salesReturns.isEmpty()) {
						for (SalesReturn salesReturnsinfo : salesReturns) {
							salesReturnsinfo.setPaymentType(modeOfPayment);

							returnQuantity = returnQuantity + salesReturnsinfo.getQuantity();
							returnAmt = returnAmt + salesReturnsinfo.getAmount();

						}
					} else {

						returnQuantity = 0;
					}

					quantity = netQuantity + returnQuantity;

					map.put("medicineName", medName);
					map.put("batch", batch);
					map.put("quantity", String.valueOf(quantity));
					map.put("mrp", mrp);

					System.out.println(netQuantity);
					map.put("netQty", String.valueOf(netQuantity));
					map.put("saleValue", String.valueOf(salesInfo.getAmount()));
					map.put("returnqty", String.valueOf(returnQuantity));
					returnQuantity = 0;
					show.add(map);
					saleAmt = saleAmt + salesInfo.getAmount();
				}
			}
			
			
			
			PatientRegistration patientRegistration = patientRegistrationServiceImpl.findByRegId(regNo);

			System.out.println(regNo);

			PatientDetails patient = patientRegistration.getPatientDetails();
			String patientName = null;
			String pfn = null;
			String pmn = null;
			String pln = null;
			if (patient.getFirstName() == null) {
				pfn = " ";
			} else {
				pfn = patient.getFirstName();
			}
			if (patient.getMiddleName() == null) {
				pmn = "";
			} else {
				pmn = patient.getMiddleName();
			}
			if (patient.getLastName() == null) {
				pln = " ";
			} else {
				pln = patient.getLastName();
			}
			if (pmn.equalsIgnoreCase("")) {
				patientName = patient.getTitle() + ". " + patient.getFirstName() + " " + patient.getLastName();
			} else {
				patientName = patient.getTitle() + ". " + patient.getFirstName() + " " + patient.getMiddleName() + " "
						+ patient.getLastName();
			}


			finalNetAmount=dueHelper.getAmount();
			
			
			
			
		
			
			// Cash + Card
			
			if(modeOfPayment.equalsIgnoreCase("Cash"))
			{
				finalCash=finalNetAmount;
			}
			else if(modeOfPayment.equalsIgnoreCase("Card"))
			{
				finalCard=finalNetAmount;
			}
			else if(modeOfPayment.equalsIgnoreCase("Cheque"))
			{
				finalCheque=finalNetAmount;
			}
			else if(modeOfPayment.equalsIgnoreCase("Due"))
			{
				finalDue=finalNetAmount;
			}
			
			
			if(modeOfPayment.equalsIgnoreCase("Cash+Card"))
			{
				int cashAmount=0;
				int cardAmount=0;
				int chequeAmount=0;
				CashPlusCard cashPlusCardLab=new CashPlusCard();
				List<Map<String,String>> multiMode=dueHelper.getMultimode();
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
				cashPlusCardLab.setDescription("Sales due");
				cashPlusCardLab.setPatientRegistrationCashCard((patientRegistration!=null) ? patientRegistration : null);
				cashPlusCardLab.setCardAmount(cardAmount);
				cashPlusCardLab.setCashAmount(cashAmount);
				cashPlusCardLab.setBillNo(billNo);
				cashPlusCardLab.setChequeAmount(chequeAmount);
				cashPlusCardServiceImpl.save(cashPlusCardLab);
				
				
				
			}
		if(patientRegistration!=null && patientRegistration.getpType().equalsIgnoreCase("INPATIENT") )
			{
				//Final Billing  
				 FinalBilling finalBilling=new FinalBilling();
				 finalBilling.setBillNo(billNo);
				 finalBilling.setBillType("Sales due");
				 finalBilling.setCardAmount(finalCard);
				 finalBilling.setCashAmount(finalCash);
				 finalBilling.setChequeAmount(finalCheque);
				 finalBilling.setDueAmount(finalDue);
				 finalBilling.setFinalAmountPaid(finalNetAmount);
				 finalBilling.setFinalBillUser(userSecurity);
				 finalBilling.setName(patientName);
				 finalBilling.setRegNo(regNo);
				 finalBilling.setPaymentType(modeOfPayment);
				 finalBilling.setTotalAmount(finalNetAmount);
				 finalBilling.setUmrNo(patientRegistration.getPatientDetails().getUmr());
				finalBillingServcieImpl.computeSave(finalBilling);
			}
			
			totalNetAmt = saleAmt + returnAmt;


			salesPaymentPdf = new SalesPaymentPdf();

			String roundOff = null;

			/*
			 * String myAd = "Plot No14,15,16 & 17,Nandi Co-op.Society," +
			 * "\n                             Main Road, Beside Navya Grand Hotel, \n                                       Miyapur,Hyderabad-49   \n                               "
			 * + "        Phone:040-23046789    " +
			 * "\n                           Email :udbhavahospitals@gmail.com";
			 * 
			 */	
			//shantharam addr

			String myAd="";
			

			
			byte[] pdfByte = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			Document document = new Document(PageSize.A4_LANDSCAPE);
			try {

				Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");
				Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
				Font redFont2 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
				Font redFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
				Font redFont3 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
				PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

				document.open();
				PdfPTable table = new PdfPTable(2);

				Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());
				// img.setWidthPercentage(20);
				img.scaleAbsolute(65, 90);
				table.setWidthPercentage(108);

				Phrase pq = new Phrase(new Chunk(img, 0, -80));
				pq.add(new Chunk(myAd, redFont));

				PdfPCell cellp = new PdfPCell(pq);
				PdfPCell cell1 = new PdfPCell();

				// for header bold
				PdfPTable table96 = new PdfPTable(1);
				table96.setWidths(new float[] { 5f });
				table96.setSpacingBefore(10);

				PdfPCell hcell96;
				hcell96 = new PdfPCell(new Phrase("UDHBAVA PHARMACY ", redFont1));
				hcell96.setBorder(Rectangle.NO_BORDER);
				hcell96.setHorizontalAlignment(Element.ALIGN_CENTER);
				hcell96.setPaddingLeft(30f);

				table96.addCell(hcell96);
				cell1.addElement(table96);

				cell1.setFixedHeight(110f);
				cell1.addElement(pq);
				cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell1);

				PdfPCell cell0 = new PdfPCell();

				PdfPTable table2 = new PdfPTable(3);
				table2.setWidths(new float[] { 5f, 1f, 5f });
				table2.setSpacingBefore(10);

				PdfPCell hcell1;
				hcell1 = new PdfPCell(new Phrase("Bill#", redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-15f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(":", redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-35f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(billNo, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-25f);
				table2.addCell(hcell1);

				// Display a date in day, month, year format
				Date date = Calendar.getInstance().getTime();
				DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
				String today = formatter.format(date).toString();

				String expdate = null;

				PdfPCell hcel123;
				hcel123 = new PdfPCell(new Phrase("Bill Date", redFont));
				hcel123.setBorder(Rectangle.NO_BORDER);
				hcel123.setPaddingLeft(-15f);

				hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel123);

				hcel123 = new PdfPCell(new Phrase(":", redFont));
				hcel123.setBorder(Rectangle.NO_BORDER);
				hcel123.setPaddingLeft(-35f);
				hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel123);

				hcel123 = new PdfPCell(new Phrase(today, redFont));
				hcel123.setBorder(Rectangle.NO_BORDER);
				hcel123.setPaddingLeft(-25f);
				hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel123);

				PdfPCell hcell18;
				hcell18 = new PdfPCell(new Phrase("Patient Name", redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-15f);
				table2.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(":", redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-35f);
				table2.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(patientName, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-25f);
				table2.addCell(hcell18);

				PdfPCell hcel;

				hcel = new PdfPCell(new Phrase("UMR No", redFont));
				hcel.setBorder(Rectangle.NO_BORDER);
				hcel.setPaddingLeft(-15f);
				table2.addCell(hcel);

				hcel = new PdfPCell(new Phrase(":", redFont));
				hcel.setBorder(Rectangle.NO_BORDER);
				hcel.setPaddingLeft(-35f);
				table2.addCell(hcel);

				hcel = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getUmr(), redFont));
				hcel.setBorder(Rectangle.NO_BORDER);
				hcel.setPaddingLeft(-25f);
				table2.addCell(hcel);

				PdfPCell hcel11;
				hcel11 = new PdfPCell(new Phrase("P.RegNo", redFont));
				hcel11.setBorder(Rectangle.NO_BORDER);
				hcel11.setPaddingLeft(-15f);
				hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel11);

				hcel11 = new PdfPCell(new Phrase(":", redFont));
				hcel11.setBorder(Rectangle.NO_BORDER);
				hcel11.setPaddingLeft(-35f);
				hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel11);

				hcel11 = new PdfPCell(new Phrase(patientRegistration.getRegId(), redFont));
				hcel11.setBorder(Rectangle.NO_BORDER);
				hcel11.setPaddingLeft(-25f);
				hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel11);

				PdfPCell hcel1;

				hcel1 = new PdfPCell(new Phrase("Doctor Name", redFont));
				hcel1.setBorder(Rectangle.NO_BORDER);
				hcel1.setPaddingLeft(-15f);
				table2.addCell(hcel1);

				hcel1 = new PdfPCell(new Phrase(":", redFont));
				hcel1.setBorder(Rectangle.NO_BORDER);
				hcel1.setPaddingLeft(-35f);
				table2.addCell(hcel1);

				hcel1 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getConsultant(), redFont));
				hcel1.setBorder(Rectangle.NO_BORDER);
				hcel1.setPaddingLeft(-25f);
				table2.addCell(hcel1);

				cell0.setFixedHeight(100f);
				cell0.setColspan(2);
				cell0.addElement(table2);
				table.addCell(cell0);

				PdfPCell cell19 = new PdfPCell();

				PdfPTable table21 = new PdfPTable(3);
				table21.setWidths(new float[] { 2f, 5f, 2f });
				table21.setSpacingBefore(10);

				PdfPCell hcell15;
				hcell15 = new PdfPCell(new Phrase("", redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingLeft(-70f);
				table21.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase("Pharmacy Settled Receipt", redFont3));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingLeft(35);
				hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
				table21.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase("", redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingRight(-40f);
				hcell15.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table21.addCell(hcell15);

				cell19.setFixedHeight(20f);
				cell19.setColspan(2);
				cell19.addElement(table21);
				table.addCell(cell19);

				PdfPCell cell3 = new PdfPCell();

				PdfPTable table1 = new PdfPTable(10);
				table1.setWidths(new float[] { 1.5f, 5f, 5f, 2f, 3f, 2f, 2f, 2f, 2f, 2f });

				table1.setSpacingBefore(10);

				PdfPCell hcell;
				hcell = new PdfPCell(new Phrase("S.No", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Item Name", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Manf Name", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Batch No", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Exp Date", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Qty", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Ret Qty", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Net Qty", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("MRP", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Sale Value", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);
				int count = 0;

				float totalsaleValue = 0;

				for (Map<String, String> showInfo : show) {

					MedicineDetails medicineDetails1 = medicineDetailsServiceImpl
							.findByName(showInfo.get("medicineName"));
					List<MedicineProcurement> medicineProcurement = medicineProcurementServiceImpl
							.findByBatchAndMedicine(showInfo.get("batch"), medicineDetails1.getMedicineId());

					PdfPCell cell;

					cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(showInfo.get("medicineName"), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(-5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					// cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					// cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(showInfo.get("batch"), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(cell);

					// for convert db date to dmy format

					expdate = medicineProcurement.get(medicineProcurement.size() - 1).getExpDate().toString()
							.substring(0, 10);
					SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
					expdate = toFormat.format(fromFormat.parse(expdate));

					System.out.println(expdate);

					cell = new PdfPCell(new Phrase(expdate, redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(showInfo.get("quantity"), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);
					cell = new PdfPCell(new Phrase(showInfo.get("returnqty"), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);
					System.out.println("--------------inpdf----------");
					System.out.println(netQuantity);

					cell = new PdfPCell(new Phrase(showInfo.get("netQty"), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(showInfo.get("mrp"), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					// float amount = Long.parseLong(showInfo.get("netQty")) *
					// Float.parseFloat(showInfo.get("mrp"));

					cell = new PdfPCell(new Phrase(String.valueOf(showInfo.get("saleValue")), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					totalsaleValue = totalsaleValue + Float.parseFloat(showInfo.get("saleValue"));

				}
				cell3.setColspan(2);
				table1.setWidthPercentage(100f);
				cell3.addElement(table1);
				table.addCell(cell3);

				PdfPCell cell4 = new PdfPCell();

				PdfPTable table4 = new PdfPTable(6);
				table4.setWidths(new float[] { 5f, 1f, 5f, 8f, 1f, 3f });
				table4.setSpacingBefore(10);

				// int ttl=(int)Math.round(total);
				PdfPCell hcell2;
				hcell2 = new PdfPCell(new Phrase("", redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(-50f);
				table4.addCell(hcell2);

				hcell2 = new PdfPCell(new Phrase(":", redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(-40f);
				table4.addCell(hcell2);
				// numberToWordsConverter.convert(ttl)
				hcell2 = new PdfPCell(new Phrase(" ", redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(-50f);
				table4.addCell(hcell2);

				hcell2 = new PdfPCell(new Phrase("Total Sale Value", redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(85f);
				table4.addCell(hcell2);

				hcell2 = new PdfPCell(new Phrase(":", redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell2.setPaddingRight(-30f);
				table4.addCell(hcell2);
				// (Math.round(total*100.0)/100.0
				hcell2 = new PdfPCell(
						new Phrase(String.valueOf((Math.round(totalsaleValue * 100.0) / 100.0)), redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell2.setPaddingRight(-40f);
				table4.addCell(hcell2);

				PdfPCell hcell04;
				hcell04 = new PdfPCell(new Phrase(""));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
				table4.addCell(hcell04);

				hcell04 = new PdfPCell(new Phrase(""));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
				table4.addCell(hcell04);

				hcell04 = new PdfPCell(new Phrase(""));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
				table4.addCell(hcell04);

				hcell04 = new PdfPCell(new Phrase("Net Amt", redFont));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell04.setPaddingLeft(85f);
				table4.addCell(hcell04);

				hcell04 = new PdfPCell(new Phrase(":", redFont));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell04.setPaddingRight(-30f);
				table4.addCell(hcell04);

				hcell04 = new PdfPCell(new Phrase(String.valueOf((Math.round(totalNetAmt * 100.0) / 100.0)), redFont));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell04.setPaddingRight(-40f);
				table4.addCell(hcell04);

				PdfPCell hcell4;
				hcell4 = new PdfPCell(new Phrase(""));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				table4.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(""));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				table4.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(""));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				table4.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase("Due Recieved", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(85f);
				table4.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(":", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell4.setPaddingRight(-30f);
				table4.addCell(hcell4);

				// Math.round(total)
				hcell4 = new PdfPCell(new Phrase(String.valueOf("0.0"), redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell4.setPaddingRight(-40f);
				table4.addCell(hcell4);

				PdfPCell hcell9;
				hcell9 = new PdfPCell(new Phrase(""));
				hcell9.setBorder(Rectangle.NO_BORDER);
				hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
				table4.addCell(hcell9);

				hcell9 = new PdfPCell(new Phrase(""));
				hcell9.setBorder(Rectangle.NO_BORDER);
				hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
				table4.addCell(hcell9);

				hcell9 = new PdfPCell(new Phrase(""));
				hcell9.setBorder(Rectangle.NO_BORDER);
				hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
				table4.addCell(hcell9);

				hcell9 = new PdfPCell(new Phrase("Due Amt", redFont));
				hcell9.setBorder(Rectangle.NO_BORDER);
				hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell9.setPaddingLeft(85f);
				table4.addCell(hcell9);

				hcell9 = new PdfPCell(new Phrase(":", redFont));
				hcell9.setBorder(Rectangle.NO_BORDER);
				hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell9.setPaddingRight(-30f);
				table4.addCell(hcell9);
				// Math.round(total)
				hcell9 = new PdfPCell(new Phrase(String.valueOf("0.0"), redFont));
				hcell9.setBorder(Rectangle.NO_BORDER);
				hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell9.setPaddingRight(-40f);
				table4.addCell(hcell9);
				cell4.setFixedHeight(65f);
				cell4.setColspan(2);
				cell4.addElement(table4);
				table.addCell(cell4);

				// for new row

				PdfPCell cell33 = new PdfPCell();

				PdfPTable table13 = new PdfPTable(5);
				table13.setWidths(new float[] { 4f, 4f, 4f, 1f, 2f });

				table13.setSpacingBefore(10);

				PdfPCell hcell33;
				hcell33 = new PdfPCell(new Phrase("", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(10f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase("", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(35f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase("Total Reciept Amt", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(55f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase(":", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(24f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase(String.valueOf((Math.round(totalNetAmt * 100.0) / 100.0)), redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell33.setPaddingRight(15f);
				table13.addCell(hcell33);

				PdfPCell hcell331;
				hcell331 = new PdfPCell(new Phrase("Recivers Signature", redFont1));
				hcell331.setBorder(Rectangle.NO_BORDER);
				hcell331.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell331.setPaddingLeft(10f);
				table13.addCell(hcell331);

				hcell331 = new PdfPCell(new Phrase("Pharmasist", redFont1));
				hcell331.setBorder(Rectangle.NO_BORDER);
				hcell331.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell331.setPaddingLeft(35f);
				table13.addCell(hcell331);

				hcell331 = new PdfPCell(new Phrase("", redFont1));
				hcell331.setBorder(Rectangle.NO_BORDER);
				hcell331.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell331.setPaddingLeft(40f);
				table13.addCell(hcell331);

				hcell331 = new PdfPCell(new Phrase("", redFont1));
				hcell331.setBorder(Rectangle.NO_BORDER);
				hcell331.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell331.setPaddingLeft(40f);
				table13.addCell(hcell331);

				hcell331 = new PdfPCell(new Phrase("", redFont1));
				hcell331.setBorder(Rectangle.NO_BORDER);
				hcell331.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell331.setPaddingLeft(50f);
				table13.addCell(hcell331);

				cell33.setFixedHeight(40f);
				cell33.setColspan(2);
				table13.setWidthPercentage(100f);
				cell33.addElement(table13);
				table.addCell(cell33);

				// for new row end

				PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository.findByShopLocation("Miyapur");

				PdfPCell cell01 = new PdfPCell();

				PdfPTable table211 = new PdfPTable(3);
				table211.setWidths(new float[] { 4f, 4f, 5f });
				table211.setSpacingBefore(10);

				PdfPCell hcell11;
				hcell11 = new PdfPCell(new Phrase("GST#    :  " + pharmacyShopDetails.getGstNO(), redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setPaddingLeft(-50f);
				table211.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase("D.L#    :  " + pharmacyShopDetails.getDlNo(), redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_CENTER);
				table211.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase("CIN#    :  " + pharmacyShopDetails.getCinNO(), redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setPaddingRight(-40f);
				hcell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table211.addCell(hcell11);

				cell01.setFixedHeight(20f);
				cell01.setColspan(2);
				cell01.addElement(table211);
				table.addCell(cell01);

				PdfPCell cell6 = new PdfPCell();

				PdfPTable table5 = new PdfPTable(2);
				table5.setWidths(new float[] { 4f, 4f });
				table5.setSpacingBefore(0);

				PdfPCell hcell5;
				hcell5 = new PdfPCell(new Phrase("Created By   : " + createdBy, redFont));
				hcell5.setBorder(Rectangle.NO_BORDER);
				hcell5.setPaddingLeft(-50f);
				hcell5.setPaddingTop(10f);
				table5.addCell(hcell5);

				hcell5 = new PdfPCell(new Phrase("Created Date   : " + today, redFont));
				hcell5.setBorder(Rectangle.NO_BORDER);
				hcell5.setPaddingTop(10f);
				hcell5.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table5.addCell(hcell5);

				PdfPCell hcell6;
				hcell6 = new PdfPCell(new Phrase("Printed By    : " + createdBy, redFont));
				hcell6.setBorder(Rectangle.NO_BORDER);
				hcell6.setPaddingLeft(-50f);
				hcell6.setPaddingTop(1f);
				table5.addCell(hcell6);

				hcell6 = new PdfPCell(new Phrase("Printed Date    : " + today, redFont));
				hcell6.setBorder(Rectangle.NO_BORDER);
				// hcell6.setPaddingLeft(-70f);
				hcell6.setPaddingTop(1f);
				hcell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table5.addCell(hcell6);

				cell6.setFixedHeight(80f);
				cell6.setColspan(2);
				cell6.addElement(table5);
				table.addCell(cell6);

				document.add(table);

				document.close();
				System.out.println("finished");
				pdfByte = byteArrayOutputStream.toByteArray();
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
						.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();

				salesPaymentPdf = new SalesPaymentPdf();
				salesPaymentPdf.setFileName(regNo + " Pharmacy Due");
				salesPaymentPdf.setFileuri(uri);
				salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
				salesPaymentPdf.setData(pdfByte);
				salesPaymentPdfServiceImpl.save(salesPaymentPdf);

			} catch (Exception e) {
				Logger.error(e.getMessage());
			}
		} else if (dueType.equalsIgnoreCase("Lab")) {

			String patientName = null;
			// for other receipts leaving service slip
			//shantharam addr

			String addr = " ";

			List<LaboratoryRegistration> laboratoryRegistrationInfor = laboratoryRegistrationRepository
					.findByInvoiceNo(billNo);

			for (LaboratoryRegistration labInfo : laboratoryRegistrationInfor) {
				labInfo.setPaymentType(modeOfPayment);
				labInfo.setPaid("YES");
				labInfo.setUpdatedBy(createdid);
				labInfo.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
				labInfo.setEnteredBy(createdid);

				List<ChargeBill> chargebill = labInfo.getChargeBill();

				for (ChargeBill chargebillInfo : chargebill) {

					chargebillInfo.setPaid("YES");
					chargebillInfo.setUpdatedBy(createdid);
					chargebillInfo.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
					chargebillInfo.setPaymentType(modeOfPayment);
				}
			}

			PatientRegistration patientRegistration = laboratoryRegistrationInfor.get(0)
					.getLaboratoryPatientRegistration();
			PatientDetails pat = patientRegistration.getPatientDetails();

			patientName = (pat.getMiddleName() != null)
					? pat.getTitle() + " " + pat.getFirstName() + " " + pat.getMiddleName() + " " + pat.getLastName()
					: pat.getTitle() + " " + pat.getFirstName() + " " + pat.getLastName();
					
					finalNetAmount=dueHelper.getAmount();
					

					// Cash + Card
					
					if(modeOfPayment.equalsIgnoreCase("Cash"))
					{
						finalCash=finalNetAmount;
					}
					else if(modeOfPayment.equalsIgnoreCase("Card"))
					{
						finalCard=finalNetAmount;
					}
					else if(modeOfPayment.equalsIgnoreCase("Cheque"))
					{
						finalCheque=finalNetAmount;
					}
					else if(modeOfPayment.equalsIgnoreCase("Due"))
					{
						finalDue=finalNetAmount;
					}
					
					
					if(modeOfPayment.equalsIgnoreCase("Cash+Card"))
					{
						int cashAmount=0;
						int cardAmount=0;
						int chequeAmount=0;
						CashPlusCard cashPlusCardLab=new CashPlusCard();
						List<Map<String,String>> multiMode=dueHelper.getMultimode();
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
						cashPlusCardLab.setDescription("Lab Due");
						cashPlusCardLab.setPatientRegistrationCashCard((patientRegistration!=null) ? patientRegistration : null);
						cashPlusCardLab.setCardAmount(cardAmount);
						cashPlusCardLab.setCashAmount(cashAmount);
						cashPlusCardLab.setBillNo(laboratoryRegistrationInfor.get(0).getBillNo());
						cashPlusCardLab.setChequeAmount(chequeAmount);
						cashPlusCardServiceImpl.save(cashPlusCardLab);
						
						
						
					}
				if(patientRegistration!=null && patientRegistration.getpType().equalsIgnoreCase("INPATIENT") )
					{
						//Final Billing  
						 FinalBilling finalBilling=new FinalBilling();
						 finalBilling.setBillNo(laboratoryRegistrationInfor.get(0).getBillNo());
						 finalBilling.setBillType("Lab due");
						 finalBilling.setCardAmount(finalCard);
						 finalBilling.setCashAmount(finalCash);
						 finalBilling.setChequeAmount(finalCheque);
						 finalBilling.setDueAmount(finalDue);
						 finalBilling.setFinalAmountPaid(finalNetAmount);
						 finalBilling.setFinalBillUser(userSecurity);
						 finalBilling.setName(patientName);
						 finalBilling.setRegNo(regNo);
						 finalBilling.setPaymentType(modeOfPayment);
						 finalBilling.setTotalAmount(finalNetAmount);
						 finalBilling.setUmrNo(patientRegistration.getPatientDetails().getUmr());
						finalBillingServcieImpl.computeSave(finalBilling);
					}

					

			byte[] pdfBytes = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			Document document = new Document(PageSize.A4_LANDSCAPE);

			try {

				Resource fileResource = resourceLoader.getResource("classpath:udbhava.png");
				Chunk cnd1 = new Chunk(new VerticalPositionMark());
				Font redFont1 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
				PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
				Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
				Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
				Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

				document.open();
				PdfPTable table = new PdfPTable(2);

				Image img = Image.getInstance(fileResource.getFile().getAbsolutePath());
				img.scaleAbsolute(56, 87);
				table.setWidthPercentage(105);

				Phrase pq = new Phrase(new Chunk(img, 0, -70));

				pq.add(new Chunk(addr, redFont));
				PdfPCell cellp = new PdfPCell(pq);
				PdfPCell cell1 = new PdfPCell();

				// Display a date in day, month, year format
				Date dateInfo = Calendar.getInstance().getTime();
				DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
				String today = formatter.format(dateInfo).toString();

				// for header Bold
				PdfPTable table96 = new PdfPTable(1);
				table96.setWidths(new float[] { 5f });
				table96.setSpacingBefore(10);

				PdfPCell hcell96;
				hcell96 = new PdfPCell(new Phrase("UDBHAVA HOSPITALS", headFont1));
				hcell96.setBorder(Rectangle.NO_BORDER);
				hcell96.setHorizontalAlignment(Element.ALIGN_CENTER);
				hcell96.setPaddingLeft(50f);

				table96.addCell(hcell96);
				cell1.addElement(table96);

				// for header end

				cell1.addElement(pq);
				cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell1);

				PdfPCell cell3 = new PdfPCell();

				PdfPTable table99 = new PdfPTable(3);
				table99.setWidths(new float[] { 3f, 1f, 4f });
				table99.setSpacingBefore(10);

				PdfPCell hcell90;
				hcell90 = new PdfPCell(new Phrase("Patient", redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingBottom(-7f);
				hcell90.setPaddingLeft(-25f);

				table99.addCell(hcell90);

				// table.addCell(cell3);

				hcell90 = new PdfPCell(new Phrase(":", redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingBottom(-7f);
				hcell90.setPaddingLeft(-61f);

				table99.addCell(hcell90);

				hcell90 = new PdfPCell(new Phrase(patientName, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingBottom(-7f);
				hcell90.setPaddingLeft(-85f);
				table99.addCell(hcell90);

				cell3.addElement(table99);

				PdfPTable table2 = new PdfPTable(6);
				table2.setWidths(new float[] { 3f, 1.2f, 5.8f, 3f, 1f, 4f });
				table2.setSpacingBefore(10);

				PdfPCell hcell1;
				hcell1 = new PdfPCell(new Phrase("Age/Sex", redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-25f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(":", redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-20f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getAge() + "/"
						+ patientRegistration.getPatientDetails().getGender(), redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-30f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase("UMR NO", redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingRight(-40f);
				// hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(":", redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
				// hcell1.setPaddingTop(-5f);;
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getUmr(), redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingRight(-30f);
				// hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				PdfPCell hcell4;
				hcell4 = new PdfPCell(new Phrase("Bill Dt", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-25f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(":", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-20f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(today, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-30f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase("INV No", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingRight(-27.5f);
				// hcell4.setPaddingLeft(25f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(":", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				// hcell1.setPaddingTop(-5f);;
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(laboratoryRegistrationInfor.get(0).getInvoiceNo(), redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingRight(-27.5f);
				// hcell1.setPaddingTop(-5f);
				table2.addCell(hcell4);

				PdfPCell hcell15;
				hcell15 = new PdfPCell(new Phrase("Ref.By", redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-25f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(":", redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-20f);
				table2.addCell(hcell15);

				String refBy = null;
				if (patientRegistration.getPatientDetails().getvRefferalDetails() == null) {
					refBy = "";
				} else {
					refBy = patientRegistration.getPatientDetails().getvRefferalDetails().getRefName();
				}
				hcell15 = new PdfPCell(new Phrase(refBy, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-30f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase("Phone", redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				// hcell15.setPaddingRight(7f);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingRight(-27.5f);

				// hcell15.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(":", redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_RIGHT);
				// hcell1.setPaddingTop(-5f);;
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(
						new Phrase(String.valueOf(patientRegistration.getPatientDetails().getMobile()), redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingRight(-27.5f);
				// hcell1.setPaddingTop(-5f);
				table2.addCell(hcell15);

				cell3.setFixedHeight(115f);
				cell3.setColspan(2);
				cell3.addElement(table2);

				PdfPTable table98 = new PdfPTable(3);
				table98.setWidths(new float[] { 3f, 1f, 4f });
				table98.setSpacingBefore(10);

				PdfPCell hcell91;
				hcell91 = new PdfPCell(new Phrase("Consultant", redFont));
				hcell91.setBorder(Rectangle.NO_BORDER);
				hcell91.setHorizontalAlignment(Element.ALIGN_LEFT);
				// hcell91.setPaddingBottom(3f);
				hcell91.setPaddingTop(-5f);
				hcell91.setPaddingLeft(-25f);
				table98.addCell(hcell91);

				hcell91 = new PdfPCell(new Phrase(":", redFont));
				hcell91.setBorder(Rectangle.NO_BORDER);
				hcell91.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell91.setPaddingTop(-5f);
				hcell91.setPaddingLeft(-61f);
				table98.addCell(hcell91);

				hcell91 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getConsultant(), redFont));
				hcell91.setBorder(Rectangle.NO_BORDER);
				hcell91.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell91.setPaddingTop(-5f);
				hcell91.setPaddingLeft(-85f);
				table98.addCell(hcell91);

				cell3.addElement(table98);

				/*
				 * PdfPTable table97 = new PdfPTable(1); table97.setWidths(new float[] { 5f });
				 * table97.setSpacingBefore(10);
				 * 
				 * PdfPCell hcell97; hcell97 = new PdfPCell(new Phrase( "*" + "OBN0094995" + "*"
				 * + "  " + "==> Scan This BarCode To Take Report At KIOSK", headFont1));
				 * hcell97.setBorder(Rectangle.NO_BORDER); //
				 * hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
				 * hcell97.setPaddingBottom(-10f); hcell97.setPaddingLeft(-35f);
				 * 
				 * table97.addCell(hcell97); cell3.addElement(table97);
				 */
				table.addCell(cell3);

				// *****************************

				PdfPCell cell19 = new PdfPCell();

				PdfPTable table21 = new PdfPTable(1);
				table21.setWidths(new float[] { 4f });
				table21.setSpacingBefore(10);

				PdfPCell hcell19;
				hcell19 = new PdfPCell(new Phrase("OP/IP Settled Reciept", headFont1));
				hcell19.setBorder(Rectangle.NO_BORDER);
				hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
				// hcell19.setPaddingLeft(-70f);
				table21.addCell(hcell19);

				cell19.setFixedHeight(20f);
				cell19.setColspan(2);
				cell19.addElement(table21);
				table.addCell(cell19);

				// **************
				PdfPCell cell31 = new PdfPCell();

				PdfPTable table1 = new PdfPTable(8);
				table1.setWidths(new float[] { 1f, 3f, 5f, 3f, 1f, 2f, 2f, 2f });

				table1.setSpacingBefore(10);

				PdfPCell hcell;
				hcell = new PdfPCell(new Phrase("S.No", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Service Code", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Service Name", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Service Type", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Qty", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Rate", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Discount", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Amount(RS)", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				int count = 0;
				long total = 0;
				for (LaboratoryRegistration laboratoryRegistrationInfo : laboratoryRegistrationInfor) {

					PdfPCell cell;

					cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(
							new Phrase(laboratoryRegistrationInfo.getLabServices().getServiceId(), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(laboratoryRegistrationInfo.getServiceName(), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(cell);

					cell = new PdfPCell(
							new Phrase(laboratoryRegistrationInfo.getLabServices().getServiceType(), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(laboratoryRegistrationInfo.getQuantity()), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(laboratoryRegistrationInfo.getPrice()), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(laboratoryRegistrationInfo.getDiscount()), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(laboratoryRegistrationInfo.getNetAmount()), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					total += laboratoryRegistrationInfo.getNetAmount();

				}

				/*
				 * cell31.setColspan(2); table1.setWidthPercentage(100f);
				 * cell31.addElement(table1); //cell31.addElement(table37);
				 * table.addCell(cell31);
				 */
				// -------------------------------

				PdfPTable table37 = new PdfPTable(6);
				table37.setWidths(new float[] { 3f, 1f, 4f, 7f, 1f, 4f });
				table37.setSpacingBefore(10);

				PdfPCell cell55;
				cell55 = new PdfPCell(new Phrase("", redFont));
				cell55.setBorder(Rectangle.NO_BORDER);
				cell55.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell55.setPaddingTop(10f);
				// cell55.setPaddingLeft(-50f);
				table37.addCell(cell55);

				cell55 = new PdfPCell(new Phrase("", redFont));
				cell55.setBorder(Rectangle.NO_BORDER);
				cell55.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell55.setPaddingTop(10f);
				// cell55.setPaddingLeft(-50f);
				table37.addCell(cell55);

				cell55 = new PdfPCell(new Phrase("", redFont));
				cell55.setBorder(Rectangle.NO_BORDER);
				cell55.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell55.setPaddingTop(10f);
				// cell55.setPaddingLeft(-50f);
				table37.addCell(cell55);

				cell55 = new PdfPCell(new Phrase("Gross Amt", redFont));
				cell55.setBorder(Rectangle.NO_BORDER);
				cell55.setPaddingTop(10f);
				cell55.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell55.setPaddingRight(-70f);
				table37.addCell(cell55);

				cell55 = new PdfPCell(new Phrase(":", redFont));
				cell55.setBorder(Rectangle.NO_BORDER);
				cell55.setPaddingTop(10f);
				cell55.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell55.setPaddingRight(-60f);
				table37.addCell(cell55);

				cell55 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
				cell55.setBorder(Rectangle.NO_BORDER);
				cell55.setPaddingTop(10f);
				cell55.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell55.setPaddingRight(-30f);
				table37.addCell(cell55);

				PdfPCell hcell56;
				hcell56 = new PdfPCell(new Phrase("", redFont));
				hcell56.setBorder(Rectangle.NO_BORDER);
				hcell56.setPaddingLeft(-1f);
				hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell56);

				hcell56 = new PdfPCell(new Phrase("", redFont));
				hcell56.setBorder(Rectangle.NO_BORDER);
				hcell56.setPaddingLeft(-1f);
				hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell56);

				hcell56 = new PdfPCell(new Phrase("", redFont));
				hcell56.setBorder(Rectangle.NO_BORDER);
				hcell56.setPaddingLeft(-1f);
				hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell56);

				hcell56 = new PdfPCell(new Phrase("Paid Amt.", redFont));
				hcell56.setBorder(Rectangle.NO_BORDER);
				hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell56.setPaddingRight(-70f);
				table37.addCell(hcell56);

				hcell56 = new PdfPCell(new Phrase(":", redFont));
				hcell56.setBorder(Rectangle.NO_BORDER);
				// hcell56.setPaddingLeft(-1f);
				hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell56.setPaddingRight(-60f);
				table37.addCell(hcell56);

				hcell56 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
				hcell56.setBorder(Rectangle.NO_BORDER);
				// hcell56.setPaddingLeft(-1f);
				hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell56.setPaddingRight(-30f);
				table37.addCell(hcell56);

				PdfPCell hcell57;
				hcell57 = new PdfPCell(new Phrase(modeOfPayment + " Amt.", redFont));
				hcell57.setBorder(Rectangle.NO_BORDER);
				hcell57.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell57.setPaddingLeft(-50f);
				table37.addCell(hcell57);

				hcell57 = new PdfPCell(new Phrase(":", redFont));
				hcell57.setBorder(Rectangle.NO_BORDER);
				hcell57.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell57.setPaddingLeft(-50f);
				table37.addCell(hcell57);

				hcell57 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
				hcell57.setBorder(Rectangle.NO_BORDER);
				hcell57.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell57.setPaddingLeft(-40f);
				table37.addCell(hcell57);

				hcell57 = new PdfPCell(new Phrase("Net Amt.", redFont));
				hcell57.setBorder(Rectangle.NO_BORDER);
				hcell57.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell57.setPaddingRight(-70f);
				table37.addCell(hcell57);

				hcell57 = new PdfPCell(new Phrase(":", redFont));
				hcell57.setBorder(Rectangle.NO_BORDER);
				hcell57.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell57.setPaddingRight(-60f);
				table37.addCell(hcell57);

				hcell57 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
				hcell57.setBorder(Rectangle.NO_BORDER);
				hcell57.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell57.setPaddingRight(-30f);
				table37.addCell(hcell57);

				PdfPCell hcell58;
				hcell58 = new PdfPCell(new Phrase(""));
				hcell58.setBorder(Rectangle.NO_BORDER);
				// hcell23.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table37.addCell(hcell58);

				hcell58 = new PdfPCell(new Phrase(""));
				hcell58.setBorder(Rectangle.NO_BORDER);
				// hcell23.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table37.addCell(hcell58);

				hcell58 = new PdfPCell(new Phrase(""));
				hcell58.setBorder(Rectangle.NO_BORDER);
				// hcell23.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table37.addCell(hcell58);

				hcell58 = new PdfPCell(new Phrase("Received Amt.", redFont));
				hcell58.setBorder(Rectangle.NO_BORDER);
				hcell58.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell58.setPaddingRight(-70f);
				table37.addCell(hcell58);

				hcell58 = new PdfPCell(new Phrase(":", redFont));
				hcell58.setBorder(Rectangle.NO_BORDER);
				hcell58.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell58.setPaddingRight(-60f);
				table37.addCell(hcell58);

				if (!modeOfPayment.equalsIgnoreCase("Due")) {
					hcell58 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
					hcell58.setBorder(Rectangle.NO_BORDER);
					hcell58.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell58.setPaddingRight(-30f);
					table37.addCell(hcell58);
				} else {
					hcell58 = new PdfPCell(new Phrase(String.valueOf(0), redFont));
					hcell58.setBorder(Rectangle.NO_BORDER);
					hcell58.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell58.setPaddingRight(-30f);
					table37.addCell(hcell58);
				}
				PdfPCell hcell59;
				hcell59 = new PdfPCell(new Phrase("Gross Amount In Words ", redFont));
				hcell59.setBorder(Rectangle.NO_BORDER);
				hcell59.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell59.setPaddingLeft(-50f);
				table37.addCell(hcell59);

				hcell59 = new PdfPCell(new Phrase("", headFont));
				hcell59.setBorder(Rectangle.NO_BORDER);
				// hcell57.setPaddingTop(18f);
				hcell59.setHorizontalAlignment(Element.ALIGN_LEFT);
				table37.addCell(hcell59);

				hcell59 = new PdfPCell(new Phrase("(" + NumberToWordsConverter.convert(total) + ")", redFont));
				hcell59.setBorder(Rectangle.NO_BORDER);
				hcell59.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell59.setPaddingLeft(-35f);
				table37.addCell(hcell59);

				hcell59 = new PdfPCell(new Phrase("", headFont));
				hcell59.setBorder(Rectangle.NO_BORDER);
				// hcell57.setPaddingTop(18f);
				hcell59.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell59);

				hcell59 = new PdfPCell(new Phrase("", headFont));
				hcell59.setBorder(Rectangle.NO_BORDER);
				// hcell57.setPaddingTop(18f);
				hcell59.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell59);

				hcell59 = new PdfPCell(new Phrase("", headFont));
				hcell59.setBorder(Rectangle.NO_BORDER);
				// hcell57.setPaddingTop(18f);
				hcell59.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell59);

				PdfPCell hcell60;
				hcell60 = new PdfPCell(new Phrase("Received Amount In Words ", redFont));

				hcell60.setBorder(Rectangle.NO_BORDER);
				hcell60.setPaddingLeft(-50f);
				hcell60.setHorizontalAlignment(Element.ALIGN_LEFT);
				table37.addCell(hcell60);

				hcell60 = new PdfPCell(new Phrase("", headFont));
				hcell60.setBorder(Rectangle.NO_BORDER);
				// hcell57.setPaddingTop(18f);
				hcell60.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell60);

				if (!modeOfPayment.equalsIgnoreCase("Due")) {
					hcell60 = new PdfPCell(new Phrase("(" + NumberToWordsConverter.convert(total) + ")", redFont));
					hcell60.setBorder(Rectangle.NO_BORDER);
					hcell60.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell60.setPaddingLeft(-20f);
					table37.addCell(hcell60);
				} else {
					hcell60 = new PdfPCell(new Phrase("(" + NumberToWordsConverter.convert(0) + ")", redFont));
					hcell60.setBorder(Rectangle.NO_BORDER);
					hcell60.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell60.setPaddingLeft(-20f);
					table37.addCell(hcell60);

				}
				hcell60 = new PdfPCell(new Phrase("", headFont));
				hcell60.setBorder(Rectangle.NO_BORDER);
				// hcell57.setPaddingTop(18f);
				hcell60.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell60);

				hcell60 = new PdfPCell(new Phrase("", headFont));
				hcell60.setBorder(Rectangle.NO_BORDER);
				// hcell57.setPaddingTop(18f);
				hcell60.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell60);

				hcell60 = new PdfPCell(new Phrase("", headFont));
				hcell60.setBorder(Rectangle.NO_BORDER);
				// hcell57.setPaddingTop(18f);
				hcell60.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table37.addCell(hcell60);

				cell31.setColspan(2);
				// cell31.setFixedHeight(170f);
				table1.setWidthPercentage(100f);
				cell31.addElement(table1);
				cell31.addElement(table37);
				table.addCell(cell31);

				// -----------------------

				PdfPCell cell5 = new PdfPCell();

				PdfPTable table35 = new PdfPTable(2);
				table35.setWidths(new float[] { 5f, 4f });
				table35.setSpacingBefore(10);

				PdfPCell hcell12;
				hcell12 = new PdfPCell(new Phrase("Created By    : " + createdBy, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				// hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell12.setPaddingTop(10f);
				hcell12.setPaddingLeft(-50f);
				table35.addCell(hcell12);

				hcell12 = new PdfPCell(new Phrase("Created Dt   :   " + today, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				hcell12.setPaddingTop(10f);
				// hcell12.setPaddingRight(0f);
				hcell12.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell12);

				PdfPCell hcell13;
				hcell13 = new PdfPCell(new Phrase("Printed By     : " + createdBy, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table35.addCell(hcell13);

				hcell13 = new PdfPCell(new Phrase("Print Dt       :   " + today, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingRight(3f);
				hcell13.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell13);

				PdfPCell hcell23;
				hcell23 = new PdfPCell(new Phrase(""));
				hcell23.setBorder(Rectangle.NO_BORDER);
				// hcell23.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table35.addCell(hcell23);

				hcell23 = new PdfPCell(new Phrase("(Authorized Signature)", headFont));
				hcell23.setBorder(Rectangle.NO_BORDER);
				hcell23.setPaddingTop(22f);
				hcell23.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell23);

				cell5.setFixedHeight(90f);
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
				salesPaymentPdf.setFileName(patientRegistration.getRegId() + " Lab Due");
				salesPaymentPdf.setFileuri(uri);
				salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
				salesPaymentPdf.setData(pdfBytes);
				salesPaymentPdfServiceImpl.save(salesPaymentPdf);
			} catch (Exception e) {
				Logger.error(e.getMessage());
			}
		}

		/*	else if (dueType.equalsIgnoreCase("consulting")) {
				
				List<PatientPayment>patientinfo=patientPaymentRepository.findByBillNo(billNo);
				
				for(PatientPayment p:patientinfo) {
					System.out.println(modeOfPayment);
					p.setModeOfPaymant(modeOfPayment);
					p.setPaid("YES");
				}
				
				PatientRegistration patientRegistration1 = patientinfo.get(0)
						.getPatientRegistration();
				PatientDetails pat1 = patientRegistration1.getPatientDetails();

				String patientName9=null;
				patientName9 = (pat1.getMiddleName() != null)
						? pat1.getTitle() + " " + pat1.getFirstName() + " " + pat1.getMiddleName() + " " + pat1.getLastName()
						: pat1.getTitle() + " " + pat1.getFirstName() + " " + pat1.getLastName();
				
				finalNetAmount=dueHelper.getAmount();
				// Cash + Card
				
				if(modeOfPayment.equalsIgnoreCase("Cash"))
				{
					finalCash=finalNetAmount;
				}
				else if(modeOfPayment.equalsIgnoreCase("Card"))
				{
					finalCard=finalNetAmount;
				}
				else if(modeOfPayment.equalsIgnoreCase("Cheque"))
				{
					finalCheque=finalNetAmount;
				}
				else if(modeOfPayment.equalsIgnoreCase("Due"))
				{
					finalDue=finalNetAmount;
				}
				
				
				if(modeOfPayment.equalsIgnoreCase("Cash+Card"))
				{
					int cashAmount=0;
					int cardAmount=0;
					int chequeAmount=0;
					CashPlusCard cashPlusCardLab=new CashPlusCard();
					List<Map<String,String>> multiMode=dueHelper.getMultimode();
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
					cashPlusCardLab.setDescription("consulting Due");
					cashPlusCardLab.setPatientRegistrationCashCard((patientRegistration1!=null) ? patientRegistration1 : null);
					cashPlusCardLab.setCardAmount(cardAmount);
					cashPlusCardLab.setCashAmount(cashAmount);
					cashPlusCardLab.setBillNo(patientinfo.get(0).getBillNo());
					cashPlusCardLab.setChequeAmount(chequeAmount);
					cashPlusCardServiceImpl.save(cashPlusCardLab);
				
				}
			}
*/
			else if (dueType.equalsIgnoreCase("osp")){
				
				List<OspService>ospinfo=ospServiceRepository.findByBillNo(billNo);
				
				User user= userRepository.findOneByUserId(ospinfo.get(0).getRefferedById());
			String docName= 	user.getFirstName()+""+user.getMiddleName()+""+user.getLastName();
				
			
				for(OspService osp:ospinfo) {
				
					osp.setPaymentType(modeOfPayment);
					osp.setPaid("YES");
					osp.setUpdatedBy(createdid);
					osp.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
					osp.setUserOspService(userSecurity);
				}
		
				finalNetAmount=dueHelper.getAmount();
				// Cash + Card
				
				if(modeOfPayment.equalsIgnoreCase("Cash"))
				{
					finalCash=finalNetAmount;
				}
				else if(modeOfPayment.equalsIgnoreCase("Card"))
				{
					finalCard=finalNetAmount;
				}
				else if(modeOfPayment.equalsIgnoreCase("Cheque"))
				{
					finalCheque=finalNetAmount;
				}
				else if(modeOfPayment.equalsIgnoreCase("Due"))
				{
					finalDue=finalNetAmount;
				}
				
				
				if(modeOfPayment.equalsIgnoreCase("Cash+Card"))
				{
					int cashAmount=0;
					int cardAmount=0;
					int chequeAmount=0;
					CashPlusCard cashPlusCardLab=new CashPlusCard();
					List<Map<String,String>> multiMode=dueHelper.getMultimode();
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
				
				
					
					cashPlusCardLab.setDescription("osp Due");
					cashPlusCardLab.setCardAmount(cardAmount);
					cashPlusCardLab.setCashAmount(cashAmount);
					cashPlusCardLab.setBillNo(ospinfo.get(0).getBillNo());
					cashPlusCardLab.setChequeAmount(chequeAmount);
					cashPlusCardServiceImpl.save(cashPlusCardLab);
					
			}
					
					try {
						

						//shantharam addr
						String newAddress="";
						
									
						
						
						byte[] pdfByte=null;
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

						Document document = new Document(PageSize.A4_LANDSCAPE);
					
						Resource fileResource=resourceLoader.getResource("classpath:udbhava.png");
						//Chunk cnd1 = new Chunk(new VerticalPositionMark());
						Font redFont1 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
						PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
						Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
						Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
						Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
							
							document.open();
							PdfPTable table = new PdfPTable(2);

							Image img = Image.getInstance(fileResource.getFile().getAbsolutePath());
							img.scaleAbsolute(56, 87);
							table.setWidthPercentage(105);

							Phrase pq = new Phrase(new Chunk(img, 0, -70));

							
							pq.add(new Chunk(newAddress,redFont));
							PdfPCell cellp = new PdfPCell(pq);
							PdfPCell cell1 = new PdfPCell();

							
							// Display a date in day, month, year format
							Date dateInfo = Calendar.getInstance().getTime();
							DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");
							String today1 = formatter1.format(dateInfo).toString();
							

							

							Date date = Calendar.getInstance().getTime();
							DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
							String today = formatter.format(date).toString();


							Timestamp timestamp1 = ospinfo.get(0).getEnteredDate();
							DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");

							Calendar calendar1 = Calendar.getInstance();
							calendar1.setTimeInMillis(timestamp1.getTime());



							

							PdfPTable table96 = new PdfPTable(1);
							table96.setWidths(new float[] { 5f });
							table96.setSpacingBefore(10);

							PdfPCell hcell96;
							hcell96 = new PdfPCell(new Phrase("UDBHAVA HOSPITALS", headFont1));
							hcell96.setBorder(Rectangle.NO_BORDER);
							hcell96.setHorizontalAlignment(Element.ALIGN_CENTER);
							hcell96.setPaddingLeft(50f);

							table96.addCell(hcell96);
							cell1.addElement(table96);

							cell1.addElement(pq);
							cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
							table.addCell(cell1);

							PdfPCell cell3 = new PdfPCell();

							PdfPTable table99 = new PdfPTable(3);
							table99.setWidths(new float[] {3f,1f,4f});
							table99.setSpacingBefore(10);

							PdfPCell hcell90;
							hcell90 = new PdfPCell(new Phrase("Patient Name", redFont));
							hcell90.setBorder(Rectangle.NO_BORDER);
							hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell90.setPaddingBottom(-7f);
							hcell90.setPaddingLeft(-25f);
							table99.addCell(hcell90);
							
							hcell90 = new PdfPCell(new Phrase(":", redFont));
							hcell90.setBorder(Rectangle.NO_BORDER);
							hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell90.setPaddingBottom(-7f);
							hcell90.setPaddingLeft(-50f);
							table99.addCell(hcell90);
							
							hcell90 = new PdfPCell(new Phrase(ospinfo.get(0).getPatientName(), redFont));
							hcell90.setBorder(Rectangle.NO_BORDER);
							hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell90.setPaddingBottom(-7f);
							hcell90.setPaddingLeft(-50f);
							table99.addCell(hcell90);
							
							cell3.addElement(table99);

							PdfPTable table2 = new PdfPTable(6);
							table2.setWidths(new float[] {3f,1f,4f,3f,1f,4f });
							table2.setSpacingBefore(10);

							PdfPCell hcell1;
							hcell1 = new PdfPCell(new Phrase("Age/Gender", redFont));
							hcell1.setBorder(Rectangle.NO_BORDER);
							hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell1.setPaddingLeft(-25f);
							table2.addCell(hcell1);
							
							hcell1 = new PdfPCell(new Phrase(":", redFont));
							hcell1.setBorder(Rectangle.NO_BORDER);
							hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell1.setPaddingLeft(-20f);
							table2.addCell(hcell1);
							
							hcell1 = new PdfPCell(new Phrase(ospinfo.get(0).getAge()+"/"+ospinfo.get(0).getGender(), redFont));
							hcell1.setBorder(Rectangle.NO_BORDER);
							hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell1.setPaddingLeft(-30f);
							table2.addCell(hcell1);

							hcell1 = new PdfPCell(new Phrase("OSP No", redFont));
							hcell1.setBorder(Rectangle.NO_BORDER);
							hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell1.setPaddingRight(-40f);
							table2.addCell(hcell1);
				
							hcell1 = new PdfPCell(new Phrase(":", redFont));
							hcell1.setBorder(Rectangle.NO_BORDER);
							hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table2.addCell(hcell1);
				
							hcell1 = new PdfPCell(new Phrase(ospinfo.get(0).getOspServiceId(), redFont));
							hcell1.setBorder(Rectangle.NO_BORDER);
							hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell1.setPaddingRight(-30f);
							table2.addCell(hcell1);
						        
						        
							PdfPCell hcell4;
							hcell4 = new PdfPCell(new Phrase("Bill Dt" , redFont));
							hcell4.setBorder(Rectangle.NO_BORDER);
							hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell4.setPaddingLeft(-25f);
							table2.addCell(hcell4);
							
							hcell4 = new PdfPCell(new Phrase(":", redFont));
							hcell4.setBorder(Rectangle.NO_BORDER);
							hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell4.setPaddingLeft(-20f);
							table2.addCell(hcell4);
							
							hcell4 = new PdfPCell(new Phrase(today1, redFont));
							hcell4.setBorder(Rectangle.NO_BORDER);
							hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell4.setPaddingLeft(-30f);
							table2.addCell(hcell4);

							hcell4 = new PdfPCell(new Phrase("Phone", redFont));
							hcell4.setBorder(Rectangle.NO_BORDER);
							hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					        hcell4.setPaddingRight(-27.5f);
							table2.addCell(hcell4);
							
							hcell4 = new PdfPCell(new Phrase(":",redFont));
							hcell4.setBorder(Rectangle.NO_BORDER);
							hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
						    table2.addCell(hcell4);
						        
							hcell4 = new PdfPCell(new Phrase(String.valueOf(ospinfo.get(0).getMobile()), redFont));
							hcell4.setBorder(Rectangle.NO_BORDER);
							hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell4.setPaddingRight(-27.5f);
							table2.addCell(hcell4);
						        
						
							cell3.setFixedHeight(115f);
							cell3.setColspan(2);
							cell3.addElement(table2);

							PdfPTable table98 = new PdfPTable(3);
							table98.setWidths(new float[] { 3f,1f,4f });
							table98.setSpacingBefore(10);

							PdfPCell hcell91;
							hcell91 = new PdfPCell(new Phrase("Refer By", redFont));
							hcell91.setBorder(Rectangle.NO_BORDER);
							hcell91.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell91.setPaddingTop(-5f);
							hcell91.setPaddingLeft(-25f);
							table98.addCell(hcell91);
							
							
							hcell91 = new PdfPCell(new Phrase(":", redFont));
							hcell91.setBorder(Rectangle.NO_BORDER);
							hcell91.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell91.setPaddingTop(-5f);
							hcell91.setPaddingLeft(-61f);
							table98.addCell(hcell91);
							
							
							hcell91 = new PdfPCell(new Phrase(docName, redFont));
							hcell91.setBorder(Rectangle.NO_BORDER);
							hcell91.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell91.setPaddingTop(-5f);
							hcell91.setPaddingLeft(-85f);
							table98.addCell(hcell91);
							
							cell3.addElement(table98);

							
							table.addCell(cell3);
							PdfPCell cell19 = new PdfPCell();

							PdfPTable table21 = new PdfPTable(1);
							table21.setWidths(new float[] { 4f });
							table21.setSpacingBefore(10);

							PdfPCell hcell19;
							hcell19 = new PdfPCell(new Phrase("OSP Due BILL CUM RECEIPT", headFont1));
							hcell19.setBorder(Rectangle.NO_BORDER);
							hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
							table21.addCell(hcell19);

							cell19.setFixedHeight(20f);
							cell19.setColspan(2);
							cell19.addElement(table21);
							table.addCell(cell19);

							PdfPCell cell31 = new PdfPCell();

							PdfPTable table1 = new PdfPTable(8);
							table1.setWidths(new float[] { 1f, 3f, 4f,3f, 3f, 2.5f, 2f,2.5f });

							table1.setSpacingBefore(10);

							PdfPCell hcell;
							hcell = new PdfPCell(new Phrase("S.No", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.LIGHT_GRAY);
							hcell.setPaddingBottom(5f);
							hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("Service Code", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.LIGHT_GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("Service Name", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.LIGHT_GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("Service Type", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.LIGHT_GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("Qty", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.LIGHT_GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
							hcell.setPaddingRight(30f);
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("Rate", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.LIGHT_GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell.setPaddingRight(35f);
							table1.addCell(hcell);
							
							hcell = new PdfPCell(new Phrase("Disc", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.LIGHT_GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell.setPaddingRight(30f);
							table1.addCell(hcell);
							
							hcell = new PdfPCell(new Phrase("Amount(RS)", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.LIGHT_GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table1.addCell(hcell);

							int count = 0;
							long total=0;
							String serviceId = null;
							String serviceName = null;
							String serviceType = null;
							float totalAmt = 0;
							String chargeBillId=null;
							String insertDt = null;
							String cRegId=null;
							String salesDate=null;
							
							
							for (OspService ospServicesInfo:ospinfo) {
								
							
								PdfPCell cell;

								cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFont));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								table1.addCell(cell);

								cell = new PdfPCell(new Phrase(ospServicesInfo.getOspLabServices().getServiceId(), redFont));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setPaddingLeft(5);
								cell.setHorizontalAlignment(Element.ALIGN_LEFT);
								table1.addCell(cell);	

								cell = new PdfPCell(new Phrase(ospServicesInfo.getServiceName(), redFont));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setPaddingLeft(5);
								cell.setHorizontalAlignment(Element.ALIGN_LEFT);
								table1.addCell(cell);

								cell = new PdfPCell(new Phrase(ospServicesInfo.getOspLabServices().getServiceType(), redFont));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setPaddingLeft(5);
								cell.setHorizontalAlignment(Element.ALIGN_LEFT);
								table1.addCell(cell);

								cell = new PdfPCell(new Phrase(String.valueOf(ospServicesInfo.getQuantity()), redFont));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell.setPaddingRight(30);
								table1.addCell(cell);

								cell = new PdfPCell(new Phrase(String.valueOf(ospServicesInfo.getPrice()), redFont));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell.setPaddingRight(35);
								table1.addCell(cell);
								
								cell = new PdfPCell(new Phrase(String.valueOf(ospServicesInfo.getDiscount()), redFont));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell.setPaddingRight(30);
								table1.addCell(cell);

								
								cell = new PdfPCell(new Phrase(String.valueOf(ospServicesInfo.getNetAmount()), redFont));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setPaddingLeft(5);
								cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
								table1.addCell(cell);
								
						       total+=ospServicesInfo.getNetAmount();

							}
							

							// -------------------------------

							
							PdfPTable table37 = new PdfPTable(6);
							table37.setWidths(new float[] {3f,1f, 4f,7f,1f, 4f });
							table37.setSpacingBefore(10);

							PdfPCell cell55;
							cell55 = new PdfPCell(new Phrase("", redFont));
							cell55.setBorder(Rectangle.NO_BORDER);
							cell55.setHorizontalAlignment(Element.ALIGN_LEFT);
							cell55.setPaddingTop(10f);
							table37.addCell(cell55);
							
							cell55 = new PdfPCell(new Phrase("", redFont));
							cell55.setBorder(Rectangle.NO_BORDER);
							cell55.setHorizontalAlignment(Element.ALIGN_LEFT);
							cell55.setPaddingTop(10f);
							table37.addCell(cell55);
							
							cell55 = new PdfPCell(new Phrase("", redFont));
							cell55.setBorder(Rectangle.NO_BORDER);
							cell55.setHorizontalAlignment(Element.ALIGN_LEFT);
							cell55.setPaddingTop(10f);
							table37.addCell(cell55);

							cell55 = new PdfPCell(new Phrase("Gross Amt", redFont));
							cell55.setBorder(Rectangle.NO_BORDER);
							cell55.setPaddingTop(10f);
							cell55.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell55.setPaddingRight(-70f);
							table37.addCell(cell55);
							
							cell55 = new PdfPCell(new Phrase(":", redFont));
							cell55.setBorder(Rectangle.NO_BORDER);
							cell55.setPaddingTop(10f);
							cell55.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell55.setPaddingRight(-60f);
							table37.addCell(cell55);
							
							cell55 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
							cell55.setBorder(Rectangle.NO_BORDER);
							cell55.setPaddingTop(10f);
							cell55.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell55.setPaddingRight(-50f);
							table37.addCell(cell55);
							
							
							PdfPCell hcell56;
							hcell56 = new PdfPCell(new Phrase("", redFont));
							hcell56.setBorder(Rectangle.NO_BORDER);
							hcell56.setPaddingLeft(-1f);
							hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table37.addCell(hcell56);
							
							hcell56 = new PdfPCell(new Phrase("", redFont));
							hcell56.setBorder(Rectangle.NO_BORDER);
							hcell56.setPaddingLeft(-1f);
							hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table37.addCell(hcell56);
							
							hcell56 = new PdfPCell(new Phrase("", redFont));
							hcell56.setBorder(Rectangle.NO_BORDER);
							hcell56.setPaddingLeft(-1f);
							hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table37.addCell(hcell56);


							hcell56 = new PdfPCell(new Phrase("Paid Amt.", redFont));
							hcell56.setBorder(Rectangle.NO_BORDER);
							hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell56.setPaddingRight(-70f);
							table37.addCell(hcell56);
							
							hcell56 = new PdfPCell(new Phrase(":", redFont));
							hcell56.setBorder(Rectangle.NO_BORDER);
							hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell56.setPaddingRight(-60f);
							table37.addCell(hcell56);
							
							hcell56 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
							hcell56.setBorder(Rectangle.NO_BORDER);
							hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell56.setPaddingRight(-50f);
							table37.addCell(hcell56);

							PdfPCell hcell57;
							hcell57 = new PdfPCell(new Phrase(ospinfo.get(0).getPaymentType()+" Amt.", redFont));
							hcell57.setBorder(Rectangle.NO_BORDER);
							hcell57.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell57.setPaddingLeft(-50f);
							table37.addCell(hcell57);

							hcell57 = new PdfPCell(new Phrase(":", redFont));
							hcell57.setBorder(Rectangle.NO_BORDER);
							hcell57.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell57.setPaddingLeft(-50f);
							table37.addCell(hcell57);

							hcell57 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
							hcell57.setBorder(Rectangle.NO_BORDER);
							hcell57.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell57.setPaddingLeft(-60f);
							table37.addCell(hcell57);

							hcell57 = new PdfPCell(new Phrase("Net Amt.", redFont));
							hcell57.setBorder(Rectangle.NO_BORDER);
							hcell57.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell57.setPaddingRight(-70f);
							table37.addCell(hcell57);
							
							hcell57 = new PdfPCell(new Phrase(":", redFont));
							hcell57.setBorder(Rectangle.NO_BORDER);
							hcell57.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell57.setPaddingRight(-60f);
							table37.addCell(hcell57);
							
							hcell57 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
							hcell57.setBorder(Rectangle.NO_BORDER);
							hcell57.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell57.setPaddingRight(-50f);
							table37.addCell(hcell57);

							PdfPCell hcell58;
							hcell58 = new PdfPCell(new Phrase(""));
							hcell58.setBorder(Rectangle.NO_BORDER);
							table37.addCell(hcell58);
							
							hcell58 = new PdfPCell(new Phrase(""));
							hcell58.setBorder(Rectangle.NO_BORDER);
							table37.addCell(hcell58);
							
							hcell58 = new PdfPCell(new Phrase(""));
							hcell58.setBorder(Rectangle.NO_BORDER);
							table37.addCell(hcell58);

							hcell58 = new PdfPCell(new Phrase("Received Amt.", redFont));
							hcell58.setBorder(Rectangle.NO_BORDER);
							hcell58.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell58.setPaddingRight(-70f);
							table37.addCell(hcell58);
							
							hcell58 = new PdfPCell(new Phrase(":", redFont));
							hcell58.setBorder(Rectangle.NO_BORDER);
							hcell58.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell58.setPaddingRight(-60f);
							table37.addCell(hcell58);
							
							hcell58 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
							hcell58.setBorder(Rectangle.NO_BORDER);
							hcell58.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell58.setPaddingRight(-50f);
							table37.addCell(hcell58);

							
							PdfPTable table371 = new PdfPTable(3);
							table371.setWidths(new float[] {7f,1f, 8f });
							table371.setSpacingBefore(10);
							
							PdfPCell hcell59;
							hcell59 = new PdfPCell(new Phrase("Gross Amount In Words ", redFont));
							hcell59.setBorder(Rectangle.NO_BORDER);
							hcell59.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell59.setPaddingLeft(-50f);
							table371.addCell(hcell59);

							hcell59 = new PdfPCell(new Phrase("", headFont));
							hcell59.setBorder(Rectangle.NO_BORDER);
							hcell59.setHorizontalAlignment(Element.ALIGN_LEFT);
							table371.addCell(hcell59);
							
							hcell59 = new PdfPCell(new Phrase("(" + numberToWordsConverter.convert(total) + ")", redFont));
							hcell59.setBorder(Rectangle.NO_BORDER);
							hcell59.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell59.setPaddingLeft(-150f);
							table371.addCell(hcell59);
							

							PdfPCell hcell60;
							hcell60 = new PdfPCell(new Phrase(
									"Received Amount In Words ", redFont));

							hcell60.setBorder(Rectangle.NO_BORDER);
							hcell60.setPaddingLeft(-50f);
							hcell60.setHorizontalAlignment(Element.ALIGN_LEFT);
							table371.addCell(hcell60);

							hcell60 = new PdfPCell(new Phrase("", headFont));
							hcell60.setBorder(Rectangle.NO_BORDER);
							hcell60.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table371.addCell(hcell60);
							
							hcell60 = new PdfPCell(new Phrase("(" + numberToWordsConverter.convert(total) + ")", redFont));
							hcell60.setBorder(Rectangle.NO_BORDER);
							hcell60.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell60.setPaddingLeft(-145f);
							table371.addCell(hcell60);
							
							

							cell31.setColspan(2);
							table1.setWidthPercentage(100f);
							cell31.addElement(table1);
							cell31.addElement(table37);
							cell31.addElement(table371);
							table.addCell(cell31);


							PdfPCell cell5 = new PdfPCell();

							PdfPTable table35 = new PdfPTable(2);
							table35.setWidths(new float[] { 5f, 4f });
							table35.setSpacingBefore(10);

							PdfPCell hcell12;
							hcell12 = new PdfPCell(new Phrase("Created By    : "+createdBy, redFont));
							hcell12.setBorder(Rectangle.NO_BORDER);
							hcell12.setPaddingTop(10f);
							hcell12.setPaddingLeft(-50f);
							table35.addCell(hcell12);

							hcell12 = new PdfPCell(new Phrase("Created Dt      :   " + today, redFont));
							hcell12.setBorder(Rectangle.NO_BORDER);
							hcell12.setPaddingTop(10f);
							hcell12.setHorizontalAlignment(Element.ALIGN_LEFT);
							table35.addCell(hcell12);

							PdfPCell hcell13;
							hcell13 = new PdfPCell(new Phrase("Printed By     : "+createdBy, redFont));
							hcell13.setBorder(Rectangle.NO_BORDER);
							hcell13.setPaddingLeft(-50f);
							table35.addCell(hcell13);

							hcell13 = new PdfPCell(new Phrase("Printed Dt       :   " + today, redFont));
							hcell13.setBorder(Rectangle.NO_BORDER);
							//hcell13.setPaddingRight(3f);
							hcell13.setHorizontalAlignment(Element.ALIGN_LEFT);
							table35.addCell(hcell13);

							PdfPCell hcell23;
							hcell23 = new PdfPCell(new Phrase(""));
							hcell23.setBorder(Rectangle.NO_BORDER);
							table35.addCell(hcell23);

							hcell23 = new PdfPCell(new Phrase("(Authorized Signature)", headFont));
							hcell23.setBorder(Rectangle.NO_BORDER);
							hcell23.setPaddingTop(22f);
							hcell23.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table35.addCell(hcell23);

							cell5.setFixedHeight(90f);
							cell5.setColspan(2);
							cell5.addElement(table35);
							table.addCell(cell5);

							document.add(table);

							document.close();							
								
							System.out.println("finished");
							pdfByte = byteArrayOutputStream.toByteArray();
							String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
									.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();

							salesPaymentPdf = new SalesPaymentPdf();
							salesPaymentPdf.setFileName(ospinfo.get(0).getOspServiceId()+" Osp Service Bill");
							salesPaymentPdf.setFileuri(uri);
							salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
							salesPaymentPdf.setData(pdfByte);
							System.out.println(salesPaymentPdf);
							salesPaymentPdfServiceImpl.save(salesPaymentPdf);
						}catch (Exception e) 
						{
							e.printStackTrace();
						}

				
				}

				
		

		return salesPaymentPdf;

	}

}

	
	

	



