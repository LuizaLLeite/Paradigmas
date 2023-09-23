import kotlin.random.Random

class PokerGame(private val numAIPlayers: Int) {
    private val deck = Deck()
    private val humanPlayer = Player("Player 1 (Humano)", 1000)
    private val aiPlayers = mutableListOf<Player>()
    private val players: List<Player>

    private val communityCards = Hand()

    private var continuarJogando = true
    private var dealerIndex = 0

    init {
        require(numAIPlayers >= 2) { "Deve haver pelo menos dois jogadores controlados pela máquina." }

        deck.shuffle()
        players = mutableListOf(humanPlayer)
        for (i in 1..numAIPlayers) {
            aiPlayers.add(Player("Player ${i + 1} (AI)", 1000))
            players.add(aiPlayers[i - 1])
        }
    }

    private fun dealHands() {
        dealerIndex = determineDealer()
        for (i in 0 until players.size) {
            val playerIndex = (dealerIndex + i + 1) % players.size
            players[playerIndex].hand.clear()
            players[playerIndex].hand.addCard(deck.dealCard()!!)
        }
    }

    private fun determineDealer(): Int {
        // Embaralha o baralho e distribui uma carta para cada jogador.
        deck.shuffle()
        val dealerCardRanks = mutableListOf<Rank>()
        for (i in 0 until players.size) {
            val playerCard = deck.dealCard()!!
            println("${players[i].name} recebeu ${playerCard.rank} de ${playerCard.suit}")
            dealerCardRanks.add(playerCard.rank)
        }
        // Determina o dealer com base na maior carta.
        val maxRank = dealerCardRanks.maxOrNull() ?: Rank.TWO
        return dealerCardRanks.indexOf(maxRank)
    }

    fun play() {
        println("Bem-vindo ao Texas Hold'em Poker!")
        println("Você possui 1000 fichas. O objetivo é vencer os jogadores controlados pela máquina.")

        while (continuarJogando) {
            deck.shuffle()
            communityCards.clear()
            for (player in players) {
                player.hand.clear()
            }

            dealHands()

            println("\nDealer: ${players[dealerIndex].name}")

            // Realiza o procedimento de apostas com base nas regras que você descreveu.
            fazerApostasPreFlop()

            if (!continuarJogando) {
                break
            }

            dealCommunityCards()

            // Procedimento de apostas pós-flop
            fazerApostasPosFlop()

            if (!continuarJogando) {
                break
            }

            // Descida das cartas (Showdown)
            showdown()

            // Pergunta se os jogadores querem continuar após cada rodada
            continuarJogando = continuePlaying()
        }
    }

    private fun fazerApostasPreFlop() {
        val smallBlindIndex = (dealerIndex + 1) % players.size
        val bigBlindIndex = (dealerIndex + 2) % players.size

        val smallBlind = players[smallBlindIndex]
        val bigBlind = players[bigBlindIndex]


        println("\n${smallBlind.name} é o small blind e aposta 10 fichas.")
        smallBlind.hand.addToChipsBet(10)
        smallBlind.bet(10)

        println("\n${bigBlind.name} é o big blind e aposta 20 fichas.")
        bigBlind.hand.addToChipsBet(20)
        bigBlind.bet(20)

        var currentPlayerIndex = (dealerIndex + 3) % players.size

        while (true) {
            val currentPlayer = players[currentPlayerIndex]
            val availableActions = listOf("Fold", "Check", "Raise")

            println("\n${currentPlayer.name}, suas fichas: ${currentPlayer.chips}")
            println("Sua mão: ${currentPlayer.hand.toString()}")
            println("Cartas comunitárias: ${communityCards.toString()}")
            println("Aposta atual: ${currentPlayer.hand.getChipsBet()} fichas")
            println("Suas opções: ${availableActions.joinToString(", ")}")

            val choice = if (currentPlayer == humanPlayer) {
                println("Escolha uma opção:")
                readLine()
            } else {
                availableActions[Random.nextInt(availableActions.size)]
            }

            when (choice) {
                "Fold" -> {
                    println("${currentPlayer.name} desistiu.")
                    currentPlayer.hand.clear()
                }
                "Check" -> {
                    println("${currentPlayer.name} deu check.")
                }
                "Raise" -> {
                    var raiseAmount = if (currentPlayer == humanPlayer) {
                        println("Digite o valor do aumento (mínimo 20, máximo ${currentPlayer.chips}): ")
                        readLine()?.toIntOrNull() ?: 0
                    } else {
                        Random.nextInt(20, currentPlayer.chips)
                    }
                    while(raiseAmount < 20 || raiseAmount > currentPlayer.chips){
                        println("Aposta inválida. Tente novamente.")
                        raiseAmount = readLine()?.toIntOrNull() ?: 0
                    }
                    println("${currentPlayer.name} aumentou a aposta em $raiseAmount fichas.")
                    currentPlayer.hand.addToChipsBet(raiseAmount)
                    currentPlayer.bet(raiseAmount)
                }
            }

            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
            if (currentPlayerIndex == smallBlindIndex) {
                break
            }
        }
    }

