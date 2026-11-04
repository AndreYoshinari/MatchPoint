package br.com.matchpoint.domain.model

data class Player(
    val id: String,
    val teamId: String,
    val name: String,
    val number: Int? = null,
)
