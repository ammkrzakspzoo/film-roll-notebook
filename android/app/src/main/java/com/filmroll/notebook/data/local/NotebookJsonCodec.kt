package com.filmroll.notebook.data.local

import com.filmroll.notebook.domain.logic.NotebookValidation
import com.filmroll.notebook.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

class NotebookJsonCodec {
    private fun obj(vararg pairs: Pair<String, Any?>): JSONObject = JSONObject().apply { pairs.forEach { put(it.first, it.second ?: JSONObject.NULL) } }
    private fun JSONObject.nullable(key: String): String? = if (isNull(key)) null else getString(key)
    private fun <T> JSONArray.items(parse: (JSONObject) -> T): List<T> = (0 until length()).map { parse(getJSONObject(it)) }
    private fun frame(value: ExposureFrame) = obj("id" to value.id, "number" to value.number, "aperture" to value.aperture, "shutter" to value.shutter, "note" to value.note, "created" to value.createdAt, "updated" to value.updatedAt)
    private fun parseFrame(value: JSONObject) = with(value) { ExposureFrame(getString("id"), getInt("number"), getString("aperture"), getString("shutter"), getString("note"), getLong("created"), getLong("updated")) }
    private fun roll(value: FilmRoll) = obj("id" to value.id, "sequence" to value.sequence, "title" to value.title, "film" to value.film, "iso" to value.iso, "capacity" to value.capacity, "cameraId" to value.cameraId, "cameraName" to value.cameraName, "created" to value.createdAt, "developed" to value.developedAt, "status" to value.status.name, "frames" to JSONArray(value.frames.map(::frame)), "sample" to value.isSample)
    private fun parseRoll(value: JSONObject) = with(value) { FilmRoll(getString("id"), getInt("sequence"), getString("title"), getString("film"), getInt("iso"), getInt("capacity"), nullable("cameraId"), nullable("cameraName"), getLong("created"), if (isNull("developed")) null else getLong("developed"), RollStatus.valueOf(getString("status")), getJSONArray("frames").items(::parseFrame), getBoolean("sample")) }
    private fun camera(value: CameraBody) = obj("id" to value.id, "name" to value.name, "notes" to value.notes, "created" to value.createdAt, "sample" to value.isSample)
    private fun parseCamera(value: JSONObject) = with(value) { CameraBody(getString("id"), getString("name"), getString("notes"), getLong("created"), getBoolean("sample")) }
    private fun defaults(value: NotebookDefaults) = obj("film" to value.film, "iso" to value.iso, "camera" to value.cameraId, "capacity" to value.capacity, "aperture" to value.aperture, "shutter" to value.shutter, "haptics" to value.haptics)
    private fun parseDefaults(value: JSONObject) = with(value) { NotebookDefaults(getString("film"), getInt("iso"), nullable("camera"), getInt("capacity"), getString("aperture"), getString("shutter"), getBoolean("haptics")) }
    private fun draft(value: NewRollDraft) = obj("title" to value.title, "film" to value.filmIndex, "custom" to value.customFilm, "iso" to value.iso, "camera" to value.cameraId, "capacity" to value.capacity, "isoChanged" to value.isoChanged)
    private fun parseDraft(value: JSONObject) = with(value) { NewRollDraft(getString("title"), getInt("film"), getString("custom"), getInt("iso"), nullable("camera"), getInt("capacity"), getBoolean("isoChanged")) }
    private fun frameDraft(value: FrameDraft) = obj("roll" to value.rollId, "editing" to value.editingNumber, "aperture" to value.aperture, "shutter" to value.shutter, "note" to value.note)
    private fun parseFrameDraft(value: JSONObject) = with(value) { FrameDraft(getString("roll"), if (isNull("editing")) null else getInt("editing"), getString("aperture"), getString("shutter"), getString("note")) }

    fun encode(value: Notebook): Map<String, String> = buildMap {
        put("schema_version", "1")
        put("initialization_complete", "true")
        put("next_roll_sequence", value.nextSequence.toString())
        put("roll_index", JSONArray(value.rolls.map { it.id }).toString())
        value.rolls.forEach { put("roll_${it.id}", roll(it).toString()) }
        put("cameras", JSONArray(value.cameras.map(::camera)).toString())
        put("defaults", defaults(value.defaults).toString())
        put("menu_state", obj("developed" to value.menu.developed, "roll" to value.menu.rollId).toString())
        value.newRollDraft?.let { put("new_roll_draft", draft(it).toString()) }
        value.frameDrafts.forEach { (id, value) -> put("frame_draft_$id", frameDraft(value).toString()) }
    }

    fun decode(values: Map<String, String>): Notebook {
        require(values["schema_version"] == "1" && values["initialization_complete"] == "true")
        fun required(key: String) = requireNotNull(values[key])
        val index = JSONArray(required("roll_index"))
        val menu = JSONObject(required("menu_state"))
        return Notebook(
            rolls = (0 until index.length()).map { parseRoll(JSONObject(required("roll_${index.getString(it)}"))) },
            cameras = JSONArray(required("cameras")).items(::parseCamera),
            defaults = parseDefaults(JSONObject(required("defaults"))),
            menu = MenuSelection(menu.getBoolean("developed"), menu.nullable("roll")),
            newRollDraft = values["new_roll_draft"]?.let { parseDraft(JSONObject(it)) },
            frameDrafts = values.filterKeys { it.startsWith("frame_draft_") }.map { (key, json) -> key.removePrefix("frame_draft_") to parseFrameDraft(JSONObject(json)) }.toMap(),
            nextSequence = required("next_roll_sequence").toInt()
        ).also { NotebookValidation.validate(it) }
    }
}
