package com.mewlog.service.sendmessage;



public interface SendMessageService {
	
	void sendReminderMessage(long chatId, String textToSend);
}
