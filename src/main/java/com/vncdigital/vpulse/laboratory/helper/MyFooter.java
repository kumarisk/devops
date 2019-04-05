package com.vncdigital.vpulse.laboratory.helper;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.vncdigital.vpulse.user.model.User;
import com.vncdigital.vpulse.user.serviceImpl.UserServiceImpl;

public class MyFooter extends PdfPageEventHelper{

	Principal principal;
	
	UserServiceImpl userServiceImpl;
	
	
	
	
	public MyFooter(Principal p,	UserServiceImpl u) {
		principal=p;
		userServiceImpl=u;
	}


	final Font font2 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
	Font ffont = new Font(Font.FontFamily.UNDEFINED, 20, Font.ITALIC);
	Font font = new Font(Font.FontFamily.UNDEFINED, 12, Font.NORMAL);
	Font font1 = new Font(Font.FontFamily.UNDEFINED, 12, Font.NORMAL, BaseColor.RED);

	
	public void onEndPage(PdfWriter writer, Document document   ) {
		
		// createdBy (Security)
		User userSecurity = userServiceImpl.findByUserName(principal.getName());
		String createdBy=null;
		
		String mn=null;
		mn=userSecurity.getMiddleName();
		if(mn==null)
		{
			createdBy = userSecurity.getFirstName() +" "+ userSecurity.getLastName();
		}
		else
		{
			createdBy = userSecurity.getFirstName() + " "+userSecurity.getMiddleName() +" "+ userSecurity.getLastName();
			
		}
		
		// Display a date in day, month, year format
	  Date date = Calendar.getInstance().getTime();
	  SimpleDateFormat	formatter = new SimpleDateFormat("dd-MMM-yyyy hh.mm aa");
		String today = formatter.format(date).toString();

		  PdfContentByte cb = writer.getDirectContent();
		  Font font = new Font(Font.FontFamily.UNDEFINED, 10, Font.NORMAL);
		
			//shantharam addr
		  Phrase footer = new Phrase("", font);
			Phrase p2 = new Phrase("", font);
			Phrase p6 = new Phrase("", font);

			Phrase p3 = new Phrase("", font);
			Phrase p4 = new Phrase("");
			Phrase p5 = new Phrase("", font1);	
			
			
			
			
			Phrase p7 = new Phrase("Printed By : " +createdBy+ "                                                                   "+"Printed Date : "+today,font2);
			
			
			
			 ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
		              p7,
		              (document.right() - document.left()) / 2 + document.leftMargin(),
		              document.bottom() +70, 0);
		 ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
				 p5,
	              (document.right() - document.left()) / 2 + document.leftMargin(),
	              document.bottom() +30, 0);
		 
		 ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
	              p4,
	              (document.right() - document.left()) / 2 + document.leftMargin(),
	              document.bottom() +50, 0);
		
      ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
              footer,
              (document.right() - document.left()) / 2 + document.leftMargin(),
              document.bottom() +15, 0);
      
      
      ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
              p2,
              (document.right() - document.left()) / 2 + document.leftMargin(),
              document.bottom() +1, 0);
      
      ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
              p3,
              (document.right() - document.left()) / 2 + document.leftMargin(),
              document.bottom() -24, 0);
      
      ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
              p6,
              (document.right() - document.left()) / 2 + document.leftMargin(),
              document.bottom() -12, 0);
      
		
	}

}