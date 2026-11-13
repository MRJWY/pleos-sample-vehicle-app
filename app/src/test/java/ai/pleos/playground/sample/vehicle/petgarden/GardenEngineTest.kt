package ai.pleos.playground.sample.vehicle.petgarden

import ai.pleos.playground.sample.vehicle.driveclub.SeasonEngine
import ai.pleos.playground.sample.vehicle.driveclub.demoDays
import org.junit.Assert.*
import org.junit.Test

class GardenEngineTest {
    private val day = "2026-11-07"
    private val pet = GardenEngine.adopt("구름")
    @Test fun restDaysCanGrowWithoutDriving() {
        val cared = listOf("pet", "feed", "rest").fold(pet) { state, action -> GardenEngine.care(state, day, action) }
        assertEquals(15, cared.xp)
        assertEquals(19, cared.leaves)
    }
    @Test fun repeatedCareDoesNotFarmRewards() {
        val once = GardenEngine.care(pet, day, "pet")
        assertEquals(once, GardenEngine.care(once, day, "pet"))
        assertEquals(10, GardenEngine.care(once, "2026-11-08", "pet").xp)
    }
    @Test fun separateJourneysShareDailyCap() {
        val first = GardenEngine.journey(pet, day, "a", 12)
        val second = GardenEngine.journey(first, day, "b", 100)
        assertEquals(20, second.xp)
        assertEquals(second, GardenEngine.journey(second, day, "b", 100))
        assertEquals(25, GardenEngine.journey(second, "2026-11-08", "c", 5).xp)
    }
    @Test fun replayOnAnotherDayNeverRepaysSameJourney() {
        val first = GardenEngine.journey(pet, day, "same", 12)
        val replay = GardenEngine.journey(first, "2026-11-08", "same", 12)
        assertEquals(first.xp, replay.xp)
        assertEquals(first.leaves, replay.leaves)
    }
    @Test fun incompleteScoresGiveNoBonusAndNeverHurtPet() {
        for (scenario in listOf(1, 2)) assertEquals(pet, GardenEngine.weeklyBonus(pet, "season", SeasonEngine.evaluate(demoDays(scenario))))
    }
    @Test fun validBonusCannotBeClaimedTwice() {
        val result = SeasonEngine.evaluate(demoDays(0))
        val once = GardenEngine.weeklyBonus(pet, "season", result)
        assertEquals(25, once.leaves)
        assertEquals(once, GardenEngine.weeklyBonus(once, "season", result))
    }
    @Test fun purchasesChargeOnceAndPlacementIsFree() {
        assertEquals(pet, GardenEngine.buy(pet, Decoration.PICNIC))
        val bought = GardenEngine.buy(pet, Decoration.FLOWERS)
        assertEquals(0, bought.leaves)
        assertEquals(bought, GardenEngine.buy(bought, Decoration.FLOWERS))
        val stored = GardenEngine.toggle(bought, Decoration.FLOWERS)
        assertTrue(stored.placed.isEmpty())
        assertEquals(bought, GardenEngine.toggle(stored, Decoration.FLOWERS))
    }
    @Test(expected = IllegalArgumentException::class) fun negativeDistanceIsRejected() {
        GardenEngine.journey(pet, day, "bad", -1)
    }
    @Test(expected = IllegalArgumentException::class) fun blankNameIsRejected() {
        GardenEngine.adopt(" ")
    }
}
