package com.vncdigital.vpulse.controller;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.access.intercept.MethodInvocationPrivilegeEvaluator;
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
import com.vncdigital.vpulse.pharmacist.dto.MedicineProcurementDto;
import com.vncdigital.vpulse.pharmacist.helper.HeaderFooterPageEvent;
import com.vncdigital.vpulse.pharmacist.model.Location;
import com.vncdigital.vpulse.pharmacist.model.MedicineDetails;
import com.vncdigital.vpulse.pharmacist.model.MedicineProcurement;
import com.vncdigital.vpulse.pharmacist.model.Sales;
import com.vncdigital.vpulse.pharmacist.model.SalesPaymentPdf;
import com.vncdigital.vpulse.pharmacist.model.SalesReturn;
import com.vncdigital.vpulse.pharmacist.model.Vendors;
import com.vncdigital.vpulse.pharmacist.repository.MedicineDetailsRepository;
import com.vncdigital.vpulse.pharmacist.repository.MedicineProcurementRepository;
import com.vncdigital.vpulse.pharmacist.repository.SalesRepository;
import com.vncdigital.vpulse.pharmacist.repository.SalesReturnRepository;
import com.vncdigital.vpulse.pharmacist.serviceImpl.MedicineDetailsServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.MedicineProcurementServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.SalesPaymentPdfServiceImpl;
import com.vncdigital.vpulse.pharmacist.serviceImpl.VendorsServiceImpl;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;

@CrossOrigin(origins="*",maxAge=36000)
@RestController
@RequestMapping("/v1/pharmacist")
public class ProcurementController {
	
	public static Logger Logger=LoggerFactory.getLogger(ProcurementController.class);
	
	
	@Autowired
	MedicineProcurementServiceImpl  medicineProcurementServiceImpl;
	
	@Autowired
	MedicineProcurementRepository medicineProcurementRepository;
	
	@Autowired	
	MedicineDetailsRepository 	medicineDetailsServiceImpl;
	
	@Autowired
	UserServiceImpl userServiceImpl;
	
	@Autowired
	SalesRepository salesRepository;
	
	@Autowired
	SalesReturnRepository salesReturnRepository;
	
	@Autowired
	VendorsServiceImpl vendorsServiceImpl;
	
	@Autowired
	ResourceLoader resourceLoader;
	
	@Autowired
	SalesPaymentPdfServiceImpl salesPaymentPdfServiceImpl;
	
	
	/*
	 * Page Loading	
	 */
	@RequestMapping(value="/procurement/create",method=RequestMethod.GET)
	public List<Object> getProcurementIds()
	{
		return medicineProcurementServiceImpl.getProcurementIds();
		
	}
	
	/*
	 * To create procurement
	 */
	@RequestMapping(value="/procurement/create",method=RequestMethod.POST)
	public SalesPaymentPdf createProcurement(@RequestBody MedicineProcurementDto medicineProcurementDto,Principal principal)
	{
		MedicineProcurement medicineProcurement=new MedicineProcurement();
		BeanUtils.copyProperties(medicineProcurementDto, medicineProcurement);
		return medicineProcurementServiceImpl.computeSave(medicineProcurement,principal);
		
		
	}
	
	/*
	 * Procurement Update
	 */
	@RequestMapping(value="/procurement/update",method=RequestMethod.PUT)
	public SalesPaymentPdf updateProcurement(@RequestBody MedicineProcurementDto medicineProcurementDto,Principal principal)
	{
		MedicineProcurement medicineProcurement=new MedicineProcurement();
		BeanUtils.copyProperties(medicineProcurementDto, medicineProcurement);
		return medicineProcurementServiceImpl.updateSave(medicineProcurement,principal);
		
		
	}
	
	/*
	 * to get procurement report
	 */
	@RequestMapping(value="/procurement/getReport/{id}",method=RequestMethod.GET)
	public List<SalesPaymentPdf> getReport(@PathVariable String id)
	{
		String procId=id+" Procurement";
		List<SalesPaymentPdf> salesPaymentPdfs=salesPaymentPdfServiceImpl.getAllReport(procId);
		return salesPaymentPdfs;
	}
	
	
	/*
	 * To get One proccurement
	 */
	@RequestMapping(value="/procurement/getOne/{procId}",method=RequestMethod.GET)
	public List<MedicineProcurement> getOneProcurement(@PathVariable String procId)
	{
		return medicineProcurementServiceImpl.findByProcurementId(procId);
	}
	
