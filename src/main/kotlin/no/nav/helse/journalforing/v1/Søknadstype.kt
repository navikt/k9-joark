package no.nav.helse.journalforing.v1

import no.nav.helse.journalforing.BrevKode
import no.nav.helse.journalforing.Tema

enum class Søknadstype(
    val urlPath: String,
    val brevkode: BrevKode,
    val tittel: String,
    val tema: Tema,
    val innsendingstype: Innsendingstype
) {
    PLEIEPENGESØKNAD(
        urlPath = "/v1/pleiepenge/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-11.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger – sykt barn - NAV 09-11.05",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    PLEIEPENGESØKNAD_ENDRINGSMELDING(
        urlPath = "/v1/pleiepenge/endringsmelding/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-11.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger – sykt barn - NAV 09-11.05",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.ENDRING
    ),

    PLEIEPENGESØKNAD_ETTERSENDING(
        urlPath = "/v1/pleiepenge/ettersending/journalforing",
        brevkode = BrevKode(brevKode = "NAVe 09-11.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger – sykt barn - NAVe 09-11.05",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    PLEIEPENGESØKNAD_LIVETS_SLUTTFASE(
        urlPath = "/v1/pleiepenge/livets-sluttfase/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-12.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase - NAV 09-12.05",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING(
        urlPath = "/v1/pleiepenge/livets-sluttfase/ettersending/journalforing",
        brevkode = BrevKode(brevKode = "NAVe 09-12.05", dokumentKategori = "SOK"),
        tittel = "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase - NAVe 09-12.05",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD(
        urlPath = "/v1/omsorgspenge/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-06.05", dokumentKategori = "SOK"),
        tittel = "Søknad om flere omsorgsdager - NAV 09-06.05",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    OMSORGSPENGESØKNAD_ETTERSENDING(
        urlPath = "/v1/omsorgspenge/ettersending/journalforing",
        brevkode = BrevKode(brevKode = "NAVe 09-06.05", dokumentKategori = "SOK"),
        tittel = "Søknad om flere omsorgsdager - NAVe 09-06.05",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG(
        urlPath = "/v1/omsorgspengeutbetaling/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-35.01", dokumentKategori = "SOK"), // TODO: Riktig kode er: NAV 09-09.03
        tittel = "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAV 09-35.01",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING(
        urlPath = "/v1/omsorgspengeutbetaling/ettersending/journalforing",
        brevkode = BrevKode(
            brevKode = "NAVe 09-35.01",
            dokumentKategori = "SOK"
        ), // TODO: Riktig kode er: NAVe 09-09.03
        tittel = "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAVe 09-35.01",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER(
        urlPath = "/v1/omsorgspengeutbetaling/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-35.02", dokumentKategori = "SOK"), // TODO: Riktig kode er: NAV 09-09.01
        tittel = "Søknad om utbetaling av omsorgspenger for arbeidstakere - NAV 09-35.02",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING(
        urlPath = "/v1/omsorgspengeutbetaling/ettersending/journalforing",
        brevkode = BrevKode(
            brevKode = "NAVe 09-35.02",
            dokumentKategori = "SOK"
        ), // TODO: Riktig kode er: NAVe 09-09.01
        tittel = "Søknad om utbetaling av omsorgspenger for arbeidstakere - NAVe 09-35.02",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER(
        urlPath = "/v1/omsorgsdageroverforing/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
        tittel = "Søknad om overføring av omsorgsdager - NAV 09-06.08",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.MELDING
    ),

    OMSORGSPENGEMELDING_DELING_AV_DAGER(
        urlPath = "/v1/omsorgsdagerdeling/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
        tittel = "Melding om deling av omsorgsdager - NAV 09-06.08",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.MELDING
    ),

    OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING(
        urlPath = "/v1/omsorgsdagerdeling/ettersending/journalforing",
        brevkode = BrevKode(brevKode = "NAVe 09-06.08", dokumentKategori = "SOK"),
        tittel = "Melding om deling av omsorgsdager - NAVe 09-06.08",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE(
        urlPath = "/v1/omsorgspenger/midlertidig-alene/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-06.07", dokumentKategori = "SOK"),
        tittel = "Søknad om ekstra omsorgsdager når den andre forelderen ikke kan ha tilsyn med barn - NAV 09-06.07",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING(
        urlPath = "/v1/omsorgspenger/midlertidig-alene/ettersending/journalforing",
        brevkode = BrevKode(brevKode = "NAVe 09-06.07", dokumentKategori = "SOK"),
        tittel = "Søknad om å bli regnet som alene  - NAVe 09-06.07",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OMSORGSDAGER_ALENEOMSORG(
        urlPath = "/v1/omsorgsdager/aleneomsorg/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-06.10", dokumentKategori = "SOK"),
        tittel = "Registrering av aleneomsorg for omsorgsdager - NAV 09-06.10",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.MELDING
    ),

    OMSORGSDAGER_ALENEOMSORG_ETTERSENDING(
        urlPath = "/v1/omsorgsdager/aleneomsorg/ettersending/journalforing",
        brevkode = BrevKode(brevKode = "NAVe 09-06.10", dokumentKategori = "SOK"),
        tittel = "Registrering av aleneomsorg for omsorgsdager - NAVe 09-06.10",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.ETTERSENDELSE
    ),

    OPPLÆRINGSPENGESØKNAD(
        urlPath = "/v1/opplæringspenge/journalforing",
        brevkode = BrevKode(brevKode = "NAV 09-11.08", dokumentKategori = "SOK"),
        tittel = "Søknad om opplæringspenger - NAV 09-11.08",
        tema = Tema.KAPITTEL_9_YTELSER,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    FRISINNSØKNAD(
        urlPath = "/v1/frisinn/journalforing",
        brevkode = BrevKode(brevKode = "NAV 00-03.02", dokumentKategori = "SOK"),
        tittel = "Søknad om inntektskompensasjon for frilansere og selvstendig næringdrivende - NAV 00-03.02",
        tema = Tema.FRISINN,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    UNGDOMSYTELSE_SØKNAD(
        urlPath = "/v1/ungdomsytelse/soknad/journalforing",
        brevkode = BrevKode(brevKode = "UNG Søknad", dokumentKategori = "SOK"),
        tittel = "Søknad om ungdomsytelse - UNG Søknad",
        tema = Tema.UNGDOMSYTELSE,
        innsendingstype = Innsendingstype.SØKNAD
    ),

    UNGDOMSYTELSE_ENDRINGSSØKNAD(
        urlPath = "/v1/ungdomsytelse/endringssoknad/journalforing",
        brevkode = BrevKode(brevKode = "UNG Endringssøknad", dokumentKategori = "SOK"),
        tittel = "Endringssøknad for ungdomsytelsen - UNG Endringssøknad",
        tema = Tema.UNGDOMSYTELSE,
        innsendingstype = Innsendingstype.SØKNAD
    )
    ;

    init {
        if (Innsendingstype.ETTERSENDELSE == innsendingstype) {
            require(brevkode.brevKode.startsWith("NAVe")) {
                "Ettersendelser skal starte med NAVe. Ugyldig brevkode for ettersendelser ${brevkode.brevKode}"
            }
        }
        if (brevkode.brevKode.startsWith("NAVe")) {
            require(Innsendingstype.ETTERSENDELSE == innsendingstype) {
                "Innsendingstype ${innsendingstype.name} kan ikke ha brevkode for ettersendelser ${brevkode.brevKode}"
            }
        }
    }

    internal companion object {
        private val norskeBokstaver = "[ÆØÅ]".toRegex()
        private fun Søknadstype.envKey() =
            "ENABLE_${name.replace(norskeBokstaver, "")}"

        internal fun enabled(env: Map<String, String> = System.getenv()) =
            entries.associateWith {
                val envKey = it.envKey()
                val value = env[envKey]?.equals("true") ?: true
                value
            }
    }
}
