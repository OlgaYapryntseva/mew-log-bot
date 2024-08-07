package com.mewlog.service.invitation;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mewlog.repository.AnimalRepository;
import com.mewlog.repository.model.Animal;
import com.mewlog.service.invitation.model.Invitation;
import com.mewlog.service.invitation.repository.InvitationRepository;

@Service
public class InvitationServiceImpl implements InvitationService {
	
	@Autowired
	InvitationRepository invitationRepository;
	
	@Autowired
	AnimalRepository animalRepository;
	
    public String generateInvitationLink(String animalId) {
        String token = UUID.randomUUID().toString();
        saveInvitationToken(animalId, token);
        return "token=" + token;
    }

    private void saveInvitationToken(String animalId, String token) {
        Invitation invitation = new Invitation(animalId, token);
        invitationRepository.save(invitation);
    }
    
    public String handleInvitation(String token, Long chatId) {
    	System.out.println("token = " + token);
        Invitation invitation = invitationRepository.findByToken(token);
        if (invitation != null) {
            Animal animal = animalRepository.findById(invitation.getAnimalId()).orElse(null);
            if (animal != null) {
                animal.addOwnerId(chatId);
                animalRepository.save(animal);
                return "Мяу-куку! Вы успешно добавлены как владелец кота " + animal.getAnimalName() + " 😺🎈!";
            }else {
                return "Питомец не найден.";
            }
        } else {
            return "Неверный токен приглашения.";
        }
    }

}
