package br.com.matchpoint.di

import androidx.room.Room
import br.com.matchpoint.data.local.db.MatchPointDatabase
import br.com.matchpoint.data.repository.MatchRepositoryImpl
import br.com.matchpoint.domain.repository.MatchRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            MatchPointDatabase::class.java,
            "matchpoint.db",
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single<MatchRepository> { MatchRepositoryImpl(get()) }
}
