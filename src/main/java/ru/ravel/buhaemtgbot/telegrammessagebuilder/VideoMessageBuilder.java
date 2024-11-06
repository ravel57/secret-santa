package ru.ravel.buhaemtgbot.telegrammessagebuilder;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.SendResponse;

import java.io.File;

public class VideoMessageBuilder extends MessageBuilder {

	private File file;


	public VideoMessageBuilder(TelegramBot bot) {
		super(bot);
	}

	@Override
	public VideoMessageBuilder telegramId(Long telegramId) {
		this.telegramId = telegramId;
		return this;
	}

	public VideoMessageBuilder file(File file) {
		this.file = file;
		return this;
	}

	public Integer execute() throws NoSuchFieldException {
		if (telegramId == null || file == null) {
			throw new NoSuchFieldException();
		}
		SendVideo message = new SendVideo(telegramId, file);
		SendResponse response = bot.execute(message);
		if (response.isOk()) {
			return response.message().messageId();
		} else {
			throw new RuntimeException(response.description());
		}
	}
}
