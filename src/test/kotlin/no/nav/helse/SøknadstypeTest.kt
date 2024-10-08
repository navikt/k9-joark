package no.nav.helse

import no.nav.helse.journalforing.v1.Søknadstype
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class SøknadstypeTest {
    @Test
    fun `Alle default enabled`() {
        val enabled = Søknadstype.enabled(mapOf())
        assertTrue(enabled.keys.toSet().containsAll(Søknadstype.values().toSet()))
        assertTrue(enabled.values.none { !it })
    }

    @Test
    fun `Disable pleiepenger`() {
        val enabled = Søknadstype.enabled(mapOf(
            "ENABLE_PLEIEPENGESKNAD" to "false"
        ))
        assertTrue(enabled.keys.toSet().containsAll(Søknadstype.entries.toSet()))
        val disabled = enabled.filterValues { !it }
        assertEquals(1, disabled.size)
        assertEquals(Søknadstype.PLEIEPENGESØKNAD, disabled.keys.firstOrNull())
    }

    @Test
    fun `Disable alle`() {
        val enabled = Søknadstype.enabled(mapOf(
            "ENABLE_PLEIEPENGESKNAD" to "false",
            "ENABLE_PLEIEPENGESKNAD_ENDRINGSMELDING" to "false",
            "ENABLE_PLEIEPENGESKNAD_ETTERSENDING" to "false",
            "ENABLE_PLEIEPENGESKNAD_LIVETS_SLUTTFASE" to "false",
            "ENABLE_PLEIEPENGESKNAD_LIVETS_SLUTTFASE_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGESKNAD" to "false",
            "ENABLE_OMSORGSPENGESKNAD_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGESKNAD_UTBETALING_FRILANSER_SELVSTENDIG" to "false",
            "ENABLE_OMSORGSPENGESKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGESKNAD_UTBETALING_ARBEIDSTAKER" to "false",
            "ENABLE_OMSORGSPENGESKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGESKNAD_OVERFRING_AV_DAGER" to "false",
            "ENABLE_OMSORGSPENGEMELDING_DELING_AV_DAGER" to "false",
            "ENABLE_OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGESKNAD_MIDLERTIDIG_ALENE" to "false",
            "ENABLE_OMSORGSPENGESKNAD_MIDLERTIDIG_ALENE_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGER_ALENEOMSORG" to "false",
            "ENABLE_OPPLRINGSPENGESKNAD" to "false",
            "ENABLE_FRISINNSKNAD" to "false",
            "ENABLE_OMSORGSDAGER_ALENEOMSORG" to "false",
            "ENABLE_OMSORGSDAGER_ALENEOMSORG_ETTERSENDING" to "false",
            "ENABLE_UNGDOMSYTELSE_SKNAD" to "false",
            "ENABLE_UNGDOMSYTELSE_ENDRINGSSKNAD" to "false",
        ))
        assertTrue(enabled.keys.toSet().containsAll(Søknadstype.entries.toSet()))
        for (entry in enabled.entries) {
            assertFalse(entry.value, "Søknadstype ${entry.key} skulle vært disabled, men er enabled")
        }
        //assertTrue(enabled.values.none { it })
    }
}
