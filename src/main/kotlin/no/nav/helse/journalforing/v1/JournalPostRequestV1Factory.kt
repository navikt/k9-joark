package no.nav.helse.journalforing.v1

import no.nav.helse.dokument.Dokument
import no.nav.helse.journalforing.*
import no.nav.helse.journalforing.gateway.*
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private const val PDF_CONTENT_TYPE = "application/pdf"
private const val JSON_CONTENT_TYPE = "application/json"
private const val XML_CONTENT_TYPE = "application/xml"

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

object JournalPostRequestV1Factory {
    internal fun instance(
        mottaker: String,
        kanal: Kanal,
        dokumenter: List<List<Dokument>>,
        datoMottatt: ZonedDateTime,
        journalpostinfo: Journalpostinfo,
        journalposttype: JournalPostType,
        avsenderMottakerIdType: AvsenderMottakerIdType,
        avsenderMottakerNavn: String?
    ) : JournalPostRequest {

        if (dokumenter.isEmpty()) {
            throw IllegalStateException("Det må sendes minst ett dokument")
        }

        val vedlegg = mutableListOf<JoarkDokument>()

        dokumenter.forEach { dokumentBolk ->
                vedlegg.add(mapDokument(dokumentBolk, journalpostinfo.brevkode))
        }

        return JournalPostRequest(
            journalposttype = journalposttype.value,
            avsenderMottaker = AvsenderMottaker(mottaker, avsenderMottakerIdType.value, avsenderMottakerNavn), // I Versjon 1 er det kun innlogget bruker som laster opp vedlegg og fyller ut søknad, så bruker == avsender
            bruker = Bruker(mottaker, avsenderMottakerIdType.value),
            tema = journalpostinfo.tema.value,
            tittel = journalpostinfo.tittel,
            kanal = kanal.value,
            journalfoerendeEnhet = "9999", //  NAV-enheten som har journalført, eventuelt skal journalføre, forsendelsen. Ved automatisk journalføring uten mennesker involvert skal enhet settes til "9999".
            datoMottatt = formatDate(datoMottatt),
            dokumenter = vedlegg,
            innsendingstype = journalpostinfo.innsendingstype
        )
    }

    private fun formatDate(dateTime: ZonedDateTime) : String {
        val utc = ZonedDateTime.ofInstant(dateTime.toInstant(), ZoneOffset.UTC)
        return DATE_TIME_FORMATTER.format(utc)
    }

    private fun mapDokument(dokumentBolk : List<Dokument>, typeReferanse: TypeReferanse) : JoarkDokument {
        val title = dokumentBolk.first().title
        val dokumenterVarianter = mutableListOf<DokumentVariant>()

        dokumentBolk.forEach {
            val arkivFilType = getArkivFilType(it)
            dokumenterVarianter.add(
                DokumentVariant(
                    filtype = arkivFilType,
                    variantformat = getVariantFormat(arkivFilType),
                    fysiskDokument = it.content
                )
            )
        }

        when (typeReferanse) {
            is DokumentType -> {
                return JoarkDokument(
                    tittel = title,
                    dokumentVarianter = dokumenterVarianter.toList()
                )
            }
            is BrevKode -> {
                return JoarkDokument(
                    tittel = title,
                    brevkode = typeReferanse.brevKode,
                    dokumentkategori = typeReferanse.dokumentKategori,
                    dokumentVarianter = dokumenterVarianter.toList()
                )
            }
            else -> throw IllegalStateException("Ikke støtttet type referense ${typeReferanse.javaClass.simpleName}")
        }
    }

    private fun getArkivFilType(dokument: Dokument) : ArkivFilType {
        if (PDF_CONTENT_TYPE == dokument.contentType) return ArkivFilType.PDFA
        if (JSON_CONTENT_TYPE == dokument.contentType) return ArkivFilType.JSON
        if (XML_CONTENT_TYPE == dokument.contentType) return ArkivFilType.XML
        throw IllegalStateException("Ikke støttet Content-Type '${dokument.contentType}'")
    }

    private fun getVariantFormat(arkivFilType: ArkivFilType) : VariantFormat {
        return if (arkivFilType == ArkivFilType.PDFA) VariantFormat.ARKIV else VariantFormat.ORIGINAL
    }
}
