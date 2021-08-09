package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.journalforing.v1.MeldingV1
import no.nav.helse.journalforing.v1.Navn
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class K9JoarkTest {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9JoarkTest::class.java)

        private val wireMockServer: WireMockServer = WireMockBuilder()
            .withNaisStsSupport()
            .withAzureSupport()
            .wireMockConfiguration {
                it.extensions(DokarkivResponseTransformer())
            }
            .build()
            .stubGetDokument()
            .stubGetDokumentFraK9Mellomlagring("12345678910")
            .stubDomotInngaaendeIsReady()
            .stubMottaInngaaendeForsendelseOk()

        private val objectMapper = jacksonObjectMapper().k9JoarkConfigured()
        private val authorizedAccessToken =
            Azure.V1_0.generateJwt(
                clientId = "hvilkem-som-helst-authorized-client-ia-aad-iac",
                audience = "pleiepenger-joark"
            )

        fun getConfig(): ApplicationConfig {
            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer
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
            request = meldingForJournalføring(
                søkerNavn = Navn(
                    fornavn = "Peie",
                    mellomnavn = "penge",
                    etternavn = "Sen"
                )
            ),
            expectedResponse = """{"journal_post_id":"1"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created
        )
    }

    @Test
    fun `Journalpost for pleiepengesøknad ettersending`() {
        requestAndAssert(
            request = meldingForJournalføring(
                søkerNavn = Navn(
                    fornavn = "Peie",
                    mellomnavn = "penge",
                    etternavn = "Sen"
                )
            ),
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
    fun `Journalpost for frisinnsøknad`() {
        requestAndAssert(
            request = meldingForJournalføring(),
            expectedResponse = """{"journal_post_id":"7"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/frisinn/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgspenger - midlertidig alene`() {
        requestAndAssert(
            request = meldingForJournalføringMedDokumenterFraK9MellomLagring(
                søkerNavn = Navn(
                    fornavn = "Peie",
                    mellomnavn = "penge",
                    etternavn = "Sen"
                ),
                norskIdent = "12345678910"
            ),
            expectedResponse = """{"journal_post_id":"8"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspenger/midlertidig-alene/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgspenger ettersending - midlertidig alene`() {
        requestAndAssert(
            request = meldingForJournalføringMedDokumenterFraK9MellomLagring(
                søkerNavn = Navn(
                    fornavn = "Peie",
                    mellomnavn = "penge",
                    etternavn = "Sen"
                ),
                norskIdent = "12345678910"
            ),
            expectedResponse = """{"journal_post_id":"13"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgspenger/midlertidig-alene/ettersending/journalforing"
        )
    }

    @Test
    fun `Journalpost for omsorgsdager aleneomsorg`() {
        requestAndAssert(
            request = meldingForJournalføringMedDokumenterFraK9MellomLagring(
                søkerNavn = Navn(
                    fornavn = "Peie",
                    mellomnavn = "penge",
                    etternavn = "Sen"
                ),
                norskIdent = "12345678910"
            ),
            expectedResponse = """{"journal_post_id":"15"}""".trimIndent(),
            expectedCode = HttpStatusCode.Created,
            uri = "/v1/omsorgsdager/aleneomsorg/journalforing"
        )
    }

    @Test
    fun `melding uten correlation id skal feile`() {
        val request = MeldingV1(
            norskIdent = "12345",
            mottatt = ZonedDateTime.now(),
            dokumenter = listOf(
                listOf(
                    getDokumentUrl("1234"),
                    getDokumentUrl("5678")
                )
            ),
            aktoerId = "12345",
            sokerNavn = Navn(
                fornavn = "ole",
                etternavn = "Nordmann"
            )
        )


        requestAndAssert(
            request = request,
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
        val request = MeldingV1(
            norskIdent = "12345",
            mottatt = ZonedDateTime.now(),
            dokumenter = listOf(
                listOf(
                    getDokumentUrl("1234"),
                    getDokumentUrl("5678")
                )
            ),
            aktoerId = "12345",
            sokerNavn = Navn(
                fornavn = "ole",
                etternavn = "Nordmann"
            )
        )

        requestAndAssert(
            request = request,
            leggTilAuthorization = false,
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null
        )
    }

    @Test
    fun `request fra ikke tillatt system`() {
        val request = MeldingV1(
            norskIdent = "12345",
            mottatt = ZonedDateTime.now(),
            dokumenter = listOf(
                listOf(
                    getDokumentUrl("1234"),
                    getDokumentUrl("5678")
                )
            ),
            aktoerId = "12345",
            sokerNavn = Navn(
                fornavn = "ole",
                etternavn = "Nordmann"
            )
        )

        val feilAuidence = Azure.V2_0.generateJwt(
            clientId = "hvilen-som-helst-app",
            audience = "feil-audience"
        )

        val ikkeAuthorizedApplication = Azure.V2_0.generateJwt(
            clientId = "hvilen-som-helst-app",
            audience = "pleiepenger-joark",
            accessAsApplication = false
        )

        val forventetResponse = """
            {
                "type": "/problem-details/unauthorized",
                "title": "unauthorized",
                "status": 403,
                "detail": "Requesten inneholder ikke tilstrekkelige tilganger.",
                "instance": "about:blank"
            }
            """.trimIndent()

        requestAndAssert(
            request = request,
            expectedCode = HttpStatusCode.Forbidden,
            accessToken = ikkeAuthorizedApplication,
            expectedResponse = forventetResponse
        )

        requestAndAssert(
            request = request,
            expectedCode = HttpStatusCode.Forbidden,
            accessToken = feilAuidence,
            expectedResponse = forventetResponse
        )
    }

    @Test
    fun `melding uten dokumenter skal feile`() {
        val request = MeldingV1(
            norskIdent = "012345678901F",
            mottatt = ZonedDateTime.now(),
            dokumenter = listOf(),
            aktoerId = "12345F",
            sokerNavn = Navn(
                fornavn = "ole",
                etternavn = "Nordmann"
            )
        )

        requestAndAssert(
            request = request,
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
                    "name": "dokument",
                    "reason": "Det må sendes minst ett dokument",
                    "invalid_value": []
                },
                {
                    "type": "entity",
                    "name": "aktoer_id",
                    "reason": "Ugyldig AktørID. Kan kun være siffer.",
                    "invalid_value": "12345F"
                },
                {
                    "type": "entity",
                    "name": "norsk_ident",
                    "reason": "Ugyldig Norsk Ident. Kan kun være siffer.",
                    "invalid_value": "012345678901F"
                }]          
            }
            """.trimIndent()
        )
    }

    @Test
    fun `melding med tomme dokumentbolker skal feile`() {
        val request = MeldingV1(
            norskIdent = "012345678901",
            mottatt = ZonedDateTime.now(),
            dokumenter = listOf(
                listOf(getDokumentUrl("1234")),
                listOf()
            ),
            aktoerId = "12345",
            sokerNavn = Navn(
                fornavn = "ole",
                etternavn = "Nordmann"
            )
        )

        requestAndAssert(
            request = request,
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
                            "name" : "dokument_bolk",
                            "reason" : "Det må være minst et dokument i en dokument bolk.",
                            "type": "entity",
                            "invalid_value": []
                        }
                    ]
                }
            """.trimIndent()
        )
    }

    private fun getDokumentUrl(dokumentId: String) = URI("${wireMockServer.getK9DokumentUrl()}/$dokumentId")
    private fun getK9MellomlagringDokumentUrl(dokumentId: String) = URI("${wireMockServer.getK9MellomlagringUrl()}/$dokumentId")

    private fun requestAndAssert(
        request: MeldingV1,
        expectedResponse: String?,
        expectedCode: HttpStatusCode,
        leggTilCorrelationId: Boolean = true,
        leggTilAuthorization: Boolean = true,
        accessToken: String = authorizedAccessToken,
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
        søkerNavn: Navn? = null
    ): MeldingV1 {
        val jpegDokumentId = "1234" // Default mocket som JPEG
        val pdfDokumentId = "4567"
        stubGetDokumentPdf(pdfDokumentId)
        val jsonDokumentId = "78910"
        stubGetDokumentJson(jsonDokumentId)

        return MeldingV1(
            norskIdent = "012345678901",
            mottatt = ZonedDateTime.now(),
            dokumenter = listOf(
                listOf(
                    getDokumentUrl(pdfDokumentId),
                    getDokumentUrl(jsonDokumentId)
                ),
                listOf(
                    getDokumentUrl(jpegDokumentId)
                )
            ),
            aktoerId = "12345",
            sokerNavn = søkerNavn
        )
    }

    private fun meldingForJournalføringMedDokumenterFraK9MellomLagring(
        søkerNavn: Navn? = null,
        norskIdent: String,
    ): MeldingV1 {
        val jpegDokumentId = "1234" // Default mocket som JPEG
        val pdfDokumentId = "4567"
        stubGetDokumentPdfFraK9Mellomlagring(norskIdent, pdfDokumentId)
        val jsonDokumentId = "78910"
        stubGetDokumentJsonFraK9Mellomlagring(norskIdent, jsonDokumentId)

        return MeldingV1(
            norskIdent = norskIdent,
            mottatt = ZonedDateTime.now(),
            dokumenter = listOf(
                listOf(
                    getK9MellomlagringDokumentUrl(pdfDokumentId),
                    getK9MellomlagringDokumentUrl(jsonDokumentId)
                ),
                listOf(
                    getK9MellomlagringDokumentUrl(jpegDokumentId)
                )
            ),
            sokerNavn = søkerNavn
        )
    }
}
