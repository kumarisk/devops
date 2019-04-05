package com.vncdigital.vpulse.pharmacist.serviceImpl;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.vncdigital.vpulse.bill.model.ChargeBill;
import com.vncdigital.vpulse.bill.repository.ChargeBillRepository;
import com.vncdigital.vpulse.bill.serviceImpl.ChargeBillServiceImpl;
import com.vncdigital.vpulse.config.ConstantValues;
import com.vncdigital.vpulse.finalBilling.model.FinalBilling;
import com.vncdigital.vpulse.finalBilling.serviceImpl.FinalBillingServiceImpl;
import com.vncdigital.vpulse.patient.model.CashPlusCard;
import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.serviceImpl.CashPlusCardServiceImpl;
import com.vncdigital.vpulse.patient.serviceImpl.PatientRegistrationServiceImpl;
import com.vncdigital.vpulse.pharmacist.helper.RefSales;
import com.vncdigital.vpulse.pharmacist.helper.RefSalesIds;
import com.vncdigital.vpulse.pharmacist.model.Location;
import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.MedicineProcurement;
import com.vncdigital.vpulse.pharmacist.model.MedicineQuantity;
import com.vncdigital.vpulse.pharmacist.model.PatientSales;
import com.vncdigital.vpulse.pharmacist.model.Sales;
import com.vncdigital.vpulse.pharmacist.model.SalesPaymentPdf;
import com.vncdigital.vpulse.pharmacist.model.SalesReturn;
import com.vncdigital.vpulse.pharmacist.repository.MedicineDetailsRepository;
import com.vncdigital.vpulse.pharmacist.repository.MedicineProcurementRepository;
import com.vncdigital.vpulse.pharmacist.repository.MedicineQuantityRepository;
import com.vncdigital.vpulse.pharmacist.repository.PatientSalesRepository;
import com.vncdigital.vpulse.pharmacist.repository.SalesRepository;
import com.vncdigital.vpulse.pharmacist.repository.SalesReturnRepository;
import com.vncdigital.vpulse.pharmacist.service.SalesService;
import com.vncdigital.vpulse.pharmacyShopDetails.model.PharmacyShopDetails;
import com.vncdigital.vpulse.pharmacyShopDetails.repository.PharmacyShopDetailsRepository;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;
@Service
public class SalesServiceImpl implements SalesService 
{
	
	private static final Logger Logger=LoggerFactory.getLogger(SalesServiceImpl.class);
	
	@Autowired
	SalesRepository salesRepository;
	
	@Autowired
	SalesReturnRepository salesReturnRepository;
	
	@Autowired
	NumberToWordsConverter numberToWordsConverter;
	
	@Autowired
	RefSalesIds refSalesIds;
	
	@Autowired
	UserServiceImpl userServiceImpl;

	
	@Autowired
	MedicineDetailsRepository medicineDetailsRepository;
	
	@Autowired
	FinalBillingServiceImpl finalBillingServcieImpl;
	
	
	@Autowired
	MedicineProcurementServiceImpl medicineProcurementServiceImpl;
	
	@Autowired
	SalesServiceImpl salesServiceImpl;
	
	@Autowired
	ChargeBillServiceImpl chargeBillServiceImpl;
	
	@Autowired
	PharmacyShopDetailsRepository pharmacyShopDetailsRepository;
	
	@Autowired
	ChargeBillRepository chargeBillRepository;
	
	@Autowired
	MedicineProcurementRepository medicineProcurementRepository;
	
	@Autowired
	MedicineDetailsServiceImpl medicineDetailsServiceImpl;
	
	@Autowired
    ResourceLoader resourceLoader;
	
	@Autowired
	LocationServiceImpl locationServiceImpl;
	
	@Autowired
	CashPlusCardServiceImpl cashPlusCardServiceImpl;
	
	@Autowired
	SalesPaymentPdf salesPaymentPdf;
	
	@Autowired
	SalesPaymentPdfServiceImpl salesPaymentPdfServiceImpl;
	
	@Autowired
	PatientSalesServiceImpl patientSalesServiceImpl;
	
	@Autowired
	PatientSalesRepository patientSalesRepository;
	
	@Autowired
	PatientRegistrationServiceImpl patientRegistrationServiceImpl;
	
	@Autowired
	MedicineQuantityServiceImpl medicineQuantityServiceImpl;
	
	@Autowired
	MedicineQuantityRepository medicineQuantityRepository;
	
	public String getNextSaleNo()
	{
		Sales sales=salesRepository.findFirstByOrderBySaleNoDesc();
		String nextId=null;
		if(sales==null)
		{
			nextId="SL0000001";
			
		}
		else
		{
			int nextIntId=Integer.parseInt(sales.getSaleNo().substring(2));
			nextIntId+=1;
			nextId="SL"+String.format("%07d", nextIntId);
			
		}
		return nextId;
	}
	
	public String getNextBillNo()
	{
		Sales sales=salesRepository.findFirstByOrderBySaleNoDesc();
		String nextId=null;
		if(sales==null)
		{
			nextId="BL0000001";
			
		}
		else
		{
			int nextIntId=Integer.parseInt(sales.getBillNo().substring(2));
			nextIntId+=1;
			nextId="BL"+String.format("%07d", nextIntId);
			
		}
		return nextId;
	}
	
