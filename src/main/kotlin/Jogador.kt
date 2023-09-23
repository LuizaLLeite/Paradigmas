class Jogador(val nome: String, var fichas: Int) {
    val mao = Mao()

    fun apostar(quantidade: Int): Boolean {
        if (quantidade > fichas) {
            println("Aposta maior do que a quantidade de fichas disponíveis.")
            return false
        }
        fichas -= quantidade
        return true
    }
}