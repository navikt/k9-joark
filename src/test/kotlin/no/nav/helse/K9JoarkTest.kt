package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.journalforing.v1.MeldingV1
import no.nav.helse.journalforing.v1.Navn
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class K9JoarkTest {

    private companion object {
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
    fun `Journalpost for pleiepengesøknad`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"1"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created
        )
    }

    @Test
    fun `Journalpost for pleiepenger livets sluttfase`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"16"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/pleiepenge/livets-sluttfase/journalforing"
        )
    }

    @Test
    fun `Journalpost for pleiepenger livets sluttfase ettersending`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"17"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/pleiepenge/livets-sluttfase/ettersending/journalforing"
        )
    }

    @Test
    fun `Journalpost for pleiepengesøknad ettersending`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"9"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/pleiepenge/ettersending/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgpengesøknad`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"2"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspenge/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgpengesøknad ettersending`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"10"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspenge/ettersending/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgspengeutbetaling for frilansere og selvstendig næringsdrivende`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"3"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspengeutbetaling/journalforing?arbeidstype=frilanser&arbeidstype=selvstendig næringsdrivende"
        )
    }

    @Test
    fun `Journalpost for omsorgspengeutbetaling ettersending for frilansere og selvstendig næringsdrivende`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"11"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspengeutbetaling/ettersending/journalforing?arbeidstype=frilanser&arbeidstype=selvstendig næringsdrivende"
        )
    }

    @Test
    fun `Journalpost for omsorgpengesøknad for overføring av dager`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"5"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgsdageroverforing/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgspengemelding for deling av dager`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"5"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgsdagerdeling/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgspengemelding ettersending for deling av dager`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"14"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgsdagerdeling/ettersending/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgspengeutbetaling for arbeidstakere`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"4"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspengeutbetaling/journalforing?arbeidstype=arbeidstaker"
        )
    }

    @Test
    fun `Journalpost for omsorgspengeutbetaling ettersending for arbeidstakere`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"12"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspengeutbetaling/ettersending/journalforing?arbeidstype=arbeidstaker"
        )
    }

    @Test
    fun `Journalpost for opplæringspengesøknad`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"6"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/opplæringspenge/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgspenger - midlertidig alene`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"8"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspenger/midlertidig-alene/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgspenger ettersending - midlertidig alene`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"13"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspenger/midlertidig-alene/ettersending/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgsdager aleneomsorg`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"15"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgsdager/aleneomsorg/journalforing"
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