    private fun dealCommunityCards() {
        communityCards.clear()
        for (i in 1..3) {
            val card = deck.dealCard()!!
            communityCards.addCard(card)
            println("Flop: ${card.rank} de ${card.suit}")
        }
        fazerApostasPosFlop()
        if (!continuarJogando) {
            return
        }

        val turnCard = deck.dealCard()!!
        communityCards.addCard(turnCard)
        println("Turn: ${turnCard.rank} de ${turnCard.suit}")
        fazerApostasPosFlop()
        if (!continuarJogando) {
            return
        }

        val riverCard = deck.dealCard()!!
        communityCards.addCard(riverCard)
        println("River: ${riverCard.rank} de ${riverCard.suit}")
        fazerApostasPosFlop()
    }

    private fun fazerApostasPosFlop() {
        var currentPlayerIndex = (dealerIndex + 1) % players.size

        while (true) {
            val currentPlayer = players[currentPlayerIndex]
            val availableActions = listOf("Fold", "Check", "Raise")

            println("\n${currentPlayer.name}, suas fichas: ${currentPlayer.chips}")
            println("Sua mão: ${currentPlayer.hand.toString()}")
            println("Cartas comunitárias: ${communityCards.toString()}")
            println("Aposta atual: ${currentPlayer.hand.getChipsBet()} fichas")
            println("Suas opções: ${availableActions.joinToString(", ")}")

            val choice = if (currentPlayer == humanPlayer) {
                println("Escolha uma opção:")
                readLine()
            } else {
                availableActions[Random.nextInt(availableActions.size)]
            }

            when (choice) {
                "Fold" -> {
                    println("${currentPlayer.name} desistiu.")
                    currentPlayer.hand.clear()
                }
                "Check" -> {
                    println("${currentPlayer.name} deu check.")
                }
                "Raise" -> {
                    val raiseAmount = if (currentPlayer == humanPlayer) {
                        println("Digite o valor do aumento (mínimo 20, máximo ${currentPlayer.chips}): ")
                        readLine()?.toIntOrNull() ?: 0
                    } else {
                        Random.nextInt(20, currentPlayer.chips + 1)
                    }
                    if (raiseAmount < 20 || raiseAmount > currentPlayer.chips) {
                        println("Aposta inválida. Tente novamente.")
                    } else {
                        println("${currentPlayer.name} aumentou em $raiseAmount fichas.")
                        currentPlayer.hand.addToChipsBet(raiseAmount)
                        currentPlayer.bet(raiseAmount)
                    }
                }
            }

            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
            if (currentPlayerIndex == dealerIndex) {
                break
            }
        }
    }

    private fun showdown() {
        val remainingPlayers = players.filter { it.hand.getCards().isNotEmpty() }

        if (remainingPlayers.size == 1) {
            val winner = remainingPlayers.first()
            println("\nResultado: ${winner.name} venceu a rodada!")
        } else {
            val handsWithPlayers = remainingPlayers.associateBy({ it }, { evaluateHand(it.hand.getCards(), communityCards.getCards()) })
            val winningPlayer = handsWithPlayers.maxByOrNull { it.value }?.key
            println("\nResultado: ${winningPlayer?.name} venceu a rodada!")
        }

        displayRoundScores() // Exibe o placar após cada rodada
    }

    private fun displayRoundScores() {
        println("\nPlacar da Rodada:")
        for (player in players) {
            println("${player.name}: ${player.chips} fichas")
        }
    }

    private fun continuePlaying(): Boolean {
        if (players.any { it.chips <= 0 }) {
            println("Fim do jogo. Um dos jogadores ficou sem fichas.")
            return false
        }

        println("\nDeseja continuar jogando? (S para sim, qualquer outra tecla para sair)")
        val choice = readLine()
        return choice?.equals("S", ignoreCase = true) == true
    }

    private fun evaluateHand(playerCards: List<Card>, communityCards: List<Card>): Int {
        val allCards = playerCards + communityCards
        val groupedByRank = allCards.groupBy { it.rank }
        val maxOccurrences = groupedByRank.values.map { it.size }.maxOrNull() ?: 0

        return when {
            maxOccurrences >= 5 -> 8  // Full House
            maxOccurrences >= 4 -> 7  // Four of a Kind
            maxOccurrences >= 3 -> {
                if (groupedByRank.values.any { it.size >= 2 }) 6 else 3  // Three of a Kind ou One Pair
            }
            groupedByRank.size == 5 && isConsecutive(allCards) -> 5  // Straight
            groupedByRank.size == 5 -> 4  // High Card
            groupedByRank.size == 4 -> 2  // Two Pair
            else -> 1  // One Pair
        }
    }

    private fun isConsecutive(cards: List<Card>): Boolean {
        val ranks = cards.map { it.rank.value }.distinct().sorted()
        return ranks == (ranks.first()..ranks.last()).toList()
    }
}