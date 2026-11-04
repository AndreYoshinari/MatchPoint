package br.com.matchpoint.presentation.error

import br.com.matchpoint.domain.model.DomainError

fun DomainError.toUiMessage(): String = when (this) {
    DomainError.MatchNotFound -> "Partida não encontrada."
    DomainError.MatchNotEditable -> "Esta partida não pode mais ser editada."
    DomainError.MatchNotInProgress -> "A partida ainda não foi iniciada."
    DomainError.MatchAlreadyFinished -> "A partida já foi encerrada."
    DomainError.InvalidRoster -> "Cada time precisa de pelo menos um jogador."
    DomainError.InvalidSettings -> "As regras da partida são inválidas."
    DomainError.InvalidServingTeam -> "Escolha um time válido para o saque."
    DomainError.PlayerNotFound -> "Jogador não encontrado."
    DomainError.BlankTeamName -> "Informe o nome dos dois times."
    DomainError.BlankPlayerName -> "Informe o nome do jogador."
    DomainError.NoPointsToUndo -> "Não há ponto para desfazer."
}
