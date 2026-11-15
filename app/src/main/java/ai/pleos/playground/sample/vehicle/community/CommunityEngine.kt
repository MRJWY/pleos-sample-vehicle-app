package ai.pleos.playground.sample.vehicle.community

data class CommunityTrip(
    val id: String, val day: String, val week: String, val region: String,
    val meters: Long, val score: Int?
)
data class CommunityState(
    val region: String? = null,
    val trips: List<CommunityTrip> = emptyList(),
    val posts: List<String> = emptyList()
)
data class CommunityMember(val name: String, val meters: Long, val score: Double?)

object CommunityEngine {
    val regions = listOf("경기도 성남시", "경기도 수원시", "서울특별시 강남구")
    const val weeklyGoalMeters = 100_000L

    fun enroll(state: CommunityState, region: String, week: String): CommunityState {
        require(region in regions) { "지역을 선택해 주세요." }
        require(state.trips.none { it.week == week } || state.region == region) {
            "이번 주 기록이 있어 지역 변경은 다음 주에 가능합니다."
        }
        return state.copy(region = region)
    }

    fun record(state: CommunityState, trip: CommunityTrip): CommunityState {
        require(state.region != null && trip.region == state.region)
        require(trip.id.isNotBlank() && trip.day.isNotBlank() && trip.week.isNotBlank())
        require(trip.meters in 1..1_000_000L)
        require(trip.score == null || trip.score in 0..100)
        // A retry cannot overwrite the record that originally owns this ID.
        if (state.trips.any { it.id == trip.id }) return state
        return state.copy(trips = state.trips + trip)
    }

    fun trips(state: CommunityState, week: String): List<CommunityTrip> =
        state.trips.filter { it.week == week && it.region == state.region }

    fun personal(state: CommunityState, week: String): CommunityMember {
        val records = trips(state, week)
        val scores = records.mapNotNull { it.score }
        return CommunityMember("나", records.sumOf { it.meters }, scores.takeIf { it.isNotEmpty() }?.average())
    }

    fun days(state: CommunityState, week: String): Int =
        trips(state, week).map { it.day }.distinct().size

    /** Fictional neighbors allow a local demo without a community server. */
    fun members(state: CommunityState, week: String): List<CommunityMember> {
        require(state.region in regions)
        return listOf(
            CommunityMember("여유로운출발", 31_000L, 96.0),
            CommunityMember("동네한바퀴", 24_000L, 92.0),
            CommunityMember("천천히함께", 15_000L, null),
            personal(state, week)
        )
    }

    fun distanceRanking(members: List<CommunityMember>): List<Pair<Int, CommunityMember>> =
        members.sortedWith(compareByDescending<CommunityMember> { it.meters }.thenBy { it.name })
            .map { member -> (1 + members.count { it.meters > member.meters }) to member }

    fun safetyRanking(members: List<CommunityMember>): List<Pair<Int, CommunityMember>> {
        val scored = members.filter { it.meters > 0 && it.score != null }
        return scored.sortedWith(compareByDescending<CommunityMember> { it.score }.thenBy { it.name })
            .map { member -> (1 + scored.count { it.score!! > member.score!! }) to member }
    }
}
