package com.mewlog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.mewlog.configuration.BotConfig;
import com.mewlog.enums.MenuText;
import com.mewlog.repository.AnimalRepository;
import com.mewlog.repository.OwnerRepository;
import com.mewlog.repository.model.Animal;
import com.mewlog.repository.model.Logs;
import com.mewlog.repository.model.Owner;
import com.mewlog.service.buttoncrafter.ButtonCrafterService;
import com.mewlog.service.dto.AnimalDto;
import com.mewlog.service.invitation.InvitationService;
import com.mewlog.service.reports.ReportsService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class TelegramBot extends TelegramLongPollingBot {
	
	@Autowired
	OwnerRepository ownerRepository;

	@Autowired
	AnimalRepository animalRepository;

	@Autowired
	InvitationService invitationService;

	@Autowired
	ButtonCrafterService buttonCrafterService;

	@Autowired
	ReportsService reportsService;
	
	final BotConfig config;
	final Map<String, BiConsumer<Long, String>> commandActions = new HashMap<>();
	final Map<Long, Boolean> waitingForAnimalName = new HashMap<>();
	final Map<Long, Boolean> waitingForLogList = new HashMap<>();
	final Map<Long, Boolean> waitingForLogPrivate = new HashMap<>();
	final Map<Long, Boolean> waitingForKeyword = new HashMap<>();
	final List<AnimalDto> listAnimal = new ArrayList<>();
	AnimalDto selectedAnimal = null;
	final Map<Long, String> keyword = new HashMap<>();
	final Map<Long, String> logOptions = new HashMap<>();	
	final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

	@SuppressWarnings("deprecation")
	public TelegramBot(BotConfig config) {
		this.config = config;
		try {
			this.execute(new SetMyCommands(config.getListBotCommand(), new BotCommandScopeDefault(), null));
		} catch (TelegramApiException e) {
			logger.error("Error setting bot's command list: " + e.getMessage());
		}
		commandActions.put("/start", (id, name) -> startCommandReceived(id, name));
		commandActions.put("/option", (id, name) -> optionCommandReceived(id));
		commandActions.put("/invitation", (id, name) -> invitationCommandReceived(id));
		commandActions.put("/report", (id, name) -> reportCommandReceived(id));
		commandActions.put("/cancel", (id, name) -> cancelCommandReceived(id));
		commandActions.put("/about", (id, name) -> aboutCommandReceived(id));
		commandActions.put("/help", (id, name) -> helpCommandReceived(id));	
	}

	@Override
	public String getBotToken() {
		return config.getBotToken();
	}

	@Override
	public String getBotUsername() {
		return config.getBotUserName();
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();		
System.out.println("message = " + messageText);	
			if (waitingForAnimalName.getOrDefault(chatId, false)) {
				if(!messageText.trim().toLowerCase().startsWith("mewlogbot=")) {
				handleAnimalNameInput(chatId, update, messageText);
				return;
				}
			}

			if (waitingForLogList.getOrDefault(chatId, false) || waitingForLogPrivate.getOrDefault(chatId, false)) {
				if (config.containsCommandKey(messageText) || config.containsOptionKey(messageText)) {
					waitingForLogList.put(chatId, false);
					waitingForLogPrivate.put(chatId, false);
				} else {
					handleLogEntryInput(chatId, messageText);
					return;
				}
			}

			if (waitingForKeyword.getOrDefault(chatId, false)) {
				keyword.put(chatId, messageText);
				waitingForKeyword.put(chatId, false);
				handleReportTypePeriod(chatId);
				return;
			}

			if (messageText.trim().toLowerCase().startsWith("mewlogbot=")) {
System.out.println("mewlogbot=");
		        waitingForAnimalName.put(chatId, false);
				String token = invitationService.extractToken(messageText);
				if (token != null) {
					sendMessage(chatId, invitationService.handleInvitation(token, chatId));
					return;
				} else {
					sendMessage(chatId, "Неверный формат ссылки");
					return;
				}
			}
			
			BiConsumer<Long, String> action = commandActions.getOrDefault(messageText,
					(id, name) -> sendMessage(id, MenuText.DEVELOPING.getKey()));
			action.accept(chatId, update.getMessage().getChat().getFirstName());

		} else if (update.hasCallbackQuery()) {
			String callbackData = update.getCallbackQuery().getData();
System.out.println("call = " + callbackData);
			long chatId = update.getCallbackQuery().getMessage().getChatId();
			if ("add animal".equals(callbackData)) {
				addAnimalCommandReceived(chatId);
			} else if ("Поменять питомца".equals(callbackData)) {
				selectAnimal(chatId);
			} else if ("add log".equals(callbackData)) {	
				showLogOptions(chatId);
            } else if (containsAnimalId(callbackData)) {
				listAnimal.forEach(a -> {if(a.getAnimalId().equals(callbackData)) selectedAnimal = a;});
				optionCommandReceived(chatId);
			} else if ("Пользовательское сообщение".equals(callbackData)) {
				addLogPrivate(chatId);
			} else if (waitingForLogList.getOrDefault(chatId, false)) {
				handleLogEntryInput(chatId, callbackData);
			} else if (callbackData.contains("отчет за период")) {
				keyword.clear();
				handleReportTypePeriod(chatId);
			} else if (callbackData.contains("отчет по ключевому слову")) {
				addKeyword(chatId);
			} else if ("день".equals(callbackData) || "месяц".equals(callbackData) || "год".equals(callbackData)) {				
				handleReportPeriod(chatId, callbackData);
			} else {
				BiConsumer<Long, String> action = commandActions.getOrDefault(callbackData,
						(id, name) -> sendMessage(id, MenuText.DEVELOPING.getKey()));
				action.accept(chatId, "");
			}
		}
	}
	
