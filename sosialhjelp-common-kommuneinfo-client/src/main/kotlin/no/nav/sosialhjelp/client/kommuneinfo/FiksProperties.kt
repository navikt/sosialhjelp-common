package no.nav.sosialhjelp.client.kommuneinfo

data class FiksProperties(
        val hentKommuneInfoUrl: String,
        val hentAlleKommuneInfoUrl: String,
        val fiksIntegrasjonId: String,
        val fiksIntegrasjonPassord: String
)