import kotlin.random.Random

class JogoPoker(private val numJogadoresAI: Int) {
    private val baralho = Baralho()
    private val jogadorHumano = Jogador("Jogador 1 (Humano)", 1000)
    private val jogadoresAI = mutableListOf<Jogador>()
    val jogadores: List<Jogador>
    private val cartasComunitarias = Mao()
    private var continuarJogando = true
    private var indiceDoDistribuidor = 0
    private var valorPote = 0


    init {
        require(numJogadoresAI >= 2) { "Deve haver pelo menos dois jogadores controlados pela máquina." }

        baralho.embaralhar()
        jogadores = mutableListOf(jogadorHumano)
        for (i in 1..numJogadoresAI) {
            jogadoresAI.add(Jogador("Jogador ${i + 1} (AI)", 1000))
            jogadores.add(jogadoresAI[i - 1])
        }
    }

    private fun distribuirMaos() {
        indiceDoDistribuidor = determinarDistribuidor()
        for (i in 0 until jogadores.size) {
            val indiceJogador = (indiceDoDistribuidor + i + 1) % jogadores.size
            jogadores[indiceJogador].mao.limpar()
            jogadores[indiceJogador].mao.adicionarCarta(baralho.pegarCarta()!!)
            jogadores[indiceJogador].mao.adicionarCarta(baralho.pegarCarta()!!)
        }
        jogadores[indiceDoDistribuidor].elegivelParaApostar= false
    }

    private fun determinarDistribuidor(): Int {
        baralho.embaralhar()
        val ranksCartasDistribuidor = mutableListOf<ValorCarta>()
        for (i in 0 until jogadores.size) {
            val cartaJogador = baralho.pegarCarta()!!
            println("${jogadores[i].nome} recebeu ${cartaJogador.valor} de ${cartaJogador.naipe}")
            ranksCartasDistribuidor.add(cartaJogador.valor)
        }

        val maiorRank = ranksCartasDistribuidor.maxOrNull() ?: ValorCarta.DOIS
        return ranksCartasDistribuidor.indexOf(maiorRank)
    }

    fun jogar() {
        println("Bem-vindo ao Poker Texas Hold'em!")
        println("Você possui 1000 fichas. O objetivo é vencer os jogadores controlados pela máquina.")

        while (continuarJogando) {
            baralho.embaralhar()
            cartasComunitarias.limpar()
            for (jogador in jogadores) {
                jogador.mao.limpar()
            }
            valorPote = 0

            distribuirMaos()

            println("\nDistribuidor: ${jogadores[indiceDoDistribuidor].nome}")

            fazerApostasPreFlop()

            if (!continuarJogando) {
                break
            }

            distribuirCartasComunitarias()

            fazerApostasPosFlop()

            if (!continuarJogando) {
                break
            }

            mostrarCartas()

            continuarJogando = continuarJogando()
        }
    }

