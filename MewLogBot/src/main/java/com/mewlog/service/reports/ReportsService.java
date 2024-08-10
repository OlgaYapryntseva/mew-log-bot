package com.mewlog.service.reports;


public interface ReportsService {
	
	String fingMessageByPeriod(long chatId, String callbackData, String animalId);
	
	String fingMessageByKeyAndPeriod(long chatId, String callbackData, String keyword, String animalId);

}
