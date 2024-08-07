package com.mewlog.service.invitation;


public interface InvitationService {
	
	String generateInvitationLink(String animalId);
	
	String handleInvitation(String token, Long chatId);
}
