package no.nav.helse.journalforing.v1

import no.nav.helse.journalforing.BrevKode
import no.nav.helse.journalforing.Tema

internal object BrevkodeTittelOgTema {
    private val Kapittel9Ytelse = Tema("OMS")
    private val FrilansereOgSelvstendigNæringdrivendesInntektskompensasjon = Tema("FRI")

    private val PLEIEPENGER_BARN = Triple(
        BrevKode(brevKode = "NAV 09-11.05", dokumentKategori = "SOK"),
        "Søknad om pleiepenger – sykt barn - NAV 09-11.05",
        Kapittel9Ytelse
    )

    private val OMSORGSPENGER_UTVIDET_RETT = Triple(
        BrevKode(brevKode = "NAV 09-06.05", dokumentKategori = "SOK"),
        "Søknad om flere omsorgsdager - NAV 09-06.05",
        Kapittel9Ytelse
    )

    private val OMSORGSPENGER_UTBETALING_FRILANSER_OG_SELVSTENDIG = Triple(
        BrevKode(brevKode = "NAV 09-35.01", dokumentKategori = "SOK"),
        "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAV 09-35.01",
        Kapittel9Ytelse
    )

    private val OMSORGSPENGER_UTBETALING_ARBEIDSTAKER = Triple(
        BrevKode(brevKode = "NAV 09-35.02", dokumentKategori = "SOK"),
        "Søknad om utbetaling av omsorgspenger for arbeidstakere - NAV 09-35.02",
        Kapittel9Ytelse
    )

    private val OMSORGSPENGER_OVERFØRING_AV_DAGER = Triple(
        BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
        "Søknad om overføring av omsorgsdager - NAV 09-06.08",
        Kapittel9Ytelse
    )

    private val OMSORGSPENGER_DELING_AV_DAGER = Triple(
        BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
        "Melding om deling av omsorgsdager - NAV 09-06.08",
        Kapittel9Ytelse
    )

    private val OMSORGSPENGER_MIDLERTIDIG_ALENE = Triple(
        BrevKode(brevKode = "NAV 09-06.07", dokumentKategori = "SOK"),
        "Søknad om å bli regnet som alene  - NAV 09-06.07",
        Kapittel9Ytelse
    )

    private val OPPLÆRINGSPENGER = Triple(
        BrevKode(brevKode = "NAV 09-11.08", dokumentKategori = "SOK"),
        "Søknad om opplæringspenger - NAV 09-11.08",
        Kapittel9Ytelse
    )

    private val FRISINN = Triple(
        BrevKode(brevKode = "NAV 00-03.02", dokumentKategori = "SOK"),
        "Søknad om inntektskompensasjon for frilansere og selvstendig næringdrivende - NAV 00-03.02",
        FrilansereOgSelvstendigNæringdrivendesInntektskompensasjon
    )

    internal fun hentFor(søknadstype: Søknadstype) = when (søknadstype) {
        Søknadstype.PLEIEPENGESØKNAD -> PLEIEPENGER_BARN
        Søknadstype.OMSORGSPENGESØKNAD -> OMSORGSPENGER_UTVIDET_RETT
        Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG -> OMSORGSPENGER_UTBETALING_FRILANSER_OG_SELVSTENDIG
        Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER -> OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
        Søknadstype.OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER -> OMSORGSPENGER_OVERFØRING_AV_DAGER
        Søknadstype.OMSORGSPENGEMELDING_DELING_AV_DAGER -> OMSORGSPENGER_DELING_AV_DAGER
        Søknadstype.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE -> OMSORGSPENGER_MIDLERTIDIG_ALENE
        Søknadstype.OPPLÆRINGSPENGESØKNAD -> OPPLÆRINGSPENGER
        Søknadstype.FRISINNSØKNAD -> FRISINN
    }
}