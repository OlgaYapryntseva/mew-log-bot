package com.mewlog.service.sendmessage;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;


public interface SendMessageService {

	void sendMessage(long chatId, String textToSend);
	
	void sendMessage(long chatId, String textToSend, InlineKeyboardMarkup keyboardMarkup);
	
	void sendReminderMessage(long chatId, String textToSend);
}
