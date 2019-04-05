package com.vncdigital.vpulse.osp.serviceImpl;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
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
import com.vncdigital.vpulse.finalBilling.model.FinalBilling;
import com.vncdigital.vpulse.finalBilling.repository.FinalBillingRepository;
import com.vncdigital.vpulse.finalBilling.serviceImpl.FinalBillingServiceImpl;
import com.vncdigital.vpulse.laboratory.helper.RefLaboratoryRegistration;
import com.vncdigital.vpulse.laboratory.model.LabServices;
import com.vncdigital.vpulse.laboratory.serviceImpl.LabServicesServiceImpl;
import com.vncdigital.vpulse.laboratory.serviceImpl.PatientServiceDetailsServiceImpl;
import com.vncdigital.vpulse.osp.model.OspService;
import com.vncdigital.vpulse.osp.repository.OspServiceRepository;
import com.vncdigital.vpulse.osp.service.OspServiceService;
import com.vncdigital.vpulse.patient.model.CashPlusCard;
import com.vncdigital.vpulse.patient.serviceImpl.CashPlusCardServiceImpl;
import com.vncdigital.vpulse.pharmacist.model.SalesPaymentPdf;
import com.vncdigital.vpulse.pharmacist.serviceImpl.SalesPaymentPdfServiceImpl;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;

@Service
public class OspServiceServiceImpl implements OspServiceService {
	
	public static Logger Logger=LoggerFactory.getLogger(OspServiceServiceImpl.class);
	
	@Autowired
	NumberToWordsConverter numberToWordsConverter;

	@Autowired
	CashPlusCardServiceImpl cashPlusCardServiceImpl;
	
	@Autowired
	SalesPaymentPdfServiceImpl salesPaymentPdfServiceImpl;
	
	@Autowired
	OspServiceRepository ospServiceRepository;
	
	@Autowired
	UserServiceImpl userServiceImpl;
	
	@Autowired
	LabServicesServiceImpl labServicesServiceImpl;
	
	@Autowired
	FinalBillingRepository finalBillingRepository;
	
	
	@Autowired
	FinalBillingServiceImpl finalBillingServcieImpl;
	

	@Autowired
    ResourceLoader resourceLoader;
	
	
	
	public String getNextBillNo()
	{
		OspService ospService=ospServiceRepository.findFirstByOrderByMasterOspServiceIdDesc();
		
		String nextId=null;
		if(ospService==null)
		{
			nextId="BL00000001";
			
		}
		else
		{
			int nextIntId=Integer.parseInt(ospService.getBillNo().substring(2));
			nextIntId+=1;
			nextId="BL"+String.format("%08d", nextIntId);
			
		}
		return nextId;
	}

		
	public String getNextMasterOpsId()
	{
		OspService ospService=ospServiceRepository.findFirstByOrderByMasterOspServiceIdDesc();
		String nextId=null;
		if(ospService==null)
		{
			nextId="MOSP0000001";
		}
		else
		{
			int nextIntId=Integer.parseInt(ospService.getMasterOspServiceId().substring(4));
			nextIntId+=1;
			nextId="MOSP"+String.format("%07d", nextIntId);
		}
		return nextId;
	}
	
	public String getNextOspId()
	{
		OspService ospService=ospServiceRepository.findFirstByOrderByMasterOspServiceIdDesc();
		String nextId=null;
		if(ospService==null)
		{
			nextId="OSP0000001";
		}
		else
		{
			int nextIntId=Integer.parseInt(ospService.getOspServiceId().substring(3));
			nextIntId+=1;
			nextId="OSP"+String.format("%07d", nextIntId);
		}
		return nextId;
	}

	@Override
	public List<Object> pageRefrersh() {
		List<Object> list=new ArrayList<>();
		
		Map<String, String> map=new HashMap<>();
		map.put("ospId",getNextOspId());
		List<LabServices> labServices=labServicesServiceImpl.getOspServices("OSP");
		List<User> user=userServiceImpl.findByRole("DOCTOR");
		
		
		list.add(map);
		list.add(labServices);
		list.add(user);
		return list;
	}

	

	public Map<String, String> getOspServiceCost(String name,String pType)
	{
		Map<String,String> displayInfo=new HashMap<>();
	
		LabServices labServices=labServicesServiceImpl.findPriceByType(name,"OSP","NA");
		System.out.println(String.valueOf(labServices.getCost()));
		displayInfo.put("cost", String.valueOf(labServices.getCost()));
		displayInfo.put("serviceName", labServices.getServiceName());
		return displayInfo;
	}
	
	

