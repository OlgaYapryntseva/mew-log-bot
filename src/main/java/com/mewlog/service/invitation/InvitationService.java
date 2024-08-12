package com.mewlog.service.invitation;

import org.bson.types.ObjectId;

public interface InvitationService {
	
	String generateInvitationLink(ObjectId animalId);
	
	String handleInvitation(String token, Long chatId);
	
	String extractToken(String url);
	
}
