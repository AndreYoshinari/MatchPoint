package br.com.matchpoint.presentation.matchsetup

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.matchpoint.R
import br.com.matchpoint.presentation.theme.MatchPointTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchSetupRoute(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: MatchSetupViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MatchSetupEvent.Saved -> onSaved(event.matchId)
            }
        }
    }
    MatchSetupScreen(
        state = state,
        onBack = onBack,
        onHomeNameChange = viewModel::onHomeNameChange,
        onAwayNameChange = viewModel::onAwayNameChange,
        onSetsToWinChange = viewModel::onSetsToWinChange,
        onHomePlayerDraftChange = viewModel::onHomePlayerDraftChange,
        onAwayPlayerDraftChange = viewModel::onAwayPlayerDraftChange,
        onAddHomePlayer = viewModel::onAddHomePlayer,
        onAddAwayPlayer = viewModel::onAddAwayPlayer,
        onRemoveHomePlayer = viewModel::onRemoveHomePlayer,
        onRemoveAwayPlayer = viewModel::onRemoveAwayPlayer,
        onConfirm = viewModel::onSave,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    state: MatchSetupUiState,
    onBack: () -> Unit,
    onHomeNameChange: (String) -> Unit,
    onAwayNameChange: (String) -> Unit,
    onSetsToWinChange: (Int) -> Unit,
    onHomePlayerDraftChange: (String) -> Unit,
    onAwayPlayerDraftChange: (String) -> Unit,
    onAddHomePlayer: () -> Unit,
    onAddAwayPlayer: () -> Unit,
    onRemoveHomePlayer: (String) -> Unit,
    onRemoveAwayPlayer: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setup_title)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.homeName,
                onValueChange = onHomeNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.setup_home_team)) },
                singleLine = true,
                enabled = !state.isLoading,
            )
            OutlinedTextField(
                value = state.awayName,
                onValueChange = onAwayNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.setup_away_team)) },
                singleLine = true,
                enabled = !state.isLoading,
            )
            Text(
                text = stringResource(R.string.setup_sets_label),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.setsToWin == 2,
                    onClick = { onSetsToWinChange(2) },
                    enabled = !state.isLoading,
                    label = { Text(stringResource(R.string.setup_best_of_three)) },
                )
                FilterChip(
                    selected = state.setsToWin == 3,
                    onClick = { onSetsToWinChange(3) },
                    enabled = !state.isLoading,
                    label = { Text(stringResource(R.string.setup_best_of_five)) },
                )
            }
            PlayerSection(
                title = stringResource(R.string.setup_home_players),
                players = state.homePlayers,
                draft = state.homePlayerDraft,
                enabled = !state.isLoading,
                onDraftChange = onHomePlayerDraftChange,
                onAdd = onAddHomePlayer,
                onRemove = onRemoveHomePlayer,
            )
            PlayerSection(
                title = stringResource(R.string.setup_away_players),
                players = state.awayPlayers,
                draft = state.awayPlayerDraft,
                enabled = !state.isLoading,
                onDraftChange = onAwayPlayerDraftChange,
                onAdd = onAddAwayPlayer,
                onRemove = onRemoveAwayPlayer,
            )
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                Text(stringResource(R.string.setup_confirm))
            }
        }
    }
}

@Composable
private fun PlayerSection(
    title: String,
    players: List<SetupPlayerDraft>,
    draft: String,
    enabled: Boolean,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        players.forEach { player ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = player.name, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemove(player.localId) }, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.setup_remove_player),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.setup_player_name)) },
                singleLine = true,
                enabled = enabled,
            )
            IconButton(onClick = onAdd, enabled = enabled) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.setup_add_player),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchSetupPreview() {
    MatchPointTheme {
        MatchSetupScreen(
            state = MatchSetupUiState(
                homePlayers = listOf(SetupPlayerDraft("1", "Ana")),
            ),
            onBack = {},
            onHomeNameChange = {},
            onAwayNameChange = {},
            onSetsToWinChange = {},
            onHomePlayerDraftChange = {},
            onAwayPlayerDraftChange = {},
            onAddHomePlayer = {},
            onAddAwayPlayer = {},
            onRemoveHomePlayer = {},
            onRemoveAwayPlayer = {},
            onConfirm = {},
        )
    }
}
