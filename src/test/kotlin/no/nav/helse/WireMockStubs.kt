package no.nav.helse

import com.github.kittinunf.fuel.core.Headers
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.helse.dusseldorf.ktor.core.fromResources
import java.util.*

private const val dokarkivBasePath = "/dokarkiv-mock"
private const val dokarkivMottaInngaaendeForsendelsePath = "$dokarkivBasePath/rest/journalpostapi/v1/journalpost"
private const val k9MellomlagringPath = "/k9-mellomlagring"

internal fun WireMockServer.stubMottaInngaaendeForsendelseOk() : WireMockServer{
    WireMock.stubFor(
        WireMock.post(
            WireMock.urlMatching(".*$dokarkivMottaInngaaendeForsendelsePath"))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[0].nokkel", WireMock.equalTo("k9.kilde")))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[0].verdi", WireMock.equalTo("DIGITAL")))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[1].nokkel", WireMock.equalTo("k9.type")))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[1].verdi", WireMock.matching("SØKNAD|MELDING|ETTERSENDELSE")))
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

internal fun WireMockServer.getDokarkivUrl() = baseUrl() + dokarkivBasePath
internal fun WireMockServer.getK9MellomlagringUrl() = baseUrl() + k9MellomlagringPath