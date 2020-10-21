package no.nav.helse

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.jackson.jackson
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.dokument.ContentTypeService
import no.nav.helse.dokument.DokumentGateway
import no.nav.helse.dokument.DokumentService
import no.nav.helse.dokument.mellomlagring.K9MellomlagringGateway
import no.nav.helse.dusseldorf.ktor.auth.*
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

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
fun Application.k9Joark() {
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    val configuration = Configuration(environment.config)
    val issuers = configuration.issuers()

    install(Authentication) {
        multipleJwtIssuers(issuers)
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
        accessTokenClient = accessTokenClientResolver.joark(),
        oppretteJournalPostScopes = configuration.getOppretteJournalpostScopes()
    )

    val dokumentGateway = DokumentGateway(
        accessTokenClient = accessTokenClientResolver.k9Dokument(),
        henteDokumentScopes = configuration.getHenteDokumentScopes()
    )

    val k9MellomLagringGateway = K9MellomlagringGateway(
        accessTokenClient = accessTokenClientResolver.k9Dokument(),
        k9MellomlagringScope = configuration.getK9MellomlagringScopes()
    )

    val contentTypeService = ContentTypeService()

    val dokumentService = DokumentService(
        dokumentGateway = dokumentGateway,
        k9MellomlagringGateway = k9MellomLagringGateway,
        image2PDFConverter = Image2PDFConverter(),
        contentTypeService = contentTypeService
    )

    val healthService = HealthService(setOf(
        journalforingGateway,
        dokumentGateway,
        k9MellomLagringGateway,
        HttpRequestHealthCheck(
            mapOf(
                Url.buildURL(baseUrl = configuration.getDokarkivBaseUrl(), pathParts = listOf("isReady")) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK)
            )
        ))
    )

    install(CallIdRequired)

    install(Routing) {
        authenticate(*issuers.allIssuers()) {
            requiresCallId {
                journalforingApis(
                    journalforingV1Service = JournalforingV1Service(
                        journalforingGateway = journalforingGateway,
                        dokumentService = dokumentService
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
    }
}

internal fun ObjectMapper.k9JoarkConfigured() = dusseldorfConfigured()
    .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
