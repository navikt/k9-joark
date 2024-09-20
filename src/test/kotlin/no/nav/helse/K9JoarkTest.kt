package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.DokarkivResponseTransformer.Companion.BREVKODE_MED_FORVENTET_JOURNALPOST_ID
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.journalforing.v1.MeldingV1
import no.nav.helse.journalforing.v1.Navn
import no.nav.helse.journalforing.v1.Søknadstype
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class K9JoarkTest {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9JoarkTest::class.java)
        private val mockOAuth2Server = MockOAuth2Server().apply { start() }
        private val wireMockServer: WireMockServer = WireMockBuilder()
            .withPort(53854)
            .withNaisStsSupport()
            .withAzureSupport()
            .wireMockConfiguration {
                it.extensions(DokarkivResponseTransformer())
            }
            .build()
            .stubGetDokumentFraK9Mellomlagring("012345678901")
            .stubDomotInngaaendeIsReady()
            .stubMottaInngaaendeForsendelseOk()

        private val objectMapper = jacksonObjectMapper().k9JoarkConfigured()
        private val azureToken = mockOAuth2Server.issueToken(
            issuerId = "azure",
            audience = "dev-gcp:dusseldorf:k9-joark",
            claims = mapOf("roles" to "access_as_application")
        ).serialize()

        fun getConfig(): ApplicationConfig {
            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    mockOAuth2Server = mockOAuth2Server
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }

        val engine = TestApplicationEngine(createTestEnvironment {
            config = getConfig()
        })

        @BeforeAll
        @JvmStatic
        fun buildUp() {
            engine.start(wait = true)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            mockOAuth2Server.shutdown()
            logger.info("Tear down complete")
        }

        @JvmStatic
        fun søknaderForJournalføring(): List<Journalføring> {
            return Søknadstype.entries.map {
                val journalpostId = BREVKODE_MED_FORVENTET_JOURNALPOST_ID.getValue(it)

                val (urlPath, forventetJournalpostId) = when(it) {
                    Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG -> it.urlPath + "?arbeidstype=frilanser&arbeidstype=selvstendig-naeringsdrivende" to journalpostId
                    Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER -> it.urlPath + "?arbeidstype=arbeidstaker" to journalpostId
                    Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING -> it.urlPath + "?arbeidstype=frilanser&arbeidstype=selvstendig-naeringsdrivende" to journalpostId
                    Søknadstype.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING -> it.urlPath + "?arbeidstype=arbeidstaker" to journalpostId
                    else -> it.urlPath to journalpostId
                }

                Journalføring(urlPath, forventetJournalpostId)
            }
        }

        data class Journalføring(
            val urlPath: String,
            val forventetJournalpostId: String
        )
    }

    @Test
    fun `test isready, isalive og metrics`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/isalive") {}.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    handleRequest(HttpMethod.Get, "/metrics") {}.apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        handleRequest(HttpMethod.Get, "/health") {}.apply {
                            assertEquals(HttpStatusCode.OK, response.status())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `Journalpost for pleiepengesøknad med 409 conflict skal ikke feile`() {
        wireMockServer.stubMottaInngaaendeForsendelseOk(409)
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"1"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created
        )
        wireMockServer.stubMottaInngaaendeForsendelseOk()
    }

    @ParameterizedTest
    @MethodSource("søknaderForJournalføring")
    fun `Journalføring fungerer som forventet`(journalføring: Journalføring) {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"${journalføring.forventetJournalpostId}"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = journalføring.urlPath
        )
    }

    @Test
    fun `melding uten correlation id skal feile`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            leggTilCorrelationId = false,
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse = """
                {
                    "type": "/problem-details/invalid-request-parameters",
                    "title": "invalid-request-parameters",
                    "detail": "Requesten inneholder ugyldige paramtere.",
                    "status": 400,
                    "instance": "about:blank",
                    "invalid_parameters" : [
                        {
                            "name" : "X-Correlation-ID",
                            "reason" : "Correlation ID må settes.",
                            "type": "header"
                        }
                    ]
                }
            """.trimIndent()
        )
    }

    @Test
    fun `mangler authorization header`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            leggTilAuthorization = false,
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null
        )
    }

    @Test
    fun `request fra ikke tillatt system`() {
        val feilAuidence = mockOAuth2Server.issueToken(
            issuerId = "azure",
            audience = "dev-gcp:dusseldorf:k9-mellomlagring",
            claims = mapOf("roles" to "access_as_application")
        ).serialize()

        val ikkeAuthorizedApplication = mockOAuth2Server.issueToken(
            issuerId = "azure",
            audience = "dev-gcp:dusseldorf:k9-joark",
            claims = mapOf("roles" to "no_access_as_application")
        ).serialize()

        requestAndAssert(
            request = meldingForJournalføring(),
            expectedCode = HttpStatusCode.Unauthorized,
            accessToken = feilAuidence,
            expectedResponse = null
        )

        requestAndAssert(
            request = meldingForJournalføring(),
            expectedCode = HttpStatusCode.Unauthorized,
            accessToken = ikkeAuthorizedApplication,
            expectedResponse = null
        )
    }

    @Test
    fun `melding uten dokumenter skal feile`() {
        requestAndAssert(
            request = meldingForJournalføring().copy(dokumentId = emptyList()),
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse = """
            {
                "type": "/problem-details/invalid-request-parameters",
                "title": "invalid-request-parameters",
                "status": 400,
                "detail": "Requesten inneholder ugyldige paramtere.",
                "instance": "about:blank",
                "invalid_parameters": [{
                    "type": "entity",
                    "name": "dokumentId",
                    "reason": "Det må sendes minst ett dokument",
                    "invalid_value": []
                }]          
            }
            """.trimIndent()
        )
    }

    @Test
    fun `melding med tomme dokumentbolker skal feile`() {
        requestAndAssert(
            request = meldingForJournalføring().copy(
                dokumentId = listOf(listOf("123"), emptyList())
            ),
            expectedCode = HttpStatusCode.BadRequest,
            expectedResponse = """
                {
                    "type": "/problem-details/invalid-request-parameters",
                    "title": "invalid-request-parameters",
                    "detail": "Requesten inneholder ugyldige paramtere.",
                    "status": 400,
                    "instance": "about:blank",
                    "invalid_parameters" : [
                        {
                            "name" : "dokumentId.dokument_bolk",
                            "reason" : "Det må være minst et dokument i en dokument bolk.",
                            "type": "entity",
                            "invalid_value": []
                        }
                    ]
                }
            """.trimIndent()
        )
    }

    private fun requestAndAssert(
        request: MeldingV1,
        expectedResponse: String?,
        expectedCode: HttpStatusCode,
        leggTilCorrelationId: Boolean = true,
        leggTilAuthorization: Boolean = true,
        accessToken: String = azureToken,
        uri: String = "/v1/pleiepenge/journalforing"
    ) {
        with(engine) {
            handleRequest(HttpMethod.Post, uri) {
                if (leggTilAuthorization) {
                    addHeader(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                if (leggTilCorrelationId) {
                    addHeader(HttpHeaders.XCorrelationId, "123156")
                }
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(objectMapper.writeValueAsString(request))
            }.apply {
                logger.info("Response Entity = ${response.content}")
                logger.info("Expected Entity = $expectedResponse")
                assertEquals(expectedCode, response.status())
                if (expectedResponse == null) assertEquals(expectedResponse, response.content)
                else JSONAssert.assertEquals(expectedResponse, response.content!!, true)
            }
        }
    }

    private fun meldingForJournalføring(
        søkerNavn: Navn? = Navn("Ole", "Nordmann", "Noo")
    ): MeldingV1 {
        val jpegDokumentId = "1234" // Default mocket som JPEG
        val pdfDokumentId = "4567"
        val jsonDokumentId = "78910"

        return MeldingV1(
            norskIdent = "012345678901",
            mottatt = ZonedDateTime.now(),
            dokumentId = listOf(
                listOf(pdfDokumentId, jsonDokumentId),
                listOf(jpegDokumentId)
            ),
            sokerNavn = søkerNavn
        )
    }
}
