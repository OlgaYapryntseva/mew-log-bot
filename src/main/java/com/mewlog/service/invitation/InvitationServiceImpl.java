package com.mewlog.service.invitation;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mewlog.enums.MenuText;
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
	
	@Override
    public String generateInvitationLink(ObjectId animalId) {
        String token = UUID.randomUUID().toString();
        saveInvitationToken(animalId, token);
        return "mewlogbot=" + token;
    }

    private void saveInvitationToken(ObjectId animalId, String token) {
        Invitation invitation = new Invitation(animalId, token);
        invitationRepository.save(invitation);
    }
    
    @Override
    public String handleInvitation(String token, Long chatId) {
    	System.out.println("Mew = " + token);
        Invitation invitation = invitationRepository.findByToken(token);
        if (invitation != null) {
            Animal animal = animalRepository.findById(invitation.getAnimalId()).orElse(null);
            if (animal != null) {
            	if(!animal.getOwnersId().contains(chatId)) {
                animal.addOwnerId(chatId);
                animalRepository.save(animal);
                invitationRepository.delete(invitation);
                return "Мяу-куку! Вы успешно добавлены как владелец питомца <b>" + animal.getAnimalName() + "</b> 😺🎈!";
            	} else {
            		return MenuText.PET_EXISTS.getFormattedText(animal.getAnimalName());
            	}
            }else {
                return "Питомец не найден.";
            }
        } else {
            return "Неверный токен приглашения.";
        }
    }
    
    @Override
    public String extractToken(String url) {
		Pattern pattern = Pattern.compile("mewlogbot=([^&]+)");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

}
