/*
 * Copyright 2023 Atick Faisal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.atick.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TranslatorScreen()
                }
            }
        }
    }
}

@Composable
fun TranslatorScreen() {
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("Ожидание...") }
    var isReady by remember { mutableStateOf(false) }

    val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.RUSSIAN)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()
    val translator = remember { Translation.getClient(options) }

    // Безопасная загрузка модели
    LaunchedEffect(Unit) {
        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { isReady = true; output = "Готов к работе" }
            .addOnFailureListener { output = "Ошибка загрузки словаря" }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = input,
            onValueChange = { 
                input = it
                if (isReady && it.isNotEmpty()) {
                    translator.translate(it)
                        .addOnSuccessListener { output = it }
                        .addOnFailureListener { output = "Ошибка перевода" }
                }
            },
            label = { Text("Введите текст") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = output, style = MaterialTheme.typography.bodyLarge)
    }
}
