package com.vncdigital.vpulse.patient.serviceImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vncdigital.vpulse.MoneyToWords.NumberToWordsConverter;
import com.vncdigital.vpulse.bed.model.RoomBookingDetails;
import com.vncdigital.vpulse.bed.model.RoomDetails;
import com.vncdigital.vpulse.bed.serviceImpl.RoomBookingDetailsServiceImpl;
import com.vncdigital.vpulse.bed.serviceImpl.RoomDetailsServiceImpl;
import com.vncdigital.vpulse.bill.model.ChargeBill;
import com.vncdigital.vpulse.bill.repository.ChargeBillRepository;
import com.vncdigital.vpulse.bill.serviceImpl.ChargeBillServiceImpl;
import com.vncdigital.vpulse.config.ConstantValues;
import com.vncdigital.vpulse.finalBilling.model.FinalBilling;
import com.vncdigital.vpulse.finalBilling.serviceImpl.FinalBillingServiceImpl;
import com.vncdigital.vpulse.laboratory.serviceImpl.LabServicesServiceImpl;
import com.vncdigital.vpulse.nurse.repository.PrescriptionDetailsRepository;
import com.vncdigital.vpulse.patient.Helper.ExistingPatientHelper;
import com.vncdigital.vpulse.patient.idGenerator.RegGenerator;
import com.vncdigital.vpulse.patient.idGenerator.UmrGenerator;
import com.vncdigital.vpulse.patient.model.CashPlusCard;
import com.vncdigital.vpulse.patient.model.MarketingQuestions;
import com.vncdigital.vpulse.patient.model.PatientDetails;
import com.vncdigital.vpulse.patient.model.PatientPayment;
import com.vncdigital.vpulse.patient.model.PatientPaymentPdf;
import com.vncdigital.vpulse.patient.model.PatientRegistration;
import com.vncdigital.vpulse.patient.model.PatientTypes;
import com.vncdigital.vpulse.patient.model.ReferralDetails;
import com.vncdigital.vpulse.patient.repository.PatientDetailsRepository;
import com.vncdigital.vpulse.patient.repository.PatientRegistrationRepository;
import com.vncdigital.vpulse.patient.repository.PaymentPdfRepository;
import com.vncdigital.vpulse.patient.repository.PaymentRepository;
import com.vncdigital.vpulse.patient.service.PatientDetailsService;
import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.MedicineProcurement;
import com.vncdigital.vpulse.pharmacist.model.Sales;
import com.vncdigital.vpulse.pharmacist.serviceImpl.MedicineDetailsServiceImpl;
import com.vncdigital.vpulse.user.model.SpecUserJoin;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.repository.SpecUserJoinRepository;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;


@Service
public class PatientDetailsServiceImpl implements PatientDetailsService
{
	// AWS_PDF -> ADMIN WISE SALES PDF
	private static final Logger Logger = LoggerFactory.getLogger(PatientDetailsServiceImpl.class);
	private static final String ADMIN_WISE_SALES_AWS_PDF = "Admin Wise Sales";
	private static final String FROM_DT_AWS_PDF = "From Dt  : ";
	private static final String TILL_DT_AWS_PDF = "Till Dt  : ";
	private static final String PATIENT_NAME_AWS_PDF = "Patient Name                :  ";
	private static final String ADMI_NO_AWS_PDF = "Admn#               :  ";
	private static final String DOC_NAME_AWS_PDF = "DOC Name        :  ";
	private static final String DOA_AWS_PDF = "DOA                             :  ";
	private static final String S_NO_AWS_PDF = "S.No";
	private static final String ITEM_NAME_AWS_PDF = "Item Name";
	private static final String BATCH_NO_AWS_PDF = "Batch#";
	private static final String EXP_DT_AWS_PDF = "Exp.Dt";
	private static final String QTY_AWS_PDF = " Qty";
	private static final String SALE_VALUE_AWS_PDF = "Sale Value";
	private static final String TOTAL_AWS_PDF = "                 Total ";
	private static final String PRINTED_BY_AWS_PDF = "Printed By      :  ";
	private static final String PRINTED_DATE_AWS_PDF = "Printed Date      :  ";
	private static final String V1_PAYMENT_VIEWFILE_PATH = "/v1/payment/viewFile/";
	private static final String TWO_DAYS = "2";
	private static final String SEVEN_DAYS = "7";
	private static final String FIFTEEN_DAYS = "15";
	private static final String THIRTY_DAYS = "30";
	private static final String FROM_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String TO_DATE_FORMAT = "dd-MM-yyyy hh:mm a";

	// ADV_PDF -> ADVANCE AMOUNT PDF
	private static final String FROM_DATE_FORMAT_ADV_PDF = "dd-MMM-yyyy hh.mm aa";
	private static final String RECT_NO_ADV_PDF = "Rect.No";
	private static final String RECT_DATE_ADV_PDF = "Rect.Dt";
	private static final String AGE_SEX_ADV_PDF = "Age/Sex";
	private static final String BACKWARD_SLASH_ADV_PDF = "/";
	private static final String PHONE_ADV_PDF = "Phone";
	private static final String ADVANCE_RECIEPT_ADV_PDF = "Advance Receipt";
	private static final String FATHER_ADV_PDF = "Father";
	private static final String ADMITTED_WARD_ADV_PDF = "Admitted Ward";
	private static final String ADMITTED_DATE = "Admn.Dt";
	private static final String DEPARTMENT_ADV_PDF = "Department";
	private static final String CONSULTANT_ADV_PDF = "Consulatant";
	private static final String RECT_AMOUNT_ADV_PDF = "Rect.Amount";
	private static final String ORG_ADV_PDF = "Org.";
	private static final String TOTAL_ADV_AMOUNT_RECEIVED = "Total Adv. Amt. Received";
	private static final String ADDRESS_ADV_PDF = "Address";
	private static final String CITY_ADV_PDF = "City";
	private static final String STATE_ADV_PDF = "State";
	private static final String CREATED_BY_ADV_PDF = "Created By    :";
	private static final String CREATED_DATE = "Created Dt   :   ";
	private static final String PRINTED_BY_ADV_PDF = "Printed By     :";
	private static final String PRINTED_DATE = "Print Dt       :   ";
	private static final String AUTHORIZED_SIGNATURE = "(Authorized Signature)";
	private static final String PATIENT_ADV_PDF = "Patient";
	private static final String COLON = ":";
	private static final String UMR_NO_ADV_PDF = "UMR NO";
	private static final String EMPTY_STRING = "";

	// SAVEINFO_PDF -> New patient registration
	private static final String PATIENT_NAME_SAVEINFO_PDF = "Patient Name";
	private static final String COLON_SAVEINFO_PDF = " : ";
	private static final String AGE_GENDER_SAVEINFO_PDF = "Age/Gender";
	private static final String VISIT_TYPE_SAVEINFO_PDF = "Visit Type";
	private static final String REG_NO_SAVEINFO_PDF = "Reg. No";
	private static final String REF_DOCTOR_SAVEINFO_PDF = "Ref.Doctor";
	private static final String UMR_SAVEINFO_PDF = "UMR";
	private static final String DATE_SAVEINFO_PDF = "Date";
	private static final String UDBHAVA_PNG_CLASSPATH = "classpath:udbhava.png";
	private static final String ASIA_KOLKATA_TIMEZONE = "Asia/Kolkata";
	private static final String CONSULTATION_FEE_SAVE_INFO_PDF = "Consultation Fee";
	private static final String ADVANCE_AMOUNT_SAVEINFO_PDF = "Advance Amount";
	private static final String CONST_NO_SAVEINFO_PDF = "Const.No";
	private static final String CONST_DATE_SAVEINFO_PDF = "Const.Dt";
	private static final String REF_BY_SAVEINFO_PDF = "Ref.By";
	private static final String CONSULTATION_RECIEPT_SAVEINFO_PDF = "Consultation Receipt";
	private static final String DEPT_NAME_SAVEINFO_PDF = "Dept.Name";
	private static final String PAYMENT_MODE_SAVEINFO_PDF = "Payment Mode";
	private static final String NURSING_CHARGES_SAVEINFO_PDF = "Nursing Charges";
	private static final String SERVICE_CHARGES_SAVEINFO_PDF = "Service Charges";
	private static final String REG_FEES_SAVEINFO_PDF = "Reg Fees";
	private static final String VACCINATION_FEES_SAVEINFO_PDF = "Vaccination fees";
	private static final String RECIEVED_WITH_THANKS_SAVEINFO_PDF = "Received with thanks from ";
	private static final String A_SUM_OF_RS_SAVEINFO_PDF = "A sum of Rs.";
	private static final String IN_WORDS_RS_SAVEINFO_PDF = "In Words Rupees ";
	private static final String STAR = "*";
	private static final String CREATED_BY_SAVEINFO_PDF = "Created By    : ";
	private static final String PRINTED_BY_SAVEINFO_PDF = "Printed By     : ";
	private static final String RECEIPT_NO_SAVEINFO_PDF = "Receipt No";
	private static final String NOT_DISCHARGED = "Not-Discharged";

	private static final String VALIDITY = "Validity             : ";
	private static final String TWO_VISITS_BEFORE = "2 Visits Before ";
	private static final String RECEIVED_WITH_THANKS_FROM = "\n\nReceived with thanks from ";

	private static final String DOCTOR = "DOCTOR";
	private static final String ALLOCATE = "ALLOCATE";
	private static final String OCCUPIED = "OCCUPIED";
	private static final String BLANK_PRESCRIPTION = " Blank Prescription";
	private static final String Y_SPACE = "Y ";
	private static final String M_SPACE = "M ";
	private static final String D = "D";
	private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
	private static final String SIMPLE_DATE_FORMAT_TO = "dd-MMM-yyyy";
	private static final String DOCTOR_FEE = "Doctor Fee";
	private static final String PATIENT_REGISTRATION = "Patient Registration";

	@Autowired
	PatientDetailsRepository repo;
	
	@Autowired
	PatientPaymentServiceImpl patientPaymentServiceImpl;
	
	
	@Autowired
	CashPlusCardServiceImpl cashPlusCardServiceImpl;
	
	@Autowired
	FinalBillingServiceImpl finalBillingServcieImpl;
	
	@Autowired
	SpecUserJoinRepository specUserJoinRepository;
	
	@Autowired
	PatientRegistrationRepository patientRegistrationRepository;
	
	@Autowired
	PaymentPdfRepository pdfrepo;
	
	@Autowired
	PaymentRepository paymentRepository;
	
	@Autowired
	PrescriptionDetailsRepository prescriptionDetailsRepository;
	
	@Autowired
	ReferralDetailsServiceImpl referralDetailsServiceImpl;
	
	@Autowired
	ChargeBillServiceImpl chargeBillServiceImpl;
	
	@Autowired
	NumberToWordsConverter numberToWordsConverter;
	
	@Autowired
	LabServicesServiceImpl labServicesServiceImpl;
	
	@Autowired
	MarketingQuestionsServiceImpl   marketingQuestionsServiceImpl;
	
	@Autowired
	PatientRegistrationServiceImpl  patientRegistrationServiceImpl;

	
	@Autowired
	PatientTypeServiceImpl  patientTypeServiceImpl;
	
	@Autowired
	PatientDetailsServiceImpl patientDetailsServiceImpl;

	@Autowired
	PatientDetailsRepository patientDetailsRepository;
	
	
	@Autowired
	UserServiceImpl userServiceImpl;
	
	@Autowired
	PaymentPdfServiceImpl paymentPdfServiceImpl;
	

	@Autowired
    ResourceLoader resourceLoader;
	
	
	@Autowired
	ExistingPatientHelper existingPatientHelper;
	
	@Autowired
	RoomBookingDetailsServiceImpl roomBookingDetailsServiceImpl;
	
	@Autowired
	RoomDetailsServiceImpl roomDetailsServiceImpl;
	
	@Autowired
	ChargeBillRepository chargeBillRepository;
	
	@Autowired
	UmrGenerator umrGenerator;
	
	@Autowired
	RegGenerator regGenerator;
	
	@Autowired
	MedicineDetailsServiceImpl medicineDetailsServiceImpl;
	
	public PatientDetails save(PatientDetails patientDetails)
	{
		return repo.save(patientDetails);
	}
	
	public Optional<PatientDetails> findById(Long id)
	{
		return repo.findById(id);
	}
	
	public void delte(Long id)
	{
		 repo.deleteById(id);
	}
	
	public PatientDetails update(PatientDetails patientDetails)
	{
		return repo.save(patientDetails);
	}
	
