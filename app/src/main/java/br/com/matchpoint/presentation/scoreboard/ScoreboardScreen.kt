package br.com.matchpoint.presentation.scoreboard

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.matchpoint.R
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.Player
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScoreboardRoute(
    onBack: () -> Unit,
    viewModel: ScoreboardViewModel = koinViewModel(),
) {
    KeepScreenOn()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScoreboardScreen(
        state = state,
        onBack = onBack,
        onPlayerPoint = viewModel::onPlayerPoint,
        onUndo = viewModel::onUndoLastPoint,
        onFinish = viewModel::onFinishMatch,
    )
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(
    state: ScoreboardUiState,
    onBack: () -> Unit,
    onPlayerPoint: (String) -> Unit,
    onUndo: () -> Unit,
    onFinish: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scoreboard_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.setup_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (state) {
                ScoreboardUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is ScoreboardUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                is ScoreboardUiState.Active -> {
                    LiveMatchContent(
                        match = state.match,
                        actionError = state.actionError,
                        isFinished = false,
                        onPlayerPoint = onPlayerPoint,
                        onUndo = onUndo,
                        onFinish = onFinish,
                    )
                }
                is ScoreboardUiState.Finished -> {
                    LiveMatchContent(
                        match = state.match,
                        actionError = state.actionError,
                        isFinished = true,
                        onPlayerPoint = onPlayerPoint,
                        onUndo = onUndo,
                        onFinish = onFinish,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveMatchContent(
    match: Match,
    actionError: String?,
    isFinished: Boolean,
    onPlayerPoint: (String) -> Unit,
    onUndo: () -> Unit,
    onFinish: () -> Unit,
) {
    var scoringTeamId by remember { mutableStateOf<String?>(null) }
    val currentSet = match.currentSet
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.scoreboard_sets, match.homeSetsWon, match.awaySetsWon),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.scoreboard_current_set, currentSet?.index ?: 1),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (isFinished) {
            Text(
                text = match.winner?.let { stringResource(R.string.scoreboard_winner, it.name) }
                    ?: stringResource(R.string.scoreboard_finished),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TeamScoreColumn(
                modifier = Modifier.weight(1f),
                teamName = match.home.name,
                points = currentSet?.homePoints ?: 0,
                isServing = match.servingTeamId == match.home.id && !isFinished,
                enabled = !isFinished,
                onAddPoint = { scoringTeamId = match.home.id },
            )
            TeamScoreColumn(
                modifier = Modifier.weight(1f),
                teamName = match.away.name,
                points = currentSet?.awayPoints ?: 0,
                isServing = match.servingTeamId == match.away.id && !isFinished,
                enabled = !isFinished,
                onAddPoint = { scoringTeamId = match.away.id },
            )
        }
        if (actionError != null) {
            Text(
                text = actionError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        OutlinedButton(
            onClick = onUndo,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.scoreboard_undo))
        }
        if (!isFinished) {
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.scoreboard_finish))
            }
        }
    }

    val teamId = scoringTeamId
    if (teamId != null) {
        val players = match.playersOf(teamId)
        PlayerPickerDialog(
            players = players,
            onDismiss = { scoringTeamId = null },
            onPlayerSelected = { playerId ->
                scoringTeamId = null
                onPlayerPoint(playerId)
            },
        )
    }
}

@Composable
private fun TeamScoreColumn(
    teamName: String,
    points: Int,
    isServing: Boolean,
    enabled: Boolean,
    onAddPoint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )
        if (isServing) {
            SuggestionChip(
                onClick = {},
                label = { Text(stringResource(R.string.scoreboard_serving)) },
            )
        } else {
            Spacer(Modifier.height(32.dp))
        }
        Text(
            text = points.toString(),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
        )
        FilledTonalButton(
            onClick = onAddPoint,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.scoreboard_add_point))
        }
    }
}

@Composable
private fun PlayerPickerDialog(
    players: List<Player>,
    onDismiss: () -> Unit,
    onPlayerSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.scoreboard_select_player)) },
        text = {
            Column {
                players.forEach { player ->
                    TextButton(
                        onClick = { onPlayerSelected(player.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(player.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.scoreboard_cancel))
            }
        },
    )
}
