package no.nav.helse.journalforing.v1

data class MetadataV1(
    val version : Int,
    val correlationId : String,
    val requestId : String?,
    val søknadstype : Søknadstype
)

enum class Søknadstype {
    PLEIEPENGESØKNAD,
    PLEIEPENGESØKNAD_ETTERSENDING,
    OMSORGSPENGESØKNAD,
    OMSORGSPENGESØKNAD_ETTERSENDING,
    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG,
    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER,
    OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER,
    OMSORGSPENGEMELDING_DELING_AV_DAGER,
    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE,
    OPPLÆRINGSPENGESØKNAD,
    FRISINNSØKNAD;

    internal companion object {
        private val norskeBokstaver = "[ÆØÅ]".toRegex()
        private fun Søknadstype.envKey() =
            "ENABLE_${name.replace(norskeBokstaver, "")}"
        internal fun enabled(env: Map<String, String> = System.getenv()) =
            values().map {
                it to (env[it.envKey()]?.equals("true")?:true)
            }.toMap()
        }
}
