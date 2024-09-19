package no.nav.helse.journalforing.v1

import no.nav.helse.journalforing.BrevKode
import no.nav.helse.journalforing.Tema

/**
 * Brukt før `Journalpostinfo`
 */
internal object BrevkodeTittelOgTema {

    private val PLEIEPENGER_BARN = Triple(
        BrevKode(brevKode = "NAV 09-11.05", dokumentKategori = "SOK"),
        "Søknad om pleiepenger – sykt barn - NAV 09-11.05",
        Tema.KAPITTEL_9_YTELSER
    )

    private val PLEIEPENGER_BARN_ETTERSENDING = Triple(
        BrevKode(brevKode = "NAVe 09-11.05", dokumentKategori = "SOK"),
        "Søknad om pleiepenger – sykt barn - NAVe 09-11.05",
        Tema.KAPITTEL_9_YTELSER
    )

    private val PLEIEPENGER_LIVETS_SLUTTFASE = Triple(
        BrevKode(brevKode = "NAV 09-12.05", dokumentKategori = "SOK"),
        "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase - NAV 09-12.05",
        Tema.KAPITTEL_9_YTELSER
    )

    private val PLEIEPENGER_LIVETS_SLUTTFASE_ETTERSENDING = Triple(
        BrevKode(brevKode = "NAVe 09-12.05", dokumentKategori = "SOK"),
        "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase - NAVe 09-12.05",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_UTVIDET_RETT = Triple(
        BrevKode(brevKode = "NAV 09-06.05", dokumentKategori = "SOK"),
        "Søknad om flere omsorgsdager - NAV 09-06.05",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_UTVIDET_RETT_ETTERSENDING = Triple(
        BrevKode(brevKode = "NAVe 09-06.05", dokumentKategori = "SOK"),
        "Søknad om flere omsorgsdager - NAVe 09-06.05",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_UTBETALING_FRILANSER_OG_SELVSTENDIG = Triple(
        BrevKode(brevKode = "NAV 09-35.01", dokumentKategori = "SOK"),
        "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAV 09-35.01",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_UTBETALING_FRILANSER_OG_SELVSTENDIG_ETTERSENDING = Triple(
        BrevKode(brevKode = "NAVe 09-35.01", dokumentKategori = "SOK"),
        "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAVe 09-35.01",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_UTBETALING_ARBEIDSTAKER = Triple(
        BrevKode(brevKode = "NAV 09-35.02", dokumentKategori = "SOK"),
        "Søknad om utbetaling av omsorgspenger for arbeidstakere - NAV 09-35.02",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_ETTERSENDING = Triple(
        BrevKode(brevKode = "NAVe 09-35.02", dokumentKategori = "SOK"),
        "Søknad om utbetaling av omsorgspenger for arbeidstakere - NAVe 09-35.02",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_OVERFØRING_AV_DAGER = Triple(
        BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
        "Søknad om overføring av omsorgsdager - NAV 09-06.08",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_DELING_AV_DAGER = Triple(
        BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
        "Melding om deling av omsorgsdager - NAV 09-06.08",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_DELING_AV_DAGER_ETTERSENDING = Triple(
        BrevKode(brevKode = "NAVe 09-06.08", dokumentKategori = "SOK"),
        "Melding om deling av omsorgsdager - NAVe 09-06.08",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_MIDLERTIDIG_ALENE = Triple(
        BrevKode(brevKode = "NAV 09-06.07", dokumentKategori = "SOK"),
        "Søknad om ekstra omsorgsdager når den andre forelderen ikke kan ha tilsyn med barn - NAV 09-06.07",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSPENGER_MIDLERTIDIG_ALENE_ETTERSENDING = Triple(
        BrevKode(brevKode = "NAVe 09-06.07", dokumentKategori = "SOK"),
        "Søknad om å bli regnet som alene  - NAVe 09-06.07",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSDAGER_ALENEOMSORG = Triple(
        BrevKode(brevKode = "NAV 09-06.10", dokumentKategori = "SOK"),
        "Registrering av aleneomsorg for omsorgsdager - NAV 09-06.10",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OMSORGSDAGER_ALENEOMSORG_ETTERSENDING = Triple(
        BrevKode(brevKode = "NAVe 09-06.10", dokumentKategori = "SOK"),
        "Registrering av aleneomsorg for omsorgsdager - NAVe 09-06.10",
        Tema.KAPITTEL_9_YTELSER
    )

    private val OPPLÆRINGSPENGER = Triple(
        BrevKode(brevKode = "NAV 09-11.08", dokumentKategori = "SOK"),
        "Søknad om opplæringspenger - NAV 09-11.08",
        Tema.KAPITTEL_9_YTELSER
    )

    private val FRISINN = Triple(
        BrevKode(brevKode = "NAV 00-03.02", dokumentKategori = "SOK"),
        "Søknad om inntektskompensasjon for frilansere og selvstendig næringdrivende - NAV 00-03.02",
        Tema.FRISINN
    )

    private val UNNGOMSYTELSE_SØKNAD = Triple(
        BrevKode(brevKode = "UNG Søknad", dokumentKategori = "SOK"),
        "Søknad om ungdomsytelse - UNG Søknad",
        Tema.UNGDOMSYTELSE
    )

    private val UNNGOMSYTELSE_ENDRINGSSØKNAD = Triple(
        BrevKode(brevKode = "UNG Endringssøknad", dokumentKategori = "SOK"),
        "Endringssøknad for ungdomsytelsen - UNG Endringssøknad",
        Tema.UNGDOMSYTELSE
    )

    internal fun hentFor(søknadstype: Søknadstype): Triple<BrevKode, String, Tema> = when (søknadstype) {
        Søknadstype.PLEIEPENGESØKNAD, Søknadstype.PLEIEPENGESØKNAD_ENDRINGSMELDING -> PLEIEPENGER_BARN
        Søknadstype.PLEIEPENGESØKNAD_ETTERSENDING -> PLEIEPENGER_BARN_ETTERSENDING
        Søknadstype.PLEIEPENGESØKNAD_LIVETS_SLUTTFASE -> PLEIEPENGER_LIVETS_SLUTTFASE
        Søknadstype.PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING -> PLEIEPENGER_LIVETS_SLUTTFASE_ETTERSENDING
        Søknadstype.OMSORGSPENGESØKNAD -> OMSORGSPENGER_UTVIDET_RETT
        Søknadstype.OMSORGSPENGESØKNAD_ETTERSENDING -> OMSORGSPENGER_UTVIDET_RETT_ETTERSENDING
        Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG -> OMSORGSPENGER_UTBETALING_FRILANSER_OG_SELVSTENDIG
        Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING -> OMSORGSPENGER_UTBETALING_FRILANSER_OG_SELVSTENDIG_ETTERSENDING
        Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER -> OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
        Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING -> OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_ETTERSENDING
        Søknadstype.OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER -> OMSORGSPENGER_OVERFØRING_AV_DAGER
        Søknadstype.OMSORGSPENGEMELDING_DELING_AV_DAGER -> OMSORGSPENGER_DELING_AV_DAGER
        Søknadstype.OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING -> OMSORGSPENGER_DELING_AV_DAGER_ETTERSENDING
        Søknadstype.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE -> OMSORGSPENGER_MIDLERTIDIG_ALENE
        Søknadstype.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING -> OMSORGSPENGER_MIDLERTIDIG_ALENE_ETTERSENDING
        Søknadstype.OMSORGSDAGER_ALENEOMSORG -> OMSORGSDAGER_ALENEOMSORG
        Søknadstype.OMSORGSDAGER_ALENEOMSORG_ETTERSENDING -> OMSORGSDAGER_ALENEOMSORG_ETTERSENDING
        Søknadstype.OPPLÆRINGSPENGESØKNAD -> OPPLÆRINGSPENGER
        Søknadstype.FRISINNSØKNAD -> FRISINN
        Søknadstype.UNGDOMSYTELSE_SØKNAD -> UNNGOMSYTELSE_SØKNAD
        Søknadstype.UNGDOMSYTELSE_ENDRINGSSØKNAD -> UNNGOMSYTELSE_ENDRINGSSØKNAD
    }
}
