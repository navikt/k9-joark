package no.nav.helse

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import no.nav.helse.journalforing.v1.Søknadstype.*
import org.json.JSONObject

internal class DokarkivResponseTransformer : ResponseTransformerV2 {
    companion object {
        val BREVKODE_MED_FORVENTET_JOURNALPOST_ID = mapOf(
            PLEIEPENGESØKNAD to "1",
            PLEIEPENGESØKNAD_ENDRINGSMELDING to "1",
            OMSORGSPENGESØKNAD to "2",
            OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG to "3",
            OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER to "4",
            OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER to "5",
            OMSORGSPENGEMELDING_DELING_AV_DAGER to "5",
            OPPLÆRINGSPENGESØKNAD to "6",
            FRISINNSØKNAD to "7",
            OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE to "8",
            PLEIEPENGESØKNAD_ETTERSENDING to "9",
            OMSORGSPENGESØKNAD_ETTERSENDING to "10",
            OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING to "11",
            OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING to "12",
            OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING to "13",
            OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING to "14",
            OMSORGSDAGER_ALENEOMSORG to "15",
            PLEIEPENGESØKNAD_LIVETS_SLUTTFASE to "16",
            PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING to "17",
            OMSORGSDAGER_ALENEOMSORG_ETTERSENDING to "18",
            UNGDOMSYTELSE_SØKNAD to "19", // TODO Bruk Tema.UNGDOMSYTELSE før lansering
            UNGDOMSYTELSE_ENDRINGSSØKNAD to "20" // TODO Bruk Tema.UNGDOMSYTELSE før lansering
        )
    }

    override fun getName(): String {
        return "dokarkiv"
    }

    override fun transform(response: Response, serveEvent: ServeEvent): Response {
        val requestEntity = serveEvent.request.bodyAsString
        val tema = JSONObject(requestEntity).getString("tema")

        val journalpostId = BREVKODE_MED_FORVENTET_JOURNALPOST_ID.entries.firstOrNull {
            val søknadstype = it.key
            requestEntity.contains(søknadstype.brevkode.brevKode) && (søknadstype.tema.kode == tema)
        }?.value ?: throw IllegalArgumentException("Ikke støttet brevkode.")

        return Response.Builder.like(response)
            .body(getResponse(journalpostId))
            .build()
    }

    override fun applyGlobally(): Boolean {
        return false
    }
}

private fun getResponse(journalpostId: String) =
    """       
        {
          "journalpostId": "$journalpostId",
          "journalstatus": "M",
          "melding": null,
          "journalpostferdigstilt": false,
          "dokumenter": [
            {
              "dokumentInfoId": "485201432"
            },
            {
              "dokumentInfoId": "485201433"
            }
          ]
        }
    """.trimIndent()
