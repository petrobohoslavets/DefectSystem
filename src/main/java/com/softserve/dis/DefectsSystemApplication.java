package com.softserve.dis;

import com.softserve.dis.bot.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class DefectsSystemApplication {
	public static TelegramBot telegramBot;

	public static void main(String[] args) throws TelegramApiException {
		SpringApplication.run(DefectsSystemApplication.class, args);
		 TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
		 telegramBot = new TelegramBot();
		 api.registerBot(telegramBot);
	}

}
