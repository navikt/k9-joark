package no.nav.helse

import no.nav.helse.Configuration.Companion.AZURE_V2_ALIAS
import no.nav.helse.dusseldorf.ktor.auth.Client
import no.nav.helse.dusseldorf.ktor.auth.PrivateKeyClient
import no.nav.helse.dusseldorf.oauth2.client.*

internal class AccessTokenClientResolver(
    private val clients: Map<String, Client>
) {

    private val azureV2 : AccessTokenClient

    init {
        val azureV2Client = azureV2Client()
        azureV2 = SignedJwtAccessTokenClient(
            clientId = azureV2Client.clientId(),
            tokenEndpoint = azureV2Client.tokenEndpoint(),
            privateKeyProvider = FromJwk(azureV2Client.privateKeyJwk),
            keyIdProvider = DirectKeyId(azureV2Client.certificateHexThumbprint)
        )
    }

    private fun azureV2Client() : PrivateKeyClient {
        val client = clients.getOrElse(AZURE_V2_ALIAS) {
            throw IllegalStateException("Client[$AZURE_V2_ALIAS] må være satt opp.")
        }
        return client as PrivateKeyClient
    }

    internal fun k9Dokument() = azureV2
    internal fun dokarkiv() = azureV2
}
