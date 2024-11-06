package ru.ravel.buhaemtgbot.telegrammessagebuilder;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;

import java.io.File;

public class PhotoMessageBuilder extends MessageBuilder {

	private File file;


	public PhotoMessageBuilder(TelegramBot bot) {
		super(bot);
	}

	@Override
	public PhotoMessageBuilder telegramId(Long telegramId) {
		this.telegramId = telegramId;
		return this;
	}

	public PhotoMessageBuilder file(File file) {
		this.file = file;
		return this;
	}

	public Integer execute() throws NoSuchFieldException {
		if (telegramId == null || file == null) {
			throw new NoSuchFieldException();
		}
		SendPhoto message = new SendPhoto(telegramId, file);
		SendResponse response = bot.execute(message);
		if (response.isOk()) {
			return response.message().messageId();
		} else {
			throw new RuntimeException(response.description());
		}
	}
}