// START	
	private void startCommandReceived(long chatId, String name) {
		listAnimal.clear();
		selectedAnimal = null;
		waitingForAnimalName.remove(chatId);
		waitingForLogList.remove(chatId);
		waitingForLogPrivate.remove(chatId);
		waitingForKeyword.remove(chatId);
		keyword.clear();
		logOptions.remove(chatId);		
		sendMessage(chatId, MenuText.START_HELLO.getFormattedText(name));
		optionCommandReceived(chatId);	
	}
	
// ABOUT
	private void aboutCommandReceived(long chatId) {
		sendMessage(chatId, MenuText.ABOUT_TEXT.getKey());
	}

// HELP
	private void helpCommandReceived(long chatId) {
		sendMessage(chatId, MenuText.HELP_TEXT.getKey());
	}

//  -------------------------- OPTIONS -------------------------------------------------------	
	
	private void optionCommandReceived(long chatId) {
		if (hasAnimal(chatId)) {
			sendMessage(chatId, MenuText.OPTION_LIST.getFormattedText(selectedAnimal.getAnimalName()));
		}
	}
	
// ADD ANIMAL
	private void addAnimalCommandReceived(long chatId) {
		sendMessage(chatId, MenuText.PET_ADD_NAME.getKey());
		waitingForAnimalName.put(chatId, true);
	}

	private void handleAnimalNameInput(long chatId, Update update, String animalName) {
		if (waitingForAnimalName.containsKey(chatId)) {
			if (!checkNameAndNotify(chatId, animalName)) {
				Animal animal = new Animal();
				animal.setAnimalName(animalName);
				animal.addOwnerId(chatId);
				animalRepository.save(animal);
				Owner owner = new Owner(chatId, update.getMessage().getChat().getFirstName());
				owner.addAnimal(animal.getAnimalId());
				ownerRepository.save(owner);
				waitingForAnimalName.remove(chatId);
				AnimalDto animalDto = new AnimalDto(animal.getAnimalId().toString(), animal.getAnimalName());
				listAnimal.add(animalDto);
				selectedAnimal = animalDto;
				sendMessage(chatId, MenuText.PET_IN_BASE.getFormattedText(animalDto.getAnimalName()));
				optionCommandReceived(chatId);
			}
			waitingForAnimalName.remove(chatId);
		}
	}

// 	INVITATION
	private void invitationCommandReceived(long chatId) {
		hasAnimal(chatId);
		String inviteLink = invitationService.generateInvitationLink(new ObjectId(selectedAnimal.getAnimalId()));
		sendMessage(chatId, MenuText.INVITATION_TEXT_1.getFormattedText(selectedAnimal.getAnimalName()));
		sendMessage(chatId, MenuText.INVITATION_TEXT_2.getFormattedText(config.getBotLink()));
		sendMessage(chatId, inviteLink);
	}

//  ADD LOG
	private void showLogOptions(long chatId) {
		sendMessage(chatId, MenuText.LOG_LIST.getFormattedText(selectedAnimal.getAnimalName()), buttonCrafterService.showLogOptions(chatId));
		waitingForLogList.put(chatId, true);
	}
	
	private void addLogPrivate(long chatId) {
		sendMessage(chatId, MenuText.USER_TEXT.getKey());
		waitingForLogPrivate.put(chatId, true);
	}
		
	private void handleLogEntryInput(long chatId, String logEntry) {
		if ((waitingForLogList.containsKey(chatId) && logEntry != null) || (waitingForLogPrivate.containsKey(chatId) && logEntry != null)) {	
			Animal animal = animalRepository.findById(new ObjectId(selectedAnimal.getAnimalId())).get();
			if (animal != null) {
				Logs log = new Logs();
				log.setMessage(logEntry);
				animal.addLog(log);
				animalRepository.save(animal);
				waitingForLogList.remove(chatId);
				waitingForLogPrivate.remove(chatId);
				logOptions.remove(chatId);
				sendMessage(chatId, logEntry);
				sendMessage(chatId, MenuText.ADD_LOG.getFormattedText(selectedAnimal.getAnimalName()));
			} else {
				sendMessage(chatId, MenuText.ADD_ANIMAL.getKey());
			}
		}
	}

