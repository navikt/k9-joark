package no.nav.helse.journalforing.api

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.PipelineContext
import no.nav.helse.journalforing.v1.JournalforingV1Service
import no.nav.helse.journalforing.v1.MeldingV1
import no.nav.helse.journalforing.v1.MetadataV1
import no.nav.helse.journalforing.v1.Søknadstype
import no.nav.helse.journalforing.v1.Søknadstype.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("no.nav.journalforingApis")

fun Route.journalforingApis(journalforingV1Service: JournalforingV1Service) {

    val isEnabled = Søknadstype.enabled().also { it.forEach { (søknadstype, enabled) ->
        logger.info("Enabled[${søknadstype.name}]=$enabled")
    }}

    suspend fun PipelineContext<Unit, ApplicationCall>.journalfør(
        melding: MeldingV1,
        metadata: MetadataV1) = when (isEnabled.getValue(metadata.søknadstype)) {
        true -> {
            val callId = call.callId!!
            val journalPostId = journalforingV1Service.journalfor(melding = melding, metaData = metadata, callId)
            call.respond(HttpStatusCode.Created, JournalforingResponse(journalPostId = journalPostId.value))
        }
        false -> {
            logger.warn("Opprettelse av journalpost for ${metadata.søknadstype.name} er skrudd av.")
            call.respond(HttpStatusCode.ServiceUnavailable)
        }
    }

    post(PLEIEPENGESØKNAD.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(PLEIEPENGESØKNAD)
        journalfør(melding, metadata)
    }

    post(PLEIEPENGESØKNAD_ENDRINGSMELDING.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(PLEIEPENGESØKNAD_ENDRINGSMELDING)
        journalfør(melding, metadata)
    }

    post(PLEIEPENGESØKNAD_ETTERSENDING.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(PLEIEPENGESØKNAD_ETTERSENDING)
        journalfør(melding, metadata)
    }

    post(PLEIEPENGESØKNAD_LIVETS_SLUTTFASE.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(PLEIEPENGESØKNAD_LIVETS_SLUTTFASE)
        journalfør(melding, metadata)
    }

    post(PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING)
        journalfør(melding, metadata)
    }


    post(OMSORGSPENGESØKNAD.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OMSORGSPENGESØKNAD)
        journalfør(melding, metadata)
    }

    post(OMSORGSPENGESØKNAD_ETTERSENDING.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OMSORGSPENGESØKNAD_ETTERSENDING)
        journalfør(melding, metadata)
    }

    post("/v1/omsorgspengeutbetaling/journalforing") {
        when {
            call.request.gjelderFrilanserOgSelvstendigNæringsdrivende() -> {
                val melding = call.receive<MeldingV1>()
                val metadata = call.genererMetadata(OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG)
                journalfør(melding, metadata)
            }
            call.request.gjelderArbeidstaker() -> {
                val melding = call.receive<MeldingV1>()
                val metadata = call.genererMetadata(OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER)
                journalfør(melding, metadata)
            }
            else -> call.response.status(HttpStatusCode.NotFound)
        }
    }

    post("/v1/omsorgspengeutbetaling/ettersending/journalforing") {
        when {
            call.request.gjelderFrilanserOgSelvstendigNæringsdrivende() -> {
                val melding = call.receive<MeldingV1>()
                val metadata = call.genererMetadata(OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING)
                journalfør(melding, metadata)
            }
            call.request.gjelderArbeidstaker() -> {
                val melding = call.receive<MeldingV1>()
                val metadata = call.genererMetadata(OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING)
                journalfør(melding, metadata)
            }
            else -> call.response.status(HttpStatusCode.NotFound)
        }
    }

    post(OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER)
        journalfør(melding, metadata)
    }

    post(OMSORGSPENGEMELDING_DELING_AV_DAGER.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OMSORGSPENGEMELDING_DELING_AV_DAGER)
        journalfør(melding, metadata)
    }

    post(OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING)
        journalfør(melding, metadata)
    }

    post(OPPLÆRINGSPENGESØKNAD.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OPPLÆRINGSPENGESØKNAD)
        journalfør(melding, metadata)
    }

    post(FRISINNSØKNAD.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(FRISINNSØKNAD)
        journalfør(melding, metadata)
    }

    post(OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE)
        journalfør(melding, metadata)
    }

    post(OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING)
        journalfør(melding, metadata)
    }

    post(OMSORGSDAGER_ALENEOMSORG.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OMSORGSDAGER_ALENEOMSORG)
        journalfør(melding, metadata)
    }

    post(OMSORGSDAGER_ALENEOMSORG_ETTERSENDING.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(OMSORGSDAGER_ALENEOMSORG_ETTERSENDING)
        journalfør(melding, metadata)
    }

    post(UNGDOMSYTELSE_SØKNAD.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(UNGDOMSYTELSE_SØKNAD)
        journalfør(melding, metadata)
    }

    post(UNGDOMSYTELSE_ENDRINGSSØKNAD.urlPath) {
        val melding = call.receive<MeldingV1>()
        val metadata = call.genererMetadata(UNGDOMSYTELSE_ENDRINGSSØKNAD)
        journalfør(melding, metadata)
    }
}

private fun ApplicationCall.genererMetadata(søknadstype: Søknadstype, version: Int = 1) = MetadataV1(
    version = version,
    correlationId = this.request.getCorrelationId(),
    requestId = this.response.getRequestId(),
    søknadstype = søknadstype
)

private fun ApplicationRequest.gjelderFrilanserOgSelvstendigNæringsdrivende() : Boolean {
    val arbeidstyper = queryParameters.getAll("arbeidstype")?: emptyList()
    return arbeidstyper.size == 2 && arbeidstyper.contains("frilanser") && arbeidstyper.contains("selvstendig-naeringsdrivende")
}

private fun ApplicationRequest.gjelderArbeidstaker() : Boolean {
    val arbeidstyper = queryParameters.getAll("arbeidstype")?: emptyList()
    return arbeidstyper.size == 1 && arbeidstyper.contains("arbeidstaker")
}

private fun ApplicationRequest.getCorrelationId(): String {
    return header(HttpHeaders.XCorrelationId) ?: throw IllegalStateException("Correlation ID ikke satt")
}

private fun ApplicationResponse.getRequestId(): String? {
    return headers[HttpHeaders.XRequestId]
}

data class JournalforingResponse(val journalPostId: String)
