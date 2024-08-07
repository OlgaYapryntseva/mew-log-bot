package com.mewlog.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
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
    
}