    private fun fazerApostasPreFlop() {
        val indiceSmallBlind = (indiceDoDistribuidor + 1) % jogadores.size
        val indiceBigBlind = (indiceDoDistribuidor + 2) % jogadores.size

        val smallBlind = jogadores[indiceSmallBlind]
        val bigBlind = jogadores[indiceBigBlind]

        println("\n${smallBlind.nome} é o small blind e aposta 10 fichas.")
        smallBlind.mao.adicionarApostaNasFichas(10)
        apostar(smallBlind, 10)

        println("\n${bigBlind.nome} é o big blind e aposta 20 fichas.")
        bigBlind.mao.adicionarApostaNasFichas(20)
        apostar(bigBlind, 20)

        var indiceJogadorAtual = (indiceDoDistribuidor + 3) % jogadores.size

        while (true) {
            val jogadorAtual = jogadores[indiceJogadorAtual]
            val acoesDisponiveis = listOf("Fold", "Check", "Raise")

            if (jogadorAtual.elegivelParaApostar && !jogadorAtual.saiuDaRodada) {
                println("\n${jogadorAtual.nome}, suas fichas: ${jogadorAtual.fichas}")
                println("Sua mão: ${jogadorAtual.mao.toString()}")
                println("Cartas comunitárias: ${cartasComunitarias.toString()}")
                exibirValorDoPote()
                println("Aposta atual: ${jogadorAtual.mao.obterFichasApostadas()} fichas")
                println("Suas opções: ${acoesDisponiveis.joinToString(", ")}")

                val escolha = if (jogadorAtual == jogadorHumano) {
                    println("Escolha uma opção:")
                    readLine()
                } else {
                    escolherAcaoInteligente(jogadorAtual, cartasComunitarias.getCartas(), acoesDisponiveis)
                }

                when (escolha?.toLowerCase()) {
                    "fold" -> {
                        println("${jogadorAtual.nome} desistiu.")
                        jogadorAtual.mao.limpar()
                        jogadorAtual.saiuDaRodada = true
                    }
                    "check" -> {
                        println("${jogadorAtual.nome} deu check.")
                    }
                    "raise" -> {
                        var valorAumento = 0
                        if (jogadorAtual == jogadorHumano) {
                            while (true) {
                                println("Digite o valor do aumento (mínimo 1, máximo ${jogadorAtual.fichas}): ")
                                val input = readLine()
                                valorAumento = input?.toIntOrNull() ?: 0
                                if (valorAumento < 1 || valorAumento > jogadorAtual.fichas) {
                                    println("Aposta inválida. Tente novamente.")
                                } else {
                                    break
                                }
                            }
                        } else {
                            valorAumento = Random.nextInt(1, jogadorAtual.fichas + 1)
                        }

                        if (apostar(jogadorAtual, valorAumento)) {
                            println("${jogadorAtual.nome} aumentou em $valorAumento fichas.")
                        }
                    }
                    else -> {
                        println("Opção inválida. Tente novamente.")
                    }
                }
                verificarVencedor()
            }

            indiceJogadorAtual = (indiceJogadorAtual + 1) % jogadores.size
            if (indiceJogadorAtual == indiceSmallBlind) {
                break
            }
        }
    }
    private fun escolherAcaoInteligente(jogador: Jogador, cartasComunitarias: List<Carta>, acoesDisponiveis: List<String>): String {
        val qualidadeMao = avaliarMao(jogador.mao.getCartas(), cartasComunitarias)

        return when {
            qualidadeMao >= 2 -> "raise"
            qualidadeMao >= 1 -> "check"
            else -> acoesDisponiveis.random()
        }
    }



    private fun distribuirCartasComunitarias() {
        cartasComunitarias.limpar()
        println("")
        println("//////////////////////////////////")
        for (i in 1..3) {
            val carta = baralho.pegarCarta()!!
            cartasComunitarias.adicionarCarta(carta)
            println("Flop: ${carta.valor} de ${carta.naipe}")
        }
        println("//////////////////////////////////")


        fazerApostasPosFlop()
        if (!continuarJogando) {
            return
        }

        val cartaTurn = baralho.pegarCarta()!!
        cartasComunitarias.adicionarCarta(cartaTurn)
        println("")
        println("//////////////////////////////////")
        println("Turn: ${cartaTurn.valor} de ${cartaTurn.naipe}")
        println("//////////////////////////////////")
        fazerApostasPosFlop()
        if (!continuarJogando) {
            return
        }

        val cartaRiver = baralho.pegarCarta()!!
        cartasComunitarias.adicionarCarta(cartaRiver)
        println("")
        println("//////////////////////////////////")
        println("River: ${cartaRiver.valor} de ${cartaRiver.naipe}")
        println("//////////////////////////////////")
        fazerApostasPosFlop()
    }

