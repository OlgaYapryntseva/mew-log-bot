package com.mewlog.repository.model;

import java.time.LocalDateTime;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import lombok.Data;

@Data
public class Logs {
	@Id
	ObjectId logId;
	String message;
	LocalDateTime dateCreate;

	public Logs() {
		this.logId = new ObjectId();
		this.dateCreate = LocalDateTime.now();
	}

}
