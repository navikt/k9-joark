package no.nav.helse.journalforing.v1

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.CorrelationId
import no.nav.helse.dokument.Dokument
import no.nav.helse.dokument.K9DokumentService
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.journalforing.*
import no.nav.helse.journalforing.gateway.JournalforingGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(JournalforingV1Service::class.java)

private val NAV_NO_KANAL = Kanal("NAV_NO")
private val AVSENDER_MOTTAKER_ID_TYPE = AvsenderMottakerIdType("FNR")
private val JOURNALPOSTTYPE = JournalPostType("INNGAAENDE")

private val ONLY_DIGITS = Regex("\\d+")

class JournalforingV1Service(
    private val journalforingGateway: JournalforingGateway,
    private val k9DokumentService: K9DokumentService
) {
    suspend fun journalfor(
        melding: MeldingV1,
        metaData: MetadataV1
    ): JournalPostId {

        val correlationId = CorrelationId(metaData.correlationId)

        logger.info(metaData.toString())

        validerMelding(melding)

        val aktoerId = AktoerId(melding.aktoerId)

        if (melding.sokerNavn == null) {
            logger.warn("Journalpost blir opprettet uten navn på søker.", keyValue("soknadtype", metaData.søknadstype.name))
        }

        logger.trace("Journalfører for AktørID $aktoerId")

        logger.trace("Henter dokumenter")
        val alleDokumenter = mutableListOf<List<Dokument>>()
        melding.dokumenter.forEach {
            alleDokumenter.add(
                k9DokumentService.hentDokumenter(
                    urls = it,
                    correlationId = correlationId,
                    aktoerId = aktoerId
                )
            )
        }

        logger.trace("Genererer request til Joark")
        val (typeReferanse, tittel, tema) = BrevkodeTittelOgTema.hentFor(metaData.søknadstype)

        val request = JournalPostRequestV1Factory.instance(
            journalposttype = JOURNALPOSTTYPE,
            tittel = tittel,
            mottaker = melding.norskIdent,
            tema = tema,
            kanal = NAV_NO_KANAL,
            dokumenter = alleDokumenter.toList(),
            datoMottatt = melding.mottatt,
            typeReferanse = typeReferanse,
            avsenderMottakerIdType = AVSENDER_MOTTAKER_ID_TYPE,
            avsenderMottakerNavn = melding.sokerNavn?.sammensattNavn()
        )

        logger.info("Sender melding til Joark")

        val response = journalforingGateway.jorunalfor(request)

        logger.info("JournalPost med ID ${response.journalpostId} opprettet")
        return JournalPostId(response.journalpostId)
    }

    private fun validerMelding(melding: MeldingV1) {
        val violations = mutableSetOf<Violation>()
        if (melding.dokumenter.isEmpty()) {
            violations.add(
                Violation(
                    parameterName = "dokument",
                    reason = "Det må sendes minst ett dokument",
                    parameterType = ParameterType.ENTITY,
                    invalidValue = melding.dokumenter
                )
            )
        }

        melding.dokumenter.forEach {
            if (it.isEmpty()) {
                violations.add(
                    Violation(
                        parameterName = "dokument_bolk",
                        reason = "Det må være minst et dokument i en dokument bolk.",
                        parameterType = ParameterType.ENTITY,
                        invalidValue = it
                    )
                )
            }
        }

        if (!melding.aktoerId.matches(ONLY_DIGITS)) {
            violations.add(
                Violation(
                    parameterName = "aktoer_id",
                    reason = "Ugyldig AktørID. Kan kun være siffer.",
                    parameterType = ParameterType.ENTITY,
                    invalidValue = melding.aktoerId
                )
            )
        }

        if (!melding.norskIdent.matches(ONLY_DIGITS)) {
            violations.add(
                Violation(
                    parameterName = "norsk_ident",
                    reason = "Ugyldig Norsk Ident. Kan kun være siffer.",
                    parameterType = ParameterType.ENTITY,
                    invalidValue = melding.norskIdent
                )
            )
        }

        if (violations.isNotEmpty()) {
            throw Throwblem(ValidationProblemDetails(violations))
        }
    }
}


private fun Navn.sammensattNavn() = when (mellomnavn) {
    null -> "$fornavn $etternavn"
    else -> "$fornavn $mellomnavn $etternavn"
}
