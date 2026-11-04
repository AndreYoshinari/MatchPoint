package br.com.matchpoint.presentation.scoreboard

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.model.Player
import br.com.matchpoint.domain.model.Team
import br.com.matchpoint.domain.model.VolleyballSet
import br.com.matchpoint.domain.repository.MatchRepository
import br.com.matchpoint.domain.usecase.FinishMatchUseCase
import br.com.matchpoint.domain.usecase.ObserveMatchUseCase
import br.com.matchpoint.domain.usecase.RegisterPointUseCase
import br.com.matchpoint.domain.usecase.StartMatchUseCase
import br.com.matchpoint.domain.usecase.UndoLastPointUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val matchFlow = MutableSharedFlow<Match?>(extraBufferCapacity = 1)
    private val repository: MatchRepository = mock()

    private val home = Team("home", "Casa")
    private val away = Team("away", "Fora")
    private val homePlayer = Player("p1", home.id, "Ana")
    private val awayPlayer = Player("p2", away.id, "Bia")

    private lateinit var storedMatch: Match

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        storedMatch = inProgressMatch()
        whenever(repository.observeById(MATCH_ID)).thenReturn(matchFlow)
        repository.stub {
            onBlocking { getById(any()) } doAnswer { storedMatch }
            onBlocking { update(any()) } doAnswer { invocation ->
                storedMatch = invocation.getArgument(0)
                matchFlow.tryEmit(storedMatch)
                Unit
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoadingUntilMatchArrives() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ScoreboardUiState.Loading, awaitItem())
            matchFlow.emit(inProgressMatch())
            assertTrue(awaitItem() is ScoreboardUiState.Active)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun missingMatchIdEmitsErrorImmediately() = runTest {
        val viewModel = viewModel(matchId = null)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ScoreboardUiState.Error)
            assertEquals("Partida não encontrada.", (state as ScoreboardUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addingAPointUpdatesTheScoreThroughTheUseCase() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ScoreboardUiState.Loading, awaitItem())
            matchFlow.emit(inProgressMatch())
            val before = awaitItem() as ScoreboardUiState.Active
            assertEquals(0, before.match.currentSet?.homePoints)

            viewModel.onPlayerPoint(homePlayer.id)

            val after = awaitItem() as ScoreboardUiState.Active
            assertEquals(1, after.match.currentSet?.homePoints)
            assertEquals(homePlayer.id, after.match.points.single().playerId)
            assertNull(after.actionError)
            verifyBlocking(repository) { update(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unknownPlayerKeepsActiveStateAndShowsError() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ScoreboardUiState.Loading, awaitItem())
            matchFlow.emit(inProgressMatch())
            assertTrue(awaitItem() is ScoreboardUiState.Active)

            viewModel.onPlayerPoint("missing")

            val state = awaitItem() as ScoreboardUiState.Active
            assertEquals("Jogador não encontrado.", state.actionError)
            assertEquals(0, state.match.currentSet?.homePoints)
            verifyBlocking(repository, never()) { update(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun nullMatchFromRepositoryEmitsError() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ScoreboardUiState.Loading, awaitItem())
            matchFlow.emit(null)
            assertTrue(awaitItem() is ScoreboardUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun finishingASetKeepsTheMatchActiveAndOpensTheNextSet() = runTest {
        storedMatch = inProgressMatch(
            settings = MatchSettings(
                setsToWin = 3,
                pointsToWinSet = 1,
                pointsToWinLastSet = 1,
                mustWinByTwo = false,
            ),
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ScoreboardUiState.Loading, awaitItem())
            matchFlow.emit(storedMatch)
            awaitItem()

            viewModel.onPlayerPoint(homePlayer.id)

            val state = awaitItem() as ScoreboardUiState.Active
            assertEquals(2, state.match.sets.size)
            assertEquals(1, state.match.homeSetsWon)
            assertEquals(2, state.match.currentSet?.index)
            assertEquals(0, state.match.currentSet?.homePoints)
            assertEquals(MatchStatus.IN_PROGRESS, state.match.status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun finishingTheMatchViaButtonEmitsFinishedState() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ScoreboardUiState.Loading, awaitItem())
            matchFlow.emit(inProgressMatch())
            assertTrue(awaitItem() is ScoreboardUiState.Active)

            viewModel.onFinishMatch()

            val state = awaitItem() as ScoreboardUiState.Finished
            assertEquals(MatchStatus.FINISHED, state.match.status)
            verifyBlocking(repository) { update(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reachingSetsToWinFinishesTheMatch() = runTest {
        storedMatch = inProgressMatch(
            settings = MatchSettings(
                setsToWin = 2,
                pointsToWinSet = 1,
                pointsToWinLastSet = 1,
                mustWinByTwo = false,
            ),
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ScoreboardUiState.Loading, awaitItem())
            matchFlow.emit(storedMatch)
            assertTrue(awaitItem() is ScoreboardUiState.Active)

            viewModel.onPlayerPoint(homePlayer.id)
            assertTrue(awaitItem() is ScoreboardUiState.Active)

            viewModel.onPlayerPoint(homePlayer.id)
            val state = awaitItem() as ScoreboardUiState.Finished
            assertEquals(home.id, state.match.winnerTeamId)
            assertEquals(MatchStatus.FINISHED, state.match.status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel(matchId: String? = MATCH_ID): ScoreboardViewModel {
        val handle = if (matchId == null) {
            SavedStateHandle()
        } else {
            SavedStateHandle(mapOf(ScoreboardViewModel.MATCH_ID_KEY to matchId))
        }
        return ScoreboardViewModel(
            savedStateHandle = handle,
            observeMatch = ObserveMatchUseCase(repository),
            startMatch = StartMatchUseCase(repository),
            registerPoint = RegisterPointUseCase(repository),
            undoLastPoint = UndoLastPointUseCase(repository),
            finishMatch = FinishMatchUseCase(repository),
        )
    }

    private fun inProgressMatch(
        settings: MatchSettings = MatchSettings.indoorDefault(),
        sets: List<VolleyballSet> = listOf(VolleyballSet(index = 1)),
    ): Match {
        return Match(
            id = MATCH_ID,
            status = MatchStatus.IN_PROGRESS,
            settings = settings,
            home = home,
            away = away,
            players = listOf(homePlayer, awayPlayer),
            sets = sets,
            servingTeamId = home.id,
            initialServingTeamId = home.id,
            startedAt = 1L,
        )
    }

    companion object {
        private const val MATCH_ID = "m1"
    }
}