	@Override
	public SalesPaymentPdf chargeForOspService(OspService ospService,Principal principal){
		
		if(ospService.getPaymentType()==null)
		{
			throw new RuntimeException("Plz Enter Payment Type");
		}
		
		SalesPaymentPdf salesPaymentPdf=null;
		//createdBy Security
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		String createdBy=userSecurity.getFirstName()+" "+userSecurity.getLastName();
		
		List<RefLaboratoryRegistration> refLaboratoryRegistrations=ospService.getRefLaboratoryRegistrations();
		ospService.setOspServiceId(getNextOspId());
		ospService.setBillNo(getNextBillNo());
		ospService.setUpdatedBy(userSecurity.getUserId());
		ospService.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
		String paymentMode=ospService.getPaymentType();
		

		
		  if(paymentMode.equalsIgnoreCase("Card")||paymentMode.
		  equalsIgnoreCase("Credit Card")||
		  paymentMode.equalsIgnoreCase("Debit Card")||paymentMode.equalsIgnoreCase(
		  "Cash+Card")) { ospService.setReferenceNumber(ospService.getReferenceNumber());
		  
		  }
		
		if(ospService.getPaymentType().equalsIgnoreCase("Due")){
			
			ospService.setPaid("NO");
			
		}else{
			ospService.setPaid("YES");
		}
		
		
		if(paymentMode.equalsIgnoreCase("Cash+Card"))
		{
			int cashAmount=0;
			int cardAmount=0;
			int chequeAmount=0;
			CashPlusCard cashPlusCardLab=new CashPlusCard();
			cashPlusCardLab.setInsertedBy(userSecurity.getUserId());
			cashPlusCardLab.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			List<Map<String,String>> multiMode=ospService.getMultimode();
			for(Map<String,String> multiModeInfo:multiMode)
			{
				if(multiModeInfo.get("mode").equalsIgnoreCase("Cash"))
				{
					cashAmount=Integer.parseInt(multiModeInfo.get("amount"));
				}
				else if(multiModeInfo.get("mode").equalsIgnoreCase("Card"))
				{
					cardAmount=Integer.parseInt(multiModeInfo.get("amount"));
				}
				else if(multiModeInfo.get("mode").equalsIgnoreCase("Cheque"))
				{
					chequeAmount=Integer.parseInt(multiModeInfo.get("amount"));
				}
				
			}
			cashPlusCardLab.setBillNo(ospService.getBillNo());
			cashPlusCardLab.setDescription("OspBill");
			cashPlusCardLab.setCardAmount(cardAmount);
			cashPlusCardLab.setCashAmount(cashAmount);
			cashPlusCardLab.setChequeAmount(chequeAmount);
			cashPlusCardServiceImpl.save(cashPlusCardLab);
			
			
			
		}
	
		
		
		
		
		
		String consultant=ospService.getRefferedById();
		String[] docNameList=consultant.split("-");
		
		String docId=docNameList[1];
		
		String docName=docNameList[0];
		
		ospService.setRefferedById(docId);
		
		
		
		LocalDate todayLocal = LocalDate.now();
		LocalDate birthday = ospService.getDob().toLocalDateTime().toLocalDate();
		Period p = Period.between(birthday, todayLocal);
		System.out.println("Days"+p.getDays()+"months"+p.getMonths()+"years"+p.getYears());
		int accurate_age=0;
		
		
		if(p.getMonths()>=5)
		{
			accurate_age=p.getYears()+1;
		}
		else
		{
			accurate_age=p.getYears();
		}
		if(p.getYears()==0)
		{
			if(p.getDays()==0)
			{
				String lessThanOne=String.valueOf(p.getYears()+"Y "+p.getMonths()+"M "+String.valueOf(1)+"D");
				ospService.setAge(String.valueOf(lessThanOne));
			}
			else
			{
				int days=p.getDays()+1;
			String lessThanOne=String.valueOf(p.getYears()+"Y "+p.getMonths())+"M "+String.valueOf(days+"D");
			ospService.setAge(String.valueOf(lessThanOne));
			}
		}
		else
		{
			int days=0;
			if(p.getMonths()==0)
			{
			 days=p.getDays()+1;
			 ospService.setAge(String.valueOf(String.valueOf(accurate_age+"Y "+p.getMonths())+"M "+days+"D"));
			}
			else
			{
				days=p.getDays()+1;
				ospService.setAge(String.valueOf(String.valueOf(accurate_age-1+"Y "+p.getMonths())+"M "+days+"D"));
				
			}
		}
		
		
		
		
		for(RefLaboratoryRegistration refLaboratoryRegistrationsInfo:refLaboratoryRegistrations){
			
			LabServices labServices=labServicesServiceImpl.findPriceByType(refLaboratoryRegistrationsInfo.getServiceName(), "OSP","NA");
			ospService.setMasterOspServiceId(getNextMasterOpsId());
			ospService.setServiceName(refLaboratoryRegistrationsInfo.getServiceName());
			ospService.setPrice(refLaboratoryRegistrationsInfo.getPrice());
			ospService.setDiscount(refLaboratoryRegistrationsInfo.getDiscount());
			ospService.setNetAmount(refLaboratoryRegistrationsInfo.getAmount());
			ospService.setQuantity(refLaboratoryRegistrationsInfo.getQuantity());
			ospService.setStatus("Not-Completed");
			Timestamp timestamp=new Timestamp(System.currentTimeMillis());
			ospService.setEnteredDate(timestamp);
			ospService.setOspLabServices(labServices);
			ospService.setUserOspService(userSecurity);
			ospServiceRepository.save(ospService);
			
			
		}
		CashPlusCard cashPlusCard=cashPlusCardServiceImpl.findByBillNo(ospService.getBillNo());
		List<OspService> ospServices=ospServiceRepository.findServices(ospService.getOspServiceId());
		
		
	
		
		//shantharam addr

		String newAddress="";
		
					
		
					
			
				Date date = Calendar.getInstance().getTime();
				DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
				String today = formatter.format(date).toString();


				Timestamp timestamp1 = ospService.getEnteredDate();
				DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");

				Calendar calendar1 = Calendar.getInstance();
				calendar1.setTimeInMillis(timestamp1.getTime());

				String to = dateFormat1.format(calendar1.getTime());

				//for department
				String dpt=null;
/*
				if(patientRegistration.getVuserD().getDepartment()!=null)
				{
					dpt=patientRegistration.getVuserD().getDepartment();
					
				}
				else
				{
					dpt="";
				}
				*/
				
				
					if(!ospService.getPaymentType().equalsIgnoreCase("Due")) {
					byte[] pdfByte=null;
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

					Document document = new Document(PageSize.A4_LANDSCAPE);
				
								
				try {

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
						
						hcell90 = new PdfPCell(new Phrase(ospService.getPatientName(), redFont));
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
						
						hcell1 = new PdfPCell(new Phrase(ospService.getAge()+"/"+ospService.getGender(), redFont));
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
			
						hcell1 = new PdfPCell(new Phrase(ospService.getOspServiceId(), redFont));
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
					        
						hcell4 = new PdfPCell(new Phrase(String.valueOf(ospService.getMobile()), redFont));
						hcell4.setBorder(Rectangle.NO_BORDER);
						hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell4.setPaddingRight(-27.5f);
						table2.addCell(hcell4);
					        
						/*PdfPCell hcell15;
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
						
						hcell15 = new PdfPCell(new Phrase("refby", redFont));
						hcell15.setBorder(Rectangle.NO_BORDER);
						hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell15.setPaddingLeft(-30f);
						table2.addCell(hcell15);


						hcell15 = new PdfPCell(new Phrase("Phone", redFont));
						hcell15.setBorder(Rectangle.NO_BORDER);
						hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell15.setPaddingRight(-27.5f);
						table2.addCell(hcell15);
						
						hcell15 = new PdfPCell(new Phrase(":",redFont));
						hcell15.setBorder(Rectangle.NO_BORDER);
						hcell15.setHorizontalAlignment(Element.ALIGN_RIGHT);
					    table2.addCell(hcell15);
					        
						hcell15 = new PdfPCell(new Phrase(String.valueOf(ospService.getMobile()), redFont));
						hcell15.setBorder(Rectangle.NO_BORDER);
						hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell15.setPaddingRight(-27.5f);
						table2.addCell(hcell15);
*/
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
						hcell19 = new PdfPCell(new Phrase("OSP BILL CUM RECEIPT", headFont1));
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
						
						
					//List<OspService> ospServices=ospServiceRepository.findServices(ospService.getOspServiceId());
				
					//String chargeName=null;
						
						for (OspService ospServicesInfo:ospServices) {
							
							//chargeName=refBillDetailsInfo.getChargeName();
							
							/*List<LabServices> labServices =labServicesServiceImpl.findByServiceName(chargeName);
							for(LabServices lab:labServices)
							{
							serviceId = lab.getServiceId();
							serviceType = lab.getServiceType();
							}*/
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
						hcell57 = new PdfPCell(new Phrase(ospService.getPaymentType()+" Amt.", redFont));
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
						salesPaymentPdf.setFileName(ospService.getOspServiceId()+" Osp Service Bill");
						salesPaymentPdf.setFileuri(uri);
						salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
						salesPaymentPdf.setData(pdfByte);
						System.out.println(salesPaymentPdf);
						salesPaymentPdfServiceImpl.save(salesPaymentPdf);
					}catch (Exception e) 
					{
						e.printStackTrace();
					}
				
					}else if(ospService.getPaymentType().equalsIgnoreCase("Due")) {
						byte[] pdfByte=null;
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

						Document document = new Document(PageSize.A4_LANDSCAPE);
					
									
					try {

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
							
							hcell90 = new PdfPCell(new Phrase(ospService.getPatientName(), redFont));
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
							
							hcell1 = new PdfPCell(new Phrase(ospService.getAge()+"/"+ospService.getGender(), redFont));
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
				
							hcell1 = new PdfPCell(new Phrase(ospService.getOspServiceId(), redFont));
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
						        
							hcell4 = new PdfPCell(new Phrase(String.valueOf(ospService.getMobile()), redFont));
							hcell4.setBorder(Rectangle.NO_BORDER);
							hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell4.setPaddingRight(-27.5f);
							table2.addCell(hcell4);
						        
							/*PdfPCell hcell15;
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
							
							hcell15 = new PdfPCell(new Phrase("refby", redFont));
							hcell15.setBorder(Rectangle.NO_BORDER);
							hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell15.setPaddingLeft(-30f);
							table2.addCell(hcell15);


							hcell15 = new PdfPCell(new Phrase("Phone", redFont));
							hcell15.setBorder(Rectangle.NO_BORDER);
							hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell15.setPaddingRight(-27.5f);
							table2.addCell(hcell15);
							
							hcell15 = new PdfPCell(new Phrase(":",redFont));
							hcell15.setBorder(Rectangle.NO_BORDER);
							hcell15.setHorizontalAlignment(Element.ALIGN_RIGHT);
						    table2.addCell(hcell15);
						        
							hcell15 = new PdfPCell(new Phrase(String.valueOf(ospService.getMobile()), redFont));
							hcell15.setBorder(Rectangle.NO_BORDER);
							hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell15.setPaddingRight(-27.5f);
							table2.addCell(hcell15);
	*/
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
							hcell19 = new PdfPCell(new Phrase("OSP BILL CUM RECEIPT", headFont1));
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
							
							
						//List<OspService> ospServices=ospServiceRepository.findServices(ospService.getOspServiceId());
					
						//String chargeName=null;
							
							for (OspService ospServicesInfo:ospServices) {
								
								//chargeName=refBillDetailsInfo.getChargeName();
								
								/*List<LabServices> labServices =labServicesServiceImpl.findByServiceName(chargeName);
								for(LabServices lab:labServices)
								{
								serviceId = lab.getServiceId();
								serviceType = lab.getServiceType();
								}*/
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
							
							hcell56 = new PdfPCell(new Phrase(String.valueOf(0), redFont));
							hcell56.setBorder(Rectangle.NO_BORDER);
							hcell56.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell56.setPaddingRight(-50f);
							table37.addCell(hcell56);

							PdfPCell hcell57;
							hcell57 = new PdfPCell(new Phrase(ospService.getPaymentType()+" Amt.", redFont));
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

							hcell58 = new PdfPCell(new Phrase("Due Amt.", redFont));
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
									"Due Amount In Words ", redFont));

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
							salesPaymentPdf.setFileName(ospService.getOspServiceId()+" Osp Service Bill");
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

	@Override
	public List<Object> findAll() {
		List<Object> list=new ArrayList<>();
		List<OspService> ospServices=ospServiceRepository.findByOrderByMasterOspServiceIdDesc();
		List<String> ospList=new ArrayList<>();
		
		
		
		String userName = null;
		String ufn = null;
		String umn = null;
		String uln = null;

		for(OspService ospServicesInfo:ospServices ){
			
			String ospServiceId=ospServicesInfo.getOspServiceId();
			
			if(!ospList.contains(ospServiceId)){
				

				String from = ospServicesInfo.getEnteredDate().toString();
				Timestamp timestamp = Timestamp.valueOf(from);
				DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(timestamp.getTime());
				String from1 = dateFormat.format(calendar.getTime());
				
				
				
				String DOB = ospServicesInfo.getDob().toString();
				Timestamp timestamp1 = Timestamp.valueOf(DOB);
				DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy hh.mm aa ");
				Calendar calendar1 = Calendar.getInstance();
				calendar1.setTimeInMillis(timestamp1.getTime());
				String DOB1 = dateFormat1.format(calendar1.getTime());
				
				
				User userInfo=userServiceImpl.findOneByUserId(ospServicesInfo.getRefferedById());
				
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

				
                 String date=from1.substring(0, 11);
				
				Map<String, String> map=new HashMap<>();
				map.put("name",ospServicesInfo.getPatientName() );
				map.put("ospServiceId",ospServicesInfo.getOspServiceId() );
				map.put("date", from1);
				map.put("dob", DOB1.substring(0, 10));
				map.put("mobileNo", String.valueOf(ospServicesInfo.getMobile()));
				map.put("gender", ospServicesInfo.getGender());
				map.put("refDoctor", userName);
				list.add(map);
				ospList.add(ospServiceId);
			}
			
			
		}
		
		return list;
	}
	
	
	public List<Map<String, String>> ospDetails(String type) {
		List<Map<String,String>> list=new ArrayList<>();
		String regDate=null;
		String inpatient=null;
		String outpatient=null;
		String docName="";
		String twodayback="";
		
		String today= new Timestamp(System.currentTimeMillis()).toString().substring(0, 10);
		String nextDay=LocalDate.parse(today).plusDays(1).toString();
		if(type.equalsIgnoreCase("2"))
		{
		twodayback=LocalDate.parse(today).plusDays(-2).toString();
		}
		else if(type.equalsIgnoreCase("7"))
		{
		twodayback=LocalDate.parse(today).plusDays(-7).toString();		
		}
		else if(type.equalsIgnoreCase("15"))
		{
		twodayback=LocalDate.parse(today).plusDays(-15).toString();		
		}
		else if(type.equalsIgnoreCase("30"))
		{
		twodayback=LocalDate.parse(today).plusDays(-30).toString();		
		}
		

          List<String> ospList=new ArrayList<>();
		
	
		String userName = null;
		String ufn = null;
		String umn = null;
		String uln = null;
		
		List<OspService> osp=  ospTwoDays(twodayback,nextDay);
for(OspService ospServicesInfo: osp){
			
			String ospServiceId=ospServicesInfo.getOspServiceId();
			
			if(!ospList.contains(ospServiceId)){
				

				String from = ospServicesInfo.getEnteredDate().toString();
				Timestamp timestamp = Timestamp.valueOf(from);
				DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(timestamp.getTime());
				String from1 = dateFormat.format(calendar.getTime());
				
				User userInfo=userServiceImpl.findOneByUserId(ospServicesInfo.getRefferedById());
				
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

				
                 String date=from1.substring(0, 11);
				
				Map<String, String> map=new HashMap<>();
				map.put("name",ospServicesInfo.getPatientName() );
				map.put("ospServiceId",ospServicesInfo.getOspServiceId() );
				map.put("date", from1);
				map.put("dob", String.valueOf(ospServicesInfo.getDob()));
				map.put("mobileNo", String.valueOf(ospServicesInfo.getMobile()));
				map.put("gender", ospServicesInfo.getGender());
				map.put("refDoctor", userName);
				list.add(map);
				ospList.add(ospServiceId);
			}
			
			
		}
		
		return list;
}
	
	public List<OspService> ospTwoDays(String twoDayBack, String today) {
		return  ospServiceRepository.ospTwoDays(twoDayBack, today);
	}
}