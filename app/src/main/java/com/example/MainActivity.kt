package com.example

import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Windows11SimulatorApp()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Windows11SimulatorApp() {
    val context = LocalContext.current

    // Wallpaper and Personalization state
    var wallpaperTheme by remember { mutableStateOf(WallpaperTheme.BLOOM_DARK) }
    var iconSize by remember { mutableStateOf(IconSize.MEDIUM) }

    // Desktop virtual file system state
    val desktopFiles = remember {
        mutableStateListOf(
            DesktopFile("this_pc", "Bu Bilgisayar", "Sistem Klasörü", isSystem = true, type = DesktopFileType.THIS_PC),
            DesktopFile("recycle_bin", "Geri Dönüşüm Kutusu", "Geri Dönüşüm Kutusu", isSystem = true, type = DesktopFileType.RECYCLE_BIN),
            DesktopFile(
                "readme_txt", 
                "Beni_Oku.txt", 
                "Windows 11 Masaüstü Simülatörüne Hoş Geldiniz!\n\n" +
                        "Bu tamamen çevrimdışı (OFFLINE) çalışan modern bir Jetpack Compose simülatörüdür.\n\n" +
                        "ÖZELLİKLER:\n" +
                        "1. Bu Bilgisayar (This PC):\n" +
                        "   - Çift tıklayarak açabilirsiniz.\n" +
                        "   - Telefonunuzun modelini, CPU mimarisini, kurulu gerçek RAM miktarını ve Android sürümünü simüle edilmeyen gerçek veriyle gösterir.\n\n" +
                        "2. Not Defteri (Notepad):\n" +
                        "   - Metin dosyalarını açıp düzenleyebilir, \"Dosya > Kaydet\" ile değişiklikleri masaüstüne kaydedebilirsiniz.\n" +
                        "   - Masaüstüne basılı tutarak \"Yeni Not Defteri\" oluşturabilirsiniz.\n\n" +
                        "3. Geri Dönüşüm Kutusu (Recycle Bin):\n" +
                        "   - Masaüstündeki txt dosyalarını Not Defteri ile açıp Dosya menüsünden sildiğinizde buraya taşınır.\n" +
                        "   - Boşalt tuşuna basıldığında boşaltma efekti çalışır ve simgesi boş olarak değişir.\n" +
                        "   - Kurtar tuşuna tıklandığında silinen dosyalarınızı masaüstüne geri döndürür.\n\n" +
                        "4. Hesap Makinesi:\n" +
                        "   - Görev çubuğunda veya Başlat Menüsü'nde yer alır. Tam fonksiyonel aritmetik işlemler sunar.\n\n" +
                        "5. Action Center (Sistem Paneli):\n" +
                        "   - Sağ alttaki saat ve pil simgelerine tıklayarak açabilirsiniz.\n" +
                        "   - Gece Işığı (Night Light) modunu açarak ekranı göz yormaması için turuncu tonlarına bürüyebilirsiniz.\n" +
                        "   - Ses ve cihaz özelliklerini kontrol edip, kişiselleştirme yapabilirsiniz.\n\n" +
                        "6. Kapatma İşlemi (Shut Down):\n" +
                        "   - Windows logosuna basıp sol alttaki Güç simgesine tıkladığınızda otantik Windows 11 kapatma ekranı devreye girer ve 3 saniye sonra uygulama kapanır.\n\n" +
                        "Tasarlayan ve Geliştiren: Mustafa\n" +
                        "Tasarım Altyapısı: Jetpack Compose & Google AI Studio Build",
                isSystem = false,
                type = DesktopFileType.NOTEPAD
            )
        )
    }

    // List of deleted files inside Recycle Bin
    val recycleBinFiles = remember { mutableStateListOf<DesktopFile>() }

    // Active opened window state
    val openWindows = remember { mutableStateListOf<OpenWindow>() }
    var focusedWindowId by remember { mutableStateOf<String?>(null) }

    // Overlay indicators
    var isStartMenuOpen by remember { mutableStateOf(false) }
    var isActionCenterOpen by remember { mutableStateOf(false) }
    var nightLightEnabled by remember { mutableStateOf(false) }
    var isShuttingDown by remember { mutableStateOf(false) }

    // Desktop interaction
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedIconId by remember { mutableStateOf<String?>(null) }

    // Screen brightness dimension layer alpha
    var screenBrightnessOverlayAlpha by remember { mutableStateOf(0f) }

    // Real System Tickers: Time and Battery
    var currentSystemTime by remember { mutableStateOf("00:00") }
    var batteryLevel by remember { mutableStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            currentSystemTime = sdf.format(Date())
            delay(10000) // update every 10s
        }
    }

    // Register active battery receiver
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                batteryLevel = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 100
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Double tap detector
    var lastIconClickTime by remember { mutableStateOf(0L) }

    // Functions to interact with windows
    val openWindow: (WindowType, String?, String) -> Unit = { type, associatedId, defaultName ->
        val windowId = associatedId ?: type.name
        val existingIndex = openWindows.indexOfFirst { win -> win.id == windowId }
        
        if (existingIndex != -1) {
            // Minimize check
            val existing = openWindows[existingIndex]
            if (existing.isMinimized) {
                openWindows[existingIndex] = existing.copy(isMinimized = false)
            }
            focusedWindowId = windowId
        } else {
            // Open new window
            val contentDraft = if (type == WindowType.NOTEPAD && associatedId != null) {
                desktopFiles.find { it.id == associatedId }?.content ?: ""
            } else ""

            openWindows.add(
                OpenWindow(
                    id = windowId,
                    title = defaultName,
                    type = type,
                    associatedFileId = associatedId,
                    draftContent = contentDraft,
                    xDp = (40 + openWindows.size * 20).dp,
                    yDp = (100 + openWindows.size * 25).dp
                )
            )
            focusedWindowId = windowId
        }
        isStartMenuOpen = false
    }

    val closeWindow: (String) -> Unit = { id ->
        openWindows.removeAll { it.id == id }
        if (focusedWindowId == id) {
            focusedWindowId = openWindows.lastOrNull()?.id
        }
    }

    val focusWindow: (String) -> Unit = { id ->
        focusedWindowId = id
        val index = openWindows.indexOfFirst { it.id == id }
        if (index != -1 && index != openWindows.size - 1) {
            val win = openWindows.removeAt(index)
            openWindows.add(win)
        }
    }

    // Back button closes active windows or menus
    BackHandler {
        when {
            isStartMenuOpen -> isStartMenuOpen = false
            isActionCenterOpen -> isActionCenterOpen = false
            focusedWindowId != null -> closeWindow(focusedWindowId!!)
            else -> {
                // Exit app warning or shutdown sequence
                isShuttingDown = true
            }
        }
    }

    // Container filling full screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        selectedIconId = null
                        showContextMenu = false
                        isStartMenuOpen = false
                        isActionCenterOpen = false
                    },
                    onLongPress = { offset ->
                        showContextMenu = true
                        contextMenuOffset = offset
                    }
                )
            }
    ) {
        // 1. Windows 11 Animated Wallpaper background
        DesktopWallpaper(theme = wallpaperTheme)

        // 2. Desktop Files Grid (Column-based wrap)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 80.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                desktopFiles.forEach { file ->
                    val isSelected = selectedIconId == file.id
                    val isRecycleNotEmpty = file.id == "recycle_bin" && recycleBinFiles.isNotEmpty()

                    Column(
                        modifier = Modifier
                            .width(80.dp)
                            .testTag("file_shortcut_${file.id}")
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x33FFFFFF) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0x44FFFFFF) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                val now = System.currentTimeMillis()
                                if (now - lastIconClickTime < 350) {
                                    // DOUBLE CLICKED!
                                    when (file.type) {
                                        DesktopFileType.THIS_PC -> openWindow(WindowType.THIS_PC, null, "Bu Bilgisayar")
                                        DesktopFileType.RECYCLE_BIN -> openWindow(WindowType.RECYCLE_BIN, null, "Geri Dönüşüm Kutusu")
                                        DesktopFileType.NOTEPAD -> openWindow(WindowType.NOTEPAD, file.id, file.name)
                                        DesktopFileType.FOLDER -> {}
                                    }
                                } else {
                                    // SINGLE CLICKED!
                                    selectedIconId = file.id
                                }
                                lastIconClickTime = now
                            }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(iconSize.dp)) {
                            when (file.type) {
                                DesktopFileType.THIS_PC -> WinIconThisPC(sizeDp = iconSize.dp)
                                DesktopFileType.RECYCLE_BIN -> WinIconRecycleBin(sizeDp = iconSize.dp, isEmpty = !isRecycleNotEmpty)
                                DesktopFileType.NOTEPAD -> WinIconNotepad(sizeDp = iconSize.dp)
                                DesktopFileType.FOLDER -> WinIconFolder(sizeDp = iconSize.dp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = file.name,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = iconSize.fontSize.sp,
                                fontWeight = FontWeight.Normal,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    offset = Offset(1f, 1f),
                                    blurRadius = 4f
                                )
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. Render Windows Overlap on the Desktop Space
        openWindows.forEach { window ->
            if (!window.isMinimized) {
                DesktopWindowLayer(
                    window = window,
                    isFocused = focusedWindowId == window.id,
                    recycleBinSize = recycleBinFiles.size,
                    onFocus = { focusWindow(window.id) },
                    onClose = { closeWindow(window.id) },
                    onMinimize = {
                        openWindows[openWindows.indexOfFirst { it.id == window.id }] = window.copy(isMinimized = true)
                    },
                    onMaximizeToggle = {
                        openWindows[openWindows.indexOfFirst { it.id == window.id }] = window.copy(isMaximized = !window.isMaximized)
                    },
                    onWindowDrag = { dx, dy ->
                        val index = openWindows.indexOfFirst { it.id == window.id }
                        if (index != -1 && !window.isMaximized) {
                            val win = openWindows[index]
                            openWindows[index] = win.copy(xDp = win.xDp + dx, yDp = win.yDp + dy)
                        }
                    },
                    onDraftChange = { newDraft ->
                        val index = openWindows.indexOfFirst { it.id == window.id }
                        if (index != -1) {
                            openWindows[index] = openWindows[index].copy(draftContent = newDraft)
                        }
                    },
                    onSaveNotepad = { fileId, contentText ->
                        val index = desktopFiles.indexOfFirst { it.id == fileId }
                        if (index != -1) {
                            desktopFiles[index] = desktopFiles[index].copy(content = contentText)
                            Toast.makeText(context, "${desktopFiles[index].name} kaydedildi.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDeleteNotepad = { fileId ->
                        val fileIndex = desktopFiles.indexOfFirst { it.id == fileId }
                        if (fileIndex != -1) {
                            val fileObj = desktopFiles[fileIndex]
                            desktopFiles.removeAt(fileIndex)
                            recycleBinFiles.add(fileObj)
                            closeWindow(window.id)
                            Toast.makeText(context, "${fileObj.name} Geri Dönüşüm Kutusu'na gönderildi.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    recycleBinFiles = recycleBinFiles,
                    onEmptyRecycleBin = {
                        recycleBinFiles.clear()
                        Toast.makeText(context, "Geri Dönüşüm Kutusu boşaltıldı.", Toast.LENGTH_SHORT).show()
                    },
                    onRestoreRecycleBin = {
                        desktopFiles.addAll(recycleBinFiles)
                        recycleBinFiles.clear()
                        Toast.makeText(context, "Dosyalar masaüstüne geri yüklendi.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 4. Centered Taskbar (GÖREV ÇUBUĞU - En Altta)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Transparent)
        ) {
            // Taskbar body
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(48.dp)
                    .background(Color(0x991C1C28))
                    .border(width = 0.5.dp, color = Color(0x22FFFFFF))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left spacing or extra layout spacer to keep start centered or empty
                Spacer(modifier = Modifier.width(50.dp))

                // Centered Windows and Pinned shortcuts
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Windows Start Logo Trigger
                    IconButton(
                        onClick = {
                            isStartMenuOpen = !isStartMenuOpen
                            isActionCenterOpen = false
                        },
                        modifier = Modifier
                            .testTag("taskbar_start_button")
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isStartMenuOpen) Color(0x22FFFFFF) else Color.Transparent)
                    ) {
                        WinIconWindows(sizeDp = 22.dp)
                    }

                    // Arama Simgesi (Search Box Trigger)
                    IconButton(
                        onClick = {
                            isStartMenuOpen = true
                            isActionCenterOpen = false
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ara",
                            tint = Color(0xFFDCDCE6),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Dosyalar Simgesi (File Explorer Trigger - Launch This PC)
                    IconButton(
                        onClick = {
                            openWindow(WindowType.THIS_PC, null, "Bu Bilgisayar")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        WinIconFolder(sizeDp = 22.dp)
                    }

                    // Calculator Pinned Icon
                    IconButton(
                        onClick = {
                            openWindow(WindowType.CALCULATOR, null, "Hesap Makinesi")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        WinIconCalculator(sizeDp = 22.dp)
                    }

                    // Custom text active dots under open apps
                    // (Matches Windows 11 taskbar showing small active line indicator)
                    Spacer(modifier = Modifier.width(2.dp))
                }

                // System Tray indicators (Battery & Clock - Bottom Right)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            isActionCenterOpen = !isActionCenterOpen
                            isStartMenuOpen = false
                        }
                        .background(if (isActionCenterOpen) Color(0x33FFFFFF) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isCharging) "🔌" else "🔋",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$batteryLevel%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = currentSystemTime,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 5. Beautiful Windows 11 Start Menu Bottom Sheet Popup (Ortalanmış Başlat Menüsü)
        AnimatedVisibility(
            visible = isStartMenuOpen,
            enter = slideInVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ) { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {
            StartMenuPopup(
                onClose = { isStartMenuOpen = false },
                onLaunchApp = { appType ->
                    launchNativeSystemApp(context, appType) { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                    isStartMenuOpen = false
                },
                onOpenCalculator = {
                    openWindow(WindowType.CALCULATOR, null, "Hesap Makinesi")
                    isStartMenuOpen = false
                },
                onOpenSettings = {
                    openWindow(WindowType.SETTINGS, null, "Ayarlar")
                    isStartMenuOpen = false
                },
                onTriggerShutdown = {
                    isStartMenuOpen = false
                    isShuttingDown = true
                }
            )
        }

        // 6. Action Center System Panel Overlay (Bottom-Right Action Tray)
        AnimatedVisibility(
            visible = isActionCenterOpen,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 60.dp, end = 12.dp)
        ) {
            ActionCenterTray(
                nightLightEnabled = nightLightEnabled,
                onNightLightToggle = { nightLightEnabled = it },
                screenBrightness = screenBrightnessOverlayAlpha,
                onBrightnessChange = { screenBrightnessOverlayAlpha = it },
                wallpaperTheme = wallpaperTheme,
                onWallpaperChange = { wallpaperTheme = it },
                iconSize = iconSize,
                onIconSizeChange = { iconSize = it },
                batteryPercent = batteryLevel,
                isCharging = isCharging,
                onClose = { isActionCenterOpen = false }
            )
        }

        // 7. Desktop Context Menu Popup (Masaüstü Sağ tık/Basılı Tutma)
        if (showContextMenu) {
            DesktopContextMenu(
                offset = contextMenuOffset,
                wallpaperTheme = wallpaperTheme,
                onWallpaperChange = { wallpaperTheme = it },
                iconSize = iconSize,
                onIconSizeChange = { iconSize = it },
                onNewNotepadFile = {
                    val count = desktopFiles.filter { it.name.startsWith("Yeni_Metin") }.size
                    val newId = "new_txt_${System.currentTimeMillis()}"
                    val newName = "Yeni_Metin_${count + 1}.txt"
                    desktopFiles.add(
                        DesktopFile(
                            id = newId,
                            name = newName,
                            content = "Yazmaya başlayın...",
                            type = DesktopFileType.NOTEPAD
                        )
                    )
                    showContextMenu = false
                    Toast.makeText(context, "\"$newName\" Masaüstüne eklendi.", Toast.LENGTH_SHORT).show()
                },
                onRefreshDesktop = {
                    // Quick flicker effect to represent refresh
                    selectedIconId = null
                    showContextMenu = false
                    Toast.makeText(context, "Masaüstü yenilendi.", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showContextMenu = false }
            )
        }

        // 8. Custom Virtual Night Light Filter Hue (Göz Koruyucu Turuncu Maske)
        if (nightLightEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x35E0A124)) // translucent amber filter
                    .pointerInput(Unit) {} // consume touch so it's strictly a visual overlay
            )
        }

        // 9. Virtual Screen Brightness Dim Overlay
        if (screenBrightnessOverlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = screenBrightnessOverlayAlpha * 0.75f))
                    .pointerInput(Unit) {} // visual filter
            )
        }

        // 10. Win11 Shut Down Screen Sequence (Bilgisayar Kapatılıyor)
        if (isShuttingDown) {
            WindowsShutdownScreen(
                onShutdownFinish = {
                    isShuttingDown = false
                    // Real exit trigger
                    (context as? Activity)?.finish()
                }
            )
        }
    }
}

@Composable
fun DesktopWallpaper(theme: WallpaperTheme) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val maxDim = maxOf(width, height)

        when (theme) {
            WallpaperTheme.BLOOM_DARK -> {
                // Windows 11 signature dark radial bloom
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2A4280), Color(0xFF101832), Color(0xFF060914)),
                        center = Offset(width * 0.5f, height * 0.4f),
                        radius = maxDim * 0.95f
                    )
                )

                // Background bloom ribbons
                val ribbon1 = Path().apply {
                    moveTo(width * 0.1f, height * 0.9f)
                    cubicTo(width * 0.25f, height * 0.35f, width * 0.7f, height * 0.4f, width * 0.9f, height * 0.85f)
                    cubicTo(width * 0.95f, height * 0.95f, width * 0.65f, height * 1.0f, width * 0.2f, height * 1.0f)
                    close()
                }
                drawPath(
                    path = ribbon1,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x5500A2ED), Color(0x187B52F9)),
                        start = Offset(width * 0.3f, height * 0.4f),
                        end = Offset(width * 0.8f, height * 0.9f)
                    )
                )

                val ribbon2 = Path().apply {
                    moveTo(width * 0.25f, height * 0.82f)
                    cubicTo(width * 0.45f, height * 0.42f, width * 0.8f, height * 0.35f, width * 0.95f, height * 0.65f)
                    cubicTo(width * 1.0f, height * 0.8f, width * 0.85f, height * 0.95f, width * 0.4f, height * 0.95f)
                    close()
                }
                drawPath(
                    path = ribbon2,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x3A8F4DFF), Color(0x0CC0392B)),
                        start = Offset(width * 0.4f, height * 0.45f),
                        end = Offset(width * 0.9f, height * 0.85f)
                    )
                )
            }
            WallpaperTheme.BLOOM_LIGHT -> {
                // Windows 11 light blue backdrop
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD9E9FC), Color(0xFFC7DCF8), Color(0xFFAEC6E8)),
                        center = Offset(width * 0.6f, height * 0.3f),
                        radius = maxDim * 1.05f
                    )
                )

                val lightRibbon = Path().apply {
                    moveTo(width * 0.15f, height * 0.95f)
                    cubicTo(width * 0.35f, height * 0.45f, width * 0.75f, height * 0.4f, width * 0.9f, height * 0.75f)
                    cubicTo(width * 0.95f, height * 0.85f, width * 0.78f, height * 0.95f, width * 0.35f, height * 0.95f)
                    close()
                }
                drawPath(
                    path = lightRibbon,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x6A91D3FF), Color(0x2A6258E2)),
                        start = Offset(width * 0.3f, height * 0.45f),
                        end = Offset(width * 0.85f, height * 0.85f)
                    )
                )
            }
            WallpaperTheme.GLOW_PURPLE -> {
                // Vibrant Aurora Glow Purple
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF5A2578), Color(0xFF1B082E), Color(0xFF0B0314)),
                        center = Offset(width * 0.5f, height * 0.35f),
                        radius = maxDim * 1.0f
                    )
                )

                val purpleRibbon = Path().apply {
                    moveTo(0f, height * 0.65f)
                    cubicTo(width * 0.4f, height * 0.25f, width * 0.7f, height * 0.85f, width, height * 0.55f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = purpleRibbon,
                    brush = Brush.verticalGradient(listOf(Color(0x4AE841B8), Color(0x133742FA)))
                )
            }
            WallpaperTheme.WAVES_EMERALD -> {
                // Calm Forest Emerald
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF194236), Color(0xFF091C17), Color(0xFF030A08)),
                        center = Offset(width * 0.4f, height * 0.3f),
                        radius = maxDim * 0.9f
                    )
                )

                val greenRibbon = Path().apply {
                    moveTo(0f, height * 0.7f)
                    cubicTo(width * 0.3f, height * 0.4f, width * 0.65f, height * 0.9f, width, height * 0.45f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = greenRibbon,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x444CD137), Color(0x0E0097E6)),
                        start = Offset(0f, height * 0.5f),
                        end = Offset(width, height)
                    )
                )
            }
        }
    }
}