//  REPORT
	private void reportCommandReceived(long chatId) {
		hasAnimal(chatId);
		InlineKeyboardMarkup showReportType = buttonCrafterService.showReportTypeOptions(chatId);
		sendMessage(chatId, MenuText.REPORT_PERIOD.getKey(), showReportType);
	}
	
	private void addKeyword(long chatId) {
		keyword.remove(chatId);
		sendMessage(chatId, MenuText.USER_TEXT.getKey());
		waitingForKeyword.put(chatId, true);		
	}

	private void handleReportTypePeriod(long chatId) {
		sendMessage(chatId, MenuText.REPORT_PERIOD.getKey());
	}
	
	private void handleReportPeriod(long chatId, String callbackData) {
		String data = null;
		if (keyword.get(chatId) != null && !keyword.get(chatId).isEmpty()) {
			data = reportsService.fingMessageByKeyAndPeriod(chatId, callbackData, keyword.get(chatId),
					selectedAnimal.getAnimalId());
		} else {
			data = reportsService.fingMessageByPeriod(chatId, callbackData, selectedAnimal.getAnimalId());
		}
		sendMessage(chatId, data.isEmpty() ? MenuText.REPORT_EMPTY.getKey()
				: MenuText.REPORT_TITLE.getFormattedText(selectedAnimal.getAnimalName(), callbackData, data));
	}
	
// CANCEL
	private void cancelCommandReceived(long chatId) {
		listAnimal.clear();
		selectedAnimal = null;
		waitingForAnimalName.remove(chatId);
		waitingForLogList.remove(chatId);
		waitingForLogPrivate.remove(chatId);
		waitingForKeyword.remove(chatId);
		keyword.clear();
		logOptions.remove(chatId);
		sendMessage(chatId, MenuText.CANCEL_TEXT.getKey());
	}
	
// SEND MESSAGE
	private void sendMessage(long chatId, String textToSend) {
		SendMessage message = new SendMessage();
		message.setChatId(String.valueOf(chatId));
		message.setText(textToSend);
		message.setParseMode(ParseMode.HTML);
		
		if(textToSend.contains("эта запись в базе")) {
			message.setReplyMarkup(buttonCrafterService.showAddOptions(chatId));
		} 
		if (textToSend.contains(MenuText.REPORT_PERIOD.getKey())) {
			message.setReplyMarkup(buttonCrafterService.showReportOptions(chatId));
		}
		if (textToSend.contains("Выберете задачу ")) {	
			message.setReplyMarkup(buttonCrafterService.showBotOptions(chatId));
		}
		try {
			execute(message);
			logger.info("TelegtamBot sendMessage: chatId:{} text: {}", chatId, textToSend);
		} catch (TelegramApiException e) {
			logger.error("Error occurred while sending message: ", e);
		}
	}

	private void sendMessage(long chatId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
		SendMessage message = new SendMessage();
		message.setChatId(String.valueOf(chatId));
		message.setText(textToSend);
		message.setReplyMarkup(keyboardMarkup);
		message.setParseMode(ParseMode.HTML);
		try {
			execute(message);
			logger.info("TelegtamBot sendMessage: chatId: {} text: {}", chatId, textToSend);
		} catch (TelegramApiException e) {
			logger.error("Error occurred while sending message: ", e);
		}
	}
	
// OTHER	
	private boolean hasAnimal(long chatId) {
	    if (listAnimal.isEmpty()) {
	        List<Animal> animals = animalRepository.findByOwnersId(chatId);
	        if (animals.isEmpty()) {
	            sendMessage(chatId, MenuText.ADD_ANIMAL.getKey());
	            addAnimalCommandReceived(chatId);
	            return false;
	        } else {
	            animals.forEach(a -> listAnimal.add(new AnimalDto(a.getAnimalId().toString(), a.getAnimalName())));
	        }
	    }
	    if (selectedAnimal == null) {
	        selectAnimal(chatId);
	    }
	    return selectedAnimal != null;
	}

	private void selectAnimal(long chatId) {
	    if (listAnimal.size() == 1) {
	        selectedAnimal = new AnimalDto(listAnimal.get(0).getAnimalId(), listAnimal.get(0).getAnimalName());
	    } else {
	        sendMessage(chatId, MenuText.PET_SELECT.getKey(), buttonCrafterService.showListAnimal(chatId, listAnimal));
	    }
	}

	private Boolean checkNameAndNotify(long chatId, String nameAnimal) {
	    List<Animal> animals = animalRepository.findByOwnersId(chatId);
	    boolean exists = animals.stream()
	                            .anyMatch(a -> a.getAnimalName().equalsIgnoreCase(nameAnimal));
	    if (exists) {
	        sendMessage(chatId, MenuText.PET_EXISTS.getFormattedText(nameAnimal));
	    }
	    return exists;
	}
	
	private Boolean containsAnimalId(String message) {
		return listAnimal.stream().anyMatch(animal -> animal.getAnimalId().equals(message));
	}
}
