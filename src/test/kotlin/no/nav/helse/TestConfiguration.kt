package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.helse.dusseldorf.testsupport.jws.ClientCredentials
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV1WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getNaisStsWellKnownUrl

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        port : Int = 8080,
        dokarkivUrl : String? = wireMockServer?.getDokarkivUrl(),
        k9MellomlagringUrl: String? = wireMockServer?.getK9MellomlagringUrl(),
        k9JoarkAzureClientId: String = "pleiepenger-joark"
    ) : Map<String, String>{
        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.dokarkiv_base_url", "$dokarkivUrl"),
            Pair("nav.k9_mellomlagring_base_url", "$k9MellomlagringUrl")
        )

        // Clients
        wireMockServer?.apply {
            map["nav.auth.clients.0.alias"] = "nais-sts"
            map["nav.auth.clients.0.client_id"] = "srvpleiepenger-joark"
            map["nav.auth.clients.0.client_secret"] = "very-secret"
            map["nav.auth.clients.0.discovery_endpoint"] = wireMockServer.getNaisStsWellKnownUrl()
        }

        wireMockServer?.apply {
            map["nav.auth.clients.1.alias"] = "azure-v2"
            map["nav.auth.clients.1.client_id"] = k9JoarkAzureClientId
            map["nav.auth.clients.1.private_key_jwk"] = ClientCredentials.ClientA.privateKeyJwk
            map["nav.auth.clients.1.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
            map["nav.auth.scopes.k9-dokument-scope"] = "k9-dokument/.default"
            map["nav.auth.scopes.k9-mellomlagring-scope"] = "k9-mellomlagring/.default"
        }

        // Issuers
        wireMockServer?.apply {
            map["nav.auth.issuers.0.type"] = "azure"
            map["nav.auth.issuers.0.alias"] = "azure-v1"
            map["nav.auth.issuers.0.discovery_endpoint"] = wireMockServer.getAzureV1WellKnownUrl()
            map["nav.auth.issuers.0.audience"] = k9JoarkAzureClientId
            map["nav.auth.issuers.0.azure.require_certificate_client_authentication"] = "true"
            map["nav.auth.issuers.0.azure.required_roles"] = "access_as_application"

            map["nav.auth.issuers.1.type"] = "azure"
            map["nav.auth.issuers.1.alias"] = "azure-v2"
            map["nav.auth.issuers.1.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
            map["nav.auth.issuers.1.audience"] = k9JoarkAzureClientId
            map["nav.auth.issuers.1.azure.require_certificate_client_authentication"] = "true"
            map["nav.auth.issuers.1.azure.required_roles"] = "access_as_application"
        }

        return map.toMap()
    }
}
