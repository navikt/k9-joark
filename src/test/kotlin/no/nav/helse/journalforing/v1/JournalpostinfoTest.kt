package no.nav.helse.journalforing.v1

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class JournalpostinfoTest {

    @Test
    internal fun `journalpostinfo fra søknadstype`() {
        Søknadstype.entries.forEach { søknadstype ->
            val (brevkode, tittel, tema) = BrevkodeTittelOgTema.hentFor(søknadstype)
            // Sjekker at mappingen er som før
            assertEquals(brevkode, søknadstype.brevkode)
            assertEquals(tittel, søknadstype.tittel)
            assertEquals(tema, søknadstype.tema)
        }
    }
}
