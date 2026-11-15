package ai.pleos.playground.sample.vehicle.community

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object CommunityCodec {
    fun encode(state: CommunityState): String = JSONObject().apply {
        put("version", 1)
        put("region", state.region ?: JSONObject.NULL)
        put("trips", JSONArray().apply {
            state.trips.forEach { trip ->
                put(JSONObject().apply {
                    put("id", trip.id)
                    put("day", trip.day)
                    put("week", trip.week)
                    put("region", trip.region)
                    put("meters", trip.meters)
                    put("score", trip.score ?: JSONObject.NULL)
                })
            }
        })
        put("posts", JSONArray(state.posts))
    }.toString()

    fun decode(raw: String): CommunityState {
        val root = JSONObject(raw)
        require(root.getInt("version") == 1)
        val region = if (root.isNull("region")) null else root.getString("region")
        require(region == null || region in CommunityEngine.regions)
        val entries = root.getJSONArray("trips")
        val trips = (0 until entries.length()).map { index ->
            val entry = entries.getJSONObject(index)
            CommunityTrip(
                entry.getString("id"), entry.getString("day"), entry.getString("week"),
                entry.getString("region"), entry.getLong("meters"),
                if (entry.isNull("score")) null else entry.getInt("score")
            )
        }
        require(trips.map { it.id }.distinct().size == trips.size)
        require(trips.all {
            it.id.isNotBlank() && it.day.isNotBlank() && it.week.isNotBlank() &&
                it.region in CommunityEngine.regions && it.meters in 1..1_000_000L &&
                (it.score == null || it.score in 0..100)
        })
        val posts = root.getJSONArray("posts")
        return CommunityState(region, trips, (0 until posts.length()).map { posts.getString(it) })
    }
}

class CommunityStore(context: Context) {
    private val preferences = context.getSharedPreferences("pleomaru-community", Context.MODE_PRIVATE)
    fun load(): Result<CommunityState> = runCatching {
        preferences.getString("state", null)?.let(CommunityCodec::decode) ?: CommunityState()
    }
    fun save(state: CommunityState): Boolean =
        preferences.edit().putString("state", CommunityCodec.encode(state)).commit()
    fun clear(): Boolean = preferences.edit().clear().commit()
}
