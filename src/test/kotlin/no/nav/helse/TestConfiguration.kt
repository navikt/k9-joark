package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.helse.dusseldorf.testsupport.jws.ClientCredentials
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV1WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getNaisStsWellKnownUrl
import no.nav.security.mock.oauth2.MockOAuth2Server

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        port : Int = 8080,
        dokarkivUrl : String? = wireMockServer?.getDokarkivUrl(),
        k9MellomlagringUrl: String? = wireMockServer?.getK9MellomlagringUrl(),
        k9JoarkAzureClientId: String = "k9-joark",
        mockOAuth2Server: MockOAuth2Server
    ) : Map<String, String>{
        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.dokarkiv_base_url", "$dokarkivUrl"),
            Pair("nav.k9_mellomlagring_service_discovery", "$k9MellomlagringUrl")
        )

        // Clients
        wireMockServer?.apply {
            map["nav.auth.clients.0.alias"] = "azure-v2"
            map["nav.auth.clients.0.client_id"] = k9JoarkAzureClientId
            map["nav.auth.clients.0.private_key_jwk"] = ClientCredentials.ClientA.privateKeyJwk
            map["nav.auth.clients.0.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
            map["nav.auth.scopes.k9-mellomlagring-scope"] = "k9-mellomlagring/.default"
            map["nav.auth.scopes.dokarkiv-scope"] = "dokarkiv/.default"
        }

        // Issuers
        wireMockServer?.apply {
            map["no.nav.security.jwt.issuers.0.issuer_name"] = "azure"
            map["no.nav.security.jwt.issuers.0.discoveryurl"] = "${mockOAuth2Server.wellKnownUrl("azure")}"
            map["no.nav.security.jwt.issuers.0.accepted_audience"] = "dev-gcp:dusseldorf:k9-joark"
        }

        return map.toMap()
    }
}