    private fun fazerApostasPosFlop() {
        var índiceJogadorAtual = (indiceDoDistribuidor + 1) % jogadores.size

        while (true) {
            val jogadorAtual = jogadores[índiceJogadorAtual]
            val acoesDisponiveis = listOf("Check", "Fold", "Raise")

            if (!jogadorAtual.saiuDaRodada) {
                println("\n${jogadorAtual.nome}, suas fichas: ${jogadorAtual.fichas}")
                println("Sua mão: ${jogadorAtual.mao.toString()}")
                println("Cartas comunitárias: ${cartasComunitarias.toString()}")
                exibirValorDoPote()
                println("Suas opções: ${acoesDisponiveis.joinToString(", ")}")

                val escolha = if (jogadorAtual == jogadorHumano) {
                    println("Escolha uma opção:")
                    readLine()
                } else {
                    escolherAcaoInteligente(jogadorAtual, cartasComunitarias.getCartas(), acoesDisponiveis)
                }

                when (escolha?.toLowerCase()) {
                    "fold" -> {
                        println("${jogadorAtual.nome} desistiu.")
                        jogadorAtual.mao.limpar()
                        jogadorAtual.saiuDaRodada = true
                    }
                    "check" -> {
                        println("${jogadorAtual.nome} deu check.")
                    }
                    "raise" -> {
                        var valorAumento = 0
                        if (jogadorAtual == jogadorHumano) {
                            while (true) {
                                println("Digite o valor do aumento (mínimo 1, máximo ${jogadorAtual.fichas}): ")
                                val input = readLine()
                                valorAumento = input?.toIntOrNull() ?: 0
                                if (valorAumento < 1 || valorAumento > jogadorAtual.fichas) {
                                    println("Aposta inválida. Tente novamente.")
                                } else {
                                    break
                                }
                            }
                        } else {
                            valorAumento = Random.nextInt(1, jogadorAtual.fichas + 1)
                        }

                        if (apostar(jogadorAtual, valorAumento)) {
                            println("${jogadorAtual.nome} aumentou em $valorAumento fichas.")
                        }
                    }
                    else -> {
                        println("Opção inválida. Tente novamente.")
                    }
                }
            }

            índiceJogadorAtual = (índiceJogadorAtual + 1) % jogadores.size
            if (índiceJogadorAtual == indiceDoDistribuidor) {
                break
            }
        }
    }

    fun apostar(jogador: Jogador, valorAposta: Int): Boolean {
        if (valorAposta > jogador.fichas) {
            println("Aposta maior do que a quantidade de fichas disponíveis.")
            return false
        }
        jogador.fichas -= valorAposta
        valorPote += valorAposta
        return true
    }


    private fun mostrarCartas() {
        val jogadoresRestantes = jogadores.filter { it.mao.getCartas().isNotEmpty() }

        if (jogadoresRestantes.size == 1) {
            val vencedor = jogadoresRestantes.first()
            println("\nResultado: ${vencedor.nome} venceu a rodada!")
            vencedor.fichas += valorPote
            valorPote = 0
        } else {
            val mãosComJogadores = jogadoresRestantes.associateBy({ it }, { avaliarMao(it.mao.getCartas(), cartasComunitarias.getCartas()) })
            val jogadorVencedor = mãosComJogadores.maxByOrNull { it.value }?.key
            println("\nResultado: ${jogadorVencedor?.nome} venceu a rodada!")
            jogadorVencedor?.fichas = valorPote
            valorPote = 0
        }

        exibirPlacarRodada()
    }

    private fun exibirPlacarRodada() {
        println("\nPlacar da Rodada:")
        for (jogador in jogadores) {
            println("${jogador.nome}: ${jogador.fichas} fichas")
        }
    }


    private fun showdown() {
        val jogadoresRestantes = jogadores.filter { it.mao.getCartas().isNotEmpty() }

        if (jogadoresRestantes.size == 1) {
            val vencedor = jogadoresRestantes.first()
            println("\nResultado: ${vencedor.nome} venceu a rodada!")
        } else {
            val maosComJogadores = jogadoresRestantes.associateBy({ it }, { avaliarMao(it.mao.getCartas(), cartasComunitarias.getCartas()) })
            val jogadorVencedor = maosComJogadores.maxByOrNull { it.value }?.key
            println("\nResultado: ${jogadorVencedor?.nome} venceu a rodada!")
        }



        exibirPontuacoesRodada()
    }


