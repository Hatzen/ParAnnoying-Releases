package de.hartz.software.parannoying.app.utils

import dagger.Component
import de.hartz.software.parannoying.app.helper.provider.SecurityModule
import de.hartz.software.parannoying.app.medium.MigrationTest
import de.hartz.software.parannoying.app.medium.RealmExportImportTest
import de.hartz.software.parannoying.app.small.FileMessageTest
import javax.inject.Singleton

@Singleton
@Component(modules = [TestModule::class, SecurityModule::class])
interface TestComponent {
    fun inject(test: FileMessageTest)
    fun inject(test: RealmExportImportTest)
    fun inject(test: MigrationTest)


    @Component.Builder
    interface Builder {
        fun build(): TestComponent
    }
}