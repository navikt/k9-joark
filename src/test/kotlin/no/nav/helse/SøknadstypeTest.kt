package no.nav.helse

import no.nav.helse.journalforing.v1.Søknadstype
import kotlin.test.Test
import kotlin.test.assertEquals
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
        assertTrue(enabled.keys.toSet().containsAll(Søknadstype.values().toSet()))
        val disabled = enabled.filterValues { !it }
        assertEquals(1, disabled.size)
        assertEquals(Søknadstype.PLEIEPENGESØKNAD, disabled.keys.firstOrNull())
    }

    @Test
    fun `Disable alle`() {
        val enabled = Søknadstype.enabled(mapOf(
            "ENABLE_PLEIEPENGESKNAD" to "false",
            "ENABLE_PLEIEPENGESKNAD_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGESKNAD" to "false",
            "ENABLE_OMSORGSPENGESKNAD_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGESKNAD_UTBETALING_FRILANSER_SELVSTENDIG" to "false",
            "ENABLE_OMSORGSPENGESKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGESKNAD_UTBETALING_ARBEIDSTAKER" to "false",
            "ENABLE_OMSORGSPENGESKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING" to "false",
            "ENABLE_OMSORGSPENGESKNAD_OVERFRING_AV_DAGER" to "false",
            "ENABLE_OMSORGSPENGEMELDING_DELING_AV_DAGER" to "false",
            "ENABLE_OMSORGSPENGESKNAD_MIDLERTIDIG_ALENE" to "false",
            "ENABLE_OMSORGSPENGESKNAD_MIDLERTIDIG_ALENE_ETTERSENDING" to "false",
            "ENABLE_OPPLRINGSPENGESKNAD" to "false",
            "ENABLE_FRISINNSKNAD" to "false"
        ))
        assertTrue(enabled.keys.toSet().containsAll(Søknadstype.values().toSet()))
        assertTrue(enabled.values.none { it })
    }
}