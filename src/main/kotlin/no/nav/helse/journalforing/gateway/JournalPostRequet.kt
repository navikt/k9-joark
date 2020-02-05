package no.nav.helse.journalforing.gateway

internal data class JournalPostRequest(
    val dokumenter : List<JoarkDokument>,
    val journalposttype: String, // INNGAAENDE
    val tittel: String,
    val bruker: AvsenderMottaker,
    val avsenderMottaker: AvsenderMottaker,
    val tema: String, /// OMS
    val datoMottatt: String, // yyyy-MM-dd'T'HH:mm:ssZ
    val kanal: String, // NAV_NO
    val arkivSak: ArkivSak? = null, // Referense til sak. Per nå opprettes sak i Gosys så denne blir ikke satt.
    val journalfoerendeEnhet: String
)

internal data class JoarkDokument(
    val tittel: String,
    val brevkode: String? = null, // Eller brevkode + dokumentkategori
    val dokumentkategori: String? = null,
    val dokumentVarianter: List<DokumentVariant>
)

internal data class AvsenderMottaker(val id: String, val idType: String)

internal data class DokumentVariant(
    val filtype: ArkivFilType,
    val variantformat: VariantFormat,
    val fysiskDokument: ByteArray
)

enum class ArkivFilType  {
    PDFA,
    XML,
    JSON
}

enum class VariantFormat  {
    ORIGINAL,
    ARKIV
}

internal class ArkivSak(
    val arkivSakSystem: String,
    val arkivSakId: String
)