package no.nav.helse

import io.ktor.server.testing.withApplication
import no.nav.helse.dusseldorf.testsupport.asArguments
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class K9JoarkWithMocks {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9JoarkWithMocks::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val mockOAuth2Server = MockOAuth2Server().apply { start() }
            val wireMockServer = WireMockBuilder()
                .withPort(8111)
                .withNaisStsSupport()
                .withAzureSupport()
                .wireMockConfiguration {
                    it.extensions(DokarkivResponseTransformer())
                }
                .build()
                .stubDomotInngaaendeIsReady()
                .stubMottaInngaaendeForsendelseOk()

            val testArgs = TestConfiguration.asMap(
                wireMockServer = wireMockServer,
                port = 8112,
                mockOAuth2Server = mockOAuth2Server
            ).asArguments()

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    logger.info("Tearing down")
                    wireMockServer.stop()
                    mockOAuth2Server.shutdown()
                    logger.info("Tear down complete")
                }
            })

            withApplication { no.nav.helse.main(testArgs) }
        }
    }
}
