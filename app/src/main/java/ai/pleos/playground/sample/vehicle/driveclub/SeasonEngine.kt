package ai.pleos.playground.sample.vehicle.driveclub

/** A snapshot covers one season day; duplicates are rejected rather than silently chosen. */
data class ScoreDay(val day: Int, val drove: Boolean, val score: Int?, val pending: Boolean = false)
enum class WeekStatus(val label: String) {
    PASSED("유지 달성"), FAILED("90점 미달"), PENDING("확인 대기"), INSUFFICIENT("관찰 부족")
}
data class SeasonResult(val weeks: List<WeekStatus>, val current: Int, val longest: Int, val average: Double?, val eligible: Boolean)

object SeasonEngine {
    fun evaluate(days: List<ScoreDay>): SeasonResult {
        require(days.all { it.day in 0..27 && (it.score == null || it.score in 0..100) })
        require(days.map { it.day }.distinct().size == days.size) { "Duplicate season day" }
        val weeks = (0..3).map { week ->
            val records = days.filter { it.day / 7 == week }
            when {
                records.any { it.score != null && it.score < 90 } -> WeekStatus.FAILED
                records.size < 7 || records.any { it.pending || (it.drove && it.score == null) } -> WeekStatus.PENDING
                records.count { it.drove } < 3 -> WeekStatus.INSUFFICIENT
                else -> WeekStatus.PASSED
            }
        }
        var current = 0
        var longest = 0
        weeks.forEach {
            current = if (it == WeekStatus.PASSED) current + 1 else 0
            longest = maxOf(longest, current)
        }
        val scores = days.mapNotNull { it.score }
        return SeasonResult(weeks, current, longest, scores.takeIf { it.isNotEmpty() }?.average(), weeks.all { it == WeekStatus.PASSED })
    }

    /** Integer won allocation, competition ranking and shared boundary slots. */
    fun prizes(results: Map<String, SeasonResult>): Map<String, Int> {
        val sorted = results.entries.filter { it.value.eligible }.sortedWith(
            compareByDescending<Map.Entry<String, SeasonResult>> { it.value.longest }
                .thenByDescending { it.value.average ?: 0.0 }
        )
        val awards = mutableMapOf<String, Int>()
        var offset = 0
        sorted.groupBy { it.value.longest to it.value.average }.values.forEach { tied ->
            val slots = minOf(tied.size, (10 - offset).coerceAtLeast(0))
            val amount = slots * 20_000 / tied.size
            tied.forEach { awards[it.key] = amount }
            offset += tied.size
        }
        return awards
    }
}

fun demoDays(scenario: Int): List<ScoreDay> = (0..27).map { day ->
    ScoreDay(day, day % 7 < 4, if (scenario == 1 && day == 15) 88 else if (scenario == 2 && day == 9) null else 94,
        pending = scenario == 2 && day == 9)
}
