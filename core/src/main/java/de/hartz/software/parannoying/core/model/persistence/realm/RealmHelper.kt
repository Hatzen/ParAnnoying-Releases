package de.hartz.software.parannoying.core.model.persistence.realm

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.helper.InitializationHelper.UNDEFINED_REALM_FILE_NAME
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.RealmEntityCollection
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import io.realm.Realm
import io.realm.RealmAny
import io.realm.RealmConfiguration
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.exceptions.RealmMigrationNeededException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class RealmHelper(val securityInterfaceHolder: SecurityInterfaceHolder, val context: Context, val moduleConfig: RealmEntityCollection) {

    companion object {
        val ONLINE_FILE_NAME = "deviceData-online"
        val OFFLINE_FILE_NAME = "deviceData-offline"

        fun getFileNameForIdAndRole(id: String = "nonTestFile", deviceRole: DeviceRole): String {
            if (DeviceRole.OFFLINE == deviceRole.roleId) {
                return OFFLINE_FILE_NAME + "-" + id
            } else if (DeviceRole.ONLINE == deviceRole.roleId) {
                return ONLINE_FILE_NAME + "-" + id
            }
            throw RuntimeException("Undefined DeviceRole" + deviceRole + " with Id " + id)
        }

        fun <R> toFieldSelector(vararg props: KProperty1<*, R>): String {
            return props.joinToString(".") { it.name }
        }

        /**
         * Recursively resolves all RealmObject references in a given RealmObject,
         * replacing them with managed versions using their primary key.
         */
        inline fun <reified T : RealmObject> resolveAllRealmReferences(obj: T, realm: Realm): T {
            val visited = mutableSetOf<RealmObject>()
            return resolveRecursive(obj, realm, visited)
        }


        inline fun <reified T : RealmObject> resolveRecursive(
            obj: T, realm: Realm, visited: MutableSet<RealmObject> = mutableSetOf()): T {
            if (obj in visited) return obj
            visited.add(obj)

            val clazz = T::class
            for (prop in clazz.memberProperties) {
                prop.isAccessible = true
                val value = prop.get(obj) ?: continue

                when {
                    // Single RealmObject reference
                    value is RealmObject -> {
                        val managed = resolveObjectByPrimaryKey(value, realm, visited)
                        if (managed != null && prop is kotlin.reflect.KMutableProperty1) {
                            prop.setter.call(obj, managed)
                        }
                    }

                    // RealmList of RealmObjects
                    value is RealmList<*> -> {
                        val resolvedList = RealmList<RealmObject>()

                        for (item in value.filterIsInstance<RealmObject>()) {
                            val managed = resolveObjectByPrimaryKey(item, realm, visited)
                            if (managed != null) {
                                resolvedList.add(managed)
                            }
                        }

                        if (prop is kotlin.reflect.KMutableProperty1) {
                            @Suppress("UNCHECKED_CAST")
                            prop.setter.call(obj, resolvedList as Any)
                        }
                    }
                }
            }

            return obj
        }

        /**
         * Loads a RealmObject by primary key and recurses into it.
         */
        fun resolveObjectByPrimaryKey(obj: RealmObject, realm: Realm, visited: MutableSet<RealmObject>): RealmObject? {
            if (obj.isManaged) {
                return obj
            } else if (obj is UniqueRealmObject) {
                if (obj.persistenceId == UniqueRealmObject.ID_META_NEWEST_ID || obj.persistenceId == UniqueRealmObject.ID_META_DO_NOT_PERSIST) {
                    return obj
                }
            }
            val pkField = getPrimaryKeyField(obj::class) ?: return null
            val pkValue = pkField.getter.call(obj) ?: return null

            val managed = realm.where(obj::class.java).equalTo(pkField.name, pkValue as RealmAny).findFirst()
            return managed?.let { resolveRecursive(it, realm, visited) }
        }

        /**
         * Returns the primary key field of a RealmObject class.
         */
        private fun getPrimaryKeyField(kclass: KClass<*>): kotlin.reflect.KProperty1<out Any, *>? {
            return kclass.memberProperties.firstOrNull { it.findAnnotation<PrimaryKey>() != null }
        }
    }

    private var realmConfig: RealmConfiguration? = null

    var password: String? = null
    // Only changeable for tests to not override real device data of the app.
    var FILE_NAME: String

    init {
        FILE_NAME = InitializationHelper.getRealmFileName(context)
        // TODO: we should probably use something more useful
        //      InitializationHelper.isInitialized(context)
        //      Maybe use nullable realmhelper provide and creat storage only with
        if (FILE_NAME != UNDEFINED_REALM_FILE_NAME) {
            Realm.init(context)

            loadPassword(context)
            initRealmConfig(moduleConfig)
        }
    }

    fun loadPassword (context: Context, initialPassword: String? = null) {
        val ESP_KEY = "PASS"
        val fileName = "secret_shared_prefs"
        val sharedPreferences: SharedPreferences
        // TODO: Encrypt? https://stackoverflow.com/a/51381469/8524651
        sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)

        password = sharedPreferences.getString(ESP_KEY, null)
        if (password == null || initialPassword != null) {
            if (initialPassword == null) {
                password = securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(64)
                password = securityInterfaceHolder.hardcodedEncryptionHelper.encrypt(password!!)
            } else {
                password = initialPassword
            }
            sharedPreferences.edit()
                    .putString(ESP_KEY, password)
                    .apply()
        }
    }

    fun isMigrationNeeded(): Boolean  {
        return try {
            val clearedPassword = securityInterfaceHolder.hardcodedEncryptionHelper.decrypt(password!!)
            // The Realm file will be located in Context.getFilesDir() with name "myrealm.realm"
            val config = RealmConfiguration.Builder()
                .name(FILE_NAME)
                .schemaVersion(moduleConfig.migration.NEWEST_REALM_VERSION)
                .modules(moduleConfig)
                // TODO: Enable for next e2e test results, currently fails as generated files dont have a key..
                // .encryptionKey(clearedPassword.toByteArray())
                .build()
            // Try opening Realm with config
            Realm.getInstance(config)
                .use { realm ->
                        // No migration needed
                }
            false
        } catch (e: RealmMigrationNeededException) {
            // Migration is needed
            true
        }
    }

    // TODO: refactore to pass a unit lambda and close sessions afterwards. Then count active instances
    fun getThreadInstance (): Realm {
        val config = getRealmConfig()
        // Use the config
        return Realm.getInstance(config)
    }

    /**
     * Method is just for debugging purpose to make kotlin properties visible.
     * https://stackoverflow.com/a/70769881/8524651
     *
     * https://stackoverflow.com/questions/31420948/android-realm-debugging
     */
    fun getDebuggableInstance(realmObject: RealmObject): Any {
        // TODO: Crash when release build?
        return getThreadInstance().copyFromRealm(realmObject)
    }

    fun deleteRealmAndCloseCurrentSession() {
        if (getRealmConfig() == null) {
            return
        }
        var counter = 1
        var exception: Exception?
        do {
            exception = null
            try {
                Realm.deleteRealm(getRealmConfig())
                Log.w(javaClass.simpleName, "deleteRealmAndCloseCurrentSession Successfull")
            } catch (e: Exception) {
                Log.w(javaClass.simpleName, "deleteRealmAndCloseCurrentSession Failed", e)
                exception = e
                counter++
                val latch = CountDownLatch(1)
                Thread {
                    Thread.sleep(3000)
                    latch.countDown()
                }.start()
                latch.await(5, TimeUnit.SECONDS)
                if (counter > 4) {
                    // TODO: We should throw an exception at this moment?
                    Log.e(javaClass.simpleName, "deleteRealmAndCloseCurrentSession Failed completly on 4th trial.", e)
                    break
                }
            }
        } while (exception != null)
    }

    private fun initRealmConfig (moduleConfig: RealmEntityCollection) {
        val clearedPassword = securityInterfaceHolder.hardcodedEncryptionHelper.decrypt(password!!)
        // The Realm file will be located in Context.getFilesDir() with name "myrealm.realm"
        val realmConfigBuilder = RealmConfiguration.Builder()
                .name(FILE_NAME)
                .allowWritesOnUiThread(true) // TODO: This should be handled and then removed.
                .schemaVersion(moduleConfig.migration.NEWEST_REALM_VERSION)
                .modules(moduleConfig) // Needed so instrumentedtest finds the model.
        // .compactOnLaunch()
            // TODO: https://github.com/Hatzen/ParAnnoying/issues/97  /  https://realm.io/docs/java/latest/#limitations-multiprocess


            // TODO: Enable for next e2e test results, currently fails as generated files dont have a key..
            // realmConfigBuilder.encryptionKey(clearedPassword.toByteArray())
            // TODO: This lead to errors in test when switching the device.

        val runMigration = true
        // Since we have E2E Test files we dont want to delete all files..
        if (!runMigration && StorageInterface.DEVELOPER_MODE) { // When not in developing mode and updating it should crash so downgrade is possible.
            realmConfigBuilder
                    .deleteRealmIfMigrationNeeded()
        } else {
            realmConfigBuilder
                .migration(moduleConfig.migration)
        }
        realmConfig = realmConfigBuilder.build()
    }

    private fun getRealmConfig (): RealmConfiguration? {
        return realmConfig
    }

}