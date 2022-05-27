package no.nav.helse

import io.ktor.config.*
import no.nav.helse.dusseldorf.ktor.auth.Client
import no.nav.helse.dusseldorf.ktor.auth.clients
import no.nav.helse.dusseldorf.ktor.auth.issuers
import no.nav.helse.dusseldorf.ktor.auth.withoutAdditionalClaimRules
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import java.net.URI

internal data class Configuration(private val config : ApplicationConfig) {
    private val clients: Map<String, Client>

    companion object {
        internal const val AZURE_V2_ALIAS = "azure-v2"
    }

    init {
        clients = config.clients()
        ensureAzureClientConfigured()
    }

    internal fun getDokarkivBaseUrl() = URI(config.getRequiredString("nav.dokarkiv_base_url", secret = false))
    internal fun getK9MellomlagringBaseUrl() = URI(config.getRequiredString("nav.k9_mellomlagring_base_url", secret = false))

    internal fun issuers() = config.issuers().withoutAdditionalClaimRules()

    internal fun clients() = clients

    private fun ensureAzureClientConfigured() {
        if(!clients().containsKey(AZURE_V2_ALIAS)) throw IllegalStateException("Azure client må være konfigurert.")
    }

    internal fun getDokarkivScope() = config.getRequiredList("nav.auth.scopes.dokarkiv-scope", secret = false, builder = { it }).toSet()

    internal fun getHenteDokumentScopes() : Set<String> {
        return config.getRequiredList("nav.auth.scopes.k9-dokument-scope", secret = false, builder = { it }).toSet()
    }

    internal fun getK9MellomlagringScopes() : Set<String> {
        return config.getRequiredList("nav.auth.scopes.k9-mellomlagring-scope", secret = false, builder = { it }).toSet()
    }
}
