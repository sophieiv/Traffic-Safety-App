package no.uio.ifi.in2000.simonng.simonng.team1.data

enum class Fylker(val visningsnavn: String, val fylkesId: String) {
    OSLO("Oslo", "03"),
    AKERSHUS("Akershus", "32"),
    BUSKERUD("Buskerud", "33"),
    VESTFOLD("Vestfold", "39"),
    OESTFOLD("Østfold", "31");

    companion object {
        val TILGJENGELIGE_FYLKER = listOf(OSLO, AKERSHUS, BUSKERUD, VESTFOLD, OESTFOLD)
    }
}