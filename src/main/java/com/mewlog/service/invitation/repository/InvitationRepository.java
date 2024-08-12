package com.mewlog.service.invitation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mewlog.service.invitation.model.Invitation;

public interface InvitationRepository extends MongoRepository<Invitation, String>{

	Invitation findByToken(String token);
	
}
