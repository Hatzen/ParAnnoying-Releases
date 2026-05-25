package de.hartz.software.parannoying.app.utils

import dagger.Module
import dagger.Provides
import io.mockk.mockk
import javax.inject.Singleton

@Module
class TestModule {

    // TODO: replace with something useful
    @Provides
    @Singleton
    fun provideMockRepository(): Object {
        return mockk()
    }
}