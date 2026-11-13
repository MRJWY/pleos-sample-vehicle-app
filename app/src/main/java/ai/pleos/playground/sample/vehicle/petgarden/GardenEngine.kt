package ai.pleos.playground.sample.vehicle.petgarden

import ai.pleos.playground.sample.vehicle.driveclub.SeasonResult

/** No clock or Android dependencies. Daily limits are shared across all journeys that day. */
data class GardenState(
    val name: String = "",
    val xp: Int = 0,
    val leaves: Int = 0,
    val day: String = "",
    val care: Set<String> = emptySet(),
    val distanceXp: Int = 0,
    val journeys: Set<String> = emptySet(),
    val bonuses: Set<String> = emptySet(),
    val owned: Set<String> = emptySet(),
    val placed: Set<String> = emptySet(),
    val resident: Resident? = null
) {
    val level: Int get() = 1 + xp / 50
}

enum class Species(val label: String) { RABBIT("토끼"), BEAR("곰"), CAT("고양이") }
enum class Badge(val label: String) { LEAF("새잎"), SUN("햇살"), MOON("초승달") }

data class Resident(val name: String, val species: Species, val badge: Badge) {
    init { require(name.isNotBlank() && name == limitName(name)) }

    companion object {
        fun limitName(value: String): String {
            val count = value.codePointCount(0, value.length)
            return if (count <= 12) value else value.substring(0, value.offsetByCodePoints(0, 12))
        }
    }
}

enum class Decoration(val label: String, val price: Int) {
    FLOWERS("들꽃 화분", 10), LAMP("반딧불 조명", 15), PICNIC("소풍 자리", 20)
}

object GardenEngine {
    private val careActions = setOf("pet", "feed", "rest")
    fun moveIn(state: GardenState, name: String, species: Species, badge: Badge): GardenState {
        if (state.resident != null) return state
        return state.copy(resident = Resident(Resident.limitName(name.trim()), species, badge))
    }

    fun adopt(name: String, state: GardenState = GardenState()): GardenState {
        if (state.name.isNotBlank()) return state
        val cleaned = name.trim().take(12)
        require(cleaned.isNotEmpty())
        return state.copy(name = cleaned, leaves = state.leaves + 10)
    }
    private fun today(state: GardenState, day: String): GardenState =
        if (state.day == day) state else state.copy(day = day, care = emptySet(), distanceXp = 0)

    fun care(state: GardenState, day: String, action: String): GardenState {
        require(action in careActions && state.name.isNotBlank())
        val current = today(state, day)
        if (action in current.care) return current
        return current.copy(xp = current.xp + 5, leaves = current.leaves + 3, care = current.care + action)
    }
    fun journey(state: GardenState, day: String, id: String, km: Int): GardenState {
        require(km in 0..10_000 && id.isNotBlank() && state.name.isNotBlank())
        val current = today(state, day)
        if (id in current.journeys) return current
        val reward = minOf(km, 20 - current.distanceXp).coerceAtLeast(0)
        return current.copy(xp = current.xp + reward, leaves = current.leaves + reward,
            distanceXp = current.distanceXp + reward, journeys = current.journeys + id)
    }
    /** Reuses DriveClub validation. No reward for pending/insufficient weeks. */
    fun weeklyBonus(state: GardenState, seasonId: String, result: SeasonResult): GardenState {
        if (!result.eligible || seasonId in state.bonuses) return state
        return state.copy(leaves = state.leaves + 15, bonuses = state.bonuses + seasonId)
    }
    fun buy(state: GardenState, item: Decoration): GardenState {
        if (item.name in state.owned || state.leaves < item.price) return state
        return state.copy(leaves = state.leaves - item.price, owned = state.owned + item.name,
            placed = state.placed + item.name)
    }
    fun toggle(state: GardenState, item: Decoration): GardenState {
        if (item.name !in state.owned) return state
        return state.copy(placed = if (item.name in state.placed) state.placed - item.name else state.placed + item.name)
    }
}
