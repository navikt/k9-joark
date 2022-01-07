package no.nav.helse.dokument.mellomlagring

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpPost
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.helse.CorrelationId
import no.nav.helse.dokument.Dokument
import no.nav.helse.dokument.DokumentEier
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.helse.journalforing.Fodselsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.net.URI
import java.time.Duration

class K9MellomlagringGateway(
    private val accessTokenClient: AccessTokenClient,
    private val k9MellomlagringScope: Set<String>,
    private val k9MellomlagringBaseUrl: URI
): HealthCheck {

    private val objectMapper = configuredObjectMapper()
    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9MellomlagringGateway::class.java)
        private const val HENTE_DOKUMENT_OPERATION = "hente-dokument-fra-k9-mellomlagring"
        private const val HENTE_ALLE_DOKUMENTER_OPERATION = "hente-alle-dokumenter-fra-k9-mellomlagring"
    }

    override suspend fun check(): Result {
        return try {
            accessTokenClient.getAccessToken(k9MellomlagringScope)
            Healthy("K9MellomlagringGateway", "Henting av access token for henting av dokument OK.")
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av access token for henting av dokument", cause)
            UnHealthy("K9MellomlagringGateway", "Henting av access token for henting av dokument Feilet.")
        }
    }

    suspend fun hentDokumenter(
        urls : List<URI>,
        eiersFodselsnummer: Fodselsnummer,
        correlationId: CorrelationId
    ) : List<Dokument> {
        val authorizationHeader = cachedAccessTokenClient.getAccessToken(k9MellomlagringScope).asAuthoriationHeader()

        return Operation.monitored(
            app = "k9-joark",
            operation = HENTE_ALLE_DOKUMENTER_OPERATION
        ) {
            coroutineScope {
                val futures = mutableListOf<Deferred<Dokument>>()
                urls.forEach { url ->
                    futures.add(async {
                        hentDokument(url, eiersFodselsnummer, authorizationHeader, correlationId)
                    })
                }
                futures.awaitAll()
            }
        }
    }

    suspend fun hentDokumenterMedDokumentId(
        dokumentId : List<String>,
        eiersFodselsnummer: Fodselsnummer,
        correlationId: CorrelationId
    ) : List<Dokument> {
        val authorizationHeader = cachedAccessTokenClient.getAccessToken(k9MellomlagringScope).asAuthoriationHeader()

        return Operation.monitored(
            app = "k9-joark",
            operation = HENTE_ALLE_DOKUMENTER_OPERATION
        ) {
            coroutineScope {
                val futures = mutableListOf<Deferred<Dokument>>()
                dokumentId.forEach { dokumentId ->
                    val komplettUrl = Url.buildURL(
                        baseUrl = k9MellomlagringBaseUrl,
                        pathParts = listOf("v1", "dokument", dokumentId)
                    )

                    futures.add(async {
                        hentDokument(komplettUrl, eiersFodselsnummer, authorizationHeader, correlationId)
                    })
                }
                futures.awaitAll()
            }
        }
    }

    private suspend fun hentDokument(
        url: URI,
        eiersFodselsnummer: Fodselsnummer,
        authorizationHeader: String,
        correlationId: CorrelationId
    ) : Dokument {
        val body = objectMapper.writeValueAsBytes(DokumentEier(eiersFodselsnummer.value))
        val contentStream = { ByteArrayInputStream(body) }

        val httpRequst = url.toString()
            .httpPost()
            .timeout(20_000)
            .timeoutRead(20_000)
            .body(contentStream)
            .header(
                Headers.CONTENT_TYPE to "application/json",
                Headers.ACCEPT to "application/json",
                Headers.AUTHORIZATION to authorizationHeader,
                HttpHeaders.XCorrelationId to correlationId.id
            )

        return Retry.retry(
            operation = HENTE_DOKUMENT_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0
        ) {
            val (request, _, result ) = Operation.monitored(
                app = "k9-joark",
                operation = HENTE_DOKUMENT_OPERATION,
                resultResolver = { 200 == it.second.statusCode}
            ) { httpRequst.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<Dokument>(success)},
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av dokument.")
                }
            )
        }
    }

    private fun configuredObjectMapper() : ObjectMapper {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        return objectMapper
    }
}
