package com.mewlog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.mewlog.configuration.BotConfig;
import com.mewlog.repository.AnimalRepository;
import com.mewlog.repository.OwnerRepository;
import com.mewlog.repository.model.Animal;
import com.mewlog.repository.model.Logs;
import com.mewlog.repository.model.Owner;
import com.mewlog.service.buttoncrafter.ButtonCrafterService;
import com.mewlog.service.invitation.InvitationService;
import com.mewlog.service.reports.ReportsService;
import com.mewlog.service.reports.dto.ReportLogDto;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

//	final String LINK = "https://t.me/MewLogBot";

	final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
	final Map<Long, Boolean> waitingForAnimalName = new HashMap<>();
	final Map<Long, Boolean> waitingForLogList = new HashMap<>();
	final Map<Long, Boolean> waitingForLogPrivate = new HashMap<>();
	final Map<Long, String> logOptions = new HashMap<>();
	final BotConfig config;

	@SuppressWarnings("deprecation")
	public TelegramBot(BotConfig config) {
		this.config = config;
		List<BotCommand> listBbotCommand = new ArrayList<>();
		listBbotCommand.add(new BotCommand("/start", "начать взаимодействие с ботом"));
		listBbotCommand.add(new BotCommand("/invitation", "пригласить второго хозяина"));
		listBbotCommand.add(new BotCommand("/report", "сделать отчет за месяц"));
		listBbotCommand.add(new BotCommand("/about", "узнать общую информацию о боте"));
		listBbotCommand.add(new BotCommand("/help", "получить информацию о доступных командах"));
		try {
			this.execute(new SetMyCommands(listBbotCommand, new BotCommandScopeDefault(), null));
		} catch (TelegramApiException e) {
			logger.error("Error setting bot's command list: " + e.getMessage());
		}
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

			if (waitingForAnimalName.getOrDefault(chatId, false)) {
				handleAnimalNameInput(chatId, update, messageText);
				return;
			}

			if (waitingForLogList.getOrDefault(chatId, false)) {
				handleLogEntryInput(chatId, messageText);
				return;
			}

			if (waitingForLogPrivate.getOrDefault(chatId, false)) {
				handleLogEntryInput(chatId, messageText);
				return;
			}

			if (messageText.startsWith("token=")) {
				String token = extractToken(messageText);
				if (token != null) {
					sendMessage(chatId, invitationService.handleInvitation(token, chatId));
				} else {
					sendMessage(chatId, "Неверный формат ссылки");
				}
			}
			switch (messageText) {
			case "/start":
				startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
				break;
			case "/invitation":
				invitationCommandReceived(chatId);
				break;
			case "/report":
				reportCommandReceived(chatId);
				break;
			case "/about":
				aboutCommandReceived(chatId);
				break;
			case "/help":
				helpCommandReceived(chatId);
				break;
			case "/cancel":
				cancelCommandReceived(chatId);
				break;
			default:
				sendMessage(chatId, "Извините, данная команда пока в разработке!🐾");
			}
		} else if (update.hasCallbackQuery()) {
			String callbackData = update.getCallbackQuery().getData();
			long chatId = update.getCallbackQuery().getMessage().getChatId();
			if ("add animal".equals(callbackData)) {
				addAnimalCommandReceived(chatId);
			} else if ("add log".equals(callbackData)) {
				showLogOptions(chatId);
			} else if ("Пользовательское сообщение".equals(callbackData)) {
				addLogPrivate(chatId);
			} else if (waitingForLogList.getOrDefault(chatId, false)) {
				handleLogEntryInput(chatId, callbackData);
			}  else if ("день".equals(callbackData) || "месяц".equals(callbackData) || "год".equals(callbackData)) {
				handleReportPeriod(chatId, callbackData);
			} else if (callbackData.contains("отчет")) {
				handleReportTypePeriod(chatId, callbackData);
			}
		}
	}

	private void handleReportTypePeriod(long chatId, String callbackData) {
		String answer = "Выберите период для нашего кошачьего отчета! 🐾📅";
		sendMessage(chatId, answer);
	}

	private void handleReportPeriod(long chatId, String callbackData) {
		LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.with(LocalTime.MIN);
        LocalDateTime end = now.with(LocalTime.MAX);
        if(callbackData.equals("месяц")) {
            start = now.minusMonths(1).with(LocalTime.MIN);
        } else if(callbackData.equals("год")) {
            start = now.minusYears(1).with(LocalTime.MIN);
        }

		List<ReportLogDto> reportLogs = reportsService.fingMessageByPeriod(chatId, start, end);
		String data = reportLogs.stream()
                .map(r -> "\n" + r.getDateCreate() + " - " + r.getMessage())
                .collect(Collectors.joining());
		sendMessage(chatId, data.isEmpty() ? "Упс, за этот период пусто! 🐾" : "<b>Отчет за " + callbackData + ": </b>" + data);		
	}

	private void reportCommandReceived(long chatId) {
		InlineKeyboardMarkup showReportType = buttonCrafterService.showReportTypeOptions(chatId);
		String answer = "Выберите период для нашего кошачьего отчета! 🐾📅";
		sendMessage(chatId, answer, showReportType);
	}

	private void startCommandReceived(long chatId, String name) {
		String answer = "<b><i>Мяу, " + name + "! 🐾 </i></b> \nРад видеть тебя здесь! 😺";
		sendMessage(chatId, answer);
	}

	private void invitationCommandReceived(long chatId) {
		Animal animal = animalRepository.findByOwnersId(chatId);
		String inviteLink = invitationService.generateInvitationLink(animal.getAnimalId());
		sendMessage(chatId, "Щелкни по ссылке: " + config.getBotLink() + "\n" + "и отправь боту этот текст: \n");
		sendMessage(chatId, inviteLink);
	}

	private void helpCommandReceived(long chatId) {
		String answer = "Добро пожаловать в наш бот!\n\n" + "Вот список доступных команд:\n\n"
				+ "/start - Начать взаимодействие с ботом и получить приветственное сообщение.\n\n"
				+ "/invitation - Пригласить второго хозяина питомца.\n\n"
				+ "/help - Получить помощь и информацию о доступных командах.\n\n"
				+ "/about - Узнать общую информацию о боте и его возможностях.\n\n"
				+ "/cancel - Отменить текущее действие или процесс.\n\n"
				+ "/settings - Настроить параметры вашего аккаунта и предпочтения.";
		sendMessage(chatId, answer);
	}

	private void aboutCommandReceived(long chatId) {
		String answer = "Добро пожаловать в <b><i>MewLogBot</i></b>, вашего личного помощника по уходу за вашим пушистым другом! 🎉\n\n"
				+ "Следите за всеми важными событиями в жизни вашего кота: \n\n"
				+ "🐱 Отслеживание посещений туалета: Узнайте, когда ваш котик в последний раз посещал лоток.\n\n"
				+ "🧼 Напоминания о смене лотка: Никогда не забудете поменять лоток благодаря нашим удобным напоминаниям.\n\n"
				+ "🍲 Кормление: Ведите учет, когда и чем вы кормили своего любимца.\n\n"
				+ "🏋️ Активности: Записывайте важные события и достижения вашего кота!\n\n"
				+ "<b><i>MewLogBot</i></b> — потому что ваш кот заслуживает только лучшего! 💖 ";
		sendMessage(chatId, answer);
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
			sendMessage(chatId, "<b>Ваш питомец теперь с нами! 🎉</b>");
		}
	}

	private void handleLogEntryInput(long chatId, String logEntry) {
		if (waitingForLogList.containsKey(chatId) && logEntry != null) {
			Animal animal = animalRepository.findByOwnersId(chatId);
			if (animal != null) {
				Logs log = new Logs();
				log.setMessage(logEntry);
				animal.addLog(log);
				animalRepository.save(animal);
				waitingForLogList.remove(chatId);
				logOptions.remove(chatId);
				sendMessage(chatId, logEntry);
				sendMessage(chatId, "<i>Мяу - эта запись в базе! ✔</i>");
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
		sendMessage(chatId, "Мяу 🐈‍⬛ , пиши ... ");
		waitingForLogPrivate.put(chatId, true);
	}

	private void sendMessage(long chatId, String textToSend) {
		SendMessage message = new SendMessage();
		message.setChatId(String.valueOf(chatId));
		message.setText(textToSend);
		message.setParseMode(ParseMode.HTML);
		if (textToSend.contains("Мяу")) {
			message.setReplyMarkup(buttonCrafterService.createInlineKeyboard(chatId));
		}
		if (textToSend.contains("Выберите период для нашего кошачьего отчета")) {
			message.setReplyMarkup(buttonCrafterService.showReportOptions(chatId));
		}
		try {
			execute(message);
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
		} catch (TelegramApiException e) {
			logger.error("Error occurred while sending message: ", e);
		}
	}

	private void showLogOptions(long chatId) {
		sendMessage(chatId, "Выберите из списка или добавьте свое: ", buttonCrafterService.showLogOptions(chatId));
		waitingForLogList.put(chatId, true);
	}

	private String extractToken(String url) {
		Pattern pattern = Pattern.compile("token=([^&]+)");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
