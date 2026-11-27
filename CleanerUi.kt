package com.example.cleaner

import android.os.Environment
import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PermissionScreen(onGrant: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("نحتاج إذنك", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("للبحث عن الملفات المؤقتة وملفات APK القديمة وحذفها، يحتاج التطبيق إلى صلاحية الوصول إلى الملفات. لا يتم رفع أي ملف خارج هاتفك.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onGrant) { Text("منح الصلاحية") }
    }
}

data class Junk(val file: File, val size: Long)

private val JUNK_EXT = setOf("tmp", "temp", "log", "apk", "bak")

fun scanJunk(): List<Junk> =
    Environment.getExternalStorageDirectory().walkTopDown()
        .onEnter { it.name != "Android" }
        .filter { it.isFile && it.extension.lowercase() in JUNK_EXT }
        .map { Junk(it, it.length()) }
        .toList()

@Composable
fun CleanerScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var found by remember { mutableStateOf<List<Junk>>(emptyList()) }
    var scanning by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("اضغط «فحص» للبدء") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("تنظيف الملفات", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(status)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = !scanning, onClick = {
                scope.launch {
                    scanning = true
                    status = "جارٍ الفحص..."
                    found = withContext(Dispatchers.IO) { scanJunk() }
                    val total = found.sumOf { it.size }
                    status = "وُجد ${found.size} ملف • ${Formatter.formatFileSize(ctx, total)}"
                    scanning = false
                }
            }) { Text("فحص") }

            Button(enabled = found.isNotEmpty() && !scanning, onClick = {
                scope.launch {
                    val freed = withContext(Dispatchers.IO) {
                        found.filter { it.file.delete() }.sumOf { it.size }
                    }
                    found = emptyList()
                    status = "تم تحرير ${Formatter.formatFileSize(ctx, freed)}"
                }
            }) { Text("حذف الكل") }
        }

        if (scanning) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(found) { j ->
                ListItem(
                    headlineContent = { Text(j.file.name, maxLines = 1) },
                    supportingContent = { Text(j.file.parent ?: "", maxLines = 1) },
                    trailingContent = { Text(Formatter.formatFileSize(ctx, j.size)) }
                )
                HorizontalDivider()
            }
        }
    }
}
