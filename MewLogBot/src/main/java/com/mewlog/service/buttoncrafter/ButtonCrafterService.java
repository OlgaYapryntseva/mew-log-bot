package com.mewlog.service.buttoncrafter;

import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import com.mewlog.service.dto.AnimalDto;


public interface ButtonCrafterService {
	
	InlineKeyboardMarkup showBotOptions(long chatId);
	
	InlineKeyboardMarkup showLogOptions(long chatId);
	
	InlineKeyboardMarkup showAddOptions(long chatId);
	
	InlineKeyboardMarkup showReportTypeOptions(long chatId);
	
	InlineKeyboardMarkup showReportOptions(long chatId);
	
	InlineKeyboardMarkup showListAnimal(long chatId, List<AnimalDto> animals);
	
}
