package no.nav.helse.journalforing.v1

import java.net.URI
import java.time.ZonedDateTime

data class MeldingV1(
    val norskIdent: String?,
    val aktoerId: String?,
    val sokerNavn: Navn?,
    val mottatt: ZonedDateTime?,
    val dokumenter: List<List<URI>>?
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
) {
    fun tilString(): String {
        return when (mellomnavn) {
            null -> "$fornavn $etternavn"
            else -> "$fornavn $mellomnavn $etternavn"
        }
    }
}
