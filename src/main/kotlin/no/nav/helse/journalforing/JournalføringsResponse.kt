package no.nav.helse.journalforing

import no.nav.helse.journalforing.gateway.DokumentInfo

data class JournalføringsResponse(val journalpostId: String, val dokumenter: List<DokumentInfo>)
