package com.mewlog.repository.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Document(collection = "animals")
public class Animal {
	@Id
	ObjectId animalId;
    String animalName;
    Set<Long> ownersId;
    List<Logs> logs;
    LocalDateTime dateCreate;
    
    
	public Animal() {
		this.ownersId = new HashSet<>();
		this.logs = new ArrayList<>();
		this.dateCreate = LocalDateTime.now();
	}
	
	public void addOwnerId(Long ownerId) {
		if(!ownersId.contains(ownerId))
			ownersId.add(ownerId);
	}
	
	public void addLog(Logs log) {
		logs.add(log);
	}
}