// Window Container Composable (Enables moving, resizing, closing and maximize status)
@Composable
fun DesktopWindowLayer(
    window: OpenWindow,
    isFocused: Boolean,
    recycleBinSize: Int,
    onFocus: () -> Unit,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onMaximizeToggle: () -> Unit,
    onWindowDrag: (Dp, Dp) -> Unit,
    onDraftChange: (String) -> Unit,
    onSaveNotepad: (String, String) -> Unit,
    onDeleteNotepad: (String) -> Unit,
    recycleBinFiles: List<DesktopFile>,
    onEmptyRecycleBin: () -> Unit,
    onRestoreRecycleBin: () -> Unit
) {
    val density = LocalDensity.current
    var windowWidth by remember { mutableStateOf(340.dp) }
    var windowHeight by remember { mutableStateOf(440.dp) }

    // Position setup adjusting dynamically for maximize mode
    val boxModifier = if (window.isMaximized) {
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(bottom = 48.dp) // leave space for taskbar
    } else {
        Modifier
            .offset(x = window.xDp, y = window.yDp)
            .size(width = windowWidth, height = windowHeight)
    }

    Card(
        modifier = boxModifier
            .shadow(if (isFocused) 20.dp else 6.dp, shape = RoundedCornerShape(12.dp))
            .clickable(interactionSource = null, indication = null) { onFocus() }
            .border(
                width = 1.3.dp,
                color = if (isFocused) Color(0x55FFFFFF) else Color(0x1BFFFFFF),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xEC151B2E) // modern glassy royal acrylic window
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TITLE BAR (Sürüklenebilir/Drag Header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color(0xFF0F1524))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onFocus() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dx = with(density) { dragAmount.x.toDp() }
                                val dy = with(density) { dragAmount.y.toDp() }
                                onWindowDrag(dx, dy)
                            }
                        )
                    }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Icon + Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(16.dp)) {
                        when (window.type) {
                            WindowType.THIS_PC -> WinIconThisPC(sizeDp = 16.dp)
                            WindowType.RECYCLE_BIN -> WinIconRecycleBin(sizeDp = 16.dp, isEmpty = recycleBinSize == 0)
                            WindowType.NOTEPAD -> WinIconNotepad(sizeDp = 16.dp)
                            WindowType.CALCULATOR -> WinIconCalculator(sizeDp = 16.dp)
                            WindowType.SETTINGS -> WinIconSettings(sizeDp = 16.dp)
                        }
                    }
                    Text(
                        text = window.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Window Actions Right Hand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Minimize Window icon
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(text = "—", color = Color(0xFFA0AACC), fontSize = 10.sp)
                    }

                    // Maximize Window icon
                    IconButton(
                        onClick = onMaximizeToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = if (window.isMaximized) "❐" else "❑",
                            color = Color(0xFFA0AACC),
                            fontSize = 12.sp
                        )
                    }

                    // Close Window Button (Red accent)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFC0392B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // WINDOW CONTENT CONTAINER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF1E263E))
            ) {
                when (window.type) {
                    WindowType.THIS_PC -> RealHardwareSpecsWindow()
                    WindowType.RECYCLE_BIN -> RecycleBinWindowContent(
                        deletedFiles = recycleBinFiles,
                        onEmpty = onEmptyRecycleBin,
                        onRestore = onRestoreRecycleBin
                    )
                    WindowType.NOTEPAD -> NotepadWindowContent(
                        window = window,
                        onDraftTextChange = onDraftChange,
                        onSave = onSaveNotepad,
                        onDelete = onDeleteNotepad
                    )
                    WindowType.CALCULATOR -> CalculationWindowContent()
                    WindowType.SETTINGS -> SettingsWindowContent()
                }
            }
        }
    }
}

