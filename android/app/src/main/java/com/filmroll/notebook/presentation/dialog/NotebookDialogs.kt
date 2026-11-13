package com.filmroll.notebook.presentation.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.filmroll.notebook.R
import com.filmroll.notebook.core.ui.SecondaryButton

@Composable
fun ConfirmationDialog(title: Int, message: String, confirmLabel: Int, onConfirm: () -> Unit, onDismiss: () -> Unit, busy: Boolean = false) {
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(stringResource(title)) }, text = { Text(message) },
        confirmButton = { SecondaryButton(confirmLabel, onConfirm, enabled = !busy) }, dismissButton = { SecondaryButton(R.string.cancel, onDismiss, enabled = !busy) })
}

@Composable
fun UnsavedChangesDialog(onSave: () -> Unit, onDiscard: () -> Unit, onKeepEditing: () -> Unit, saving: Boolean = false, settings: Boolean = false) {
    AlertDialog(onDismissRequest = { if (!saving) onKeepEditing() }, title = { Text(stringResource(R.string.unsaved_title)) },
        text = { Text(stringResource(if (settings) R.string.settings_unsaved else R.string.unsaved_message)) },
        confirmButton = {
            Column {
                SecondaryButton(if (settings) R.string.save_defaults else R.string.save_draft, onSave, enabled = !saving)
                SecondaryButton(R.string.discard, onDiscard, enabled = !saving)
            }
        }, dismissButton = { SecondaryButton(R.string.keep_editing, onKeepEditing, enabled = !saving) })
}
