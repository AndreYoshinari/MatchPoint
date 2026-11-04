package br.com.matchpoint.domain.model

sealed class DomainError {
    data object MatchNotFound : DomainError()
    data object MatchNotEditable : DomainError()
    data object MatchNotInProgress : DomainError()
    data object MatchAlreadyFinished : DomainError()
    data object InvalidRoster : DomainError()
    data object InvalidSettings : DomainError()
    data object InvalidServingTeam : DomainError()
    data object PlayerNotFound : DomainError()
    data object BlankTeamName : DomainError()
    data object BlankPlayerName : DomainError()
    data object NoPointsToUndo : DomainError()
}
