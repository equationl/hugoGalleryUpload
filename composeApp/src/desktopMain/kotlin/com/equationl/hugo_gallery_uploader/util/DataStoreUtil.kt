package com.equationl.hugo_gallery_uploader.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File
import kotlin.io.path.pathString


/**
 * Gets the singleton DataStore instance, creating it if necessary.
 */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal const val dataStoreFileName = "prefs.preferences_pb"

// 这里如果不指定一个储存目录的话，如果是运行的打包程序，则会闪退，应该是没权限写文件到自己的包里面？？？
// val dataStore: DataStore<Preferences> = createDataStore { dataStoreFileName }
val dataStore: DataStore<Preferences> = createDataStore {
    val dataPath = UserDataDir.getAppPath().pathString
    println("dataPath = $dataPath")
    File(dataPath, dataStoreFileName).absolutePath
}

