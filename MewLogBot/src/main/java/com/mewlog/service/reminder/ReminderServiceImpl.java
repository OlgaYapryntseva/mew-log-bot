package com.mewlog.service.reminder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.mewlog.repository.AnimalRepository;
import com.mewlog.service.TelegramBot;
import com.mewlog.service.reminder.dto.ReminderDto;
import com.mewlog.service.sendmessage.SendMessageService;

@Service
public class ReminderServiceImpl implements ReminderService {

	@Autowired
	AnimalRepository animalRepository;
	
	@Autowired
	SendMessageService sendMessageService;
	final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	final Integer daysWithNoPoop = 1;
	final Integer daysUntilLitterChange = 1;
	final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
	
	@Override
	@Scheduled(cron = "0 0 9 * * ?", zone = "UTC")
	//@Scheduled(cron = "0 */1 * * * ?")
	public void sendReminderNoPoop() {
		 List<ReminderDto> reminderList = animalRepository.findLastLitterBoxVisitDate();
		    for (ReminderDto rem : reminderList) {
		        String reminderText = "<b><i>Мяу-сигнал 🚨: </i></b>\nПоследний кошачий сюрприз 💩: " + rem.getLastPoopDate().format(format);
		        LocalDateTime now = LocalDateTime.now();
		        for (Long ownerId : rem.getOwnersId()) {
		        	long daysBetween = ChronoUnit.DAYS.between(now, rem.getLastPoopDate());
		        	if(daysBetween >= daysWithNoPoop)
		        	sendMessageService.sendReminderMessage(ownerId, reminderText);
		        }
		    }
	}

	@Override
	@Scheduled(cron = "0 0 9 * * ?", zone = "UTC")
	//@Scheduled(cron = "0 */2 * * * ?")
	public void sendReminderLitterChange() {
		List<ReminderDto> reminderList = animalRepository.findLastLitterBoxChangeDate();
	    for (ReminderDto rem : reminderList) {
	        String reminderText = "<b><i>Мяу-сигнал 🚨: </i></b>\nПоследняя уборка лотка была : " + rem.getLastPoopDate().format(format);
	        LocalDateTime now = LocalDateTime.now();
	        for (Long ownerId : rem.getOwnersId()) {
	        	long daysBetween = ChronoUnit.DAYS.between(now, rem.getLastPoopDate());
	        	if(daysBetween >= daysUntilLitterChange)
	        	sendMessageService.sendReminderMessage(ownerId, reminderText);
	        }
	    }		
	}
	
	@Override
	@Scheduled(cron = "0 */5 * * * ?")
	public void sendServerNonStop() {
//		 long count = animalRepository.count();
//		 logger.info("sendServerNonStop count animal = {}", count);
    }
}
