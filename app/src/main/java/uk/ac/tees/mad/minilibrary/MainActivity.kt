package uk.ac.tees.mad.minilibrary

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import uk.ac.tees.mad.minilibrary.navigation.NavGraph
import uk.ac.tees.mad.minilibrary.ui.theme.MiniLibraryTheme

val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            val context = this
            val scope = rememberCoroutineScope()

            val isDarkTheme by context.dataStore.data
                .map { prefs: Preferences ->
                    prefs[DARK_THEME_KEY] ?: false
                }
                .collectAsState(initial = false)

            MiniLibraryTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()

                    NavGraph(
                        navController = navController,
                        isDarkTheme = isDarkTheme,

                        onThemeChange = { newValue ->
                            scope.launch {
                                saveTheme(context, newValue)
                            }
                        }
                    )
                }
            }
        }
    }
}

suspend fun saveTheme(context: Context, isDark: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[MainActivity.DARK_THEME_KEY] = isDark
    }
}
