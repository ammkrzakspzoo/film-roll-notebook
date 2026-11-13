package com.filmroll.notebook.data.local

import android.content.Context

class SharedPreferencesStore(context: Context) {
    private val preferences = context.getSharedPreferences("film_roll_notebook_v1", Context.MODE_PRIVATE)
    fun read(): Map<String, String> = preferences.all.mapValues { requireNotNull(it.value as? String) }
    fun commit(previous: Map<String, String>, next: Map<String, String>) {
        val editor = preferences.edit()
        previous.keys.filter { it !in next }.forEach(editor::remove)
        next.filter { previous[it.key] != it.value }.forEach { (key, value) -> editor.putString(key, value) }
        check(editor.commit())
    }
    fun reset(next: Map<String, String>) {
        val editor = preferences.edit().clear()
        next.forEach { (key, value) -> editor.putString(key, value) }
        check(editor.commit())
    }
}
