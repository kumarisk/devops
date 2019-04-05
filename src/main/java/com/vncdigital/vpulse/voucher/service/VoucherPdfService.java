package com.vncdigital.vpulse.voucher.service;

import com.vncdigital.vpulse.voucher.model.VoucherPdf;

public interface VoucherPdfService {

	void save(VoucherPdf pdf);

	VoucherPdf findById(String vid);

	public String getNextPdfId();

}