	/*
	 * get All procurement
	 */
	@RequestMapping(value="/procurement/getAll",method=RequestMethod.GET)
	public List<Object> getAllProcurement()
	{	
		return medicineProcurementServiceImpl.getAllProcurement(); 
	}
	
	/*
	 * to get draft's procurement
	 */
	@RequestMapping(value="/procurement/draft/{invNo}",method=RequestMethod.GET)
	public List<MedicineProcurement> getDrafts(@PathVariable String invNo)
	{
		List<MedicineProcurement> procList=medicineProcurementRepository.findByInvoiceNoAndDraft(invNo,"YES");
		procList.forEach((s) -> {
			String dop=s.getDateOfProcurement().toString().substring(0,10);
			
			SimpleDateFormat from=new SimpleDateFormat("yyyy-mm-dd");
			SimpleDateFormat to=new SimpleDateFormat("dd-mm-yyyy");
			
			 try
			 {
			s.setDateOfProc(to.format(from.parse(dop)));
			}
			 catch (ParseException e) 
			 {
				e.printStackTrace();
			}
			
			s.setLocation(s.getMedicineProcurmentLocation().getLocationName());
			s.setVendorName(s.getMedicineProcurmentVendors().getVendorName());
		});
		return procList;
	}
	
	/*
	 * get All one procurement
	 */
	@RequestMapping(value="/procurement/getCombine/{days}",method=RequestMethod.GET)
	public ArrayList<Object> getOneAllProcurement(@PathVariable int days)
	{	
		ArrayList<Object> returnInfo=new ArrayList<>();
		
		ArrayList<Map<String,String>> info=new ArrayList<>();
		
		Map<String,String> displayInfo=null;
		ArrayList<String> procId=new ArrayList<>();
		
		List<MedicineProcurement> medicineProcurement=null;
		String nextDay=null;
		String fromDay=null;
		String today=new Timestamp(System.currentTimeMillis()).toString().substring(0,10);
		
		
		if(days==2)
		{
			nextDay=LocalDate.parse(today).plusDays(1).toString();
			fromDay=LocalDate.parse(today).plusDays(-2).toString();
			
			medicineProcurement=medicineProcurementRepository.findAllByOrderByMasterProcurementIdDesc(fromDay,nextDay);
		}
		else if(days==7)
		{
			nextDay=LocalDate.parse(today).plusDays(1).toString();
			fromDay=LocalDate.parse(today).plusDays(-7).toString();
			
			medicineProcurement=medicineProcurementRepository.findAllByOrderByMasterProcurementIdDesc(fromDay,nextDay);
		}
		else if(days==15)
		{
			nextDay=LocalDate.parse(today).plusDays(1).toString();
			fromDay=LocalDate.parse(today).plusDays(-15).toString();
			
			medicineProcurement=medicineProcurementRepository.findAllByOrderByMasterProcurementIdDesc(fromDay,nextDay);
		}
		else if(days==30)
		{
			nextDay=LocalDate.parse(today).plusDays(1).toString();
			fromDay=LocalDate.parse(today).plusDays(-30).toString();
			
			medicineProcurement=medicineProcurementRepository.findAllByOrderByMasterProcurementIdDesc(fromDay,nextDay);
		}
		
		for(MedicineProcurement medicineProcurementInfo:medicineProcurement)
		{
			displayInfo=new HashMap<>();
			if(!procId.contains(medicineProcurementInfo.getProcurementId()))
			{
				List<MedicineProcurement> medicineProcurementList= medicineProcurementRepository.getGroupBy(medicineProcurementInfo.getProcurementId());
					
				float amount=0;
				for(MedicineProcurement medicineProcurementListInfo:medicineProcurementList)
				{
					amount+=medicineProcurementListInfo.getAmount();
				}
					displayInfo.put("MasterprocId",medicineProcurementInfo.getMasterProcurementId());
					displayInfo.put("procId",medicineProcurementInfo.getProcurementId());
					String procDate=medicineProcurementList.get(0).getDateOfProcurement().toString();
					SimpleDateFormat from=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					SimpleDateFormat to=new SimpleDateFormat("dd-MM-yyyy hh:mm a");
					try
					{
						procDate=to.format(from.parse(procDate));
					} catch (ParseException e) 
					{
						e.printStackTrace();
					}
					
					displayInfo.put("date",procDate);
					Vendors vendors=medicineProcurementList.get(0).getMedicineProcurmentVendors();
					Location location=medicineProcurementList.get(0).getMedicineProcurmentLocation();
					displayInfo.put("vendor",vendors.getVendorName());
					displayInfo.put("status",medicineProcurementList.get(0).getStatus());
					displayInfo.put("location",location.getLocationName());
					displayInfo.put("amount",String.valueOf(Math.round(amount)));
					displayInfo.put("invoiceNo",medicineProcurementInfo.getInvoiceNo());
				procId.add(medicineProcurementInfo.getProcurementId());
				info.add(displayInfo);
			}
			
		}
		returnInfo.add(info);
		returnInfo.add(vendorsServiceImpl.findAll());
		return returnInfo; 
	}
	
	
	/*
	 * To get all procurement
	 * for that perticular MasterProcId
	 */
	@RequestMapping(value="/getProcList/{procId}",method=RequestMethod.GET)
	public List<MedicineProcurement> getProcurementList(@PathVariable String procId)
	{
		return medicineProcurementServiceImpl.findByProcurementId(procId);
	}
	
	
	
