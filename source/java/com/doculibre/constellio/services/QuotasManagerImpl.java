package com.doculibre.constellio.services;


public class QuotasManagerImpl implements QuotasManager{

	//@Value("${quotas.indexedrecords}")
	private double quotaIndexedRecords;
	
	//@Value("${quotas.sizedisk}")
	private double quotaSizeDisk;
	
	//@Value("${quotas.percentage}")
	private double percentage;
	
	
	@Override
	public void setQuotaIndexedRecords(int quotaIndexedRecords) {
		this.quotaIndexedRecords=quotaIndexedRecords;
	}

	@Override
	public double getQuotaIndexedRecords() {
		return quotaIndexedRecords;
	}

	@Override
	public void setQuotaSizeDisk(int quotaSizeDisk) {
		this.quotaSizeDisk=quotaSizeDisk;
	}

	@Override
	public double getQuotaSizeDisk() {
		return quotaSizeDisk;
	}
	
	public double getQuotaPercentage(){
		return percentage;
	}
	
	public void setQuotaPercentage(int percentage){
		this.percentage=percentage;
	}

}
