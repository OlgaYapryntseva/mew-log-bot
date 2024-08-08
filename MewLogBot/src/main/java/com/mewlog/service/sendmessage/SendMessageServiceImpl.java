package com.mewlog.service.sendmessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.mewlog.service.TelegramBot;
import com.mewlog.service.buttoncrafter.ButtonCrafterService;

@Service
public class SendMessageServiceImpl implements SendMessageService {
	
	@Autowired
	TelegramBot telegramBot;
	
	@Autowired
	ButtonCrafterService buttonCrafterService;
	
	final Logger logger = LoggerFactory.getLogger(SendMessageServiceImpl.class);

	@Override
	public void sendMessage(long chatId, String textToSend) {
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
		System.out.println("sendMessage text = " + textToSend);
		try {
			telegramBot.execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error occurred while sending message: ", e);
		}
	}

	@Override
	public void sendMessage(long chatId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
		SendMessage message = new SendMessage();
		message.setChatId(String.valueOf(chatId));
		message.setText(textToSend);
		message.setReplyMarkup(keyboardMarkup);
		message.setParseMode(ParseMode.HTML);
		try {
			telegramBot.execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error occurred while sending message: ", e);
		}
	}
	
	@Override
	public void sendReminderMessage(long chatId, String textToSend) {
		SendMessage message = new SendMessage();
		message.setChatId(String.valueOf(chatId));
		message.setText(textToSend);
		message.setParseMode(ParseMode.HTML);
		try {
			telegramBot.execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error occurred while sending message: ", e);
		}
	}
}
