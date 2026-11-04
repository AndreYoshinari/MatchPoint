package br.com.matchpoint.app

import android.app.Application
import br.com.matchpoint.di.dataModule
import br.com.matchpoint.di.domainModule
import br.com.matchpoint.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MatchPointApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MatchPointApp)
            modules(presentationModule, domainModule, dataModule)
        }
    }
}
