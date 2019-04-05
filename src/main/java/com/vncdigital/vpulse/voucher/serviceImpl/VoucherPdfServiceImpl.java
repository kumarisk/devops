package com.vncdigital.vpulse.voucher.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vncdigital.vpulse.voucher.model.VoucherPdf;
import com.vncdigital.vpulse.voucher.repository.VoucherPdfRepository;
import com.vncdigital.vpulse.voucher.service.VoucherPdfService;

@Service
public class VoucherPdfServiceImpl implements VoucherPdfService{

	@Autowired
	VoucherPdfRepository voucherPdfRepository;

	@Override
	public void save(VoucherPdf pdf) {
		voucherPdfRepository.save(pdf);
		
	}

	public VoucherPdf findById(String id) {
		
		
		return voucherPdfRepository.findByVid(id);
	}
	
	public String getNextPdfId() 
	{
		VoucherPdf voucherPdf=voucherPdfRepository.findFirstByOrderByVidDesc();
		String nextId=null;
		if(voucherPdf==null)
		{
			nextId="PDF0000001";
		}
		else
		{
			String lastUmr=voucherPdf.getVid();
			
			int umrIntId=Integer.parseInt(nextId=lastUmr.substring(3));
			umrIntId+=1;
			nextId="PDF"+String.format("%07d",umrIntId );
		}
		return nextId;
	}
	
	public VoucherPdf getVoucherPdf(String paymentNo) {
		return voucherPdfRepository.getVoucherPdf(paymentNo);
	}

	
	

}