// 1. This PC Windows Content - Reads ACTUAL real device statistics!
@Composable
fun RealHardwareSpecsWindow() {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Specs") }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar Navigation of files window
        Column(
            modifier = Modifier
                .width(95.dp)
                .fillMaxHeight()
                .background(Color(0xFF0F1524))
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val items = listOf("Specs" to "Donanım", "Drives" to "Sürücüler", "Folders" to "Klasörler")
            items.forEach { (catId, label) ->
                val isSelected = selectedCategory == catId
                Text(
                    text = label,
                    color = if (isSelected) Color(0xFF00ADEF) else Color(0xFF8FA0B2),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Color(0x1A00ADEF) else Color.Transparent)
                        .clickable { selectedCategory = catId }
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    textAlign = TextAlign.Start
                )
            }
        }

        // Main Viewer panel
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedCategory) {
                "Specs" -> { // READS ACCURATE DEVICE DIAGNOSTICS USING SECURE NATIVE APIs!
                    // Read total system RAM securely
                    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                    val memInfo = ActivityManager.MemoryInfo()
                    actManager?.getMemoryInfo(memInfo)
                    val totalRamGb = memInfo.totalMem / (1024 * 1024 * 1024.0)
                    val displayRam = if (totalRamGb > 0) String.format("%.2f GB", totalRamGb) else "8.0 GB"

                    // Read virtual uptime
                    val rawUptimeMin = SystemClock.elapsedRealtime() / 60000
                    val uptimeFormatted = "${rawUptimeMin / 60} saat ${rawUptimeMin % 60} dakika"

                    Text("Cihaz Özellikleri", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Divider(color = Color(0x228FA0B2))

                    SpecItemRow("Cihaz Adı", "MUSTAFA-ANDROID-PC")
                    SpecItemRow("İşlemci", "${Build.BOARD.uppercase()} (Architecture: ${Build.SUPPORTED_ABIS[0]})")
                    SpecItemRow("Kurulu RAM", displayRam)
                    SpecItemRow("Sistem Türü", "64-bit İşletim Sistemi, ARM-64 tabanlı")
                    SpecItemRow("Sürüm", "Windows 11 Home (Android OS ${Build.VERSION.RELEASE})")
                    SpecItemRow("Derleme", "API Seviyesi: ${Build.VERSION.SDK_INT}")
                    SpecItemRow("Uptime Süresi", uptimeFormatted)
                    SpecItemRow("Üretici", Build.MANUFACTURER.uppercase())
                }

                "Drives" -> {
                    Text("Cihazlar ve Sürücüler", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Divider(color = Color(0x228FA0B2))

                    // Simulated storage sizes derived cleanly
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF131824))
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💾", fontSize = 24.sp)
                            Column {
                                Text("Yerel Disk (C:)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                // Styled storage progress indicator
                                LinearProgressIndicator(
                                    progress = 0.65f,
                                    color = Color(0xFF00ADEF),
                                    trackColor = Color(0xFF223650),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("89.3 GB boş / 256 GB", color = Color(0xFF8FA0B2), fontSize = 11.sp)
                            }
                        }
                    }
                }

                "Folders" -> {
                    Text("Sistem Klasörleri", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Divider(color = Color(0x228FA0B2))

                    val folders = listOf("Belgeler" to "📁", "İndirilenler" to "📥", "Resimler" to "🖼️", "Sistem" to "🗄️")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(folders) { (name, iconSymbol) ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF131824))
                                    .clickable { }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(iconSymbol, fontSize = 20.sp)
                                Text(name, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecItemRow(label: String, valText: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(label, color = Color(0xFF8FA0B2), fontSize = 11.sp, modifier = Modifier.weight(0.35f))
            Text(valText, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.65f))
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}

// 2. Recycle Bin Windows Content - Functional emptying and restoring
@Composable
fun RecycleBinWindowContent(
    deletedFiles: List<DesktopFile>,
    onEmpty: () -> Unit,
    onRestore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar with action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131824))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onEmpty,
                enabled = deletedFiles.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B), disabledContainerColor = Color(0xFF321D1D)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Boşalt", fontSize = 10.sp, color = Color.White)
            }

            Button(
                onClick = onRestore,
                enabled = deletedFiles.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D5A2B), disabledContainerColor = Color(0xFF112317)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hepsini Kurtar", fontSize = 10.sp, color = Color.White)
            }
        }

        // Deleted Grid
        if (deletedFiles.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🗑️", fontSize = 52.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Klasör Boş", color = Color(0xFF8FA0B2), fontSize = 13.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(deletedFiles) { file ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF151B2E))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WinIconNotepad(sizeDp = 30.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            file.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// 3. Notepad Windows Content - Working Text editor
@Composable
fun NotepadWindowContent(
    window: OpenWindow,
    onDraftTextChange: (String) -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var showNotepadMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Notepad Menu Row (Dosya / Düzen / Görünüm)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131824))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Text(
                    text = "Dosya",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { showNotepadMenu = !showNotepadMenu }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                DropdownMenu(
                    expanded = showNotepadMenu,
                    onDismissRequest = { showNotepadMenu = false },
                    modifier = Modifier.background(Color(0xFF1E263E))
                ) {
                    DropdownMenuItem(
                        text = { Text("Kaydet", color = Color.White, fontSize = 12.sp) },
                        onClick = {
                            showNotepadMenu = false
                            if (window.associatedFileId != null) {
                                onSave(window.associatedFileId, window.draftContent)
                            }
                        },
                        leadingIcon = { Text("💾", fontSize = 14.sp) }
                    )
                    DropdownMenuItem(
                        text = { Text("Masaüstünden Sil", color = Color(0xFFEF5350), fontSize = 12.sp) },
                        onClick = {
                            showNotepadMenu = false
                            if (window.associatedFileId != null) {
                                onDelete(window.associatedFileId)
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            Text("Düzen", color = Color(0xFF8FA0B2), fontSize = 12.sp)
            Text("Görünüm", color = Color(0xFF8FA0B2), fontSize = 12.sp)
        }

        // Multi Line Input Pad
        BasicTextField(
            value = window.draftContent,
            onValueChange = onDraftTextChange,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            ),
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9FA))
                .padding(12.dp),
            decorationBox = { innerTextField ->
                if (window.draftContent.isEmpty()) {
                    Text("Buraya yazmaya başlayın...", color = Color.LightGray, fontSize = 13.sp)
                }
                innerTextField()
            }
        )
    }
}

