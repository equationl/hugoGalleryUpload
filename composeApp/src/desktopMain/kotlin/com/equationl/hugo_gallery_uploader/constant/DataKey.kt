package com.equationl.hugo_gallery_uploader.constant

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataKey {
    val TIME_ZONE = stringPreferencesKey("TIME_ZONE")
    val OBS_BUCKET = stringPreferencesKey("OBS_BUCKET")
    val OBS_SAVE_FOLDER = stringPreferencesKey("OBS_SAVE_FOLDER")
    val OBS_ACCESS_KEY = stringPreferencesKey("OBS_ACCESS_KEY")
    val OBS_SECRET_KEY = stringPreferencesKey("OBS_SECRET_KEY")
    val OBS_ENDPOINT = stringPreferencesKey("OBS_ENDPOINT")
    val IS_AUTO_CREATE_FOLDER = booleanPreferencesKey("IS_AUTO_CREATE_FOLDER")
}