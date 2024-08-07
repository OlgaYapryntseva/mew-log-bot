package com.mewlog.service.buttoncrafter;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface ButtonCrafterService {
	
	InlineKeyboardMarkup showLogOptions(long chatId);
	
	InlineKeyboardMarkup createInlineKeyboard(long chatId);
	
	InlineKeyboardMarkup showReportTypeOptions(long chatId);
	
	InlineKeyboardMarkup showReportOptions(long chatId);
}
