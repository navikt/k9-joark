package no.nav.helse.dokument

data class Dokument(
    val title : String,
    val content : ByteArray,
    val contentType : String,
    val eier: DokumentEier? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dokument

        if (title != other.title) return false
        if (!content.contentEquals(other.content)) return false
        if (contentType != other.contentType) return false
        if (eier != other.eier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + eier.hashCode()
        return result
    }
}

data class DokumentEier(val eiersFødselsnummer: String)
