package com.filmroll.notebook.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.filmroll.notebook.R
import com.filmroll.notebook.domain.model.*

@Composable
fun RollFormFields(draft: NewRollDraft, cameras: List<CameraBody>, fieldError: Boolean, enabled: Boolean, onCustomFilm: (String) -> Unit, onCapacity: (Int) -> Unit, onPicker: (String) -> Unit) {
    val customFilmFocus = remember { FocusRequester() }
    LaunchedEffect(fieldError) { if (fieldError && draft.filmIndex == 4) customFilmFocus.requestFocus() }
    ChoiceField(R.string.film_stock, if (draft.filmIndex == 4) stringResource(R.string.other_film) else ExposureOptions.films[draft.filmIndex], { onPicker("film") }, enabled)
    if (draft.filmIndex == 4) {
        OutlinedTextField(draft.customFilm, onCustomFilm, Modifier.fillMaxWidth().focusRequester(customFilmFocus), enabled = enabled, label = { Text(stringResource(R.string.custom_film)) }, isError = fieldError, supportingText = { if (fieldError) Text(stringResource(R.string.film_required)) }, singleLine = true)
    }
    ChoiceField(R.string.rated_iso, stringResource(R.string.iso_value, draft.iso), { onPicker("iso") }, enabled)
    Text(stringResource(R.string.iso_helper), style = MaterialTheme.typography.bodyMedium)
    ChoiceField(R.string.camera, cameras.find { it.id == draft.cameraId }?.name ?: stringResource(R.string.no_camera), { onPicker("camera") }, enabled)
    if (cameras.isEmpty()) Text(stringResource(R.string.no_camera_helper), style = MaterialTheme.typography.bodyMedium)
    Text(stringResource(R.string.capacity), style = MaterialTheme.typography.labelSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(24, 36).forEach { capacity ->
            val description = stringResource(R.string.capacity_value, capacity)
            FilterChip(selected = draft.capacity == capacity, onClick = { onCapacity(capacity) }, label = { Text(description) }, enabled = enabled, modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { stateDescription = description })
        }
    }
}

@Composable
fun RollFormPicker(picker: String?, draft: NewRollDraft, cameras: List<CameraBody>, onChoose: (String) -> Unit, onDismiss: () -> Unit) {
    when (picker) {
        "film" -> ValuePickerSheet(R.string.film_stock, ExposureOptions.films.mapIndexed { index, film -> PickerItem(index.toString(), film) } + PickerItem("4", stringResource(R.string.other_film)), draft.filmIndex.toString(), onChoose, onDismiss)
        "iso" -> ValuePickerSheet(R.string.rated_iso, ExposureOptions.isoValues.map { PickerItem(it.toString(), stringResource(R.string.iso_value, it)) }, draft.iso.toString(), onChoose, onDismiss)
        "camera" -> ValuePickerSheet(R.string.camera, listOf(PickerItem("", stringResource(R.string.no_camera))) + cameras.map { PickerItem(it.id, it.name) }, draft.cameraId ?: "", onChoose, onDismiss)
    }
}
