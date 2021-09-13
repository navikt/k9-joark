package no.nav.helse.journalforing.gateway

internal data class JournalPostRequest(
    val dokumenter : List<JoarkDokument>,
    val journalposttype: String, // INNGAAENDE
    val tittel: String,
    val bruker: Bruker,
    val avsenderMottaker: AvsenderMottaker,
    val tema: String, /// OMS
    val datoMottatt: String, // yyyy-MM-dd'T'HH:mm:ssZ
    val kanal: String, // NAV_NO
    val journalfoerendeEnhet: String) {
    val tilleggsopplysninger = listOf(
        Tilleggsopplysning("K9.KILDE", "DIGITAL_SØKNADSDIALOG")
    )
}

internal data class Tilleggsopplysning(
    val nokkel: String,
    val verdi: String
)

internal data class JoarkDokument(
    val tittel: String,
    val brevkode: String? = null, // Eller brevkode + dokumentkategori
    val dokumentkategori: String? = null,
    val dokumentVarianter: List<DokumentVariant>
)

internal data class AvsenderMottaker(val id: String, val idType: String, val navn: String? = null)
internal data class Bruker(val id: String, val idType: String)

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
