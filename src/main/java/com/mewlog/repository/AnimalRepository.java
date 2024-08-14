package com.mewlog.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mewlog.repository.model.Animal;
import com.mewlog.service.reminder.dto.ReminderDto;



public interface AnimalRepository extends MongoRepository<Animal, ObjectId>{

	long count();
	
	Animal findByAnimalId(ObjectId animalId);
	
	List<Animal> findByOwnersId(Long ownerId);
	
	
	@Aggregation(pipeline = {
		    "{ $unwind: '$logs' }",
		    "{ $match: { 'logs.message': '💩' } }",
		    "{ $sort: { 'logs.dateCreate': -1 } }",
		    "{ $group: { _id: '$ownersId', lastPoopDate: { $first: '$logs.dateCreate' } } }",
		    "{ $project: { _id: 0, ownersId: '$_id', lastPoopDate: '$lastPoopDate' } }"
		})
	List<ReminderDto> findLastLitterBoxVisitDate();
	
	@Aggregation(pipeline = {
		    "{ $unwind: '$logs' }",
		    "{ $match: { 'logs.message': 'Смена лотка 🚾' } }",
		    "{ $sort: { 'logs.dateCreate': -1 } }",
		    "{ $group: { _id: '$ownersId', lastPoopDate: { $first: '$logs.dateCreate' } } }",
		    "{ $project: { _id: 0, ownersId: '$_id', lastPoopDate: '$lastPoopDate' } }"
	})
	List<ReminderDto> findLastLitterBoxChangeDate();
}
