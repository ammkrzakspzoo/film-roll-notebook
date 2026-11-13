package com.filmroll.notebook.domain.logic

import com.filmroll.notebook.domain.model.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ExportLabels(val title: String, val sample: String, val yes: String, val no: String, val film: String, val iso: String, val camera: String, val noCamera: String, val created: String, val status: String, val active: String, val developed: String, val recorded: String, val of: String, val frames: String, val frame: String, val note: String, val noNote: String, val aperture: String, val seconds: String, val bulb: String)
data class TextExport(val text: String, val filename: String)

class TextExportFormatter(private val labels: ExportLabels) {
    fun format(roll: FilmRoll): TextExport {
        val date = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())
        val text = buildString {
            appendLine(labels.title)
            appendLine(roll.title)
            appendLine("${labels.sample}: ${if (roll.isSample) labels.yes else labels.no}")
            appendLine("${labels.film}: ${roll.film}")
            appendLine("${labels.iso}: ${roll.iso}")
            appendLine("${labels.camera}: ${roll.cameraName ?: labels.noCamera}")
            appendLine("${labels.created}: ${date.format(Instant.ofEpochMilli(roll.createdAt))}")
            appendLine("${labels.status}: ${if (roll.status == RollStatus.ACTIVE) labels.active else labels.developed}")
            roll.developedAt?.let { appendLine("${labels.developed}: ${date.format(Instant.ofEpochMilli(it))}") }
            appendLine("${labels.recorded}: ${roll.frames.size} ${labels.of} ${roll.capacity}")
            appendLine()
            appendLine(labels.frames)
            roll.frames.sortedBy { it.number }.forEach { frame ->
                appendLine()
                val shutter = if (frame.shutter == "BULB") labels.bulb else "${frame.shutter} ${labels.seconds}"
                appendLine("${labels.frame} ${frame.number.toString().padStart(2, '0')} | ${labels.aperture}${frame.aperture} | $shutter")
                appendLine("${labels.note}: ${frame.note.takeIf { it.isNotBlank() } ?: labels.noNote}")
            }
        }
        val slug = roll.title.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_").trim('_').take(40).ifEmpty { "roll" }
        val created = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(roll.createdAt))
        return TextExport(text, "film_roll_${slug}_${created}_${roll.id.take(8)}.txt")
    }
}