	/*
	 * Approve procurement
	 */
	@RequestMapping(value="/procurement/approve/{procId}",method=RequestMethod.PUT)
	public SalesPaymentPdf approve(@PathVariable String procId,Principal principal)
	{
		return medicineProcurementServiceImpl.approve(procId,principal);
	}
	
	/*
	 * To get manufacturer and batch no
	 */
	@RequestMapping(value="/batch/manufacturer/{medName}",method=RequestMethod.GET)
	public Map<String, String> getManufacturer(@PathVariable String medName)
	{
		MedicineDetails medicineDetails=medicineProcurementServiceImpl.getManufacturer(medName);
		Map<String,String> medInfo=new HashMap<>();
		medInfo.put("Batch", medicineDetails.getBatchNo());
		medInfo.put("Manufacturer", medicineDetails.getManufacturer());
		return medInfo;
	}
	
	
	/*
	 * procurement stock summary
	 */
	@RequestMapping(value = "/procurement/stocksummary", method = RequestMethod.POST)
	public SalesPaymentPdf getStockSummaryDetails(@RequestBody Map<String, Timestamp> userShift, Principal principal) {

		Map<String, Timestamp> user = userShift;

		List<MedicineProcurement> procurement = medicineProcurementRepository.getStockSummary(user.get("fromDate"),
				user.get("toDate"));

		// List<Sales> salesList1 =
		// salesRepository.findTheUserWiseDetails(user.get("fromDate"),
		// user.get("toDate"));

		// List<SalesReturn> salesReturnList =
		// salesReturnRepository.findTheUserWiseDetails1(user.get("fromDate"),user.get("toDate"));

		SalesPaymentPdf salesPaymentPdf = null;

		// CreatedBy (Security)
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		String createdBy = userSecurity.getFirstName() + " " + userSecurity.getLastName();

		try {

			byte[] pdfByte = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			Document document = new Document(PageSize.A4.rotate());

			Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
			Font redFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);

			Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
			Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
			PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
			HeaderFooterPageEvent event = new HeaderFooterPageEvent();
			 writer.setPageEvent(event);
			Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");
			document.open();
			PdfPTable table = new PdfPTable(2);

			Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());
			img.scaleAbsolute(75, 95);
			table.setWidthPercentage(105f);

			Phrase pq = new Phrase(new Chunk(img, 5, -80));

			//shantharam addr
			pq.add(new Chunk(
					"",
					redFont));
			PdfPCell cellp = new PdfPCell(pq);
			PdfPCell cell1 = new PdfPCell();

			// for header Bold
			PdfPTable table96 = new PdfPTable(1);
			table96.setWidths(new float[] { 5f });
			table96.setSpacingBefore(10);

			PdfPCell hcell96;
			hcell96 = new PdfPCell(new Phrase("UDBHAVA PHARMACY", headFont1));
			hcell96.setBorder(Rectangle.NO_BORDER);
			hcell96.setHorizontalAlignment(Element.ALIGN_CENTER);
			hcell96.setPaddingLeft(25f);

			table96.addCell(hcell96);
			cell1.addElement(table96);

			PdfPTable table961 = new PdfPTable(1);
			table961.setWidths(new float[] { 5f });
			table961.setSpacingBefore(10);

