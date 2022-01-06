package no.nav.helse.journalforing.v1

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.CorrelationId
import no.nav.helse.dokument.Dokument
import no.nav.helse.dokument.DokumentService
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.journalforing.*
import no.nav.helse.journalforing.gateway.JournalforingGateway
import no.nav.helse.journalforing.v1.Journalpostinfo.Companion.somJournalpostinfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(JournalforingV1Service::class.java)

private val NAV_NO_KANAL = Kanal("NAV_NO")
private val AVSENDER_MOTTAKER_ID_TYPE = AvsenderMottakerIdType("FNR")
private val JOURNALPOSTTYPE = JournalPostType("INNGAAENDE")

private val ONLY_DIGITS = Regex("\\d+")

class JournalforingV1Service(
    private val journalforingGateway: JournalforingGateway,
    private val dokumentService: DokumentService
) {
    suspend fun journalfor(
        melding: MeldingV1,
        metaData: MetadataV1
    ): JournalPostId {

        val correlationId = CorrelationId(metaData.correlationId)

        logger.info(metaData.toString())

        validerMelding(melding)

        val fodselsnummer = Fodselsnummer(melding.norskIdent)

        if (melding.sokerNavn == null) {
            logger.warn("Journalpost blir opprettet uten navn på søker.", keyValue("soknadtype", metaData.søknadstype.name))
        }

        logger.trace("Henter dokumenter")
        val alleDokumenter = mutableListOf<List<Dokument>>()

        melding.dokumentId?.forEach { dokumentId ->
            logger.info("Henter dokumenter basert på dokumentId")
            alleDokumenter.add(
                dokumentService.hentDokumenterMedDokumentId(
                    dokumentId = dokumentId,
                    correlationId = correlationId,
                    fodselsnummer = fodselsnummer
                )
            )
        }

        melding.dokumenter?.forEach {
            alleDokumenter.add(
                dokumentService.hentDokumenter(
                    urls = it,
                    correlationId = correlationId,
                    aktoerId = melding.aktoerId,
                    fodselsnummer = fodselsnummer
                )
            )
        }

        logger.trace("Genererer request til Joark")
        val journalpostinfo = metaData.søknadstype.somJournalpostinfo()

        val request = JournalPostRequestV1Factory.instance(
            journalposttype = JOURNALPOSTTYPE,
            mottaker = melding.norskIdent,
            kanal = NAV_NO_KANAL,
            dokumenter = alleDokumenter.toList(),
            datoMottatt = melding.mottatt,
            journalpostinfo = journalpostinfo,
            avsenderMottakerIdType = AVSENDER_MOTTAKER_ID_TYPE,
            avsenderMottakerNavn = melding.sokerNavn?.sammensattNavn()
        )

        logger.info("Sender melding til Joark")

        val response = journalforingGateway.journalfor(request)

        logger.info("JournalPost med ID ${response.journalpostId} opprettet")
        return JournalPostId(response.journalpostId)
    }

    private fun validerDokumenter(melding: MeldingV1): MutableSet<Violation> {
        val feil = mutableSetOf<Violation>()

        if(melding.dokumentId == null && melding.dokumenter == null){
            feil.add(
                Violation(
                    parameterName = "dokumenter, dokumentId",
                    reason = "dokumentId eller dokumenter må være satt. Ikke begge kan være null.",
                    parameterType = ParameterType.ENTITY,
                    invalidValue = "${melding.dokumenter}, ${melding.dokumentId}"
                )
            )
        }

        if(melding.dokumentId != null && melding.dokumenter != null){
            feil.add(
                Violation(
                    parameterName = "dokumenter, dokumentId",
                    reason = "Kun en av dokumentId og dokumenter kan være satt, ikke begge.",
                    parameterType = ParameterType.ENTITY,
                    invalidValue = "${melding.dokumenter}, ${melding.dokumentId}"
                )
            )
        }

        melding.dokumenter?.let {
            if(it.isEmpty()){
                feil.add(
                    Violation(
                        parameterName = "dokumenter",
                        reason = "Det må sendes minst ett dokument",
                        parameterType = ParameterType.ENTITY,
                        invalidValue = melding.dokumenter
                    )
                )
            }

            it.forEach { dokumentBolk ->
                if (dokumentBolk.isEmpty()) {
                    feil.add(
                        Violation(
                            parameterName = "dokumenter.dokument_bolk",
                            reason = "Det må være minst et dokument i en dokument bolk.",
                            parameterType = ParameterType.ENTITY,
                            invalidValue = dokumentBolk
                        )
                    )
                }
            }
        }

        melding.dokumentId?.let {
            if(it.isEmpty()){
                feil.add(
                    Violation(
                        parameterName = "dokumentId",
                        reason = "Det må sendes minst ett dokument",
                        parameterType = ParameterType.ENTITY,
                        invalidValue = melding.dokumentId
                    )
                )
            }

            it.forEach { dokumentBolk ->
                if (dokumentBolk.isEmpty()) {
                    feil.add(
                        Violation(
                            parameterName = "dokumentId.dokument_bolk",
                            reason = "Det må være minst et dokument i en dokument bolk.",
                            parameterType = ParameterType.ENTITY,
                            invalidValue = dokumentBolk
                        )
                    )
                }
            }
        }

        return feil
    }

    private fun validerMelding(melding: MeldingV1) {
        val violations = mutableSetOf<Violation>()

        violations.addAll(validerDokumenter(melding))

        if (melding.aktoerId != null && !melding.aktoerId.matches(ONLY_DIGITS)) {
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