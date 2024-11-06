package ru.ravel.secretsanta.model

import jakarta.persistence.*

@Entity
class Member(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,

	var name: String? = null,

	var telegramId: Long? = null,

	var telegramUsername: String? = null,

	@Enumerated(EnumType.ORDINAL)
	var state: State? = null,

	@ManyToOne
	@JoinColumn(name = "group_id")
	var group: Group? = null,

	var wish: String? = null,

	@OneToOne
	var giftTo: Member? = null,
)