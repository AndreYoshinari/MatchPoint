package br.com.matchpoint.di

import br.com.matchpoint.domain.usecase.AddPlayerUseCase
import br.com.matchpoint.domain.usecase.CreateMatchUseCase
import br.com.matchpoint.domain.usecase.FinishMatchUseCase
import br.com.matchpoint.domain.usecase.GetGlobalStatsUseCase
import br.com.matchpoint.domain.usecase.GetMatchStatsUseCase
import br.com.matchpoint.domain.usecase.ObserveMatchHistoryUseCase
import br.com.matchpoint.domain.usecase.ObserveMatchUseCase
import br.com.matchpoint.domain.usecase.RegisterPointUseCase
import br.com.matchpoint.domain.usecase.RemovePlayerUseCase
import br.com.matchpoint.domain.usecase.StartMatchUseCase
import br.com.matchpoint.domain.usecase.UndoLastPointUseCase
import br.com.matchpoint.domain.usecase.UpdateMatchSetupUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { CreateMatchUseCase(get()) }
    factory { UpdateMatchSetupUseCase(get()) }
    factory { AddPlayerUseCase(get()) }
    factory { RemovePlayerUseCase(get()) }
    factory { StartMatchUseCase(get()) }
    factory { RegisterPointUseCase(get()) }
    factory { UndoLastPointUseCase(get()) }
    factory { FinishMatchUseCase(get()) }
    factory { ObserveMatchUseCase(get()) }
    factory { ObserveMatchHistoryUseCase(get()) }
    factory { GetMatchStatsUseCase(get()) }
    factory { GetGlobalStatsUseCase(get()) }
}
