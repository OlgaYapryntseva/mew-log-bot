package com.mewlog.repository.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import lombok.Data;

@Data
public class Logs {
	@Id
	String logId;
	String message;
	LocalDateTime dateCreate;

	public Logs() {
		this.dateCreate = LocalDateTime.now();
	}

}
