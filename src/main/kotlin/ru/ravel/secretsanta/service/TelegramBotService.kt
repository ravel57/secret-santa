package ru.ravel.secretsanta.service

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.ParseMode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.ravel.buhaemtgbot.telegrammessagebuilder.SendMessageBuilder
import ru.ravel.secretsanta.model.Command
import ru.ravel.secretsanta.model.Group
import ru.ravel.secretsanta.model.Member
import ru.ravel.secretsanta.model.State
import ru.ravel.secretsanta.repository.GroupRepository
import ru.ravel.secretsanta.repository.MemberRepository
import java.util.*

@Service
class TelegramBotService @Autowired constructor(
	private val personRepository: MemberRepository,

	@Value("\${telegram.token}")
	private val token: String,

	private val groupRepository: GroupRepository,

	private val randomizerService: RandomizerService,
) {

	private val bot: TelegramBot = TelegramBot(token)

	init {
		bot.setUpdatesListener(
			BotUpdatesListener(
				memberRepository = personRepository,
				bot = bot,
				groupRepository = groupRepository,
				randomizerService = randomizerService
			)
		)
	}

	private class BotUpdatesListener(
		private val memberRepository: MemberRepository,
		private val bot: TelegramBot,
		private val groupRepository: GroupRepository,
		private val randomizerService: RandomizerService,
	) : UpdatesListener {
		private val logger = LoggerFactory.getLogger(javaClass)

		override fun process(
			updates: List<Update>,
		): Int {
			updates.forEach { update: Update ->
				try {
					val member: Member = memberRepository.findByTelegramId(
						update.message().from().id()
					) ?: Member(
						name = update.message().from().firstName(),
						telegramId = update.message().from().id(),
						telegramUsername = update.message().from().username(),
					)
					val text = update.message().text()
					if (text?.startsWith("/") == true) {
						when (Command.getCommandByText(text)) {
							Command.START -> {
								member.state = State.NO_STATE
								val stringBuilder =
									"""
									|Привет, ${member.name}!
									|Для входа в группу выбери команду: ${Command.CREATE_GROUP.command}
									|Для создания новой группы выбери команду: ${Command.JOIN_GROUP.command}
									|Для ввода списка пожеланий выбери: ${Command.INPUT_WISH.command}
									""".trimMargin()
								SendMessageBuilder(bot)
									.telegramId(member.telegramId)
									.text(stringBuilder)
									.parseMode(ParseMode.HTML)
									.execute()
							}

							Command.CREATE_GROUP -> {
								if (member.group != null && member.group!!.owner == member) {
									member.group!!.owner = null
									groupRepository.save(member.group!!)
								}
								val group = groupRepository.save(
									Group(
										number = Random().nextInt(999999)
									)
								)
								group.owner = member
								group.members.add(member)
								groupRepository.save(group)
								member.group = group
								SendMessageBuilder(bot)
									.telegramId(member.telegramId)
									.text("Создана комната номер: <b>${group.number}</b>")
									.parseMode(ParseMode.HTML)
									.execute()
								SendMessageBuilder(bot)
									.telegramId(member.telegramId)
									.text("Для запуска рандомайзера выбери: ${Command.START_RANDOMIZE.command}")
									.execute()
							}

							Command.JOIN_GROUP -> {
								member.state = State.JOINING_GROUP
								SendMessageBuilder(bot)
									.telegramId(member.telegramId)
									.text("Введи код группы")
									.execute()
							}

							Command.INPUT_WISH -> {
								member.state = State.INPUT_WISH
								SendMessageBuilder(bot)
									.telegramId(member.telegramId)
									.text("Введи список своих пожеланий 😄")
									.execute()
							}

							Command.START_RANDOMIZE -> {
								SendMessageBuilder(bot)
									.telegramId(member.telegramId)
									.text("Рандомайзер запущен")
									.execute()
								if (member.group?.owner == member) {
									randomizerService.randomize(member.group!!).forEach {
										SendMessageBuilder(bot)
											.telegramId(it.giftFrom.telegramId)
											.text(
												"""
												|Тебе выпал ${it.giftTo.name} (@${it.giftTo.telegramUsername})
												|С пожеланием: ${it.giftTo.wish ?: "<i>пусто</i>"}
												""".trimMargin()
											)
											.parseMode(ParseMode.HTML)
											.execute()
									}

								} else {
									SendMessageBuilder(bot)
										.telegramId(member.telegramId)
										.text("Ты не являешься создателем группы")
										.execute()
								}
							}

							Command.UNKNOWN -> {
								logger.error("Unknown command: ${update.message()}")
								SendMessageBuilder(bot)
									.telegramId(member.telegramId)
									.text("Неизвестная мне команда")
									.execute()
							}
						}
					} else {
						when (member.state!!) {
							State.NO_STATE -> {
								SendMessageBuilder(bot)
									.telegramId(member.telegramId)
									.text("Не понял")
									.execute()
							}

							State.JOINING_GROUP -> {
								member.state = State.NO_STATE
								val group = groupRepository.findByNumber(number = text!!.toInt())
								if (group != null) {
									group.members.add(member)
									member.group = group
									groupRepository.save(group)
									SendMessageBuilder(bot)
										.telegramId(member.telegramId)
										.text("Добро пожаловать в группу <b>${group.number}</b>")
										.parseMode(ParseMode.HTML)
										.execute()
									group.members.filter {
										it.telegramId != member.telegramId
									}.forEach {
										SendMessageBuilder(bot)
											.telegramId(it.telegramId)
											.text("Пользователь ${member.name} зашел в группу!")
											.execute()
									}
								} else {
									SendMessageBuilder(bot)
										.telegramId(member.telegramId)
										.text("Такая группа не найдена")
										.execute()
								}
							}

							State.INPUT_WISH -> {
								member.state = State.NO_STATE
								member.wish = text
								SendMessageBuilder(bot)
									.telegramId(member.telegramId)
									.text("Сохранил!")
									.execute()
							}
						}
					}
					memberRepository.save(member)
				} catch (e: Exception) {
					logger.error(e.message)
					SendMessageBuilder(bot)
						.telegramId(memberRepository.findByTelegramId(update.message().from().id())?.telegramId)
						.text("Произошла необработанная ошибка")
						.execute()
				}
			}
			return UpdatesListener.CONFIRMED_UPDATES_ALL
		}
	}
}