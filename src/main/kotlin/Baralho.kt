class Baralho {
    private val cartas: MutableList<Carta> = mutableListOf()

    init {
        for (naipe in Naipe.values()) {
            for (valor in ValorCarta.values()) {
                cartas.add(Carta(valor, naipe))
            }
        }
    }

    fun embaralhar() {
        cartas.shuffle()
    }

    fun pegarCarta(): Carta? {
        return if (cartas.isNotEmpty()) cartas.removeAt(0) else null
    }
}