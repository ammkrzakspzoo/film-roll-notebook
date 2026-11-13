package com.filmroll.notebook.data.local

import android.content.Context
import android.net.Uri
import com.filmroll.notebook.domain.repository.TextDocumentWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalTextDocumentWriter(context: Context) : TextDocumentWriter {
    private val resolver = context.contentResolver
    override suspend fun write(uri: String, text: String) = withContext(Dispatchers.IO) {
        requireNotNull(resolver.openOutputStream(Uri.parse(uri), "wt")).use { stream ->
            stream.write(text.toByteArray(Charsets.UTF_8))
            stream.flush()
        }
    }
}
