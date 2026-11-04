package br.com.matchpoint.domain

import java.util.UUID

fun interface IdGenerator {
    fun next(): String

    companion object {
        val uuid: IdGenerator = IdGenerator { UUID.randomUUID().toString() }
    }
}