	public static PdfPCell createCell(String content, float borderWidth, int colspan, int alignment, Font redFont) 
	{
	    PdfPCell cell = new PdfPCell(new Phrase(content));
	    cell.setBorderWidth(borderWidth);
	    cell.setColspan(colspan);
	    cell.setHorizontalAlignment(alignment);
	    return cell;
	}
	
	
public List<Object> displaySalesList(int days) {
		
		Iterable<Sales> sales=null;
		
		String today=new Timestamp(System.currentTimeMillis()).toString().substring(0,10);
		String nextDay=null;
		String fromDay=null;
		
		
		if(days==2)
		{
			nextDay=LocalDate.parse(today).plusDays(1).toString();
			fromDay=LocalDate.parse(today).plusDays(-2).toString();
			sales=salesRepository.findTheUserWiseDetails(fromDay, nextDay);
		}
		else if(days==7)
		{
			nextDay=LocalDate.parse(today).plusDays(1).toString();
			fromDay=LocalDate.parse(today).plusDays(-7).toString();
			sales=salesRepository.findTheUserWiseDetails(fromDay, nextDay);
		}
		else if(days==15)
		{
			nextDay=LocalDate.parse(today).plusDays(1).toString();
			fromDay=LocalDate.parse(today).plusDays(-15).toString();
			sales=salesRepository.findTheUserWiseDetails(fromDay, nextDay);
		}
		else if(days==30)
		{
			nextDay=LocalDate.parse(today).plusDays(1).toString();
			fromDay=LocalDate.parse(today).plusDays(-30).toString();
			sales=salesRepository.findTheUserWiseDetails(fromDay, nextDay);
		}
		
		
		
		
		String userName=null;
		String billNo=null;
		float amount=0;
		float returnAmount=0;
		String salesdate=null;
		List<Object> list=new ArrayList<>();
		
		
		List<String> billList=new ArrayList<>();
		Map<String, String> map=null;
		
		
		for(Sales salesInfo:sales) {
			billNo=salesInfo.getBillNo();
			List<Sales> salesBillNo=salesRepository.findByBillNo(billNo);
			if(!billList.contains(billNo))
			{
			for(Sales sale:salesBillNo)
			{
			amount+=sale.getActualAmount();
			
						
			
			String soldBy=sale.getSoldBy();
			
			User user=userServiceImpl.findOneByUserId(soldBy);
			userName = (user.getMiddleName() != null) ? user.getFirstName() + ConstantValues.ONE_SPACE_STRING
					+ user.getMiddleName() + ConstantValues.ONE_SPACE_STRING + user.getLastName()
					: user.getFirstName() + ConstantValues.ONE_SPACE_STRING + user.getLastName();

					
			String saleDate=String.valueOf(sale.getBillDate().toString());
			SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
			try {
				salesdate=toFormat.format(fromFormat.parse(saleDate));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			    map=new HashMap<>();
				map.put("billNo", billNo);
				map.put("mobileNo", String.valueOf(sale.getMobileNo()));
				map.put("patientName", sale.getName());
				map.put("Date", salesdate);
				map.put("paymentType", sale.getPaymentType());
				map.put("userName", userName);
				map.put("transactionType", "Sales");
				
				
			}
			
			billList.add(billNo);
			map.put("Amount",String.valueOf(Math.round(amount)));
			list.add(map);
			}
			amount=0;
			
	}	

			
			return list;
		}
	
	
	@Transactional
	public SalesPaymentPdf computeSave(Sales sales,Principal principal)
	{
		String regId=null;
		SalesPaymentPdf salesPaymentPdf=null;
		String paymentMode="";
		PatientRegistration patientRegistration=null;
		long finalCash=0; //final billing
		long finalCard=0; //final billing
		long finalCheque=0; //final billing
		long finalDue=0; //final billing
		long netAmount=0;
		long finalNetAmount=0;
		String billNo="";
		String patientName="";
		String umr="";
		String expdate=null;
		
		
		if(sales.getPaymentType()==null)
		{
			throw new RuntimeException(ConstantValues.ENTER_PAYMENT_TYPE_ERROR_MSG);
		}
		
		String paymentType = sales.getPaymentType();
		if (paymentType.equalsIgnoreCase(ConstantValues.CARD) || paymentType.equalsIgnoreCase("Credit Card")
				|| paymentType.equalsIgnoreCase("Debit Card") || paymentType.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {
			sales.setReferenceNumber(sales.getReferenceNumber());
		}		
		
		
		
		if(sales.getRegId()!=null)
		{
			patientRegistration=patientRegistrationServiceImpl.findByRegId(sales.getRegId());
			PatientDetails patientDetails=patientRegistration.getPatientDetails();
			sales.setUmr(patientDetails.getUmr());
			sales.setName(patientDetails.getFirstName()+" "+patientDetails.getLastName());
			sales.setMobileNo(patientDetails.getMobile());
			sales.setPatientRegistration(patientRegistration);
			umr=patientRegistration.getPatientDetails().getUmr();
			
			
			if(sales.getPatientRegistration().getPatientDetails().getMiddleName()!=null)
			{
				patientName=	sales.getPatientRegistration().getPatientDetails().getTitle()+". "+
						sales.getPatientRegistration().getPatientDetails().getFirstName() + " "
						+sales.getPatientRegistration().getPatientDetails().getMiddleName()+" "
						+ sales.getPatientRegistration().getPatientDetails().getLastName();
						
			}
			else
			{
				patientName=	sales.getPatientRegistration().getPatientDetails().getTitle()+". "+
						sales.getPatientRegistration().getPatientDetails().getFirstName() + " "
						+ sales.getPatientRegistration().getPatientDetails().getLastName();
						
			}
		}
		
		
		//CreatedBy (Security)
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		String createdBy=userSecurity.getFirstName()+" "+userSecurity.getLastName();
		
		
		List<RefSales> refSales=sales.getRefSales();
		sales.setBillDate(new Timestamp(System.currentTimeMillis()));
		sales.setSoldBy(userSecurity.getUserId());
		sales.setPatientSalesUser(userSecurity);
		Location location=locationServiceImpl.findByLocationName(sales.getLocation());
		sales.setPatientSaleslocation(location);
		sales.setBillNo(getNextBillNo());
		sales.setUpdatedBy(userSecurity.getUserId());
		sales.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
		billNo=sales.getBillNo();
		patientName=sales.getName();
		
		PatientSales patientSales=new PatientSales();
		patientSales.setSoldBy(createdBy);
		patientSales.setPatientSalesUser(userSecurity);
		patientSales.setUpdatedBy(userSecurity.getUserId());
		patientSales.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
	
		
		
		String billNoo=chargeBillServiceImpl.getNextBillNo();
		for(RefSales refSalesList:refSales)
		{
			finalNetAmount+=refSalesList.getAmount();
			sales.setSaleNo(getNextSaleNo());
			sales.setAmount(refSalesList.getAmount());
			sales.setBatchNo(refSalesList.getBatchNo());
			sales.setDiscount(refSalesList.getDiscount());
			sales.setGst(refSalesList.getGst());
			sales.setMedicineName(refSalesList.getMedicineName());
			MedicineDetails medicineDetails=medicineDetailsServiceImpl.findByName(refSalesList.getMedicineName());
			sales.setPatientSalesMedicineDetails(medicineDetails);
			sales.setMrp(refSalesList.getMrp());
			sales.setQuantity(refSalesList.getQuantity());
			sales.setActualAmount(refSalesList.getAmount());
			sales.setExpireDate(refSalesList.getExpDate());
			sales.setCostPrice(refSalesList.getMrp()*refSalesList.getQuantity());
			//for medicine quantity
			expdate=refSalesList.getExpDate();
					
			MedicineQuantity medicineQuantity = new MedicineQuantity();
			
			MedicineDetails medicineDetailsQuantity = medicineDetailsServiceImpl.findByName(sales.getMedicineName());
			
			MedicineQuantity medicineQuantityInfo = medicineQuantityServiceImpl.findByMedicineDetails(medicineDetailsQuantity);
			
			
			if(medicineQuantityInfo!=null) {
				long totalSold=medicineQuantityInfo.getSold();
				totalSold+=sales.getQuantity();
				medicineQuantityInfo.setSold(totalSold);
				medicineQuantityInfo.setBalance(medicineQuantityInfo.getTotalQuantity()-medicineQuantityInfo.getSold());
				medicineQuantityRepository.save(medicineQuantityInfo);
				
			}
			
			// for patient sales
			if(sales.getRegId()!=null)
			{
				regId=sales.getRegId();
				patientSales.setBillNo(patientSalesServiceImpl.getNextBillNo());
				patientSales.setUmr(sales.getUmr());
				patientSales.setName(sales.getName());
				patientSales.setMobileNo(sales.getMobileNo());
				patientSales.setAmount(sales.getAmount());
				patientSales.setBillDate(sales.getBillDate());
				patientSales.setQuantity(sales.getQuantity());
				patientSales.setExpireDate(sales.getExpireDate());
				patientSales.setMedicineName(sales.getMedicineName());
				patientSales.setMrp(sales.getMrp());
				patientSales.setSoldBy(sales.getSoldBy());
				patientSales.setBatchNo(sales.getBatchNo());
				patientSales.setShopName(sales.getShopName());
				if(patientRegistration.getpType().equals(ConstantValues.INPATIENT) && (sales.getPaymentType().equalsIgnoreCase("Advance") || sales.getPaymentType().equalsIgnoreCase(ConstantValues.DUE)))
				{	
					patientSales.setPaid(ConstantValues.NO);
					sales.setPaid(ConstantValues.NO);
					ChargeBill chargeBill=new ChargeBill();
					chargeBill.setChargeBillId(chargeBillServiceImpl.getNextId());
					chargeBill.setPatRegId(patientRegistration);
					chargeBill.setMrp(sales.getMrp());
					chargeBill.setAmount(refSalesList.getQuantity()*refSalesList.getMrp());
					chargeBill.setDiscount(sales.getDiscount());
					List<ChargeBill> chargeBillList=chargeBillServiceImpl.findByPatRegId(patientRegistration);
					if(chargeBillList.isEmpty())
					{
						chargeBill.setBillNo(billNoo);
					}
					else
					{
						chargeBill.setBillNo(chargeBillList.get(0).getBillNo());
					}
					chargeBill.setPaid(ConstantValues.NO);
					chargeBill.setQuantity(sales.getQuantity());
					chargeBill.setNetAmount(refSalesList.getAmount());
					chargeBill.setUpdatedBy(userSecurity.getUserId());
					chargeBill.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
					netAmount=(long)refSalesList.getAmount();
					chargeBill.setInsertedDate(new Timestamp(System.currentTimeMillis()));
					chargeBill.setSaleId(sales);
					chargeBill.setPaymentType(sales.getPaymentType());
					chargeBill.setInsertedBy(userSecurity.getUserId());
					chargeBillRepository.save(chargeBill);
				}
				else if(patientRegistration.getpType().equals(ConstantValues.INPATIENT) && (!sales.getPaymentType().equalsIgnoreCase("Advance") || !sales.getPaymentType().equalsIgnoreCase(ConstantValues.DUE)))
				{
					patientSales.setPaid(ConstantValues.YES);
					sales.setPaid(ConstantValues.YES);
					ChargeBill chargeBill=new ChargeBill();
					chargeBill.setChargeBillId(chargeBillServiceImpl.getNextId());
					chargeBill.setPatRegId(patientRegistration);
					chargeBill.setMrp(sales.getMrp());
					chargeBill.setUpdatedBy(userSecurity.getUserId());
					chargeBill.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
					chargeBill.setPaymentType(sales.getPaymentType());
					chargeBill.setAmount(refSalesList.getQuantity()*refSalesList.getMrp());
					chargeBill.setDiscount(sales.getDiscount());
					List<ChargeBill> chargeBillList=chargeBillServiceImpl.findByPatRegId(patientRegistration);
					if(chargeBillList.isEmpty())
					{
						chargeBill.setBillNo(billNoo);
					}
					else
					{
						chargeBill.setBillNo(chargeBillList.get(0).getBillNo());
					}
					chargeBill.setPaid(ConstantValues.YES);
					chargeBill.setQuantity(sales.getQuantity());
					chargeBill.setNetAmount(refSalesList.getAmount());
					chargeBill.setInsertedDate(new Timestamp(System.currentTimeMillis()));
					chargeBill.setSaleId(sales);
					chargeBill.setInsertedBy(userSecurity.getUserId());
					chargeBillRepository.save(chargeBill);
				}
				else
				{
					sales.setPaid(ConstantValues.YES);
					patientSales.setPaid(ConstantValues.YES);
				}
				patientSales.setGst(sales.getGst());
				patientSales.setDiscount(sales.getDiscount());
				patientSales.setPaymentType(sales.getPaymentType());
				paymentMode=sales.getPaymentType();
				patientSales.setSalesBillNo(sales.getBillNo());
				patientSales.setPatientSaleslocation(sales.getPatientSaleslocation());
				patientSales.setPatientSalesPatientRegistration(sales.getPatientRegistration());
				patientSales.setPatientSalesMedicineDetails(sales.getPatientSalesMedicineDetails());
				patientSales.setAmtInWords(sales.getAmtInWords());
				patientSalesRepository.save(patientSales);
			}
			else
			{
				paymentMode=sales.getPaymentType();
				sales.setPaid(ConstantValues.YES);
			}
			
			
			
			//For Employee sales
			if(sales.getEmpId()!=null)
			{
				User user=userServiceImpl.findOneByUserId(sales.getEmpId());
				sales.setEmployeeId(user);
			}
			
			
			salesRepository.save(sales);
			
		}
		
		
	// Cash + Card
		
		if(paymentMode.equalsIgnoreCase(ConstantValues.CASH))
		{
			finalCash=finalNetAmount;
		}
		else if(paymentMode.equalsIgnoreCase(ConstantValues.CARD))
		{
			finalCard=finalNetAmount;
		}
		else if(paymentMode.equalsIgnoreCase(ConstantValues.CHEQUE))
		{
			finalCheque=finalNetAmount;
		}
		else if(paymentMode.equalsIgnoreCase(ConstantValues.DUE))
		{
			finalDue=finalNetAmount;
		}
		
		
		if(paymentMode.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD))
		{
			int cashAmount=0;
			int cardAmount=0;
			int chequeAmount=0;
			CashPlusCard cashPlusCardLab=new CashPlusCard();
			cashPlusCardLab.setInsertedBy(userSecurity.getUserId());
			cashPlusCardLab.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			List<Map<String,String>> multiMode=sales.getMultimode();
			for(Map<String,String> multiModeInfo:multiMode)
			{
				if(multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CASH))
				{
					cashAmount=Integer.parseInt(multiModeInfo.get("amount"));
					finalCash=Long.parseLong(multiModeInfo.get("amount"));
				}
				else if(multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CARD))
				{
					cardAmount=Integer.parseInt(multiModeInfo.get("amount"));
					finalCard=Long.parseLong(multiModeInfo.get("amount"));
				}
				else if(multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CHEQUE))
				{
					chequeAmount=Integer.parseInt(multiModeInfo.get("amount"));
					finalCheque=Long.parseLong(multiModeInfo.get("amount"));
				}
				
			}
			cashPlusCardLab.setDescription((patientRegistration!=null) ? "Sales" :"Walkin Sales");
			cashPlusCardLab.setPatientRegistrationCashCard((patientRegistration!=null) ? patientRegistration : null);
			cashPlusCardLab.setCardAmount(cardAmount);
			cashPlusCardLab.setCashAmount(cashAmount);
			cashPlusCardLab.setBillNo(sales.getBillNo());
			cashPlusCardLab.setChequeAmount(chequeAmount);
			cashPlusCardServiceImpl.save(cashPlusCardLab);
			
			
			
		}
		
		
		if(patientRegistration!=null && patientRegistration.getpType().equalsIgnoreCase(ConstantValues.INPATIENT) )
		{
			//Final Billing  
			 FinalBilling finalBilling=new FinalBilling();
			 finalBilling.setBillNo(billNo);
			 finalBilling.setBillType("Sales");
			 finalBilling.setCardAmount(finalCard);
			 finalBilling.setCashAmount(finalCash);
			 finalBilling.setChequeAmount(finalCheque);
			 finalBilling.setDueAmount(finalDue);
			 finalBilling.setFinalAmountPaid(finalNetAmount);
			 finalBilling.setFinalBillUser(userSecurity);
			 finalBilling.setName(patientName);
			 finalBilling.setRegNo(regId);
			 finalBilling.setPaymentType(paymentMode);
			 finalBilling.setTotalAmount(finalNetAmount);
			 finalBilling.setUmrNo(umr);
			finalBillingServcieImpl.computeSave(finalBilling);
		}
		
		//To find total amount of bill
	
		float total=0.0f;
		List<Sales> listSales=findByBillNo(billNo);
		for(Sales listSalesInfo:listSales)
		{
			total+=listSalesInfo.getAmount();
		}
		
		String roundOff=null;

	/*	String myAd="Plot No14,15,16 & 17,Nandi Co-op.Society,"
		+ "\n                             Main Road, Beside Navya Grand Hotel, \n                                       Miyapur,Hyderabad-49   \n                               "
				+ "        Phone:040-23046789    "
				+ "\n                           Email :udbhavahospitals@gmail.com";
*/
		//shantharam addr

		String myAd="";
		
		
		
		
		
		if (patientRegistration != null) {
			if (!patientRegistration.getpType().equals(ConstantValues.INPATIENT)) {
				byte[] pdfByte = null;
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

				Document document = new Document(PageSize.A4_LANDSCAPE);
				try {

					Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");
					Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
					Font redFonts = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
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
					pq.add(new Chunk(
					myAd,		redFont));
	                   
					PdfPCell cellp = new PdfPCell(pq);
					PdfPCell cell1 = new PdfPCell();
					
					//for header bold
					PdfPTable table96 = new PdfPTable(1);
					table96.setWidths(new float[] { 5f });
					table96.setSpacingBefore(10);

					PdfPCell hcell96;
					hcell96 = new PdfPCell(new Phrase(ConstantValues.PHARMACY_NAME, redFont1));
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
					table2.setWidths(new float[] { 5f,1f,5f });
					table2.setSpacingBefore(10);

					PdfPCell hcell1;
					hcell1 = new PdfPCell(new Phrase("Bill#" , redFont));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setPaddingLeft(-15f);
					table2.addCell(hcell1);
					
					hcell1 = new PdfPCell(new Phrase(":" , redFont));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setPaddingLeft(-35f);
					table2.addCell(hcell1);
					
					hcell1 = new PdfPCell(new Phrase( sales.getBillNo(), redFont));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setPaddingLeft(-25f);
					table2.addCell(hcell1);

					// Display a date in day, month, year format
					Date date = Calendar.getInstance().getTime();
					DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
					String today = formatter.format(date).toString();
					
					
					
					
					PdfPCell hcel123;
					hcel123 = new PdfPCell(new Phrase("Bill Date" , redFont));
					hcel123.setBorder(Rectangle.NO_BORDER);
					hcel123.setPaddingLeft(-15f);

					hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel123);
					
					hcel123 = new PdfPCell(new Phrase(":" , redFont));
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
					hcell18 = new PdfPCell(new Phrase("Patient Name" ,redFont));
					hcell18.setBorder(Rectangle.NO_BORDER);
					hcell18.setPaddingLeft(-15f);
					table2.addCell(hcell18);
					
					hcell18 = new PdfPCell(new Phrase(":",redFont));
					hcell18.setBorder(Rectangle.NO_BORDER);
					hcell18.setPaddingLeft(-35f);
					table2.addCell(hcell18);
					
					hcell18 = new PdfPCell(new Phrase(
							patientName,
							redFont));
					hcell18.setBorder(Rectangle.NO_BORDER);
					hcell18.setPaddingLeft(-25f);
					table2.addCell(hcell18);
					
					
					PdfPCell hcel;

					hcel = new PdfPCell(new Phrase("UMR No" , redFont));
					hcel.setBorder(Rectangle.NO_BORDER);
					hcel.setPaddingLeft(-15f);
					table2.addCell(hcel);
					
					hcel = new PdfPCell(new Phrase(":" , redFont));
					hcel.setBorder(Rectangle.NO_BORDER);
					hcel.setPaddingLeft(-35f);
					table2.addCell(hcel);
					
					hcel = new PdfPCell(new Phrase(sales.getPatientRegistration().getPatientDetails().getUmr(), redFont));
					hcel.setBorder(Rectangle.NO_BORDER);
					hcel.setPaddingLeft(-25f);
					table2.addCell(hcel);
					
					
					PdfPCell hcel11;
					hcel11 = new PdfPCell(new Phrase("P.RegNo" , redFont));
					hcel11.setBorder(Rectangle.NO_BORDER);
					hcel11.setPaddingLeft(-15f);
					hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel11);
					
					hcel11 = new PdfPCell(new Phrase(":" , redFont));
					hcel11.setBorder(Rectangle.NO_BORDER);
					hcel11.setPaddingLeft(-35f);
					hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel11);
					
					hcel11 = new PdfPCell(new Phrase( sales.getPatientRegistration().getRegId(), redFont));
					hcel11.setBorder(Rectangle.NO_BORDER);
					hcel11.setPaddingLeft(-25f);
					hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel11);
					
					PdfPCell hcel1;

					hcel1 = new PdfPCell(new Phrase("Doctor Name", redFont));
					hcel1.setBorder(Rectangle.NO_BORDER);
					hcel1.setPaddingLeft(-15f);
					table2.addCell(hcel1);
					
					hcel1 = new PdfPCell(new Phrase(":" , redFont));
					hcel1.setBorder(Rectangle.NO_BORDER);
					hcel1.setPaddingLeft(-35f);
					table2.addCell(hcel1);
					
					hcel1 = new PdfPCell(new Phrase(sales.getPatientRegistration().getPatientDetails().getConsultant() , redFont));
					hcel1.setBorder(Rectangle.NO_BORDER);
					hcel1.setPaddingLeft(-25f);
					table2.addCell(hcel1);
					
					
					cell0.setFixedHeight(100f);
					cell0.setColspan(2);
					cell0.addElement(table2);
					table.addCell(cell0);				
					
					PdfPCell cell19 = new PdfPCell();

					PdfPTable table21 = new PdfPTable(3);
					table21.setWidths(new float[] { 4f, 4f, 5f });
					table21.setSpacingBefore(10);

					PdfPCell hcell15;
					hcell15 = new PdfPCell(new Phrase("", redFont));
					hcell15.setBorder(Rectangle.NO_BORDER);
					hcell15.setPaddingLeft(-70f);
					table21.addCell(hcell15);

					hcell15 = new PdfPCell(new Phrase("Pharmacy Receipt", redFont3));
					hcell15.setBorder(Rectangle.NO_BORDER);
					hcell15.setPaddingLeft(35);
					hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
					table21.addCell(hcell15);

					hcell15 = new PdfPCell(new Phrase("" , redFont));
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
					table1.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2.8f});

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
					hcell.setPaddingLeft(-15f);
				
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("Batch No", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell.setPaddingLeft(20f);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("Exp Date", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell.setPaddingRight(-5f);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("Qty", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell.setPaddingRight(-18);
					table1.addCell(hcell);
					
					hcell = new PdfPCell(new Phrase("Disc(%)", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell.setPaddingRight(-25f);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("MRP", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell.setPaddingRight(-10f);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("GST", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell.setPaddingRight(-10f);
					table1.addCell(hcell);
					
					hcell = new PdfPCell(new Phrase("Sale Value", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table1.addCell(hcell);
					int count = 0;

					PdfPTable table20 = new PdfPTable(10);
					table20.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2f});

					table20.setSpacingBefore(10);

					
					
					for (RefSales a : refSales) {

						MedicineDetails medicineDetails1 = medicineDetailsServiceImpl.findByName(a.getMedicineName());
						List<MedicineProcurement> medicineProcurement=medicineProcurementServiceImpl.findByBatchAndMedicine(a.getBatchNo(), medicineDetails1.getMedicineId());
						PdfPCell cell;

						cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table20.addCell(cell);

						cell = new PdfPCell(new Phrase(a.getMedicineName(), redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(-1);
						//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					    table20.addCell(cell);

						cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						//cell.setPaddingLeft(-30);
						//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					 cell.setPaddingLeft(-15f);
					 table20.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(a.getBatchNo()), redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						
						//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				   		cell.setPaddingLeft(12);
						// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table20.addCell(cell);
						
						// for convert db date to dmy format
						
						
						/*String expdate=medicineProcurement.get(medicineProcurement.size() - 1).getExpDate().toString().substring(0,10);
						SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
						expdate=toFormat.format(fromFormat.parse(expdate));
					*/
						
						try
						{
							expdate=a.getExpDate().toString().substring(0, 10);
						SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
						expdate=toFormat.format(fromFormat.parse(expdate));
						
						}
						catch(Exception e)
						{
						e.printStackTrace();
						}
						
						cell = new PdfPCell(new Phrase(expdate,redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setPaddingLeft(-10);
						table20.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf((a.getQuantity())), redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(8);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table20.addCell(cell);
						
						cell = new PdfPCell(new Phrase(String.valueOf((a.getDiscount())), redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(8);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setPaddingLeft(10f);
						table20.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf((a.getMrp())), redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setPaddingRight(10);
						table20.addCell(cell);
						
						
						cell = new PdfPCell(new Phrase(String.valueOf((a.getGst())), redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(-15);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table20.addCell(cell);


						cell = new PdfPCell(new Phrase(String.valueOf((a.getAmount())), redFonts));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table20.addCell(cell);

					}
					cell3.setColspan(2);
					table1.setWidthPercentage(100f);
					table20.setWidthPercentage(100f);
					cell3.addElement(table1);  
					cell3.addElement(table20);
					table.addCell(cell3);
					
					
					PdfPCell cell4 = new PdfPCell();

					PdfPTable table4 = new PdfPTable(6);
					table4.setWidths(new float[] { 5f,1f, 5f,8f,1f, 3f });
					table4.setSpacingBefore(10);

					int ttl=(int)Math.round(total);
					PdfPCell hcell2;
					hcell2 = new PdfPCell(new Phrase("Recieved Sum of Rupees",redFont));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table4.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(":",redFont));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-40f);
					table4.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(numberToWordsConverter.convert(ttl) + " Rupees Only",redFont));
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
					
					hcell2 = new PdfPCell(new Phrase(String.valueOf(Math.round(total*100.0)/100.0), redFont));
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

					hcell04 = new PdfPCell(new Phrase("Rounded Off To", redFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell04.setPaddingLeft(85f);
					table4.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase(":", redFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell04.setPaddingRight(-30f);
					table4.addCell(hcell04);
					
					/*BigDecimal bg=new BigDecimal(total-Math.floor(total));
					bg=bg.setScale(2,RoundingMode.HALF_DOWN);
					float round=bg.floatValue();
					//float rd=Math.nextUp(1.0f-round);
					float rd=1.00f-round;
					
					if(round<0.50)
					{
						hcell04 = new PdfPCell(new Phrase("-" +round, redFont));
					}
					else
					{
						if(String.valueOf(rd).length()>=4)
						{
						hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd).substring(0, 4)  , redFont));
						}
						else
						{
							hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd+"/"+round) , redFont));
							
							
						}
					}
					*/
					
					hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)) , redFont));
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

					hcell4 = new PdfPCell(new Phrase("Net Amount", redFont));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell4.setPaddingLeft(85f);
					table4.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(":", redFont));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell4.setPaddingRight(-30f);
					table4.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

					hcell9 = new PdfPCell(new Phrase("Reciept Amount", redFont));
					hcell9.setBorder(Rectangle.NO_BORDER);
					hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell9.setPaddingLeft(85f);
					table4.addCell(hcell9);
					
					hcell9 = new PdfPCell(new Phrase(":", redFont));
					hcell9.setBorder(Rectangle.NO_BORDER);					
					hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell9.setPaddingRight(-30f);
					table4.addCell(hcell9);
					
					hcell9 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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
					table13.setWidths(new float[] { 2f, 3f,3f, 3f, 3f });

					table13.setSpacingBefore(10);

					PdfPCell hcell33;
					hcell33 = new PdfPCell(new Phrase("Pay Mode", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(10f);
					table13.addCell(hcell33);

					hcell33 = new PdfPCell(new Phrase("Amount", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(35f);
					table13.addCell(hcell33);
					
					
					hcell33 = new PdfPCell(new Phrase("Card#", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(40f);
					table13.addCell(hcell33);

					hcell33 = new PdfPCell(new Phrase("Bank Name", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(40f);
					table13.addCell(hcell33);

					hcell33 = new PdfPCell(new Phrase("Exp Date", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(50f);
					table13.addCell(hcell33);

					PdfPCell hcell34;
					hcell34 = new PdfPCell(new Phrase(sales.getPaymentType(), redFont2));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(10f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont2));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(35f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase("", redFont1));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(40f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase("", redFont1));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(40f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase("", redFont1));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(50f);
					table13.addCell(hcell34);

					cell33.setFixedHeight(35f);
					cell33.setColspan(2);
					table13.setWidthPercentage(100f);
					cell33.addElement(table13);
					table.addCell(cell33);

										// for new row end

					PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository
							.findByShopLocation(sales.getLocation());

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

					PdfPCell hcell7;

					hcell7 = new PdfPCell(new Phrase(
							"Instructions  : "
									+ "1) Returns are accepted within TEN(10)Days. \n                       2) Fridge Items once sold cannot be taken Back.",
							redFont));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setPaddingLeft(-50f);
					table5.addCell(hcell7);

					hcell7 = new PdfPCell(new Phrase("Pharmacist"));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell7.setPaddingTop(25f);
					table5.addCell(hcell7);

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
					salesPaymentPdf.setFileName(billNo+"-"+regId+" Medicine Sales");
					salesPaymentPdf.setFileuri(uri);
					salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
					salesPaymentPdf.setData(pdfByte);
					salesPaymentPdfServiceImpl.save(salesPaymentPdf);

				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		else if(patientRegistration.getpType().equals(ConstantValues.INPATIENT) && sales.getPaymentType().equalsIgnoreCase("Advance"))
		{
			byte[] pdfByte = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			Document document = new Document(PageSize.A4_LANDSCAPE);
			try {

				Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");
				Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
				Font redFonts = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
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
				pq.add(new Chunk(myAd,redFont));
                   
				PdfPCell cellp = new PdfPCell(pq);
				PdfPCell cell1 = new PdfPCell();
				
				//for header bold
				PdfPTable table96 = new PdfPTable(1);
				table96.setWidths(new float[] { 5f });
				table96.setSpacingBefore(10);

				PdfPCell hcell96;
				hcell96 = new PdfPCell(new Phrase("UDBHAVA PHARMACY", redFont1));
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
				table2.setWidths(new float[] { 5f,1f,5f });
				table2.setSpacingBefore(10);

				PdfPCell hcell1;
				hcell1 = new PdfPCell(new Phrase("Bill#" , redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-15f);
				table2.addCell(hcell1);
				
				hcell1 = new PdfPCell(new Phrase(":" , redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-35f);
				table2.addCell(hcell1);
				
				hcell1 = new PdfPCell(new Phrase( sales.getBillNo(), redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-25f);
				table2.addCell(hcell1);

				// Display a date in day, month, year format
				Date date = Calendar.getInstance().getTime();
				DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
				String today = formatter.format(date).toString();
				
				
				
				PdfPCell hcel123;
				hcel123 = new PdfPCell(new Phrase("Bill Date" , redFont));
				hcel123.setBorder(Rectangle.NO_BORDER);
				hcel123.setPaddingLeft(-15f);

				hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel123);
				
				hcel123 = new PdfPCell(new Phrase(":" , redFont));
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
				hcell18 = new PdfPCell(new Phrase("Patient Name" ,redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-15f);
				table2.addCell(hcell18);
				
				hcell18 = new PdfPCell(new Phrase(":",redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-35f);
				table2.addCell(hcell18);
				
				hcell18 = new PdfPCell(new Phrase(patientName,
						redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-25f);
				table2.addCell(hcell18);
				
				
				PdfPCell hcel;

				hcel = new PdfPCell(new Phrase("UMR No" , redFont));
				hcel.setBorder(Rectangle.NO_BORDER);
				hcel.setPaddingLeft(-15f);
				table2.addCell(hcel);
				
				hcel = new PdfPCell(new Phrase(":" , redFont));
				hcel.setBorder(Rectangle.NO_BORDER);
				hcel.setPaddingLeft(-35f);
				table2.addCell(hcel);
				
				hcel = new PdfPCell(new Phrase(sales.getPatientRegistration().getPatientDetails().getUmr(), redFont));
				hcel.setBorder(Rectangle.NO_BORDER);
				hcel.setPaddingLeft(-25f);
				table2.addCell(hcel);
				
				
				PdfPCell hcel11;
				hcel11 = new PdfPCell(new Phrase("P.RegNo" , redFont));
				hcel11.setBorder(Rectangle.NO_BORDER);
				hcel11.setPaddingLeft(-15f);
				hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel11);
				
				hcel11 = new PdfPCell(new Phrase(":" , redFont));
				hcel11.setBorder(Rectangle.NO_BORDER);
				hcel11.setPaddingLeft(-35f);
				hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel11);
				
				hcel11 = new PdfPCell(new Phrase( sales.getPatientRegistration().getRegId(), redFont));
				hcel11.setBorder(Rectangle.NO_BORDER);
				hcel11.setPaddingLeft(-25f);
				hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel11);
				
				PdfPCell hcel1;

				hcel1 = new PdfPCell(new Phrase("Doctor Name", redFont));
				hcel1.setBorder(Rectangle.NO_BORDER);
				hcel1.setPaddingLeft(-15f);
				table2.addCell(hcel1);
				
				hcel1 = new PdfPCell(new Phrase(":" , redFont));
				hcel1.setBorder(Rectangle.NO_BORDER);
				hcel1.setPaddingLeft(-35f);
				table2.addCell(hcel1);
				
				hcel1 = new PdfPCell(new Phrase(sales.getPatientRegistration().getPatientDetails().getConsultant() , redFont));
				hcel1.setBorder(Rectangle.NO_BORDER);
				hcel1.setPaddingLeft(-25f);
				table2.addCell(hcel1);
				
				
				cell0.setFixedHeight(100f);
				cell0.setColspan(2);
				cell0.addElement(table2);
				table.addCell(cell0);				
				
				PdfPCell cell19 = new PdfPCell();

				PdfPTable table21 = new PdfPTable(3);
				table21.setWidths(new float[] { 4f, 8f, 5f });
				table21.setSpacingBefore(10);

				PdfPCell hcell15;
				hcell15 = new PdfPCell(new Phrase("", redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingLeft(-70f);
				table21.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase("Pharmacy Advance Receipt", redFont3));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingLeft(10);
				hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
				table21.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase("" , redFont));
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
				table1.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2.8f});

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
				hcell.setPaddingLeft(-15f);
			
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Batch No", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(20f);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Exp Date", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-5f);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Qty", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-18);
				table1.addCell(hcell);
				
				hcell = new PdfPCell(new Phrase("Disc(%)", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-25f);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("MRP", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-10f);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("GST", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-10f);
				table1.addCell(hcell);
				
				hcell = new PdfPCell(new Phrase("Sale Value", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table1.addCell(hcell);
				int count = 0;

				PdfPTable table20 = new PdfPTable(10);
				table20.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2f});

				table20.setSpacingBefore(10);

				
				
				for (RefSales a : refSales) {

					MedicineDetails medicineDetails1 = medicineDetailsServiceImpl.findByName(a.getMedicineName());
					List<MedicineProcurement> medicineProcurement=medicineProcurementServiceImpl.findByBatchAndMedicine(a.getBatchNo(), medicineDetails1.getMedicineId());
					PdfPCell cell;

					cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table20.addCell(cell);

					cell = new PdfPCell(new Phrase(a.getMedicineName(), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(-1);
					//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				    table20.addCell(cell);

					cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					//cell.setPaddingLeft(-30);
					//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				 cell.setPaddingLeft(-15f);
				 table20.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(a.getBatchNo()), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					
					//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			   		cell.setPaddingLeft(12);
					// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table20.addCell(cell);
					
					// for convert db date to dmy format
					
					
					/*String expdate=medicineProcurement.get(medicineProcurement.size() - 1).getExpDate().toString().substring(0,10);
					SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
					expdate=toFormat.format(fromFormat.parse(expdate));
				*/
					
					try
					{
						expdate=a.getExpDate().toString().substring(0, 10);
					SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
					expdate=toFormat.format(fromFormat.parse(expdate));
					
					}
					catch(Exception e)
					{
					e.printStackTrace();
					}
					
					cell = new PdfPCell(new Phrase(expdate,redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setPaddingLeft(-10);
					table20.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf((a.getQuantity())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(8);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table20.addCell(cell);
					
					cell = new PdfPCell(new Phrase(String.valueOf((a.getDiscount())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(8);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPaddingLeft(10f);
					table20.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf((a.getMrp())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setPaddingRight(10);
					table20.addCell(cell);
					
					
					cell = new PdfPCell(new Phrase(String.valueOf((a.getGst())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(-15);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table20.addCell(cell);


					cell = new PdfPCell(new Phrase(String.valueOf((a.getAmount())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table20.addCell(cell);

				}
				cell3.setColspan(2);
				table1.setWidthPercentage(100f);
				table20.setWidthPercentage(100f);
				cell3.addElement(table1);  
				cell3.addElement(table20);
				table.addCell(cell3);
				
				
				PdfPCell cell4 = new PdfPCell();

				PdfPTable table4 = new PdfPTable(6);
				table4.setWidths(new float[] { 5f,1f, 5f,8f,1f, 3f });
				table4.setSpacingBefore(10);

				int ttl=(int)Math.round(total);
				PdfPCell hcell2;
				hcell2 = new PdfPCell(new Phrase("Recieved Sum of Rupees",redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(-50f);
				table4.addCell(hcell2);
				
				hcell2 = new PdfPCell(new Phrase(":",redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(-40f);
				table4.addCell(hcell2);
				
				hcell2 = new PdfPCell(new Phrase(numberToWordsConverter.convert(ttl) + " Rupees Only",redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(-50f);
				table4.addCell(hcell2);

				hcell2 = new PdfPCell(new Phrase("Total Sale Value ", redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(85f);
				table4.addCell(hcell2);
				
				hcell2 = new PdfPCell(new Phrase(":", redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell2.setPaddingRight(-30f);
				table4.addCell(hcell2);
				
				hcell2 = new PdfPCell(new Phrase(String.valueOf(Math.round(total*100.0)/100.0), redFont));
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

				hcell04 = new PdfPCell(new Phrase("Rounded Off To", redFont));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell04.setPaddingLeft(85f);
				table4.addCell(hcell04);
				
				hcell04 = new PdfPCell(new Phrase(":", redFont));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell04.setPaddingRight(-30f);
				table4.addCell(hcell04);
				
			/*	BigDecimal bg=new BigDecimal(total-Math.floor(total));
				bg=bg.setScale(2,RoundingMode.HALF_DOWN);
				float round=bg.floatValue();
				//float rd=Math.nextUp(1f-round);
				float rd=1.00f-round;
				
				if(round<0.50)
				{
					hcell04 = new PdfPCell(new Phrase("-" +round , redFont));
				}
				else
				{
					
					if(String.valueOf(rd).length()>=4)
					{
						hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd).substring(0, 4), redFont));}
					else
					{
						hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd), redFont));	
						
					}
					
				}
			*/	
				hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

				hcell4 = new PdfPCell(new Phrase("Net Amount", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(85f);
				table4.addCell(hcell4);
				
				hcell4 = new PdfPCell(new Phrase(":", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell4.setPaddingRight(-30f);
				table4.addCell(hcell4);
				
				hcell4 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

				hcell9 = new PdfPCell(new Phrase("Reciept Amount", redFont));
				hcell9.setBorder(Rectangle.NO_BORDER);
				hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell9.setPaddingLeft(85f);
				table4.addCell(hcell9);
				
				hcell9 = new PdfPCell(new Phrase(":", redFont));
				hcell9.setBorder(Rectangle.NO_BORDER);					
				hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell9.setPaddingRight(-30f);
				table4.addCell(hcell9);
				
				hcell9 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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
				table13.setWidths(new float[] { 2f, 3f,3f, 3f, 3f });

				table13.setSpacingBefore(10);

				PdfPCell hcell33;
				hcell33 = new PdfPCell(new Phrase("Pay Mode", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(10f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase("Amount", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(35f);
				table13.addCell(hcell33);
				
				
				hcell33 = new PdfPCell(new Phrase("Card#", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(40f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase("Bank Name", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(40f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase("Exp Date", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(50f);
				table13.addCell(hcell33);

				PdfPCell hcell34;
				hcell34 = new PdfPCell(new Phrase(sales.getPaymentType(), redFont2));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(10f);
				table13.addCell(hcell34);

				hcell34 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont2));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(35f);
				table13.addCell(hcell34);

				hcell34 = new PdfPCell(new Phrase("", redFont1));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(40f);
				table13.addCell(hcell34);

				hcell34 = new PdfPCell(new Phrase("", redFont1));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(40f);
				table13.addCell(hcell34);

				hcell34 = new PdfPCell(new Phrase("", redFont1));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(50f);
				table13.addCell(hcell34);

				cell33.setFixedHeight(35f);
				cell33.setColspan(2);
				table13.setWidthPercentage(100f);
				cell33.addElement(table13);
				table.addCell(cell33);

									// for new row end

				PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository
						.findByShopLocation(sales.getLocation());

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

				PdfPCell hcell7;

				hcell7 = new PdfPCell(new Phrase(
						"Instructions  : "
								+ "1) Returns are accepted within TEN(10)Days. \n                       2) Fridge Items once sold cannot be taken Back.",
						redFont));
				hcell7.setBorder(Rectangle.NO_BORDER);
				hcell7.setPaddingLeft(-50f);
				table5.addCell(hcell7);

				hcell7 = new PdfPCell(new Phrase("Pharmacist"));
				hcell7.setBorder(Rectangle.NO_BORDER);
				hcell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell7.setPaddingTop(25f);
				table5.addCell(hcell7);

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
				salesPaymentPdf.setFileName(billNo+"-"+regId+" Medicine Sales");
				salesPaymentPdf.setFileuri(uri);
				salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
				salesPaymentPdf.setData(pdfByte);
				salesPaymentPdfServiceImpl.save(salesPaymentPdf);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		else if(patientRegistration.getpType().equals(ConstantValues.INPATIENT) && !sales.getPaymentType().equalsIgnoreCase("Advance"))
		{
			byte[] pdfByte = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			Document document = new Document(PageSize.A4_LANDSCAPE);
			try {
				
				Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");
				Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
				Font redFonts = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
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
				pq.add(new Chunk(myAd,redFont));
                   
				PdfPCell cellp = new PdfPCell(pq);
				PdfPCell cell1 = new PdfPCell();
				
				//for header bold
				PdfPTable table96 = new PdfPTable(1);
				table96.setWidths(new float[] { 5f });
				table96.setSpacingBefore(10);

				PdfPCell hcell96;
				hcell96 = new PdfPCell(new Phrase("UDBHAVA PHARMACY", redFont1));
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
				table2.setWidths(new float[] { 5f,1f,5f });
				table2.setSpacingBefore(10);

				PdfPCell hcell1;
				hcell1 = new PdfPCell(new Phrase("Bill#" , redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-15f);
				table2.addCell(hcell1);
				
				hcell1 = new PdfPCell(new Phrase(":" , redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-35f);
				table2.addCell(hcell1);
				
				hcell1 = new PdfPCell(new Phrase( sales.getBillNo(), redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-25f);
				table2.addCell(hcell1);

				// Display a date in day, month, year format
				Date date = Calendar.getInstance().getTime();
				DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
				String today = formatter.format(date).toString();
				
				
				
				PdfPCell hcel123;
				hcel123 = new PdfPCell(new Phrase("Bill Date" , redFont));
				hcel123.setBorder(Rectangle.NO_BORDER);
				hcel123.setPaddingLeft(-15f);

				hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel123);
				
				hcel123 = new PdfPCell(new Phrase(":" , redFont));
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
				hcell18 = new PdfPCell(new Phrase("Patient Name" ,redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-15f);
				table2.addCell(hcell18);
				
				hcell18 = new PdfPCell(new Phrase(":",redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-35f);
				table2.addCell(hcell18);
				
				hcell18 = new PdfPCell(new Phrase(
						patientName,
						redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-25f);
				table2.addCell(hcell18);
				
				
				PdfPCell hcel;

				hcel = new PdfPCell(new Phrase("UMR No" , redFont));
				hcel.setBorder(Rectangle.NO_BORDER);
				hcel.setPaddingLeft(-15f);
				table2.addCell(hcel);
				
				hcel = new PdfPCell(new Phrase(":" , redFont));
				hcel.setBorder(Rectangle.NO_BORDER);
				hcel.setPaddingLeft(-35f);
				table2.addCell(hcel);
				
				hcel = new PdfPCell(new Phrase(sales.getPatientRegistration().getPatientDetails().getUmr(), redFont));
				hcel.setBorder(Rectangle.NO_BORDER);
				hcel.setPaddingLeft(-25f);
				table2.addCell(hcel);
				
				
				PdfPCell hcel11;
				hcel11 = new PdfPCell(new Phrase("P.RegNo" , redFont));
				hcel11.setBorder(Rectangle.NO_BORDER);
				hcel11.setPaddingLeft(-15f);
				hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel11);
				
				hcel11 = new PdfPCell(new Phrase(":" , redFont));
				hcel11.setBorder(Rectangle.NO_BORDER);
				hcel11.setPaddingLeft(-35f);
				hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel11);
				
				hcel11 = new PdfPCell(new Phrase( sales.getPatientRegistration().getRegId(), redFont));
				hcel11.setBorder(Rectangle.NO_BORDER);
				hcel11.setPaddingLeft(-25f);
				hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcel11);
				
				PdfPCell hcel1;

				hcel1 = new PdfPCell(new Phrase("Doctor Name", redFont));
				hcel1.setBorder(Rectangle.NO_BORDER);
				hcel1.setPaddingLeft(-15f);
				table2.addCell(hcel1);
				
				hcel1 = new PdfPCell(new Phrase(":" , redFont));
				hcel1.setBorder(Rectangle.NO_BORDER);
				hcel1.setPaddingLeft(-35f);
				table2.addCell(hcel1);
				
				hcel1 = new PdfPCell(new Phrase(sales.getPatientRegistration().getPatientDetails().getConsultant() , redFont));
				hcel1.setBorder(Rectangle.NO_BORDER);
				hcel1.setPaddingLeft(-25f);
				table2.addCell(hcel1);
				
				
				cell0.setFixedHeight(100f);
				cell0.setColspan(2);
				cell0.addElement(table2);
				table.addCell(cell0);				
				
				PdfPCell cell19 = new PdfPCell();

				PdfPTable table21 = new PdfPTable(3);
				table21.setWidths(new float[] { 4f, 8f, 5f });
				table21.setSpacingBefore(10);

				PdfPCell hcell15;
				hcell15 = new PdfPCell(new Phrase("", redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingLeft(-70f);
				table21.addCell(hcell15);
				
				if(sales.getPaymentType().equalsIgnoreCase(ConstantValues.DUE))
				{
					hcell15 = new PdfPCell(new Phrase("Pharmacy Due Receipt", redFont3));
					hcell15.setBorder(Rectangle.NO_BORDER);
					hcell15.setPaddingLeft(35);
					hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
					table21.addCell(hcell15);
				}
				else
				{
					hcell15 = new PdfPCell(new Phrase("Pharmacy Receipt", redFont3));
					hcell15.setBorder(Rectangle.NO_BORDER);
					hcell15.setPaddingLeft(35);
					hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
					table21.addCell(hcell15);
				}
				
				hcell15 = new PdfPCell(new Phrase("" , redFont));
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
				table1.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2.8f});

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
				hcell.setPaddingLeft(-15f);
			
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Batch No", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(20f);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Exp Date", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-5f);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("Qty", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-18);
				table1.addCell(hcell);
				
				hcell = new PdfPCell(new Phrase("Disc(%)", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-25f);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("MRP", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-10f);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase("GST", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell.setPaddingRight(-10f);
				table1.addCell(hcell);
				
				hcell = new PdfPCell(new Phrase("Sale Value", redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table1.addCell(hcell);
				int count = 0;

				PdfPTable table20 = new PdfPTable(10);
				table20.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2f});

				table20.setSpacingBefore(10);

				
				
				for (RefSales a : refSales) {

					MedicineDetails medicineDetails1 = medicineDetailsServiceImpl.findByName(a.getMedicineName());
					List<MedicineProcurement> medicineProcurement=medicineProcurementServiceImpl.findByBatchAndMedicine(a.getBatchNo(), medicineDetails1.getMedicineId());
					PdfPCell cell;

					cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table20.addCell(cell);

					cell = new PdfPCell(new Phrase(a.getMedicineName(), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(-1);
					//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				    table20.addCell(cell);

					cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					//cell.setPaddingLeft(-30);
					//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				 cell.setPaddingLeft(-15f);
				 table20.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(a.getBatchNo()), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					
					//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			   		cell.setPaddingLeft(12);
					// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table20.addCell(cell);
					
					// for convert db date to dmy format
					
					
					/*String expdate=medicineProcurement.get(medicineProcurement.size() - 1).getExpDate().toString().substring(0,10);
					SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
					expdate=toFormat.format(fromFormat.parse(expdate));
				*/
					
					try
					{
						expdate=a.getExpDate().toString().substring(0, 10);
					SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
					expdate=toFormat.format(fromFormat.parse(expdate));
					
					}
					catch(Exception e)
					{
					e.printStackTrace();
					}
					
					cell = new PdfPCell(new Phrase(expdate,redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setPaddingLeft(-10);
					table20.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf((a.getQuantity())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(8);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table20.addCell(cell);
					
					cell = new PdfPCell(new Phrase(String.valueOf((a.getDiscount())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(8);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPaddingLeft(10f);
					table20.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf((a.getMrp())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setPaddingRight(10);
					table20.addCell(cell);
					
					
					cell = new PdfPCell(new Phrase(String.valueOf((a.getGst())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(-15);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table20.addCell(cell);


					cell = new PdfPCell(new Phrase(String.valueOf((a.getAmount())), redFonts));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table20.addCell(cell);

				}
				cell3.setColspan(2);
				table1.setWidthPercentage(100f);
				table20.setWidthPercentage(100f);
				cell3.addElement(table1);  
				cell3.addElement(table20);
				table.addCell(cell3);
				
				
				PdfPCell cell4 = new PdfPCell();

				PdfPTable table4 = new PdfPTable(6);
				table4.setWidths(new float[] { 5f,1f, 5f,8f,1f, 3f });
				table4.setSpacingBefore(10);

				int ttl=(int)Math.round(total);
				PdfPCell hcell2;
				hcell2 = new PdfPCell(new Phrase("Recieved Sum of Rupees",redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(-50f);
				table4.addCell(hcell2);
				
				hcell2 = new PdfPCell(new Phrase(":",redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell2.setPaddingLeft(-40f);
				table4.addCell(hcell2);
				
				hcell2 = new PdfPCell(new Phrase(numberToWordsConverter.convert(ttl) + " Rupees Only",redFont));
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
				
				hcell2 = new PdfPCell(new Phrase(String.valueOf(Math.round(total*100.0)/100.0), redFont));
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

				hcell04 = new PdfPCell(new Phrase("Rounded Off To", redFont));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell04.setPaddingLeft(85f);
				table4.addCell(hcell04);
				
				hcell04 = new PdfPCell(new Phrase(":", redFont));
				hcell04.setBorder(Rectangle.NO_BORDER);
				hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell04.setPaddingRight(-30f);
				table4.addCell(hcell04);
				
			/*	BigDecimal bg=new BigDecimal(total-Math.floor(total));
				bg=bg.setScale(2,RoundingMode.HALF_DOWN);
				float round=bg.floatValue();
				//float rd=Math.nextUp(1f-round);
				float rd=1.00f-round;
				
				if(round<0.50)
				{
					hcell04 = new PdfPCell(new Phrase("-" +round , redFont));
				}
				else
				{
					
					if(String.valueOf(rd).length()>=4)
					{
						hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd).substring(0,4) , redFont));
					}

					else
					{
						hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd) , redFont));	
					}
					
				}
*/				
				hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)) , redFont));
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

				hcell4 = new PdfPCell(new Phrase("Net Amount", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(85f);
				table4.addCell(hcell4);
				
				hcell4 = new PdfPCell(new Phrase(":", redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell4.setPaddingRight(-30f);
				table4.addCell(hcell4);
				
				hcell4 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

				hcell9 = new PdfPCell(new Phrase("Reciept Amount", redFont));
				hcell9.setBorder(Rectangle.NO_BORDER);
				hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell9.setPaddingLeft(85f);
				table4.addCell(hcell9);
				
				hcell9 = new PdfPCell(new Phrase(":", redFont));
				hcell9.setBorder(Rectangle.NO_BORDER);					
				hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell9.setPaddingRight(-30f);
				table4.addCell(hcell9);
				
				hcell9 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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
				table13.setWidths(new float[] { 2f, 3f,3f, 3f, 3f });

				table13.setSpacingBefore(10);

				PdfPCell hcell33;
				hcell33 = new PdfPCell(new Phrase("Pay Mode", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(10f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase("Amount", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(35f);
				table13.addCell(hcell33);
				
				
				hcell33 = new PdfPCell(new Phrase("Card#", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(40f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase("Bank Name", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(40f);
				table13.addCell(hcell33);

				hcell33 = new PdfPCell(new Phrase("Exp Date", redFont1));
				hcell33.setBorder(Rectangle.NO_BORDER);
				hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell33.setPaddingLeft(50f);
				table13.addCell(hcell33);

				PdfPCell hcell34;
				hcell34 = new PdfPCell(new Phrase(sales.getPaymentType(), redFont2));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(10f);
				table13.addCell(hcell34);

				hcell34 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont2));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(35f);
				table13.addCell(hcell34);

				hcell34 = new PdfPCell(new Phrase("", redFont1));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(40f);
				table13.addCell(hcell34);

				hcell34 = new PdfPCell(new Phrase("", redFont1));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(40f);
				table13.addCell(hcell34);

				hcell34 = new PdfPCell(new Phrase("", redFont1));
				hcell34.setBorder(Rectangle.NO_BORDER);
				hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell34.setPaddingLeft(50f);
				table13.addCell(hcell34);

				cell33.setFixedHeight(35f);
				cell33.setColspan(2);
				table13.setWidthPercentage(100f);
				cell33.addElement(table13);
				table.addCell(cell33);

									// for new row end

				PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository
						.findByShopLocation(sales.getLocation());

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

				PdfPCell hcell7;

				hcell7 = new PdfPCell(new Phrase(
						"Instructions  : "
								+ "1) Returns are accepted within TEN(10)Days. \n                       2) Fridge Items once sold cannot be taken Back.",
						redFont));
				hcell7.setBorder(Rectangle.NO_BORDER);
				hcell7.setPaddingLeft(-50f);
				table5.addCell(hcell7);

				hcell7 = new PdfPCell(new Phrase("Pharmacist"));
				hcell7.setBorder(Rectangle.NO_BORDER);
				hcell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell7.setPaddingTop(25f);
				table5.addCell(hcell7);

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
				salesPaymentPdf.setFileName(billNo+"-"+regId+" Medicine Sales");
				salesPaymentPdf.setFileuri(uri);
				salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
				salesPaymentPdf.setData(pdfByte);
				salesPaymentPdfServiceImpl.save(salesPaymentPdf);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		}
		else //for walk-ins
		{
			byte[] pdfByte = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			Document document = new Document(PageSize.A4_LANDSCAPE);
						try {

							Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");

							Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
							Font redFonts = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
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
							pq.add(new Chunk(myAd,redFont));
			                   
							PdfPCell cellp = new PdfPCell(pq);
							PdfPCell cell1 = new PdfPCell();
							
							//for header bold
							PdfPTable table96 = new PdfPTable(1);
							table96.setWidths(new float[] { 5f });
							table96.setSpacingBefore(10);

							PdfPCell hcell96;
							hcell96 = new PdfPCell(new Phrase("UDBHAVA PHARMACY", redFont1));
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
							table2.setWidths(new float[] { 5f,1f,5f });
							table2.setSpacingBefore(10);

							PdfPCell hcell1;
							hcell1 = new PdfPCell(new Phrase("Bill#" , redFont));
							hcell1.setBorder(Rectangle.NO_BORDER);
							hcell1.setPaddingLeft(-15f);
							table2.addCell(hcell1);
							
							hcell1 = new PdfPCell(new Phrase(":" , redFont));
							hcell1.setBorder(Rectangle.NO_BORDER);
							hcell1.setPaddingLeft(-35f);
							table2.addCell(hcell1);
							
							hcell1 = new PdfPCell(new Phrase( sales.getBillNo(), redFont));
							hcell1.setBorder(Rectangle.NO_BORDER);
							hcell1.setPaddingLeft(-25f);
							table2.addCell(hcell1);

							// Display a date in day, month, year format
							Date date = Calendar.getInstance().getTime();
							DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
							String today = formatter.format(date).toString();
							
							
							PdfPCell hcel123;
							hcel123 = new PdfPCell(new Phrase("Bill Date" , redFont));
							hcel123.setBorder(Rectangle.NO_BORDER);
							hcel123.setPaddingLeft(-15f);

							hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
							table2.addCell(hcel123);
							
							hcel123 = new PdfPCell(new Phrase(":" , redFont));
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
							hcell18 = new PdfPCell(new Phrase("Patient Name" ,redFont));
							hcell18.setBorder(Rectangle.NO_BORDER);
							hcell18.setPaddingLeft(-15f);
							table2.addCell(hcell18);
							
							hcell18 = new PdfPCell(new Phrase(":",redFont));
							hcell18.setBorder(Rectangle.NO_BORDER);
							hcell18.setPaddingLeft(-35f);
							table2.addCell(hcell18);
							
							hcell18 = new PdfPCell(new Phrase(sales.getName(),redFont));
							hcell18.setBorder(Rectangle.NO_BORDER);
							hcell18.setPaddingLeft(-25f);
							table2.addCell(hcell18);
							
							
									
							cell0.setFixedHeight(100f);
							cell0.setColspan(2);
							cell0.addElement(table2);
							table.addCell(cell0);				
							
							PdfPCell cell19 = new PdfPCell();

							PdfPTable table21 = new PdfPTable(3);
							table21.setWidths(new float[] { 4f, 4f, 5f });
							table21.setSpacingBefore(10);

							PdfPCell hcell15;
							hcell15 = new PdfPCell(new Phrase("", redFont));
							hcell15.setBorder(Rectangle.NO_BORDER);
							hcell15.setPaddingLeft(-70f);
							table21.addCell(hcell15);

							hcell15 = new PdfPCell(new Phrase("Pharmacy Receipt", redFont3));
							hcell15.setBorder(Rectangle.NO_BORDER);
							hcell15.setPaddingLeft(35);
							hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
							table21.addCell(hcell15);

							hcell15 = new PdfPCell(new Phrase("" , redFont));
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
							table1.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2.8f});

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
							hcell.setPaddingLeft(-15f);
						
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("Batch No", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell.setPaddingLeft(20f);
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("Exp Date", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell.setPaddingRight(-5f);
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("Qty", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell.setPaddingRight(-18);
							table1.addCell(hcell);
							
							hcell = new PdfPCell(new Phrase("Disc(%)", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell.setPaddingRight(-25f);
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("MRP", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell.setPaddingRight(-10f);
							table1.addCell(hcell);

							hcell = new PdfPCell(new Phrase("GST", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell.setPaddingRight(-10f);
							table1.addCell(hcell);
							
							hcell = new PdfPCell(new Phrase("Sale Value", redFont));
							hcell.setBorder(Rectangle.NO_BORDER);
							hcell.setBackgroundColor(BaseColor.GRAY);
							hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table1.addCell(hcell);
							int count = 0;

							PdfPTable table20 = new PdfPTable(10);
							table20.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2f});

							table20.setSpacingBefore(10);

							
							
							for (RefSales a : refSales) {

								MedicineDetails medicineDetails1 = medicineDetailsServiceImpl.findByName(a.getMedicineName());
								List<MedicineProcurement> medicineProcurement=medicineProcurementServiceImpl.findByBatchAndMedicine(a.getBatchNo(), medicineDetails1.getMedicineId());
								PdfPCell cell;

								cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								table20.addCell(cell);

								cell = new PdfPCell(new Phrase(a.getMedicineName(), redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setPaddingLeft(-1);
								//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							    table20.addCell(cell);

								cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								//cell.setPaddingLeft(-30);
								//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							 cell.setPaddingLeft(-15f);
							 table20.addCell(cell);

								cell = new PdfPCell(new Phrase(String.valueOf(a.getBatchNo()), redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								
								//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						   		cell.setPaddingLeft(12);
								// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
								table20.addCell(cell);
								
								// for convert db date to dmy format
								
								
								/*String expdate=medicineProcurement.get(medicineProcurement.size() - 1).getExpDate().toString().substring(0,10);
								SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
								SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
								expdate=toFormat.format(fromFormat.parse(expdate));
							*/
								
								try
								{
									expdate=a.getExpDate().toString().substring(0, 10);
								SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
								SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
								expdate=toFormat.format(fromFormat.parse(expdate));
								
								}
								catch(Exception e)
								{
								e.printStackTrace();
								}
								
								cell = new PdfPCell(new Phrase(expdate,redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell.setPaddingLeft(-10);
								table20.addCell(cell);

								cell = new PdfPCell(new Phrase(String.valueOf((a.getQuantity())), redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setPaddingLeft(8);
								cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								table20.addCell(cell);
								
								cell = new PdfPCell(new Phrase(String.valueOf((a.getDiscount())), redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setPaddingLeft(8);
								cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell.setPaddingLeft(10f);
								table20.addCell(cell);

								cell = new PdfPCell(new Phrase(String.valueOf((a.getMrp())), redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
								cell.setPaddingRight(10);
								table20.addCell(cell);
								
								
								cell = new PdfPCell(new Phrase(String.valueOf((a.getGst())), redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setPaddingLeft(-15);
								cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
								table20.addCell(cell);


								cell = new PdfPCell(new Phrase(String.valueOf((a.getAmount())), redFonts));
								cell.setBorder(Rectangle.NO_BORDER);
								cell.setPaddingLeft(5);
								cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
								table20.addCell(cell);

							}
							cell3.setColspan(2);
							table1.setWidthPercentage(100f);
							table20.setWidthPercentage(100f);
							cell3.addElement(table1);  
							cell3.addElement(table20);
							table.addCell(cell3);							
							PdfPCell cell4 = new PdfPCell();

							PdfPTable table4 = new PdfPTable(6);
							table4.setWidths(new float[] { 5f,1f, 5f,8f,1f, 3f });
							table4.setSpacingBefore(10);

							int ttl=(int)Math.round(total);
							PdfPCell hcell2;
							hcell2 = new PdfPCell(new Phrase("Recieved Sum of Rupees",redFont));
							hcell2.setBorder(Rectangle.NO_BORDER);
							hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell2.setPaddingLeft(-50f);
							table4.addCell(hcell2);
							
							hcell2 = new PdfPCell(new Phrase(":",redFont));
							hcell2.setBorder(Rectangle.NO_BORDER);
							hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell2.setPaddingLeft(-40f);
							table4.addCell(hcell2);
							
							hcell2 = new PdfPCell(new Phrase(numberToWordsConverter.convert(ttl) + " Rupees Only",redFont));
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
							
							hcell2 = new PdfPCell(new Phrase(String.valueOf(Math.round(total*100.0)/100.0), redFont));
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

							hcell04 = new PdfPCell(new Phrase("Rounded Off To", redFont));
							hcell04.setBorder(Rectangle.NO_BORDER);
							hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell04.setPaddingLeft(85f);
							table4.addCell(hcell04);
							
							hcell04 = new PdfPCell(new Phrase(":", redFont));
							hcell04.setBorder(Rectangle.NO_BORDER);
							hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell04.setPaddingRight(-30f);
							table4.addCell(hcell04);
							
						/*	BigDecimal bg=new BigDecimal(total-Math.floor(total));
							bg=bg.setScale(2,RoundingMode.HALF_DOWN);
							float round=bg.floatValue();
							//float rd=Math.nextUp(1f-round);
							float rd=1.00f-round;
							
							if(round<0.50)
							{
								hcell04 = new PdfPCell(new Phrase("-" +round , redFont));
							}
							else
							{
								

								if(String.valueOf(rd).length()>=4)
								{
									hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd).substring(0,4) , redFont));
								}

								else
								{
									hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd) , redFont));
	
								}

							}
*/							
							hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)) , redFont));
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

							hcell4 = new PdfPCell(new Phrase("Net Amount", redFont));
							hcell4.setBorder(Rectangle.NO_BORDER);
							hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell4.setPaddingLeft(85f);
							table4.addCell(hcell4);
							
							hcell4 = new PdfPCell(new Phrase(":", redFont));
							hcell4.setBorder(Rectangle.NO_BORDER);
							hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell4.setPaddingRight(-30f);
							table4.addCell(hcell4);
							
							hcell4 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

							hcell9 = new PdfPCell(new Phrase("Reciept Amount", redFont));
							hcell9.setBorder(Rectangle.NO_BORDER);
							hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell9.setPaddingLeft(85f);
							table4.addCell(hcell9);
							
							hcell9 = new PdfPCell(new Phrase(":", redFont));
							hcell9.setBorder(Rectangle.NO_BORDER);					
							hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell9.setPaddingRight(-30f);
							table4.addCell(hcell9);
							
							hcell9 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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
							table13.setWidths(new float[] { 2f, 3f,3f, 3f, 3f });

							table13.setSpacingBefore(10);

							PdfPCell hcell33;
							hcell33 = new PdfPCell(new Phrase("Pay Mode", redFont1));
							hcell33.setBorder(Rectangle.NO_BORDER);
							hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell33.setPaddingLeft(10f);
							table13.addCell(hcell33);

							hcell33 = new PdfPCell(new Phrase("Amount", redFont1));
							hcell33.setBorder(Rectangle.NO_BORDER);
							hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell33.setPaddingLeft(35f);
							table13.addCell(hcell33);
							
							
							hcell33 = new PdfPCell(new Phrase("Card#", redFont1));
							hcell33.setBorder(Rectangle.NO_BORDER);
							hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell33.setPaddingLeft(40f);
							table13.addCell(hcell33);

							hcell33 = new PdfPCell(new Phrase("Bank Name", redFont1));
							hcell33.setBorder(Rectangle.NO_BORDER);
							hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell33.setPaddingLeft(40f);
							table13.addCell(hcell33);

							hcell33 = new PdfPCell(new Phrase("Exp Date", redFont1));
							hcell33.setBorder(Rectangle.NO_BORDER);
							hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell33.setPaddingLeft(50f);
							table13.addCell(hcell33);

							PdfPCell hcell34;
							hcell34 = new PdfPCell(new Phrase(sales.getPaymentType(), redFont2));
							hcell34.setBorder(Rectangle.NO_BORDER);
							hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell34.setPaddingLeft(10f);
							table13.addCell(hcell34);

							hcell34 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont2));
							hcell34.setBorder(Rectangle.NO_BORDER);
							hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell34.setPaddingLeft(35f);
							table13.addCell(hcell34);

							hcell34 = new PdfPCell(new Phrase("", redFont1));
							hcell34.setBorder(Rectangle.NO_BORDER);
							hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell34.setPaddingLeft(40f);
							table13.addCell(hcell34);

							hcell34 = new PdfPCell(new Phrase("", redFont1));
							hcell34.setBorder(Rectangle.NO_BORDER);
							hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell34.setPaddingLeft(40f);
							table13.addCell(hcell34);

							hcell34 = new PdfPCell(new Phrase("", redFont1));
							hcell34.setBorder(Rectangle.NO_BORDER);
							hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
							hcell34.setPaddingLeft(50f);
							table13.addCell(hcell34);

							cell33.setFixedHeight(35f);
							cell33.setColspan(2);
							table13.setWidthPercentage(100f);
							cell33.addElement(table13);
							table.addCell(cell33);

												// for new row end

							PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository
									.findByShopLocation(sales.getLocation());

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

							PdfPCell hcell7;

							hcell7 = new PdfPCell(new Phrase(
									"Instructions  : "
											+ "1) Returns are accepted within TEN(10)Days. \n                       2) Fridge Items once sold cannot be taken Back.",
									redFont));
							hcell7.setBorder(Rectangle.NO_BORDER);
							hcell7.setPaddingLeft(-50f);
							table5.addCell(hcell7);

							hcell7 = new PdfPCell(new Phrase("Pharmacist",redFont1));
							hcell7.setBorder(Rectangle.NO_BORDER);
							hcell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
							hcell7.setPaddingTop(25f);
							table5.addCell(hcell7);

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
				salesPaymentPdf.setFileName(billNo+" "+"Sales");
				salesPaymentPdf.setFileuri(uri);
				salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
				salesPaymentPdf.setData(pdfByte);
				salesPaymentPdfServiceImpl.save(salesPaymentPdf);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		return salesPaymentPdf;	
		
		
	}
	
	
	@Transactional
	public SalesPaymentPdf wardSave(Sales sales,Principal principal)
	{
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		StringBuilder createdBy=new StringBuilder();
		createdBy=
				(userSecurity.getMiddleName()!=null) ? createdBy.append(userSecurity.getFirstName())
														.append(" ")
														.append(userSecurity.getMiddleName())
														.append(" ")
														.append(userSecurity.getLastName()) :
															createdBy.append(userSecurity.getFirstName())
															.append(" ")
															.append(userSecurity.getLastName()) ;
														
		
		float total=0;
		String wardName="";
		String expdate="";
		String billNo="";
		//shantharam addr

		String myAd=" ";
		
		
		List<RefSales> refSales=sales.getRefSales();
		
		sales.setBillDate(new Timestamp(System.currentTimeMillis()));
		sales.setBillNo(getNextBillNo());
		billNo=sales.getBillNo();
		sales.setUpdatedBy(userSecurity.getUserId());
		sales.setSoldBy(userSecurity.getUserId());
		sales.setPatientSalesUser(userSecurity);
		sales.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
		Location location=locationServiceImpl.findByLocationName(sales.getLocation());
		sales.setPatientSaleslocation(location);	
	
		for(RefSales refSalesList:refSales)
		{
			sales.setSaleNo(getNextSaleNo());
			sales.setAmount(refSalesList.getAmount());
			sales.setBatchNo(refSalesList.getBatchNo());
			sales.setDiscount(refSalesList.getDiscount());
			sales.setGst(refSalesList.getGst());
			sales.setMedicineName(refSalesList.getMedicineName());
			MedicineDetails medicineDetails=medicineDetailsServiceImpl.findByName(refSalesList.getMedicineName());
			sales.setPatientSalesMedicineDetails(medicineDetails);
			sales.setMrp(refSalesList.getMrp());
			sales.setQuantity(refSalesList.getQuantity());
			sales.setActualAmount(refSalesList.getAmount());
			sales.setExpireDate(refSalesList.getExpDate());
			expdate=sales.getExpireDate();
			wardName=sales.getName();
			sales.setCostPrice(refSalesList.getMrp()*refSalesList.getQuantity());
			sales.setPaymentType("NA_WO");
			//for medicine quantity
			sales.setPaid("-");
			total+=sales.getActualAmount();
			MedicineDetails medicineDetailsQuantity = medicineDetailsServiceImpl.findByName(sales.getMedicineName());
			
			MedicineQuantity medicineQuantityInfo = medicineQuantityServiceImpl.findByMedicineDetails(medicineDetailsQuantity);
			
			
			if(medicineQuantityInfo!=null) {
				long totalSold=medicineQuantityInfo.getSold();
				totalSold+=sales.getQuantity();
				medicineQuantityInfo.setSold(totalSold);
				medicineQuantityInfo.setBalance(medicineQuantityInfo.getTotalQuantity()-medicineQuantityInfo.getSold());
				medicineQuantityRepository.save(medicineQuantityInfo);
				
			}
		
			salesRepository.save(sales);
		}
		

		byte[] pdfByte = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		Document document = new Document(PageSize.A4_LANDSCAPE);
		try {

			Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");
			Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
			Font redFonts = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);
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
			pq.add(new Chunk(
			myAd,		redFont));
               
			PdfPCell cellp = new PdfPCell(pq);
			PdfPCell cell1 = new PdfPCell();
			
			//for header bold
			PdfPTable table96 = new PdfPTable(1);
			table96.setWidths(new float[] { 5f });
			table96.setSpacingBefore(10);

			PdfPCell hcell96;
			hcell96 = new PdfPCell(new Phrase(ConstantValues.PHARMACY_NAME, redFont1));
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
			table2.setWidths(new float[] { 5f,1f,5f });
			table2.setSpacingBefore(10);

			PdfPCell hcell1;
			hcell1 = new PdfPCell(new Phrase("Bill#" , redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setPaddingLeft(-15f);
			table2.addCell(hcell1);
			
			hcell1 = new PdfPCell(new Phrase(":" , redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setPaddingLeft(-35f);
			table2.addCell(hcell1);
			
			hcell1 = new PdfPCell(new Phrase( sales.getBillNo(), redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setPaddingLeft(-25f);
			table2.addCell(hcell1);

			// Display a date in day, month, year format
			Date date = Calendar.getInstance().getTime();
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
			String today = formatter.format(date).toString();
			
			
			
			
			PdfPCell hcel123;
			hcel123 = new PdfPCell(new Phrase("Bill Date" , redFont));
			hcel123.setBorder(Rectangle.NO_BORDER);
			hcel123.setPaddingLeft(-15f);

			hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcel123);
			
			hcel123 = new PdfPCell(new Phrase(":" , redFont));
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
			hcell18 = new PdfPCell(new Phrase("Ward Name" ,redFont));
			hcell18.setBorder(Rectangle.NO_BORDER);
			hcell18.setPaddingLeft(-15f);
			table2.addCell(hcell18);
			
			hcell18 = new PdfPCell(new Phrase(":",redFont));
			hcell18.setBorder(Rectangle.NO_BORDER);
			hcell18.setPaddingLeft(-35f);
			table2.addCell(hcell18);
			
			hcell18 = new PdfPCell(new Phrase(
					wardName,
					redFont));
			hcell18.setBorder(Rectangle.NO_BORDER);
			hcell18.setPaddingLeft(-25f);
			table2.addCell(hcell18);
			
			/*
			PdfPCell hcel;

			hcel = new PdfPCell(new Phrase("" , redFont));
			hcel.setBorder(Rectangle.NO_BORDER);
			hcel.setPaddingLeft(-15f);
			table2.addCell(hcel);
			
			hcel = new PdfPCell(new Phrase("" , redFont));
			hcel.setBorder(Rectangle.NO_BORDER);
			hcel.setPaddingLeft(-35f);
			table2.addCell(hcel);
			
			hcel = new PdfPCell(new Phrase("", redFont));
			hcel.setBorder(Rectangle.NO_BORDER);
			hcel.setPaddingLeft(-25f);
			table2.addCell(hcel);
			
			
			PdfPCell hcel11;
			hcel11 = new PdfPCell(new Phrase("" , redFont));
			hcel11.setBorder(Rectangle.NO_BORDER);
			hcel11.setPaddingLeft(-15f);
			hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcel11);
			
			hcel11 = new PdfPCell(new Phrase("" , redFont));
			hcel11.setBorder(Rectangle.NO_BORDER);
			hcel11.setPaddingLeft(-35f);
			hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcel11);
			
			hcel11 = new PdfPCell(new Phrase( "", redFont));
			hcel11.setBorder(Rectangle.NO_BORDER);
			hcel11.setPaddingLeft(-25f);
			hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcel11);
			
			PdfPCell hcel1;

			hcel1 = new PdfPCell(new Phrase("", redFont));
			hcel1.setBorder(Rectangle.NO_BORDER);
			hcel1.setPaddingLeft(-15f);
			table2.addCell(hcel1);
			
			hcel1 = new PdfPCell(new Phrase("" , redFont));
			hcel1.setBorder(Rectangle.NO_BORDER);
			hcel1.setPaddingLeft(-35f);
			table2.addCell(hcel1);
			
			hcel1 = new PdfPCell(new Phrase("" , redFont));
			hcel1.setBorder(Rectangle.NO_BORDER);
			hcel1.setPaddingLeft(-25f);
			table2.addCell(hcel1);
			
*/			
			cell0.setFixedHeight(100f);
			cell0.setColspan(2);
			cell0.addElement(table2);
			table.addCell(cell0);				
			
			PdfPCell cell19 = new PdfPCell();

			PdfPTable table21 = new PdfPTable(3);
			table21.setWidths(new float[] { 4f, 4f, 5f });
			table21.setSpacingBefore(10);

			PdfPCell hcell15;
			hcell15 = new PdfPCell(new Phrase("", redFont));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setPaddingLeft(-70f);
			table21.addCell(hcell15);

			hcell15 = new PdfPCell(new Phrase("Ward Pharmacy Receipt", redFont3));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setPaddingLeft(05);
			hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
			table21.addCell(hcell15);

			hcell15 = new PdfPCell(new Phrase("" , redFont));
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
			table1.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2.8f});

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
			hcell.setPaddingLeft(-15f);
		
			table1.addCell(hcell);

			hcell = new PdfPCell(new Phrase("Batch No", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setBackgroundColor(BaseColor.GRAY);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell.setPaddingLeft(20f);
			table1.addCell(hcell);

			hcell = new PdfPCell(new Phrase("Exp Date", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setBackgroundColor(BaseColor.GRAY);
			hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell.setPaddingRight(-5f);
			table1.addCell(hcell);

			hcell = new PdfPCell(new Phrase("Qty", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setBackgroundColor(BaseColor.GRAY);
			hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell.setPaddingRight(-18);
			table1.addCell(hcell);
			
			hcell = new PdfPCell(new Phrase("Disc(%)", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setBackgroundColor(BaseColor.GRAY);
			hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell.setPaddingRight(-25f);
			table1.addCell(hcell);

			hcell = new PdfPCell(new Phrase("MRP", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setBackgroundColor(BaseColor.GRAY);
			hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell.setPaddingRight(-10f);
			table1.addCell(hcell);

			hcell = new PdfPCell(new Phrase("GST", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setBackgroundColor(BaseColor.GRAY);
			hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell.setPaddingRight(-10f);
			table1.addCell(hcell);
			
			hcell = new PdfPCell(new Phrase("Sale Value", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setBackgroundColor(BaseColor.GRAY);
			hcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table1.addCell(hcell);
			int count = 0;

			PdfPTable table20 = new PdfPTable(10);
			table20.setWidths(new float[] { 1.5f, 5.4f, 4.5f, 3f, 2f, 1.2f, 1.5f, 2f, 1.5f ,2f});

			table20.setSpacingBefore(10);

			
			
			for (RefSales a : refSales) {

				MedicineDetails medicineDetails1 = medicineDetailsServiceImpl.findByName(a.getMedicineName());
				List<MedicineProcurement> medicineProcurement=medicineProcurementServiceImpl.findByBatchAndMedicine(a.getBatchNo(), medicineDetails1.getMedicineId());
				PdfPCell cell;

				cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table20.addCell(cell);

				cell = new PdfPCell(new Phrase(a.getMedicineName(), redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(-1);
				//cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			    table20.addCell(cell);

				cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
			 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			 cell.setPaddingLeft(-15f);
			 table20.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf(a.getBatchNo()), redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
				
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		   		cell.setPaddingLeft(12);
				table20.addCell(cell);
				
				
				try
				{
					expdate=a.getExpDate().toString().substring(0, 10);
				SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
				expdate=toFormat.format(fromFormat.parse(expdate));
				
				}
				catch(Exception e)
				{
				e.printStackTrace();
				}
				
				cell = new PdfPCell(new Phrase(expdate,redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setPaddingLeft(-10);
				table20.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf((a.getQuantity())), redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(8);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table20.addCell(cell);
				
				cell = new PdfPCell(new Phrase(String.valueOf((a.getDiscount())), redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(8);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setPaddingLeft(10f);
				table20.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf((a.getMrp())), redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setPaddingRight(10);
				table20.addCell(cell);
				
				
				cell = new PdfPCell(new Phrase(String.valueOf((a.getGst())), redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(-15);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table20.addCell(cell);


				cell = new PdfPCell(new Phrase(String.valueOf((a.getAmount())), redFonts));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(5);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table20.addCell(cell);

			}
			cell3.setColspan(2);
			table1.setWidthPercentage(100f);
			table20.setWidthPercentage(100f);
			cell3.addElement(table1);  
			cell3.addElement(table20);
			table.addCell(cell3);
			
			
			PdfPCell cell4 = new PdfPCell();

			PdfPTable table4 = new PdfPTable(6);
			table4.setWidths(new float[] { 5f,1f, 5f,8f,1f, 3f });
			table4.setSpacingBefore(10);

			PdfPCell hcell2;
			hcell2 = new PdfPCell(new Phrase("",redFont));
			hcell2.setBorder(Rectangle.NO_BORDER);
			hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2.setPaddingLeft(-50f);
			table4.addCell(hcell2);
			
			hcell2 = new PdfPCell(new Phrase("",redFont));
			hcell2.setBorder(Rectangle.NO_BORDER);
			hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2.setPaddingLeft(-40f);
			table4.addCell(hcell2);
			
			hcell2 = new PdfPCell(new Phrase("",redFont));
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
			
		
			hcell2 = new PdfPCell(new Phrase(String.valueOf(Math.round(total*100.0)/100.0), redFont));
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

			hcell04 = new PdfPCell(new Phrase("Rounded Off To", redFont));
			hcell04.setBorder(Rectangle.NO_BORDER);
			hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell04.setPaddingLeft(85f);
			table4.addCell(hcell04);
			
			hcell04 = new PdfPCell(new Phrase(":", redFont));
			hcell04.setBorder(Rectangle.NO_BORDER);
			hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell04.setPaddingRight(-30f);
			table4.addCell(hcell04);
			
			hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)) , redFont));
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

			hcell4 = new PdfPCell(new Phrase("Net Amount", redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(85f);
			table4.addCell(hcell4);
			
			hcell4 = new PdfPCell(new Phrase(":", redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell4.setPaddingRight(-30f);
			table4.addCell(hcell4);
			
			hcell4 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

			hcell9 = new PdfPCell(new Phrase("", redFont));
			hcell9.setBorder(Rectangle.NO_BORDER);
			hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell9.setPaddingLeft(85f);
			table4.addCell(hcell9);
			
			hcell9 = new PdfPCell(new Phrase("", redFont));
			hcell9.setBorder(Rectangle.NO_BORDER);					
			hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell9.setPaddingRight(-30f);
			table4.addCell(hcell9);
			
			hcell9 = new PdfPCell(new Phrase("", redFont));
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
			table13.setWidths(new float[] { 2f, 3f,3f, 3f, 3f });

			table13.setSpacingBefore(10);

			PdfPCell hcell33;
			hcell33 = new PdfPCell(new Phrase("Pay Mode", redFont1));
			hcell33.setBorder(Rectangle.NO_BORDER);
			hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell33.setPaddingLeft(10f);
			table13.addCell(hcell33);

			hcell33 = new PdfPCell(new Phrase("Amount", redFont1));
			hcell33.setBorder(Rectangle.NO_BORDER);
			hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell33.setPaddingLeft(35f);
			table13.addCell(hcell33);
			
			
			hcell33 = new PdfPCell(new Phrase("Card#", redFont1));
			hcell33.setBorder(Rectangle.NO_BORDER);
			hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell33.setPaddingLeft(40f);
			table13.addCell(hcell33);

			hcell33 = new PdfPCell(new Phrase("Bank Name", redFont1));
			hcell33.setBorder(Rectangle.NO_BORDER);
			hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell33.setPaddingLeft(40f);
			table13.addCell(hcell33);

			hcell33 = new PdfPCell(new Phrase("Exp Date", redFont1));
			hcell33.setBorder(Rectangle.NO_BORDER);
			hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell33.setPaddingLeft(50f);
			table13.addCell(hcell33);

			PdfPCell hcell34;
			hcell34 = new PdfPCell(new Phrase(sales.getPaymentType(), redFont2));
			hcell34.setBorder(Rectangle.NO_BORDER);
			hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell34.setPaddingLeft(10f);
			table13.addCell(hcell34);

			hcell34 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont2));
			hcell34.setBorder(Rectangle.NO_BORDER);
			hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell34.setPaddingLeft(35f);
			table13.addCell(hcell34);

			hcell34 = new PdfPCell(new Phrase("", redFont1));
			hcell34.setBorder(Rectangle.NO_BORDER);
			hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell34.setPaddingLeft(40f);
			table13.addCell(hcell34);

			hcell34 = new PdfPCell(new Phrase("", redFont1));
			hcell34.setBorder(Rectangle.NO_BORDER);
			hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell34.setPaddingLeft(40f);
			table13.addCell(hcell34);

			hcell34 = new PdfPCell(new Phrase("", redFont1));
			hcell34.setBorder(Rectangle.NO_BORDER);
			hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell34.setPaddingLeft(50f);
			table13.addCell(hcell34);

			cell33.setFixedHeight(35f);
			cell33.setColspan(2);
			table13.setWidthPercentage(100f);
			cell33.addElement(table13);
			table.addCell(cell33);

								// for new row end

			PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository
					.findByShopLocation(sales.getLocation());

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

			PdfPCell hcell7;

			hcell7 = new PdfPCell(new Phrase(
					"Instructions  : "
							+ "1) Returns are accepted within TEN(10)Days. \n                       2) Fridge Items once sold cannot be taken Back.",
					redFont));
			hcell7.setBorder(Rectangle.NO_BORDER);
			hcell7.setPaddingLeft(-50f);
			table5.addCell(hcell7);

			hcell7 = new PdfPCell(new Phrase("Pharmacist"));
			hcell7.setBorder(Rectangle.NO_BORDER);
			hcell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell7.setPaddingTop(25f);
			table5.addCell(hcell7);

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
			salesPaymentPdf.setFileName(billNo+" Medicine Sales Ward Online Issue");
			salesPaymentPdf.setFileuri(uri);
			salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
			salesPaymentPdf.setData(pdfByte);
			salesPaymentPdfServiceImpl.save(salesPaymentPdf);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return salesPaymentPdf;
		
		
	}
	
	public List<Sales> findByPatientRegistration(PatientRegistration patientRegistration)
	{
		return salesRepository.findByPatientRegistration(patientRegistration);
	}
	
	public List<Sales> findByBillNo(String id)
	{
		return salesRepository.findByBillNo(id);
	}
	
	/*
	 * Modified code for sales
	 */
	public List<Map<String, String>> findMedicineDetailsModified(List<Map<String,String>> medicine)
	{
		List<Map<String,String>> info=new ArrayList<>();
		
		
		List<Map<String,String>> med=medicine;
		for(int i=0;i<med.size();i++)
		{
			Map<String,String> lis=new HashMap<>();
			System.out.println(med.get(i).get("med"));
			System.out.println(med.get(i).get("quantity"));
			MedicineDetails medicineDetails=medicineDetailsRepository.findByName(med.get(i).get("med"));
			List<MedicineProcurement> medicineProcurement= medicineDetails.getMedicineProcurement();
			
			float cost=medicineProcurement.get(medicineProcurement.size()-1).getMrp()*Float.parseFloat(med.get(i).get("quantity"));
			lis.put("total",String.valueOf(cost));
			lis.put("med", medicineDetails.getName());
			lis.put("batch",medicineProcurement.get(medicineProcurement.size()-1).getBatch());
			lis.put("mrp",String.valueOf(medicineProcurement.get(medicineProcurement.size()-1).getMrp()));
			info.add(lis);
			
		}
		
		
		return info;
	}
	
	public RefSalesIds findMedicineDetails(String medicine)
	{
		MedicineDetails medicineDetails=medicineDetailsRepository.findByName(medicine);
		refSalesIds.setBatch(medicineDetails.getBatchNo());
		List<MedicineProcurement> medicineProcurement= medicineDetails.getMedicineProcurement();
		for(MedicineProcurement medicineProcurementL:medicineProcurement)
		{
			System.out.println(medicineProcurementL);
		}
		refSalesIds.setExpDate(medicineProcurement.get(medicineProcurement.size()-1).getExpDate());
		refSalesIds.setMrp(medicineProcurement.get(medicineProcurement.size()-1).getMrp());
		return refSalesIds;		
	}
	
	public List<Object> getBillIds()
	{
		List<Object> displayList=new ArrayList<>();
		Iterable<MedicineDetails> medicineDetails=medicineDetailsServiceImpl.findAll();
		for(MedicineDetails medicineDetailsInfo:medicineDetails)
		{
			List<MedicineProcurement> medicineProcurements=medicineProcurementServiceImpl.findOneApproved(medicineDetailsInfo);
			
			if(!medicineProcurements.isEmpty())
			{	
				RefSalesIds refSalesIds=new RefSalesIds();
				refSalesIds.setBillNo(salesServiceImpl.getNextBillNo());
				Timestamp timestamp=new Timestamp(System.currentTimeMillis());
				refSalesIds.setDate(timestamp.toString().substring(0, 10));
				refSalesIds.setMedName(medicineDetailsInfo.getName());
				refSalesIds.setBatch(medicineDetailsInfo.getBatchNo());
				refSalesIds.setExpDate(medicineProcurements.get(medicineProcurements.size()-1).getExpDate());
				refSalesIds.setMrp(medicineProcurements.get(medicineProcurements.size()-1).getMrp());
				refSalesIds.setGst(medicineProcurements.get(medicineProcurements.size()-1).getTax());
				displayList.add(refSalesIds);
			}
		}
		return displayList;
	}
	
	public List<Object> getBillId()
	{
		List<Object> displayList=new ArrayList<>();
		RefSalesIds refSalesIds=new RefSalesIds();
		refSalesIds.setBillNo(salesServiceImpl.getNextBillNo());
		Timestamp timestamp=new Timestamp(System.currentTimeMillis());
		refSalesIds.setDate(timestamp.toString().substring(0, 10));
		displayList.add(refSalesIds);
			
		
		return displayList;
	}
	
	
 	public List<Sales> findByBatchAndMedicine(String batch,String medicine)
 	{
 		return salesRepository.findByBatchAndMedicine(batch, medicine);
 	}
 	
 	public 	List<Sales> findByName(String medName)
 	{
 		return salesRepository.findByName(medName);
 	}

	@Override
	public List<Sales> findByPaymentTypeAndPatientRegistration(String payment, PatientRegistration reg) {
		return salesRepository.findByPaymentTypeAndPatientRegistration(payment, reg);
	}
	
	// find due bill
	public List<Sales> findByPatientRegistrationAndPaymentType(PatientRegistration patientRegistration,String paymentType)
	{
		return salesRepository.findByPatientRegistrationAndPaymentType(patientRegistration, paymentType);
	}
	
public List<Object> getPreviousDaySales(){
		
		List<Object> list=new ArrayList<Object>();
		
		List<Sales> salesList=salesRepository.getPreviousDaySales();
		for(Sales salesListInfo:salesList) {
			Map<String, String> map=new HashMap<String, String>();
			map.put("SaleId",salesListInfo.getSaleNo() );
			map.put("billNo", salesListInfo.getBillNo());
			map.put("regId", salesListInfo.getPatientRegistration().getRegId());
			map.put("umr", salesListInfo.getUmr());
			map.put("medName",salesListInfo.getMedicineName() );
			map.put("count", String.valueOf(salesListInfo.getQuantity()));
			map.put("sellingPrice", String.valueOf(salesListInfo.getMrp()));
			map.put("paymentType", salesListInfo.getPaymentType());
			map.put("discount", String.valueOf(salesListInfo.getDiscount()));
			map.put("amount", String.valueOf(salesListInfo.getAmount()));
			map.put("totalAmount", String.valueOf(salesListInfo.getCostPrice()));
			map.put("patientType", salesListInfo.getPatientRegistration().getpType());
			map.put("date", String.valueOf(salesListInfo.getBillDate()));
			map.put("name", salesListInfo.getName());
			list.add(map);
			
			
		}
		
		return	list;
		}


public void updateMedDetails(Sales sales,Principal principal,String billNo) 
{

	User userSecurity=userServiceImpl.findByUserName(principal.getName());
	String createdBy=(userSecurity.getMiddleName()!=null) ? userSecurity.getFirstName()+" "+
														userSecurity.getMiddleName()+" "+
														userSecurity.getLastName()       :
																							userSecurity.getFirstName()+" "+
																							userSecurity.getLastName();
	
	Sales salesPrev=null;
	MedicineDetails newMedicineDetails=null;
	MedicineDetails prevMedicineDetails=null;
	PatientRegistration patientRegistration=null;
	float newAmount=0;
	String newSaleId="";
	String newMedName="";
	long newQuantity=0;
	String newBatchNo="";
	float newDiscount=0;
	String newPaymentType="";
	String newExpireDate="";
	float newGst=0;
	float newMrp=0;
	String patientName="";
	String umr="";
	String regId="";
	String location=null;
	
	
	
	
	List<Map<String,String>> addMedicine= sales.getAddMedDetails();
	for(Map<String,String> addMedicineInfo:addMedicine)
	{
		newAmount=Float.valueOf(addMedicineInfo.get("amount"));
		newSaleId=addMedicineInfo.get("saleId");
		newMedName=addMedicineInfo.get("medicineName");
		newQuantity=Long.valueOf(addMedicineInfo.get("quantity"));
		newBatchNo=addMedicineInfo.get("batchNo");
		newDiscount=Float.valueOf(addMedicineInfo.get("discount"));
		newPaymentType=addMedicineInfo.get("paymentMode");
		newExpireDate=addMedicineInfo.get("expiryDate");
		newGst=Float.valueOf(addMedicineInfo.get("gst"));
		newMrp=Float.valueOf(addMedicineInfo.get("mrp"));
		location=addMedicineInfo.get("location");
		
		long finalCash=0; //final billing
		long finalCard=0; //final billing
		long finalCheque=0; //final billing
		long finalDue=0; //final billing
		long netAmount=0;
		long finalNetAmount=0;
		
		
		
		salesPrev= salesRepository.findBySaleNo(newSaleId);
		prevMedicineDetails=salesPrev.getPatientSalesMedicineDetails();
		newMedicineDetails=medicineDetailsRepository.findByName(newMedName);
		salesPrev.setPatientSalesMedicineDetails(newMedicineDetails);
		salesPrev.setAmount(newAmount);
		salesPrev.setMedicineName(newMedName);
		salesPrev.setQuantity(newQuantity);
		salesPrev.setBatchNo(newBatchNo);
		salesPrev.setDiscount(newDiscount);
		salesPrev.setPaymentType(newPaymentType);
		salesPrev.setExpireDate(newExpireDate);
		salesPrev.setGst(newGst);
		salesPrev.setMrp(newMrp);
		salesPrev.setLocation(location);
		
		
		// Updating medicine quantity for previous medicine
		MedicineQuantity prevMedicineQuantityInfo=medicineQuantityServiceImpl.findByMedicineDetails(prevMedicineDetails);
		prevMedicineQuantityInfo.setBalance(prevMedicineQuantityInfo.getBalance()+salesPrev.getQuantity());
		prevMedicineQuantityInfo.setSold(prevMedicineQuantityInfo.getSold()-salesPrev.getQuantity());
		medicineQuantityRepository.save(prevMedicineQuantityInfo);
		
		
		// Updating medicine quantity for previous medicine
		MedicineQuantity newMedicineQuantityInfo=medicineQuantityServiceImpl.findByMedicineDetails(newMedicineDetails);
		newMedicineQuantityInfo.setBalance(newMedicineQuantityInfo.getBalance()+newQuantity);
		newMedicineQuantityInfo.setSold(newMedicineQuantityInfo.getSold()-newQuantity);
		medicineQuantityRepository.save(newMedicineQuantityInfo);
		
		
		
		
		if(salesPrev.getPatientRegistration()!=null)
		{	
			patientRegistration=salesPrev.getPatientRegistration();
			regId=patientRegistration.getRegId();
			umr=patientRegistration.getPatientDetails().getUmr();
	
			if(patientRegistration.getPatientDetails().getMiddleName()!=null)
			{
				patientName=	patientRegistration.getPatientDetails().getTitle()+". "+
						patientRegistration.getPatientDetails().getFirstName() + " "
						+patientRegistration.getPatientDetails().getMiddleName()+" "
						+ patientRegistration.getPatientDetails().getLastName();
						
			}
			else
			{
				patientName=	patientRegistration.getPatientDetails().getTitle()+". "+
						patientRegistration.getPatientDetails().getFirstName() + " "
						+ patientRegistration.getPatientDetails().getLastName();
						
			}

			
			
			System.out.println(billNo);
			System.out.println(prevMedicineDetails);
			PatientSales patientSales=patientSalesRepository.findBySalesBillNoAndPatientSalesMedicineDetails(billNo, prevMedicineDetails);
			System.out.println("patient sales--------------------------------------------"+patientSales);
			System.out.println("patient sales--------------------------------------------"+billNo);
			System.out.println("patient sales--------------------------------------------"+prevMedicineDetails);
			patientSales.setAmount(newAmount);
			patientSales.setBatchNo(newBatchNo);
			patientSales.setSalesBillNo(billNo);
			patientSales.setDiscount(newDiscount);
			patientSales.setExpireDate(newExpireDate);
			patientSales.setGst(newGst);
			patientSales.setMedicineName(newMedName);
			patientSales.setPatientSalesMedicineDetails(newMedicineDetails);
			patientSales.setQuantity(newQuantity);
			patientSales.setPaymentType(newPaymentType);
			patientSales.setMrp(newMrp);
			
			if(salesPrev.getPatientRegistration().getpType().equals(ConstantValues.INPATIENT) && newPaymentType.equalsIgnoreCase("Advance") || newPaymentType.equalsIgnoreCase(ConstantValues.DUE))
			{
				patientSales.setPaid(ConstantValues.NO);
				salesPrev.setPaid(ConstantValues.NO);
				
				ChargeBill chargeBillPrev = chargeBillServiceImpl.findBySaleId(salesPrev);
				chargeBillPrev.setMrp(newMrp);
				chargeBillPrev.setAmount(newAmount);
				chargeBillPrev.setDiscount(newDiscount);
				chargeBillPrev.setPaid(ConstantValues.NO);
				chargeBillPrev.setQuantity(newQuantity);
				chargeBillPrev.setNetAmount(newAmount);
				chargeBillPrev.setAmount(newQuantity*newMrp);
				chargeBillPrev.setPaymentType(newPaymentType);
				chargeBillPrev.setInsertedBy(userSecurity.getUserId());
				chargeBillRepository.save(chargeBillPrev);
			}
			else if(salesPrev.getPatientRegistration().getpType().equals(ConstantValues.INPATIENT) && !newPaymentType.equalsIgnoreCase("Advance") || !newPaymentType.equalsIgnoreCase(ConstantValues.DUE))
			{
				
				patientSales.setPaid(ConstantValues.YES);
				salesPrev.setPaid(ConstantValues.YES);
				
				ChargeBill chargeBillPrev = chargeBillServiceImpl.findBySaleId(salesPrev);
				chargeBillPrev.setMrp(newMrp);
				chargeBillPrev.setAmount(newAmount);
				chargeBillPrev.setDiscount(newDiscount);
				chargeBillPrev.setPaid(ConstantValues.YES);
				chargeBillPrev.setQuantity(newQuantity);
				chargeBillPrev.setNetAmount(newAmount);
				chargeBillPrev.setAmount(newQuantity*newMrp);
				chargeBillPrev.setPaymentType(newPaymentType);
				chargeBillPrev.setInsertedBy(userSecurity.getUserId());
				chargeBillRepository.save(chargeBillPrev);
			
			}
			else
			{
				salesPrev.setPaid(ConstantValues.YES);
				patientSales.setPaid(ConstantValues.YES);
			}
			
			patientSalesRepository.save(patientSales);
	}
		salesRepository.save(salesPrev);
		
		
		
		// Cash + Card
			
			if(newPaymentType.equalsIgnoreCase(ConstantValues.CASH))
			{
				finalCash=finalNetAmount;
			}
			else if(newPaymentType.equalsIgnoreCase(ConstantValues.CARD))
			{
				finalCard=finalNetAmount;
			}
			else if(newPaymentType.equalsIgnoreCase(ConstantValues.CHEQUE))
			{
				finalCheque=finalNetAmount;
			}
			else if(newPaymentType.equalsIgnoreCase(ConstantValues.DUE))
			{
				finalDue=finalNetAmount;
			}
			
			
			if(newPaymentType.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD))
			{
				int cashAmount=0;
				int cardAmount=0;
				int chequeAmount=0;
				CashPlusCard cashPlusCardLab=new CashPlusCard();
				cashPlusCardLab.setInsertedBy(userSecurity.getUserId());
				List<Map<String,String>> multiMode=sales.getMultimode();
				for(Map<String,String> multiModeInfo:multiMode)
				{
					if(multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CASH))
					{
						cashAmount=Integer.parseInt(multiModeInfo.get("amount"));
						finalCash=Long.parseLong(multiModeInfo.get("amount"));
					}
					else if(multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CARD))
					{
						cardAmount=Integer.parseInt(multiModeInfo.get("amount"));
						finalCard=Long.parseLong(multiModeInfo.get("amount"));
					}
					else if(multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CHEQUE))
					{
						chequeAmount=Integer.parseInt(multiModeInfo.get("amount"));
						finalCheque=Long.parseLong(multiModeInfo.get("amount"));
					}
					
				}
				cashPlusCardLab.setInsertedDate(new Timestamp(System.currentTimeMillis()));
				cashPlusCardLab.setDescription("Sales");
				cashPlusCardLab.setPatientRegistrationCashCard((patientRegistration!=null) ? patientRegistration : null);
				cashPlusCardLab.setCardAmount(cardAmount);
				cashPlusCardLab.setCashAmount(cashAmount);
				cashPlusCardLab.setBillNo(sales.getBillNo());
				cashPlusCardLab.setChequeAmount(chequeAmount);
				cashPlusCardServiceImpl.save(cashPlusCardLab);
				
				
				
			}
			
			if(patientRegistration.getpType().equalsIgnoreCase(ConstantValues.INPATIENT))
			{
				//Final Billing  
				 FinalBilling finalBilling=new FinalBilling();
				 finalBilling.setBillNo(billNo);
				 finalBilling.setBillType("Sales");
				 finalBilling.setCardAmount(finalCard);
				 finalBilling.setCashAmount(finalCash);
				 finalBilling.setChequeAmount(finalCheque);
				 finalBilling.setDueAmount(finalDue);
				 finalBilling.setFinalAmountPaid(finalNetAmount);
				 finalBilling.setFinalBillUser(userSecurity);
				 finalBilling.setName(patientName);
				 finalBilling.setRegNo(regId);
				 finalBilling.setPaymentType(newPaymentType);
				 finalBilling.setTotalAmount(finalNetAmount);
				 finalBilling.setUmrNo(umr);
				finalBillingServcieImpl.computeSave(finalBilling);
			}
		
	
	}
	 
	PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository.findByShopLocation(location);
	
	
//------------------------------Pdf code---------------------------------
	
	
	//To find total amount of bill
	
			float total=0.0f;
			List<Sales> listSales=findByBillNo(billNo);
			for(Sales listSalesInfo:listSales)
			{
				total+=listSalesInfo.getAmount();
			}
			
			String roundOff=null;

			//shantharam addr

			String myAd="";

			
			
		
			if (patientRegistration != null) {
				
				if (!patientRegistration.getpType().equals(ConstantValues.INPATIENT)) {
					
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
						pq.add(new Chunk(
						myAd,		redFont));
		                   
						PdfPCell cellp = new PdfPCell(pq);
						PdfPCell cell1 = new PdfPCell();
						
						//for header bold
						PdfPTable table96 = new PdfPTable(1);
						table96.setWidths(new float[] { 5f });
						table96.setSpacingBefore(10);

						PdfPCell hcell96;
						hcell96 = new PdfPCell(new Phrase(ConstantValues.PHARMACY_NAME, redFont1));
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
						table2.setWidths(new float[] { 5f,1f,5f });
						table2.setSpacingBefore(10);

						PdfPCell hcell1;
						hcell1 = new PdfPCell(new Phrase("Bill#" , redFont));
						hcell1.setBorder(Rectangle.NO_BORDER);
						hcell1.setPaddingLeft(-15f);
						table2.addCell(hcell1);
						
						hcell1 = new PdfPCell(new Phrase(":" , redFont));
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
						
						String expdate=null;
						
						
						PdfPCell hcel123;
						hcel123 = new PdfPCell(new Phrase("Bill Date" , redFont));
						hcel123.setBorder(Rectangle.NO_BORDER);
						hcel123.setPaddingLeft(-15f);

						hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
						table2.addCell(hcel123);
						
						hcel123 = new PdfPCell(new Phrase(":" , redFont));
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
						hcell18 = new PdfPCell(new Phrase("Patient Name" ,redFont));
						hcell18.setBorder(Rectangle.NO_BORDER);
						hcell18.setPaddingLeft(-15f);
						table2.addCell(hcell18);
						
						hcell18 = new PdfPCell(new Phrase(":",redFont));
						hcell18.setBorder(Rectangle.NO_BORDER);
						hcell18.setPaddingLeft(-35f);
						table2.addCell(hcell18);
						
						hcell18 = new PdfPCell(new Phrase(
								patientName,
								redFont));
						hcell18.setBorder(Rectangle.NO_BORDER);
						hcell18.setPaddingLeft(-25f);
						table2.addCell(hcell18);
						
						
						PdfPCell hcel;

						hcel = new PdfPCell(new Phrase("UMR No" , redFont));
						hcel.setBorder(Rectangle.NO_BORDER);
						hcel.setPaddingLeft(-15f);
						table2.addCell(hcel);
						
						hcel = new PdfPCell(new Phrase(":" , redFont));
						hcel.setBorder(Rectangle.NO_BORDER);
						hcel.setPaddingLeft(-35f);
						table2.addCell(hcel);
						
						hcel = new PdfPCell(new Phrase(umr, redFont));
						hcel.setBorder(Rectangle.NO_BORDER);
						hcel.setPaddingLeft(-25f);
						table2.addCell(hcel);
						
						
						PdfPCell hcel11;
						hcel11 = new PdfPCell(new Phrase("P.RegNo" , redFont));
						hcel11.setBorder(Rectangle.NO_BORDER);
						hcel11.setPaddingLeft(-15f);
						hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
						table2.addCell(hcel11);
						
						hcel11 = new PdfPCell(new Phrase(":" , redFont));
						hcel11.setBorder(Rectangle.NO_BORDER);
						hcel11.setPaddingLeft(-35f);
						hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
						table2.addCell(hcel11);
						
						hcel11 = new PdfPCell(new Phrase( regId, redFont));
						hcel11.setBorder(Rectangle.NO_BORDER);
						hcel11.setPaddingLeft(-25f);
						hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
						table2.addCell(hcel11);
						
						PdfPCell hcel1;

						hcel1 = new PdfPCell(new Phrase("Doctor Name", redFont));
						hcel1.setBorder(Rectangle.NO_BORDER);
						hcel1.setPaddingLeft(-15f);
						table2.addCell(hcel1);
						
						hcel1 = new PdfPCell(new Phrase(":" , redFont));
						hcel1.setBorder(Rectangle.NO_BORDER);
						hcel1.setPaddingLeft(-35f);
						table2.addCell(hcel1);
						
						hcel1 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getConsultant() , redFont));
						hcel1.setBorder(Rectangle.NO_BORDER);
						hcel1.setPaddingLeft(-25f);
						table2.addCell(hcel1);
						
						
						cell0.setFixedHeight(100f);
						cell0.setColspan(2);
						cell0.addElement(table2);
						table.addCell(cell0);				
						
						PdfPCell cell19 = new PdfPCell();

						PdfPTable table21 = new PdfPTable(3);
						table21.setWidths(new float[] { 4f, 4f, 5f });
						table21.setSpacingBefore(10);

						PdfPCell hcell15;
						hcell15 = new PdfPCell(new Phrase("", redFont));
						hcell15.setBorder(Rectangle.NO_BORDER);
						hcell15.setPaddingLeft(-70f);
						table21.addCell(hcell15);

						hcell15 = new PdfPCell(new Phrase("Pharmacy Receipt", redFont3));
						hcell15.setBorder(Rectangle.NO_BORDER);
						hcell15.setPaddingLeft(35);
						hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
						table21.addCell(hcell15);

						hcell15 = new PdfPCell(new Phrase("" , redFont));
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
						table1.setWidths(new float[] { 1.5f, 5f, 5f, 2f, 3f, 2f, 2f, 2f, 2f ,2f});

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
						
						hcell = new PdfPCell(new Phrase("Disc(%)", redFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(hcell);

						hcell = new PdfPCell(new Phrase("MRP", redFont));
						hcell.setBorder(Rectangle.NO_BORDER);
						hcell.setBackgroundColor(BaseColor.GRAY);
						hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(hcell);

						hcell = new PdfPCell(new Phrase("GST", redFont));
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

						for (Map<String,String> a : addMedicine) {
							

							MedicineDetails medicineDetails1 = medicineDetailsServiceImpl.findByName(a.get("medicineName"));
							List<MedicineProcurement> medicineProcurement=medicineProcurementServiceImpl.findByBatchAndMedicine(a.get("batchNo"), medicineDetails1.getMedicineId());
							PdfPCell cell;

							cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table1.addCell(cell);

							cell = new PdfPCell(new Phrase(a.get("medicineName"), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setPaddingLeft(5);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							table1.addCell(cell);

							cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setPaddingLeft(5);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							table1.addCell(cell);

							cell = new PdfPCell(new Phrase(String.valueOf(a.get("batchNo")), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setPaddingLeft(5);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
							table1.addCell(cell);
							
							// for convert db date to dmy format
							
							
							/*String expdate=medicineProcurement.get(medicineProcurement.size() - 1).getExpDate().toString().substring(0,10);
							SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
							SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
							expdate=toFormat.format(fromFormat.parse(expdate));
						*/
							
							try
							{
							expdate=a.get("expiryDate").toString().substring(0, 10);
							SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
							SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
							expdate=toFormat.format(fromFormat.parse(expdate));
							
							}
							catch(Exception e)
							{
								Logger.error(e.getMessage());
							}
							
							cell = new PdfPCell(new Phrase(expdate,redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setPaddingLeft(5);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table1.addCell(cell);

							cell = new PdfPCell(new Phrase(String.valueOf(a.get("quantity")), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setPaddingLeft(5);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table1.addCell(cell);
							cell = new PdfPCell(new Phrase(String.valueOf(a.get("discount")), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setPaddingLeft(5);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table1.addCell(cell);

							cell = new PdfPCell(new Phrase(String.valueOf(a.get("mrp")), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setPaddingLeft(5);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table1.addCell(cell);
							
							
							cell = new PdfPCell(new Phrase(String.valueOf(a.get("gst")), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setPaddingLeft(5);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table1.addCell(cell);


							cell = new PdfPCell(new Phrase(String.valueOf(a.get("amount")), redFont));
							cell.setBorder(Rectangle.NO_BORDER);
							cell.setPaddingLeft(5);
							cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							table1.addCell(cell);

						}
						cell3.setColspan(2);
						table1.setWidthPercentage(100f);
						cell3.addElement(table1);
						table.addCell(cell3);
						
						PdfPCell cell4 = new PdfPCell();

						PdfPTable table4 = new PdfPTable(6);
						table4.setWidths(new float[] { 5f,1f, 5f,8f,1f, 3f });
						table4.setSpacingBefore(10);

						int ttl=(int)Math.round(total);
						PdfPCell hcell2;
						hcell2 = new PdfPCell(new Phrase("Recieved Sum of Rupees",redFont));
						hcell2.setBorder(Rectangle.NO_BORDER);
						hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell2.setPaddingLeft(-50f);
						table4.addCell(hcell2);
						
						hcell2 = new PdfPCell(new Phrase(":",redFont));
						hcell2.setBorder(Rectangle.NO_BORDER);
						hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell2.setPaddingLeft(-40f);
						table4.addCell(hcell2);
						
						hcell2 = new PdfPCell(new Phrase(numberToWordsConverter.convert(ttl) + " Rupees Only",redFont));
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
						
						hcell2 = new PdfPCell(new Phrase(String.valueOf(Math.round(total*100.0)/100.0), redFont));
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

						hcell04 = new PdfPCell(new Phrase("Rounded Off To", redFont));
						hcell04.setBorder(Rectangle.NO_BORDER);
						hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell04.setPaddingLeft(85f);
						table4.addCell(hcell04);
						
						hcell04 = new PdfPCell(new Phrase(":", redFont));
						hcell04.setBorder(Rectangle.NO_BORDER);
						hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell04.setPaddingRight(-30f);
						table4.addCell(hcell04);
						
						/*BigDecimal bg=new BigDecimal(total-Math.floor(total));
						bg=bg.setScale(2,RoundingMode.HALF_DOWN);
						float round=bg.floatValue();
						//float rd=Math.nextUp(1.0f-round);
						float rd=1.00f-round;
						
						if(round<0.50)
						{
							hcell04 = new PdfPCell(new Phrase("-" +round, redFont));
						}
						else
						{
							if(String.valueOf(rd).length()>=4)
							{
							hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd).substring(0, 4)  , redFont));
							}
							else
							{
								hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd+"/"+round) , redFont));
								
								
							}
						}
						*/
						
						hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)) , redFont));
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

						hcell4 = new PdfPCell(new Phrase("Net Amount", redFont));
						hcell4.setBorder(Rectangle.NO_BORDER);
						hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell4.setPaddingLeft(85f);
						table4.addCell(hcell4);
						
						hcell4 = new PdfPCell(new Phrase(":", redFont));
						hcell4.setBorder(Rectangle.NO_BORDER);
						hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell4.setPaddingRight(-30f);
						table4.addCell(hcell4);
						
						hcell4 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

						hcell9 = new PdfPCell(new Phrase("Reciept Amount", redFont));
						hcell9.setBorder(Rectangle.NO_BORDER);
						hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell9.setPaddingLeft(85f);
						table4.addCell(hcell9);
						
						hcell9 = new PdfPCell(new Phrase(":", redFont));
						hcell9.setBorder(Rectangle.NO_BORDER);					
						hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell9.setPaddingRight(-30f);
						table4.addCell(hcell9);
						
						hcell9 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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
						table13.setWidths(new float[] { 2f, 3f,3f, 3f, 3f });

						table13.setSpacingBefore(10);

						PdfPCell hcell33;
						hcell33 = new PdfPCell(new Phrase("Pay Mode", redFont1));
						hcell33.setBorder(Rectangle.NO_BORDER);
						hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell33.setPaddingLeft(10f);
						table13.addCell(hcell33);

						hcell33 = new PdfPCell(new Phrase("Amount", redFont1));
						hcell33.setBorder(Rectangle.NO_BORDER);
						hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell33.setPaddingLeft(35f);
						table13.addCell(hcell33);
						
						
						hcell33 = new PdfPCell(new Phrase("Card#", redFont1));
						hcell33.setBorder(Rectangle.NO_BORDER);
						hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell33.setPaddingLeft(40f);
						table13.addCell(hcell33);

						hcell33 = new PdfPCell(new Phrase("Bank Name", redFont1));
						hcell33.setBorder(Rectangle.NO_BORDER);
						hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell33.setPaddingLeft(40f);
						table13.addCell(hcell33);

						hcell33 = new PdfPCell(new Phrase("Exp Date", redFont1));
						hcell33.setBorder(Rectangle.NO_BORDER);
						hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell33.setPaddingLeft(50f);
						table13.addCell(hcell33);

						PdfPCell hcell34;
						hcell34 = new PdfPCell(new Phrase(newPaymentType, redFont2));
						hcell34.setBorder(Rectangle.NO_BORDER);
						hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell34.setPaddingLeft(10f);
						table13.addCell(hcell34);

						hcell34 = new PdfPCell(new Phrase(String.valueOf(total), redFont2));
						hcell34.setBorder(Rectangle.NO_BORDER);
						hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell34.setPaddingLeft(35f);
						table13.addCell(hcell34);

						hcell34 = new PdfPCell(new Phrase("", redFont1));
						hcell34.setBorder(Rectangle.NO_BORDER);
						hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell34.setPaddingLeft(40f);
						table13.addCell(hcell34);

						hcell34 = new PdfPCell(new Phrase("", redFont1));
						hcell34.setBorder(Rectangle.NO_BORDER);
						hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell34.setPaddingLeft(40f);
						table13.addCell(hcell34);

						hcell34 = new PdfPCell(new Phrase("", redFont1));
						hcell34.setBorder(Rectangle.NO_BORDER);
						hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell34.setPaddingLeft(50f);
						table13.addCell(hcell34);

						cell33.setFixedHeight(35f);
						cell33.setColspan(2);
						table13.setWidthPercentage(100f);
						cell33.addElement(table13);
						table.addCell(cell33);

											// for new row end

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

						PdfPCell hcell7;

						hcell7 = new PdfPCell(new Phrase(
								"Instructions  : "
										+ "1) Returns are accepted within TEN(10)Days. \n                       2) Fridge Items once sold cannot be taken Back.",
								redFont));
						hcell7.setBorder(Rectangle.NO_BORDER);
						hcell7.setPaddingLeft(-50f);
						table5.addCell(hcell7);

						hcell7 = new PdfPCell(new Phrase("Pharmacist"));
						hcell7.setBorder(Rectangle.NO_BORDER);
						hcell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
						hcell7.setPaddingTop(25f);
						table5.addCell(hcell7);

						cell6.setFixedHeight(80f);
						cell6.setColspan(2);
						cell6.addElement(table5);
						table.addCell(cell6);

						document.add(table);

						document.close();
						System.out.println("finished");
						pdfByte = byteArrayOutputStream.toByteArray();
						
						
						SalesPaymentPdf salesPaymentPdfs = salesPaymentPdfServiceImpl.findByFileName(billNo);
						
						if (salesPaymentPdfs!=null) {
							salesPaymentPdf = new SalesPaymentPdf();
							salesPaymentPdf.setFileName(billNo+"-"+regId+" Medicine Sales");
							salesPaymentPdf.setFileuri(salesPaymentPdfs.getFileuri());
							salesPaymentPdf.setPid(salesPaymentPdfs.getPid());
							salesPaymentPdf.setData(pdfByte);
							salesPaymentPdfServiceImpl.save(salesPaymentPdf);
							}
							else
							{
								
								String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
										.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();
								
								salesPaymentPdf = new SalesPaymentPdf();
								salesPaymentPdf.setFileName(billNo+"-"+regId+" Medicine Sales");
								salesPaymentPdf.setFileuri(uri);
								salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
								salesPaymentPdf.setData(pdfByte);
								salesPaymentPdfServiceImpl.save(salesPaymentPdf);
							}

					} catch (Exception e) {
						Logger.error(e.getMessage());
						//e.printStackTrace();
					}
			}
			
			else if(patientRegistration.getpType().equals(ConstantValues.INPATIENT) && newPaymentType.equalsIgnoreCase("Advance"))
			{
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
					pq.add(new Chunk(myAd,redFont));
	                   
					PdfPCell cellp = new PdfPCell(pq);
					PdfPCell cell1 = new PdfPCell();
					
					//for header bold
					PdfPTable table96 = new PdfPTable(1);
					table96.setWidths(new float[] { 5f });
					table96.setSpacingBefore(10);

					PdfPCell hcell96;
					hcell96 = new PdfPCell(new Phrase("UDBHAVA PHARMACY", redFont1));
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
					table2.setWidths(new float[] { 5f,1f,5f });
					table2.setSpacingBefore(10);

					PdfPCell hcell1;
					hcell1 = new PdfPCell(new Phrase("Bill#" , redFont));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setPaddingLeft(-15f);
					table2.addCell(hcell1);
					
					hcell1 = new PdfPCell(new Phrase(":" , redFont));
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
					
					String expdate=null;
					
					
					PdfPCell hcel123;
					hcel123 = new PdfPCell(new Phrase("Bill Date" , redFont));
					hcel123.setBorder(Rectangle.NO_BORDER);
					hcel123.setPaddingLeft(-15f);

					hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel123);
					
					hcel123 = new PdfPCell(new Phrase(":" , redFont));
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
					hcell18 = new PdfPCell(new Phrase("Patient Name" ,redFont));
					hcell18.setBorder(Rectangle.NO_BORDER);
					hcell18.setPaddingLeft(-15f);
					table2.addCell(hcell18);
					
					hcell18 = new PdfPCell(new Phrase(":",redFont));
					hcell18.setBorder(Rectangle.NO_BORDER);
					hcell18.setPaddingLeft(-35f);
					table2.addCell(hcell18);
					
					hcell18 = new PdfPCell(new Phrase(patientName,
							redFont));
					hcell18.setBorder(Rectangle.NO_BORDER);
					hcell18.setPaddingLeft(-25f);
					table2.addCell(hcell18);
					
					
					PdfPCell hcel;

					hcel = new PdfPCell(new Phrase("UMR No" , redFont));
					hcel.setBorder(Rectangle.NO_BORDER);
					hcel.setPaddingLeft(-15f);
					table2.addCell(hcel);
					
					hcel = new PdfPCell(new Phrase(":" , redFont));
					hcel.setBorder(Rectangle.NO_BORDER);
					hcel.setPaddingLeft(-35f);
					table2.addCell(hcel);
					
					hcel = new PdfPCell(new Phrase(umr, redFont));
					hcel.setBorder(Rectangle.NO_BORDER);
					hcel.setPaddingLeft(-25f);
					table2.addCell(hcel);
					
					
					PdfPCell hcel11;
					hcel11 = new PdfPCell(new Phrase("P.RegNo" , redFont));
					hcel11.setBorder(Rectangle.NO_BORDER);
					hcel11.setPaddingLeft(-15f);
					hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel11);
					
					hcel11 = new PdfPCell(new Phrase(":" , redFont));
					hcel11.setBorder(Rectangle.NO_BORDER);
					hcel11.setPaddingLeft(-35f);
					hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel11);
					
					hcel11 = new PdfPCell(new Phrase(regId, redFont));
					hcel11.setBorder(Rectangle.NO_BORDER);
					hcel11.setPaddingLeft(-25f);
					hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel11);
					
					PdfPCell hcel1;

					hcel1 = new PdfPCell(new Phrase("Doctor Name", redFont));
					hcel1.setBorder(Rectangle.NO_BORDER);
					hcel1.setPaddingLeft(-15f);
					table2.addCell(hcel1);
					
					hcel1 = new PdfPCell(new Phrase(":" , redFont));
					hcel1.setBorder(Rectangle.NO_BORDER);
					hcel1.setPaddingLeft(-35f);
					table2.addCell(hcel1);
					
					hcel1 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getConsultant() , redFont));
					hcel1.setBorder(Rectangle.NO_BORDER);
					hcel1.setPaddingLeft(-25f);
					table2.addCell(hcel1);
					
					
					cell0.setFixedHeight(100f);
					cell0.setColspan(2);
					cell0.addElement(table2);
					table.addCell(cell0);				
					
					PdfPCell cell19 = new PdfPCell();

					PdfPTable table21 = new PdfPTable(3);
					table21.setWidths(new float[] { 4f, 8f, 5f });
					table21.setSpacingBefore(10);

					PdfPCell hcell15;
					hcell15 = new PdfPCell(new Phrase("", redFont));
					hcell15.setBorder(Rectangle.NO_BORDER);
					hcell15.setPaddingLeft(-70f);
					table21.addCell(hcell15);

					hcell15 = new PdfPCell(new Phrase("Pharmacy Advance Receipt", redFont3));
					hcell15.setBorder(Rectangle.NO_BORDER);
					hcell15.setPaddingLeft(10);
					hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
					table21.addCell(hcell15);

					hcell15 = new PdfPCell(new Phrase("" , redFont));
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
					table1.setWidths(new float[] { 1.5f, 5f, 5f, 2f, 3f, 2f, 2f, 2f, 2f ,2f});

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
					
					hcell = new PdfPCell(new Phrase("Disc(%)", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("MRP", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("GST", redFont));
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

					for (Map<String,String> a : addMedicine) {

						MedicineDetails medicineDetails1 = medicineDetailsServiceImpl.findByName(a.get("medicineName"));
						List<MedicineProcurement> medicineProcurement=medicineProcurementServiceImpl.findByBatchAndMedicine(a.get("batchNo"), medicineDetails1.getMedicineId());
						PdfPCell cell;

						cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(a.get("medicineName"), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(a.get("batchNo")), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table1.addCell(cell);
						
						// for convert db date to dmy format
						
						
						/*String expdate=medicineProcurement.get(medicineProcurement.size() - 1).getExpDate().toString().substring(0,10);
						SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
						expdate=toFormat.format(fromFormat.parse(expdate));
					*/
						try
						{
						expdate=a.get("expiryDate").toString().substring(0, 10);
						SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
						expdate=toFormat.format(fromFormat.parse(expdate));
						
						}
						catch(Exception e)
						{
							Logger.error(e.getMessage());
						}
						
						cell = new PdfPCell(new Phrase(expdate,redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(a.get("quantity")), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);
						cell = new PdfPCell(new Phrase(String.valueOf((a.get("discount"))), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf((a.get("mrp"))), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);
						
						
						cell = new PdfPCell(new Phrase(String.valueOf((a.get("gst"))), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);


						cell = new PdfPCell(new Phrase(String.valueOf((a.get("amount"))), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);

					}
					cell3.setColspan(2);
					table1.setWidthPercentage(100f);
					cell3.addElement(table1);
					table.addCell(cell3);
					
					PdfPCell cell4 = new PdfPCell();

					PdfPTable table4 = new PdfPTable(6);
					table4.setWidths(new float[] { 5f,1f, 5f,8f,1f, 3f });
					table4.setSpacingBefore(10);

					int ttl=(int)Math.round(total);
					PdfPCell hcell2;
					hcell2 = new PdfPCell(new Phrase("Recieved Sum of Rupees",redFont));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table4.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(":",redFont));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-40f);
					table4.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(numberToWordsConverter.convert(ttl) + " Rupees Only",redFont));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table4.addCell(hcell2);

					hcell2 = new PdfPCell(new Phrase("Total Sale Value ", redFont));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(85f);
					table4.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(":", redFont));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell2.setPaddingRight(-30f);
					table4.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(String.valueOf(Math.round(total*100.0)/100.0), redFont));
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

					hcell04 = new PdfPCell(new Phrase("Rounded Off To", redFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell04.setPaddingLeft(85f);
					table4.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase(":", redFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell04.setPaddingRight(-30f);
					table4.addCell(hcell04);
					
				/*	BigDecimal bg=new BigDecimal(total-Math.floor(total));
					bg=bg.setScale(2,RoundingMode.HALF_DOWN);
					float round=bg.floatValue();
					//float rd=Math.nextUp(1f-round);
					float rd=1.00f-round;
					
					if(round<0.50)
					{
						hcell04 = new PdfPCell(new Phrase("-" +round , redFont));
					}
					else
					{
						
						if(String.valueOf(rd).length()>=4)
						{
							hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd).substring(0, 4), redFont));}
						else
						{
							hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd), redFont));	
							
						}
						
					}
				*/	
					hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

					hcell4 = new PdfPCell(new Phrase("Net Amount", redFont));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell4.setPaddingLeft(85f);
					table4.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(":", redFont));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell4.setPaddingRight(-30f);
					table4.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

					hcell9 = new PdfPCell(new Phrase("Reciept Amount", redFont));
					hcell9.setBorder(Rectangle.NO_BORDER);
					hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell9.setPaddingLeft(85f);
					table4.addCell(hcell9);
					
					hcell9 = new PdfPCell(new Phrase(":", redFont));
					hcell9.setBorder(Rectangle.NO_BORDER);					
					hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell9.setPaddingRight(-30f);
					table4.addCell(hcell9);
					
					hcell9 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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
					table13.setWidths(new float[] { 2f, 3f,3f, 3f, 3f });

					table13.setSpacingBefore(10);

					PdfPCell hcell33;
					hcell33 = new PdfPCell(new Phrase("Pay Mode", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(10f);
					table13.addCell(hcell33);

					hcell33 = new PdfPCell(new Phrase("Amount", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(35f);
					table13.addCell(hcell33);
					
					
					hcell33 = new PdfPCell(new Phrase("Card#", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(40f);
					table13.addCell(hcell33);

					hcell33 = new PdfPCell(new Phrase("Bank Name", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(40f);
					table13.addCell(hcell33);

					hcell33 = new PdfPCell(new Phrase("Exp Date", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(50f);
					table13.addCell(hcell33);

					PdfPCell hcell34;
					hcell34 = new PdfPCell(new Phrase(newPaymentType, redFont2));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(10f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase(String.valueOf(total), redFont2));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(35f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase("", redFont1));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(40f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase("", redFont1));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(40f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase("", redFont1));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(50f);
					table13.addCell(hcell34);

					cell33.setFixedHeight(35f);
					cell33.setColspan(2);
					table13.setWidthPercentage(100f);
					cell33.addElement(table13);
					table.addCell(cell33);

										// for new row end

					/*PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository
							.findByShopLocation(sales.getLocation());
*/
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

					PdfPCell hcell7;

					hcell7 = new PdfPCell(new Phrase(
							"Instructions  : "
									+ "1) Returns are accepted within TEN(10)Days. \n                       2) Fridge Items once sold cannot be taken Back.",
							redFont));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setPaddingLeft(-50f);
					table5.addCell(hcell7);

					hcell7 = new PdfPCell(new Phrase("Pharmacist"));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell7.setPaddingTop(25f);
					table5.addCell(hcell7);

					cell6.setFixedHeight(80f);
					cell6.setColspan(2);
					cell6.addElement(table5);
					table.addCell(cell6);

					document.add(table);



					document.close();
					System.out.println("finished");
					pdfByte = byteArrayOutputStream.toByteArray();
					
					SalesPaymentPdf salesPaymentPdfs = salesPaymentPdfServiceImpl.findByFileName(billNo);
					
					if (salesPaymentPdfs!=null) {
						salesPaymentPdf = new SalesPaymentPdf();
						salesPaymentPdf.setFileName(billNo+"-"+regId+" Medicine Sales");
						salesPaymentPdf.setFileuri(salesPaymentPdfs.getFileuri());
						salesPaymentPdf.setPid(salesPaymentPdfs.getPid());
						salesPaymentPdf.setData(pdfByte);
						salesPaymentPdfServiceImpl.save(salesPaymentPdf);
						}
						else
						{
							
							String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
									.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();
							
							salesPaymentPdf = new SalesPaymentPdf();
							salesPaymentPdf.setFileName(billNo+"-"+regId+" Medicine Sales");
							salesPaymentPdf.setFileuri(uri);
							salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
							salesPaymentPdf.setData(pdfByte);
							salesPaymentPdfServiceImpl.save(salesPaymentPdf);
						}


				} catch (Exception e) {
					Logger.error(e.getMessage());
				}
			}

			else if(patientRegistration.getpType().equals(ConstantValues.INPATIENT) && !newPaymentType.equalsIgnoreCase("Advance"))
			{
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
					pq.add(new Chunk(myAd,redFont));
	                   
					PdfPCell cellp = new PdfPCell(pq);
					PdfPCell cell1 = new PdfPCell();
					
					//for header bold
					PdfPTable table96 = new PdfPTable(1);
					table96.setWidths(new float[] { 5f });
					table96.setSpacingBefore(10);

					PdfPCell hcell96;
					hcell96 = new PdfPCell(new Phrase("UDBHAVA PHARMACY", redFont1));
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
					table2.setWidths(new float[] { 5f,1f,5f });
					table2.setSpacingBefore(10);

					PdfPCell hcell1;
					hcell1 = new PdfPCell(new Phrase("Bill#" , redFont));
					hcell1.setBorder(Rectangle.NO_BORDER);
					hcell1.setPaddingLeft(-15f);
					table2.addCell(hcell1);
					
					hcell1 = new PdfPCell(new Phrase(":" , redFont));
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
					
					String expdate=null;
					
					
					PdfPCell hcel123;
					hcel123 = new PdfPCell(new Phrase("Bill Date" , redFont));
					hcel123.setBorder(Rectangle.NO_BORDER);
					hcel123.setPaddingLeft(-15f);

					hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel123);
					
					hcel123 = new PdfPCell(new Phrase(":" , redFont));
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
					hcell18 = new PdfPCell(new Phrase("Patient Name" ,redFont));
					hcell18.setBorder(Rectangle.NO_BORDER);
					hcell18.setPaddingLeft(-15f);
					table2.addCell(hcell18);
					
					hcell18 = new PdfPCell(new Phrase(":",redFont));
					hcell18.setBorder(Rectangle.NO_BORDER);
					hcell18.setPaddingLeft(-35f);
					table2.addCell(hcell18);
					
					hcell18 = new PdfPCell(new Phrase(
							patientName,
							redFont));
					hcell18.setBorder(Rectangle.NO_BORDER);
					hcell18.setPaddingLeft(-25f);
					table2.addCell(hcell18);
					
					
					PdfPCell hcel;

					hcel = new PdfPCell(new Phrase("UMR No" , redFont));
					hcel.setBorder(Rectangle.NO_BORDER);
					hcel.setPaddingLeft(-15f);
					table2.addCell(hcel);
					
					hcel = new PdfPCell(new Phrase(":" , redFont));
					hcel.setBorder(Rectangle.NO_BORDER);
					hcel.setPaddingLeft(-35f);
					table2.addCell(hcel);
					
					hcel = new PdfPCell(new Phrase(umr, redFont));
					hcel.setBorder(Rectangle.NO_BORDER);
					hcel.setPaddingLeft(-25f);
					table2.addCell(hcel);
					
					
					PdfPCell hcel11;
					hcel11 = new PdfPCell(new Phrase("P.RegNo" , redFont));
					hcel11.setBorder(Rectangle.NO_BORDER);
					hcel11.setPaddingLeft(-15f);
					hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel11);
					
					hcel11 = new PdfPCell(new Phrase(":" , redFont));
					hcel11.setBorder(Rectangle.NO_BORDER);
					hcel11.setPaddingLeft(-35f);
					hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel11);
					
					hcel11 = new PdfPCell(new Phrase(regId, redFont));
					hcel11.setBorder(Rectangle.NO_BORDER);
					hcel11.setPaddingLeft(-25f);
					hcel11.setHorizontalAlignment(Element.ALIGN_LEFT);
					table2.addCell(hcel11);
					
					PdfPCell hcel1;

					hcel1 = new PdfPCell(new Phrase("Doctor Name", redFont));
					hcel1.setBorder(Rectangle.NO_BORDER);
					hcel1.setPaddingLeft(-15f);
					table2.addCell(hcel1);
					
					hcel1 = new PdfPCell(new Phrase(":" , redFont));
					hcel1.setBorder(Rectangle.NO_BORDER);
					hcel1.setPaddingLeft(-35f);
					table2.addCell(hcel1);
					
					hcel1 = new PdfPCell(new Phrase(patientRegistration.getPatientDetails().getConsultant() , redFont));
					hcel1.setBorder(Rectangle.NO_BORDER);
					hcel1.setPaddingLeft(-25f);
					table2.addCell(hcel1);
					
					
					cell0.setFixedHeight(100f);
					cell0.setColspan(2);
					cell0.addElement(table2);
					table.addCell(cell0);				
					
					PdfPCell cell19 = new PdfPCell();

					PdfPTable table21 = new PdfPTable(3);
					table21.setWidths(new float[] { 4f, 8f, 5f });
					table21.setSpacingBefore(10);

					PdfPCell hcell15;
					hcell15 = new PdfPCell(new Phrase("", redFont));
					hcell15.setBorder(Rectangle.NO_BORDER);
					hcell15.setPaddingLeft(-70f);
					table21.addCell(hcell15);
					
					if(newPaymentType.equalsIgnoreCase(ConstantValues.DUE))
					{
						hcell15 = new PdfPCell(new Phrase("Pharmacy Due Receipt", redFont3));
						hcell15.setBorder(Rectangle.NO_BORDER);
						hcell15.setPaddingLeft(35);
						hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
						table21.addCell(hcell15);
					}
					else
					{
						hcell15 = new PdfPCell(new Phrase("Pharmacy Receipt", redFont3));
						hcell15.setBorder(Rectangle.NO_BORDER);
						hcell15.setPaddingLeft(35);
						hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
						table21.addCell(hcell15);
					}
					
					hcell15 = new PdfPCell(new Phrase("" , redFont));
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
					table1.setWidths(new float[] { 1.5f, 5f, 5f, 2f, 3f, 2f, 2f, 2f, 2f ,2f});

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
					
					hcell = new PdfPCell(new Phrase("Disc(%)", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("MRP", redFont));
					hcell.setBorder(Rectangle.NO_BORDER);
					hcell.setBackgroundColor(BaseColor.GRAY);
					hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(hcell);

					hcell = new PdfPCell(new Phrase("GST", redFont));
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

					for (Map<String,String> a : addMedicine) {

						MedicineDetails medicineDetails1 = medicineDetailsServiceImpl.findByName(a.get("medicineName"));
						List<MedicineProcurement> medicineProcurement=medicineProcurementServiceImpl.findByBatchAndMedicine(a.get("batcheNo"), medicineDetails1.getMedicineId());
						PdfPCell cell;

						cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(a.get("medicineName"), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf(a.get("batchNo")), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						table1.addCell(cell);
						
						// for convert db date to dmy format
						
						
						/*String expdate=medicineProcurement.get(medicineProcurement.size() - 1).getExpDate().toString().substring(0,10);
						SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
						expdate=toFormat.format(fromFormat.parse(expdate));
					*/
						
						try
						{
						expdate=a.get("expiryDate").toString().substring(0, 10);
						SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
						expdate=toFormat.format(fromFormat.parse(expdate));
						
						}
						catch(Exception e)
						{
							Logger.error(e.getMessage());
							//e.printStackTrace();
						}
						
					//	System.out.println("-------------------------expiryDate-----"+expdate);
						cell = new PdfPCell(new Phrase(expdate,redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf((a.get("quantity"))), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);
						cell = new PdfPCell(new Phrase(String.valueOf((a.get("discount"))), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);

						cell = new PdfPCell(new Phrase(String.valueOf((a.get("mrp"))), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);
						
						
						cell = new PdfPCell(new Phrase(String.valueOf((a.get("gst"))), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);


						cell = new PdfPCell(new Phrase(String.valueOf((a.get("amount"))), redFont));
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setPaddingLeft(5);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table1.addCell(cell);

					}
					cell3.setColspan(2);
					table1.setWidthPercentage(100f);
					cell3.addElement(table1);
					table.addCell(cell3);
					
					PdfPCell cell4 = new PdfPCell();

					PdfPTable table4 = new PdfPTable(6);
					table4.setWidths(new float[] { 5f,1f, 5f,8f,1f, 3f });
					table4.setSpacingBefore(10);

					int ttl=(int)Math.round(total);
					PdfPCell hcell2;
					hcell2 = new PdfPCell(new Phrase("Recieved Sum of Rupees",redFont));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-50f);
					table4.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(":",redFont));
					hcell2.setBorder(Rectangle.NO_BORDER);
					hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell2.setPaddingLeft(-40f);
					table4.addCell(hcell2);
					
					hcell2 = new PdfPCell(new Phrase(numberToWordsConverter.convert(ttl) + " Rupees Only",redFont));
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
					
					hcell2 = new PdfPCell(new Phrase(String.valueOf(Math.round(total*100.0)/100.0), redFont));
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

					hcell04 = new PdfPCell(new Phrase("Rounded Off To", redFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell04.setPaddingLeft(85f);
					table4.addCell(hcell04);
					
					hcell04 = new PdfPCell(new Phrase(":", redFont));
					hcell04.setBorder(Rectangle.NO_BORDER);
					hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell04.setPaddingRight(-30f);
					table4.addCell(hcell04);
					
				/*	BigDecimal bg=new BigDecimal(total-Math.floor(total));
					bg=bg.setScale(2,RoundingMode.HALF_DOWN);
					float round=bg.floatValue();
					//float rd=Math.nextUp(1f-round);
					float rd=1.00f-round;
					
					if(round<0.50)
					{
						hcell04 = new PdfPCell(new Phrase("-" +round , redFont));
					}
					else
					{
						
						if(String.valueOf(rd).length()>=4)
						{
							hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd).substring(0,4) , redFont));
						}

						else
						{
							hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd) , redFont));	
						}
						
					}
	*/				
					hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)) , redFont));
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

					hcell4 = new PdfPCell(new Phrase("Net Amount", redFont));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell4.setPaddingLeft(85f);
					table4.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(":", redFont));
					hcell4.setBorder(Rectangle.NO_BORDER);
					hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell4.setPaddingRight(-30f);
					table4.addCell(hcell4);
					
					hcell4 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

					hcell9 = new PdfPCell(new Phrase("Reciept Amount", redFont));
					hcell9.setBorder(Rectangle.NO_BORDER);
					hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell9.setPaddingLeft(85f);
					table4.addCell(hcell9);
					
					hcell9 = new PdfPCell(new Phrase(":", redFont));
					hcell9.setBorder(Rectangle.NO_BORDER);					
					hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell9.setPaddingRight(-30f);
					table4.addCell(hcell9);
					
					hcell9 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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
					table13.setWidths(new float[] { 2f, 3f,3f, 3f, 3f });

					table13.setSpacingBefore(10);

					PdfPCell hcell33;
					hcell33 = new PdfPCell(new Phrase("Pay Mode", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(10f);
					table13.addCell(hcell33);

					hcell33 = new PdfPCell(new Phrase("Amount", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(35f);
					table13.addCell(hcell33);
					
					
					hcell33 = new PdfPCell(new Phrase("Card#", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(40f);
					table13.addCell(hcell33);

					hcell33 = new PdfPCell(new Phrase("Bank Name", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(40f);
					table13.addCell(hcell33);

					hcell33 = new PdfPCell(new Phrase("Exp Date", redFont1));
					hcell33.setBorder(Rectangle.NO_BORDER);
					hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell33.setPaddingLeft(50f);
					table13.addCell(hcell33);

					PdfPCell hcell34;
					hcell34 = new PdfPCell(new Phrase(newPaymentType, redFont2));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(10f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase(String.valueOf(total), redFont2));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(35f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase("", redFont1));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(40f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase("", redFont1));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(40f);
					table13.addCell(hcell34);

					hcell34 = new PdfPCell(new Phrase("", redFont1));
					hcell34.setBorder(Rectangle.NO_BORDER);
					hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell34.setPaddingLeft(50f);
					table13.addCell(hcell34);

					cell33.setFixedHeight(35f);
					cell33.setColspan(2);
					table13.setWidthPercentage(100f);
					cell33.addElement(table13);
					table.addCell(cell33);

										// for new row end
/*
					PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository
							.findByShopLocation(sales.getLocation());

*/					PdfPCell cell01 = new PdfPCell();

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

					PdfPCell hcell7;

					hcell7 = new PdfPCell(new Phrase(
							"Instructions  : "
									+ "1) Returns are accepted within TEN(10)Days. \n                       2) Fridge Items once sold cannot be taken Back.",
							redFont));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setPaddingLeft(-50f);
					table5.addCell(hcell7);

					hcell7 = new PdfPCell(new Phrase("Pharmacist"));
					hcell7.setBorder(Rectangle.NO_BORDER);
					hcell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
					hcell7.setPaddingTop(25f);
					table5.addCell(hcell7);

					cell6.setFixedHeight(80f);
					cell6.setColspan(2);
					cell6.addElement(table5);
					table.addCell(cell6);

					document.add(table);

					document.close();
					System.out.println("finished");
					pdfByte = byteArrayOutputStream.toByteArray();
					SalesPaymentPdf salesPaymentPdfs = salesPaymentPdfServiceImpl.findByFileName(billNo);
					
					if (salesPaymentPdfs!=null) {
						salesPaymentPdf = new SalesPaymentPdf();
						salesPaymentPdf.setFileName(billNo+"-"+regId+" Medicine Sales");
						salesPaymentPdf.setFileuri(salesPaymentPdfs.getFileuri());
						salesPaymentPdf.setPid(salesPaymentPdfs.getPid());
						salesPaymentPdf.setData(pdfByte);
						salesPaymentPdfServiceImpl.save(salesPaymentPdf);
						}
						else
						{
							
							String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
									.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();
							
							salesPaymentPdf = new SalesPaymentPdf();
							salesPaymentPdf.setFileName(billNo+"-"+regId+" Medicine Sales");
							salesPaymentPdf.setFileuri(uri);
							salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
							salesPaymentPdf.setData(pdfByte);
							salesPaymentPdfServiceImpl.save(salesPaymentPdf);
						}


				} catch (Exception e) {
					Logger.error(e.getMessage());
					//e.printStackTrace();
				}
			}

			}
			else //for walk-ins
			{
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
								pq.add(new Chunk(myAd,redFont));
				                   
								PdfPCell cellp = new PdfPCell(pq);
								PdfPCell cell1 = new PdfPCell();
								
								//for header bold
								PdfPTable table96 = new PdfPTable(1);
								table96.setWidths(new float[] { 5f });
								table96.setSpacingBefore(10);

								PdfPCell hcell96;
								hcell96 = new PdfPCell(new Phrase("UDBHAVA PHARMACY", redFont1));
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
								table2.setWidths(new float[] { 5f,1f,5f });
								table2.setSpacingBefore(10);

								PdfPCell hcell1;
								hcell1 = new PdfPCell(new Phrase("Bill#" , redFont));
								hcell1.setBorder(Rectangle.NO_BORDER);
								hcell1.setPaddingLeft(-15f);
								table2.addCell(hcell1);
								
								hcell1 = new PdfPCell(new Phrase(":" , redFont));
								hcell1.setBorder(Rectangle.NO_BORDER);
								hcell1.setPaddingLeft(-35f);
								table2.addCell(hcell1);
								
								hcell1 = new PdfPCell(new Phrase( billNo, redFont));
								hcell1.setBorder(Rectangle.NO_BORDER);
								hcell1.setPaddingLeft(-25f);
								table2.addCell(hcell1);

								// Display a date in day, month, year format
								Date date = Calendar.getInstance().getTime();
								DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
								String today = formatter.format(date).toString();
								String expdate=null;
								
								
								PdfPCell hcel123;
								hcel123 = new PdfPCell(new Phrase("Bill Date" , redFont));
								hcel123.setBorder(Rectangle.NO_BORDER);
								hcel123.setPaddingLeft(-15f);

								hcel123.setHorizontalAlignment(Element.ALIGN_LEFT);
								table2.addCell(hcel123);
								
								hcel123 = new PdfPCell(new Phrase(":" , redFont));
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
								hcell18 = new PdfPCell(new Phrase("Patient Name" ,redFont));
								hcell18.setBorder(Rectangle.NO_BORDER);
								hcell18.setPaddingLeft(-15f);
								table2.addCell(hcell18);
								
								hcell18 = new PdfPCell(new Phrase(":",redFont));
								hcell18.setBorder(Rectangle.NO_BORDER);
								hcell18.setPaddingLeft(-35f);
								table2.addCell(hcell18);
								
								hcell18 = new PdfPCell(new Phrase(salesPrev.getName(),redFont));
								hcell18.setBorder(Rectangle.NO_BORDER);
								hcell18.setPaddingLeft(-25f);
								table2.addCell(hcell18);
								
								
										
								cell0.setFixedHeight(100f);
								cell0.setColspan(2);
								cell0.addElement(table2);
								table.addCell(cell0);				
								
								PdfPCell cell19 = new PdfPCell();

								PdfPTable table21 = new PdfPTable(3);
								table21.setWidths(new float[] { 4f, 4f, 5f });
								table21.setSpacingBefore(10);

								PdfPCell hcell15;
								hcell15 = new PdfPCell(new Phrase("", redFont));
								hcell15.setBorder(Rectangle.NO_BORDER);
								hcell15.setPaddingLeft(-70f);
								table21.addCell(hcell15);

								hcell15 = new PdfPCell(new Phrase("Pharmacy Receipt", redFont3));
								hcell15.setBorder(Rectangle.NO_BORDER);
								hcell15.setPaddingLeft(35);
								hcell15.setHorizontalAlignment(Element.ALIGN_CENTER);
								table21.addCell(hcell15);

								hcell15 = new PdfPCell(new Phrase("" , redFont));
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
								table1.setWidths(new float[] { 1.5f, 5f, 5f, 2f, 3f, 2f, 2f, 2f, 2f ,2f});

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
								
								hcell = new PdfPCell(new Phrase("Disc(%)", redFont));
								hcell.setBorder(Rectangle.NO_BORDER);
								hcell.setBackgroundColor(BaseColor.GRAY);
								hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
								table1.addCell(hcell);

								hcell = new PdfPCell(new Phrase("MRP", redFont));
								hcell.setBorder(Rectangle.NO_BORDER);
								hcell.setBackgroundColor(BaseColor.GRAY);
								hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
								table1.addCell(hcell);

								hcell = new PdfPCell(new Phrase("GST", redFont));
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

								for (Map<String,String> a : addMedicine) {

									MedicineDetails medicineDetails1 = medicineDetailsServiceImpl.findByName(a.get("medicineName"));
									List<MedicineProcurement> medicineProcurement=medicineProcurementServiceImpl.findByBatchAndMedicine(a.get("batchNo"), medicineDetails1.getMedicineId());
									PdfPCell cell;

									cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table1.addCell(cell);

									cell = new PdfPCell(new Phrase(a.get("medicineName"), redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setPaddingLeft(5);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
									table1.addCell(cell);

									cell = new PdfPCell(new Phrase(medicineDetails1.getManufacturer(), redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setPaddingLeft(5);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									//cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
									table1.addCell(cell);

									cell = new PdfPCell(new Phrase(String.valueOf(a.get("batchNo")), redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setPaddingLeft(5);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									// cell.setHorizontalAlignment(Element.ALIGN_LEFT);
									table1.addCell(cell);
									
									// for convert db date to dmy format
									
									
									/*String expdate=medicineProcurement.get(medicineProcurement.size() - 1).getExpDate().toString().substring(0,10);
									SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
									SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
									expdate=toFormat.format(fromFormat.parse(expdate));
								*/
									
									try
									{
									expdate=a.get("expiryDate").toString().substring(0, 10);
									SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
									SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
									expdate=toFormat.format(fromFormat.parse(expdate));
									
									}
									catch(Exception e)
									{
										Logger.error(e.getMessage());
									}
									
									cell = new PdfPCell(new Phrase(expdate,redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setPaddingLeft(5);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table1.addCell(cell);

									cell = new PdfPCell(new Phrase(String.valueOf((a.get("quantity"))), redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setPaddingLeft(5);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table1.addCell(cell);
									cell = new PdfPCell(new Phrase(String.valueOf((a.get("discount"))), redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setPaddingLeft(5);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table1.addCell(cell);

									cell = new PdfPCell(new Phrase(String.valueOf((a.get("mrp"))), redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setPaddingLeft(5);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table1.addCell(cell);
									
									
									cell = new PdfPCell(new Phrase(String.valueOf((a.get("gst"))), redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setPaddingLeft(5);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table1.addCell(cell);


									cell = new PdfPCell(new Phrase(String.valueOf((a.get("amount"))), redFont));
									cell.setBorder(Rectangle.NO_BORDER);
									cell.setPaddingLeft(5);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table1.addCell(cell);

								}
								cell3.setColspan(2);
								table1.setWidthPercentage(100f);
								cell3.addElement(table1);
								table.addCell(cell3);
								
								PdfPCell cell4 = new PdfPCell();

								PdfPTable table4 = new PdfPTable(6);
								table4.setWidths(new float[] { 5f,1f, 5f,8f,1f, 3f });
								table4.setSpacingBefore(10);

								int ttl=(int)Math.round(total);
								PdfPCell hcell2;
								hcell2 = new PdfPCell(new Phrase("Recieved Sum of Rupees",redFont));
								hcell2.setBorder(Rectangle.NO_BORDER);
								hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell2.setPaddingLeft(-50f);
								table4.addCell(hcell2);
								
								hcell2 = new PdfPCell(new Phrase(":",redFont));
								hcell2.setBorder(Rectangle.NO_BORDER);
								hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell2.setPaddingLeft(-40f);
								table4.addCell(hcell2);
								
								hcell2 = new PdfPCell(new Phrase(numberToWordsConverter.convert(ttl) + " Rupees Only",redFont));
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
								
								hcell2 = new PdfPCell(new Phrase(String.valueOf(Math.round(total*100.0)/100.0), redFont));
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

								hcell04 = new PdfPCell(new Phrase("Rounded Off To", redFont));
								hcell04.setBorder(Rectangle.NO_BORDER);
								hcell04.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell04.setPaddingLeft(85f);
								table4.addCell(hcell04);
								
								hcell04 = new PdfPCell(new Phrase(":", redFont));
								hcell04.setBorder(Rectangle.NO_BORDER);
								hcell04.setHorizontalAlignment(Element.ALIGN_RIGHT);
								hcell04.setPaddingRight(-30f);
								table4.addCell(hcell04);
								
							/*	BigDecimal bg=new BigDecimal(total-Math.floor(total));
								bg=bg.setScale(2,RoundingMode.HALF_DOWN);
								float round=bg.floatValue();
								//float rd=Math.nextUp(1f-round);
								float rd=1.00f-round;
								
								if(round<0.50)
								{
									hcell04 = new PdfPCell(new Phrase("-" +round , redFont));
								}
								else
								{
									

									if(String.valueOf(rd).length()>=4)
									{
										hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd).substring(0,4) , redFont));
									}

									else
									{
										hcell04 = new PdfPCell(new Phrase("+" +String.valueOf(rd) , redFont));
		
									}

								}
	*/							
								hcell04 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)) , redFont));
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

								hcell4 = new PdfPCell(new Phrase("Net Amount", redFont));
								hcell4.setBorder(Rectangle.NO_BORDER);
								hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell4.setPaddingLeft(85f);
								table4.addCell(hcell4);
								
								hcell4 = new PdfPCell(new Phrase(":", redFont));
								hcell4.setBorder(Rectangle.NO_BORDER);
								hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
								hcell4.setPaddingRight(-30f);
								table4.addCell(hcell4);
								
								hcell4 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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

								hcell9 = new PdfPCell(new Phrase("Reciept Amount", redFont));
								hcell9.setBorder(Rectangle.NO_BORDER);
								hcell9.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell9.setPaddingLeft(85f);
								table4.addCell(hcell9);
								
								hcell9 = new PdfPCell(new Phrase(":", redFont));
								hcell9.setBorder(Rectangle.NO_BORDER);					
								hcell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
								hcell9.setPaddingRight(-30f);
								table4.addCell(hcell9);
								
								hcell9 = new PdfPCell(new Phrase(String.valueOf(Math.round(total)), redFont));
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
								table13.setWidths(new float[] { 2f, 3f,3f, 3f, 3f });

								table13.setSpacingBefore(10);

								PdfPCell hcell33;
								hcell33 = new PdfPCell(new Phrase("Pay Mode", redFont1));
								hcell33.setBorder(Rectangle.NO_BORDER);
								hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell33.setPaddingLeft(10f);
								table13.addCell(hcell33);

								hcell33 = new PdfPCell(new Phrase("Amount", redFont1));
								hcell33.setBorder(Rectangle.NO_BORDER);
								hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell33.setPaddingLeft(35f);
								table13.addCell(hcell33);
								
								
								hcell33 = new PdfPCell(new Phrase("Card#", redFont1));
								hcell33.setBorder(Rectangle.NO_BORDER);
								hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell33.setPaddingLeft(40f);
								table13.addCell(hcell33);

								hcell33 = new PdfPCell(new Phrase("Bank Name", redFont1));
								hcell33.setBorder(Rectangle.NO_BORDER);
								hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell33.setPaddingLeft(40f);
								table13.addCell(hcell33);

								hcell33 = new PdfPCell(new Phrase("Exp Date", redFont1));
								hcell33.setBorder(Rectangle.NO_BORDER);
								hcell33.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell33.setPaddingLeft(50f);
								table13.addCell(hcell33);

								PdfPCell hcell34;
								hcell34 = new PdfPCell(new Phrase(sales.getPaymentType(), redFont2));
								hcell34.setBorder(Rectangle.NO_BORDER);
								hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell34.setPaddingLeft(10f);
								table13.addCell(hcell34);

								hcell34 = new PdfPCell(new Phrase(String.valueOf(total), redFont2));
								hcell34.setBorder(Rectangle.NO_BORDER);
								hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell34.setPaddingLeft(35f);
								table13.addCell(hcell34);

								hcell34 = new PdfPCell(new Phrase("", redFont1));
								hcell34.setBorder(Rectangle.NO_BORDER);
								hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell34.setPaddingLeft(40f);
								table13.addCell(hcell34);

								hcell34 = new PdfPCell(new Phrase("", redFont1));
								hcell34.setBorder(Rectangle.NO_BORDER);
								hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell34.setPaddingLeft(40f);
								table13.addCell(hcell34);

								hcell34 = new PdfPCell(new Phrase("", redFont1));
								hcell34.setBorder(Rectangle.NO_BORDER);
								hcell34.setHorizontalAlignment(Element.ALIGN_LEFT);
								hcell34.setPaddingLeft(50f);
								table13.addCell(hcell34);

								cell33.setFixedHeight(35f);
								cell33.setColspan(2);
								table13.setWidthPercentage(100f);
								cell33.addElement(table13);
								table.addCell(cell33);

													// for new row end
/*
								PharmacyShopDetails pharmacyShopDetails = pharmacyShopDetailsRepository
										.findByShopLocation(sales.getLocation());
*/
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

								PdfPCell hcell7;

								hcell7 = new PdfPCell(new Phrase(
										"Instructions  : "
												+ "1) Returns are accepted within TEN(10)Days. \n                       2) Fridge Items once sold cannot be taken Back.",
										redFont));
								hcell7.setBorder(Rectangle.NO_BORDER);
								hcell7.setPaddingLeft(-50f);
								table5.addCell(hcell7);

								hcell7 = new PdfPCell(new Phrase("Pharmacist",redFont1));
								hcell7.setBorder(Rectangle.NO_BORDER);
								hcell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
								hcell7.setPaddingTop(25f);
								table5.addCell(hcell7);

								cell6.setFixedHeight(80f);
								cell6.setColspan(2);
								cell6.addElement(table5);
								table.addCell(cell6);

								document.add(table);

								document.close();
								System.out.println("finished");
					pdfByte = byteArrayOutputStream.toByteArray();
					SalesPaymentPdf salesPaymentPdfs = salesPaymentPdfServiceImpl.findByFileName(billNo);
					
					if (salesPaymentPdfs!=null) {
						salesPaymentPdf = new SalesPaymentPdf();
						salesPaymentPdf.setFileName(billNo+" "+"Sales");
						salesPaymentPdf.setFileuri(salesPaymentPdfs.getFileuri());
						salesPaymentPdf.setPid(salesPaymentPdfs.getPid());
						salesPaymentPdf.setData(pdfByte);
						salesPaymentPdfServiceImpl.save(salesPaymentPdf);
						}
						else
						{
							
							System.out.println("---------Coming to else condition---------------");
							String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
									.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();
							
							salesPaymentPdf = new SalesPaymentPdf();
							salesPaymentPdf.setFileName(billNo+" "+"Sales");
							salesPaymentPdf.setFileuri(uri);
							salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
							salesPaymentPdf.setData(pdfByte);
							salesPaymentPdfServiceImpl.save(salesPaymentPdf);
						}


				} catch (Exception e) {
					Logger.error(e.getMessage());
				}
			}

	
//---------------------end--------------------------------

	
}
	



}
