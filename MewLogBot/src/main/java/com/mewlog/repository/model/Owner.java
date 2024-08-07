package com.mewlog.repository.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "owners")
public class Owner {
	@Id
	Long ownerId;
	String ownerName;
	@Indexed(unique = true)
	Set<String> animals;
	LocalDateTime dateRegister;
	
	public Owner(Long ownerId, String ownerName) {
		this.ownerId = ownerId;
		this.ownerName = ownerName;
		this.animals = new HashSet<>();
		this.dateRegister = LocalDateTime.now();
	}
	
	public void addAnimal(String animalId) {
		if(!animals.contains(animalId))
			animals.add(animalId);
	}

}
