package no.nav.helse.journalforing.v1

import no.nav.helse.journalforing.BrevKode
import no.nav.helse.journalforing.Tema

enum class Innsendingstype {
    SØKNAD,
    MELDING,
    ETTERSENDELSE,
    ENDRING
}

internal data class Journalpostinfo(
    internal val brevkode: BrevKode,
    internal val tema: Tema,
    internal val tittel: String,
    internal val innsendingstype: Innsendingstype) {
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
        private val Kapittel9Ytelse = Tema("OMS")
        private val FrilansereOgSelvstendigNæringdrivendesInntektskompensasjon = Tema("FRI")

        private val journalpostinfo = mapOf(
            Søknadstype.PLEIEPENGESØKNAD to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-11.05", dokumentKategori = "SOK"),
                tittel = "Søknad om pleiepenger – sykt barn - NAV 09-11.05",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.SØKNAD
            ),
            Søknadstype.PLEIEPENGESØKNAD_ENDRINGSMELDING to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-11.05", dokumentKategori = "SOK"),
                tittel = "Søknad om pleiepenger – sykt barn - NAV 09-11.05",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.ENDRING
            ),
            Søknadstype.PLEIEPENGESØKNAD_ETTERSENDING to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAVe 09-11.05", dokumentKategori = "SOK"),
                tittel = "Søknad om pleiepenger – sykt barn - NAVe 09-11.05",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.ETTERSENDELSE
            ),
            Søknadstype.PLEIEPENGESØKNAD_LIVETS_SLUTTFASE to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-12.05", dokumentKategori = "SOK"),
                tittel = "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase - NAV 09-12.05",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.SØKNAD
            ),
            Søknadstype.PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAVe 09-12.05", dokumentKategori = "SOK"),
                tittel = "Søknad om pleiepenger ved pleie i hjemmet av nærstående i livets sluttfase - NAVe 09-12.05",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.ETTERSENDELSE
            ),
            Søknadstype.OMSORGSPENGESØKNAD to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-06.05", dokumentKategori = "SOK"),
                tittel = "Søknad om flere omsorgsdager - NAV 09-06.05",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.SØKNAD
            ),
            Søknadstype.OMSORGSPENGESØKNAD_ETTERSENDING to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAVe 09-06.05", dokumentKategori = "SOK"),
                tittel = "Søknad om flere omsorgsdager - NAVe 09-06.05",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.ETTERSENDELSE
            ),
            Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-35.01", dokumentKategori = "SOK"), // TODO: Riktig kode er: NAV 09-09.03
                tittel = "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAV 09-35.01",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.SØKNAD
            ),
            Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAVe 09-35.01", dokumentKategori = "SOK"), // TODO: Riktig kode er: NAVe 09-09.03
                tittel = "Søknad om utbetaling av omsorgsdager frilanser/selvstendig - NAVe 09-35.01",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.ETTERSENDELSE
            ),
            Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-35.02", dokumentKategori = "SOK"), // TODO: Riktig kode er: NAV 09-09.01
                tittel = "Søknad om utbetaling av omsorgspenger for arbeidstakere - NAV 09-35.02",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.SØKNAD
            ),
            Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAVe 09-35.02", dokumentKategori = "SOK"), // TODO: Riktig kode er: NAVe 09-09.01
                tittel = "Søknad om utbetaling av omsorgspenger for arbeidstakere - NAVe 09-35.02",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.ETTERSENDELSE
            ),
            Søknadstype.OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
                tittel = "Søknad om overføring av omsorgsdager - NAV 09-06.08",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.MELDING
            ),
            Søknadstype.OMSORGSPENGEMELDING_DELING_AV_DAGER to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-06.08", dokumentKategori = "SOK"),
                tittel = "Melding om deling av omsorgsdager - NAV 09-06.08",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.MELDING
            ),
            Søknadstype.OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAVe 09-06.08", dokumentKategori = "SOK"),
                tittel = "Melding om deling av omsorgsdager - NAVe 09-06.08",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.ETTERSENDELSE
            ),
            Søknadstype.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-06.07", dokumentKategori = "SOK"),
                tittel = "Søknad om ekstra omsorgsdager når den andre forelderen ikke kan ha tilsyn med barn - NAV 09-06.07",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.SØKNAD
            ),
            Søknadstype.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAVe 09-06.07", dokumentKategori = "SOK"),
                tittel = "Søknad om å bli regnet som alene  - NAVe 09-06.07",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.ETTERSENDELSE
            ),
            Søknadstype.OMSORGSDAGER_ALENEOMSORG to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-06.10", dokumentKategori = "SOK"),
                tittel = "Registrering av aleneomsorg for omsorgsdager - NAV 09-06.10",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.MELDING
            ),
            Søknadstype.OPPLÆRINGSPENGESØKNAD to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 09-11.08", dokumentKategori = "SOK"),
                tittel = "Søknad om opplæringspenger - NAV 09-11.08",
                tema = Kapittel9Ytelse,
                innsendingstype = Innsendingstype.SØKNAD
            ),
            Søknadstype.FRISINNSØKNAD to Journalpostinfo(
                brevkode = BrevKode(brevKode = "NAV 00-03.02", dokumentKategori = "SOK"),
                tittel = "Søknad om inntektskompensasjon for frilansere og selvstendig næringdrivende - NAV 00-03.02",
                tema = FrilansereOgSelvstendigNæringdrivendesInntektskompensasjon,
                innsendingstype = Innsendingstype.SØKNAD
            )
        )
        internal fun Søknadstype.somJournalpostinfo() = journalpostinfo[this]
            ?: throw IllegalStateException("Mangler journalpostinfo for søknadstype $this.")
    }
}
