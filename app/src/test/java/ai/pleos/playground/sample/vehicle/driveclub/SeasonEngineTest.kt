package ai.pleos.playground.sample.vehicle.driveclub

import org.junit.Assert.*
import org.junit.Test

class SeasonEngineTest {
    @Test fun completeSeasonQualifies() {
        val result = SeasonEngine.evaluate(demoDays(0))
        assertEquals(4, result.current)
        assertEquals(4, result.longest)
        assertTrue(result.eligible)
    }
    @Test fun lowScoreEndsStreakButRetainsBest() {
        val result = SeasonEngine.evaluate(demoDays(1))
        assertEquals(WeekStatus.FAILED, result.weeks[2])
        assertEquals(2, result.longest)
        assertEquals(1, result.current)
        assertFalse(result.eligible)
    }
    @Test fun pendingNeverBridgesWeeks() {
        val result = SeasonEngine.evaluate(demoDays(2))
        assertEquals(WeekStatus.PENDING, result.weeks[1])
        assertEquals(2, result.longest)
        assertFalse(result.eligible)
    }
    @Test fun restIsNotFailureButInsufficientObservationBreaksStreak() {
        val days = demoDays(0).map { if (it.day in 7..13) it.copy(drove = false, score = null) else it }
        val result = SeasonEngine.evaluate(days)
        assertEquals(WeekStatus.INSUFFICIENT, result.weeks[1])
        assertEquals(2, result.longest)
    }
    @Test fun missingDayRequiresVerification() {
        assertEquals(WeekStatus.PENDING, SeasonEngine.evaluate(demoDays(0).drop(1)).weeks[0])
    }
    @Test(expected = IllegalArgumentException::class) fun duplicatesAreRejected() {
        SeasonEngine.evaluate(demoDays(0) + demoDays(0).first())
    }
    @Test fun ninetyIsInclusive() {
        assertTrue(SeasonEngine.evaluate(demoDays(0).map { it.copy(score = 90) }).eligible)
    }
    @Test fun boundaryTieStaysWithinBudget() {
        val base = SeasonEngine.evaluate(demoDays(0))
        val participants = (1..8).associate { "top$it" to base.copy(average = 99.0) } +
            (1..3).associate { "tie$it" to base.copy(average = 94.0) }
        val prizes = SeasonEngine.prizes(participants)
        assertEquals(13_333, prizes["tie1"])
        assertEquals(199_999, prizes.values.sum())
    }
    @Test fun unverifiedParticipantReceivesNoSeasonPrize() {
        assertTrue(SeasonEngine.prizes(mapOf("pending" to SeasonEngine.evaluate(demoDays(2)))).isEmpty())
    }
}
