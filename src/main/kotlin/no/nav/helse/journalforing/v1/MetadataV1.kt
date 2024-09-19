package no.nav.helse.journalforing.v1

data class MetadataV1(
    val version : Int,
    val correlationId : String,
    val requestId : String?,
    val søknadstype : Søknadstype
)

enum class Søknadstype(val urlPath: String) {
    PLEIEPENGESØKNAD("/v1/pleiepenge/journalforing"),
    PLEIEPENGESØKNAD_ENDRINGSMELDING("/v1/pleiepenge/endringsmelding/journalforing"),
    PLEIEPENGESØKNAD_ETTERSENDING("/v1/pleiepenge/ettersending/journalforing"),
    PLEIEPENGESØKNAD_LIVETS_SLUTTFASE("/v1/pleiepenge/livets-sluttfase/journalforing"),
    PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING("/v1/pleiepenge/livets-sluttfase/ettersending/journalforing"),
    OMSORGSPENGESØKNAD("/v1/omsorgspenge/journalforing"),
    OMSORGSPENGESØKNAD_ETTERSENDING("/v1/omsorgspenge/ettersending/journalforing"),
    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG("/v1/omsorgspengeutbetaling/journalforing"),
    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING("/v1/omsorgspengeutbetaling/ettersending/journalforing"),
    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER("/v1/omsorgspengeutbetaling/journalforing"),
    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING("/v1/omsorgspengeutbetaling/ettersending/journalforing"),
    OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER("/v1/omsorgsdageroverforing/journalforing"),
    OMSORGSPENGEMELDING_DELING_AV_DAGER("/v1/omsorgsdagerdeling/journalforing"),
    OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING("/v1/omsorgsdagerdeling/ettersending/journalforing"),
    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE("/v1/omsorgspenger/midlertidig-alene/journalforing"),
    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING("/v1/omsorgspenger/midlertidig-alene/ettersending/journalforing"),
    OMSORGSDAGER_ALENEOMSORG("/v1/omsorgsdager/aleneomsorg/journalforing"),
    OMSORGSDAGER_ALENEOMSORG_ETTERSENDING("/v1/omsorgsdager/aleneomsorg/ettersending/journalforing"),
    OPPLÆRINGSPENGESØKNAD("/v1/opplæringspenge/journalforing"),
    FRISINNSØKNAD("/v1/frisinn/journalforing"),
    UNGDOMSYTELSE_SØKNAD("/v1/ungdomsytelse/soknad/journalforing"),
    UNGDOMSYTELSE_ENDRINGSSØKNAD("/v1/ungdomsytelse/endringssoknad/journalforing")
    ;

    internal companion object {
        private val norskeBokstaver = "[ÆØÅ]".toRegex()
        private fun Søknadstype.envKey() =
            "ENABLE_${name.replace(norskeBokstaver, "")}"
        internal fun enabled(env: Map<String, String> = System.getenv()) =
            values().map {
                val envKey = it.envKey()
                val value = env[envKey]?.equals("true") ?: true
                it to value
            }.toMap()
        }
}
