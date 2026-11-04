package br.com.matchpoint.domain.usecase

import app.cash.turbine.test
import br.com.matchpoint.domain.fakes.InMemoryMatchRepository
import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.model.MatchStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchUseCasesTest {

    private val repository = InMemoryMatchRepository()
    private val ids = mutableListOf("match", "home", "away", "p1", "p2", "pt1", "pt2")
    private val idGenerator = br.com.matchpoint.domain.IdGenerator { ids.removeFirst() }

    @Test
    fun createStartAndRegisterPointPersistsThroughRepository() = runTest {
        val created = success(
            CreateMatchUseCase(repository, idGenerator).invoke("Casa", "Fora"),
        )
        AddPlayerUseCase(repository, idGenerator).invoke(created.id, created.home.id, "Ana")
        AddPlayerUseCase(repository, idGenerator).invoke(created.id, created.away.id, "Bia")
        val started = success(StartMatchUseCase(repository) { 10L }.invoke(created.id, created.home.id))
        assertEquals(MatchStatus.IN_PROGRESS, started.status)

        val afterPoint = success(RegisterPointUseCase(repository, idGenerator) { 11L }.invoke(created.id, "p1"))
        assertEquals(1, afterPoint.currentSet?.homePoints)

        ObserveMatchUseCase(repository).invoke(created.id).test {
            val latest = awaitItem()
            assertEquals(1, latest?.points?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cannotStartWithoutPlayers() = runTest {
        val created = success(CreateMatchUseCase(repository, idGenerator).invoke())
        val result = StartMatchUseCase(repository).invoke(created.id, created.home.id)
        assertEquals(DomainError.InvalidRoster, (result as DomainResult.Failure).error)
    }

    @Test
    fun cannotChangeSetupAfterStart() = runTest {
        val created = success(CreateMatchUseCase(repository, idGenerator).invoke())
        AddPlayerUseCase(repository, idGenerator).invoke(created.id, created.home.id, "Ana")
        AddPlayerUseCase(repository, idGenerator).invoke(created.id, created.away.id, "Bia")
        StartMatchUseCase(repository).invoke(created.id, created.home.id)
        val result = UpdateMatchSetupUseCase(repository).invoke(
            matchId = created.id,
            homeName = "Novo",
            awayName = "Outro",
            settings = MatchSettings.indoorDefault(),
        )
        assertEquals(DomainError.MatchNotEditable, (result as DomainResult.Failure).error)
    }

    @Test
    fun observeHistoryEmitsCreatedMatches() = runTest {
        ObserveMatchHistoryUseCase(repository).invoke().test {
            assertTrue(awaitItem().isEmpty())
            CreateMatchUseCase(repository, idGenerator).invoke("A", "B")
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun <T> success(result: DomainResult<T>): T {
        assertTrue(result is DomainResult.Success)
        return (result as DomainResult.Success).data
    }
}
