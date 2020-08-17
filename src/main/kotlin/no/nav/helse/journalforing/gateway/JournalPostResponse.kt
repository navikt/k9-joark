package no.nav.helse.journalforing.gateway

import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(JournalPostResponse::class.java)

data class JournalPostResponse(
    val journalpostId: String,
    val journalpostFerdigstilt: Boolean,
    val dokumenter: List<DokumentInfo>
)
data class DokumentInfo(
    val dokumentInfoId: String,
    val tittel: String? = null
)
