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
import cz.cvut.fel.android_app.domain.model.User
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

    val user: Flow<User?> = context.dataStore.data.map { prefs ->
        val firstName = prefs[FIRST_NAME] ?: return@map null
        val lastName = prefs[LAST_NAME] ?: return@map null
        val email = prefs[EMAIL] ?: return@map null
        val multipointMeasurement = prefs[MULTIPOINT_MEASUREMENT] ?: return@map null
        val singlePointHeight = prefs[SINGLEPOINT_HEIGHT] ?: return@map null
        val preferredUnit = prefs[PREFERRED_UNIT]?.let { MeasurementUnit.valueOf(it) } ?: MeasurementUnit.HYDROMETRIC
        User(firstName, lastName, email, multipointMeasurement, singlePointHeight, preferredUnit)
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { prefs ->
            prefs[FIRST_NAME] = user.firstName
            prefs[LAST_NAME] = user.lastName
            prefs[EMAIL] = user.email
            prefs[MULTIPOINT_MEASUREMENT] = user.multipointMeasurement
            prefs[SINGLEPOINT_HEIGHT] = user.singlePointHeight
            prefs[PREFERRED_UNIT] = user.preferredUnit.name
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { it.clear() }
    }
}