package ai.pleos.playground.sample.vehicle.petgarden

import org.junit.Assert.*
import org.junit.Test

class ResidentTest {
    @Test fun legacySaveKeepsEveryGardenFieldOnMoveIn() {
        val old = GardenJson.decode("""{
            "name":"구름", "xp":71, "leaves":8, "day":"2026-11-07", "distanceXp":12,
            "care":["pet"], "journeys":["trip"], "bonuses":["season"], "owned":["FLOWERS"], "placed":["FLOWERS"]
        }""")
        assertNull(old.resident)
        val resident = Resident("하루", Species.CAT, Badge.MOON)
        val new = GardenEngine.moveIn(old, " 하루 ", resident.species, resident.badge)
        assertEquals(old.copy(resident = resident), GardenJson.decode(GardenJson.encode(new)))
        assertEquals(new, GardenEngine.adopt("다른 양", new))
    }

    @Test fun everyAppearanceSurvivesRestartBeforeAndAfterAdoption() {
        for (species in Species.entries) for (badge in Badge.entries) {
            val profileOnly = GardenEngine.moveIn(GardenState(), "마루", species, badge)
            val restored = GardenJson.decode(GardenJson.encode(profileOnly))
            assertEquals(Resident("마루", species, badge), restored.resident)
            assertEquals(0, restored.leaves)
            assertEquals("", restored.name)
            val adopted = GardenEngine.adopt("구름", restored)
            assertEquals(restored.copy(name = "구름", leaves = 10), GardenJson.decode(GardenJson.encode(adopted)))
            assertEquals(adopted, GardenEngine.moveIn(adopted, "새 주민", Species.BEAR, Badge.SUN))
            assertEquals(adopted, GardenEngine.adopt("구름", adopted))
        }
    }

    @Test fun longNamesKeepTheTwelfthCharacterIntact() {
        val firstTwelve = "가".repeat(11) + "🌱"
        val state = GardenEngine.moveIn(GardenState(), firstTwelve + "나", Species.RABBIT, Badge.LEAF)
        assertEquals(firstTwelve, GardenJson.decode(GardenJson.encode(state)).resident?.name)
    }

    @Test(expected = IllegalArgumentException::class) fun emptyResidentNameIsRejected() {
        GardenEngine.moveIn(GardenState(), "  ", Species.BEAR, Badge.LEAF)
    }
}
