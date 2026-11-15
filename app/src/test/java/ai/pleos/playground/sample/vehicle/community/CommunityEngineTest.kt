package ai.pleos.playground.sample.vehicle.community

import org.junit.Assert.*
import org.junit.Test

class CommunityEngineTest {
    private val region = CommunityEngine.regions.first()
    private val week = "2026-11-09"
    private val state = CommunityState(region)
    private fun trip(id: String = "a", day: String = "2026-11-09", score: Int? = 94) =
        CommunityTrip(id, day, week, region, 30_000, score)

    @Test fun retryAfterRestartDoesNotCountTwice() {
        val first = CommunityEngine.record(state, trip())
        val restored = CommunityCodec.decode(CommunityCodec.encode(first))
        assertEquals(first, CommunityEngine.record(restored, trip().copy(meters = 80_000)))
        assertEquals(30_000L, CommunityEngine.personal(restored, week).meters)
    }

    @Test fun twoTripsOnOneDayCountAsOneParticipationDay() {
        val first = CommunityEngine.record(state, trip())
        val next = CommunityEngine.record(first, trip("b"))
        assertEquals(60_000L, CommunityEngine.personal(next, week).meters)
        assertEquals(1, CommunityEngine.days(next, week))
    }

    @Test fun lateRecordRetainsItsOriginalWeek() {
        val old = trip().copy(week = "2026-11-02")
        val next = CommunityEngine.record(state, old)
        assertEquals(0L, CommunityEngine.personal(next, week).meters)
        assertEquals(30_000L, CommunityEngine.personal(next, old.week).meters)
    }

    @Test fun regionIsLockedAfterFirstContribution() {
        val enrolled = CommunityEngine.record(state, trip())
        assertThrows(IllegalArgumentException::class.java) {
            CommunityEngine.enroll(enrolled, CommunityEngine.regions[1], week)
        }
        assertEquals(enrolled, CommunityEngine.enroll(enrolled, region, week))
    }

    @Test fun nextWeekRegionChangePreservesHistoryAndExcludesOldContribution() {
        val first = CommunityEngine.record(state, trip())
        val next = CommunityEngine.enroll(first, CommunityEngine.regions[1], "2026-11-16")
        assertEquals(1, next.trips.size)
        assertEquals(0L, CommunityEngine.personal(next, week).meters)
    }

    @Test fun rejectsInvalidAndForeignRecords() {
        listOf(trip().copy(meters = -1), trip().copy(score = 101), trip().copy(region = "다른 지역")).forEach { invalid ->
            assertThrows(IllegalArgumentException::class.java) { CommunityEngine.record(state, invalid) }
        }
    }

    @Test fun pendingScoresHaveNoSafetyRankAndTiesShareRank() {
        val members = listOf(
            CommunityMember("가", 30_000, 94.0),
            CommunityMember("나", 20_000, 94.0),
            CommunityMember("다", 10_000, 90.0),
            CommunityMember("라", 0, null)
        )
        assertEquals(listOf(1, 1, 3), CommunityEngine.safetyRanking(members).map { it.first })
        assertEquals(listOf(1, 2, 3, 4), CommunityEngine.distanceRanking(members).map { it.first })
    }

    @Test fun personalContributionReplacesItselfInSharedTotal() {
        val baseline = CommunityEngine.members(state, week).sumOf { it.meters }
        val next = CommunityEngine.record(state, trip())
        val updated = CommunityEngine.members(next, week).sumOf { it.meters }
        assertEquals(30_000L, updated - baseline)
        assertEquals(CommunityEngine.weeklyGoalMeters, updated)
    }

    @Test fun missingScoresStayMissingAfterPersistence() {
        val next = CommunityEngine.record(state, trip(score = null)).copy(posts = listOf("함께해요"))
        assertEquals(next, CommunityCodec.decode(CommunityCodec.encode(next)))
        assertNull(CommunityEngine.personal(next, week).score)
        assertEquals(CommunityState(), CommunityCodec.decode(CommunityCodec.encode(CommunityState())))
    }

    @Test fun unknownSaveVersionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            CommunityCodec.decode("""{"version":2,"region":null,"trips":[],"posts":[]}""")
        }
    }
}