	/*
	 * Adminwise sales for inpatient 
	 */
	public PatientPaymentPdf admnWiseSales(String regId,Principal principal) {
		
		//createdBy (Security)
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		
		String createdBy=userSecurity.getFirstName()+ConstantValues.ONE_SPACE_STRING+userSecurity.getLastName();
		
		PatientPaymentPdf patientPaymentPdf = null;
		byte[] pdfBytes = null;

		PatientRegistration patientRegistration = patientRegistrationServiceImpl.findByRegId(regId);

		List<Sales> sales = patientRegistration.getSales();

		//shantharam addr

		String addres="";
		
		if (patientRegistration.getpType().equalsIgnoreCase(ConstantValues.INPATIENT)) {

			try {

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

				Document document = new Document(PageSize.A4_LANDSCAPE);

				Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

				Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
				PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
				Resource fileResourcee = resourceLoader.getResource(UDBHAVA_PNG_CLASSPATH);
				document.open();
				
				
				
				PdfPTable table = new PdfPTable(2);

				Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());
				img.scaleAbsolute(62, 87);
				table.setWidthPercentage(105f);

				Phrase pq = new Phrase(new Chunk(img, 5, -63));

				pq.add(new Chunk(addres,redFont));
				
				PdfPCell cellp = new PdfPCell(pq);
				PdfPCell cell1 = new PdfPCell();
				cell1.setBorder(0);
				
				PdfPTable table96 = new PdfPTable(1);
				table96.setWidths(new float[] { 5f });
				table96.setSpacingBefore(10);

				PdfPCell hcell96;
				hcell96 = new PdfPCell(new Phrase(ConstantValues.HOSPITAL_NAME, headFont1));
				hcell96.setBorder(Rectangle.NO_BORDER);
				hcell96.setHorizontalAlignment(Element.ALIGN_CENTER);
				hcell96.setPaddingLeft(50f);

				table96.addCell(hcell96);
				cell1.addElement(table96);

				PdfPTable table21 = new PdfPTable(1);
				table21.setWidths(new float[] { 5f });
				table21.setSpacingBefore(10);

				PdfPCell hcell20;
				
				hcell20 = new PdfPCell(new Phrase(ADMIN_WISE_SALES_AWS_PDF, headFont1));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_CENTER);
				hcell20.setPaddingLeft(50f);
				table21.addCell(hcell20);
				Timestamp timestamp = patientRegistration.getDateOfJoining();
				DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");

				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(timestamp.getTime());

				String from1 = dateFormat.format(calendar.getTime());

				
				Date date = Calendar.getInstance().getTime();

				// Display a date in day, month, year format
				DateFormat formatter = new SimpleDateFormat(FROM_DATE_FORMAT_ADV_PDF);
				String today = formatter.format(date).toString();

				PdfPCell hcell201;
				
				
				hcell201 = new PdfPCell(new Phrase(FROM_DT_AWS_PDF + from1 + "   " + TILL_DT_AWS_PDF + today, redFont));
				hcell201.setBorder(Rectangle.NO_BORDER);
				hcell201.setHorizontalAlignment(Element.ALIGN_CENTER);
				// hcell19.setPaddingLeft(100f);
				table21.addCell(hcell201);

				// -------------

				PdfPTable table3 = new PdfPTable(2);
				
				table3.setWidths(new float[] { 5f, 5f });
				table3.setSpacingBefore(10);

				PdfPCell hcell1;
			
				hcell1 = new PdfPCell(new Phrase(ADMI_NO_AWS_PDF + regId, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-70f);
				table3.addCell(hcell1);

				
				hcell1 = new PdfPCell(new Phrase(
						PATIENT_NAME_AWS_PDF +patientRegistration.getPatientDetails().getTitle()+". "+ patientRegistration.getPatientDetails().getFirstName() + ConstantValues.ONE_SPACE_STRING
								+ patientRegistration.getPatientDetails().getLastName(),
						redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(5f);
				table3.addCell(hcell1);

				PdfPCell hcell2;
			
				hcell2 = new PdfPCell(new Phrase(
						DOC_NAME_AWS_PDF + patientRegistration.getPatientDetails().getConsultant(), redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setPaddingLeft(-70f);
				table3.addCell(hcell2);

				hcell2 = new PdfPCell(new Phrase(DOA_AWS_PDF + from1, redFont));
				hcell2.setBorder(Rectangle.NO_BORDER);
				hcell2.setPaddingLeft(5f);
				table3.addCell(hcell2);

				
				

				PdfPTable table1 = new PdfPTable(6);
				table1.setWidths(new float[] {2, 3, 2, 2, 2,2 });
				table1.setSpacingBefore(10);

				PdfPCell hcell;
				
				hcell = new PdfPCell(new Phrase(S_NO_AWS_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table1.addCell(hcell);

				hcell = new PdfPCell(new Phrase(ITEM_NAME_AWS_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				
				hcell = new PdfPCell(new Phrase(BATCH_NO_AWS_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				
				hcell = new PdfPCell(new Phrase(EXP_DT_AWS_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				
				hcell = new PdfPCell(new Phrase(QTY_AWS_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);

				
				hcell = new PdfPCell(new Phrase(SALE_VALUE_AWS_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setBackgroundColor(BaseColor.GRAY);
				hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table1.addCell(hcell);


				int count = 0;
				Double total = 0.0;
				String expDt = null;
				for (Sales sale : sales) {
					MedicineDetails medicineDetails = medicineDetailsServiceImpl.findByName(sale.getMedicineName());
					List<MedicineProcurement> medicineProcurements = medicineDetails.getMedicineProcurement();

					for (MedicineProcurement medicineProcurementInfo : medicineProcurements) {
						expDt = medicineProcurementInfo.getExpDate().substring(0, 10);
					}

					PdfPCell cell;
					cell = new PdfPCell(new Phrase(String.valueOf(count = count + 1), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(sale.getMedicineName(), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingLeft(5);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(sale.getBatchNo(), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(expDt, redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(sale.getQuantity()), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					// calculation for saleval
					Double salVal = (double) (sale.getQuantity() * sale.getAmount());
					cell = new PdfPCell(new Phrase(String.valueOf(salVal), redFont));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table1.addCell(cell);

					total += salVal;

				}


				
				
				PdfPTable table2 = new PdfPTable(2);
				table2.setWidths(new float[] { 7f, 5f });
				table2.setSpacingBefore(10);

				PdfPCell hcell21;
				
				hcell21 = new PdfPCell(new Phrase(TOTAL_AWS_PDF, redFont));
				hcell21.setBorder(Rectangle.NO_BORDER);
				hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
			    hcell21.setPaddingRight(-84f);
				table2.addCell(hcell21);

				hcell21 = new PdfPCell(new Phrase(String.valueOf(total), redFont));
				hcell21.setBorder(Rectangle.NO_BORDER);
				hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell21.setPaddingRight(-30f);
				table2.addCell(hcell21);
				
				
				
				PdfPTable table4 = new PdfPTable(4);
				table4.setWidths(new float[] { 4f, 4f, 4f, 4f });
				table4.setSpacingBefore(10);

				PdfPCell hcell22;
				
				hcell22 = new PdfPCell(new Phrase(PRINTED_BY_AWS_PDF, redFont));
				hcell22.setBorder(Rectangle.NO_BORDER);
				hcell22.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell22.setPaddingLeft(-70);
				table4.addCell(hcell22);

				hcell22 = new PdfPCell(new Phrase(createdBy, redFont));
				hcell22.setBorder(Rectangle.NO_BORDER);
				hcell22.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell22.setPaddingLeft(-50f);
				table4.addCell(hcell22);

				
				hcell22 = new PdfPCell(new Phrase(PRINTED_DATE_AWS_PDF, redFont));
				hcell22.setBorder(Rectangle.NO_BORDER);
				hcell22.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table4.addCell(hcell22);

				hcell22 = new PdfPCell(new Phrase(today, redFont));
				hcell22.setBorder(Rectangle.NO_BORDER);
				hcell22.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table4.addCell(hcell22);


				
				
				table1.setWidthPercentage(100f);
				cell1.setFollowingIndent(100f);;
				cell1.addElement(pq);
				cell1.addElement(table21);
				cell1.addElement(table3);
				cell1.addElement(table1);
				cell1.addElement(table2);
				cell1.addElement(table4);
				cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell1.setColspan(2);
				table.addCell(cell1);
				document.add(table);

				document.close();

				pdfBytes = byteArrayOutputStream.toByteArray();
			
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(V1_PAYMENT_VIEWFILE_PATH)
						.path(paymentPdfServiceImpl.getNextPdfId()).toUriString();

			
				patientPaymentPdf = new PatientPaymentPdf(ADMIN_WISE_SALES_AWS_PDF, uri, pdfBytes);
				patientPaymentPdf.setPid(paymentPdfServiceImpl.getNextPdfId());
				paymentPdfServiceImpl.save(patientPaymentPdf);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return patientPaymentPdf;
	}
	
	public String getNextUmr() 
	{
		PatientDetails patientDetails=repo.findFirstByOrderByPatientIdDesc();
		String nextUmr=null;
		if(patientDetails==null)
		{
			nextUmr="UMR0000001";
		}
		else
		{
			String lastUmr=patientDetails.getUmr();
			
			int umrIntId=Integer.parseInt(nextUmr=lastUmr.substring(3));
			umrIntId+=1;
			nextUmr=UMR_SAVEINFO_PDF+String.format("%07d",umrIntId );
		}
		return nextUmr;
	}
	
	public PatientDetails getPatientByUmr(String umr) {
		return repo.findByUmr(umr);
	}
	
	public List<PatientDetails> findAllByOrderByPatientIdDesc()
	{
		return repo.findAllByOrderByPatientIdDesc();
	}
	
	
	
	
	/*
	 * List Of INPATIENT (ONLY FOR 2 DAYS)
	 *
	 */
	public List<Map<String, String>> patientDetails(String type)
	{
		List<Map<String,String>> display=new ArrayList<>();
		String regDate=null;
		String inpatient=null;
		String outpatient=null;
		String docName=EMPTY_STRING;
		String twodayback=EMPTY_STRING;
		
		String today= new Timestamp(System.currentTimeMillis()).toString().substring(0, 10);
		String nextDay=LocalDate.parse(today).plusDays(1).toString();
		
		if(type.equalsIgnoreCase(TWO_DAYS))
		{
		twodayback=LocalDate.parse(today).plusDays(-2).toString();
		}
		else if(type.equalsIgnoreCase(SEVEN_DAYS))
		{
		twodayback=LocalDate.parse(today).plusDays(-7).toString();		
		}
		else if(type.equalsIgnoreCase(FIFTEEN_DAYS))
		{
		twodayback=LocalDate.parse(today).plusDays(-15).toString();		
		}
		else if(type.equalsIgnoreCase(THIRTY_DAYS))
		{
		twodayback=LocalDate.parse(today).plusDays(-30).toString();		
		}
	
		
		List<PatientRegistration> patientRegistration= patientRegistrationServiceImpl.expectOutPatientTwoDays(twodayback,nextDay);
		
		
			for(PatientRegistration patientRegistrationInfo:patientRegistration)
			{
				Map<String,String> displayInfo=new HashMap<>();
				
				float payment=0;
				
				displayInfo.put("name",patientRegistrationInfo.getPatientDetails().getTitle()+". "+patientRegistrationInfo.getPatientDetails().getFirstName()+ConstantValues.ONE_SPACE_STRING+patientRegistrationInfo.getPatientDetails().getLastName());
				
				displayInfo.put("patType",patientRegistrationInfo.getpType());
				
				displayInfo.put("umr",patientRegistrationInfo.getPatientDetails().getUmr());
				
				docName=(patientRegistrationInfo.getVuserD().getMiddleName()!=null) ? patientRegistrationInfo.getVuserD().getFirstName()+ConstantValues.ONE_SPACE_STRING+
																						patientRegistrationInfo.getVuserD().getMiddleName()+ConstantValues.ONE_SPACE_STRING+
																							patientRegistrationInfo.getVuserD().getLastName() 
																							:
																								patientRegistrationInfo.getVuserD().getFirstName()+ConstantValues.ONE_SPACE_STRING+
																										patientRegistrationInfo.getVuserD().getLastName();
				
				
				displayInfo.put("doctor",docName);
				
				//for different format
				String daoDate=String.valueOf(patientRegistrationInfo.getDateOfJoining().toString());
				SimpleDateFormat fromFormat =new SimpleDateFormat(FROM_DATE_FORMAT); //yyyy-MM-dd HH:mm:ss
				SimpleDateFormat toFormat = new SimpleDateFormat(TO_DATE_FORMAT); //dd-MM-yyyy hh:mm a
				try {
					daoDate=toFormat.format(fromFormat.parse(daoDate));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				displayInfo.put("DOJ",daoDate);
				
				List<ChargeBill> patList=chargeBillRepository.findByPatRegIdAndDischarged(patientRegistrationInfo.getRegId());
			
				if(!patList.isEmpty())
				{
					if(patList.get(patList.size()-1).getDichargedDate()!=null)
					{
						//for dischargedate
						String dischargeDate=String.valueOf(patList.get(0).getDichargedDate().toString());
						
						SimpleDateFormat fromFormat1 = new SimpleDateFormat(FROM_DATE_FORMAT); //yyyy-MM-dd HH:mm:ss
						SimpleDateFormat toFormat1 = new SimpleDateFormat(TO_DATE_FORMAT); //dd-MM-yyyy hh:mm a
						try {
							dischargeDate=toFormat1.format(fromFormat1.parse(dischargeDate));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
						displayInfo.put("DOD",dischargeDate);
					
					}
					else
					{
						displayInfo.put("DOD",NOT_DISCHARGED);
					}
				}
				else
				{
						displayInfo.put("DOD",NOT_DISCHARGED);
				}
			
				
				List<RoomBookingDetails> roomBookingDetails=patientRegistrationInfo.getRoomBookingDetails();
				for(RoomBookingDetails roomBookingDetailsInfo:roomBookingDetails)
				{
					displayInfo.put("room", roomBookingDetailsInfo.getBedNo());
				}
				
				List<PatientPayment> patientPayment= paymentRepository.findByPatientRegistration(patientRegistrationInfo.getRegId(),ConstantValues.NO);
				for(PatientPayment patientPaymentInfo:patientPayment)
				{
					payment+=patientPaymentInfo.getAmount();
					
				}
				
				List<ChargeBill> chargeBills=chargeBillRepository.findByPatRegIdStatus(patientRegistrationInfo.getRegId(),ConstantValues.NO);
				for(ChargeBill chargeBillInfo:chargeBills)
				{
					payment+=chargeBillInfo.getNetAmount();
				}
				
				
				
				displayInfo.put("soFar", String.valueOf(Math.round(payment)));
				displayInfo.put("regId", patientRegistrationInfo.getRegId());
				
				displayInfo.put("advance", String.valueOf(patientRegistrationInfo.getAdvanceAmount()));
				
				display.add(displayInfo);
				
			}
			
			return display;
	}
	
	
	
	
	
	//Paying advance amount for patients
	@Transactional
	public PatientPaymentPdf advanceAmount(Map<String,Object> info, String regID,Principal principal)
	{
		String patientName = null;
		String patientType = null;
		String regId = null;
		String regDate = null;

		String refBy;
		String regValidity = null;
		String phone = null;
		String gender = null;
		String age = null;
		String formattedDate = null;
		String amount = null;
		String mop = null;
		String toc = null;
		String consultant = null;
		String state = null;
		String city = null;
		String address = null;
		String father = null;
		String admittedWard = null;
		String department = null;
		Timestamp regDateTimestamp = null;
		String advanceAmount = null;
		long paidAmount = 0;
		String umr = null;
		String billNo;
		String paymentMode;
		long rectAmount = 0;
		String paymentNextBillNo;

		long finalCash = 0; // for final billing
		long finalCard = 0; // for final billing
		long finalCheque = 0; // for final billing

		// createdBy Security
		User userSecurity = userServiceImpl.findByUserName(principal.getName());
		String createdBy = userSecurity.getFirstName() + ConstantValues.ONE_SPACE_STRING + userSecurity.getMiddleName()
				+ ConstantValues.ONE_SPACE_STRING + userSecurity.getLastName();

		PatientRegistration patientRegistrationInfo = patientRegistrationServiceImpl.findByRegId(regID);
		PatientDetails patientDetails = patientRegistrationInfo.getPatientDetails();
		umr = patientDetails.getUmr();
		patientName = patientDetails.getFirstName() + ConstantValues.ONE_SPACE_STRING + patientDetails.getMiddleName()
				+ ConstantValues.ONE_SPACE_STRING + patientDetails.getLastName();
		phone = String.valueOf(patientDetails.getMobile());
		gender = patientDetails.getGender();
		state = patientDetails.getState();
		city = patientDetails.getCity();
		address = patientDetails.getAddress();
		father = patientDetails.getMotherName();

		age = patientDetails.getAge();
		consultant = patientDetails.getConsultant();
		User user = patientRegistrationInfo.getVuserD();
		department = user.getSpecUserJoin().get(0).getDocSpec().getSpecName();

		patientType = patientRegistrationInfo.getpType();
		regId = patientRegistrationInfo.getRegId();
		regValidity = patientRegistrationInfo.getRegValidity();
		regDateTimestamp = patientRegistrationInfo.getDateOfJoining();
		amount = String.valueOf(patientRegistrationInfo.getAdvanceAmount());
		paidAmount = patientRegistrationInfo.getAdvanceAmount() + Integer.parseInt(info.get("advance").toString());

		List<RoomBookingDetails> r = patientRegistrationInfo.getRoomBookingDetails();
		for (RoomBookingDetails roomInfo : r) {
			RoomDetails roomDetails = roomDetailsServiceImpl.findByBedName(roomInfo.getBedNo());
			admittedWard = roomDetails.getRoomType();
		}

		long advanceAmt = patientRegistrationInfo.getAdvanceAmount();
		long adAmount = Long.parseLong(info.get("advance").toString());
		advanceAmt += Integer.parseInt(info.get("advance").toString());
		patientRegistrationInfo.setAdvanceAmount(advanceAmt);
		if (patientRegistrationInfo.getPatientDetails().getvRefferalDetails() != null) {
			refBy = patientRegistrationInfo.getPatientDetails().getvRefferalDetails().getRefName();
		}
		advanceAmount = info.get("advance").toString();
		PatientPayment paymentList = new PatientPayment();
		paymentList.setModeOfPaymant(info.get("mode").toString());
		paymentMode = paymentList.getModeOfPaymant();
		if (paymentMode.equalsIgnoreCase(ConstantValues.CARD)
				|| paymentMode.equalsIgnoreCase(ConstantValues.CREDIT_CARD)
				|| paymentMode.equalsIgnoreCase(ConstantValues.DEBIT_CARD)
				|| paymentMode.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {

			paymentList.setReferenceNumber(info.get("referenceNo").toString());

		}
		paymentList.setAmount(Integer.parseInt(info.get("advance").toString()));
		paymentList.setInsertedDate(new Timestamp(System.currentTimeMillis()));
		paymentList.setPaid(ConstantValues.YES);

		paymentList.setTypeOfCharge(ConstantValues.ADVANCE);

		rectAmount = paymentList.getAmount();
		paymentList.setBillNo(patientPaymentServiceImpl.findNextBillNo());
		paymentNextBillNo = paymentList.getBillNo();
		paymentList.setRaisedById(userSecurity.getUserId());
		paymentList.setPatientRegistration(patientRegistrationInfo);
		paymentRepository.save(paymentList);

		patientRegistrationServiceImpl.save(patientRegistrationInfo);

		// Final Billing

		if (paymentMode.equalsIgnoreCase(ConstantValues.CASH)) {
			finalCash = rectAmount;
		} else if (paymentMode.equalsIgnoreCase(ConstantValues.CARD)) {
			finalCard = rectAmount;
		} else if (paymentMode.equalsIgnoreCase(ConstantValues.CHEQUE)) {
			finalCheque = rectAmount;
		}

		// Cash + card

		PatientRegistration patientRegistration = patientRegistrationServiceImpl.findByRegId(regId);
		if (paymentMode.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {
			CashPlusCard cashPlusCard = new CashPlusCard();
			cashPlusCard.setInsertedBy(userSecurity.getUserId());
			cashPlusCard.setBillNo(paymentNextBillNo);
			cashPlusCard.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			cashPlusCard.setDescription("Patient Advance");
			cashPlusCard.setPatientRegistrationCashCard(patientRegistration);

			List<Map<String, String>> multiMode = (List<Map<String, String>>) info.get("multiMode");
			for (Map<String, String> multiModeInfo : multiMode) {

				if (multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CASH)) {

					cashPlusCard.setCashAmount(Long.parseLong(multiModeInfo.get("amount")));
					finalCash = Long.parseLong(multiModeInfo.get("amount"));
				} else if (multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CARD)) {

					cashPlusCard.setCardAmount(Long.parseLong(multiModeInfo.get("amount")));
					finalCard = Long.parseLong(multiModeInfo.get("amount"));
				} else if (multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CHEQUE)) {

					cashPlusCard.setChequeAmount(Long.parseLong(multiModeInfo.get("amount")));
					finalCheque = Long.parseLong(multiModeInfo.get("amount"));
				}

			}
			cashPlusCardServiceImpl.save(cashPlusCard);

		}

		if (patientType.equalsIgnoreCase(ConstantValues.INPATIENT)) {
			FinalBilling finalBilling = new FinalBilling();
			finalBilling.setBillNo(paymentNextBillNo);
			finalBilling.setBillType("Patient Registration");
			finalBilling.setCardAmount(finalCard);
			finalBilling.setCashAmount(finalCash);
			finalBilling.setChequeAmount(finalCheque);
			finalBilling.setFinalAmountPaid(rectAmount);
			finalBilling.setFinalBillUser(userSecurity);
			finalBilling.setName(patientName);
			finalBilling.setRegNo(regId);
			finalBilling.setPaymentType(mop);
			finalBilling.setTotalAmount(rectAmount);
			finalBilling.setUmrNo(umr);
			finalBillingServcieImpl.computeSave(finalBilling);
		}

		// Display a date in day, month, year format
		Date date = new Date(regDateTimestamp.getTime());

		DateFormat formatter = new SimpleDateFormat(FROM_DATE_FORMAT_ADV_PDF); // "dd-MMM-yyyy hh.mm aa"
		String today = formatter.format(date).toString();

		date = new Date(new Timestamp(System.currentTimeMillis()).getTime());
		formatter = new SimpleDateFormat(FROM_DATE_FORMAT_ADV_PDF);
		String printed = formatter.format(date).toString();

		PatientPaymentPdf patientPaymentPdf = null;
		byte[] pdfBytes = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		Document document = new Document(PageSize.A4_LANDSCAPE);

		//shantharam addr

		String addrss = "";

		try {

			Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

			Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
			Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
			PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

			Resource fileResourcee = resourceLoader.getResource(UDBHAVA_PNG_CLASSPATH);

			Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());

			document.open();
			PdfPTable table = new PdfPTable(2);

			img.scaleAbsolute(75, 95);
			table.setWidthPercentage(105);

			Phrase pq = new Phrase(new Chunk(img, 0, -80));
			pq.add(new Chunk(addrss, redFont));
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
			table99.setWidths(new float[] { 3f, 1f, 5f });
			table99.setSpacingBefore(10);

			PdfPCell hcell90;

			hcell90 = new PdfPCell(new Phrase(PATIENT_ADV_PDF, redFont));
			hcell90.setBorder(Rectangle.NO_BORDER);
			hcell90.setPaddingBottom(-7f);
			hcell90.setPaddingLeft(-25f);
			table99.addCell(hcell90);

			hcell90 = new PdfPCell(new Phrase(COLON, redFont));
			hcell90.setBorder(Rectangle.NO_BORDER);
			hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell90.setPaddingLeft(-45f);
			table99.addCell(hcell90);

			hcell90 = new PdfPCell(new Phrase(patientName, redFont));
			hcell90.setBorder(Rectangle.NO_BORDER);
			hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell90.setPaddingBottom(-7f);
			hcell90.setPaddingLeft(-60f);
			table99.addCell(hcell90);

			cell3.addElement(table99);

			PdfPTable table2 = new PdfPTable(6);
			table2.setWidths(new float[] { 3f, 1f, 5f, 3f, 1f, 4f });
			table2.setSpacingBefore(10);

			PdfPCell hcell1;

			hcell1 = new PdfPCell(new Phrase(UMR_NO_ADV_PDF, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setPaddingLeft(-25f);
			hcell1.setPaddingTop(-5f);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(COLON, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-10f);
			hcell1.setPaddingTop(-5f);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(umr, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-15f);
			hcell1.setPaddingTop(-5f);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setPaddingRight(34f);
			hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setPaddingRight(34f);
			hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setPaddingRight(34f);
			hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table2.addCell(hcell1);

			PdfPCell hcell4;

			hcell4 = new PdfPCell(new Phrase(RECT_NO_ADV_PDF, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setPaddingLeft(-25f);
			// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(COLON, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(-10f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(regId, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(-15f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(RECT_DATE_ADV_PDF, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingRight(-27.5f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(COLON, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell4.setPaddingRight(-0.1f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(today.substring(0, 11), redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingRight(-20.5f);
			table2.addCell(hcell4);

			PdfPCell hcell15;

			hcell15 = new PdfPCell(new Phrase(AGE_SEX_ADV_PDF, redFont));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setPaddingLeft(-25f);
			// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcell15);

			hcell15 = new PdfPCell(new Phrase(COLON, redFont));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell15.setPaddingLeft(-10f);
			table2.addCell(hcell15);

			hcell15 = new PdfPCell(new Phrase(age + BACKWARD_SLASH_ADV_PDF + gender, redFont));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell15.setPaddingLeft(-15f);
			table2.addCell(hcell15);

			hcell15 = new PdfPCell(new Phrase(PHONE_ADV_PDF, redFont));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setPaddingRight(-27.5f);
			hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcell15);

			hcell4 = new PdfPCell(new Phrase(COLON, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell4.setPaddingRight(-0.1f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(phone, redFont));
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

			hcell19 = new PdfPCell(new Phrase(ADVANCE_RECIEPT_ADV_PDF, headFont1));
			hcell19.setBorder(Rectangle.NO_BORDER);
			hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
			// hcell19.setPaddingLeft(-70f);
			table21.addCell(hcell19);

			cell19.setFixedHeight(20f);
			cell19.setColspan(2);
			cell19.addElement(table21);
			table.addCell(cell19);

			PdfPCell cell4 = new PdfPCell();

			PdfPTable table3 = new PdfPTable(6);
			table3.setWidths(new float[] { 5f, 1f, 6f, 9f, 1f, 6f });
			table3.setSpacingBefore(10);

			PdfPCell hcell;

			hcell = new PdfPCell(new Phrase(FATHER_ADV_PDF, redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setPaddingLeft(-50f);
			table3.addCell(hcell);

			hcell = new PdfPCell(new Phrase(COLON, redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell.setPaddingLeft(-80f);
			table3.addCell(hcell);

			hcell = new PdfPCell(new Phrase(father, redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell.setPaddingLeft(-80f);
			table3.addCell(hcell);

			Font redFont5 = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);

			hcell = new PdfPCell(new Phrase(ADMITTED_WARD_ADV_PDF, redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell);

			hcell = new PdfPCell(new Phrase(COLON, redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell.setPaddingLeft(-15f);
			table3.addCell(hcell);

			hcell = new PdfPCell(new Phrase(admittedWard, redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell.setPaddingLeft(-20f);
			table3.addCell(hcell);

			PdfPCell hcell11;

			hcell11 = new PdfPCell(new Phrase(ADMITTED_DATE, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setPaddingLeft(-50f);
			table3.addCell(hcell11);

			hcell11 = new PdfPCell(new Phrase(COLON, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell11.setPaddingLeft(-80f);
			table3.addCell(hcell11);

			hcell11 = new PdfPCell(new Phrase(today, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell11.setPaddingLeft(-80f);
			table3.addCell(hcell11);

			String dpt = null;
			if (department != null) {
				dpt = department;
			} else {
				dpt = EMPTY_STRING;
			}

			hcell11 = new PdfPCell(new Phrase(DEPARTMENT_ADV_PDF, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell11);

			hcell11 = new PdfPCell(new Phrase(COLON, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell11.setPaddingLeft(-15f);
			table3.addCell(hcell11);

			hcell11 = new PdfPCell(new Phrase(dpt, redFont5));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell11.setPaddingLeft(-20f);
			table3.addCell(hcell11);

			PdfPCell hcell14;

			hcell14 = new PdfPCell(new Phrase(CONSULTANT_ADV_PDF, redFont));
			hcell14.setBorder(Rectangle.NO_BORDER);
			hcell14.setPaddingLeft(-50f);
			table3.addCell(hcell14);

			hcell14 = new PdfPCell(new Phrase(COLON, redFont));
			hcell14.setBorder(Rectangle.NO_BORDER);
			hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell14.setPaddingLeft(-80f);
			table3.addCell(hcell14);

			hcell14 = new PdfPCell(new Phrase(consultant, redFont));
			hcell14.setBorder(Rectangle.NO_BORDER);
			hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell14.setPaddingLeft(-80f);
			table3.addCell(hcell14);

			hcell14 = new PdfPCell(new Phrase(RECT_AMOUNT_ADV_PDF, redFont));
			hcell14.setBorder(Rectangle.NO_BORDER);
			hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell14);

			hcell14 = new PdfPCell(new Phrase(COLON, redFont));
			hcell14.setBorder(Rectangle.NO_BORDER);
			hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell14.setPaddingLeft(-15f);
			table3.addCell(hcell14);

			hcell14 = new PdfPCell(new Phrase(advanceAmount, redFont));
			hcell14.setBorder(Rectangle.NO_BORDER);
			hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell14.setPaddingLeft(-20f);
			table3.addCell(hcell14);

			PdfPCell hcell16;

			hcell16 = new PdfPCell(new Phrase(ORG_ADV_PDF, redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setPaddingLeft(-50f);
			// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell16);

			hcell16 = new PdfPCell(new Phrase(COLON, redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell16.setPaddingLeft(-80f);
			table3.addCell(hcell16);

			hcell16 = new PdfPCell(new Phrase(ConstantValues.HOSPITAL_NAME, redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell16.setPaddingLeft(-80f);
			table3.addCell(hcell16);

			hcell16 = new PdfPCell(new Phrase(TOTAL_ADV_AMOUNT_RECEIVED, redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell16);

			hcell16 = new PdfPCell(new Phrase(COLON, redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell16.setPaddingLeft(-15f);
			table3.addCell(hcell16);

			hcell16 = new PdfPCell(new Phrase(String.valueOf(paidAmount), redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell16.setPaddingLeft(-20f);
			table3.addCell(hcell16);

			PdfPCell hcell17;

			hcell17 = new PdfPCell(new Phrase(ADDRESS_ADV_PDF, redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setPaddingLeft(-50f);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase(COLON, redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell17.setPaddingLeft(-80f);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase(address, redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell17.setPaddingLeft(-80f);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell17.setPaddingLeft(-15f);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell17.setPaddingLeft(-20f);
			table3.addCell(hcell17);

			PdfPCell hcell18;

			hcell18 = new PdfPCell(new Phrase(CITY_ADV_PDF, redFont));
			hcell18.setBorder(Rectangle.NO_BORDER);
			hcell18.setPaddingLeft(-50f);
			table3.addCell(hcell18);

			hcell18 = new PdfPCell(new Phrase(COLON, redFont));
			hcell18.setBorder(Rectangle.NO_BORDER);
			hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell18.setPaddingLeft(-80f);
			table3.addCell(hcell18);

			hcell18 = new PdfPCell(new Phrase(city, redFont));
			hcell18.setBorder(Rectangle.NO_BORDER);
			hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell18.setPaddingLeft(-80f);
			table3.addCell(hcell18);

			hcell18 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell18.setBorder(Rectangle.NO_BORDER);
			hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell18);

			hcell18 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell18.setBorder(Rectangle.NO_BORDER);
			hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell18.setPaddingLeft(-15f);
			table3.addCell(hcell18);

			hcell18 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell18.setBorder(Rectangle.NO_BORDER);
			hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell18.setPaddingLeft(-20f);
			table3.addCell(hcell18);

			PdfPCell hcell20;

			hcell20 = new PdfPCell(new Phrase(STATE_ADV_PDF, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setPaddingLeft(-50f);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(COLON, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20.setPaddingLeft(-80f);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(state, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20.setPaddingLeft(-80f);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell20);

			PdfPTable table91 = new PdfPTable(1);
			table91.setWidths(new float[] { 5f });
			table91.setSpacingBefore(10);

			cell4.setColspan(2);
			cell4.addElement(table3);

			PdfPCell hcell98;
			hcell98 = new PdfPCell(new Phrase("Validity       :" + TWO_VISITS_BEFORE + regValidity
					+ RECEIVED_WITH_THANKS_FROM + patientName + "," + A_SUM_OF_RS_SAVEINFO_PDF + adAmount + "\n\n"
					+ IN_WORDS_RS_SAVEINFO_PDF + numberToWordsConverter.convert(adAmount), redFont));
			hcell98.setBorder(Rectangle.NO_BORDER);
			hcell98.setPaddingLeft(-50f);
			hcell98.setPaddingTop(20);
			table91.addCell(hcell98);
			cell4.addElement(table91);
			table.addCell(cell4);

			PdfPCell cell5 = new PdfPCell();

			PdfPTable table35 = new PdfPTable(2);
			table35.setWidths(new float[] { 5f, 4f });
			table35.setSpacingBefore(10);

			PdfPCell hcell21;
			Font redFont3 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
			hcell21 = new PdfPCell(new Phrase(STAR + umr + STAR, redFont3));
			hcell21.setBorder(Rectangle.NO_BORDER);
			hcell21.setPaddingLeft(-50f);
			table35.addCell(hcell21);

			hcell21 = new PdfPCell(new Phrase(STAR + regId + STAR, redFont3));
			hcell21.setBorder(Rectangle.NO_BORDER);
			hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table35.addCell(hcell21);

			PdfPCell hcell12;

			hcell12 = new PdfPCell(new Phrase(CREATED_BY_ADV_PDF + createdBy, redFont));
			hcell12.setBorder(Rectangle.NO_BORDER);
			hcell12.setPaddingTop(10f);
			hcell12.setPaddingLeft(-50f);
			table35.addCell(hcell12);

			hcell12 = new PdfPCell(new Phrase(CREATED_DATE + printed, redFont));
			hcell12.setBorder(Rectangle.NO_BORDER);
			hcell12.setPaddingTop(10f);
			hcell12.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table35.addCell(hcell12);

			PdfPCell hcell13;

			hcell13 = new PdfPCell(new Phrase(PRINTED_BY_ADV_PDF + createdBy, redFont));
			hcell13.setBorder(Rectangle.NO_BORDER);
			hcell13.setPaddingLeft(-50f);
			table35.addCell(hcell13);

			hcell13 = new PdfPCell(new Phrase(PRINTED_DATE + printed, redFont));
			hcell13.setBorder(Rectangle.NO_BORDER);
			hcell13.setPaddingRight(3f);
			hcell13.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table35.addCell(hcell13);

			PdfPCell hcell23;
			hcell23 = new PdfPCell(new Phrase(EMPTY_STRING));
			hcell23.setBorder(Rectangle.NO_BORDER);
			table35.addCell(hcell23);

			hcell23 = new PdfPCell(new Phrase(AUTHORIZED_SIGNATURE, headFont));
			hcell23.setBorder(Rectangle.NO_BORDER);
			hcell23.setPaddingTop(18f);
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
			String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(V1_PAYMENT_VIEWFILE_PATH)
					.path(paymentPdfServiceImpl.getNextPdfId()).toUriString();

			patientPaymentPdf = new PatientPaymentPdf(regId + " Advance Payment", uri, pdfBytes);
			patientPaymentPdf.setPid(paymentPdfServiceImpl.getNextPdfId());
			paymentPdfServiceImpl.save(patientPaymentPdf);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return patientPaymentPdf;
	}

	
	public List<PatientDetails> findAll()
	{
		return repo.findAll();
	}
	
	public List<Object> pageLoad()
	{

		List<User> user = userServiceImpl.findByUserRole(DOCTOR);
		umrGenerator = new UmrGenerator(patientDetailsServiceImpl.getNextUmr());
		regGenerator = new RegGenerator(patientRegistrationServiceImpl.getNextRegId());
		List<Object> a1 = new ArrayList<>();
		Iterable<PatientTypes> patientTypes = patientTypeServiceImpl.findAll();
		List<ReferralDetails> referralDetails = referralDetailsServiceImpl.findDistinct();
		for (ReferralDetails referralDetailsInfo : referralDetails) {
			System.out.println(referralDetailsInfo.getRefName());
		}
		List<RoomDetails> freeRoom = new ArrayList<>();
		// Empty Bed
		List<RoomDetails> roomDetails = roomDetailsServiceImpl.findAll();
		for (RoomDetails roomDetailsinfo : roomDetails) {
			RoomBookingDetails roomBookingDetailsInfo = roomBookingDetailsServiceImpl
					.getroomStatus(roomDetailsinfo.getBedId());
			if (roomBookingDetailsInfo == null) {

				roomDetailsinfo.setStatus(ALLOCATE);
				freeRoom.add(roomDetailsinfo);
			} else {

				roomDetailsinfo.setStatus(OCCUPIED);
				freeRoom.add(roomDetailsinfo);

			}
		}
		a1.add(freeRoom);
		a1.add(patientTypes);
		a1.add(referralDetails);
		a1.add(user);
		// String nextId=patientDetailsServiceImpl.getFirstByOrderByPIdDesc();
		a1.add(regGenerator);
		a1.add(umrGenerator);
		return a1;

	}
	
	/*
	 * get all patients for existing patient
	 */
	public List<Object> getAllPd()
	{
		List<Object> pinfo = new ArrayList<>();

		List<PatientDetails> patientDetails = patientDetailsServiceImpl.findAll();
		patientDetails.forEach(System.out::println);
		
		List<PatientDetails> patientDetailsFiltered = new ArrayList<>();
		List<User> user = userServiceImpl.findByRole(DOCTOR);
		pinfo.add(user);
		List<RoomDetails> freeRoom = new ArrayList<>();
		// Empty Bed

		List<RoomDetails> roomDetails = roomDetailsServiceImpl.findAll();
		for (RoomDetails roomDetailsinfo : roomDetails) {
			RoomBookingDetails roomBookingDetailsInfo = roomBookingDetailsServiceImpl
					.getroomStatus(roomDetailsinfo.getBedId());
			if (roomBookingDetailsInfo == null) {
				roomDetailsinfo.setStatus(ALLOCATE);
				freeRoom.add(roomDetailsinfo);
			} else {

				roomDetailsinfo.setStatus(OCCUPIED);
				freeRoom.add(roomDetailsinfo);

			}
		}

		for (PatientDetails pList : patientDetails) {
			PatientRegistration patientRegistration = patientRegistrationServiceImpl
					.findLatestReg(pList.getPatientId());

			if (patientRegistration != null) {
				Set<PatientRegistration> patientRegistrationInfo = new HashSet<>();

				patientRegistrationInfo.add(patientRegistration);
				pList.setvPatientRegistration(patientRegistrationInfo);
				patientDetailsFiltered.add(pList);

			}
		}
		pinfo.add(patientDetailsFiltered);
		pinfo.add(freeRoom);
		return pinfo;
	}
	
	
	/*
	 * Updating patient
	 */
	public PatientDetails updatePatient( PatientDetails patientDetails, String umr)
	{
		PatientDetails  patientDetailsInfo=patientDetailsServiceImpl.getPatientByUmr(umr);
		if(patientDetails.getEmail()!=null)
		{
			patientDetailsInfo.setEmail(patientDetails.getEmail());
		}
		if(patientDetails.getMobile()!=0) {
			patientDetailsInfo.setMobile(patientDetails.getMobile());
		}
		if(patientDetails.getAddress()!=null) {
			patientDetailsInfo.setAddress(patientDetails.getAddress());
		}
		return patientDetailsRepository.save(patientDetailsInfo);
	}

	/*
	 * Generate Blank pdf
	 */
	public PatientPaymentPdf blankPrescription(String regId)
	{
		
		String blankValue=regId+BLANK_PRESCRIPTION;
		PatientPaymentPdf patientPaymentPdf = paymentPdfServiceImpl.getBlankPdf(blankValue);
		
		return patientPaymentPdf;
		
	}	
	
	@Transactional
	public PatientPaymentPdf saveInfo(PatientDetails patientDetail,Principal principal) throws Exception
	{
		PatientDetails patientDetails = patientDetail;

		User userSecurity = userServiceImpl.findByUserName(principal.getName());

		/*
		 * Checking if user is already exists
		 */
		PatientDetails exPatientDetails = patientDetails;
		String exMobile = String.valueOf(exPatientDetails.getMobile());
		String exFname = exPatientDetails.getFirstName();
		String exMname = exPatientDetails.getMiddleName();
		String exLname = exPatientDetails.getLastName();

		List<PatientDetails> exP = patientDetailsServiceImpl.patientAlreadyExists(exMobile, exFname, exLname);
		String exDate = String.valueOf(new Timestamp(System.currentTimeMillis())).substring(0, 10);
		for (PatientDetails exPList : exP) {
			PatientRegistration pr = patientRegistrationServiceImpl.patientAlredyExists(exPList.getPatientId(), exDate);

			if (pr != null) {
				Logger.error(ConstantValues.PATIENT_ALREADY_EXISTS_ERROR_MSG);
				throw new RuntimeException(ConstantValues.PATIENT_ALREADY_EXISTS_ERROR_MSG + "\n"
						+ exP.get(0).getFirstName() + ConstantValues.ONE_SPACE_STRING + exP.get(0).getMiddleName()
						+ ConstantValues.ONE_SPACE_STRING + exP.get(0).getLastName() + " \n" + exP.get(0).getMobile());
			}

		}

		// For payment
		String createdBy = null;
		String patientName = null;
		String patientType = null;
		String umr = null;
		String regId = null;
		String regDate = null;
		Timestamp regDateTimestamp = null;
		String refBy = null;
		String regValidity = null;
		String phone = null;
		String gender = null;
		String age = null;
		String formattedDate = null;
		String firstname = null;
		String middlename = null;
		String lastname = null;
		long mob = 0;
		String amount = null;
		String mop = null;
		String toc = null;
		String consultant = null;
		String deptName = null;
		String state = null;

		String modeOfPaymentAdvance = EMPTY_STRING;
		String city = null;
		String address = null;
		String father = null;
		String admittedWard = null;
		String department = null;
		String advanceAmount = null;
		long rectAmount = 0;
		long amtPdf = 0;
		long adPdf = 0;
		String docamount = null;
		long docamtPdf = 0;
		String docmop = null;
		String doctoc = null;
		String serviceRoomType = EMPTY_STRING;

		long finalCash = 0; // for final billing
		long finalCard = 0; // for final billing
		long finalCheque = 0; // for final billing

		Map<String, String> refInfo = patientDetails.getRefName();

		PatientRegistration patientRegistrationChargeBill = null;

		// Patient Payment bill no
		String paymentNextBillNo = patientPaymentServiceImpl.findNextBillNo();

		if (!refInfo.isEmpty()) {
			ReferralDetails refDetails = referralDetailsServiceImpl.findBySourceAndRefName(refInfo.get("source"),
					refInfo.get("refName"));
			if (refDetails == null && !refInfo.get("source").equals(EMPTY_STRING)
					&& !refInfo.get("refName").equals(EMPTY_STRING)) {
				ReferralDetails refObject = new ReferralDetails();
				refObject.setRefAdd(refInfo.get("adr"));
				refObject.setRefName(refInfo.get("refName"));
				refObject.setRefPhone(Long.parseLong(refInfo.get("phone")));
				refObject.setSource(refInfo.get("source"));
				referralDetailsServiceImpl.save(refObject);
				patientDetails.setvRefferalDetails(refObject);
			} else if (!refInfo.get("source").equals(EMPTY_STRING) && refInfo.get("refName").equals(EMPTY_STRING)) {
				patientDetails.setvRefferalDetails(null);
			} else if (refDetails != null) {
				patientDetails.setvRefferalDetails(refDetails);
			} else {
				patientDetails.setvRefferalDetails(null);
			}
			if (patientDetails.getvRefferalDetails() != null) {
				refBy = patientDetails.getvRefferalDetails().getRefName();
			} else {
				refBy = EMPTY_STRING;
			}

		}

		String fn = null;
		String mn = null;
		String ln = null;
		if (userSecurity.getFirstName() == null) {
			fn = ConstantValues.ONE_SPACE_STRING;
		} else {
			fn = userSecurity.getFirstName();
		}

		if (userSecurity.getMiddleName() == null) {
			mn = EMPTY_STRING;
		} else {
			mn = userSecurity.getMiddleName();
		}
		if (userSecurity.getLastName() == null) {
			ln = ConstantValues.ONE_SPACE_STRING;
		} else {
			ln = userSecurity.getLastName();
		}
		if (mn.equals(EMPTY_STRING)) {
			createdBy = fn + ConstantValues.ONE_SPACE_STRING + ln;

		} else {
			createdBy = fn + ConstantValues.ONE_SPACE_STRING + mn + ConstantValues.ONE_SPACE_STRING + ln;

		}

		Set<PatientRegistration> patientRegistrations = null;
		PatientPayment paymentListAdvance = null;
		PatientDetails patient = patientDetails;
		patient.setUmr(patientDetailsServiceImpl.getNextUmr());
		MarketingQuestions marketingQuestions = marketingQuestionsServiceImpl
				.findByQuestion(patient.getMarketingName());
		patient.setvMarketingQuestion(marketingQuestions);
		PatientTypes patientTypes = patientTypeServiceImpl.findByPType(patient.getPatientTypeName());
		String uName = patient.getConsultant();
		String docName[] = uName.split("-");
		consultant = docName[0];
		patient.setConsultant(docName[0]);
		firstname = patient.getFirstName();
		lastname = patient.getLastName();
		mob = patient.getMobile();
		father = patient.getMotherName();
		address = patient.getAddress();
		city = patient.getCity();
		state = patient.getState();
		patient.setDischarged("-");

		if (patient.getAgeCalculation() != 0) {
			int ageCalculation = patient.getAgeCalculation();
			Calendar ageCal = Calendar.getInstance();
			ageCal.setTimeInMillis(new Timestamp(System.currentTimeMillis()).getTime());
			ageCal.add(Calendar.YEAR, -ageCalculation);
			DateFormat dateFormatw = new SimpleDateFormat(FROM_DATE_FORMAT);
			patient.setDob(Timestamp.valueOf(dateFormatw.format(ageCal.getTime())));
		}
		// Calculating age
		LocalDate todayLocal = LocalDate.now();
		LocalDate birthday = patient.getDob().toLocalDateTime().toLocalDate();
		Period p = Period.between(birthday, todayLocal);
		int accurate_age = 0;

		if (p.getMonths() >= 5) {
			accurate_age = p.getYears() + 1;
		} else {
			accurate_age = p.getYears();
		}

		if (p.getYears() == 0) {
			if (p.getDays() == 0) {
				String lessThanOne = String
						.valueOf(p.getYears() + Y_SPACE + p.getMonths() + M_SPACE + String.valueOf(1) + D);
				patient.setAge(String.valueOf(lessThanOne));
			} else {
				int days = p.getDays() + 1;
				String lessThanOne = String.valueOf(p.getYears() + Y_SPACE + p.getMonths()) + M_SPACE
						+ String.valueOf(days + D);
				patient.setAge(String.valueOf(lessThanOne));
			}
		} else {
			int days = 0;
			if (p.getMonths() == 0) {
				days = p.getDays() + 1;
				patient.setAge(
						String.valueOf(String.valueOf(p.getYears() + Y_SPACE + p.getMonths()) + M_SPACE + days + D));
			} else {
				days = p.getDays() + 1;
				patient.setAge(
						String.valueOf(String.valueOf(p.getYears() + Y_SPACE + p.getMonths()) + M_SPACE + days + D));

			}
		}
		age = patient.getAge();

		patientRegistrations = patient.getvPatientRegistration();

		for (PatientRegistration patientRegistration : patientRegistrations) {
			patientRegistration.setRegId(patientRegistrationServiceImpl.getNextRegId());
			regId = patientRegistration.getRegId();
			patientRegistration.setCreatedBy(userSecurity.getUserId());

			// for payment
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			Date dateInfo = new Date();
			dateInfo.setTime(timestamp.getTime());

			formattedDate = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(dateInfo);

			// for payment
			gender = patient.getGender();
			phone = String.valueOf(patient.getMobile());
			umr = patient.getUmr();

			String pfn = null;
			String pmn = null;
			String pln = null;

			if (patient.getFirstName() == null) {
				pfn = ConstantValues.ONE_SPACE_STRING;
			} else {
				pfn = patient.getFirstName();
			}
			if (patient.getMiddleName() == null) {
				pmn = EMPTY_STRING;
			} else {
				pmn = patient.getMiddleName();
			}
			if (patient.getLastName() == null) {
				pln = EMPTY_STRING;
			} else {
				pln = patient.getLastName();
			}
			if (pmn.equalsIgnoreCase(EMPTY_STRING)) {
				patientName = patient.getTitle() + ". " + patient.getFirstName() + ConstantValues.ONE_SPACE_STRING
						+ patient.getLastName();
			} else {
				patientName = patient.getTitle() + ". " + patient.getFirstName() + ConstantValues.ONE_SPACE_STRING
						+ patient.getMiddleName() + ConstantValues.ONE_SPACE_STRING + patient.getLastName();
			}

			// TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
			patientRegistration.setDateOfJoining(patientRegistration.getRegDate());
			patientRegistration.setRegDate(patientRegistration.getRegDate());
			regDate = patientRegistration.getRegDate().toString().substring(0, 10);
			regDateTimestamp = patientRegistration.getRegDate();
			regValidity = LocalDate.parse(regDate).plusDays(7).toString();

			// Reg validity Format
			try {
				SimpleDateFormat fromFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
				SimpleDateFormat toFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT_TO);
				regValidity = toFormat.format(fromFormat.parse(regValidity));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			patientRegistration.setRegValidity(regValidity);
			patientRegistration.setCreatedAt(timestamp);
			patientRegistration.setpatientType(patientTypes);
			patientRegistration.setpType(patientTypes.getpType());
			patientType = patientTypes.getpType();
			patientRegistration.setPatientDetails(patient);

			User user = userServiceImpl.findOneByUserId(docName[1]);
			patientRegistration.setVuserD(user);
			department = user.getSpecUserJoin().get(0).getDocSpec().getSpecName();

			paymentListAdvance = new PatientPayment();

			// Room booking
			if (patientRegistration.getpType().equals(ConstantValues.INPATIENT)) {

				patientRegistration.setTriggeredDate(timestamp);
				List<RoomBookingDetails> roomBookingDetails = patientRegistration.getRoomBookingDetails();
				for (RoomBookingDetails roomBookingDetailsInfo : roomBookingDetails) {
					roomBookingDetailsInfo.setBookingId(roomBookingDetailsServiceImpl.getNextBookingId());
					patientRegistration.setAdvanceAmount(roomBookingDetailsInfo.getAdvanceAmount());
					patientRegistration.setEstimationAmount(roomBookingDetailsInfo.getEstimateAmount());
					roomBookingDetailsInfo.setStatus(1);
					RoomDetails roomDetails = roomDetailsServiceImpl.findByBedName(roomBookingDetailsInfo.getBedNo());
					roomBookingDetailsInfo.setCostSoFar((int) roomDetails.getCostPerDay());
					roomBookingDetailsInfo.setFromDate(roomBookingDetailsInfo.getFromDate());
					roomBookingDetailsInfo.setToDate(roomBookingDetailsInfo.getToDate());
					advanceAmount = String.valueOf(roomBookingDetailsInfo.getAdvanceAmount());
					adPdf = roomBookingDetailsInfo.getAdvanceAmount();
					serviceRoomType = roomDetails.getRoomType();
					patient.setDischarged("-");
					try {
						String fromString = roomBookingDetailsInfo.getFromDate().toString();
						System.out.println(roomBookingDetailsInfo.getFromDate().toLocalDateTime());
						DateFormat formatterIST = new SimpleDateFormat(FROM_DATE_FORMAT);

						formatterIST.setTimeZone(TimeZone.getTimeZone(ASIA_KOLKATA_TIMEZONE)); // better than using IST
						Date date = formatterIST.parse(fromString);
						System.out.println(formatterIST.format(date)); // output: 15-05-2014 00:00:00
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					Timestamp times = roomBookingDetailsInfo.getToDate();

					if (times != null) {
						DateFormat dateFormat = new SimpleDateFormat(FROM_DATE_FORMAT);
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(times.getTime());
						calendar.add(Calendar.DATE, -1);
						roomBookingDetailsInfo
								.setTentativeDischargeDate(Timestamp.valueOf(dateFormat.format(calendar.getTime())));
					}
					roomBookingDetailsInfo.setRoomDetails(roomDetails);
					admittedWard = roomDetails.getRoomType();
					roomBookingDetailsInfo.setPatientRegistrationBooking(patientRegistration);
					// roomBookingDetailsServiceImpl.save(roomBookingDetailsInfo);
				}

				/*
				 * // DMO charges
				 * 
				 * PatientPayment dmoPayment=new PatientPayment(); dmoPayment.setAmount(400);
				 * dmoPayment.setInsertedDate(new Timestamp(System.currentTimeMillis()));
				 * dmoPayment.setModeOfPaymant("Due"); dmoPayment.setPaid("No");
				 * dmoPayment.setPatientRegistration(patientRegistration);
				 * dmoPayment.setRaisedById(userSecurity.getUserId());
				 * dmoPayment.setTypeOfCharge("DMO Charge"); paymentRepository.save(dmoPayment);
				 * 
				 * // Nursing Charges
				 * 
				 * PatientPayment nursePayment=new PatientPayment();
				 * nursePayment.setAmount(400); nursePayment.setInsertedDate(new
				 * Timestamp(System.currentTimeMillis())); nursePayment.setModeOfPaymant("Due");
				 * nursePayment.setPaid("No");
				 * nursePayment.setPatientRegistration(patientRegistration);
				 * nursePayment.setRaisedById(userSecurity.getUserId());
				 * nursePayment.setTypeOfCharge("Nursing Charge");
				 * paymentRepository.save(nursePayment);
				 */

				paymentListAdvance.setAmount(patientRegistration.getAdvanceAmount());
				paymentListAdvance.setInsertedDate(new Timestamp(System.currentTimeMillis()));
				paymentListAdvance.setPaid(ConstantValues.YES);
				paymentListAdvance.setBillNo(paymentNextBillNo);
				paymentListAdvance.setTypeOfCharge(ConstantValues.ADVANCE);
				paymentListAdvance.setRaisedById(userSecurity.getUserId());
				paymentListAdvance.setPatientRegistration(patientRegistration);

			} else {
				patientRegistration.setRoomBookingDetails(null);
			}

			Set<PatientPayment> patientPayment = patientRegistration.getPatientPayment();
			Timestamp paymentTime = new Timestamp(System.currentTimeMillis());

			for (PatientPayment paymentList : patientPayment) {

				paymentList.setInsertedDate(paymentTime);
				paymentList.setPatientRegistration(patientRegistration);
				paymentList.setRaisedById(userSecurity.getUserId());
				paymentList.setBillNo(paymentNextBillNo);
				modeOfPaymentAdvance = paymentList.getModeOfPaymant();
				if (paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.ADVANCE)
						|| paymentList.getModeOfPaymant().equalsIgnoreCase("Due")) {
					paymentList.setPaid(ConstantValues.NO);
				} else {
					paymentList.setPaid(ConstantValues.YES);

				}

				if (paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.CARD)
						|| paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.CREDIT_CARD)
						|| paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.DEBIT_CARD)
						|| paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {
					paymentList.setReferenceNumber(patientRegistration.getReferenceNumber());

				}

				// added code for pdf

				if (paymentList.getTypeOfCharge().equals(DOCTOR_FEE)) {
					docamount = String.valueOf(paymentList.getAmount());
					docamtPdf = paymentList.getAmount();
					docmop = paymentList.getModeOfPaymant();
					doctoc = paymentList.getTypeOfCharge();
				} else {

					amount = String.valueOf(paymentList.getAmount());
					amtPdf = paymentList.getAmount();
					mop = paymentList.getModeOfPaymant();
					toc = paymentList.getTypeOfCharge();
				}

			}

			patientRegistrationChargeBill = patientRegistration;

		}

		patientDetailsServiceImpl.save(patientDetails);

		// Advance payment for INPATIENT
		if (patientType.equalsIgnoreCase(ConstantValues.INPATIENT)) {
			paymentListAdvance.setModeOfPaymant(modeOfPaymentAdvance);
			paymentRepository.save(paymentListAdvance);

			if (modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CARD)
					|| modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CREDIT_CARD)
					|| modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.DEBIT_CARD)
					|| modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {

				paymentListAdvance.setReferenceNumber(patientRegistrationChargeBill.getReferenceNumber());
			}

			String billNo = chargeBillServiceImpl.getNextBillNo();

			/*
			 * // For DMO and Nursing charges ChargeBill cb=new ChargeBill(); LabServices
			 * labServices=labServicesServiceImpl.findPriceByType("GENERAL WARD DMO CHARGES"
			 * , patientType,serviceRoomType); cb.setAmount(labServices.getCost());
			 * cb.setBillNo(billNo); cb.setChargeBillId(chargeBillServiceImpl.getNextId());
			 * cb.setInsertedBy(userSecurity.getUserId()); cb.setInsertedDate(new
			 * Timestamp(System.currentTimeMillis())); cb.setMrp(labServices.getCost());
			 * cb.setNetAmount(labServices.getCost()); cb.setPaid(ConstantValues.NO);
			 * cb.setPatRegId(patientRegistrationChargeBill); cb.setQuantity(1);
			 * cb.setServiceId(labServices); cb.setPaymentType("Due");
			 * chargeBillServiceImpl.save(cb);
			 * 
			 * 
			 * labServices=labServicesServiceImpl.
			 * findPriceByType("GENERAL WARD NURSING CHARGES", patientType,serviceRoomType);
			 * cb.setAmount(labServices.getCost()); cb.setBillNo(billNo);
			 * cb.setChargeBillId(chargeBillServiceImpl.getNextId());
			 * cb.setInsertedBy(userSecurity.getUserId()); cb.setInsertedDate(new
			 * Timestamp(System.currentTimeMillis())); cb.setMrp(labServices.getCost());
			 * cb.setNetAmount(labServices.getCost()); cb.setPaid("NO");
			 * cb.setPatRegId(patientRegistrationChargeBill); cb.setQuantity(1);
			 * cb.setServiceId(labServices); cb.setPaymentType("Due");
			 * chargeBillServiceImpl.save(cb);
			 */
		}

		// calculating Total Amount paid
		if (adPdf != 0) {
			rectAmount = amtPdf + adPdf + docamtPdf;
		} else {
			rectAmount = amtPdf + docamtPdf;
		}

		// Final Billing

		if (modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CASH)) {
			finalCash = rectAmount;
		} else if (modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CARD)) {
			finalCard = rectAmount;
		} else if (modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CHEQUE)) {
			finalCheque = rectAmount;
		}

		// Cash + card
		PatientRegistration patientRegistration = patientRegistrationServiceImpl.findByRegId(regId);
		if (modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {
			CashPlusCard cashPlusCard = new CashPlusCard();
			cashPlusCard.setInsertedBy(userSecurity.getUserId());
			cashPlusCard.setBillNo(paymentNextBillNo);
			cashPlusCard.setDescription("Registration");
			cashPlusCard.setPatientRegistrationCashCard(patientRegistration);
			cashPlusCard.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			List<Map<String, String>> multiMode = patientRegistration.getMultimode();
			for (Map<String, String> multiModeInfo : multiMode) {

				if (multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CASH)) {

					cashPlusCard.setCashAmount(Long.parseLong(multiModeInfo.get("amount")));
					finalCash = Long.parseLong(multiModeInfo.get("amount"));
				} else if (multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CARD)) {

					cashPlusCard.setCardAmount(Long.parseLong(multiModeInfo.get("amount")));
					finalCard = Long.parseLong(multiModeInfo.get("amount"));
				} else if (multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CHEQUE)) {

					cashPlusCard.setChequeAmount(Long.parseLong(multiModeInfo.get("amount")));
					finalCheque = Long.parseLong(multiModeInfo.get("amount"));
				}

			}
			cashPlusCardServiceImpl.save(cashPlusCard);

		}

		if (patientType.equalsIgnoreCase(ConstantValues.INPATIENT)) {
			FinalBilling finalBilling = new FinalBilling();
			finalBilling.setBillNo(paymentNextBillNo);

			finalBilling.setBillType(PATIENT_REGISTRATION);
			finalBilling.setCardAmount(finalCard);
			finalBilling.setCashAmount(finalCash);
			finalBilling.setChequeAmount(finalCheque);
			finalBilling.setFinalAmountPaid(rectAmount);
			finalBilling.setFinalBillUser(userSecurity);
			finalBilling.setName(patientName);
			finalBilling.setRegNo(regId);
			finalBilling.setPaymentType(mop);
			finalBilling.setTotalAmount(rectAmount);
			finalBilling.setUmrNo(umr);
			finalBillingServcieImpl.computeSave(finalBilling);
		}
		// for sms
		try {

			String msg = "Hi,%20" + firstname + "%20" + lastname
					+ "%20Thanks%20for%20registering%20with%20us.%20Your%20UMR%20number%20is%20" + umr
					+ ".%20Doctor%20will%20be%20seeing%20you%20shortly.";
			URL url = new URL(
					"https://smsapi.engineeringtgr.com/send/?Mobile=9019438586&Password=N3236Q&Key=nikhilfI0ahSMOQkcb6uJ&Message="
							+ msg + "&To=" + mob);
			URLConnection urlcon = url.openConnection();
			InputStream stream = urlcon.getInputStream();
			int i;
			String response = EMPTY_STRING;
			while ((i = stream.read()) != -1) {
				response += (char) i;
			}
			if (response.contains("success")) {
				System.out.println("Successfully send SMS");
			} else {
				System.out.println(response);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		// ------------------------------------------------------Admission
		// Slip--------------------------------------------------------
		if (!patientType.equalsIgnoreCase(ConstantValues.OUTPATIENT)) {
			admissionSlipInfo(regId, principal);
		}
		// ------------------------------------------------------Admission Slip
		// end--------------------------------------------

		// ------------------------------------------------------Blank Prescription
		// begining-----------------------------------------------------

		PatientDetails patientDetailsBlank = patientDetailsRepository.findByUmr(patientDetails.getUmr());

		String visitTypeBlank;

		String deptNameBlank = EMPTY_STRING;

		PatientRegistration pBlank = patientRegistrationServiceImpl.findByRegId(regId);

		User user = pBlank.getVuserD();

		String consulatantBlank = patientDetailsBlank.getConsultant();

		List<SpecUserJoin> listSpec = specUserJoinRepository.findByUserSpec(user);
		if (!listSpec.isEmpty()) {
			for (SpecUserJoin deptNameInfo : listSpec) {
				deptNameBlank += deptNameInfo.getDocSpec().getSpecName() + "\n";
			}
		}
		visitTypeBlank = pBlank.getpType();

		String patientNameBlank = null;
		String pmn = EMPTY_STRING;
		if (patientDetails.getMiddleName() != null) {
			pmn = patientDetails.getMiddleName();
		}
		if (pmn.equalsIgnoreCase(EMPTY_STRING)) {
			patientNameBlank = patientDetails.getTitle() + ". " + patientDetails.getFirstName()
					+ ConstantValues.ONE_SPACE_STRING + patientDetails.getLastName();
		} else {
			patientNameBlank = patientDetails.getTitle() + ". " + patientDetails.getFirstName()
					+ ConstantValues.ONE_SPACE_STRING + patientDetails.getMiddleName() + ConstantValues.ONE_SPACE_STRING
					+ patientDetails.getLastName();
		}

		PatientPaymentPdf patientPaymentPdf = null;
		byte[] pdfBytes = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
		Font redFontInfo = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD);
		Font blueFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.NORMAL);
		Font font = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

		/*
		 * PatientRegistration patientRegistration = patientRegistrationServiceImpl
		 * .findByRegId(prescription.get("regId"));
		 */

		Document document = new Document();

		try {
			PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

			Font redFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
			document.open();

			Paragraph p55 = new Paragraph("\n\n\n\n");
			p55.setAlignment(Element.ALIGN_RIGHT);
			document.add(p55);

			PdfPTable table4 = new PdfPTable(1);
			table4.setWidths(new float[] { 5f });
			table4.setSpacingBefore(10);

			PdfPCell hcell11;
			hcell11 = new PdfPCell(new Phrase(consulatantBlank, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
			// hcell11.setPaddingRight(230f);
			table4.addCell(hcell11);

			if (user.getDoctorDetails().getQualification() != null) {
				PdfPCell hcell111;
				hcell111 = new PdfPCell(new Phrase(user.getDoctorDetails().getQualification(), redFontInfo));
				hcell111.setBorder(Rectangle.NO_BORDER);
				hcell111.setHorizontalAlignment(Element.ALIGN_RIGHT);
				// hcell111.setPaddingRight(230f);
				table4.addCell(hcell111);
			}

			PdfPCell hcell112;
			hcell112 = new PdfPCell(new Phrase(deptNameBlank, redFontInfo));
			hcell112.setBorder(Rectangle.NO_BORDER);
			hcell112.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell112.setPaddingBottom(10);
			// hcell112.setPaddingRight(230f);
			table4.addCell(hcell112);
			document.add(table4);

			// Display a date in day, month, year format
			Date date = Calendar.getInstance().getTime();
			DateFormat formatter = new SimpleDateFormat(SIMPLE_DATE_FORMAT_TO);
			String today = formatter.format(date).toString();

			PdfPTable table = new PdfPTable(2);
			PdfPCell cell19 = new PdfPCell();
			table.setWidthPercentage(100f);
			cell19.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
			// cell19.setBorder(Rectangle);

			PdfPTable table3 = new PdfPTable(6);
			table3.setWidths(new float[] { 5f, 1f, 5f, 5f, 1f, 5f });
			table3.setSpacingBefore(10);

			PdfPCell hcell1;

			hcell1 = new PdfPCell(new Phrase(PATIENT_NAME_SAVEINFO_PDF, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-50f);
			table3.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-50f);
			table3.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(patientNameBlank, redFont1));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-50f);
			table3.addCell(hcell1);

			PdfPCell hcell10;

			hcell10 = new PdfPCell(new Phrase(AGE_GENDER_SAVEINFO_PDF, redFont));
			hcell10.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell10);

			hcell10 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell10.setBorder(Rectangle.NO_BORDER);
			hcell10.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell10);

			hcell10 = new PdfPCell(new Phrase(
					patientDetails.getAge() + BACKWARD_SLASH_ADV_PDF + patientDetails.getGender(), redFont1));
			hcell10.setBorder(Rectangle.NO_BORDER);
			hcell10.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell10);

			PdfPCell hcell2;
			hcell2 = new PdfPCell(new Phrase(VISIT_TYPE_SAVEINFO_PDF, redFont));
			hcell2.setBorder(Rectangle.NO_BORDER);
			hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2.setPaddingLeft(-50f);
			table3.addCell(hcell2);

			hcell2 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell2.setBorder(Rectangle.NO_BORDER);
			hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2.setPaddingLeft(-50f);
			table3.addCell(hcell2);

			hcell2 = new PdfPCell(new Phrase(String.valueOf(visitTypeBlank), redFont1));
			hcell2.setBorder(Rectangle.NO_BORDER);
			hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2.setPaddingLeft(-50f);
			table3.addCell(hcell2);

			PdfPCell hcell20;

			hcell20 = new PdfPCell(new Phrase(REG_NO_SAVEINFO_PDF, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(regId, redFont1));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell20);

			PdfPCell hcell3;

			hcell3 = new PdfPCell(new Phrase(REF_DOCTOR_SAVEINFO_PDF, redFont));
			hcell3.setBorder(Rectangle.NO_BORDER);
			hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell3.setPaddingLeft(-50f);
			table3.addCell(hcell3);

			hcell3 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell3.setBorder(Rectangle.NO_BORDER);
			hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell3.setPaddingLeft(-50f);
			table3.addCell(hcell3);

			String ref = null;
			if (patientDetails.getvRefferalDetails() != null) {
				ref = patientDetails.getvRefferalDetails().getRefName();
			} else {
				ref = EMPTY_STRING;
			}

			hcell3 = new PdfPCell(new Phrase(ref, redFont1));
			hcell3.setBorder(Rectangle.NO_BORDER);
			hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell3.setPaddingLeft(-50f);
			table3.addCell(hcell3);

			PdfPCell hcell30;

			hcell30 = new PdfPCell(new Phrase(UMR_SAVEINFO_PDF, redFont));
			hcell30.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell30);

			hcell30 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell30.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell30);

			hcell30 = new PdfPCell(new Phrase(patientDetails.getUmr(), redFont1));
			hcell30.setBorder(Rectangle.NO_BORDER);
			hcell30.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell30);

			PdfPCell hcell301;
			hcell301 = new PdfPCell(new Phrase(ADDRESS_ADV_PDF, redFont));
			hcell301.setBorder(Rectangle.NO_BORDER);
			hcell301.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell301.setPaddingLeft(-50f);
			table3.addCell(hcell301);

			hcell301 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell301.setBorder(Rectangle.NO_BORDER);
			hcell301.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell301.setPaddingLeft(-50f);
			table3.addCell(hcell301);

			hcell301 = new PdfPCell(new Phrase(address, redFont1));
			hcell301.setBorder(Rectangle.NO_BORDER);
			hcell301.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell301.setPaddingLeft(-50f);
			table3.addCell(hcell301);

			PdfPCell hcell3011;

			hcell3011 = new PdfPCell(new Phrase(DATE_SAVEINFO_PDF, redFont));
			hcell3011.setBorder(Rectangle.NO_BORDER);
			hcell3011.setPaddingBottom(5f);
			table3.addCell(hcell3011);

			hcell3011 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont1));
			hcell3011.setBorder(Rectangle.NO_BORDER);
			hcell3011.setPaddingBottom(5f);
			table3.addCell(hcell3011);

			hcell3011 = new PdfPCell(new Phrase(today, redFont1));
			hcell3011.setBorder(Rectangle.NO_BORDER);
			hcell3011.setPaddingBottom(5f);
			hcell3011.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell3011);

			cell19.setColspan(2);
			cell19.addElement(table3);
			table3.addCell(cell19);
			table.addCell(cell19);
			document.add(table);
			document.close();
			pdfBytes = byteArrayOutputStream.toByteArray();
			String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(V1_PAYMENT_VIEWFILE_PATH)
					.path(paymentPdfServiceImpl.getNextPdfId()).toUriString();

			patientPaymentPdf = new PatientPaymentPdf(regId + BLANK_PRESCRIPTION, uri, pdfBytes);
			patientPaymentPdf.setPid(paymentPdfServiceImpl.getNextPdfId());
			paymentPdfServiceImpl.save(patientPaymentPdf);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// ------------------------------------------------------Blank Prescription
		// ending-------------------------------------------------------

		// Display a date in day, month, year format
		Date date = new Date(regDateTimestamp.getTime());
		DateFormat formatter = new SimpleDateFormat(SIMPLE_DATE_FORMAT_TO);
		String today = formatter.format(date).toString();

		date = new Date(new Timestamp(System.currentTimeMillis()).getTime());
		formatter = new SimpleDateFormat(FROM_DATE_FORMAT_ADV_PDF);
		String printedDate = formatter.format(date).toString();

		// for advance reciept
		//shantharam addr

		String addr = " ";

		//shantharam addr

		// for consultation reciept
		String addrss = "";

		patientPaymentPdf = null;
		pdfBytes = null;
		byteArrayOutputStream = new ByteArrayOutputStream();

		// -----------------new pdf code-------------------------------

		if (patientType.equals(ConstantValues.INPATIENT)) {
			try {
				document = new Document(PageSize.A4_LANDSCAPE);

				redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

				Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
				Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
				PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

				Resource fileResourcee = resourceLoader.getResource(UDBHAVA_PNG_CLASSPATH);

				Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());

				document.open();
				PdfPTable table = new PdfPTable(2);

				img.scaleAbsolute(75, 95);
				table.setWidthPercentage(105);

				Phrase pq = new Phrase(new Chunk(img, 0, -80));
				pq.add(new Chunk(addr, redFont));
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
				table99.setWidths(new float[] { 3f, 1f, 5f });
				table99.setSpacingBefore(10);

				PdfPCell hcell90;
				hcell90 = new PdfPCell(new Phrase(PATIENT_ADV_PDF, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setPaddingBottom(-7f);
				hcell90.setPaddingLeft(-25f);
				table99.addCell(hcell90);

				hcell90 = new PdfPCell(new Phrase(COLON, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingLeft(-45f);
				table99.addCell(hcell90);

				hcell90 = new PdfPCell(new Phrase(patientName, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingBottom(-7f);
				hcell90.setPaddingLeft(-60f);
				table99.addCell(hcell90);

				cell3.addElement(table99);

				PdfPTable table2 = new PdfPTable(6);
				table2.setWidths(new float[] { 3f, 1f, 5f, 3f, 1f, 4f });
				table2.setSpacingBefore(10);

				PdfPCell hcell1;
				hcell1 = new PdfPCell(new Phrase(UMR_NO_ADV_PDF, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-25f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(COLON, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-10f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(umr, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-15f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingRight(34f);
				hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingRight(34f);
				hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingRight(34f);
				hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell1);

				PdfPCell hcell4;
				hcell4 = new PdfPCell(new Phrase(RECT_NO_ADV_PDF, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setPaddingLeft(-25f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-10f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(regId, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-15f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(RECT_DATE_ADV_PDF, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingRight(-27.5f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell4.setPaddingRight(-0.1f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(today, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingRight(-20.5f);
				table2.addCell(hcell4);

				PdfPCell hcell15;
				hcell15 = new PdfPCell(new Phrase(AGE_SEX_ADV_PDF, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingLeft(-25f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(COLON, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-10f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(age + BACKWARD_SLASH_ADV_PDF + gender, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-15f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(PHONE_ADV_PDF, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingRight(-27.5f);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcell15);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell4.setPaddingRight(-0.1f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(phone, redFont));
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
				hcell19 = new PdfPCell(new Phrase(ADVANCE_RECIEPT_ADV_PDF, headFont1));
				hcell19.setBorder(Rectangle.NO_BORDER);
				hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
				// hcell19.setPaddingLeft(-70f);
				table21.addCell(hcell19);

				cell19.setFixedHeight(20f);
				cell19.setColspan(2);
				cell19.addElement(table21);
				table.addCell(cell19);

				PdfPCell cell4 = new PdfPCell();

				PdfPTable table3 = new PdfPTable(6);
				table3.setWidths(new float[] { 5f, 1f, 6f, 5f, 1f, 6f });
				table3.setSpacingBefore(10);

				PdfPCell hcell;
				hcell = new PdfPCell(new Phrase(FATHER_ADV_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setPaddingLeft(-50f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(COLON, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-80f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(father, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-80f);
				table3.addCell(hcell);

				Font redFont5 = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);

				hcell = new PdfPCell(new Phrase("Admitted Ward", redFont5));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(COLON, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-15f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(admittedWard, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-20f);
				table3.addCell(hcell);

				PdfPCell hcell11;
				hcell11 = new PdfPCell(new Phrase(ADMITTED_DATE, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setPaddingLeft(-50f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(COLON, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-80f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(today, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-80f);
				table3.addCell(hcell11);

				String dpt = null;
				if (department != null) {
					dpt = department;
				} else {
					dpt = EMPTY_STRING;
				}
				hcell11 = new PdfPCell(new Phrase(DEPARTMENT_ADV_PDF, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(COLON, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-15f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(dpt, redFont5));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-20f);
				table3.addCell(hcell11);

				PdfPCell hcell14;
				hcell14 = new PdfPCell(new Phrase(CONSULTANT_ADV_PDF, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setPaddingLeft(-50f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(COLON, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-80f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(consultant, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-80f);
				table3.addCell(hcell14);

				String cFee = null;
				if (docamount != null) {
					cFee = docamount;
				} else {
					cFee = "0";
				}

				hcell14 = new PdfPCell(new Phrase(CONSULTATION_FEE_SAVE_INFO_PDF, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(COLON, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-15f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(cFee, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-20f);
				table3.addCell(hcell14);

				PdfPCell hcell16;

				hcell16 = new PdfPCell(new Phrase(ORG_ADV_PDF, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell16);

				hcell16 = new PdfPCell(new Phrase(COLON, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell16.setPaddingLeft(-80f);
				table3.addCell(hcell16);

				hcell16 = new PdfPCell(new Phrase(ConstantValues.HOSPITAL_NAME, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell16.setPaddingLeft(-80f);
				table3.addCell(hcell16);

				String aAmt = null;
				if (advanceAmount != null) {
					aAmt = advanceAmount;
				} else {
					aAmt = EMPTY_STRING;
				}

				hcell16 = new PdfPCell(new Phrase(ADVANCE_AMOUNT_SAVEINFO_PDF, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell16);

				hcell16 = new PdfPCell(new Phrase(COLON, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell16.setPaddingLeft(-15f);
				table3.addCell(hcell16);

				hcell16 = new PdfPCell(new Phrase(aAmt, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell16.setPaddingLeft(-20f);
				table3.addCell(hcell16);

				PdfPCell hcell17;
				hcell17 = new PdfPCell(new Phrase(ADDRESS_ADV_PDF, redFont));
				hcell17.setBorder(Rectangle.NO_BORDER);
				hcell17.setPaddingLeft(-50f);
				table3.addCell(hcell17);

				hcell17 = new PdfPCell(new Phrase(COLON, redFont));
				hcell17.setBorder(Rectangle.NO_BORDER);
				hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell17.setPaddingLeft(-80f);
				table3.addCell(hcell17);

				hcell17 = new PdfPCell(new Phrase(address, redFont));
				hcell17.setBorder(Rectangle.NO_BORDER);
				hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell17.setPaddingLeft(-80f);
				table3.addCell(hcell17);

				if (toc.equalsIgnoreCase(NURSING_CHARGES_SAVEINFO_PDF)) {
					hcell17 = new PdfPCell(new Phrase(toc, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(COLON, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-15f);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-20f);
					table3.addCell(hcell17);
				} else if (toc.equalsIgnoreCase(SERVICE_CHARGES_SAVEINFO_PDF)) {
					hcell17 = new PdfPCell(new Phrase(toc, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(COLON, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-15f);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-20f);
					table3.addCell(hcell17);
				} else if (toc.equalsIgnoreCase(REG_FEES_SAVEINFO_PDF)) {
					hcell17 = new PdfPCell(new Phrase(toc, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(COLON, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-15f);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-20f);
					table3.addCell(hcell17);
				} else if (toc.equalsIgnoreCase(VACCINATION_FEES_SAVEINFO_PDF)) {
					hcell17 = new PdfPCell(new Phrase(toc, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(COLON, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-15f);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-20f);
					table3.addCell(hcell17);
				}

				PdfPCell hcell18;
				hcell18 = new PdfPCell(new Phrase(CITY_ADV_PDF, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-50f);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(COLON, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell18.setPaddingLeft(-80f);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(city, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell18.setPaddingLeft(-80f);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(RECT_AMOUNT_ADV_PDF, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(COLON, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell18.setPaddingLeft(-15f);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(String.valueOf(rectAmount), redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell18.setPaddingLeft(-20f);
				table3.addCell(hcell18);

				PdfPCell hcell20;
				hcell20 = new PdfPCell(new Phrase(STATE_ADV_PDF, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setPaddingLeft(-50f);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(COLON, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell20.setPaddingLeft(-80f);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(state, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell20.setPaddingLeft(-80f);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell20);

				PdfPTable table91 = new PdfPTable(1);
				table91.setWidths(new float[] { 5f });
				table91.setSpacingBefore(10);

				cell4.setFixedHeight(170f);
				cell4.setColspan(2);
				cell4.addElement(table3);

				PdfPCell hcell98;
				hcell98 = new PdfPCell(new Phrase(
						RECIEVED_WITH_THANKS_SAVEINFO_PDF + patientName + ", " + A_SUM_OF_RS_SAVEINFO_PDF + rectAmount
								+ "\n\n" + IN_WORDS_RS_SAVEINFO_PDF + numberToWordsConverter.convert((int) rectAmount),
						redFont));
				hcell98.setBorder(Rectangle.NO_BORDER);
				hcell98.setPaddingLeft(-50f);
				hcell98.setPaddingTop(3);
				table91.addCell(hcell98);
				cell4.addElement(table91);
				table.addCell(cell4);

				PdfPCell cell5 = new PdfPCell();

				PdfPTable table35 = new PdfPTable(2);
				table35.setWidths(new float[] { 5f, 4f });
				table35.setSpacingBefore(10);

				PdfPCell hcell21;
				Font redFont3 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
				hcell21 = new PdfPCell(new Phrase(STAR + umr + STAR, redFont3));
				hcell21.setBorder(Rectangle.NO_BORDER);
				hcell21.setPaddingLeft(-50f);
				table35.addCell(hcell21);

				hcell21 = new PdfPCell(new Phrase(STAR + regId + STAR, redFont3));
				hcell21.setBorder(Rectangle.NO_BORDER);
				hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell21);

				PdfPCell hcell12;
				hcell12 = new PdfPCell(new Phrase(CREATED_BY_SAVEINFO_PDF + createdBy, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				hcell12.setPaddingTop(10f);
				hcell12.setPaddingLeft(-50f);
				table35.addCell(hcell12);

				hcell12 = new PdfPCell(new Phrase(CREATED_DATE + printedDate, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				hcell12.setPaddingTop(10f);
				hcell12.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell12);

				PdfPCell hcell13;
				hcell13 = new PdfPCell(new Phrase(PRINTED_BY_SAVEINFO_PDF + createdBy, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingLeft(-50f);
				table35.addCell(hcell13);

				hcell13 = new PdfPCell(new Phrase(PRINTED_DATE + printedDate, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingRight(3f);
				hcell13.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell13);

				PdfPCell hcell23;
				hcell23 = new PdfPCell(new Phrase(EMPTY_STRING));
				hcell23.setBorder(Rectangle.NO_BORDER);
				table35.addCell(hcell23);

				hcell23 = new PdfPCell(new Phrase(AUTHORIZED_SIGNATURE, headFont));
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
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(V1_PAYMENT_VIEWFILE_PATH)
						.path(paymentPdfServiceImpl.getNextPdfId()).toUriString();

				patientPaymentPdf = new PatientPaymentPdf(regId + " Advance Reciept", uri, pdfBytes);
				patientPaymentPdf.setPid(paymentPdfServiceImpl.getNextPdfId());
				paymentPdfServiceImpl.save(patientPaymentPdf);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		else
		// -----------------------------------------
		{
			try {
				document = new Document(PageSize.A4_LANDSCAPE);

				redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

				Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
				Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
				PdfWriter r = PdfWriter.getInstance(document, byteArrayOutputStream);
				document.open();
				PdfPTable table = new PdfPTable(2);
				Resource fileResourcee = resourceLoader.getResource(UDBHAVA_PNG_CLASSPATH);

				Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());

				img.scaleAbsolute(75, 95);
				table.setWidthPercentage(105);

				Phrase pq = new Phrase(new Chunk(img, 0, -80));
				pq.add(new Chunk(addrss, redFont));
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
				hcell96.setPaddingTop(10f);
				hcell96.setPaddingLeft(52f);

				table96.addCell(hcell96);
				cell1.addElement(table96);
				cell1.addElement(pq);
				cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell1);
//----------------------------------
				PdfPCell cell3 = new PdfPCell();

				PdfPTable table99 = new PdfPTable(3);
				table99.setWidths(new float[] { 3f, 1f, 5f });
				table99.setSpacingBefore(10);

				PdfPCell hcell90;
				hcell90 = new PdfPCell(new Phrase(PATIENT_ADV_PDF, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingLeft(-25f);
				hcell90.setPaddingTop(15f);
				table99.addCell(hcell90);

				hcell90 = new PdfPCell(new Phrase(COLON, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingLeft(-50f);
				hcell90.setPaddingTop(15f);
				table99.addCell(hcell90);

				hcell90 = new PdfPCell(new Phrase(patientName, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingLeft(-65f);
				hcell90.setPaddingTop(15f);
				table99.addCell(hcell90);

				cell3.addElement(table99);
				// table.addCell(cell3);

				PdfPTable table2 = new PdfPTable(6);
				table2.setWidths(new float[] { 3f, 1f, 5f, 3f, 1f, 4f });
				table2.setSpacingBefore(10);

				PdfPCell hcell1;
				hcell1 = new PdfPCell(new Phrase(AGE_SEX_ADV_PDF, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-25f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(COLON, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-15f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(age + BACKWARD_SLASH_ADV_PDF + gender, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-20f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(UMR_NO_ADV_PDF, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingRight(-27.5f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(COLON, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell1.setPaddingTop(-5f);
				;
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(umr, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingRight(-27.5f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				PdfPCell hcell4;

				hcell4 = new PdfPCell(new Phrase(CONST_NO_SAVEINFO_PDF, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-25f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-15f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(regId, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-20f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(CONST_DATE_SAVEINFO_PDF, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingRight(-27.5f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(today, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingRight(-27.5f);
				table2.addCell(hcell4);

				PdfPCell hcell15;

				String ref = null;
				if (refBy != null) {
					ref = refBy;
				} else {
					ref = EMPTY_STRING;
				}

				hcell15 = new PdfPCell(new Phrase(REF_BY_SAVEINFO_PDF, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-25f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(COLON, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-15f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(ref, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-20f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(PHONE_ADV_PDF, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingRight(-27.5f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(COLON, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(phone, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingRight(-27.5f);
				table2.addCell(hcell15);

				cell3.setFixedHeight(115f);
				cell3.setColspan(2);
				cell3.addElement(table2);
				table.addCell(cell3);

				PdfPCell cell19 = new PdfPCell();

				PdfPTable table21 = new PdfPTable(1);
				table21.setWidths(new float[] { 4f });
				table21.setSpacingBefore(10);

				PdfPCell hcell19;

				hcell19 = new PdfPCell(new Phrase(CONSULTATION_RECIEPT_SAVEINFO_PDF, headFont1));
				hcell19.setBorder(Rectangle.NO_BORDER);
				hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
				table21.addCell(hcell19);

				cell19.setFixedHeight(20f);
				cell19.setColspan(2);
				cell19.addElement(table21);
				table.addCell(cell19);

				PdfPCell cell4 = new PdfPCell();

				PdfPTable table3 = new PdfPTable(6);
				table3.setWidths(new float[] { 4f, 1f, 5f, 5f, 1f, 6f });
				table3.setSpacingBefore(10);

				PdfPCell hcell;
				hcell = new PdfPCell(new Phrase(CONSULTANT_ADV_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-50f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(COLON, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-65f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(consultant, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-80f);
				table3.addCell(hcell);

				String dpt = null;
				if (department != null) {
					dpt = department;
				} else {
					dpt = EMPTY_STRING;
				}

				hcell = new PdfPCell(new Phrase(DEPT_NAME_SAVEINFO_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingRight(27.5f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(COLON, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-27f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(dpt, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-40f);
				table3.addCell(hcell);

				PdfPCell hcell11;
				hcell11 = new PdfPCell(new Phrase(VISIT_TYPE_SAVEINFO_PDF, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-50f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(COLON, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-65f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(patientType, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-80f);
				table3.addCell(hcell11);

				String docFee = null;
				if (docamtPdf != 0) {
					docFee = String.valueOf(docamtPdf);
				} else {
					docFee = "0";
				}
				hcell11 = new PdfPCell(new Phrase("Consultant Fee", redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingRight(27.5f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(COLON, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-27f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(docFee, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-40f);
				table3.addCell(hcell11);

				PdfPCell hcell14;

				hcell14 = new PdfPCell(new Phrase(PAYMENT_MODE_SAVEINFO_PDF, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-50f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(COLON, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-65f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(mop, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-80f);
				table3.addCell(hcell14);

				if (toc.equalsIgnoreCase(NURSING_CHARGES_SAVEINFO_PDF)) {
					hcell14 = new PdfPCell(new Phrase(toc, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingRight(27.5f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(COLON, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-27f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-40f);
					table3.addCell(hcell14);
				} else if (toc.equalsIgnoreCase(SERVICE_CHARGES_SAVEINFO_PDF)) {
					hcell14 = new PdfPCell(new Phrase(toc, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingRight(27.5f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(COLON, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-27f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-40f);
					table3.addCell(hcell14);
				} else if (toc.equalsIgnoreCase(REG_FEES_SAVEINFO_PDF)) {
					hcell14 = new PdfPCell(new Phrase(toc, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingRight(27.5f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(COLON, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-27f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-40f);
					table3.addCell(hcell14);
				} else if (toc.equalsIgnoreCase(VACCINATION_FEES_SAVEINFO_PDF)) {
					hcell14 = new PdfPCell(new Phrase(toc, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingRight(27.5f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(COLON, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-27f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-40f);
					table3.addCell(hcell14);
				}
				PdfPCell hcell16;

				if (mop == null) {
					hcell16 = new PdfPCell(new Phrase(RECEIPT_NO_SAVEINFO_PDF, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setPaddingLeft(-50f);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(COLON, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-65f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(regId, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-80f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-20f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-27f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-40f);
					table3.addCell(hcell16);

				} else {
					hcell16 = new PdfPCell(new Phrase(RECEIPT_NO_SAVEINFO_PDF, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setPaddingLeft(-50f);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(COLON, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-65f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(regId, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-80f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(RECT_AMOUNT_ADV_PDF, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingRight(27.5f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(COLON, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-27f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(String.valueOf((int) amtPdf + (int) docamtPdf), redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-40f);
					table3.addCell(hcell16);

				}

				PdfPTable table91 = new PdfPTable(1);
				table91.setWidths(new float[] { 5f });
				table91.setSpacingBefore(10);

				cell4.setFixedHeight(130f);
				cell4.setColspan(2);
				cell4.addElement(table3);

				PdfPCell hcell98;

				hcell98 = new PdfPCell(new Phrase(VALIDITY + TWO_VISITS_BEFORE + regValidity + RECEIVED_WITH_THANKS_FROM
						+ patientName + ", " + A_SUM_OF_RS_SAVEINFO_PDF + String.valueOf((int) amtPdf + (int) docamtPdf)
						+ "\n\n" + IN_WORDS_RS_SAVEINFO_PDF
						+ numberToWordsConverter.convert((int) amtPdf + (int) docamtPdf), redFont));
				hcell98.setBorder(Rectangle.NO_BORDER);
				// hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell98.setPaddingLeft(-50f);
				hcell98.setPaddingTop(5);
				table91.addCell(hcell98);
				cell4.addElement(table91);

				table.addCell(cell4);

				PdfPCell cell5 = new PdfPCell();

				PdfPTable table35 = new PdfPTable(2);
				table35.setWidths(new float[] { 5f, 4f });
				table35.setSpacingBefore(10);

				PdfPCell hcell21;
				Font redFont3 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
				hcell21 = new PdfPCell(new Phrase(STAR + umr + STAR, redFont3));
				hcell21.setBorder(Rectangle.NO_BORDER);
				// hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell21.setPaddingLeft(-50f);
				table35.addCell(hcell21);

				hcell21 = new PdfPCell(new Phrase(STAR + regId + STAR, redFont3));
				hcell21.setBorder(Rectangle.NO_BORDER);
				hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell21);

				PdfPCell hcell12;
				hcell12 = new PdfPCell(new Phrase(CREATED_BY_SAVEINFO_PDF + createdBy, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				// hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell12.setPaddingTop(5f);
				hcell12.setPaddingLeft(-50f);
				table35.addCell(hcell12);

				hcell12 = new PdfPCell(new Phrase(CREATED_DATE + printedDate, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				hcell12.setPaddingTop(5f);
				// hcell12.setPaddingRight(0f);
				hcell12.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell12);

				PdfPCell hcell13;
				hcell13 = new PdfPCell(new Phrase(PRINTED_BY_SAVEINFO_PDF + createdBy, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table35.addCell(hcell13);

				hcell13 = new PdfPCell(new Phrase(PRINTED_DATE + printedDate, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingRight(3f);
				hcell13.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell13);

				PdfPCell hcell23;
				hcell23 = new PdfPCell(new Phrase(EMPTY_STRING));
				hcell23.setBorder(Rectangle.NO_BORDER);
				// hcell23.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table35.addCell(hcell23);

				hcell23 = new PdfPCell(new Phrase(AUTHORIZED_SIGNATURE, headFont));
				hcell23.setBorder(Rectangle.NO_BORDER);
				hcell23.setPaddingTop(15f);
				hcell23.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell23);

				cell5.setFixedHeight(95f);
				cell5.setColspan(2);
				cell5.addElement(table35);
				table.addCell(cell5);

				document.add(table);

				document.close();

				pdfBytes = byteArrayOutputStream.toByteArray();
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(V1_PAYMENT_VIEWFILE_PATH)
						.path(paymentPdfServiceImpl.getNextPdfId()).toUriString();

				patientPaymentPdf = new PatientPaymentPdf(regId + " Consultation Reciept", uri, pdfBytes);
				patientPaymentPdf.setPid(paymentPdfServiceImpl.getNextPdfId());
				paymentPdfServiceImpl.save(patientPaymentPdf);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return patientPaymentPdf;
	}
	
	

	
	
	/*
	 * Existing patient registartion
	 */
	@Transactional
		public PatientPaymentPdf newRegistration(PatientRegistration patientRegistration,String umr,Principal principal)
	{

		/*
		 * // To check whether patient is registered for the same day
		 * patientRegistrationServiceImpl.findLatestReg(uid);
		 * 
		 */
		User userSecurity = userServiceImpl.findByUserName(principal.getName());

		// For payment
		String createdBy = null;
		String umrp = null;
		String patientName = null;
		String patientType = null;
		String regId = null;
		String regDate = null;
		String refBy = null;
		String regValidity = null;
		String phone = null;
		String gender = null;
		String age = null;
		String formattedDate = null;
		String amount = null;
		String mop = null;
		String toc = null;
		String consultant = null;
		String deptName = null;
		String state = null;
		String city = null;
		String address = null;
		String father = null;
		String admittedWard = null;
		String department = null;
		Timestamp regDateTimestamp = null;
		String advanceAmount = null;
		long amtPdf = 0;
		long rectAmount = 0;
		long adPdf = 0;
		String docamount = null;
		long docamtPdf = 0;
		String docmop = null;
		String doctoc = null;
		String paymentMode = EMPTY_STRING;

		long finalCash = 0; // final billing
		long finalCard = 0; // final billing
		long finalCheque = 0; // final billing

		String fn = null;
		String mn = null;
		String ln = null;
		if (userSecurity.getFirstName() == null) {
			fn = ConstantValues.ONE_SPACE_STRING;
		} else {
			fn = userSecurity.getFirstName();
		}

		if (userSecurity.getMiddleName() == null) {
			mn = EMPTY_STRING;
		} else {
			mn = userSecurity.getMiddleName();
		}
		if (userSecurity.getLastName() == null) {
			ln = ConstantValues.ONE_SPACE_STRING;
		} else {
			ln = userSecurity.getLastName();
		}
		if (mn.equals(EMPTY_STRING)) {
			createdBy = fn + ConstantValues.ONE_SPACE_STRING + ln;

		} else {
			createdBy = fn + ConstantValues.ONE_SPACE_STRING + mn + ConstantValues.ONE_SPACE_STRING + ln;

		}

		PatientTypes patientTypes = patientTypeServiceImpl.findByPType(patientRegistration.getRePatientType());

		PatientDetails patient = patientDetailsServiceImpl.getPatientByUmr(umr);

		String paymentBillNo = patientPaymentServiceImpl.findNextBillNo();

		// Patient Payment bill no
		String paymentNextBillNo = patientPaymentServiceImpl.findNextBillNo();

		// Referral Details
		Map<String, String> refInfo = patientRegistration.getRefName();

		if (!refInfo.isEmpty()) {
			ReferralDetails refDetails = referralDetailsServiceImpl.findBySourceAndRefName(refInfo.get("source"),
					refInfo.get("refName"));
			if (refDetails == null && !refInfo.get("source").equals(EMPTY_STRING)
					&& !refInfo.get("refName").equals(EMPTY_STRING)) {
				ReferralDetails refObject = new ReferralDetails();
				refObject.setRefAdd(refInfo.get("adr"));
				refObject.setRefName(refInfo.get("refName"));
				refObject.setRefPhone(Long.parseLong(refInfo.get("phone")));
				refObject.setSource(refInfo.get("source"));
				referralDetailsServiceImpl.save(refObject);
				patient.setvRefferalDetails(refObject);
			} else if (!refInfo.get("source").equals(EMPTY_STRING) && refInfo.get("refName").equals(EMPTY_STRING)) {
				patient.setvRefferalDetails(null);
			} else if (refDetails != null) {
				patient.setvRefferalDetails(refDetails);
			} else {
				patient.setvRefferalDetails(null);
			}
			if (patient.getvRefferalDetails() != null) {
				refBy = patient.getvRefferalDetails().getRefName();
			} else {
				refBy = EMPTY_STRING;
			}

		}

		// Calculating age
		LocalDate today2 = LocalDate.now();
		LocalDate birthday = patient.getDob().toLocalDateTime().toLocalDate();
		Period p = Period.between(birthday, today2);
		System.out.println("Days" + p.getDays() + "months" + p.getMonths() + "years" + p.getYears());
		int accurate_age = 0;
		int days = 0;
		if (p.getMonths() >= 5) {
			accurate_age = p.getYears() + 1;
		} else {
			accurate_age = p.getYears();
		}

		if (p.getYears() == 0) {
			if (p.getDays() == 0) {
				String lessThanOne = String
						.valueOf(p.getYears() + Y_SPACE + p.getMonths() + M_SPACE + String.valueOf(1) + D);
				patient.setAge(String.valueOf(lessThanOne));
			} else {
				days = p.getDays() + 1;
				String lessThanOne = String.valueOf(p.getYears() + Y_SPACE + p.getMonths()) + M_SPACE
						+ String.valueOf(days + D);
				patient.setAge(String.valueOf(lessThanOne));
			}
		} else {
			days = 0;
			if (p.getMonths() == 0) {
				days = p.getDays() + 1;
				patient.setAge(
						String.valueOf(String.valueOf(p.getYears() + Y_SPACE + p.getMonths()) + M_SPACE + days + D));
			} else {
				days = p.getDays() + 1;
				patient.setAge(
						String.valueOf(String.valueOf(p.getYears() + Y_SPACE + p.getMonths()) + M_SPACE + days + D));

			}
		}
		age = patient.getAge();

		String uName = patientRegistration.getReConsultant();
		String docName[] = uName.split("-");

		patient.setConsultant(docName[0]);
		// for payment
		gender = patient.getGender();
		phone = String.valueOf(patient.getMobile());
		umrp = patient.getUmr();

		String pfn = null;
		String pmn = null;
		String pln = null;
		if (patient.getFirstName() == null) {
			pfn = ConstantValues.ONE_SPACE_STRING;
		} else {
			pfn = patient.getFirstName();
		}
		if (patient.getMiddleName() == null) {
			pmn = EMPTY_STRING;
		} else {
			pmn = patient.getMiddleName();
		}
		if (patient.getLastName() == null) {
			pln = ConstantValues.ONE_SPACE_STRING;
		} else {
			pln = patient.getLastName();
		}
		if (pmn.equalsIgnoreCase(EMPTY_STRING)) {
			patientName = patient.getTitle() + ". " + patient.getFirstName() + ConstantValues.ONE_SPACE_STRING
					+ patient.getLastName();
		} else {
			patientName = patient.getTitle() + ". " + patient.getFirstName() + ConstantValues.ONE_SPACE_STRING
					+ patient.getMiddleName() + ConstantValues.ONE_SPACE_STRING + patient.getLastName();
		}
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Date dateInfo = new Date();
		dateInfo.setTime(timestamp.getTime());
		formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(dateInfo);
		consultant = patient.getConsultant();
		regDate = new Timestamp(System.currentTimeMillis()).toString().substring(0, 10);
		regValidity = LocalDate.parse(regDate).plusDays(7).toString();

		// reg validity Format
		try {
			SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat toFormat = new SimpleDateFormat("dd-MMM-yyyy");
			regValidity = toFormat.format(fromFormat.parse(regValidity));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		patientRegistration.setCreatedBy(userSecurity.getUserId());
		patientRegistration.setRegValidity(regValidity);
		patientType = patientRegistration.getRePatientType();
		age = patient.getAge();
		regId = patientRegistrationServiceImpl.getNextRegId();
		refBy = ConstantValues.ONE_SPACE_STRING;
		state = patient.getState();
		address = patient.getAddress();
		city = patient.getCity();
		father = patient.getMotherName();

		User user = userServiceImpl.findOneByUserId(docName[1]);
		department = user.getSpecUserJoin().get(0).getDocSpec().getSpecName();
		patientRegistration.setRegId(patientRegistrationServiceImpl.getNextRegId());
		Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());

		PatientDetails patientDetails = patientDetailsServiceImpl.getPatientByUmr(umr);

		PatientPayment paymentListAdvance = null;
		// Room booking
		String serviceRoomType = EMPTY_STRING;
		if (patientRegistration.getRePatientType().equals(ConstantValues.INPATIENT)) {

			patientRegistration.setTriggeredDate(timestamp);
			List<RoomBookingDetails> roomBookingDetails = patientRegistration.getRoomBookingDetails();
			for (RoomBookingDetails roomBookingDetailsInfo : roomBookingDetails) {
				roomBookingDetailsInfo.setBookingId(roomBookingDetailsServiceImpl.getNextBookingId());
				patientRegistration.setAdvanceAmount(roomBookingDetailsInfo.getAdvanceAmount());
				patientRegistration.setEstimationAmount(roomBookingDetailsInfo.getEstimateAmount());
				roomBookingDetailsInfo.setStatus(1);
				RoomDetails roomDetails = roomDetailsServiceImpl.findByBedName(roomBookingDetailsInfo.getBedNo());
				advanceAmount = String.valueOf(roomBookingDetailsInfo.getAdvanceAmount());
				roomBookingDetailsInfo.setCostSoFar((int) roomDetails.getCostPerDay());
				serviceRoomType = roomDetails.getRoomType();
				patient.setDischarged("-");
				if (roomBookingDetailsInfo.getToDate() != null) {
					Timestamp times = roomBookingDetailsInfo.getToDate();
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(times.getTime());
					calendar.add(Calendar.DATE, -1);
					roomBookingDetailsInfo
							.setTentativeDischargeDate(Timestamp.valueOf(dateFormat.format(calendar.getTime())));
				}
				roomBookingDetailsInfo.setRoomDetails(roomDetails);
				admittedWard = roomDetails.getRoomType();
				roomBookingDetailsInfo.setPatientRegistrationBooking(patientRegistration);
				// roomBookingDetailsServiceImpl.save(roomBookingDetailsInfo);
			}

			paymentListAdvance = new PatientPayment();
			paymentListAdvance.setAmount(patientRegistration.getAdvanceAmount());
			paymentListAdvance.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			paymentListAdvance.setPaid(ConstantValues.YES);
			paymentListAdvance.setBillNo(paymentBillNo);
			paymentListAdvance.setTypeOfCharge(ConstantValues.ADVANCE);
			paymentListAdvance.setRaisedById(userSecurity.getUserId());
			paymentListAdvance.setPatientRegistration(patientRegistration);

		} else {
			patientRegistration.setRoomBookingDetails(null);
		}

		// new code
		Set<PatientPayment> patientPayment = patientRegistration.getPatientPayment();
		String modeOfPaymentAdvance = EMPTY_STRING;
		for (PatientPayment paymentList : patientPayment) {
			paymentMode = paymentList.getModeOfPaymant();

			Timestamp paymentTime = new Timestamp(System.currentTimeMillis());
			paymentList.setInsertedDate(paymentTime);
			paymentList.setPatientRegistration(patientRegistration);
			paymentList.setRaisedById(userSecurity.getUserId());
			paymentList.setBillNo(paymentBillNo);
			modeOfPaymentAdvance = paymentList.getModeOfPaymant();
			if (paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.ADVANCE)
					|| paymentList.getModeOfPaymant().equalsIgnoreCase("Due")) {
				paymentList.setPaid(ConstantValues.NO);
			} else {
				paymentList.setPaid(ConstantValues.YES);

			}

			// for reference number

			if (paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.CARD)
					|| paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.CREDIT_CARD)
					|| paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.DEBIT_CARD)
					|| paymentList.getModeOfPaymant().equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {
				paymentList.setReferenceNumber(patientRegistration.getReferenceNumber());

			}
			// added code for pdf
			if (paymentList.getTypeOfCharge().equals("Doctor Fee")) {
				docamount = String.valueOf(paymentList.getAmount());
				docamtPdf = paymentList.getAmount();
				docmop = paymentList.getModeOfPaymant();
				doctoc = paymentList.getTypeOfCharge();
				mop = paymentList.getModeOfPaymant();
				// For reg fee
				toc = REG_FEES_SAVEINFO_PDF;
			}

		}

		// Advance amount for INPATIENT
		if (patientType.equalsIgnoreCase(ConstantValues.INPATIENT)) {
			paymentListAdvance.setModeOfPaymant(modeOfPaymentAdvance);

			// for reference number

			if (modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CARD)
					|| modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CREDIT_CARD)
					|| modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.DEBIT_CARD)
					|| modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {

				paymentListAdvance.setReferenceNumber(patientRegistration.getReferenceNumber());
			}

			paymentRepository.save(paymentListAdvance);

			String billNo = chargeBillServiceImpl.getNextBillNo();

			/*
			 * // For DMO and Nursing charges ChargeBill cb=new ChargeBill(); LabServices
			 * labServices=labServicesServiceImpl.findPriceByType("GENERAL WARD DMO CHARGES"
			 * , patientType,serviceRoomType); cb.setAmount(labServices.getCost());
			 * cb.setBillNo(billNo); cb.setChargeBillId(chargeBillServiceImpl.getNextId());
			 * cb.setInsertedBy(userSecurity.getUserId()); cb.setInsertedDate(timestamp);
			 * cb.setMrp(labServices.getCost()); cb.setNetAmount(labServices.getCost());
			 * cb.setPaid("NO"); cb.setPatRegId(patientRegistration); cb.setQuantity(1);
			 * cb.setServiceId(labServices); cb.setPaymentType("Due");
			 * chargeBillServiceImpl.save(cb);
			 * 
			 * labServices=labServicesServiceImpl.
			 * findPriceByType("GENERAL WARD NURSING CHARGES", patientType,serviceRoomType);
			 * cb.setAmount(labServices.getCost()); cb.setBillNo(billNo);
			 * cb.setChargeBillId(chargeBillServiceImpl.getNextId());
			 * cb.setInsertedBy(userSecurity.getUserId()); cb.setInsertedDate(timestamp);
			 * cb.setMrp(labServices.getCost()); cb.setNetAmount(labServices.getCost());
			 * cb.setPaid("NO"); cb.setPatRegId(patientRegistration); cb.setQuantity(1);
			 * cb.setServiceId(labServices); cb.setPaymentType("Due");
			 * chargeBillServiceImpl.save(cb);
			 */
		}

		// for sms
		String patientname = patientDetails.getFirstName() + "%20" + patientDetails.getLastName();
		long mob = patientDetails.getMobile();

		patientRegistration.setCreatedAt(timestamp2);
		patientRegistration.setpatientType(patientTypes);
		patientRegistration.setpType(patientTypes.getpType());
		patientRegistration.setPatientDetails(patientDetails);
		patientRegistration.setDateOfJoining(timestamp2);
		patientRegistration.setRegDate(timestamp2);
		regDateTimestamp = patientRegistration.getRegDate();

		patientRegistration.setVuserD(user);
		deptName = patientRegistration.getVuserD().getDoctorDetails().getSpecilization();
		patientRegistrationServiceImpl.save(patientRegistration);

		if (advanceAmount != null) {
			rectAmount = amtPdf + Integer.parseInt(advanceAmount) + docamtPdf;
		} else {
			rectAmount = amtPdf + docamtPdf;
		}

		// Final Billing

		if (modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CASH)) {
			finalCash = rectAmount;
		} else if (modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CARD)) {
			finalCard = rectAmount;
		} else if (modeOfPaymentAdvance.equalsIgnoreCase(ConstantValues.CHEQUE)) {
			finalCheque = rectAmount;
		}

		// Cash + card
		if (mop.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {
			CashPlusCard cashPlusCard = new CashPlusCard();
			cashPlusCard.setInsertedBy(userSecurity.getUserId());
			cashPlusCard.setBillNo(paymentNextBillNo);
			cashPlusCard.setInsertedDate(new Timestamp(System.currentTimeMillis()));
			cashPlusCard.setDescription("Existing Patient Registration");
			cashPlusCard.setPatientRegistrationCashCard(patientRegistration);

			List<Map<String, String>> multiMode = patientRegistration.getMultimode();
			for (Map<String, String> multiModeInfo : multiMode) {

				if (multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CASH)) {

					cashPlusCard.setCashAmount(Long.parseLong(multiModeInfo.get("amount")));

				} else if (multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CARD)) {

					cashPlusCard.setCardAmount(Long.parseLong(multiModeInfo.get("amount")));
				} else if (multiModeInfo.get("mode").equalsIgnoreCase(ConstantValues.CHEQUE)) {

					cashPlusCard.setChequeAmount(Long.parseLong(multiModeInfo.get("amount")));

				}

			}
			cashPlusCardServiceImpl.save(cashPlusCard);

		}

		if (patientType.equalsIgnoreCase(ConstantValues.INPATIENT)) {

			FinalBilling finalBilling = new FinalBilling();
			finalBilling.setBillNo(paymentNextBillNo);
			finalBilling.setBillType("Patient Registration");
			finalBilling.setCardAmount(finalCard);
			finalBilling.setCashAmount(finalCash);
			finalBilling.setChequeAmount(finalCheque);
			finalBilling.setFinalAmountPaid(rectAmount);
			finalBilling.setFinalBillUser(userSecurity);
			finalBilling.setName(patientName);
			finalBilling.setRegNo(regId);
			finalBilling.setPaymentType(mop);
			finalBilling.setTotalAmount(rectAmount);
			finalBilling.setUmrNo(umr);
			finalBillingServcieImpl.computeSave(finalBilling);
		}

		try {

			String msg = "Hi," + patientname
					+ "%20Thanks%20for%20registering%20with%20us.%20Your%20UMR%20number%20is%20" + umr
					+ ".%20Doctor%20will%20be%20seeing%20you%20shortly.";
			URL url = new URL(
					"https://smsapi.engineeringtgr.com/send/?Mobile=9019438586&Password=N3236Q&Key=nikhilfI0ahSMOQkcb6uJ&Message="
							+ msg + "&To=" + mob);
			URLConnection urlcon = url.openConnection();
			InputStream stream = urlcon.getInputStream();
			int i;
			String response = EMPTY_STRING;
			while ((i = stream.read()) != -1) {
				response += (char) i;
			}
			if (response.contains("success")) {
				System.out.println("Successfully send SMS");
				// your code when message send success
			} else {
				System.out.println(response);
				// your code when message not send
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		if (!patientType.equalsIgnoreCase(ConstantValues.OUTPATIENT)) {
			admissionSlipInfo(regId, principal);
		}
	
		PatientDetails patientDetailsBlank = patientDetailsRepository.findByUmr(patientDetails.getUmr());

		String visitTypeBlank = null;

		String deptNameBlank = EMPTY_STRING;

		PatientRegistration pBlank = patientRegistrationServiceImpl.findByRegId(regId);

		user = pBlank.getVuserD();

		String consulatantBlank = patientDetailsBlank.getConsultant();

		List<SpecUserJoin> listSpec = specUserJoinRepository.findByUserSpec(user);
		if (!listSpec.isEmpty()) {
			for (SpecUserJoin deptNameInfo : listSpec) {
				deptNameBlank += deptNameInfo.getDocSpec().getSpecName() + "\n";
			}
		}
		visitTypeBlank = pBlank.getpType();

		String patientNameBlank = null;
		pmn = EMPTY_STRING;
		if (patientDetails.getMiddleName() != null) {
			pmn = patientDetails.getMiddleName();
		}
		if (pmn.equalsIgnoreCase(EMPTY_STRING)) {
			patientNameBlank = patientDetails.getTitle() + ". " + patientDetails.getFirstName()
					+ ConstantValues.ONE_SPACE_STRING + patientDetails.getLastName();
		} else {
			patientNameBlank = patientDetails.getTitle() + ". " + patientDetails.getFirstName()
					+ ConstantValues.ONE_SPACE_STRING + patientDetails.getMiddleName() + ConstantValues.ONE_SPACE_STRING
					+ patientDetails.getLastName();
		}

		PatientPaymentPdf patientPaymentPdf = null;
		byte[] pdfBytes = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
		Font redFontInfo = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD);
		Font blueFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.NORMAL);
		Font font = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

		/*
		 * PatientRegistration patientRegistration = patientRegistrationServiceImpl
		 * .findByRegId(prescription.get("regId"));
		 */

		Document document = new Document();

		try {
			PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

			Font redFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
			document.open();

			Paragraph p55 = new Paragraph("\n\n\n\n");
			p55.setAlignment(Element.ALIGN_RIGHT);
			document.add(p55);

			PdfPTable table4 = new PdfPTable(1);
			table4.setWidths(new float[] { 5f });
			table4.setSpacingBefore(10);

			PdfPCell hcell11;
			hcell11 = new PdfPCell(new Phrase(consulatantBlank, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
			// hcell11.setPaddingRight(230f);
			table4.addCell(hcell11);

			if (user.getDoctorDetails().getQualification() != null) {
				PdfPCell hcell111;
				hcell111 = new PdfPCell(new Phrase(user.getDoctorDetails().getQualification(), redFontInfo));
				hcell111.setBorder(Rectangle.NO_BORDER);
				hcell111.setHorizontalAlignment(Element.ALIGN_RIGHT);
				// hcell111.setPaddingRight(230f);
				table4.addCell(hcell111);
			}

			PdfPCell hcell112;
			hcell112 = new PdfPCell(new Phrase(deptNameBlank, redFontInfo));
			hcell112.setBorder(Rectangle.NO_BORDER);
			hcell112.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell112.setPaddingBottom(10);
			// hcell112.setPaddingRight(230f);
			table4.addCell(hcell112);
			document.add(table4);

			// Display a date in day, month, year format
			Date date = Calendar.getInstance().getTime();
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			String today = formatter.format(date).toString();

			PdfPTable table = new PdfPTable(2);
			PdfPCell cell19 = new PdfPCell();
			table.setWidthPercentage(100f);
			cell19.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
			// cell19.setBorder(Rectangle);

			PdfPTable table3 = new PdfPTable(6);
			table3.setWidths(new float[] { 5f, 1f, 5f, 5f, 1f, 5f });
			table3.setSpacingBefore(10);

			PdfPCell hcell1;
			hcell1 = new PdfPCell(new Phrase(PATIENT_NAME_SAVEINFO_PDF, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-50f);
			table3.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-50f);
			table3.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(patientNameBlank, redFont1));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-50f);
			table3.addCell(hcell1);

			PdfPCell hcell10;
			hcell10 = new PdfPCell(new Phrase(AGE_GENDER_SAVEINFO_PDF, redFont));
			hcell10.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell10);

			hcell10 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell10.setBorder(Rectangle.NO_BORDER);
			hcell10.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell10);

			hcell10 = new PdfPCell(new Phrase(
					patientDetails.getAge() + BACKWARD_SLASH_ADV_PDF + patientDetails.getGender(), redFont1));
			hcell10.setBorder(Rectangle.NO_BORDER);
			hcell10.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell10);

			PdfPCell hcell2;
			hcell2 = new PdfPCell(new Phrase(VISIT_TYPE_SAVEINFO_PDF, redFont));
			hcell2.setBorder(Rectangle.NO_BORDER);
			hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2.setPaddingLeft(-50f);
			table3.addCell(hcell2);

			hcell2 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell2.setBorder(Rectangle.NO_BORDER);
			hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2.setPaddingLeft(-50f);
			table3.addCell(hcell2);

			hcell2 = new PdfPCell(new Phrase(String.valueOf(visitTypeBlank), redFont1));
			hcell2.setBorder(Rectangle.NO_BORDER);
			hcell2.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2.setPaddingLeft(-50f);
			table3.addCell(hcell2);

			PdfPCell hcell20;
			hcell20 = new PdfPCell(new Phrase(REG_NO_SAVEINFO_PDF, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(regId, redFont1));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell20);

			PdfPCell hcell3;
			hcell3 = new PdfPCell(new Phrase(REF_DOCTOR_SAVEINFO_PDF, redFont));
			hcell3.setBorder(Rectangle.NO_BORDER);
			hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell3.setPaddingLeft(-50f);
			table3.addCell(hcell3);

			hcell3 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell3.setBorder(Rectangle.NO_BORDER);
			hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell3.setPaddingLeft(-50f);
			table3.addCell(hcell3);

			String ref = null;
			if (patientDetails.getvRefferalDetails() != null) {
				ref = patientDetails.getvRefferalDetails().getRefName();
			} else {
				ref = EMPTY_STRING;
			}

			hcell3 = new PdfPCell(new Phrase(ref, redFont1));
			hcell3.setBorder(Rectangle.NO_BORDER);
			hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell3.setPaddingLeft(-50f);
			table3.addCell(hcell3);

			PdfPCell hcell30;
			hcell30 = new PdfPCell(new Phrase(UMR_SAVEINFO_PDF, redFont));
			hcell30.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell30);

			hcell30 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell30.setBorder(Rectangle.NO_BORDER);
			table3.addCell(hcell30);

			hcell30 = new PdfPCell(new Phrase(patientDetails.getUmr(), redFont1));
			hcell30.setBorder(Rectangle.NO_BORDER);
			hcell30.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell30);

			PdfPCell hcell301;
			hcell301 = new PdfPCell(new Phrase(ADDRESS_ADV_PDF, redFont));
			hcell301.setBorder(Rectangle.NO_BORDER);
			hcell301.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell301.setPaddingLeft(-50f);
			table3.addCell(hcell301);

			hcell301 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont));
			hcell301.setBorder(Rectangle.NO_BORDER);
			hcell301.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell301.setPaddingLeft(-50f);
			table3.addCell(hcell301);

			hcell301 = new PdfPCell(new Phrase(address, redFont1));
			hcell301.setBorder(Rectangle.NO_BORDER);
			hcell301.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell301.setPaddingLeft(-50f);
			table3.addCell(hcell301);

			PdfPCell hcell3011;
			hcell3011 = new PdfPCell(new Phrase(DATE_SAVEINFO_PDF, redFont));
			hcell3011.setBorder(Rectangle.NO_BORDER);
			hcell3011.setPaddingBottom(5f);
			table3.addCell(hcell3011);

			hcell3011 = new PdfPCell(new Phrase(COLON_SAVEINFO_PDF, redFont1));
			hcell3011.setBorder(Rectangle.NO_BORDER);
			hcell3011.setPaddingBottom(5f);
			table3.addCell(hcell3011);

			hcell3011 = new PdfPCell(new Phrase(today, redFont1));
			hcell3011.setBorder(Rectangle.NO_BORDER);
			hcell3011.setPaddingBottom(5f);
			hcell3011.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell3011);

			cell19.setColspan(2);
			cell19.addElement(table3);
			table3.addCell(cell19);
			table.addCell(cell19);
			document.add(table);
			document.close();
			pdfBytes = byteArrayOutputStream.toByteArray();
			String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(V1_PAYMENT_VIEWFILE_PATH)
					.path(paymentPdfServiceImpl.getNextPdfId()).toUriString();

			patientPaymentPdf = new PatientPaymentPdf(regId + BLANK_PRESCRIPTION, uri, pdfBytes);
			patientPaymentPdf.setPid(paymentPdfServiceImpl.getNextPdfId());
			paymentPdfServiceImpl.save(patientPaymentPdf);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// ------------------------------------------------------Blank Prescription
		// ending-------------------------------------------------------

		// Display a date in day, month, year format
		Date date = new Date(regDateTimestamp.getTime());
		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		String today = formatter.format(date).toString();

		date = new Date(new Timestamp(System.currentTimeMillis()).getTime());
		formatter = new SimpleDateFormat(FROM_DATE_FORMAT_ADV_PDF);
		String printedDt = formatter.format(date).toString();

		// for advance reciept
		//shantharam addr

		String addr = "";

		// for consultation reciept
		String addrss = "";

		patientPaymentPdf = null;
		pdfBytes = null;
		byteArrayOutputStream = new ByteArrayOutputStream();

		if (patientRegistration.getPatientDetails().getvRefferalDetails() != null) {
			refBy = patientRegistration.getPatientDetails().getvRefferalDetails().getRefName();
		} else {
			refBy = EMPTY_STRING;
		}
		document = new Document(PageSize.A4.rotate());

		if (patientType.equals(ConstantValues.INPATIENT)) {
			try {
				document = new Document(PageSize.A4_LANDSCAPE);

				redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

				Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
				Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
				PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

				Resource fileResourcee = resourceLoader.getResource(UDBHAVA_PNG_CLASSPATH);

				Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());

				document.open();
				PdfPTable table = new PdfPTable(2);

				img.scaleAbsolute(75, 95);
				table.setWidthPercentage(105);

				Phrase pq = new Phrase(new Chunk(img, 0, -80));
				pq.add(new Chunk(addr, redFont));
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
				table99.setWidths(new float[] { 3f, 1f, 5f });
				table99.setSpacingBefore(10);

				PdfPCell hcell90;
				hcell90 = new PdfPCell(new Phrase(PATIENT_ADV_PDF, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setPaddingBottom(-7f);
				hcell90.setPaddingLeft(-25f);
				table99.addCell(hcell90);

				hcell90 = new PdfPCell(new Phrase(COLON, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingLeft(-45f);
				table99.addCell(hcell90);

				hcell90 = new PdfPCell(new Phrase(patientName, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingBottom(-7f);
				hcell90.setPaddingLeft(-60f);
				table99.addCell(hcell90);

				cell3.addElement(table99);

				PdfPTable table2 = new PdfPTable(6);
				table2.setWidths(new float[] { 3f, 1f, 5f, 3f, 1f, 4f });
				table2.setSpacingBefore(10);

				PdfPCell hcell1;
				hcell1 = new PdfPCell(new Phrase(UMR_NO_ADV_PDF, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingLeft(-25f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(COLON, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-10f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(umr, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-15f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingRight(34f);
				hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingRight(34f);
				hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setPaddingRight(34f);
				hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell1);

				PdfPCell hcell4;
				hcell4 = new PdfPCell(new Phrase(RECT_NO_ADV_PDF, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setPaddingLeft(-25f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-10f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(regId, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-15f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(RECT_DATE_ADV_PDF, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingRight(-27.5f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell4.setPaddingRight(-0.1f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(today, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingRight(-20.5f);
				table2.addCell(hcell4);

				PdfPCell hcell15;
				hcell15 = new PdfPCell(new Phrase(AGE_SEX_ADV_PDF, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingLeft(-25f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(COLON, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-10f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(age + BACKWARD_SLASH_ADV_PDF + gender, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-15f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(PHONE_ADV_PDF, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setPaddingRight(-27.5f);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				table2.addCell(hcell15);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell4.setPaddingRight(-0.1f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(phone, redFont));
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
				hcell19 = new PdfPCell(new Phrase(ADVANCE_RECIEPT_ADV_PDF, headFont1));
				hcell19.setBorder(Rectangle.NO_BORDER);
				hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
				// hcell19.setPaddingLeft(-70f);
				table21.addCell(hcell19);

				cell19.setFixedHeight(20f);
				cell19.setColspan(2);
				cell19.addElement(table21);
				table.addCell(cell19);

				PdfPCell cell4 = new PdfPCell();

				PdfPTable table3 = new PdfPTable(6);
				table3.setWidths(new float[] { 5f, 1f, 6f, 5f, 1f, 6f });
				table3.setSpacingBefore(10);

				PdfPCell hcell;
				hcell = new PdfPCell(new Phrase(FATHER_ADV_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setPaddingLeft(-50f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(COLON, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-80f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(father, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-80f);
				table3.addCell(hcell);

				Font redFont5 = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);

				hcell = new PdfPCell(new Phrase("Admitted Ward", redFont5));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(COLON, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-15f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(admittedWard, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-20f);
				table3.addCell(hcell);

				PdfPCell hcell11;
				hcell11 = new PdfPCell(new Phrase(ADMITTED_DATE, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setPaddingLeft(-50f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(COLON, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-80f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(today, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-80f);
				table3.addCell(hcell11);

				String dpt = null;
				if (department != null) {
					dpt = department;
				} else {
					dpt = EMPTY_STRING;
				}
				hcell11 = new PdfPCell(new Phrase(DEPARTMENT_ADV_PDF, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(COLON, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-15f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(dpt, redFont5));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-20f);
				table3.addCell(hcell11);

				PdfPCell hcell14;
				hcell14 = new PdfPCell(new Phrase(CONSULTANT_ADV_PDF, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setPaddingLeft(-50f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(COLON, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-80f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(consultant, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-80f);
				table3.addCell(hcell14);

				String cFee = null;
				if (docamount != null) {
					cFee = docamount;
				} else {
					cFee = "0";
				}

				hcell14 = new PdfPCell(new Phrase(CONSULTATION_FEE_SAVE_INFO_PDF, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(COLON, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-15f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(cFee, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-20f);
				table3.addCell(hcell14);

				PdfPCell hcell16;
				hcell16 = new PdfPCell(new Phrase(ORG_ADV_PDF, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell16);

				hcell16 = new PdfPCell(new Phrase(COLON, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell16.setPaddingLeft(-80f);
				table3.addCell(hcell16);

				hcell16 = new PdfPCell(new Phrase(ConstantValues.HOSPITAL_NAME, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell16.setPaddingLeft(-80f);
				table3.addCell(hcell16);

				String aAmt = null;
				if (advanceAmount != null) {
					aAmt = advanceAmount;
				} else {
					aAmt = EMPTY_STRING;
				}
				hcell16 = new PdfPCell(new Phrase(ADVANCE_AMOUNT_SAVEINFO_PDF, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell16);

				hcell16 = new PdfPCell(new Phrase(COLON, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell16.setPaddingLeft(-15f);
				table3.addCell(hcell16);

				hcell16 = new PdfPCell(new Phrase(aAmt, redFont));
				hcell16.setBorder(Rectangle.NO_BORDER);
				hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell16.setPaddingLeft(-20f);
				table3.addCell(hcell16);

				PdfPCell hcell17;
				hcell17 = new PdfPCell(new Phrase(ADDRESS_ADV_PDF, redFont));
				hcell17.setBorder(Rectangle.NO_BORDER);
				hcell17.setPaddingLeft(-50f);
				table3.addCell(hcell17);

				hcell17 = new PdfPCell(new Phrase(COLON, redFont));
				hcell17.setBorder(Rectangle.NO_BORDER);
				hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell17.setPaddingLeft(-80f);
				table3.addCell(hcell17);

				hcell17 = new PdfPCell(new Phrase(address, redFont));
				hcell17.setBorder(Rectangle.NO_BORDER);
				hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell17.setPaddingLeft(-80f);
				table3.addCell(hcell17);

				if (toc.equalsIgnoreCase(NURSING_CHARGES_SAVEINFO_PDF)) {
					hcell17 = new PdfPCell(new Phrase(toc, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(COLON, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-15f);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-20f);
					table3.addCell(hcell17);
				} else if (toc.equalsIgnoreCase(SERVICE_CHARGES_SAVEINFO_PDF)) {
					hcell17 = new PdfPCell(new Phrase(toc, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(COLON, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-15f);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-20f);
					table3.addCell(hcell17);
				} else if (toc.equalsIgnoreCase(REG_FEES_SAVEINFO_PDF)) {
					hcell17 = new PdfPCell(new Phrase(toc, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(COLON, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-15f);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-20f);
					table3.addCell(hcell17);
				} else if (toc.equalsIgnoreCase(VACCINATION_FEES_SAVEINFO_PDF)) {
					hcell17 = new PdfPCell(new Phrase(toc, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(COLON, redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-15f);
					table3.addCell(hcell17);

					hcell17 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
					hcell17.setBorder(Rectangle.NO_BORDER);
					hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell17.setPaddingLeft(-20f);
					table3.addCell(hcell17);
				}

				PdfPCell hcell18;
				hcell18 = new PdfPCell(new Phrase(CITY_ADV_PDF, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setPaddingLeft(-50f);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(COLON, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell18.setPaddingLeft(-80f);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(city, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell18.setPaddingLeft(-80f);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(RECT_AMOUNT_ADV_PDF, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(COLON, redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell18.setPaddingLeft(-15f);
				table3.addCell(hcell18);

				hcell18 = new PdfPCell(new Phrase(String.valueOf(rectAmount), redFont));
				hcell18.setBorder(Rectangle.NO_BORDER);
				hcell18.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell18.setPaddingLeft(-20f);
				table3.addCell(hcell18);

				PdfPCell hcell20;
				hcell20 = new PdfPCell(new Phrase(STATE_ADV_PDF, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setPaddingLeft(-50f);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(COLON, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell20.setPaddingLeft(-80f);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(state, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell20.setPaddingLeft(-80f);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell20);

				hcell20 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
				hcell20.setBorder(Rectangle.NO_BORDER);
				hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
				table3.addCell(hcell20);

				PdfPTable table91 = new PdfPTable(1);
				table91.setWidths(new float[] { 5f });
				table91.setSpacingBefore(10);

				cell4.setFixedHeight(170f);
				cell4.setColspan(2);
				cell4.addElement(table3);

				PdfPCell hcell98;
				hcell98 = new PdfPCell(new Phrase(
						RECIEVED_WITH_THANKS_SAVEINFO_PDF + patientName + ", " + A_SUM_OF_RS_SAVEINFO_PDF + rectAmount
								+ "\n\n" + IN_WORDS_RS_SAVEINFO_PDF + numberToWordsConverter.convert(rectAmount),
						redFont));
				hcell98.setBorder(Rectangle.NO_BORDER);
				hcell98.setPaddingLeft(-50f);
				hcell98.setPaddingTop(3);
				table91.addCell(hcell98);
				cell4.addElement(table91);
				table.addCell(cell4);

				PdfPCell cell5 = new PdfPCell();

				PdfPTable table35 = new PdfPTable(2);
				table35.setWidths(new float[] { 5f, 4f });
				table35.setSpacingBefore(10);

				PdfPCell hcell21;
				Font redFont3 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
				hcell21 = new PdfPCell(new Phrase(STAR + umr + STAR, redFont3));
				hcell21.setBorder(Rectangle.NO_BORDER);
				hcell21.setPaddingLeft(-50f);
				table35.addCell(hcell21);

				hcell21 = new PdfPCell(new Phrase(STAR + regId + STAR, redFont3));
				hcell21.setBorder(Rectangle.NO_BORDER);
				hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell21);

				PdfPCell hcell12;
				hcell12 = new PdfPCell(new Phrase(CREATED_BY_SAVEINFO_PDF + createdBy, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				hcell12.setPaddingTop(10f);
				hcell12.setPaddingLeft(-50f);
				table35.addCell(hcell12);

				hcell12 = new PdfPCell(new Phrase(CREATED_DATE + printedDt, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				hcell12.setPaddingTop(10f);
				hcell12.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell12);

				PdfPCell hcell13;
				hcell13 = new PdfPCell(new Phrase(PRINTED_BY_SAVEINFO_PDF + createdBy, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingLeft(-50f);
				table35.addCell(hcell13);

				hcell13 = new PdfPCell(new Phrase(PRINTED_DATE + printedDt, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingRight(3f);
				hcell13.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell13);

				PdfPCell hcell23;
				hcell23 = new PdfPCell(new Phrase(EMPTY_STRING));
				hcell23.setBorder(Rectangle.NO_BORDER);
				table35.addCell(hcell23);

				hcell23 = new PdfPCell(new Phrase(AUTHORIZED_SIGNATURE, headFont));
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
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(V1_PAYMENT_VIEWFILE_PATH)
						.path(paymentPdfServiceImpl.getNextPdfId()).toUriString();

				patientPaymentPdf = new PatientPaymentPdf(regId + " Consultation Reciept", uri, pdfBytes);
				patientPaymentPdf.setPid(paymentPdfServiceImpl.getNextPdfId());
				paymentPdfServiceImpl.save(patientPaymentPdf);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {

				Document document1 = new Document(PageSize.A4_LANDSCAPE);

				redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

				Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
				Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
				PdfWriter r = PdfWriter.getInstance(document1, byteArrayOutputStream);
				document1.open();
				PdfPTable table = new PdfPTable(2);
				Resource fileResourcee = resourceLoader.getResource(UDBHAVA_PNG_CLASSPATH);

				Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());

				img.scaleAbsolute(75, 95);
				table.setWidthPercentage(105);

				Phrase pq = new Phrase(new Chunk(img, 0, -80));
				pq.add(new Chunk(addrss, redFont));
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
				cell1.addElement(pq);
				cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell1);

				PdfPCell cell3 = new PdfPCell();

				PdfPTable table99 = new PdfPTable(3);
				table99.setWidths(new float[] { 3f, 1f, 5f });
				table99.setSpacingBefore(10);

				PdfPCell hcell90;
				hcell90 = new PdfPCell(new Phrase(PATIENT_ADV_PDF, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingLeft(-25f);
				hcell90.setPaddingTop(15f);
				table99.addCell(hcell90);

				hcell90 = new PdfPCell(new Phrase(COLON, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingLeft(-50f);
				hcell90.setPaddingTop(15f);
				table99.addCell(hcell90);

				hcell90 = new PdfPCell(new Phrase(patientName, redFont));
				hcell90.setBorder(Rectangle.NO_BORDER);
				hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell90.setPaddingLeft(-65f);
				hcell90.setPaddingTop(15f);
				table99.addCell(hcell90);

				cell3.addElement(table99);
				// table.addCell(cell3);

				PdfPTable table2 = new PdfPTable(6);
				table2.setWidths(new float[] { 3f, 1f, 5f, 3f, 1f, 4f });
				table2.setSpacingBefore(10);

				PdfPCell hcell1;
				hcell1 = new PdfPCell(new Phrase(AGE_SEX_ADV_PDF, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-25f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(COLON, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-15f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(age + BACKWARD_SLASH_ADV_PDF + gender, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingLeft(-20f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(UMR_NO_ADV_PDF, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingRight(-27.5f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(COLON, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
				hcell1.setPaddingTop(-5f);
				;
				table2.addCell(hcell1);

				hcell1 = new PdfPCell(new Phrase(umr, redFont));
				hcell1.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell1.setPaddingRight(-27.5f);
				hcell1.setPaddingTop(-5f);
				table2.addCell(hcell1);

				PdfPCell hcell4;
				hcell4 = new PdfPCell(new Phrase(CONST_NO_SAVEINFO_PDF, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-25f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-15f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(regId, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingLeft(-20f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(CONST_DATE_SAVEINFO_PDF, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingRight(-27.5f);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(COLON, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell4);

				hcell4 = new PdfPCell(new Phrase(today, redFont));
				hcell4.setBorder(Rectangle.NO_BORDER);
				hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell4.setPaddingRight(-27.5f);
				table2.addCell(hcell4);

				PdfPCell hcell15;

				String ref = null;
				if (refBy != null) {
					ref = refBy;
				} else {
					ref = EMPTY_STRING;
				}
				hcell15 = new PdfPCell(new Phrase(REF_BY_SAVEINFO_PDF, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-25f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(COLON, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-15f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(ref, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingLeft(-20f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(PHONE_ADV_PDF, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingRight(-27.5f);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(COLON, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table2.addCell(hcell15);

				hcell15 = new PdfPCell(new Phrase(phone, redFont));
				hcell15.setBorder(Rectangle.NO_BORDER);
				hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell15.setPaddingRight(-27.5f);
				table2.addCell(hcell15);

				cell3.setFixedHeight(115f);
				cell3.setColspan(2);
				cell3.addElement(table2);
				table.addCell(cell3);

				PdfPCell cell19 = new PdfPCell();

				PdfPTable table21 = new PdfPTable(1);
				table21.setWidths(new float[] { 4f });
				table21.setSpacingBefore(10);

				PdfPCell hcell19;
				hcell19 = new PdfPCell(new Phrase(CONSULTATION_RECIEPT_SAVEINFO_PDF, headFont1));
				hcell19.setBorder(Rectangle.NO_BORDER);
				hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
				table21.addCell(hcell19);

				cell19.setFixedHeight(20f);
				cell19.setColspan(2);
				cell19.addElement(table21);
				table.addCell(cell19);

				PdfPCell cell4 = new PdfPCell();

				PdfPTable table3 = new PdfPTable(6);
				table3.setWidths(new float[] { 4f, 1f, 5f, 5f, 1f, 6f });
				table3.setSpacingBefore(10);

				PdfPCell hcell;
				hcell = new PdfPCell(new Phrase(CONSULTANT_ADV_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-50f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(COLON, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-65f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(consultant, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-80f);
				table3.addCell(hcell);

				String dpt = null;
				if (department != null) {
					dpt = department;
				} else {
					dpt = EMPTY_STRING;
				}
				hcell = new PdfPCell(new Phrase(DEPT_NAME_SAVEINFO_PDF, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingRight(27.5f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(COLON, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-27f);
				table3.addCell(hcell);

				hcell = new PdfPCell(new Phrase(dpt, redFont));
				hcell.setBorder(Rectangle.NO_BORDER);
				hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell.setPaddingLeft(-40f);
				table3.addCell(hcell);

				PdfPCell hcell11;
				hcell11 = new PdfPCell(new Phrase(VISIT_TYPE_SAVEINFO_PDF, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-50f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(COLON, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-65f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(patientType, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-80f);
				table3.addCell(hcell11);

				String docFee = null;
				if (docamtPdf != 0) {
					docFee = String.valueOf(docamtPdf);
				} else {
					docFee = EMPTY_STRING;
				}
				hcell11 = new PdfPCell(new Phrase("Consultant Fee", redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingRight(27.5f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(COLON, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-27f);
				table3.addCell(hcell11);

				hcell11 = new PdfPCell(new Phrase(docFee, redFont));
				hcell11.setBorder(Rectangle.NO_BORDER);
				hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell11.setPaddingLeft(-40f);
				table3.addCell(hcell11);

				PdfPCell hcell14;

				hcell14 = new PdfPCell(new Phrase(PAYMENT_MODE_SAVEINFO_PDF, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-50f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(COLON, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-65f);
				table3.addCell(hcell14);

				hcell14 = new PdfPCell(new Phrase(paymentMode, redFont));
				hcell14.setBorder(Rectangle.NO_BORDER);
				hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell14.setPaddingLeft(-80f);
				table3.addCell(hcell14);

				if (toc != null) {
					if (toc.equalsIgnoreCase(NURSING_CHARGES_SAVEINFO_PDF)) {
						hcell14 = new PdfPCell(new Phrase(toc, redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingRight(27.5f);
						table3.addCell(hcell14);

						hcell14 = new PdfPCell(new Phrase(COLON, redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingLeft(-27f);
						table3.addCell(hcell14);

						hcell14 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingLeft(-40f);
						table3.addCell(hcell14);
					} else if (toc.equalsIgnoreCase(SERVICE_CHARGES_SAVEINFO_PDF)) {
						hcell14 = new PdfPCell(new Phrase(toc, redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingRight(27.5f);
						table3.addCell(hcell14);

						hcell14 = new PdfPCell(new Phrase(COLON, redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingLeft(-27f);
						table3.addCell(hcell14);

						hcell14 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingLeft(-40f);
						table3.addCell(hcell14);
					} else if (toc.equalsIgnoreCase(REG_FEES_SAVEINFO_PDF)) {
						hcell14 = new PdfPCell(new Phrase(toc, redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingRight(27.5f);
						table3.addCell(hcell14);

						hcell14 = new PdfPCell(new Phrase(COLON, redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingLeft(-27f);
						table3.addCell(hcell14);

						hcell14 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingLeft(-40f);
						table3.addCell(hcell14);
					} else if (toc.equalsIgnoreCase(VACCINATION_FEES_SAVEINFO_PDF)) {
						hcell14 = new PdfPCell(new Phrase(toc, redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingRight(27.5f);
						table3.addCell(hcell14);

						hcell14 = new PdfPCell(new Phrase(COLON, redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingLeft(-27f);
						table3.addCell(hcell14);

						hcell14 = new PdfPCell(new Phrase(String.valueOf(amtPdf), redFont));
						hcell14.setBorder(Rectangle.NO_BORDER);
						hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
						hcell14.setPaddingLeft(-40f);
						table3.addCell(hcell14);
					}
				} else {
					hcell14 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingRight(27.5f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-27f);
					table3.addCell(hcell14);

					hcell14 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
					hcell14.setBorder(Rectangle.NO_BORDER);
					hcell14.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell14.setPaddingLeft(-40f);
					table3.addCell(hcell14);
				}

				PdfPCell hcell16;

				if (docmop == null) {
					hcell16 = new PdfPCell(new Phrase(RECEIPT_NO_SAVEINFO_PDF, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setPaddingLeft(-50f);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(COLON, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-65f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(regId, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-80f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-20f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-27f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-40f);
					table3.addCell(hcell16);

				} else {
					hcell16 = new PdfPCell(new Phrase(RECEIPT_NO_SAVEINFO_PDF, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setPaddingLeft(-50f);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(COLON, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-65f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(regId, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-80f);
					table3.addCell(hcell16);

					if (paymentMode.contains("Debit")
							|| paymentMode.contains("Credit") && !paymentMode.contains("Note")) {
						hcell16 = new PdfPCell(new Phrase("Card Amount", redFont));
					} else if (paymentMode.equalsIgnoreCase(ConstantValues.CASH)) {
						hcell16 = new PdfPCell(new Phrase("Cash Amount", redFont));
					} else if (paymentMode.equalsIgnoreCase(ConstantValues.CHEQUE)) {
						hcell16 = new PdfPCell(new Phrase("Cheque Amount", redFont));
					} else if (paymentMode.equalsIgnoreCase(ConstantValues.CASH_PLUS_CARD)) {
						hcell16 = new PdfPCell(new Phrase("Cash + Card", redFont));
					} else if (paymentMode.contains("Note")) {
						hcell16 = new PdfPCell(new Phrase("Credit Note", redFont));
					}
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingRight(27.5f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(COLON, redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-27f);
					table3.addCell(hcell16);

					hcell16 = new PdfPCell(new Phrase(String.valueOf((int) amtPdf + (int) docamtPdf), redFont));
					hcell16.setBorder(Rectangle.NO_BORDER);
					hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
					hcell16.setPaddingLeft(-40f);
					table3.addCell(hcell16);

				}

				PdfPTable table91 = new PdfPTable(1);
				table91.setWidths(new float[] { 5f });
				table91.setSpacingBefore(10);

				cell4.setFixedHeight(130f);
				cell4.setColspan(2);
				cell4.addElement(table3);

				PdfPCell hcell98;
				hcell98 = new PdfPCell(new Phrase(VALIDITY + TWO_VISITS_BEFORE + regValidity + RECEIVED_WITH_THANKS_FROM
						+ patientName + ", " + A_SUM_OF_RS_SAVEINFO_PDF + String.valueOf((int) amtPdf + (int) docamtPdf)
						+ "\n\n" + IN_WORDS_RS_SAVEINFO_PDF
						+ numberToWordsConverter.convert((int) amtPdf + (int) docamtPdf), redFont));
				hcell98.setBorder(Rectangle.NO_BORDER);
				// hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell98.setPaddingLeft(-50f);
				hcell98.setPaddingTop(5);
				table91.addCell(hcell98);
				cell4.addElement(table91);

				table.addCell(cell4);

				PdfPCell cell5 = new PdfPCell();

				PdfPTable table35 = new PdfPTable(2);
				table35.setWidths(new float[] { 5f, 4f });
				table35.setSpacingBefore(10);

				PdfPCell hcell21;
				Font redFont3 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
				hcell21 = new PdfPCell(new Phrase(STAR + umr + STAR, redFont3));
				hcell21.setBorder(Rectangle.NO_BORDER);
				// hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell21.setPaddingLeft(-50f);
				table35.addCell(hcell21);

				hcell21 = new PdfPCell(new Phrase(STAR + regId + STAR, redFont3));
				hcell21.setBorder(Rectangle.NO_BORDER);
				hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell21);

				PdfPCell hcell12;
				hcell12 = new PdfPCell(new Phrase(CREATED_BY_SAVEINFO_PDF + createdBy, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				// hcell3.setHorizontalAlignment(Element.ALIGN_LEFT);
				hcell12.setPaddingTop(5f);
				hcell12.setPaddingLeft(-50f);
				table35.addCell(hcell12);

				hcell12 = new PdfPCell(new Phrase(CREATED_DATE + printedDt, redFont));
				hcell12.setBorder(Rectangle.NO_BORDER);
				hcell12.setPaddingTop(5f);
				// hcell12.setPaddingRight(0f);
				hcell12.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell12);

				PdfPCell hcell13;
				hcell13 = new PdfPCell(new Phrase(PRINTED_BY_SAVEINFO_PDF + createdBy, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table35.addCell(hcell13);

				hcell13 = new PdfPCell(new Phrase(PRINTED_DATE + printedDt, redFont));
				hcell13.setBorder(Rectangle.NO_BORDER);
				hcell13.setPaddingRight(3f);
				hcell13.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell13);

				PdfPCell hcell23;
				hcell23 = new PdfPCell(new Phrase(EMPTY_STRING));
				hcell23.setBorder(Rectangle.NO_BORDER);
				// hcell23.setPaddingLeft(-50f);
				// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				table35.addCell(hcell23);

				hcell23 = new PdfPCell(new Phrase(AUTHORIZED_SIGNATURE, headFont));
				hcell23.setBorder(Rectangle.NO_BORDER);
				hcell23.setPaddingTop(15f);
				hcell23.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table35.addCell(hcell23);

				cell5.setFixedHeight(95f);
				cell5.setColspan(2);
				cell5.addElement(table35);
				table.addCell(cell5);

				document1.add(table);

				document1.close();
				pdfBytes = byteArrayOutputStream.toByteArray();
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(V1_PAYMENT_VIEWFILE_PATH)
						.path(paymentPdfServiceImpl.getNextPdfId()).toUriString();

				patientPaymentPdf = new PatientPaymentPdf(regId + " Consultation Reciept", uri, pdfBytes);
				patientPaymentPdf.setPid(paymentPdfServiceImpl.getNextPdfId());
				paymentPdfServiceImpl.save(patientPaymentPdf);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return patientPaymentPdf;
	}

	@Override
	public List<Map<String, String>> inPatientDetails() {
		List<Map<String, String>> display = new ArrayList<>();
		String regDate = null;
		String inpatient = null;
		String outpatient = null;

		

		List<PatientRegistration> patientRegistration = patientRegistrationServiceImpl
				.findByPType(ConstantValues.INPATIENT);

		for (PatientRegistration patientRegistrationInfo : patientRegistration) {
			Map<String, String> displayInfo = new HashMap<>();

			float payment = 0;

			displayInfo.put("name", patientRegistrationInfo.getPatientDetails().getTitle() + ". "
					+ patientRegistrationInfo.getPatientDetails().getFirstName() + ConstantValues.ONE_SPACE_STRING
					+ patientRegistrationInfo.getPatientDetails().getLastName());

			displayInfo.put("patType", patientRegistrationInfo.getpType());

			displayInfo.put("umr", patientRegistrationInfo.getPatientDetails().getUmr());

			displayInfo.put("doctor", patientRegistrationInfo.getPatientDetails().getConsultant());

			displayInfo.put("DOJ", String.valueOf(patientRegistrationInfo.getDateOfJoining()).substring(0, 10));

			List<ChargeBill> patList = chargeBillRepository.findByPatRegId(patientRegistrationInfo);

			if (!patList.isEmpty()) {
				if (patList.get(0).getDichargedDate() != null) {

					displayInfo.put("DOD", String.valueOf(patList.get(0).getDichargedDate()).substring(0, 10));

				} else {

					displayInfo.put("DOD", NOT_DISCHARGED);
				}
			}

			List<RoomBookingDetails> roomBookingDetails = patientRegistrationInfo.getRoomBookingDetails();
			for (RoomBookingDetails roomBookingDetailsInfo : roomBookingDetails) {
				displayInfo.put("room", roomBookingDetailsInfo.getBedNo());
			}

			List<PatientPayment> patientPayment = paymentRepository
					.findByPatientRegistration(patientRegistrationInfo.getRegId(), ConstantValues.NO);
			for (PatientPayment patientPaymentInfo : patientPayment) {
				payment += patientPaymentInfo.getAmount();

			}

			List<ChargeBill> chargeBills = chargeBillRepository.findByPatRegIdStatus(patientRegistrationInfo.getRegId(),
					ConstantValues.NO);
			for (ChargeBill chargeBillInfo : chargeBills) {
				payment += chargeBillInfo.getNetAmount();
			}

			displayInfo.put("soFar", String.valueOf(Math.round(payment)));
			displayInfo.put("regId", patientRegistrationInfo.getRegId());

			displayInfo.put("advance", String.valueOf(patientRegistrationInfo.getAdvanceAmount()));

			display.add(displayInfo);

		}

		return display;

	}

		@Override
	public List<Map<String, String>> outPatientDetails(String type) {

		List<Map<String, String>> display = new ArrayList<>();
		String regDate = null;
		String inpatient = null;
		String outpatient = null;
		String docName = EMPTY_STRING;
		String twodayback = EMPTY_STRING;

		String today = new Timestamp(System.currentTimeMillis()).toString().substring(0, 10);
		String nextDay = LocalDate.parse(today).plusDays(1).toString();
		if (type.equalsIgnoreCase(TWO_DAYS)) {
			twodayback = LocalDate.parse(today).plusDays(-2).toString();
		} else if (type.equalsIgnoreCase(SEVEN_DAYS)) {
			twodayback = LocalDate.parse(today).plusDays(-7).toString();
		} else if (type.equalsIgnoreCase(FIFTEEN_DAYS)) {
			twodayback = LocalDate.parse(today).plusDays(-15).toString();
		} else if (type.equalsIgnoreCase(THIRTY_DAYS)) {
			twodayback = LocalDate.parse(today).plusDays(-30).toString();
		}

		List<PatientRegistration> patientRegistration = patientRegistrationServiceImpl.onlyOutPatientTwoDays(twodayback,
				nextDay);

		for (PatientRegistration patientRegistrationInfo : patientRegistration) {
			Map<String, String> displayInfo = new HashMap<>();

			long payment = 0;

			displayInfo.put("name", patientRegistrationInfo.getPatientDetails().getTitle() + ". "
					+ patientRegistrationInfo.getPatientDetails().getFirstName() + ConstantValues.ONE_SPACE_STRING
					+ patientRegistrationInfo.getPatientDetails().getLastName());

			displayInfo.put("patType", patientRegistrationInfo.getpType());

			displayInfo.put("umr", patientRegistrationInfo.getPatientDetails().getUmr());

			docName = (patientRegistrationInfo.getVuserD().getMiddleName() != null)
					? patientRegistrationInfo.getVuserD().getFirstName() + ConstantValues.ONE_SPACE_STRING
							+ patientRegistrationInfo.getVuserD().getMiddleName() + ConstantValues.ONE_SPACE_STRING
							+ patientRegistrationInfo.getVuserD().getLastName()
					: patientRegistrationInfo.getVuserD().getFirstName() + ConstantValues.ONE_SPACE_STRING
							+ patientRegistrationInfo.getVuserD().getLastName();

			displayInfo.put("doctor", docName);

			String dojDate = String.valueOf(patientRegistrationInfo.getDateOfJoining());
			SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
			try {
				dojDate = toFormat.format(fromFormat.parse(dojDate));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			displayInfo.put("DOJ", dojDate);

			List<PatientPayment> patientPayment = paymentRepository
					.findByPatientRegistration(patientRegistrationInfo.getRegId(), ConstantValues.NO);
			for (PatientPayment patientPaymentInfo : patientPayment) {
				payment += patientPaymentInfo.getAmount();

			}

			displayInfo.put("soFar", String.valueOf(payment));
			displayInfo.put("regId", patientRegistrationInfo.getRegId());

			displayInfo.put("advance", String.valueOf(patientRegistrationInfo.getAdvanceAmount()));

			display.add(displayInfo);

		}

		return display;
	}

	public List<PatientDetails> patientAlreadyExists(String mobile, String fname, String lname) {
		return patientDetailsRepository.patientAlreadyExists(mobile, fname, lname);
	}

	public PatientDetails findByMobile(long mobile) {
		return patientDetailsRepository.findByMobile(mobile);
	}
		
		
		
	public List<Map<String, String>> outPatientDetailsByConsultant(Map<String, String> map) {

		List<Map<String, String>> display = new ArrayList<>();
		String regDate = null;
		String inpatient = null;
		String outpatient = null;

		String date = map.get("date").toString().substring(0, 10);

		String consultant = map.get("consultant");

		String[] docName = consultant.split("-");
		String name = docName[0];
		String id = docName[1];

		List<PatientRegistration> patientRegistration = patientRegistrationRepository.findPatientListByConsultant(id,
				date, ConstantValues.OUTPATIENT);

		for (PatientRegistration patientRegistrationInfo : patientRegistration) {
			Map<String, String> displayInfo = new HashMap<>();

			long payment = 0;

			displayInfo.put("name", patientRegistrationInfo.getPatientDetails().getTitle() + ". "
					+ patientRegistrationInfo.getPatientDetails().getFirstName() + ConstantValues.ONE_SPACE_STRING
					+ patientRegistrationInfo.getPatientDetails().getLastName());

			displayInfo.put("patType", patientRegistrationInfo.getpType());

			displayInfo.put("umr", patientRegistrationInfo.getPatientDetails().getUmr());

			displayInfo.put("doctor", patientRegistrationInfo.getPatientDetails().getConsultant());

			displayInfo.put("DOJ", String.valueOf(patientRegistrationInfo.getDateOfJoining()).substring(0, 10));

			List<PatientPayment> patientPayment = paymentRepository
					.findByPatientRegistration(patientRegistrationInfo.getRegId(), ConstantValues.NO);
			for (PatientPayment patientPaymentInfo : patientPayment) {
				payment += patientPaymentInfo.getAmount();

			}

			displayInfo.put("soFar", String.valueOf(payment));
			displayInfo.put("regId", patientRegistrationInfo.getRegId());

			displayInfo.put("advance", String.valueOf(patientRegistrationInfo.getAdvanceAmount()));

			display.add(displayInfo);

		}

		return display;
	}
		
	public PatientPaymentPdf admissionSlipInfo(String regId, Principal principal) {
		// createdBy Security
		User userSecurity = userServiceImpl.findByUserName(principal.getName());
		String createdBy = userSecurity.getFirstName() + ConstantValues.ONE_SPACE_STRING + userSecurity.getMiddleName()
				+ ConstantValues.ONE_SPACE_STRING + userSecurity.getLastName();

		//shantharam addr

		String addres = "";

		PatientRegistration patientRegistrationInfo = patientRegistrationServiceImpl.findByRegId(regId);

		PatientDetails patientDetails = patientRegistrationInfo.getPatientDetails();

		String wardName = null;
		String bedName = null;

		RoomBookingDetails roomBookingDetails = roomBookingDetailsServiceImpl
				.findByPatientRegistrationBooking(patientRegistrationInfo);
		if (roomBookingDetails != null) {
			RoomDetails roomDetails = roomBookingDetails.getRoomDetails();
			wardName = roomDetails.getRoomType();
			bedName = roomDetails.getBedName();
		}

		

		String date = patientRegistrationInfo.getRegDate().toString();

		Timestamp timestamp = Timestamp.valueOf(date);
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp.getTime());

		String from1 = dateFormat.format(calendar.getTime());

		
		List<SpecUserJoin> specUserJoins = patientRegistrationInfo.getVuserD().getSpecUserJoin();

		String department = specUserJoins.get(0).getDocSpec().getSpecName();

		String dpt = null;
		if (department != null) {
			dpt = department;
		} else {
			dpt = EMPTY_STRING;
		}

		PatientDetails patient = patientRegistrationInfo.getPatientDetails();
		String patientName = null;

		String pfn = null;
		String pmn = null;
		String pln = null;
		if (patient.getFirstName() == null) {
			pfn = ConstantValues.ONE_SPACE_STRING;
		} else {
			pfn = patient.getFirstName();
		}
		if (patient.getMiddleName() == null) {
			pmn = EMPTY_STRING;
		} else {
			pmn = patient.getMiddleName();
		}
		if (patient.getLastName() == null) {
			pln = ConstantValues.ONE_SPACE_STRING;
		} else {
			pln = patient.getLastName();
		}
		if (pmn.equalsIgnoreCase(EMPTY_STRING)) {
			patientName = patient.getTitle() + ". " + patient.getFirstName() + ConstantValues.ONE_SPACE_STRING
					+ patient.getLastName();
		} else {
			patientName = patient.getTitle() + ". " + patient.getFirstName() + ConstantValues.ONE_SPACE_STRING
					+ patient.getMiddleName() + ConstantValues.ONE_SPACE_STRING + patient.getLastName();
		}

		// Display a date in day, month, year format
		Date date1 = Calendar.getInstance().getTime();

		DateFormat formatter = new SimpleDateFormat(FROM_DATE_FORMAT_ADV_PDF);
		String today = formatter.format(date1).toString();

		PatientPaymentPdf patientPaymentPdf = null;
		byte[] pdfBytes = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		// -----------------new pdf code------------------------------
		try {
			Document document = new Document(PageSize.A4_LANDSCAPE);

			Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

			Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
			Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
			PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

			Resource fileResourcee = resourceLoader.getResource(UDBHAVA_PNG_CLASSPATH);

			Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());

			document.open();
			PdfPTable table = new PdfPTable(2);

			img.scaleAbsolute(75, 95);
			table.setWidthPercentage(105);

			Phrase pq = new Phrase(new Chunk(img, 0, -80));
			pq.add(new Chunk(addres, redFont));
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
			table99.setWidths(new float[] { 3f, 1f, 5f });
			table99.setSpacingBefore(10);

			PdfPCell hcell90;
			hcell90 = new PdfPCell(new Phrase(PATIENT_ADV_PDF, redFont));
			hcell90.setBorder(Rectangle.NO_BORDER);
			hcell90.setPaddingBottom(-7f);
			hcell90.setPaddingLeft(-25f);
			table99.addCell(hcell90);

			hcell90 = new PdfPCell(new Phrase(COLON, redFont));
			hcell90.setBorder(Rectangle.NO_BORDER);
			hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell90.setPaddingLeft(-55f);
			table99.addCell(hcell90);

			hcell90 = new PdfPCell(new Phrase(patientName, redFont));
			hcell90.setBorder(Rectangle.NO_BORDER);
			hcell90.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell90.setPaddingBottom(-7f);
			hcell90.setPaddingLeft(-75f);
			table99.addCell(hcell90);

			cell3.addElement(table99);

			PdfPTable table2 = new PdfPTable(6);
			table2.setWidths(new float[] { 4f, 1f, 5f, 3f, 1f, 4f });
			table2.setSpacingBefore(10);

			PdfPCell hcell1;
			hcell1 = new PdfPCell(new Phrase("Adm.No", redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setPaddingLeft(-25f);
			hcell1.setPaddingTop(-5f);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(COLON, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-30f);
			hcell1.setPaddingTop(-5f);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(patientRegistrationInfo.getRegId(), redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-38f);
			hcell1.setPaddingTop(-5f);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase("Adm.Dt", redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setPaddingLeft(-15f);
			hcell1.setPaddingTop(-5f);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(COLON, redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-5f);
			hcell1.setPaddingTop(-5f);
			table2.addCell(hcell1);

			hcell1 = new PdfPCell(new Phrase(from1.substring(0, 11), redFont));
			hcell1.setBorder(Rectangle.NO_BORDER);
			// hcell1.setPaddingRight(34f);
			hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell1.setPaddingLeft(-10f);
			hcell1.setPaddingTop(-5f);
			table2.addCell(hcell1);

			PdfPCell hcell4;
			hcell4 = new PdfPCell(new Phrase(UMR_NO_ADV_PDF, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setPaddingLeft(-25f);
			// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(COLON, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(-30f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(patientDetails.getUmr(), redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(-38f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase("Adm.Time", redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(-15f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(COLON, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(-5f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(from1.substring(11, 20), redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(-10f);
			table2.addCell(hcell4);

			PdfPCell hcell15;
			hcell15 = new PdfPCell(new Phrase(AGE_SEX_ADV_PDF, redFont));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setPaddingLeft(-25f);
			// hcell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			table2.addCell(hcell15);

			hcell15 = new PdfPCell(new Phrase(COLON, redFont));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell15.setPaddingLeft(-30f);
			table2.addCell(hcell15);

			hcell15 = new PdfPCell(
					new Phrase(patientDetails.getAge() + BACKWARD_SLASH_ADV_PDF + patientDetails.getGender(), redFont));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell15.setPaddingLeft(-38f);
			table2.addCell(hcell15);

			hcell15 = new PdfPCell(new Phrase(PHONE_ADV_PDF, redFont));
			hcell15.setBorder(Rectangle.NO_BORDER);
			hcell15.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell15.setPaddingLeft(-15f);
			table2.addCell(hcell15);

			hcell4 = new PdfPCell(new Phrase(COLON, redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(-5f);
			table2.addCell(hcell4);

			hcell4 = new PdfPCell(new Phrase(String.valueOf(patientDetails.getMobile()), redFont));
			hcell4.setBorder(Rectangle.NO_BORDER);
			hcell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell4.setPaddingLeft(-10f);
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
			hcell19 = new PdfPCell(new Phrase("Admission Slip", headFont1));
			hcell19.setBorder(Rectangle.NO_BORDER);
			hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
			// hcell19.setPaddingLeft(-70f);
			table21.addCell(hcell19);

			cell19.setFixedHeight(20f);
			cell19.setColspan(2);
			cell19.addElement(table21);
			table.addCell(cell19);

			PdfPCell cell4 = new PdfPCell();

			PdfPTable table3 = new PdfPTable(6);
			table3.setWidths(new float[] { 5f, 1f, 6f, 5f, 1f, 6f });
			table3.setSpacingBefore(10);

			PdfPCell hcell;
			hcell = new PdfPCell(new Phrase("S/O.D/O.W/O", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setPaddingLeft(-50f);
			table3.addCell(hcell);

			hcell = new PdfPCell(new Phrase(COLON, redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell.setPaddingLeft(-65f);
			table3.addCell(hcell);

			hcell = new PdfPCell(new Phrase(patientDetails.getMotherName(), redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell.setPaddingLeft(-75f);
			table3.addCell(hcell);

			hcell = new PdfPCell(new Phrase("Mother Name", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell);

			hcell = new PdfPCell(new Phrase(COLON, redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell.setPaddingLeft(-20f);
			table3.addCell(hcell);

			hcell = new PdfPCell(new Phrase("Not Specified", redFont));
			hcell.setBorder(Rectangle.NO_BORDER);
			hcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell.setPaddingLeft(-30f);
			table3.addCell(hcell);

			PdfPCell hcell11;
			hcell11 = new PdfPCell(new Phrase(CONSULTANT_ADV_PDF, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setPaddingLeft(-50f);
			table3.addCell(hcell11);

			hcell11 = new PdfPCell(new Phrase(COLON, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell11.setPaddingLeft(-65f);
			table3.addCell(hcell11);

			hcell11 = new PdfPCell(new Phrase(patientDetails.getConsultant(), redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell11.setPaddingLeft(-75f);
			table3.addCell(hcell11);

			hcell11 = new PdfPCell(new Phrase(DEPARTMENT_ADV_PDF, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell11);

			hcell11 = new PdfPCell(new Phrase(COLON, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell11.setPaddingLeft(-20f);
			table3.addCell(hcell11);

			hcell11 = new PdfPCell(new Phrase(dpt, redFont));
			hcell11.setBorder(Rectangle.NO_BORDER);
			hcell11.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell11.setPaddingLeft(-30f);
			table3.addCell(hcell11);

			PdfPCell hcell16;
			hcell16 = new PdfPCell(new Phrase("Ref. By", redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setPaddingLeft(-50f);
			table3.addCell(hcell16);

			hcell16 = new PdfPCell(new Phrase(COLON, redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell16.setPaddingLeft(-65f);
			table3.addCell(hcell16);

			if (patientDetails.getvRefferalDetails() != null) {
				hcell16 = new PdfPCell(new Phrase(patientDetails.getvRefferalDetails().getRefName(), redFont));
			} else {
				hcell16 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));

			}
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell16.setPaddingLeft(-75f);
			table3.addCell(hcell16);

			hcell16 = new PdfPCell(new Phrase("Admn.Type", redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell16);

			hcell16 = new PdfPCell(new Phrase(COLON, redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell16.setPaddingLeft(-20f);
			table3.addCell(hcell16);

			hcell16 = new PdfPCell(new Phrase("GENERAL", redFont));
			hcell16.setBorder(Rectangle.NO_BORDER);
			hcell16.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell16.setPaddingLeft(-30f);
			table3.addCell(hcell16);

			PdfPCell hcell17;
			hcell17 = new PdfPCell(new Phrase("Patient Type", redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setPaddingLeft(-50f);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase(COLON, redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell17.setPaddingLeft(-65f);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase(patientRegistrationInfo.getpType(), redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell17.setPaddingLeft(-75f);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase("Marital Status", redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase(COLON, redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell17.setPaddingLeft(-20f);
			table3.addCell(hcell17);

			hcell17 = new PdfPCell(new Phrase(patientDetails.getMaritialStatus(), redFont));
			hcell17.setBorder(Rectangle.NO_BORDER);
			hcell17.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell17.setPaddingLeft(-30f);
			table3.addCell(hcell17);

			PdfPCell hcell20;
			hcell20 = new PdfPCell(new Phrase(ADDRESS_ADV_PDF, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setPaddingLeft(-50f);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(COLON, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20.setPaddingLeft(-65f);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(patientDetails.getAddress(), redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20.setPaddingLeft(-75f);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase("Ward/Bed", redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(COLON, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20.setPaddingLeft(-20);
			table3.addCell(hcell20);

			hcell20 = new PdfPCell(new Phrase(wardName + " / " + bedName, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20.setPaddingLeft(-30f);
			table3.addCell(hcell20);

			PdfPCell hcell201;
			hcell201 = new PdfPCell(new Phrase("City Name", redFont));
			hcell201.setBorder(Rectangle.NO_BORDER);
			hcell201.setPaddingLeft(-50f);
			table3.addCell(hcell201);

			hcell201 = new PdfPCell(new Phrase(COLON, redFont));
			hcell201.setBorder(Rectangle.NO_BORDER);
			hcell201.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell201.setPaddingLeft(-65f);
			table3.addCell(hcell201);

			hcell201 = new PdfPCell(new Phrase(patientDetails.getCity(), redFont));
			hcell201.setBorder(Rectangle.NO_BORDER);
			hcell201.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell201.setPaddingLeft(-75f);
			table3.addCell(hcell201);

			hcell201 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell201.setBorder(Rectangle.NO_BORDER);
			hcell201.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell201);

			hcell201 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell201.setBorder(Rectangle.NO_BORDER);
			hcell201.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell201.setPaddingLeft(-20f);
			table3.addCell(hcell201);

			hcell201 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell201.setBorder(Rectangle.NO_BORDER);
			hcell201.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell201.setPaddingLeft(-30f);
			table3.addCell(hcell201);

			PdfPCell hcell2011;
			hcell2011 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell2011.setBorder(Rectangle.NO_BORDER);
			hcell2011.setPaddingLeft(-50f);
			table3.addCell(hcell2011);

			hcell2011 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell2011.setBorder(Rectangle.NO_BORDER);
			hcell2011.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2011.setPaddingLeft(-65f);
			table3.addCell(hcell2011);

			hcell2011 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell2011.setBorder(Rectangle.NO_BORDER);
			hcell2011.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2011.setPaddingLeft(-75f);
			table3.addCell(hcell2011);

			hcell2011 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell2011.setBorder(Rectangle.NO_BORDER);
			hcell2011.setHorizontalAlignment(Element.ALIGN_LEFT);
			table3.addCell(hcell2011);

			hcell2011 = new PdfPCell(new Phrase(EMPTY_STRING, redFont));
			hcell2011.setBorder(Rectangle.NO_BORDER);
			hcell2011.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2011.setPaddingLeft(-20f);
			table3.addCell(hcell2011);

			hcell2011 = new PdfPCell(new Phrase("Patient/Attendent Signature", headFont));
			hcell2011.setBorder(Rectangle.NO_BORDER);
			hcell2011.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell2011.setPaddingLeft(-20f);
			table3.addCell(hcell2011);

			PdfPTable table91 = new PdfPTable(1);
			table91.setWidths(new float[] { 5f });
			table91.setSpacingBefore(10);

			// cell4.setFixedHeight(170f);
			cell4.setColspan(2);
			cell4.addElement(table3);

			PdfPCell cell5 = new PdfPCell();

			PdfPTable table35 = new PdfPTable(6);
			table35.setWidths(new float[] { 5f, 1f, 6f, 5f, 1f, 6f });
			table35.setSpacingBefore(10);

			PdfPCell hcell20112;
			hcell20112 = new PdfPCell(new Phrase("Created By", redFont));
			hcell20112.setBorder(Rectangle.NO_BORDER);
			hcell20112.setPaddingLeft(-50f);
			table35.addCell(hcell20112);

			hcell20112 = new PdfPCell(new Phrase(COLON, redFont));
			hcell20112.setBorder(Rectangle.NO_BORDER);
			hcell20112.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20112.setPaddingLeft(-75f);
			table35.addCell(hcell20112);

			hcell20112 = new PdfPCell(new Phrase(createdBy, redFont));
			hcell20112.setBorder(Rectangle.NO_BORDER);
			hcell20112.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20112.setPaddingLeft(-85f);
			table35.addCell(hcell20112);

			hcell20112 = new PdfPCell(new Phrase("Created Date", redFont));
			hcell20112.setBorder(Rectangle.NO_BORDER);
			hcell20112.setHorizontalAlignment(Element.ALIGN_LEFT);
			table35.addCell(hcell20112);

			hcell20112 = new PdfPCell(new Phrase(COLON, redFont));
			hcell20112.setBorder(Rectangle.NO_BORDER);
			hcell20112.setHorizontalAlignment(Element.ALIGN_LEFT);
			table35.addCell(hcell20112);

			hcell20112 = new PdfPCell(new Phrase(today, redFont));
			hcell20112.setBorder(Rectangle.NO_BORDER);
			hcell20112.setHorizontalAlignment(Element.ALIGN_LEFT);
			table35.addCell(hcell20112);

			PdfPCell hcell20113;
			hcell20113 = new PdfPCell(new Phrase("Printed By", redFont));
			hcell20113.setBorder(Rectangle.NO_BORDER);
			hcell20113.setPaddingLeft(-50f);
			table35.addCell(hcell20113);

			hcell20113 = new PdfPCell(new Phrase(COLON, redFont));
			hcell20113.setBorder(Rectangle.NO_BORDER);
			hcell20113.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20113.setPaddingLeft(-75f);
			table35.addCell(hcell20113);

			hcell20113 = new PdfPCell(new Phrase(createdBy, redFont));
			hcell20113.setBorder(Rectangle.NO_BORDER);
			hcell20113.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell20113.setPaddingLeft(-85f);
			table35.addCell(hcell20113);

			hcell20113 = new PdfPCell(new Phrase("Printed Date", redFont));
			hcell20113.setBorder(Rectangle.NO_BORDER);
			hcell20113.setHorizontalAlignment(Element.ALIGN_LEFT);
			table35.addCell(hcell20113);

			hcell20113 = new PdfPCell(new Phrase(COLON, redFont));
			hcell20113.setBorder(Rectangle.NO_BORDER);
			hcell20113.setHorizontalAlignment(Element.ALIGN_LEFT);
			table35.addCell(hcell20113);

			hcell20113 = new PdfPCell(new Phrase(today, redFont));
			hcell20113.setBorder(Rectangle.NO_BORDER);
			hcell20113.setHorizontalAlignment(Element.ALIGN_LEFT);
			table35.addCell(hcell20113);

			PdfPCell hcell21;
			Font redFont3 = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
			hcell21 = new PdfPCell(new Phrase(STAR + patientDetails.getUmr() + STAR, redFont3));
			hcell21.setBorder(Rectangle.NO_BORDER);
			hcell21.setPaddingLeft(-50f);
			table35.addCell(hcell21);

			hcell21 = new PdfPCell(new Phrase(EMPTY_STRING, redFont3));
			hcell21.setBorder(Rectangle.NO_BORDER);
			hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table35.addCell(hcell21);

			hcell21 = new PdfPCell(new Phrase(EMPTY_STRING, redFont3));
			hcell21.setBorder(Rectangle.NO_BORDER);
			hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table35.addCell(hcell21);

			hcell21 = new PdfPCell(new Phrase(EMPTY_STRING, redFont3));
			hcell21.setBorder(Rectangle.NO_BORDER);
			hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table35.addCell(hcell21);

			hcell21 = new PdfPCell(new Phrase(EMPTY_STRING, redFont3));
			hcell21.setBorder(Rectangle.NO_BORDER);
			hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table35.addCell(hcell21);

			hcell21 = new PdfPCell(new Phrase(STAR + patientRegistrationInfo.getRegId() + STAR, redFont3));
			hcell21.setBorder(Rectangle.NO_BORDER);
			hcell21.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table35.addCell(hcell21);

			// cell5.setFixedHeight(105f);
			cell5.setColspan(2);
			cell5.addElement(table35);
			table.addCell(cell4);
			table.addCell(cell5);

			document.add(table);

			document.close();

			System.out.println("finished");

			pdfBytes = byteArrayOutputStream.toByteArray();
			String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(V1_PAYMENT_VIEWFILE_PATH)
					.path(paymentPdfServiceImpl.getNextPdfId()).toUriString();

			patientPaymentPdf = new PatientPaymentPdf(patientRegistrationInfo.getRegId() + " Admission Slip", uri,
					pdfBytes);
			patientPaymentPdf.setPid(paymentPdfServiceImpl.getNextPdfId());
			paymentPdfServiceImpl.save(patientPaymentPdf);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return patientPaymentPdf;
	}
		
	public List<Object> getDischargeAndNonDischargePatients(String dischargeType) {
		List<Object> display = new ArrayList<>();


		List<PatientRegistration> patientRegistration = patientRegistrationServiceImpl
				.findByPType(ConstantValues.INPATIENT);

		List<ChargeBill> patList = null;
		for (PatientRegistration patientRegistrationInfo : patientRegistration) {
			Map<String, String> displayInfo = new HashMap<>();

			float payment = 0;

			if (dischargeType.equalsIgnoreCase("DISCHARGED")) {
				patList = chargeBillRepository.findByPatRegIdAndDischarged(patientRegistrationInfo.getRegId());

				if (!patList.isEmpty()) {
					if (patList.get(0).getDichargedDate() != null) {

						displayInfo.put("name",
								patientRegistrationInfo.getPatientDetails().getTitle() + ". "
										+ patientRegistrationInfo.getPatientDetails().getFirstName()
										+ ConstantValues.ONE_SPACE_STRING
										+ patientRegistrationInfo.getPatientDetails().getLastName());

						displayInfo.put("patType", patientRegistrationInfo.getpType());

						displayInfo.put("umr", patientRegistrationInfo.getPatientDetails().getUmr());

						displayInfo.put("doctor", (patientRegistrationInfo.getVuserD().getMiddleName() != null)
								? patientRegistrationInfo.getVuserD().getFirstName() + ConstantValues.ONE_SPACE_STRING
										+ patientRegistrationInfo.getVuserD().getMiddleName()
										+ ConstantValues.ONE_SPACE_STRING
										+ patientRegistrationInfo.getVuserD().getLastName()
								: patientRegistrationInfo.getVuserD().getFirstName() + ConstantValues.ONE_SPACE_STRING
										+ patientRegistrationInfo.getVuserD().getLastName());
						// for different format
						String daoDate = String.valueOf(patientRegistrationInfo.getDateOfJoining().toString());
						SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
						try {
							daoDate = toFormat.format(fromFormat.parse(daoDate));
						} catch (ParseException e) {
							e.printStackTrace();
						}

						displayInfo.put("DOJ", daoDate);
						displayInfo.put("timeOfAdmission",
								String.valueOf(patientRegistrationInfo.getDateOfJoining()).substring(11, 16));

						// for dischargedate
						String dischargeDate = String.valueOf(patList.get(0).getDichargedDate().toString());
						SimpleDateFormat fromFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						SimpleDateFormat toFormat1 = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
						try {
							dischargeDate = toFormat1.format(fromFormat1.parse(dischargeDate));
						} catch (ParseException e) {
							e.printStackTrace();
						}

						displayInfo.put("DOD", dischargeDate);
						displayInfo.put("timeOfDischarge",
								String.valueOf(patList.get(0).getDichargedDate()).substring(11, 16));
						List<RoomBookingDetails> roomBookingDetails = patientRegistrationInfo.getRoomBookingDetails();
						for (RoomBookingDetails roomBookingDetailsInfo : roomBookingDetails) {
							displayInfo.put("room", roomBookingDetailsInfo.getBedNo());
						}

						List<PatientPayment> patientPayment = paymentRepository
								.findByPatientRegistration(patientRegistrationInfo.getRegId(), ConstantValues.NO);
						for (PatientPayment patientPaymentInfo : patientPayment) {
							payment += patientPaymentInfo.getAmount();

						}

						List<ChargeBill> chargeBills = chargeBillRepository
								.findByPatRegIdStatus(patientRegistrationInfo.getRegId(), ConstantValues.NO);
						for (ChargeBill chargeBillInfo : chargeBills) {
							payment += chargeBillInfo.getNetAmount();
						}

						displayInfo.put("soFar", String.valueOf(Math.round(payment)));
						displayInfo.put("regId", patientRegistrationInfo.getRegId());

						displayInfo.put("advance", String.valueOf(patientRegistrationInfo.getAdvanceAmount()));

						display.add(displayInfo);
					}
				}
			} else if (dischargeType.equalsIgnoreCase("NOT DISCHARGED")) {

				patList = chargeBillRepository.findByPatRegIdAndNotDischarged(patientRegistrationInfo.getRegId());

				if (!patList.isEmpty()) {
					if (patList.get(0).getDichargedDate() == null) {

						displayInfo.put("name",
								patientRegistrationInfo.getPatientDetails().getTitle() + ". "
										+ patientRegistrationInfo.getPatientDetails().getFirstName()
										+ ConstantValues.ONE_SPACE_STRING
										+ patientRegistrationInfo.getPatientDetails().getLastName());

						displayInfo.put("patType", patientRegistrationInfo.getpType());

						displayInfo.put("umr", patientRegistrationInfo.getPatientDetails().getUmr());

						displayInfo.put("doctor", (patientRegistrationInfo.getVuserD().getMiddleName() != null)
								? patientRegistrationInfo.getVuserD().getFirstName() + ConstantValues.ONE_SPACE_STRING
										+ patientRegistrationInfo.getVuserD().getMiddleName()
										+ ConstantValues.ONE_SPACE_STRING
										+ patientRegistrationInfo.getVuserD().getLastName()
								: patientRegistrationInfo.getVuserD().getFirstName() + ConstantValues.ONE_SPACE_STRING
										+ patientRegistrationInfo.getVuserD().getLastName());

						// for different format
						String daoDate = String.valueOf(patientRegistrationInfo.getDateOfJoining().toString());
						SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						SimpleDateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
						try {
							daoDate = toFormat.format(fromFormat.parse(daoDate));
						} catch (ParseException e) {
							e.printStackTrace();
						}

						displayInfo.put("DOJ", daoDate);
						displayInfo.put("timeOfAdmission",
								String.valueOf(patientRegistrationInfo.getDateOfJoining()).substring(11, 16));
						displayInfo.put("DOD", NOT_DISCHARGED);
						displayInfo.put("timeOfDischarge", ConstantValues.NO);

						List<RoomBookingDetails> roomBookingDetails = patientRegistrationInfo.getRoomBookingDetails();
						for (RoomBookingDetails roomBookingDetailsInfo : roomBookingDetails) {
							displayInfo.put("room", roomBookingDetailsInfo.getBedNo());
						}

						List<PatientPayment> patientPayment = paymentRepository
								.findByPatientRegistration(patientRegistrationInfo.getRegId(), ConstantValues.NO);
						for (PatientPayment patientPaymentInfo : patientPayment) {
							payment += patientPaymentInfo.getAmount();

						}

						List<ChargeBill> chargeBills = chargeBillRepository
								.findByPatRegIdStatus(patientRegistrationInfo.getRegId(), ConstantValues.NO);
						for (ChargeBill chargeBillInfo : chargeBills) {
							payment += chargeBillInfo.getNetAmount();
						}

						displayInfo.put("soFar", String.valueOf(Math.round(payment)));
						displayInfo.put("regId", patientRegistrationInfo.getRegId());

						displayInfo.put("advance", String.valueOf(patientRegistrationInfo.getAdvanceAmount()));

						display.add(displayInfo);
					}
				}

			}

		}

		return display;
	}


	
}
