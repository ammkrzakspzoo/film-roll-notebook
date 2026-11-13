package com.filmroll.notebook.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.filmroll.notebook.R
import com.filmroll.notebook.core.theme.*

data class PickerItem(val id: String, val label: String)

@Composable
fun ChoiceField(label: Int, value: String, onOpen: () -> Unit, enabled: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(label), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
        OutlinedButton(onOpen, Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { stateDescription = value }, enabled = enabled, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, SecondaryLavender)) {
            Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.choose), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ExposureStepper(label: Int, value: String, canDecrease: Boolean, canIncrease: Boolean, onDecrease: () -> Unit, onIncrease: () -> Unit, onOpen: () -> Unit, enabled: Boolean = true) {
    Column(Modifier.fillMaxWidth().semantics { stateDescription = value }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceField(label, value, onOpen, enabled)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(R.string.decrease, onDecrease, Modifier.weight(1f), enabled && canDecrease)
            SecondaryButton(R.string.increase, onIncrease, Modifier.weight(1f), enabled && canIncrease)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValuePickerSheet(title: Int, items: List<PickerItem>, selectedId: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = NotebookBackground) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).safeDrawingPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
            LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 450.dp)) {
                items(items, key = { it.id }) { item ->
                    TextButton(onClick = { onSelect(item.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { selected = item.id == selectedId; stateDescription = item.label }) {
                        RadioButton(selected = item.id == selectedId, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(item.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            SecondaryButton(R.string.done, onDismiss, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
    }
}
