class Player(val name: String, var chips: Int) {
    val hand = Hand()

    fun bet(amount: Int): Boolean {
        if (amount > chips) {
            println("Aposta maior do que a quantidade de fichas disponíveis.")
            return false
        }
        chips -= amount
        return true
    }
}