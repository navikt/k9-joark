package no.nav.helse

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.metrics.micrometer.*
import io.ktor.routing.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.dokument.ContentTypeService
import no.nav.helse.dokument.DokumentService
import no.nav.helse.dokument.mellomlagring.K9MellomlagringGateway
import no.nav.helse.dusseldorf.ktor.auth.AuthStatusPages
import no.nav.helse.dusseldorf.ktor.auth.idToken
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthConfig
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.dusseldorf.ktor.metrics.init
import no.nav.helse.journalforing.api.journalforingApis
import no.nav.helse.journalforing.converter.Image2PDFConverter
import no.nav.helse.journalforing.gateway.JournalforingGateway
import no.nav.helse.journalforing.v1.JournalforingV1Service
import no.nav.security.token.support.ktor.RequiredClaims
import no.nav.security.token.support.ktor.asIssuerProps
import no.nav.security.token.support.ktor.tokenValidationSupport
import org.slf4j.LoggerFactory

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.k9Joark() {
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    val logger = LoggerFactory.getLogger("no.nav.k9.k9Joark")
    val configuration = Configuration(environment.config)
    val allIssuers = environment.config.asIssuerProps().keys

    install(Authentication) {
        allIssuers.forEach { issuer: String ->
            tokenValidationSupport(
                name = issuer,
                config = environment.config,
                requiredClaims = RequiredClaims(
                    issuer = issuer,
                    claimMap = arrayOf("roles=access_as_application")
                )
            )
        }
    }

    install(ContentNegotiation) {
        jackson {
            k9JoarkConfigured()
        }
    }

    install(StatusPages) {
        DefaultStatusPages()
        JacksonStatusPages()
        AuthStatusPages()
    }

    val accessTokenClientResolver = AccessTokenClientResolver(
        clients = configuration.clients()
    )

    val journalforingGateway = JournalforingGateway(
        baseUrl = configuration.getDokarkivBaseUrl(),
        accessTokenClient = accessTokenClientResolver.azureClient(),
        oppretteJournalPostScopes = configuration.getDokarkivScope()
    )

    val k9MellomLagringGateway = K9MellomlagringGateway(
        accessTokenClient = accessTokenClientResolver.azureClient(),
        k9MellomlagringScope = configuration.getK9MellomlagringScopes(),
        k9MellomlagringBaseUrl = configuration.getK9MellomlagringBaseUrl()
    )

    val contentTypeService = ContentTypeService()

    val dokumentService = DokumentService(
        k9MellomlagringGateway = k9MellomLagringGateway,
        image2PDFConverter = Image2PDFConverter(),
        contentTypeService = contentTypeService
    )

    val healthService = HealthService(setOf(
        journalforingGateway,
        k9MellomLagringGateway,
        HttpRequestHealthCheck(
            mapOf(
                Url.buildURL(baseUrl = configuration.getDokarkivBaseUrl(), pathParts = listOf("isReady")) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK)
            )
        ))
    )

    install(CallIdRequired)

    install(Routing) {
        authenticate(*allIssuers.toTypedArray()) {
            requiresCallId {
                journalforingApis(
                    journalforingV1Service = JournalforingV1Service(
                        journalforingGateway = journalforingGateway,
                        dokumentService = dokumentService,
                    )
                )
            }
        }
        MetricsRoute()
        DefaultProbeRoutes()
        HealthRoute(
            healthService = healthService
        )
    }

    install(MicrometerMetrics) {
        init(appId)
    }


    install(CallId) {
        fromXCorrelationIdHeader()
    }

    intercept(ApplicationCallPipeline.Monitoring) {
        call.request.log()
    }

    install(CallLogging) {
        correlationIdAndRequestIdInMdc()
        logRequests()
        mdc("id_token_jti") { call ->
            try {
                val idToken = call.idToken()
                logger.info("Issuer [{}]", idToken.issuer())
                idToken.getId()
            } catch (cause: Throwable) {
                null
            }
        }
    }
}

internal fun ObjectMapper.k9JoarkConfigured() = dusseldorfConfigured()
    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
