package ai.pleos.playground.sample.vehicle.petgarden

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class GardenStore(context: Context) {
    private val prefs = context.getSharedPreferences("pet-garden-v1", Context.MODE_PRIVATE)
    val consent: Boolean get() = prefs.getBoolean("consent", false)
    fun consent(value: Boolean): Boolean = prefs.edit().putBoolean("consent", value).commit()
    fun load(): GardenState {
        val raw = prefs.getString("state", null) ?: return GardenState()
        return GardenJson.decode(raw)
    }
    fun save(state: GardenState): Boolean =
        prefs.edit().putString("state", GardenJson.encode(state)).commit()
    fun clear(): Boolean = prefs.edit().clear().commit()
}

internal object GardenJson {
    fun decode(raw: String): GardenState {
        // A corrupt save is reported to the UI; never silently replace the user's garden.
        val json = JSONObject(raw)
        fun items(key: String): Set<String> {
            val array = json.optJSONArray(key) ?: JSONArray()
            return (0 until array.length()).map { array.getString(it) }.toSet()
        }
        val resident = if (json.has("resident")) json.getJSONObject("resident").let {
            Resident(it.getString("name"), Species.valueOf(it.getString("species")), Badge.valueOf(it.getString("badge")))
        } else null
        return GardenState(json.optString("name"), json.optInt("xp"), json.optInt("leaves"),
            json.optString("day"), items("care"), json.optInt("distanceXp"), items("journeys"),
            items("bonuses"), items("owned"), items("placed"), resident)
    }
    fun encode(state: GardenState): String {
        val json = JSONObject().put("name", state.name).put("xp", state.xp).put("leaves", state.leaves)
            .put("day", state.day).put("distanceXp", state.distanceXp)
        listOf("care" to state.care, "journeys" to state.journeys, "bonuses" to state.bonuses,
            "owned" to state.owned, "placed" to state.placed).forEach { (key, items) -> json.put(key, JSONArray(items.toList())) }
        state.resident?.let {
            json.put("resident", JSONObject().put("name", it.name).put("species", it.species.name).put("badge", it.badge.name))
        }
        return json.toString()
    }
}
