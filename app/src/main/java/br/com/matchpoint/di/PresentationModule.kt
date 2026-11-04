package br.com.matchpoint.di

import br.com.matchpoint.presentation.home.HomeViewModel
import br.com.matchpoint.presentation.matchsetup.MatchSetupViewModel
import br.com.matchpoint.presentation.roster.RosterViewModel
import br.com.matchpoint.presentation.scoreboard.ScoreboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::MatchSetupViewModel)
    viewModelOf(::RosterViewModel)
    viewModelOf(::ScoreboardViewModel)
}
