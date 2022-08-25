package no.nav.helse.journalforing.v1

import java.time.ZonedDateTime

data class MeldingV1 (
    val norskIdent: String,
    val mottatt: ZonedDateTime,
    val sokerNavn: Navn?,
    val dokumentId: List<List<String>>
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)
