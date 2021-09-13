package no.nav.helse

import com.github.kittinunf.fuel.core.Headers
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.helse.dusseldorf.ktor.core.fromResources
import java.util.*

private const val dokarkivBasePath = "/dokarkiv-mock"
private const val dokarkivMottaInngaaendeForsendelsePath = "$dokarkivBasePath/rest/journalpostapi/v1/journalpost"
private const val k9DokumentPath = "/k9-dokument-mock"
private const val k9MellomlagringPath = "/k9-mellomlagring"

internal fun WireMockServer.stubMottaInngaaendeForsendelseOk() : WireMockServer{
    WireMock.stubFor(
        WireMock.post(
            WireMock.urlMatching(".*$dokarkivMottaInngaaendeForsendelsePath"))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[0].nokkel", WireMock.equalTo("k9.kilde")))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[0].verdi", WireMock.equalTo("DIGITAL_SØKNADSDIALOG")))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("dokarkiv")
                )
    )
    return this
}

internal fun WireMockServer.stubDomotInngaaendeIsReady() : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*$dokarkivBasePath/isReady"))
            .willReturn(
                WireMock.aResponse().withStatus(200)
            )
    )
    return this
}

internal fun WireMockServer.stubGetDokument(): WireMockServer {
    val content = Base64.getEncoder().encodeToString("iPhone_6.jpg".fromResources().readBytes())
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$k9DokumentPath.*"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            {
                                "content": "$content",
                                "content_type": "image/jpeg",
                                "title": "Dette er en tittel"
                            }
                        """.trimIndent()
                    )
            )
    )
    return this
}

internal fun stubGetDokumentJson(
    dokumentId: String
) {
    val content = Base64.getEncoder().encodeToString("jwkset.json".fromResources().readBytes())
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$k9DokumentPath.*/$dokumentId"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            {
                                "content": "$content",
                                "content_type": "application/json",
                                "title": "Dette er en tittel"
                            }
                        """.trimIndent()
                    )
            )
    )
}

internal fun stubGetDokumentPdf(
    dokumentId: String
) {
    val content = Base64.getEncoder().encodeToString("test.pdf".fromResources().readBytes())
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$k9DokumentPath.*/$dokumentId"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            {
                                "content": "$content",
                                "content_type": "application/pdf",
                                "title": "Dette er en tittel"
                            }
                        """.trimIndent()
                    )
            )
    )
}

internal fun WireMockServer.stubGetDokumentFraK9Mellomlagring(eiersFødselsnummer: String): WireMockServer {
    val content = Base64.getEncoder().encodeToString("iPhone_6.jpg".fromResources().readBytes())
    WireMock.stubFor(
        WireMock.post(WireMock.urlPathMatching(".*$k9MellomlagringPath.*"))
            .withRequestBody(WireMock.equalToJson("""{"eiers_fødselsnummer": "$eiersFødselsnummer"}""".trimIndent()))
            .withHeader(Headers.CONTENT_TYPE, WireMock.equalTo("application/json"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            {
                                "content": "$content",
                                "content_type": "image/jpeg",
                                "title": "Dette er en tittel",
                                "eier": {
                                    "eiers_fødselsnummer": "$eiersFødselsnummer"
                                }
                            }
                        """.trimIndent()
                    )
            )
    )
    return this
}

internal fun stubGetDokumentJsonFraK9Mellomlagring(eiersFødselsnummer: String, dokumentId: String) {
    val content = Base64.getEncoder().encodeToString("jwkset.json".fromResources().readBytes())
    WireMock.stubFor(
        WireMock.post(WireMock.urlPathMatching(".*$k9MellomlagringPath.*/$dokumentId"))
            .withRequestBody(WireMock.equalToJson("""{"eiers_fødselsnummer": "$eiersFødselsnummer"}""".trimIndent()))
            .withHeader(Headers.CONTENT_TYPE, WireMock.equalTo("application/json"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            {
                                "content": "$content",
                                "content_type": "application/json",
                                "title": "Dette er en tittel",
                                "eier": {
                                    "eiers_fødselsnummer": "$eiersFødselsnummer"
                                }
                            }
                        """.trimIndent()
                    )
            )
    )
}

internal fun stubGetDokumentPdfFraK9Mellomlagring(eiersFødselsnummer: String, dokumentId: String) {
    val content = Base64.getEncoder().encodeToString("test.pdf".fromResources().readBytes())
    WireMock.stubFor(
        WireMock.post(WireMock.urlPathMatching(".*$k9MellomlagringPath.*/$dokumentId"))
            .withRequestBody(WireMock.equalToJson("""{"eiers_fødselsnummer": "$eiersFødselsnummer"}""".trimIndent()))
            .withHeader(Headers.CONTENT_TYPE, WireMock.equalTo("application/json"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            {
                                "content": "$content",
                                "content_type": "application/pdf",
                                "title": "Dette er en tittel",
                                "eier": {
                                    "eiers_fødselsnummer": "$eiersFødselsnummer"
                                }
                            }
                        """.trimIndent()
                    )
            )
    )
}


internal fun WireMockServer.getDokarkivUrl() = baseUrl() + dokarkivBasePath
internal fun WireMockServer.getK9DokumentUrl() = baseUrl() + k9DokumentPath
internal fun WireMockServer.getK9MellomlagringUrl() = baseUrl() + k9MellomlagringPath