			PdfPCell hcell71;
			hcell71 = new PdfPCell(new Phrase("UDBHAVA HOSPITALS", headFont1));
			hcell71.setBorder(Rectangle.NO_BORDER);
			hcell71.setHorizontalAlignment(Element.ALIGN_CENTER);
			hcell71.setPaddingLeft(25f);

			table961.addCell(hcell71);
			cell1.addElement(table961);
			// for header end
			// cell1.setFixedHeight(107f);
			cell1.addElement(pq);
			cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell1.setColspan(2);
			//table.addCell(cell1);
			
			
			PdfPTable table22 = new PdfPTable(1);
			table22.setWidths(new float[] {15f});
			table22.setSpacingBefore(10);
			 table22.setWidthPercentage(100f);

			PdfPCell hcell190;
			hcell190 = new PdfPCell(new Phrase("____________________________________________________________________________________________________________________________________", headFont1));
			hcell190.setBorder(Rectangle.NO_BORDER);
			hcell190.setHorizontalAlignment(Element.ALIGN_CENTER);
			// hcell19.setPaddingLeft(-70f);
			table22.addCell(hcell190);

		//	PdfPCell cell19 = new PdfPCell();

			PdfPTable table21 = new PdfPTable(1);
			table21.setWidths(new float[] { 4f });
			table21.setSpacingBefore(10);

			PdfPCell hcell19;
			hcell19 = new PdfPCell(new Phrase("Stock Summary", headFont1));
			hcell19.setBorder(Rectangle.NO_BORDER);
			hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
			// hcell19.setPaddingLeft(-70f);
			table21.addCell(hcell19);

			// calender format date

