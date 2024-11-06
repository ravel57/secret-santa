package ru.ravel.secretsanta.service

import org.springframework.stereotype.Service
import ru.ravel.secretsanta.model.Group
import ru.ravel.secretsanta.model.Presenter
import java.util.*
import kotlin.collections.ArrayList

@Service
class RandomizerService {
	fun randomize(group: Group): List<Presenter> {
		val members = group.members.shuffled()
		return if (members.size > 1) {
			members.mapIndexed { index, member ->
				val giftTo = members[(index + 1) % members.size]
				member.giftTo = giftTo
				Presenter(member, giftTo)
			}
		} else {
			listOf()
		}
	}
}