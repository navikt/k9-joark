package no.nav.helse.journalforing.v1

data class MetadataV1(
    val version : Int,
    val correlationId : String,
    val requestId : String?,
    val søknadstype : Søknadstype
)

enum class Søknadstype {
    PLEIEPENGESØKNAD,
    PLEIEPENGESØKNAD_ENDRINGSMELDING,
    PLEIEPENGESØKNAD_ETTERSENDING,
    PLEIEPENGESØKNAD_LIVETS_SLUTTFASE,
    PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING,
    OMSORGSPENGESØKNAD,
    OMSORGSPENGESØKNAD_ETTERSENDING,
    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG,
    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING,
    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING,
    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER,
    OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER,
    OMSORGSPENGEMELDING_DELING_AV_DAGER,
    OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING,
    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE,
    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING,
    OMSORGSDAGER_ALENEOMSORG,
    OPPLÆRINGSPENGESØKNAD,
    FRISINNSØKNAD;

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