			String from = user.get("fromDate").toString();
			Timestamp timestamp = Timestamp.valueOf(from);
			DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timestamp.getTime());
			String from1 = dateFormat.format(calendar.getTime());

			String to = user.get("toDate").toString();
			Timestamp timestamp1 = Timestamp.valueOf(to);
			DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTimeInMillis(timestamp1.getTime());
			String to1 = dateFormat1.format(calendar1.getTime());
			Date date = Calendar.getInstance().getTime();

			// Display a date in day, month, year format
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
			String today = formatter.format(date).toString();

			PdfPCell hcell20;
			hcell20 = new PdfPCell(new Phrase("From Dt  : " + from1 + "           " + "Till Dt  : " + to1, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_CENTER);
			table21.addCell(hcell20);

			PdfPTable table3 = new PdfPTable(2);
			table3.setWidths(new float[] { 5f, 4f });
			table3.setSpacingBefore(10);

			PdfPCell hcell111;
			hcell111 = new PdfPCell(new Phrase("Printed By          :  " + createdBy, redFont));
			hcell111.setBorder(Rectangle.NO_BORDER);
			hcell111.setPaddingLeft(-70f);

			table3.addCell(hcell111);

			hcell111 = new PdfPCell(new Phrase("Printed Date & Time    :  " + today, redFont));
			hcell111.setBorder(Rectangle.NO_BORDER);
			hcell111.setPaddingRight(-70f);
			hcell111.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table3.addCell(hcell111);

			/*cell19.setFixedHeight(60f);
			cell19.setColspan(2);
			cell19.addElement(table21);
			cell19.addElement(table3);
			table.addCell(cell19);

			PdfPCell cell112 = new PdfPCell();
*/
			Font h3 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

			PdfPTable table01 = new PdfPTable(10);
			table01.setWidths(new float[] { 1.5f, 6f, 3f, 5f, 3f, 5f, 3f, 5f, 3f, 5f });
			table01.setSpacingBefore(10);

			PdfPCell hcell01;
			hcell01 = new PdfPCell(new Phrase("S.No", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Item Name", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Opening Qty", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Opening Val", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Outward Qty", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell01.setPaddingRight(-15f);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Outward Val", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Inward Qty", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell01.setPaddingRight(-15f);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Inward Val", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Closing Qty", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell01.setPaddingRight(-15f);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Closing val", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell01.setPaddingBottom(5f);
			table01.addCell(hcell01);

			float totalOpeningQty = 0;
			float totalOpeningVal = 0;
			float totalSaleQty = 0;
			float totalSaleAmt = 0;
			float totalSaleRQty = 0;
			float totalSaleRAmt = 0;
			float totalClosingQty = 0;
			float totalClosingAmt = 0;
			String drugType = null;
			List<Map<String, String>> showReturn = new ArrayList<>();

			ArrayList<String> info1 = new ArrayList<>();

			for (MedicineProcurement mp : procurement) {

				String medName = mp.getItemName();

				Map<String, String> displayInfo = new HashMap<>();
				long qty = 0;
				float amount = 0;
				long saleQnty = 0;
				long closingQty = 0;
				long saleRQty = 0;
				float saleRAmt = 0;
				float closingAmt = 0;
				float saleAmt = 0;

				ArrayList<String> medNames = new ArrayList<>();

				if (!info1.contains(medName)) {
					List<MedicineProcurement> findMedicine = medicineProcurementRepository.getMedicineName(medName);

					for (MedicineProcurement saleInfo : findMedicine) {

						displayInfo.put("MedicineName", medName);
						qty += saleInfo.getDetailedQuantity();
						amount += saleInfo.getAmount();
					}
					displayInfo.put("Qty", String.valueOf(qty));
					displayInfo.put("Amt", String.valueOf(amount));

					// List<Sales> sales1 = salesRepository.findByName(medName);
					List<Sales> sales1 = salesRepository.findStockDetails(user.get("fromDate"), user.get("toDate"),
							medName);

					for (Sales sale : sales1) {
						saleQnty += sale.getQuantity();
						saleAmt += sale.getAmount();
					}
					displayInfo.put("saleQnty", String.valueOf(saleQnty));
					displayInfo.put("saleAmt", String.valueOf(saleAmt));

					// List<SalesReturn> salesReturn = salesReturnRepository.findByName(medName);

					List<SalesReturn> salesReturn = salesReturnRepository.getStockDetails(user.get("fromDate"),
							user.get("toDate"), medName);
					for (SalesReturn salesReturn1 : salesReturn) {
						saleRQty += salesReturn1.getQuantity();
						saleRAmt += salesReturn1.getAmount();
					}

					displayInfo.put("saleRQty", String.valueOf(saleRQty));
					displayInfo.put("saleRAmt", String.valueOf(saleRAmt));

					closingQty = (qty - saleQnty + saleRQty);
					closingAmt = (amount - saleAmt + saleRAmt);

					displayInfo.put("closingQty", String.valueOf(closingQty));
					displayInfo.put("closingAmt", String.valueOf(closingAmt));

					totalOpeningQty += qty;
					totalOpeningVal += amount;
					totalSaleQty += saleQnty;
					totalSaleAmt += saleAmt;
					totalSaleRQty += saleRQty;
					totalSaleRAmt += saleRAmt;
					totalClosingQty += closingQty;
					totalClosingAmt += closingAmt;

					showReturn.add(displayInfo);
					info1.add(medName);
					saleQnty = 0;
					saleAmt = 0;
					saleRQty = 0;
					saleRAmt = 0;
					closingQty = 0;
					closingAmt = 0;

				}

			}

			int count1 = 0;
			for (Map<String, String> show1 : showReturn) {

				PdfPCell cell;
				cell = new PdfPCell(new Phrase(String.valueOf(count1 = count1 + 1), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table01.addCell(cell);

				cell = new PdfPCell(new Phrase(show1.get("MedicineName"), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table01.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf(show1.get("Qty")), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table01.addCell(cell);

				cell = new PdfPCell(new Phrase(show1.get("Amt"), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table01.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf(show1.get("saleQnty")), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setPaddingRight(-15f);
				table01.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf(show1.get("saleAmt")), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table01.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf(show1.get("saleRQty")), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setPaddingRight(-15f);
				table01.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf(show1.get("saleRAmt")), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table01.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf(show1.get("closingQty")), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setPaddingRight(-15f);
				table01.addCell(cell);

				cell = new PdfPCell(new Phrase(String.valueOf(show1.get("closingAmt")), h3));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table01.addCell(cell);

			}

		/*	cell112.setColspan(2);
			table01.setWidthPercentage(100f);
			cell112.addElement(table01);
			table.addCell(cell112);
*/
			
			//cell19.setColspan(2);
			table01.setWidthPercentage(100f);
			cell1.addElement(table22);
			cell1.addElement(table21);
			cell1.addElement(table3);
			cell1.addElement(table01);
			table.addCell(cell1);
			PdfPCell cell003 = new PdfPCell();

			PdfPTable table004 = new PdfPTable(10);
			table004.setWidths(new float[] { 1.5f, 6f, 3f, 5f, 3f, 5f, 3f, 5f, 3f, 5f });
			table004.setSpacingBefore(10);

			PdfPCell hcell001;
			hcell001 = new PdfPCell(new Phrase("", headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setBackgroundColor(BaseColor.WHITE);
			hcell001.setHorizontalAlignment(Element.ALIGN_LEFT);
			table004.addCell(hcell001);

			hcell001 = new PdfPCell(new Phrase("Total :", headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setHorizontalAlignment(Element.ALIGN_LEFT);
			table004.addCell(hcell001);

			hcell001 = new PdfPCell(new Phrase(String.valueOf(totalOpeningQty), headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setBackgroundColor(BaseColor.WHITE);
			hcell001.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table004.addCell(hcell001);

			hcell001 = new PdfPCell(new Phrase(String.valueOf(totalOpeningVal), headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setBackgroundColor(BaseColor.WHITE);
			hcell001.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table004.addCell(hcell001);

			hcell001 = new PdfPCell(new Phrase(String.valueOf(totalSaleQty), headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell001.setPaddingRight(-15f);
			table004.addCell(hcell001);

			hcell001 = new PdfPCell(new Phrase(String.valueOf(totalSaleAmt), headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table004.addCell(hcell001);

			hcell001 = new PdfPCell(new Phrase(String.valueOf(totalSaleRQty), headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell001.setPaddingRight(-15f);
			table004.addCell(hcell001);

			hcell001 = new PdfPCell(new Phrase(String.valueOf(totalSaleRAmt), headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table004.addCell(hcell001);

			hcell001 = new PdfPCell(new Phrase(String.valueOf(totalClosingQty), headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell001.setPaddingRight(-15f);
			table004.addCell(hcell001);

			hcell001 = new PdfPCell(new Phrase(String.valueOf(totalClosingAmt), headFont));
			hcell001.setBorder(Rectangle.NO_BORDER);
			hcell001.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table004.addCell(hcell001);

			cell003.setColspan(2);
			table004.setWidthPercentage(100f);
			cell003.addElement(table004);
			table.addCell(cell003);

			document.add(table);

			document.close();

			System.out.println("finished");
			pdfByte = byteArrayOutputStream.toByteArray();
			String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
					.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();

			salesPaymentPdf = new SalesPaymentPdf();
			salesPaymentPdf.setFileName("Stock Summary");
			salesPaymentPdf.setFileuri(uri);
			salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
			salesPaymentPdf.setData(pdfByte);
			System.out.println(salesPaymentPdf);
			System.out.println(drugType);

			salesPaymentPdfServiceImpl.save(salesPaymentPdf);

		} catch (Exception e) {
			Logger.error(e.getMessage());
		}

		return salesPaymentPdf;

	}	
	
	
	/*
	 * Pdf to show expired items 
	 */
	@RequestMapping(value = "/procurement/itemstatus", method = RequestMethod.POST)
	public SalesPaymentPdf getItemExpiryStatus(@RequestBody Map<String, Timestamp> userShift, Principal principal) {

		Map<String, Timestamp> user = userShift;
		
		List<MedicineProcurement> procurement = medicineProcurementRepository.getItemExpiry(user.get("fromDate"),
				user.get("toDate"));
				
		SalesPaymentPdf salesPaymentPdf = null;

		// CreatedBy (Security)
		User userSecurity=userServiceImpl.findByUserName(principal.getName());
		String createdBy = userSecurity.getFirstName() + " " + userSecurity.getLastName();

		try {

			byte[] pdfByte = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			Document document = new Document(PageSize.A4.rotate());

			Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
			Font redFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);

			Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
			Font headFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
			PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
			HeaderFooterPageEvent event = new HeaderFooterPageEvent();
			 writer.setPageEvent(event);
			Resource fileResourcee = resourceLoader.getResource("classpath:udbhava.png");
			document.open();
			PdfPTable table = new PdfPTable(2);

			Image img = Image.getInstance(fileResourcee.getFile().getAbsolutePath());
			img.scaleAbsolute(75, 95);
			table.setWidthPercentage(105f);

			Phrase pq = new Phrase(new Chunk(img, 5, -80));

			//shantharam addr
			pq.add(new Chunk(
					""
							+ ""
							+ "\n                                                                                                                                        Email : udbhavahospital@gmail.com",
					redFont));
			PdfPCell cellp = new PdfPCell(pq);
			PdfPCell cell1 = new PdfPCell();

			// for header Bold
			PdfPTable table96 = new PdfPTable(1);
			table96.setWidths(new float[] { 5f });
			table96.setSpacingBefore(10);

			PdfPCell hcell96;
			hcell96 = new PdfPCell(new Phrase("UDBHAVA PHARMACY", headFont1));
			hcell96.setBorder(Rectangle.NO_BORDER);
			hcell96.setHorizontalAlignment(Element.ALIGN_CENTER);
			hcell96.setPaddingLeft(25f);

			table96.addCell(hcell96);
			cell1.addElement(table96);

			PdfPTable table961 = new PdfPTable(1);
			table961.setWidths(new float[] { 5f });
			table961.setSpacingBefore(10);

			PdfPCell hcell71;
			hcell71 = new PdfPCell(new Phrase("UDBHAVA HOSPITALS", headFont1));
			hcell71.setBorder(Rectangle.NO_BORDER);
			hcell71.setHorizontalAlignment(Element.ALIGN_CENTER);
			hcell71.setPaddingLeft(25f);

			table961.addCell(hcell71);
			cell1.addElement(table961);
			
			cell1.addElement(pq);
			cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell1.setColspan(2);
			table.addCell(cell1);

			PdfPCell cell19 = new PdfPCell();

			PdfPTable table21 = new PdfPTable(1);
			table21.setWidths(new float[] { 4f });
			table21.setSpacingBefore(10);

			PdfPCell hcell19;
			hcell19 = new PdfPCell(new Phrase("Item Expiry Status", headFont1));
			hcell19.setBorder(Rectangle.NO_BORDER);
			hcell19.setHorizontalAlignment(Element.ALIGN_CENTER);
			table21.addCell(hcell19);

			// calender format date

			String from = user.get("fromDate").toString();
			Timestamp timestamp = Timestamp.valueOf(from);
			DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timestamp.getTime());
			String from1 = dateFormat.format(calendar.getTime());

			String to = user.get("toDate").toString();
			Timestamp timestamp1 = Timestamp.valueOf(to);
			DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa ");
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTimeInMillis(timestamp1.getTime());
			String to1 = dateFormat1.format(calendar1.getTime());
			
			// Display a date in day, month, year format
			Date date = Calendar.getInstance().getTime();
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
			String today = formatter.format(date).toString();

			PdfPCell hcell20;
			hcell20 = new PdfPCell(new Phrase("From Dt  : " + from1 + "           " + "Till Dt  : " + to1, redFont));
			hcell20.setBorder(Rectangle.NO_BORDER);
			hcell20.setHorizontalAlignment(Element.ALIGN_CENTER);
			table21.addCell(hcell20);

			PdfPTable table3 = new PdfPTable(2);
			table3.setWidths(new float[] { 5f, 4f });
			table3.setSpacingBefore(10);

			PdfPCell hcell111;
			hcell111 = new PdfPCell(new Phrase("Printed By          :  " + createdBy, redFont));
			hcell111.setBorder(Rectangle.NO_BORDER);
			hcell111.setPaddingLeft(-70f);

			table3.addCell(hcell111);

			hcell111 = new PdfPCell(new Phrase("Printed Date & Time    :  " + today, redFont));
			hcell111.setBorder(Rectangle.NO_BORDER);
			hcell111.setPaddingRight(-70f);
			hcell111.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table3.addCell(hcell111);

			cell19.setFixedHeight(60f);
			cell19.setColspan(2);
			cell19.addElement(table21);
			cell19.addElement(table3);
			table.addCell(cell19);

			PdfPCell cell112 = new PdfPCell();

			Font h3 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

			PdfPTable table01 = new PdfPTable(10);
			table01.setWidths(new float[] { 1.5f,4f, 6f,4f, 5f, 4f, 4f, 5f, 4f, 5f });
			table01.setSpacingBefore(10);

			PdfPCell hcell01;
			hcell01 = new PdfPCell(new Phrase("S.No", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Item Cd", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Item Name", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Batch #", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Expiry Dt", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Purc Rate", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Sale Rate", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Purc Val", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table01.addCell(hcell01);

			hcell01 = new PdfPCell(new Phrase("Remaining Stock", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_RIGHT);
			hcell01.setPaddingBottom(5f);
			table01.addCell(hcell01);
			
			hcell01 = new PdfPCell(new Phrase("Vendor Name", redFont1));
			hcell01.setBorder(Rectangle.NO_BORDER);
			hcell01.setBackgroundColor(BaseColor.LIGHT_GRAY);
			hcell01.setHorizontalAlignment(Element.ALIGN_LEFT);
			hcell01.setPaddingBottom(5f);
			table01.addCell(hcell01);
		
			List<Map<String,String>> info=new ArrayList<>();
			Map<String,String> medInfo=new HashMap<>();
			
			List<Map<String,String>> infoDisplay=new ArrayList<>();
			Map<String,String> displayInfo=null;
			
			
			List<String> show=new ArrayList<>();
			
			int count1 = 0;
			for(MedicineProcurement procurementList:procurement)
			{
				String batch=procurementList.getBatch();
				String medName=procurementList.getItemName();
				String medId=procurementList.getMedicineProcurmentMedicineDetails().getMedicineId();
				long pQty=0;
				float pMrp=0;
				float pCostPrice=0;
				String expDt = null;
				long sQty =0;
				long remainingStock=0;
				float purcVal = 0;
				String vName = null;
				displayInfo=new HashMap<>();
				
				if(!show.contains(batch+medName))
				{
					List<MedicineProcurement> medicineProcurements=medicineProcurementRepository.findByBatchAndMedicine(batch, medId);
					
					for(MedicineProcurement medicineProcurementsInfo:medicineProcurements)
					{
						pQty+=medicineProcurementsInfo.getQuantity();
						pMrp=medicineProcurementsInfo.getMrp();
						pCostPrice=medicineProcurementsInfo.getCostPrice();
						expDt=medicineProcurementsInfo.getExpDate().substring(0,10);
						
						displayInfo.put("medId", medId);
						displayInfo.put("med", medName);
						displayInfo.put("expDt", expDt);
						displayInfo.put("qty",String.valueOf( pQty));
						displayInfo.put("mrp",String.valueOf(  pMrp));
						displayInfo.put("cp",String.valueOf( pCostPrice));
						infoDisplay.add(displayInfo);
						medInfo.put("med",medName);
						medInfo.put("batch",batch);
						info.add(medInfo);
							
					}
					show.add(batch+medName);
					
					List<Sales> sales = salesRepository.findExpiryDetails(user.get("fromDate"), user.get("toDate"),
							batch,medId);
					for(Sales sale : sales)
					{
						sQty+=sale.getQuantity();
					}
					
					Vendors vendor = procurementList.getMedicineProcurmentVendors();
					vName = vendor.getVendorName();
					
					remainingStock=(pQty-sQty);
					purcVal=(pCostPrice*remainingStock);

					PdfPCell cell;
					cell = new PdfPCell(new Phrase(String.valueOf(count1 = count1 + 1), h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table01.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(medId), h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table01.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(medName), h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table01.addCell(cell);
				
					cell = new PdfPCell(new Phrase(String.valueOf(batch), h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table01.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(expDt), h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table01.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(pCostPrice), h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table01.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(pMrp), h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table01.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(purcVal), h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table01.addCell(cell);

					cell = new PdfPCell(new Phrase(String.valueOf(remainingStock), h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table01.addCell(cell);

					cell = new PdfPCell(new Phrase(vName, h3));
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					table01.addCell(cell);

				}
				
			}

			cell112.setColspan(2);
			table01.setWidthPercentage(100f);
			cell112.addElement(table01);
			table.addCell(cell112);

			document.add(table);

			document.close();

			System.out.println("finished");
			pdfByte = byteArrayOutputStream.toByteArray();
			String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/sales/viewFile/")
					.path(salesPaymentPdfServiceImpl.getNextId()).toUriString();

			salesPaymentPdf = new SalesPaymentPdf();
			salesPaymentPdf.setFileName("Item Expiry Status");
			salesPaymentPdf.setFileuri(uri);
			salesPaymentPdf.setPid(salesPaymentPdfServiceImpl.getNextId());
			salesPaymentPdf.setData(pdfByte);
			//System.out.println(drugType);

			salesPaymentPdfServiceImpl.save(salesPaymentPdf);

		} catch (Exception e) {
			Logger.error(e.getMessage());
		}

		return salesPaymentPdf;

		
	}
	
	
	@RequestMapping("/getProcured/{id}")
	public List<MedicineProcurement> getProcured(@PathVariable String id)
	{
		
		 return  medicineProcurementRepository.findOneApproved(medicineDetailsServiceImpl.findByMedicineId(id));
	}
	
}
