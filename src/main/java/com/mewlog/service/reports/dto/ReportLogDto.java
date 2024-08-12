package com.mewlog.service.reports.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportLogDto {
	String dateCreate;
	String message;

	public ReportLogDto(LocalDateTime local, String message) {
		this.dateCreate = local.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		this.message = message;
	}

}
