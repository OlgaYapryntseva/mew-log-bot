package com.mewlog.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mewlog.repository.model.Owner;

public interface OwnerRepository extends MongoRepository<Owner, Long>{

}
