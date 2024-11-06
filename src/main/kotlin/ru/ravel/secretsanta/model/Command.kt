package ru.ravel.secretsanta.model

import ru.ravel.secretsanta.model.Command.UNKNOWN
import ru.ravel.secretsanta.model.Command.entries

enum class Command(
	val command: String?,
) {
	START("/start"),
	CREATE_GROUP("/creategroup"),
	JOIN_GROUP("/joingroup"),
	INPUT_WISH("/inputwish"),
	START_RANDOMIZE("/startrandomize"),
	UNKNOWN,
	;

	constructor() : this(null)

	companion object {
		fun getCommandByText(command: String): Command {
			return entries.find { it.command.equals(command, true) } ?: UNKNOWN
		}
	}
}