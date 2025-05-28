package no.uio.ifi.in2000.simonng.simonng.team1.data.onBoarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "trygve_onboarding")

class OnBoardingRepository private constructor(private val applicationContext: Context) {

    companion object {
        private val FORSTE_START = booleanPreferencesKey("firstStart")

        @Volatile
        private var INSTANCE: OnBoardingRepository? = null

        fun getInstance(context: Context): OnBoardingRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = OnBoardingRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    val onboardingStatusFlow: Flow<Boolean> = applicationContext.onboardingDataStore.data
        .map { preferences -> preferences[FORSTE_START] ?: true }

    suspend fun setOnboardingCompleted(state: Boolean) {
        applicationContext.onboardingDataStore.edit { preferences ->
            preferences[FORSTE_START] = state
        }
    }
}