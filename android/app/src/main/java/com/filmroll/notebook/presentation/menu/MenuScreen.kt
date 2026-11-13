package com.filmroll.notebook.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.filmroll.notebook.R
import com.filmroll.notebook.core.di.ServiceLocator
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.core.theme.*
import com.filmroll.notebook.core.ui.*
import com.filmroll.notebook.domain.model.*
import com.filmroll.notebook.presentation.dialog.ConfirmationDialog

@Composable
fun MenuRoute(onEvent: (UiEvent) -> Unit) {
    val model: MenuViewModel = viewModel(factory = ServiceLocator.factory(Screen.Menu))
    val state by model.state.collectAsStateWithLifecycle()
    UiEffects(model.events, onEvent)
    MenuScreen(state, model::play, { model.navigate(Screen.NewRoll()) }, { model.navigate(Screen.Cameras()) }, { model.navigate(Screen.Settings) }, model::tab, model::open, model::askDelete, model::delete, model::cancelDialog, model::retry, model::askReset, model::reset)
}

@Composable
fun MenuScreen(state: MenuUiState, onPlay: () -> Unit, onNewRoll: () -> Unit, onCameras: () -> Unit, onSettings: () -> Unit, onTab: (Boolean) -> Unit, onOpen: (FilmRoll) -> Unit, onAskDelete: (FilmRoll) -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit, onRetry: () -> Unit, onAskReset: () -> Unit, onReset: () -> Unit) {
    val activeScroll = rememberLazyListState()
    val developedScroll = rememberLazyListState()
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val compact = maxHeight < 680.dp || fontScale > 1.2f
    NotebookPage(scroll = compact) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.film_library), color = PrimaryIndigo, style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.your_rolls), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            }
            Image(painterResource(R.drawable.logo), null, Modifier.size(44.dp), contentScale = ContentScale.Fit)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(R.string.cameras, onCameras, Modifier.weight(1f), enabled = !state.loading && !state.storageError)
            SecondaryButton(R.string.settings, onSettings, Modifier.weight(1f), enabled = !state.loading && !state.storageError)
        }
        NotebookSurface(Modifier.fillMaxWidth(), dark = true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.hero_helper), style = MaterialTheme.typography.bodyMedium, color = SecondaryLavender)
                }
                Image(painterResource(R.drawable.sprite_2), null, Modifier.size(80.dp), contentScale = ContentScale.Fit)
            }
        }
        if (!state.storageError && !state.loading) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(false, true).forEach { developed ->
                    FilterChip(selected = state.developed == developed, onClick = { onTab(developed) }, label = { Text(stringResource(if (developed) R.string.developed_tab else R.string.active_tab)) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp))
                }
            }
        }
        LazyColumn(if (compact) Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 380.dp) else Modifier.weight(1f).fillMaxWidth(), state = if (state.developed) developedScroll else activeScroll, verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 4.dp)) {
            when {
                state.loading -> item { LoadingMessage() }
                state.storageError -> item {
                    StatusMessage(stringResource(R.string.storage_error), error = true)
                    SecondaryButton(R.string.retry, onRetry, Modifier.fillMaxWidth())
                    SecondaryButton(R.string.reset_local_data, onAskReset, Modifier.fillMaxWidth())
                }
                else -> {
                    if (state.writeError) item { StatusMessage(stringResource(R.string.save_error), error = true) }
                    val rolls = state.rolls.filter { (it.status == RollStatus.DEVELOPED) == state.developed }.sortedByDescending { it.createdAt }
                    if (rolls.isEmpty()) item { EmptyNotebookState(if (state.developed) R.string.empty_developed else R.string.empty_active, if (state.developed) R.string.empty_developed_helper else R.string.empty_active_helper) }
                    items(rolls, key = { it.id }) { roll -> RollCard(roll, roll.id == state.selectedRoll?.id, { onOpen(roll) }, { onAskDelete(roll) }) }
                }
            }
        }
        if (!state.storageError) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(when {
                    state.selectedRoll == null -> stringResource(R.string.start_first)
                    state.selectedRoll.frames.size == state.selectedRoll.capacity -> stringResource(R.string.review_full)
                    else -> stringResource(R.string.continue_roll, state.selectedRoll.title)
                }, style = MaterialTheme.typography.bodyMedium)
                PrimaryButton(R.string.play, onPlay, enabled = !state.loading && !state.busy)
                SecondaryButton(R.string.new_roll, onNewRoll, Modifier.fillMaxWidth(), enabled = !state.loading && !state.busy)
            }
        }
    }
    }
    state.pendingDeletion?.let { roll -> ConfirmationDialog(R.string.delete_roll_title, stringResource(R.string.delete_roll_message, roll.title, roll.frames.size), R.string.delete_roll, onDelete, onDismiss, state.busy) }
    if (state.resetConfirmation) ConfirmationDialog(R.string.reset_title, stringResource(R.string.reset_message), R.string.reset, onReset, onDismiss, state.busy)
}
