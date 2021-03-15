package no.nav.sosialhjelp.client.kommuneinfo

import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.client.utils.objectMapper
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.io.IOException


fun <T : WebClientResponseException> T.toFiksErrorMessage(): ErrorMessage? {
    return try {
        objectMapper.readValue(this.responseBodyAsByteArray, ErrorMessage::class.java)
    } catch (e: IOException) {
        null
    }
}

val ErrorMessage.feilmeldingUtenFnr: String?
    get() {
        return this.message?.feilmeldingUtenFnr
    }

val String.feilmeldingUtenFnr: String?
    get() {
        return this.replace(Regex("""\b[0-9]{11}\b"""), "[FNR]")
    }
