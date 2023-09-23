import kotlin.random.Random

class JogoPoker(private val numJogadoresAI: Int) {
    private val baralho = Baralho()
    private val jogadorHumano = Jogador("Jogador 1 (Humano)", 1000)
    private val jogadoresAI = mutableListOf<Jogador>()
    private val jogadores: List<Jogador>

    private val cartasComunitarias = Mao()

    private var continuarJogando = true
    private var indiceDoDistribuidor = 0

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
        println("Bem-vindo ao Texas Hold'em Poker!")
        println("Você possui 1000 fichas. O objetivo é vencer os jogadores controlados pela máquina.")

        while (continuarJogando) {
            baralho.embaralhar()
            cartasComunitarias.limpar()
            for (jogador in jogadores) {
                jogador.mao.limpar()
            }

            distribuirMaos()

            println("\nDistribuidor: ${jogadores[indiceDoDistribuidor].nome}")

            // Realizar o procedimento de apostas com base nas regras que você descreveu.
            fazerApostasPreFlop()

            if (!continuarJogando) {
                break
            }

            distribuirCartasComunitarias()

            // Procedimento de apostas pós-flop
            fazerApostasPosFlop()

            if (!continuarJogando) {
                break
            }

            // Showdown (revelação das cartas)
            showdown()

            // Perguntar se os jogadores desejam continuar após cada rodada
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
        smallBlind.apostar(10)

        println("\n${bigBlind.nome} é o big blind e aposta 20 fichas.")
        bigBlind.mao.adicionarApostaNasFichas(20)
        bigBlind.apostar(20)

        var indiceJogadorAtual = (indiceDoDistribuidor + 3) % jogadores.size

        while (true) {
            val jogadorAtual = jogadores[indiceJogadorAtual]
            val acoesDisponiveis = listOf("Fold", "Check", "Raise")

            println("\n${jogadorAtual.nome}, suas fichas: ${jogadorAtual.fichas}")
            println("Sua mão: ${jogadorAtual.mao.toString()}")
            println("Cartas comunitárias: ${cartasComunitarias.toString()}")
            println("Aposta atual: ${jogadorAtual.mao.obterFichasApostadas()} fichas")
            println("Suas opções: ${acoesDisponiveis.joinToString(", ")}")

            val escolha = if (jogadorAtual == jogadorHumano) {
                println("Escolha uma opção:")
                readLine()
            } else {
                acoesDisponiveis[Random.nextInt(acoesDisponiveis.size)]
            }

            when (escolha) {
                "Fold" -> {
                    println("${jogadorAtual.nome} desistiu.")
                    jogadorAtual.mao.limpar()
                }
                "Check" -> {
                    println("${jogadorAtual.nome} deu check.")
                }
                "Raise" -> {
                    var valorAumento = if (jogadorAtual == jogadorHumano) {
                        println("Digite o valor do aumento (mínimo 20, máximo ${jogadorAtual.fichas}): ")
                        readLine()?.toIntOrNull() ?: 0
                    } else {
                        Random.nextInt(20, jogadorAtual.fichas)
                    }
                    while(valorAumento < 20 || valorAumento > jogadorAtual.fichas){
                        println("Aposta inválida. Tente novamente.")
                        valorAumento = readLine()?.toIntOrNull() ?: 0
                    }
                    println("${jogadorAtual.nome} aumentou a aposta em $valorAumento fichas.")
                    jogadorAtual.mao.adicionarApostaNasFichas(valorAumento)
                    jogadorAtual.apostar(valorAumento)
                }
            }

            indiceJogadorAtual = (indiceJogadorAtual + 1) % jogadores.size
            if (indiceJogadorAtual == indiceSmallBlind) {
                break
            }
        }
    }

    private fun distribuirCartasComunitarias() {
        cartasComunitarias.limpar()
        for (i in 1..3) {
            val carta = baralho.pegarCarta()!!
            cartasComunitarias.adicionarCarta(carta)
            println("Flop: ${carta.valor} de ${carta.naipe}")
        }
        fazerApostasPosFlop()
        if (!continuarJogando) {
            return
        }

        val cartaTurn = baralho.pegarCarta()!!
        cartasComunitarias.adicionarCarta(cartaTurn)
        println("Turn: ${cartaTurn.valor} de ${cartaTurn.naipe}")
        fazerApostasPosFlop()
        if (!continuarJogando) {
            return
        }

        val cartaRiver = baralho.pegarCarta()!!
        cartasComunitarias.adicionarCarta(cartaRiver)
        println("River: ${cartaRiver.valor} de ${cartaRiver.naipe}")
        fazerApostasPosFlop()
    }

    private fun fazerApostasPosFlop() {
        var indiceJogadorAtual = (indiceDoDistribuidor + 1) % jogadores.size

        while (true) {
            val jogadorAtual = jogadores[indiceJogadorAtual]
            val acoesDisponiveis = listOf("Fold", "Check", "Raise")

            println("\n${jogadorAtual.nome}, suas fichas: ${jogadorAtual.fichas}")
            println("Sua mão: ${jogadorAtual.mao.toString()}")
            println("Cartas comunitárias: ${cartasComunitarias.toString()}")
            println("Aposta atual: ${jogadorAtual.mao.obterFichasApostadas()} fichas")
            println("Suas opções: ${acoesDisponiveis.joinToString(", ")}")

            val escolha = if (jogadorAtual == jogadorHumano) {
                println("Escolha uma opção:")
                readLine()
            } else {
                acoesDisponiveis[Random.nextInt(acoesDisponiveis.size)]
            }

            when (escolha) {
                "Fold" -> {
                    println("${jogadorAtual.nome} desistiu.")
                    jogadorAtual.mao.limpar()
                }
                "Check" -> {
                    println("${jogadorAtual.nome} deu check.")
                }
                "Raise" -> {
                    val valorAumento = if (jogadorAtual == jogadorHumano) {
                        println("Digite o valor do aumento (mínimo 20, máximo ${jogadorAtual.fichas}): ")
                        readLine()?.toIntOrNull() ?: 0
                    } else {
                        Random.nextInt(20, jogadorAtual.fichas + 1)
                    }
                    if (valorAumento < 20 || valorAumento > jogadorAtual.fichas) {
                        println("Aposta inválida. Tente novamente.")
                    } else {
                        println("${jogadorAtual.nome} aumentou em $valorAumento fichas.")
                        jogadorAtual.mao.adicionarApostaNasFichas(valorAumento)
                        jogadorAtual.apostar(valorAumento)
                    }
                }
            }

            indiceJogadorAtual = (indiceJogadorAtual + 1) % jogadores.size
            if (indiceJogadorAtual == indiceDoDistribuidor) {
                break
            }
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

        exibirPontuacoesRodada() // Exibe as pontuações após cada rodada
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
        return escolha?.equals("S", ignoreCase = true) == true
    }

    private fun avaliarMao(cartasJogador: List<Carta>, cartasComunitarias: List<Carta>): Int {
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
}
