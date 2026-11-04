package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.repository.MatchRepository

class UpdateMatchSetupUseCase(
    private val repository: MatchRepository,
) {
    suspend operator fun invoke(
        matchId: String,
        homeName: String,
        awayName: String,
        settings: MatchSettings,
    ): DomainResult<Match> {
        val match = repository.getById(matchId)
            ?: return DomainResult.Failure(DomainError.MatchNotFound)
        if (match.status != MatchStatus.SETUP) {
            return DomainResult.Failure(DomainError.MatchNotEditable)
        }
        if (!settings.isValid()) return DomainResult.Failure(DomainError.InvalidSettings)
        if (homeName.isBlank() || awayName.isBlank()) {
            return DomainResult.Failure(DomainError.BlankTeamName)
        }
        val updated = match.copy(
            settings = settings,
            home = match.home.copy(name = homeName.trim()),
            away = match.away.copy(name = awayName.trim()),
        )
        repository.update(updated)
        return DomainResult.Success(updated)
    }
}
