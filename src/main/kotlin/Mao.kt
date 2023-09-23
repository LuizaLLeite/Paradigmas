class Mao {
    private val cartas: MutableList<Carta> = mutableListOf()
    private var fichasApostadas: Int = 0

    fun adicionarCarta(carta: Carta) {
        cartas.add(carta)
    }

    fun limpar() {
        cartas.clear()
        fichasApostadas = 0  // Zere as fichas apostadas quando a mão for limpa
    }

    fun getCartas(): List<Carta> {
        return cartas.toList()
    }

    fun obterFichasApostadas(): Int {
        return fichasApostadas
    }

    // Adicione um método para atualizar as fichas apostadas
    fun adicionarApostaNasFichas(quantidade: Int) {
        fichasApostadas += quantidade
    }

    override fun toString(): String {
        return cartas.joinToString(", ") { "${it.valor} de ${it.naipe}" }
    }
}
