package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.IdGenerator
import br.com.matchpoint.domain.fakes.InMemoryMatchRepository
import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.success
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchInvalidStateUseCaseTest {

    private val repository = InMemoryMatchRepository()
    private var nextId = 0
    private val ids = IdGenerator { "id-${nextId++}" }

    @Test
    fun cannotCreateAMatchWithBlankTeamNames() = runTest {
        val result = CreateMatchUseCase(repository, ids).invoke(homeName = " ", awayName = "Fora")

        assertEquals(DomainError.BlankTeamName, errorOf(result))
    }

    @Test
    fun cannotCreateAMatchWithInvalidSettings() = runTest {
        val result = CreateMatchUseCase(repository, ids).invoke(
            settings = MatchSettings(setsToWin = 1, pointsToWinSet = 25, pointsToWinLastSet = 15, mustWinByTwo = true),
        )

        assertEquals(DomainError.InvalidSettings, errorOf(result))
    }

    @Test
    fun cannotStartAMatchThatDoesNotExist() = runTest {
        val result = StartMatchUseCase(repository).invoke("missing", "home")

        assertEquals(DomainError.MatchNotFound, errorOf(result))
    }

    @Test
    fun cannotStartAMatchWithAnUnknownServingTeam() = runTest {
        val match = success(CreateMatchUseCase(repository, ids).invoke())
        AddPlayerUseCase(repository, ids).invoke(match.id, match.home.id, "Ana")
        AddPlayerUseCase(repository, ids).invoke(match.id, match.away.id, "Bia")

        val result = StartMatchUseCase(repository).invoke(match.id, "other-team")

        assertEquals(DomainError.InvalidServingTeam, errorOf(result))
    }

    @Test
    fun cannotAddAPlayerAfterTheMatchHasStarted() = runTest {
        val match = startedMatch()

        val result = AddPlayerUseCase(repository, ids).invoke(match.id, match.home.id, "Carla")

        assertEquals(DomainError.MatchNotEditable, errorOf(result))
    }

    @Test
    fun cannotRegisterAPointForAnUnknownPlayer() = runTest {
        val match = startedMatch()

        val result = RegisterPointUseCase(repository, ids).invoke(match.id, "nobody")

        assertEquals(DomainError.PlayerNotFound, errorOf(result))
    }

    @Test
    fun cannotRegisterAPointOnAnUnknownMatch() = runTest {
        val result = RegisterPointUseCase(repository, ids).invoke("missing", "p1")

        assertEquals(DomainError.MatchNotFound, errorOf(result))
    }

    private suspend fun startedMatch() = success(
        CreateMatchUseCase(repository, ids).invoke(),
    ).let { created ->
        AddPlayerUseCase(repository, ids).invoke(created.id, created.home.id, "Ana")
        AddPlayerUseCase(repository, ids).invoke(created.id, created.away.id, "Bia")
        success(StartMatchUseCase(repository).invoke(created.id, created.home.id))
    }

    private fun errorOf(result: DomainResult<*>): DomainError {
        assertTrue(result is DomainResult.Failure)
        return (result as DomainResult.Failure).error
    }
}
