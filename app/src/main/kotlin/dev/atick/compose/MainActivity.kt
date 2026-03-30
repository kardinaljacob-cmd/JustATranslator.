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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TranslatorCard()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorCard() {
    var sourceText by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var isRuToEn by remember { mutableStateOf(true) } // Переключатель направления
    
    var isModelReady by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Загрузка...") }

    // Настройки меняются при нажатии на кнопку переключения
    val options = remember(isRuToEn) {
        TranslatorOptions.Builder()
            .setSourceLanguage(if (isRuToEn) TranslateLanguage.RUSSIAN else TranslateLanguage.ENGLISH)
            .setTargetLanguage(if (isRuToEn) TranslateLanguage.ENGLISH else TranslateLanguage.RUSSIAN)
            .build()
    }
    
    val translator = remember(options) { Translation.getClient(options) }

    // Загрузка нужного словаря
    LaunchedEffect(options) {
        isModelReady = false
        statusMessage = "Загрузка словаря..."
        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { 
                isModelReady = true 
                statusMessage = "Готов к работе"
            }
            .addOnFailureListener { 
                statusMessage = "Ошибка загрузки словаря"
            }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Переводчик", style = MaterialTheme.typography.headlineSmall)
                    
                    // Кнопка смены языка
                    TextButton(onClick = { 
                        isRuToEn = !isRuToEn 
                        sourceText = ""
                        targetText = ""
                    }) {
                        Text(if (isRuToEn) "RU -> EN" else "EN -> RU")
                    }
                }

                OutlinedTextField(
                    value = sourceText,
                    onValueChange = {
                        sourceText = it
                        if (it.isBlank()) {
                            targetText = ""
                        } else if (isModelReady) {
                            translator.translate(it)
                                .addOnSuccessListener { targetText = it }
                                .addOnFailureListener { targetText = "Ошибка" }
                        }
                    },
                    label = { Text(if (isRuToEn) "Русский" else "English") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isRuToEn) "English" else "Русский") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
                
                Text(
                    text = statusMessage, 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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
