package no.nav.helse.journalforing.v1

import java.net.URI
import java.time.ZonedDateTime

data class MeldingV1 (
    val norskIdent: String,
    val aktoerId: String? = null,
    val mottatt: ZonedDateTime,
    val sokerNavn: Navn?,
    val dokumenter: List<List<URI>>
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)
