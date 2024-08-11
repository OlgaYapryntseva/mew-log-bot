package com.mewlog.configuration;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import lombok.Data;

@Configuration
@Data
@EnableScheduling
public class BotConfig {
	
	@Value("${telegram.bot.username}")
    String botUserName;

    @Value("${telegram.bot.token}")
    String botToken;

    @Value("${telegram.bot.link}")
    String botLink;
    
    List<BotCommand> listBotCommand = new ArrayList<>();
    List<BotCommand> listBotOption = new ArrayList<>();
    
    public BotConfig() {
        listBotCommand.add(new BotCommand("/start", "начать взаимодействие с ботом"));
        listBotCommand.add(new BotCommand("/option", "вывести список всех команд"));
        listBotOption.add(new BotCommand("/invitation", "пригласить еще одного хозяина"));
        listBotOption.add(new BotCommand("/report", "сделать отчет"));
        listBotCommand.add(new BotCommand("/cancel", "отменить все операции"));
        listBotCommand.add(new BotCommand("/about", "узнать общую информацию о боте"));
        listBotCommand.add(new BotCommand("/help", "получить информацию о доступных командах"));
    } 
    
	public boolean containsCommandKey(String message) {
	    return listBotCommand.stream()
	                         .map(BotCommand::getCommand)
	                         .anyMatch(message::contains);
	}
	
	public boolean containsOptionKey(String message) {
	    return listBotOption.stream()
	                         .map(BotCommand::getCommand)
	                         .anyMatch(message::contains);
	}
    
}
