package com.myapplication

package com.example.minimalistlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinimalistApp()
        }
    }
}

@Composable
fun MinimalistApp() {
    var showAppDrawer by remember { mutableStateOf(false) }
    
    // Zabrání zavření launcheru tlačítkem zpět, místo toho zavře jen seznam aplikací
    BackHandler(enabled = showAppDrawer) {
        showAppDrawer = false
    }

    if (showAppDrawer) {
        AppDrawer(onClose = { showAppDrawer = false })
    } else {
        HomeScreen(onOpenDrawer = { showAppDrawer = true })
    }
}

@Composable
fun HomeScreen(onOpenDrawer: () -> Unit) {
    val context = LocalContext.current
    val time = remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        // Hodiny
        Text(text = time.value, color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(48.dp))

        // Volání
        AppItem("Telefon") {
            launchApp(context, "com.android.dialer") ?: launchApp(context, "com.google.android.dialer")
        }
        
        // SMS
        AppItem("Zprávy") {
            launchApp(context, "com.android.messaging") ?: launchApp(context, "com.google.android.apps.messaging")
        }
        
        // Google
        AppItem("Google") {
            launchApp(context, "com.android.chrome")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Tlačítko pro všechny aplikace
        Text(
            text = "Všechny aplikace →",
            color = Color.Gray,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .clickable { onOpenDrawer() }
        )
    }
}

@Composable
fun AppDrawer(onClose: () -> Unit) {
    val context = LocalContext.current
    val apps = remember { getInstalledApps(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Text(
            text = "← Zpět",
            color = Color.Gray,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(bottom = 24.dp, top = 24.dp)
                .clickable { onClose() }
        )

        LazyColumn {
            items(apps) { app ->
                Text(
                    text = app.loadLabel(context.packageManager).toString(),
                    color = Color.White,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clickable {
                            val intent = context.packageManager.getLaunchIntentForPackage(app.activityInfo.packageName)
                            intent?.let { context.startActivity(it) }
                        }
                )
            }
        }
    }
}

@Composable
fun AppItem(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 32.sp,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .clickable { onClick() }
    )
}

// Pomocná funkce pro vyhledání všech spustitelných aplikací
fun getInstalledApps(context: Context): List<ResolveInfo> {
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return context.packageManager.queryIntentActivities(intent, 0)
        .sortedBy { it.loadLabel(context.packageManager).toString().lowercase() }
}

// Pomocná funkce pro spuštění aplikace podle balíčku
fun launchApp(context: Context, packageName: String): Unit? {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    return intent?.let {
        context.startActivity(it)
    }
}
