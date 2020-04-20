package no.nav.helse.journalforing.v1

import no.nav.helse.journalforing.BrevKode

internal object BrevKodeOgTittel {
    private val PLEIEPENGER_BARN = Pair(
        BrevKode(brevKode = "NAV 09-11.05", dokumentKategori = "SOK"),
        "Søknad om pleiepenger – sykt barn - NAV 09-11.05"
    )

    private val OMSORGSPENGER_UTVIDET_RETT = Pair(
        BrevKode(brevKode = "NAV 09-06.05", dokumentKategori = "SOK"),
        "Søknad om flere omsorgsdager - NAV 09-06.05"
    )

    private val OMSORGSPENGER_UTBETALING_FRILANSER_OG_SELVSTENDIG = Pair(
        BrevKode(brevKode = "NAV 09-35.01", dokumentKategori = "SOK"),
        "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAV 09-35.01"
    )

    private val OMSORGSPENGER_OVERFØRING_AV_DAGER = Pair(
        BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
        "Søknad om overføring av omsorgsdager - NAV 09-06.08"
    )

    private val OPPLÆRINGSPENGER = Pair(
        BrevKode(brevKode = "NAV 09-11.08", dokumentKategori = "SOK"),
        "Søknad om opplæringspenger - NAV 09-11.08"
    )

    private val FRISINN = Pair(
        BrevKode(brevKode = "NAV 00-03.02", dokumentKategori = "SOK"),
        "Søknad om inntektskompensasjon for frilansere og selvstendig næringdrivende - NAV 00-03.02"
    )

    internal fun hentFor(søknadstype: Søknadstype) = when (søknadstype) {
        Søknadstype.PLEIEPENGESØKNAD -> PLEIEPENGER_BARN
        Søknadstype.OMSORGSPENGESØKNAD -> OMSORGSPENGER_UTVIDET_RETT
        Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG -> OMSORGSPENGER_UTBETALING_FRILANSER_OG_SELVSTENDIG
        Søknadstype.OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER -> OMSORGSPENGER_OVERFØRING_AV_DAGER
        Søknadstype.OPPLÆRINGSPENGESØKNAD -> OPPLÆRINGSPENGER
        Søknadstype.FRISINNSØKNAD -> FRISINN
    }
}