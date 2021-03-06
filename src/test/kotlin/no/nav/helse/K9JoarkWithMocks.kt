package no.nav.helse

import io.ktor.server.testing.withApplication
import no.nav.helse.dusseldorf.testsupport.asArguments
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class K9JoarkWithMocks {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9JoarkWithMocks::class.java)

        @JvmStatic
        fun main(args: Array<String>) {

            val wireMockServer = WireMockBuilder()
                .withPort(8111)
                .withNaisStsSupport()
                .withAzureSupport()
                .wireMockConfiguration {
                    it.extensions(DokarkivResponseTransformer())
                }
                .build()
                .stubGetDokument()
                .stubDomotInngaaendeIsReady()
                .stubMottaInngaaendeForsendelseOk()

            val testArgs = TestConfiguration.asMap(
                wireMockServer = wireMockServer,
                port = 8112
            ).asArguments()

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    logger.info("Tearing down")
                    wireMockServer.stop()
                    logger.info("Tear down complete")
                }
            })

            withApplication { no.nav.helse.main(testArgs) }
        }
    }
}