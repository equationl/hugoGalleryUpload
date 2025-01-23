package com.equationl.hugo_gallery_uploader.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

// FIXME need confirm https://stackoverflow.com/questions/79177464/kmp-compose-multiplatform-datastore-not-working-on-jvm-release-build


/**
 * Gets the singleton DataStore instance, creating it if necessary.
 */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal const val dataStoreFileName = "prefs.preferences_pb"

val dataStore: DataStore<Preferences> = createDataStore { dataStoreFileName }