    private fun exibirPontuacoesRodada() {
        println("\nPontuações da Rodada:")
        for (jogador in jogadores) {
            println("${jogador.nome}: ${jogador.fichas} fichas")
        }
    }

    private fun continuarJogando(): Boolean {
        if (jogadores.any { it.fichas <= 0 }) {
            println("Fim do jogo. Um dos jogadores ficou sem fichas.")
            return false
        }

        println("\nDeseja continuar jogando? (S para sim, qualquer outra tecla para sair)")
        val escolha = readLine()
        if (escolha?.equals("S", ignoreCase = true) == true) {

            for (jogador in jogadores) {
                jogador.saiuDaRodada = false
                jogador.mao.limpar()
            }
            return true
        }
        return false
    }


    fun avaliarMao(cartasJogador: List<Carta>, cartasComunitarias: List<Carta>): Int {
        val todasCartas = cartasJogador + cartasComunitarias
        val agrupadoPorValor = todasCartas.groupBy { it.valor }
        val maxOcorrencias = agrupadoPorValor.values.map { it.size }.maxOrNull() ?: 0

        return when {
            maxOcorrencias >= 5 -> 8  // Full House
            maxOcorrencias >= 4 -> 7  // Quadra
            maxOcorrencias >= 3 -> {
                if (agrupadoPorValor.values.any { it.size >= 2 }) 6 else 3  // Trinca ou Dois Pares
            }
            agrupadoPorValor.size == 5 && eSequencia(todasCartas) -> 5  // Sequência
            agrupadoPorValor.size == 5 -> 4  // Carta Alta
            agrupadoPorValor.size == 4 -> 2  // Dois Pares
            else -> 1  // Um Par
        }
    }

    private fun eSequencia(cartas: List<Carta>): Boolean {
        val valores = cartas.map { it.valor.valor }.distinct().sorted()
        return valores == (valores.first()..valores.last()).toList()
    }

    private fun verificarVencedor() {
        if (jogadores.count { !it.saiuDaRodada } == 1) {
            val vencedor = jogadores.first { !it.saiuDaRodada }
            println("\nResultado: ${vencedor.nome} venceu a rodada!")
            exibirPontuacoesRodada()
            continuarJogando = continuarJogando()
        }
    }



    fun compararCartas(carta1: Carta, carta2: Carta): Carta {

        val comparacaoNaipe = carta1.naipe.compareTo(carta2.naipe)

        if (comparacaoNaipe != 0) {

            return if (carta1.naipe > carta2.naipe) carta1 else carta2
        } else {

            return if (carta1.valor > carta2.valor) carta1 else carta2
        }
    }

    fun desempatar(maosJogadores: List<List<Carta>>, jogadores: List<Jogador>): Jogador? {
        var jogadorVencedor: Jogador? = null
        var cartaMaisAlta: Carta? = null

        for (i in maosJogadores.indices) {
            val cartasMao = maosJogadores[i]

            val cartaMaisAltaMao = cartasMao.maxByOrNull { it.valor.valor }

            if (cartaMaisAlta == null || (cartaMaisAltaMao != null && cartaMaisAltaMao.valor.valor > cartaMaisAlta.valor.valor)) {
                cartaMaisAlta = cartaMaisAltaMao
                jogadorVencedor = jogadores[i]
            }
        }

        return jogadorVencedor
    }

    private fun exibirValorDoPote() {
        println("Pote atual: $valorPote fichas")
    }

    private fun calcularApostaAtual(): Int {
        var apostaAtual = 0

        for (jogador in jogadores) {
            if (!jogador.saiuDaRodada) {
                apostaAtual += jogador.mao.obterFichasApostadas()
            }
        }

        return apostaAtual
    }


}