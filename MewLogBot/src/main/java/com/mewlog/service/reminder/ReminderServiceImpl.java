package com.mewlog.service.reminder;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.mewlog.repository.AnimalRepository;
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
	
	@Override
	@Scheduled(cron = "0 0 9 * * ?")
	//@Scheduled(cron = "0 */1 * * * ?")
	public void sendReminderNoPoop() {
		 List<ReminderDto> reminderList = animalRepository.findLastLitterBoxVisitDate();
		    for (ReminderDto rem : reminderList) {
		        String reminderText = "<b><i>Мяу-сигнал 🚨: </i></b>\nПоследний кошачий сюрприз 💩: " + rem.getLastPoopDate().format(format);
		        for (Long ownerId : rem.getOwnersId()) {
		        	sendMessageService.sendReminderMessage(ownerId, reminderText);
		        }
		    }
	}

	@Override
	@Scheduled(cron = "0 0 9 * * ?")
	//@Scheduled(cron = "0 */2 * * * ?")
	public void sendReminderLitterChange() {
		List<ReminderDto> reminderList = animalRepository.findLastLitterBoxChangeDate();
	    for (ReminderDto rem : reminderList) {
	        String reminderText = "<b><i>Мяу-сигнал 🚨: </i></b>\nПоследняя уборка лотка была : " + rem.getLastPoopDate().format(format);
	        for (Long ownerId : rem.getOwnersId()) {
	        	sendMessageService.sendReminderMessage(ownerId, reminderText);
	        }
	    }		
	}

}
