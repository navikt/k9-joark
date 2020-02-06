package no.nav.helse.journalforing.gateway

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpPost
import io.ktor.http.Url
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.net.URI

/*
    https://dokarkiv-q1.nais.preprod.local/rest/mottaInngaaendeForsendelse
                                                  /rest/journalpostapi/v1/journalpost?foersoekFerdigstill=true/false
 */

class JournalforingGateway(
    baseUrl: URI,
    private val accessTokenClient: AccessTokenClient,
    private val oppretteJournalPostScopes: Set<String>
) : HealthCheck {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(JournalforingGateway::class.java)
    }

    private val mottaInngaaendeForsendelseUrl = Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf("rest", "journalpostapi", "v1", "journalpost")
    ).toString()

    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)
    private val objectMapper = configuredObjectMapper()


    override suspend fun check(): Result {
        return try {
            accessTokenClient.getAccessToken(oppretteJournalPostScopes)
            Healthy("JournalforingGateway", "Henting av access token for opprettelse av journalpost OK.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for opprettelse av journalpost", cause)
            UnHealthy("JournalforingGateway", "Henting av access token for opprettelse av journalpost Feilet.")
        }
    }

    internal suspend fun jorunalfor(journalPostRequest: JournalPostRequest): JournalPostResponse {
        val authorizationHeader =
            cachedAccessTokenClient.getAccessToken(oppretteJournalPostScopes).asAuthoriationHeader()

        logger.trace("Genererer body for request")
        val body = objectMapper.writeValueAsBytes(journalPostRequest)
        val contentStream = { ByteArrayInputStream(body) }
        logger.trace("Generer http request")
        val httpRequest = mottaInngaaendeForsendelseUrl
            .httpPost()
            .body(contentStream)
            .header(
                Headers.AUTHORIZATION to authorizationHeader,
                Headers.CONTENT_TYPE to "application/json",
                Headers.ACCEPT to "application/json"
            )

        logger.trace("Sender request")
        val (request, _, result) = Operation.monitored(
            app = "k9-joark",
            operation = "opprette-journalpost",
            resultResolver = { 200 == it.second.statusCode }
        ) { httpRequest.awaitStringResponseResult() }

        logger.trace("Håndterer response")

        return result.fold(
            { success -> objectMapper.readValue(success) },
            { error ->
                logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                logger.error(error.toString())
                throw IllegalStateException("Feil ved opperttelse av jorunalpost.")
            }
        )
    }

    private fun configuredObjectMapper(): ObjectMapper {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }
}
