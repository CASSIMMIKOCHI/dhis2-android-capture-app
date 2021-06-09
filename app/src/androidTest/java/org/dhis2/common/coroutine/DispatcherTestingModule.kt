package org.dhis2.common.coroutine

import dagger.Module
import dagger.Provides
import org.dhis2.data.dispatcher.DispatcherModule
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.coroutine.TestingDispatcher
import javax.inject.Singleton

@Module
class DispatcherTestingModule(): DispatcherModule() {

    @Provides
    @Singleton
    override fun provideDispatcherModule(): DispatcherProvider {
        return TestingDispatcher()
    }
}