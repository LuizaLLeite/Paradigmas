class Hand {
    private val cards: MutableList<Card> = mutableListOf()
    private var chipsBet: Int = 0

    fun addCard(card: Card) {
        cards.add(card)
    }

    fun clear() {
        cards.clear()
        chipsBet = 0  // Limpe as fichas apostadas quando a mão for clara
    }

    fun getCards(): List<Card> {
        return cards.toList()
    }

    fun getChipsBet(): Int {
        return chipsBet
    }

    // Adicione um método para atualizar as fichas apostadas
    fun addToChipsBet(amount: Int) {
        chipsBet += amount
    }

    override fun toString(): String {
        return cards.joinToString(", ") { "${it.rank} of ${it.suit}" }
    }
}
