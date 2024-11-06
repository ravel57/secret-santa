package ru.ravel.buhaemtgbot.telegrammessagebuilder;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.util.ArrayList;
import java.util.List;

public class SendMessageBuilder extends MessageBuilder {

	private String text;
	private ParseMode parseMode;
	private List<InlineKeyboardButton> buttons;
	private Integer keyboardOffset;
	private Integer replyMessageId;


	public SendMessageBuilder(TelegramBot bot) {
		super(bot);
	}

	@Override
	public SendMessageBuilder telegramId(Long telegramId) {
		this.telegramId = telegramId;
		return this;
	}

	public SendMessageBuilder text(String text) {
		this.text = text;
		return this;
	}

	public SendMessageBuilder buttons(Integer keyboardOffset, List<InlineKeyboardButton> buttons) {
		this.keyboardOffset = keyboardOffset;
		this.buttons = buttons;
		return this;
	}

	public SendMessageBuilder buttons(Integer keyboardOffset, InlineKeyboardButton... buttons) {
		this.keyboardOffset = keyboardOffset;
		this.buttons = List.of(buttons);
		return this;
	}

	/**
	 * callbackData = "back"
	 *
	 * @param text button text
	 * @return SendMessageBuilder
	 */
	public SendMessageBuilder addBackButton(String text) {
		this.buttons.add(new InlineKeyboardButton(text).callbackData("back"));
		return this;
	}

	public SendMessageBuilder parseMode(ParseMode parseMode) {
		this.parseMode = parseMode;
		return this;
	}

	public SendMessageBuilder replyMessage(Integer replyMessageId) {
		this.replyMessageId = replyMessageId;
		return this;
	}

	public Integer execute() throws NoSuchFieldException, TelegramException {
		if (telegramId == null || text == null) {
			throw new NoSuchFieldException();
		}
		SendMessage message = new SendMessage(telegramId, text);
		if (buttons != null && keyboardOffset != null) {
			var inlineKeyboard = new InlineKeyboardMarkup();
			List<InlineKeyboardButton> row = new ArrayList<>();
			for (InlineKeyboardButton button : buttons) {
				row.add(button);
				if (row.size() == keyboardOffset) {
					InlineKeyboardButton[] buttonArray = row.toArray(new InlineKeyboardButton[0]);
					inlineKeyboard.addRow(buttonArray);
					row = new ArrayList<>();
				}
			}
			if (!row.isEmpty()) {
				InlineKeyboardButton[] buttonArray = row.toArray(new InlineKeyboardButton[0]);
				inlineKeyboard.addRow(buttonArray);
			}
			message.replyMarkup(inlineKeyboard);
		}
		if (parseMode != null) {
			message.parseMode(parseMode);
		}
		if (replyMessageId != null) {
			message.replyToMessageId(replyMessageId);
		}

		SendResponse response = bot.execute(message);
		if (response.isOk()) {
			return response.message().messageId();
		} else {
			throw new TelegramException(new RuntimeException(response.description()));
		}
	}
}
