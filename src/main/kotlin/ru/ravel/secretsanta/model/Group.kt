package ru.ravel.secretsanta.model

import jakarta.persistence.*

@Entity
@Table(name = "group_table")
class Group (
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,

	var number: Int? = null,

	@OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
	val members: MutableList<Member> = mutableListOf(),

	@OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
	var owner: Member? = null,
)