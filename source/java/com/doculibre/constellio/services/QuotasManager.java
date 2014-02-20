package com.doculibre.constellio.services;

public interface QuotasManager {

	public void setQuotaIndexedRecords(int quotaIndexedRecords);
	public double getQuotaIndexedRecords();
	
	public void setQuotaSizeDisk(int quotaSizeDisk);
	public double getQuotaSizeDisk();
	
	
	public double getQuotaPercentage();
	public void setQuotaPercentage(int percentage);
	
}
