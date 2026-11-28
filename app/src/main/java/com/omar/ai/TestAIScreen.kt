package com.omar.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private const val GEMINI_MODEL = "gemini-3.8-flash"

@Composable
fun TestAIScreen() {
    var key by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = key,
            onValueChange = { key = it },
            label = { Text("مفتاح Gemini") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            scope.launch {
                answer = try {
                    GeminiProvider(key.trim(), GEMINI_MODEL)
                        .ask("قل مرحباً بالعربية في جملة قصيرة")
                } catch (e: Exception) {
                    "خطأ: ${e.message}"
                }
            }
        }) {
            Text("اختبار")
        }

        Text(answer, modifier = Modifier.padding(top = 16.dp))
    }
}
