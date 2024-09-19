package no.nav.helse.journalforing.v1

import no.nav.helse.journalforing.v1.Journalpostinfo.Companion.somJournalpostinfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class JournalpostinfoTest {

    @Test
    internal fun `journalpostinfo fra søknadstype`() {
        Søknadstype.entries.forEach { søknadstype ->
            val (brevkode, tittel, tema) = BrevkodeTittelOgTema.hentFor(søknadstype)
            val journalpostinfo = søknadstype.somJournalpostinfo()
            // Sjekker at mappingen er som før
            assertEquals(brevkode, journalpostinfo.brevkode)
            assertEquals(tittel, journalpostinfo.tittel)
            assertEquals(tema, journalpostinfo.tema)
        }
    }
}
