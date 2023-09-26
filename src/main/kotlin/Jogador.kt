class Jogador(val nome: String, var fichas: Int) {
    val mao = Mao()
    var saiuDaRodada: Boolean = false
    var ativo: Boolean = true
    var elegivelParaApostar: Boolean = true

}