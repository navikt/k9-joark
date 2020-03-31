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
import no.nav.helse.journalforing.gateway.JournalforingGateway
import no.nav.helse.journalforing.v1.JournalforingV1Service
import no.nav.helse.journalforing.v1.MeldingV1
import no.nav.helse.journalforing.v1.MetadataV1
import no.nav.helse.journalforing.v1.Søknadstype
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(JournalforingGateway::class.java)
fun Route.journalforingApis(
    journalforingV1Service: JournalforingV1Service
) {

    post("/v1/pleiepenge/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.PLEIEPENGESØKNAD)
        journalfør(journalforingV1Service, melding, metadata)
    }
    post("/v1/omsorgspenge/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OMSORGSPENGESØKNAD)
        journalfør(journalforingV1Service, melding, metadata)
    }
    post("/v1/omsorgspengeutbetaling/journalforing") {
        if (call.request.gjelderFrilanserOgSelvstendigNæringsdrivende()) {
            val melding = call.receive<MeldingV1>()
            val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG)
            journalfør(journalforingV1Service, melding, metadata)
        } else {
            call.response.status(HttpStatusCode.NotFound)
        }
    }
    post("/v1/omsorgsdageroverforing/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER)
        journalfør(journalforingV1Service, melding, metadata)
    }
    post("/v1/opplæringspenge/journalforing") {
        val melding = call.receive<MeldingV1>()
        val metadata = MetadataV1(version = 1, correlationId = call.request.getCorrelationId(), requestId = call.response.getRequestId(), søknadstype = Søknadstype.OPPLÆRINGSPENGESØKNAD)
        journalfør(journalforingV1Service, melding, metadata)
    }
}

private fun ApplicationRequest.gjelderFrilanserOgSelvstendigNæringsdrivende() : Boolean {
    val arbeidstyper = queryParameters.getAll("arbeidstype")?: emptyList()
    return arbeidstyper.size == 2 && arbeidstyper.contains("frilanser") && arbeidstyper.contains("selvstendig næringsdrivende")
}

private suspend fun PipelineContext<Unit, ApplicationCall>.journalfør(
    journalforingV1Service: JournalforingV1Service,
    melding: MeldingV1,
    metadata: MetadataV1
) {
    val journalPostId = journalforingV1Service.journalfor(melding = melding, metaData = metadata)
    call.respond(HttpStatusCode.Created, JournalforingResponse(journalPostId = journalPostId.value))
}

private fun ApplicationRequest.getCorrelationId(): String {
    return header(HttpHeaders.XCorrelationId) ?: throw IllegalStateException("Correlation ID ikke satt")
}

private fun ApplicationResponse.getRequestId(): String? {
    return headers[HttpHeaders.XRequestId]
}

data class JournalforingResponse(val journalPostId: String)