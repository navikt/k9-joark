package no.nav.helse.journalforing.v1

data class MetadataV1(
    val version : Int,
    val correlationId : String,
    val requestId : String?,
    val søknadstype : Søknadstype
)

enum class Søknadstype {
    PLEIEPENGESØKNAD,
    OMSORGSPENGESØKNAD,
    OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG,
    OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER,
    OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER,
    OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE,
    OPPLÆRINGSPENGESØKNAD,
    FRISINNSØKNAD
}