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

	final Map<String, BiConsumer<Long, String>> commandActions = new HashMap<>();
	final Map<Long, Boolean> waitingForAnimalName = new HashMap<>();
	final Map<Long, Boolean> waitingForLogList = new HashMap<>();
	final Map<Long, Boolean> waitingForLogPrivate = new HashMap<>();
	final Map<Long, Boolean> waitingForKeyword = new HashMap<>();
	final List<AnimalDto> listAnimal = new ArrayList<>();
	AnimalDto selectedAnimal = null;
	final Map<Long, String> keyword = new HashMap<>();
	final Map<Long, String> logOptions = new HashMap<>();
	final BotConfig config;
	
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
		commandActions.put("/about", (id, name) -> aboutCommandReceived(id));
		commandActions.put("/help", (id, name) -> helpCommandReceived(id));
		commandActions.put("/cancel", (id, name) -> cancelCommandReceived(id));
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
				handleAnimalNameInput(chatId, update, messageText);
				return;
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

			if (messageText.startsWith("token=")) {
				String token = invitationService.extractToken(messageText);
				if (token != null) {
					sendMessage(chatId, invitationService.handleInvitation(token, chatId));
				} else {
					sendMessage(chatId, "Неверный формат ссылки");
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
				selectAminal(chatId);
			} else if ("add log".equals(callbackData) || containsAnimalId(callbackData)) {
				showLogOptions(chatId);
			} else if ("Пользовательское сообщение".equals(callbackData)) {
				addLogPrivate(chatId);
			} else if (waitingForLogList.getOrDefault(chatId, false)) {
				handleLogEntryInput(chatId, callbackData);
			} else if (callbackData.contains("отчет за период")) {
				keyword.remove(chatId);
				handleReportTypePeriod(chatId);
			} else if (callbackData.contains("отчет по ключевому слову")) {
				addKeyword(chatId);
			} else if ("день".equals(callbackData) || "месяц".equals(callbackData) || "год".equals(callbackData)) {
				if (keyword.get(chatId) != null && !keyword.get(chatId).isEmpty()) {
					handleReportKeyAndPeriod(chatId, callbackData, keyword.get(chatId));
				} else {
					handleReportPeriod(chatId, callbackData);
				}	
			} else {
				BiConsumer<Long, String> action = commandActions.getOrDefault(callbackData,
						(id, name) -> sendMessage(id, MenuText.DEVELOPING.getKey()));
				action.accept(chatId, "");
			}
		}
	}

	private void selectAminal(long chatId) {
		selectedAnimal = null;
		sendMessage(chatId, "Выберете питомца: ", buttonCrafterService.showListAnimal(chatId, listAnimal));	
	}

	private void optionCommandReceived(long chatId) {
		sendMessage(chatId, "Выберете задачу! 🎯");
	}
	
	private void addKeyword(long chatId) {
		sendMessage(chatId, MenuText.USER_TEXT.getKey());
		waitingForKeyword.put(chatId, true);		
	}

	private void handleReportTypePeriod(long chatId) {
		sendMessage(chatId, MenuText.REPORT_PERIOD.getKey());
	}
	
	private void handleReportPeriod(long chatId, String callbackData) {
		String data = reportsService.fingMessageByPeriod(chatId, callbackData, selectedAnimal.getAnimalId());
		sendMessage(chatId, data.isEmpty() ? 
				"Упс, за этот период пусто! 🐾" : "<b><i>" + selectedAnimal.getAnimalName() + " 💖\n</i>Отчет за " + callbackData + ": </b>" + data);
	}

	private void handleReportKeyAndPeriod(long chatId, String keyword, String callbackData ) {
		String data = reportsService.fingMessageByKeyAndPeriod(chatId, keyword, callbackData, selectedAnimal.getAnimalId());
		sendMessage(chatId, data.isEmpty() ? 
				"Упс, за этот период пусто! 🐾" :  "<b><i>" + selectedAnimal.getAnimalName() + " 💖\n</i>Отчет за " + callbackData + ": </b>" + data);
	}
	
	private void reportCommandReceived(long chatId) {
		InlineKeyboardMarkup showReportType = buttonCrafterService.showReportTypeOptions(chatId);
		sendMessage(chatId, MenuText.REPORT_PERIOD.getKey(), showReportType);
	}

	private void startCommandReceived(long chatId, String name) {
		listAnimal.clear();
		List<Animal> animals = animalRepository.findByOwnersId(chatId);
		animals.forEach(a -> listAnimal.add(new AnimalDto(a.getAnimalId().toString(), a.getAnimalName())));
		System.out.println("size = " + animals.size());
		String answer = "<b><i>Мяу, " + name + "! 🐾 </i></b> \nРад видеть тебя здесь! 😺";
		sendMessage(chatId, answer);
		if(listAnimal.size() == 1) {
			selectedAnimal = new AnimalDto(animals.get(0).getAnimalId().toString(), animals.get(0).getAnimalName());
		}
		if (listAnimal.size() > 1) {
			selectAminal(chatId);
		}
	}

	private void invitationCommandReceived(long chatId) {
		List<Animal> animal = animalRepository.findByOwnersId(chatId);
		String inviteLink = invitationService.generateInvitationLink(animal.get(0).getAnimalId());
		sendMessage(chatId, "Перешлите это сообщение другому хозяину питомца по имени " + selectedAnimal.getAnimalName()  + " 👇 \n");
		sendMessage(chatId, "Щелкни по ссылке: " + config.getBotLink() + "\n" + "и отправь боту этот текст: \n");
		sendMessage(chatId, inviteLink);
	}

	private void helpCommandReceived(long chatId) {
		sendMessage(chatId, MenuText.HELP_TEXT.getKey());
	}

	private void aboutCommandReceived(long chatId) {
		sendMessage(chatId, MenuText.ABOUT_TEXT.getKey());
	}

	private void cancelCommandReceived(long chatId) {
		waitingForAnimalName.remove(chatId);
		waitingForLogList.remove(chatId);
		waitingForLogPrivate.remove(chatId);
		logOptions.remove(chatId);
		sendMessage(chatId, "Операция отменена.");
	}

	private void handleAnimalNameInput(long chatId, Update update, String animalName) {
		if (waitingForAnimalName.containsKey(chatId)) {
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
			sendMessage(chatId, "<b>Ваш питомец <i>" + animalDto.getAnimalName() + "</i> теперь с нами! 🎉</b>");
			
			optionCommandReceived(chatId);
		}
	}

	private void handleLogEntryInput(long chatId, String logEntry) {
		System.out.println("handleLogEntryInput = " + logEntry + " ID = " + (waitingForLogList.containsKey(chatId) && logEntry != null)
				 + "  " + (waitingForLogPrivate.containsKey(chatId) && logEntry != null));
		if ((waitingForLogList.containsKey(chatId) && logEntry != null) || (waitingForLogPrivate.containsKey(chatId) && logEntry != null)) {
			System.out.println("handleLogEntryInput mess = "  + logEntry + "id = " + selectedAnimal.getAnimalId());		
			Animal animal = animalRepository.findById(new ObjectId(selectedAnimal.getAnimalId())).get();
			if (animal != null) {
				Logs log = new Logs();
				log.setMessage(logEntry);
				animal.addLog(log);
				animalRepository.save(animal);
				waitingForLogList.remove(chatId);
				logOptions.remove(chatId);
				sendMessage(chatId, logEntry);
				sendMessage(chatId, "<i>Мяу - эта запись в базе у <b>" + selectedAnimal.getAnimalName() + "</b>! ✔️</i>");
			} else {
				sendMessage(chatId, "Ваш питомец спрятался! Добавьте его, чтобы продолжить! 🙀");
			}
		}
	}

	private void addAnimalCommandReceived(long chatId) {
		sendMessage(chatId, "Как зовут вашего пушистика? ");
		waitingForAnimalName.put(chatId, true);
	}

	private void addLogPrivate(long chatId) {
		sendMessage(chatId, MenuText.USER_TEXT.getKey());
		waitingForLogPrivate.put(chatId, true);
	}

	private void showLogOptions(long chatId) {
		if(selectedAnimal == null) {
			sendMessage(chatId, "Пожалуйста, начни с команды /start");
		}
		sendMessage(chatId, "Выберите из списка или добавьте свое для <b>" + selectedAnimal.getAnimalName() + "</b>: ", buttonCrafterService.showLogOptions(chatId));
		waitingForLogList.put(chatId, true);
	}

	private void sendMessage(long chatId, String textToSend) {
		SendMessage message = new SendMessage();
		message.setChatId(String.valueOf(chatId));
		message.setText(textToSend);
		message.setParseMode(ParseMode.HTML);
		if (textToSend.contains("Мяу") && !textToSend.contains("пиши ...")) {
			if(listAnimal.size() <= 1 || textToSend.contains("запись")) {
			message.setReplyMarkup(buttonCrafterService.createInlineKeyboard(chatId));
			} 
		}
		if (textToSend.contains(MenuText.REPORT_PERIOD.getKey())) {
			message.setReplyMarkup(buttonCrafterService.showReportOptions(chatId));
		}
		if (textToSend.equals("Выберете задачу! 🎯")) {
			message.setReplyMarkup(buttonCrafterService.createAllOptions(chatId));
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

	private Boolean containsAnimalId(String message) {
		return listAnimal.stream().anyMatch(animal -> animal.getAnimalId().equals(message));
	}
}
