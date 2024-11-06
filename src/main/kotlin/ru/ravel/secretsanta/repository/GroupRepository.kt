package ru.ravel.secretsanta.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.ravel.secretsanta.model.Group

@Repository
interface GroupRepository : JpaRepository<Group, Long> {
	fun findByNumber(number: Int): Group?
}