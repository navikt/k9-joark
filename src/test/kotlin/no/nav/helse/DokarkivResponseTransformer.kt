package no.nav.helse

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import org.json.JSONObject

internal class DokarkivResponseTransformer : ResponseTransformer() {
    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val requestEntity = request!!.bodyAsString
        val tema = JSONObject(requestEntity).getString("tema")

        val journalpostId = when {
            requestEntity.contains("NAV 09-11.05") && "OMS" == tema -> "1"
            requestEntity.contains("NAV 09-06.05") && "OMS" == tema -> "2"
            requestEntity.contains("NAV 09-35.01") && "OMS" == tema -> "3"
            requestEntity.contains("NAV 09-35.02") && "OMS" == tema -> "4"
            requestEntity.contains("NAV 09-06.08") && "OMS" == tema -> "5"
            requestEntity.contains("NAV 09-11.08") && "OMS" == tema -> "6"
            requestEntity.contains("NAV 00-03.02") && "FRI" == tema -> "7"
            else -> throw IllegalArgumentException("Ikke støttet brevkode.")
        }

        return Response.Builder.like(response)
            .body(getResponse(journalpostId))
            .build()
    }

    override fun getName(): String {
        return "dokarkiv"
    }

    override fun applyGlobally(): Boolean {
        return false
    }
}

private fun getResponse(journalpostId: String) =
    //language=json
    """       
        {
          "journalpostId": "$journalpostId",
          "journalstatus": "M",
          "melding": null,
          "journalpostferdigstilt": false,
          "dokumenter": [
            {
              "dokumentInfoId": "485201432",
              "tittel": "Søknad om omsorgspenger - utvidet rett"
            },
            {
              "dokumentInfoId": "485201433",
              "tittel": null
            }
          ]
        }
    """.trimIndent()
