package com.plasmavan.geocue

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
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
import androidx.compose.ui.unit.dp
import com.plasmavan.geocue.ui.theme.GeocueTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun HomeBase(options: ArrayList<String>, supabase: SupabaseClient) {
        var searchText by rememberSaveable { mutableStateOf("") }
        var menuText by remember { mutableStateOf(options[12]) }
        var expanded by remember { mutableStateOf(false) }
        var geoItems by remember { mutableStateOf<List<GeoData>>(listOf()) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                geoItems = supabase.from("geographic")
                    .select().decodeList<GeoData>()
            }
        }
        
        val filteredItems by remember { derivedStateOf { geoItems.filter { it.name.contains(searchText, ignoreCase = true) } } }

        Column {
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
                    label = { Text( text = "観光地名" ) },
                    placeholder = { Text( text = "例）東京ドームシティ")}
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
                            label = { Text("都道府県") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
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
                                    val uri = Uri.parse(item.url)
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = "Edit"
                                )
                            }
                            IconButton(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("google.streetview:cbll=${item.map}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    startActivity(mapIntent)
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

//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(32.dp),
//            contentAlignment = Alignment.BottomEnd
//        ) {
//            FloatingActionButton(
//                onClick = {},
//                containerColor = MaterialTheme.colorScheme.background
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.Favorite,
//                    contentDescription = "お気に入りを追加"
//                )
//            }
//        }
    }
}