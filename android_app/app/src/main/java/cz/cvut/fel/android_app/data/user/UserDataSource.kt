package cz.cvut.fel.android_app.data.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserDataSource(private val context: Context) {

    companion object {
        private val FIRST_NAME = stringPreferencesKey("first_name")
        private val LAST_NAME = stringPreferencesKey("last_name")
        private val EMAIL = stringPreferencesKey("email")
        private val MULTIPOINT_MEASUREMENT = booleanPreferencesKey("multipoint_measurement")
        private val SINGLEPOINT_HEIGHT = doublePreferencesKey("singlepoint_height")
        private val PREFERRED_UNIT = stringPreferencesKey("preferred_unit")
    }

    val userProfile: Flow<UserProfile?> = context.dataStore.data.map { prefs ->
        val firstName = prefs[FIRST_NAME] ?: ""
        val lastName = prefs[LAST_NAME] ?: ""
        val email = prefs[EMAIL] ?: ""
        val multipointMeasurement = prefs[MULTIPOINT_MEASUREMENT] ?: true
        val singlePointHeight = prefs[SINGLEPOINT_HEIGHT] ?: 0.6
        val preferredUnit = prefs[PREFERRED_UNIT]?.let { MeasurementUnit.valueOf(it) } ?: MeasurementUnit.HYDROMETRIC
        if (firstName.isEmpty() && lastName.isEmpty() && email.isEmpty() && prefs[PREFERRED_UNIT] == null) return@map null
        UserProfile(firstName, lastName, email, multipointMeasurement, singlePointHeight, preferredUnit)
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[FIRST_NAME] = profile.firstName
            prefs[LAST_NAME] = profile.lastName
            prefs[EMAIL] = profile.email
            prefs[MULTIPOINT_MEASUREMENT] = profile.multipointMeasurement
            prefs[SINGLEPOINT_HEIGHT] = profile.singlePointHeight
            prefs[PREFERRED_UNIT] = profile.preferredUnit.name
        }
    }

    suspend fun clearUserProfile() {
        context.dataStore.edit { it.clear() }
    }
}