// 4. Windows 11 Fluent Calculator Content
@Composable
fun CalculationWindowContent() {
    var formulaText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var shouldClearOnNext by remember { mutableStateOf(false) }

    val onKeyPress: (String) -> Unit = { key ->
        when (key) {
            "C" -> {
                formulaText = ""
                resultText = "0"
                shouldClearOnNext = false
            }
            "=" -> {
                try {
                    val resultVal = evaluateEquationSafe(formulaText)
                    resultText = if (resultVal == resultVal.toInt().toDouble()) {
                        resultVal.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.5f", resultVal).trimEnd('0').trimEnd('.')
                    }
                    shouldClearOnNext = true
                } catch (e: Exception) {
                    resultText = "Hata"
                }
            }
            else -> {
                if (shouldClearOnNext) {
                    formulaText = ""
                    shouldClearOnNext = false
                }
                formulaText += key
            }
        }
    }

    val keys = listOf(
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "*"),
        listOf("1", "2", "3", "-"),
        listOf("C", "0", "=", "+")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF131824))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                formulaText.ifEmpty { "0" },
                color = Color(0xFF8FA0B2),
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                resultText,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Buttons grid
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            keys.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowKeys.forEach { keySym ->
                        val isOp = keySym in listOf("/", "*", "-", "+", "=")
                        val btnColor = when {
                            keySym == "=" -> Color(0xFF00ADEF) // signature accent
                            isOp -> Color(0xFF161F32)
                            else -> Color(0xFF131824)
                        }

                        Button(
                            onClick = { onKeyPress(keySym) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                keySym,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Math evaluation safety parser
private fun evaluateEquationSafe(eq: String): Double {
    if (eq.isEmpty()) return 0.0
    // Very simple sequential math resolver for stability as a calculator applet
    var tokens = eq.split("(?<=[-+*/])|(?=[-+*/])".toRegex()).map { it.trim() }.filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return 0.0
    
    var accum = tokens[0].toDoubleOrNull() ?: return 0.0
    var i = 1
    while (i < tokens.size - 1) {
        val op = tokens[i]
        val nextValVal = tokens[i + 1].toDoubleOrNull() ?: 0.0
        when (op) {
            "+" -> accum += nextValVal
            "-" -> accum -= nextValVal
            "*" -> accum *= nextValVal
            "/" -> if (nextValVal != 0.0) accum /= nextValVal else return 0.0
        }
        i += 2
    }
    return accum
}

// 5. Windows 11 Fluent Settings Content - Dynamic Personalization
@Composable
fun SettingsWindowContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WinIconSettings(sizeDp = 32.dp)
            Text("Ayarlar", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Divider(color = Color(0x1AFFFFFF))

        // Personalization info
        Text("Kişiselleştirme Ayarları", color = Color(0xFF00ADEF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(
            "Duvar kağıdını ve simge boyutlarını değiştirmek için masaüstüne uzun basarak açılan \"Masaüstü Bağlam Menüsü\" panelini kullanabilirsiniz.",
            color = Color(0xFF8FA0B2),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Core system info inside settings
        Text("Sistem Hakkında", color = Color(0xFF00ADEF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF131824))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Ürün Sürümü", color = Color(0xFF8FA0B2), fontSize = 11.sp)
                Text("Windows 11 Simülatörü v2.4", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Geliştirici", color = Color(0xFF8FA0B2), fontSize = 11.sp)
                Text("Mustafa", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Altyapı", color = Color(0xFF8FA0B2), fontSize = 11.sp)
                Text("Jetpack Compose", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Windows 11 Start Menu Bottom Popup Drawer ---
@Composable
fun StartMenuPopup(
    onClose: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenSettings: () -> Unit,
    onTriggerShutdown: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(360.dp)
            .height(480.dp)
            .padding(horizontal = 8.dp)
            .border(width = 1.dp, color = Color(0x33FFFFFF), shape = RoundedCornerShape(16.dp))
            .shadow(24.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA151B2E)) // Fluent Acrylic
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Search Bar (Non functional search look for authenticity)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(Color(0xFF0F1524))
                    .border(width = 0.5.dp, color = Color(0x22FFFFFF), shape = RoundedCornerShape(19.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8FA0B2), modifier = Modifier.size(16.dp))
                Text("Uygulama, ayar ve belge arayın...", color = Color(0xFF8FA0B2), fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sabitlenenler (Pinned Apps Section Header)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sabitlenenler", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Tüm uygulamalar >",
                    color = Color(0xFF00ADEF),
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { }
                )
            }

            // Pinned applications grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f).padding(top = 12.dp)
            ) {
                // Row 1 - Native Shortcuts (Basıldığında gerçek telefondaki sistem uygulamasını açar!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Kamera Kısayolu
                    StartMenuItem(
                        symbolSymbol = "📷",
                        title = "Kamera",
                        onTap = { onLaunchApp("camera") }
                    )
                    // Galeri Kısayolu
                    StartMenuItem(
                        symbolSymbol = "🖼️",
                        title = "Galeri",
                        onTap = { onLaunchApp("gallery") }
                    )
                    // Gerçek Cihaz Ayarları
                    StartMenuItem(
                        symbolSymbol = "⚙️",
                        title = "Ayarlar",
                        onTap = { onLaunchApp("settings") }
                    )
                }

                // Row 2 - Web Sim shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Calculator Simulation shortcut
                    Column(
                        modifier = Modifier
                            .width(75.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenCalculator() }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WinIconCalculator(sizeDp = 28.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Hesap Mak.", color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }

                    // settings trigger shortcut
                    Column(
                        modifier = Modifier
                            .width(75.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenSettings() }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WinIconSettings(sizeDp = 28.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Kişiselleştir", color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }

                    // Simulated Web browser
                    StartMenuItem(
                        symbolSymbol = "🌐",
                        title = "Tarayıcı",
                        onTap = { onLaunchApp("browser") }
                    )
                }
            }

            // Footer profile and shut down
            Divider(color = Color(0x1BFFFFFF))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Profile info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00ADEF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("M", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Mustafa", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("mustafa.tire.1997@gmail.com", color = Color(0xFF8FA0B2), fontSize = 8.sp)
                    }
                }

                // Shut down system icon (Triggers Shutting down animation)
                IconButton(
                    onClick = onTriggerShutdown,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF))
                ) {
                    Text(
                        text = "⏻",
                        color = Color(0xFFEF5350),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StartMenuItem(symbolSymbol: String, title: String, onTap: () -> Unit) {
    Column(
        modifier = Modifier
            .width(75.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTap() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(symbolSymbol, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(title, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

// --- Action Center Quick Settings Tray (Bottom Right Click) ---
@Composable
fun ActionCenterTray(
    nightLightEnabled: Boolean,
    onNightLightToggle: (Boolean) -> Unit,
    screenBrightness: Float,
    onBrightnessChange: (Float) -> Unit,
    wallpaperTheme: WallpaperTheme,
    onWallpaperChange: (WallpaperTheme) -> Unit,
    iconSize: IconSize,
    onIconSizeChange: (IconSize) -> Unit,
    batteryPercent: Int,
    isCharging: Boolean,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .wrapContentHeight()
            .border(width = 1.dp, color = Color(0x22FFFFFF), shape = RoundedCornerShape(12.dp))
            .shadow(16.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA151B2E))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Action Panel Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hızlı Ayarlar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                }
            }

            // Quick Toggle Grid (Wi-Fi, Bluetooth, Nightlight, Personalize)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle 1: Gece Işığı (Night light)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (nightLightEnabled) Color(0xFF00ADEF) else Color(0xFF1E263E))
                        .clickable { onNightLightToggle(!nightLightEnabled) }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (nightLightEnabled) "☀️" else "🌙", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Gece Işığı", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Toggle 2: Wallpaper theme cycle switcher
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E263E))
                        .clickable {
                            val nextTheme = when (wallpaperTheme) {
                                WallpaperTheme.BLOOM_DARK -> WallpaperTheme.BLOOM_LIGHT
                                WallpaperTheme.BLOOM_LIGHT -> WallpaperTheme.GLOW_PURPLE
                                WallpaperTheme.GLOW_PURPLE -> WallpaperTheme.WAVES_EMERALD
                                WallpaperTheme.WAVES_EMERALD -> WallpaperTheme.BLOOM_DARK
                            }
                            onWallpaperChange(nextTheme)
                        }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎨", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Duvar Kağıdı", color = Color.White, fontSize = 10.sp)
                }
            }

            // Sliders for Brightness (Injected overlay controller)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Ekran Karartma (Karartıcı)", color = Color(0xFF8FA0B2), fontSize = 11.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🌘", fontSize = 14.sp)
                    Slider(
                        value = screenBrightness,
                        onValueChange = onBrightnessChange,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00ADEF),
                            activeTrackColor = Color(0xFF00ADEF)
                        )
                    )
                    Text("🌒", fontSize = 14.sp)
                }
            }

            // Desktop Icon sizes selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Simgeler Boyutu", color = Color(0xFF8FA0B2), fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconSize.values().forEach { sizeVal ->
                        val isSel = iconSize == sizeVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) Color(0xFF00ADEF) else Color(0xFF1E263E))
                                .clickable { onIconSizeChange(sizeVal) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when(sizeVal) {
                                    IconSize.SMALL -> "Küçük"
                                    IconSize.MEDIUM -> "Orta"
                                    IconSize.LARGE -> "Büyük"
                                },
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Divider(color = Color(0x1BFFFFFF))

            // Footer - Real Battery Info Diagnostic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCharging) "🔌 Şarj Ediliyor" else "🔋 Pilde Çalışıyor",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%$batteryPercent",
                    color = Color(0xFF8FA0B2),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// --- Desktop Long Press Context Menu ---
@Composable
fun DesktopContextMenu(
    offset: Offset,
    wallpaperTheme: WallpaperTheme,
    onWallpaperChange: (WallpaperTheme) -> Unit,
    iconSize: IconSize,
    onIconSizeChange: (IconSize) -> Unit,
    onNewNotepadFile: () -> Unit,
    onRefreshDesktop: () -> Unit,
    onDismiss: () -> Unit
) {
    var showWallpaperSubMenu by remember { mutableStateOf(false) }
    var showSizesSubMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset(x = offset.x.dp / 2.5f, y = offset.y.dp / 2.5f) // approximate dp positioning safely
    ) {
        Card(
            modifier = Modifier
                .width(180.dp)
                .border(width = 0.5.dp, color = Color(0x33FFFFFF), shape = RoundedCornerShape(8.dp))
                .shadow(12.dp, shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEC121824))
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                // Item 1: Yenile (Refresh)
                ContextMenuItem("🔄 Yenile", onClick = onRefreshDesktop)

                Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                // Item 2: Yeni Not Defteri
                ContextMenuItem("📝 Yeni Metin Belgesi", onClick = onNewNotepadFile)

                Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                // Item 3: Duvar Kağıdı Değiştir
                ContextMenuItem("🎨 Duvar Kağıdı Seç", onClick = { showWallpaperSubMenu = !showWallpaperSubMenu })
                if (showWallpaperSubMenu) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F1524))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        WallpaperSubItem("Bloom Koyu", WallpaperTheme.BLOOM_DARK, wallpaperTheme, onWallpaperChange, onDismiss)
                        WallpaperSubItem("Bloom Açık", WallpaperTheme.BLOOM_LIGHT, wallpaperTheme, onWallpaperChange, onDismiss)
                        WallpaperSubItem("Aura Mor", WallpaperTheme.GLOW_PURPLE, wallpaperTheme, onWallpaperChange, onDismiss)
                        WallpaperSubItem("Zümrüt Yeşil", WallpaperTheme.WAVES_EMERALD, wallpaperTheme, onWallpaperChange, onDismiss)
                    }
                }

                // Item 4: Simge boyutu değiştir
                ContextMenuItem("📏 Simge Boyutu", onClick = { showSizesSubMenu = !showSizesSubMenu })
                if (showSizesSubMenu) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F1524))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SizeSubItem("Küçük", IconSize.SMALL, iconSize, onIconSizeChange, onDismiss)
                        SizeSubItem("Orta", IconSize.MEDIUM, iconSize, onIconSizeChange, onDismiss)
                        SizeSubItem("Büyük", IconSize.LARGE, iconSize, onIconSizeChange, onDismiss)
                    }
                }
            }
        }
    }
}

