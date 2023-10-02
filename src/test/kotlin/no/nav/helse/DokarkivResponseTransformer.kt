package no.nav.helse

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import org.json.JSONObject

internal class DokarkivResponseTransformer : ResponseTransformerV2 {
    override fun getName(): String {
        return "dokarkiv"
    }

    override fun transform(response: Response, serveEvent: ServeEvent): Response {
        val requestEntity = serveEvent.request.bodyAsString
        val tema = JSONObject(requestEntity).getString("tema")

        val journalpostId = when {
            requestEntity.contains("NAV 09-11.05") && "OMS" == tema -> "1"
            requestEntity.contains("NAV 09-06.05") && "OMS" == tema -> "2"
            requestEntity.contains("NAV 09-35.01") && "OMS" == tema -> "3"
            requestEntity.contains("NAV 09-35.02") && "OMS" == tema -> "4"
            requestEntity.contains("NAV 09-06.08") && "OMS" == tema -> "5"
            requestEntity.contains("NAV 09-11.08") && "OMS" == tema -> "6"
            requestEntity.contains("NAV 00-03.02") && "FRI" == tema -> "7"
            requestEntity.contains("NAV 09-06.07") && "OMS" == tema -> "8"
            requestEntity.contains("NAVe 09-11.05") && "OMS" == tema -> "9"
            requestEntity.contains("NAVe 09-06.05") && "OMS" == tema -> "10"
            requestEntity.contains("NAVe 09-35.01") && "OMS" == tema -> "11"
            requestEntity.contains("NAVe 09-35.02") && "OMS" == tema -> "12"
            requestEntity.contains("NAVe 09-06.07") && "OMS" == tema -> "13"
            requestEntity.contains("NAVe 09-06.08") && "OMS" == tema -> "14"
            requestEntity.contains("NAV 09-06.10") && "OMS" == tema -> "15"
            requestEntity.contains("NAV 09-12.05") && "OMS" == tema -> "16"
            requestEntity.contains("NAVe 09-12.05") && "OMS" == tema -> "17"
            else -> throw IllegalArgumentException("Ikke støttet brevkode.")
        }

        return Response.Builder.like(response)
            .body(getResponse(journalpostId))
            .build()
    }

    override fun applyGlobally(): Boolean {
        return false
    }
}

private fun getResponse(journalpostId: String) =
    """       
        {
          "journalpostId": "$journalpostId",
          "journalstatus": "M",
          "melding": null,
          "journalpostferdigstilt": false,
          "dokumenter": [
            {
              "dokumentInfoId": "485201432"
            },
            {
              "dokumentInfoId": "485201433"
            }
          ]
        }
    """.trimIndent()
