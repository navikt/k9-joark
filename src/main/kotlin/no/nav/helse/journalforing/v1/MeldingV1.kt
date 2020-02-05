package no.nav.helse.journalforing.v1

import java.net.URI
import java.time.ZonedDateTime

data class MeldingV1 (
    val norskIdent: String,
    val mottatt: ZonedDateTime,
    val dokumenter: List<List<URI>>
)