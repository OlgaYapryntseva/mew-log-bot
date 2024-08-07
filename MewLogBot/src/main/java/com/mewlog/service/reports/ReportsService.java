package com.mewlog.service.reports;

import java.time.LocalDateTime;
import java.util.List;
import com.mewlog.service.reports.dto.ReportLogDto;


public interface ReportsService {
	
	List<ReportLogDto> fingMessageByPeriod(long chatId, LocalDateTime dateStart, LocalDateTime dateEnd);
	
	List<ReportLogDto> fingMessageByTextAndPeriod(long chatId, String text, LocalDateTime dateStart, LocalDateTime dateEnd);

}
