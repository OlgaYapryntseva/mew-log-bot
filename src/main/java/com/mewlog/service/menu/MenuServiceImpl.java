package com.mewlog.service.menu;
/*
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mewlog.repository.AnimalRepository;
import com.mewlog.service.invitation.InvitationService;

@Service
public class MenuServiceImpl implements MenuService {
	long chatId;
	String firstName;
	Map<String, Runnable> actionMap = new HashMap<>();

	@Autowired
	AnimalRepository animalRepository;

	@Autowired
	InvitationService invitationService;

	public MenuServiceImpl() {
		actionMap.put("/start", this::startCommandReceived);
		actionMap.put("/invitation", () -> invitationService.invitationCommandReceived(chatId));
		actionMap.put("/report", this::reportCommandReceived);
	}

	@Override
	public Runnable getAction(String action, long chatId, String firstName) {
		this.chatId = chatId;
		this.firstName = firstName;
		return actionMap.getOrDefault(action, this::defaultAction);

	}

	private void startCommandReceived() {
//		String answer = "<b><i>Мяу, " + name + "! 🐾 </i></b> \nРад видеть тебя здесь! 😺";
//		sendMessage(chatId, answer);
		System.out.println("Start command received for chat: " + chatId + ", user: " + firstName);
	}

//	private void invitationCommandReceived() {
//		Animal animal = animalRepository.findByOwnersId(chatId);
//		String inviteLink = invitationService.generateInvitationLink(animal.getAnimalId());
//		sendMessage(chatId, "Щелкни по ссылке: " + config.getBotLink() + "\n" + "и отправь боту этот текст: \n");
//		sendMessage(chatId, inviteLink);
//		invitationService.invitationCommandReceived(chatId);
//		System.out.println("Start command received for chat: " + chatId + ", user: " + firstName);
//
//	}

	private void reportCommandReceived() {
//		InlineKeyboardMarkup showReportType = buttonCrafterService.showReportTypeOptions(chatId);
//		sendMessage(chatId, MenuText.REPORT_PERIOD.getKey(), showReportType);
		System.out.println("Report command received for chat: " + chatId);
	}

	private void defaultAction() {
		// Действие по умолчанию, если команда не найдена
		System.out.println("Unknown command for chat: " + chatId);
	}
}
*/