@Composable
fun ContextMenuItem(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 11.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 10.dp)
    )
}

@Composable
fun WallpaperSubItem(
    label: String,
    theme: WallpaperTheme,
    activeTheme: WallpaperTheme,
    onChange: (WallpaperTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val isAct = theme == activeTheme
    Text(
        text = if (isAct) "• $label" else "  $label",
        color = if (isAct) Color(0xFF00ADEF) else Color.LightGray,
        fontSize = 10.sp,
        fontWeight = if (isAct) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onChange(theme)
                onDismiss()
            }
            .padding(vertical = 4.dp)
    )
}

@Composable
fun SizeSubItem(
    label: String,
    size: IconSize,
    activeSize: IconSize,
    onChange: (IconSize) -> Unit,
    onDismiss: () -> Unit
) {
    val isAct = size == activeSize
    Text(
        text = if (isAct) "• $label" else "  $label",
        color = if (isAct) Color(0xFF00ADEF) else Color.LightGray,
        fontSize = 10.sp,
        fontWeight = if (isAct) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onChange(size)
                onDismiss()
            }
            .padding(vertical = 4.dp)
    )
}

// --- Windows 11 Shutdown Animating Sequence Screen Overlay ---
@Composable
fun WindowsShutdownScreen(onShutdownFinish: () -> Unit) {
    // Shutdown animations ticker
    var progressAngle by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 2500) {
            progressAngle = (progressAngle + 12f) % 360f
            delay(30)
        }
        onShutdownFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001F4D)), // iconic solid startup/shutdown blue screen
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant dots progress spinning loop
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .pointerInput(Unit) {}
            ) {
                for (i in 0 until 5) {
                    val angle = progressAngle + (i * 30f)
                    val rad = Math.toRadians(angle.toDouble())
                    val x = 24.dp + (16.dp * Math.cos(rad).toFloat())
                    val y = 24.dp + (16.dp * Math.sin(rad).toFloat())

                    Box(
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 1f - (i * 0.15f)))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Bilgisayarınız Kapatılıyor",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Mustafa oturumu kapatıyor...",
                color = Color(0xFFAEC6E8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- Native Intent Launcher Helper with Safe Exception Catching ---
fun launchNativeSystemApp(context: Context, type: String, onError: (String) -> Unit) {
    val intent = when (type) {
        "camera" -> {
            context.packageManager.getLaunchIntentForPackage("com.android.camera")
                ?: context.packageManager.getLaunchIntentForPackage("com.google.android.GoogleCamera")
                ?: Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        }
        "gallery" -> {
            context.packageManager.getLaunchIntentForPackage("com.google.android.apps.photos")
                ?: context.packageManager.getLaunchIntentForPackage("com.android.gallery3d")
                ?: context.packageManager.getLaunchIntentForPackage("com.sec.android.gallery3d")
                ?: Intent(Intent.ACTION_VIEW).apply {
                    this.type = "image/*"
                }
        }
        "settings" -> {
            Intent(android.provider.Settings.ACTION_SETTINGS)
        }
        "browser" -> {
            Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com"))
        }
        else -> null
    }

    if (intent != null) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            onError("Sistem uygulaması bu cihazda bulunamadı veya açılamadı: ${e.localizedMessage}")
        }
    } else {
        onError("Belirtilen sistem uygulaması uyumsuz.")
    }
}
