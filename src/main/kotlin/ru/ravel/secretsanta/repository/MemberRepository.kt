package ru.ravel.secretsanta.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.ravel.secretsanta.model.Member

@Repository
interface MemberRepository : JpaRepository<Member, Long>{
	fun findByTelegramId(telegramId: Long) : Member?
}