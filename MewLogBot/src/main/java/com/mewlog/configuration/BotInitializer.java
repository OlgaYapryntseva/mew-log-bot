package com.mewlog.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.mewlog.service.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BotInitializer {

    @Autowired
    TelegramBot telegramBot;
    
    final Logger logger = LoggerFactory.getLogger(BotInitializer.class);
    
    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
        	 logger.error("Error occurred while registering bot: ", e);
        }
    }
}

