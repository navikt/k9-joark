package no.nav.helse.journalforing.api

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.ApplicationRequest
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.ApplicationResponse
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext
import no.nav.helse.journalforing.v1.JournalforingV1Service
import no.nav.helse.journalforing.v1.MeldingV1
import no.nav.helse.journalforing.v1.MetadataV1
import no.nav.helse.journalforing.v1.Søknadstype
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
            val journalPostId = journalforingV1Service.journalfor(melding = melding, metaData = metadata)
            call.respond(HttpStatusCode.Created, JournalforingResponse(journalPostId = journalPostId.value))
        }
        false -> {
            logger.warn("Opprettelse av journalpost for ${metadata.søknadstype.name} er skrudd av.")
            call.respond(HttpStatusCode.ServiceUnavailable)
        }
    }

    post("/v1/pleiepenge/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(
            version = 1,
            correlationId = call.request.getCorrelationId(),
            requestId = call.response.getRequestId(),
            søknadstype = Søknadstype.PLEIEPENGESØKNAD
        )
        journalfør(melding, metadata)
    }

    post("/v1/pleiepenge/ettersending/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(
            version = 1,
            correlationId = call.request.getCorrelationId(),
            requestId = call.response.getRequestId(),
            søknadstype = Søknadstype.PLEIEPENGESØKNAD_ETTERSENDING
        )
        journalfør(melding, metadata)
    }

    post("/v1/omsorgspenge/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(
            version = 1,
            correlationId = call.request.getCorrelationId(),
            requestId = call.response.getRequestId(),
            søknadstype = Søknadstype.OMSORGSPENGESØKNAD
        )
        journalfør(melding, metadata)
    }

    post("/v1/omsorgspenge/ettersending/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(
            version = 1,
            correlationId = call.request.getCorrelationId(),
            requestId = call.response.getRequestId(),
            søknadstype = Søknadstype.OMSORGSPENGESØKNAD_ETTERSENDING
        )
        journalfør(melding, metadata)
    }

    post("/v1/omsorgspengeutbetaling/journalforing") {
        when {
            call.request.gjelderFrilanserOgSelvstendigNæringsdrivende() -> {
                val melding = call.receive<MeldingV1>()
                val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG)
                journalfør(melding, metadata)
            }
            call.request.gjelderArbeidstaker() -> {
                val melding = call.receive<MeldingV1>()
                val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER)
                journalfør(melding, metadata)
            }
            else -> call.response.status(HttpStatusCode.NotFound)
        }
    }

    post("/v1/omsorgspengeutbetaling/ettersending/journalforing") {
        when {
            call.request.gjelderFrilanserOgSelvstendigNæringsdrivende() -> {
                val melding = call.receive<MeldingV1>()
                val metadata = MetadataV1(
                    version = 1,
                    correlationId = call.request.getCorrelationId(),
                    requestId = call.response.getRequestId(),
                    søknadstype = Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING
                )
                journalfør(melding, metadata)
            }
            call.request.gjelderArbeidstaker() -> {
                val melding = call.receive<MeldingV1>()
                val metadata = MetadataV1(
                    version = 1,
                    correlationId = call.request.getCorrelationId(),
                    requestId = call.response.getRequestId(),
                    søknadstype = Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING
                )
                journalfør(melding, metadata)
            }
            else -> call.response.status(HttpStatusCode.NotFound)
        }
    }

    post("/v1/omsorgsdageroverforing/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER)
        journalfør(melding, metadata)
    }

    post("/v1/omsorgsdagerdeling/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OMSORGSPENGEMELDING_DELING_AV_DAGER)
        journalfør(melding, metadata)
    }

    post("/v1/omsorgsdagerdeling/ettersending/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(
            version = 1,
            correlationId = call.request.getCorrelationId(),
            requestId = call.response.getRequestId(),
            søknadstype = Søknadstype.OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING
        )
        journalfør(melding, metadata)
    }

    post("/v1/opplæringspenge/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OPPLÆRINGSPENGESØKNAD)
        journalfør(melding, metadata)
    }

    post("/v1/frisinn/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.FRISINNSØKNAD)
        journalfør(melding, metadata)
    }

    post("/v1/omsorgspenger/midlertidig-alene/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE)
        journalfør(melding, metadata)
    }

    post("/v1/omsorgspenger/midlertidig-alene/ettersending/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(
            version = 1,
            correlationId = call.request.getCorrelationId(),
            requestId = call.response.getRequestId(),
            søknadstype = Søknadstype.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING
        )
        journalfør(melding, metadata)
    }
}

private fun ApplicationRequest.gjelderFrilanserOgSelvstendigNæringsdrivende() : Boolean {
    val arbeidstyper = queryParameters.getAll("arbeidstype")?: emptyList()
    return arbeidstyper.size == 2 && arbeidstyper.contains("frilanser") && arbeidstyper.contains("selvstendig næringsdrivende")
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