package com.mewlog.service.buttoncrafter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.mewlog.repository.AnimalRepository;
import com.mewlog.repository.model.Animal;
import com.mewlog.service.dto.AnimalDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ButtonCrafterServiceImpl implements ButtonCrafterService{

	@Autowired
	AnimalRepository animalRepository;
	
	public InlineKeyboardMarkup createInlineKeyboard(long chatId) {
		List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
		List<Animal> animal = animalRepository.findByOwnersId(chatId);
		if (animal.size() == 0) {
			InlineKeyboardButton addAnimalButton = new InlineKeyboardButton("Добавить пушистого друга ➕");
			addAnimalButton.setCallbackData("add animal");
			keyboard.add(Collections.singletonList(addAnimalButton));
			return new InlineKeyboardMarkup(keyboard);
		}
		InlineKeyboardButton addLogButton = new InlineKeyboardButton("Записать кошачьи приключения ✏");
		addLogButton.setCallbackData("add log");
		keyboard.add(Collections.singletonList(addLogButton));
		return new InlineKeyboardMarkup(keyboard);
	}
	
	public InlineKeyboardMarkup createAllOptions(long chatId) {
		List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
		InlineKeyboardButton addAnimalButton = new InlineKeyboardButton("Добавить пушистика 😻");
		addAnimalButton.setCallbackData("add animal");
		
		InlineKeyboardButton addOwnerButton = new InlineKeyboardButton("Добавить ещё хозяина 🐾");
		addOwnerButton.setCallbackData("/invitation");	
		
		InlineKeyboardButton addLogButton = new InlineKeyboardButton("Записать приключения ✏");
		addLogButton.setCallbackData("add log");
		
		InlineKeyboardButton addReportButton = new InlineKeyboardButton("Создать отчет 📊");
		addReportButton.setCallbackData("/report");
		
		InlineKeyboardButton addSubButton = new InlineKeyboardButton("Поменять питомца 🔄");
		addSubButton.setCallbackData("Поменять питомца");

		List<InlineKeyboardButton> row1 = Arrays.asList(addAnimalButton, addOwnerButton);
	    List<InlineKeyboardButton> row2 = Arrays.asList(addLogButton, addReportButton);
	    List<InlineKeyboardButton> row3 = Arrays.asList(addSubButton);

		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);

		return new InlineKeyboardMarkup(keyboard);
	}

	public InlineKeyboardMarkup showLogOptions(long chatId) {
		InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

		InlineKeyboardButton catPoopedButton = new InlineKeyboardButton("Цель достигнута 💩");
		catPoopedButton.setCallbackData("💩");

		InlineKeyboardButton litterChangeButton = new InlineKeyboardButton("Смена лотка 🚾");
		litterChangeButton.setCallbackData("Смена лотка 🚾");

		InlineKeyboardButton foodChangeButton = new InlineKeyboardButton("Миска обновлена 🥩");
		foodChangeButton.setCallbackData("Миска обновлена 🥩");

		InlineKeyboardButton waterChangeButton = new InlineKeyboardButton("Водичка обновлена 💦");
		waterChangeButton.setCallbackData("Водичка обновлена 💦");

		InlineKeyboardButton customLogButton = new InlineKeyboardButton("Ваше наблюдение 🧐");
		customLogButton.setCallbackData("Пользовательское сообщение");
		
		List<InlineKeyboardButton> row1 = Arrays.asList(catPoopedButton, litterChangeButton);
	    List<InlineKeyboardButton> row2 = Arrays.asList(foodChangeButton, waterChangeButton);
	    List<InlineKeyboardButton> row3 = Arrays.asList(customLogButton);

		List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		keyboardMarkup.setKeyboard(keyboard);		
		return keyboardMarkup;
	}

	@Override
	public InlineKeyboardMarkup showReportTypeOptions(long chatId) {
		List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
		InlineKeyboardButton addPeriodButton = new InlineKeyboardButton("Котоотчет за период");
		InlineKeyboardButton addKeywordButton = new InlineKeyboardButton("Котоключ за период");
		addPeriodButton.setCallbackData("отчет за период");
		addKeywordButton.setCallbackData("отчет по ключевому слову");
		List<InlineKeyboardButton> row = Arrays.asList(addPeriodButton, addKeywordButton);
		keyboard.add(row);
		return new InlineKeyboardMarkup(keyboard);
	}
	
	@Override
	public InlineKeyboardMarkup showReportOptions(long chatId) {
		List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
		InlineKeyboardButton addDayButton = new InlineKeyboardButton("День");
		InlineKeyboardButton addMonthButton = new InlineKeyboardButton("Месяц");
		InlineKeyboardButton addYearButton = new InlineKeyboardButton("Год");
		addDayButton.setCallbackData("день");
		addMonthButton.setCallbackData("месяц");
		addYearButton.setCallbackData("год");
		List<InlineKeyboardButton> row = Arrays.asList(addDayButton, addMonthButton, addYearButton);
		keyboard.add(row);
		return new InlineKeyboardMarkup(keyboard);
	}
	
	@Override
	public InlineKeyboardMarkup showListAnimal(long chatId, List<AnimalDto> animals) {
		List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
		animals.forEach(animal -> {
			InlineKeyboardButton button = new InlineKeyboardButton(animal.getAnimalName());
			button.setCallbackData(animal.getAnimalId());
			List<InlineKeyboardButton> row = Arrays.asList(button);
			keyboard.add(row);
		});
		return new InlineKeyboardMarkup(keyboard);
	}
}
