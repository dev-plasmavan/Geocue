package com.plasmavan.geocue

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.plasmavan.geocue.ui.theme.GeocueTheme
import java.util.*

class SettingsActivity : ComponentActivity() {

    private val PREFS_NAME = "AppSettings"
    private val PREFS_KEY_LANGUAGE = "selected_language"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GeocueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsActivityUI()
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            super.onKeyDown(keyCode, event)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SettingsActivityUI() {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString(PREFS_KEY_LANGUAGE, Locale.getDefault().language)
        val defaultLanguage = Locale.getDefault().language
        var selectedLanguage by remember { mutableStateOf(savedLanguage ?: defaultLanguage) }

        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(
                            text = "Settings"
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                finish()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column {
                    Text(
                        text = getString(R.string.language_selection),
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .selectableGroup()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedLanguage == Locale.JAPANESE.language,
                                onClick = {
                                    selectedLanguage = Locale.JAPANESE.language
                                    saveLanguagePreference(Locale.JAPANESE.language, context)
                                    restartApp(context)
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = getString(R.string.japanese),
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedLanguage == Locale.ENGLISH.language,
                                onClick = {
                                    selectedLanguage = Locale.ENGLISH.language
                                    saveLanguagePreference(Locale.ENGLISH.language, context)
                                    restartApp(context)
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = getString(R.string.english),
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun saveLanguagePreference(language: String, context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(PREFS_KEY_LANGUAGE, language)
            apply()
        }
    }

    private fun restartApp(context: Context) {
        Toast.makeText(context, getString(R.string.changed_language), Toast.LENGTH_LONG)
            .show()

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(it)
            (context as? ComponentActivity)?.finish()
        }
    }
}
