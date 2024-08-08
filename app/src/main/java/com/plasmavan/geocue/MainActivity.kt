package com.plasmavan.geocue

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.plasmavan.geocue.ui.theme.GeocueTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val language = prefs.getString("app_language", "en") ?: "en"

        setLocale(language)

        val prefecturesList: ArrayList<String> = arrayListOf()
        try {
            val csvFile = resources.assets.open("prefectures.csv")
            val fileReader = BufferedReader(InputStreamReader(csvFile))
            fileReader.forEachLine {
                prefecturesList.add(it)
            }
        } catch (e: Exception) {
            Log.e("Error", "$e")
        }

        val supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.supabaseUrl,
            supabaseKey = BuildConfig.supabaseKey
        ) {
            defaultSerializer = KotlinXSerializer(Json)
            install(Auth)
            install(Postgrest) {
                defaultSchema = "api"
            }
        }

        setContent {
            GeocueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeBase(prefecturesList, supabase)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun HomeBase(options: ArrayList<String>, supabase: SupabaseClient) {
        var searchText by rememberSaveable { mutableStateOf("") }
        var menuText by remember { mutableStateOf(options[12]) }
        var expanded by remember { mutableStateOf(false) }
        var geoItems by remember { mutableStateOf<List<GeoData>>(listOf()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                geoItems = supabase.from("geographic")
                    .select().decodeList<GeoData>()
                isLoading = false
            }
        }

        val filteredPrefectures by remember {
            derivedStateOf {
                geoItems.filter {
                    it.prefecture.contains(
                        menuText,
                        ignoreCase = true
                    )
                }
            }
        }
        val filteredItems by remember {
            derivedStateOf {
                filteredPrefectures.filter {
                    it.name.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
            }
        }

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
                            text = getString(R.string.app_name)
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val intent =
                                    Intent(applicationContext, SettingsActivity::class.java)
                                startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text(text = getString(R.string.tourist_attraction_name)) },
                    placeholder = { Text(text = getString(R.string.tourist_attraction_name_example)) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Row {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        TextField(
                            modifier = Modifier.menuAnchor(),
                            value = menuText,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text(text = getString(R.string.prefectures_of_japan)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        menuText = option
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                }
            }

            if(isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            text = getString(R.string.loading_now),
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier,
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                }
            } else {
                LazyColumn {
                    items(
                        filteredItems,
                        key = { item -> item.id },
                    ) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.name,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                IconButton(
                                    onClick = {
//                                    val uri = Uri.parse(item.url)
//                                    startActivity(Intent(Intent.ACTION_VIEW, uri))

                                        val intent =
                                            Intent(applicationContext, WebViewActivity::class.java)
                                        intent.putExtra("selectedUrl", item.url)
                                        intent.putExtra("selectedName", item.name)

                                        startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Info,
                                        contentDescription = "Edit"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val trimItem: String = item.map.replace(" ", "")
                                        val splitList: List<String> = trimItem.split(",")

                                        val latitude: String = splitList[0]
                                        val longitude: String = splitList[1]

                                        val uri =
                                            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        intent.setPackage("com.google.android.apps.maps")

                                        startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = "Delete"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setLocale(localeName: String) {
        val locale = Locale(localeName)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}