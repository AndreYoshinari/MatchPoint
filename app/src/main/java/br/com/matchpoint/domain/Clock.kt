package br.com.matchpoint.domain

fun interface Clock {
    fun nowMillis(): Long

    companion object {
        val system: Clock = Clock { System.currentTimeMillis() }
    }
}
