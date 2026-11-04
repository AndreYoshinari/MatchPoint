package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.IdGenerator
import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.model.Team
import br.com.matchpoint.domain.repository.MatchRepository

class CreateMatchUseCase(
    private val repository: MatchRepository,
    private val idGenerator: IdGenerator = IdGenerator.uuid,
) {
    suspend operator fun invoke(
        homeName: String = "Time A",
        awayName: String = "Time B",
        settings: MatchSettings = MatchSettings.indoorDefault(),
    ): DomainResult<Match> {
        if (!settings.isValid()) return DomainResult.Failure(DomainError.InvalidSettings)
        if (homeName.isBlank() || awayName.isBlank()) {
            return DomainResult.Failure(DomainError.BlankTeamName)
        }
        val match = Match(
            id = idGenerator.next(),
            status = MatchStatus.SETUP,
            settings = settings,
            home = Team(id = idGenerator.next(), name = homeName.trim()),
            away = Team(id = idGenerator.next(), name = awayName.trim()),
        )
        repository.create(match)
        return DomainResult.Success(match)
    }
}
