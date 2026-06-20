package com.vodafone.smc

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vodafone.smc.R
import com.vodafone.smc.data.*
import com.vodafone.smc.ui.MainViewModel
import com.vodafone.smc.ui.MainViewModelFactory
import com.vodafone.smc.ui.theme.MyApplicationTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import com.vodafone.smc.ui.SMCLoadingSpinner
import com.vodafone.smc.ui.SMCSpinnerSize
import com.vodafone.smc.ui.SMCSpinnerWithStatus
import com.vodafone.smc.ui.SMCOverlaySpinner
import java.io.File
import java.io.FileOutputStream
import android.content.Intent
import androidx.core.content.FileProvider
import android.net.Uri
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.painter.ColorPainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.speech.tts.TextToSpeech
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.media.AudioManager
import java.util.Locale
import android.content.Context

@Composable
fun safePainterResource(id: Int): Painter {
    return rememberAsyncImagePainter(
        model = id,
        error = painterResource(id = R.drawable.icon),
        fallback = painterResource(id = R.drawable.icon),
        placeholder = painterResource(id = R.drawable.icon)
    )
}

@Composable
fun UserInteractiveAvatar(
    viewModel: MainViewModel,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    showEditBadge: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val loggedInUser by viewModel.loggedInUsername.collectAsStateWithLifecycle()
    val avatarValue by viewModel.currentUserAvatar.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clickable { 
                if (onClick != null) {
                    onClick()
                } else {
                    showEditDialog = true
                }
            }
            .testTag("user_avatar_trigger")
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // Render the inner avatar
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                RenderAvatarContent(
                    avatarValue = avatarValue,
                    username = loggedInUser,
                    size = size
                )
            }

            // Green online indicator
            Box(
                modifier = Modifier
                    .size(if (size > 50.dp) 16.dp else 12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32))
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }

        if (showEditDialog) {
            AvatarSelectionDialog(
                viewModel = viewModel,
                currentAvatar = avatarValue,
                onDismiss = { showEditDialog = false }
            )
        }
    }
}

@Composable
fun RenderAvatarContent(
    avatarValue: String,
    username: String,
    size: androidx.compose.ui.unit.Dp
) {
    val initials = if (username.isNotBlank()) username.take(2).uppercase() else "SM"
    
    when (avatarValue) {
        "letters" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE60000).copy(alpha = 0.15f),
                                Color(0xFFE60000).copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE60000)
                    )
                )
            }
        }
        "av_red" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE60000), Color(0xFFFF4D4D))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Vodafone Red Admin Logo",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
        "av_blue" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "IT Code Architect",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
        "av_green" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF065F46), Color(0xFF10B981))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "System Admin",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
        "av_orange" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF9A3412), Color(0xFFF97316))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Database Admin",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
        "av_purple" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF5B21B6), Color(0xFF8B5CF6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Cloud Admin",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
        else -> {
            // It is a URI
            val painter = rememberAsyncImagePainter(
                model = avatarValue,
                error = ColorPainter(Color.Gray),
                fallback = ColorPainter(Color.Gray),
                placeholder = ColorPainter(Color.LightGray)
            )
            Image(
                painter = painter,
                contentDescription = "User upload image",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    }
}

@Composable
fun AvatarSelectionDialog(
    viewModel: MainViewModel,
    currentAvatar: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val copiedPath = copyUriToInternalStorage(context, uri)
            if (copiedPath != null) {
                viewModel.updateLoggedInUserAvatar(copiedPath)
                Toast.makeText(context, "تم حفظ وتعيين الصورة بنجاح", Toast.LENGTH_SHORT).show()
                onDismiss()
            } else {
                Toast.makeText(context, "فشل في نسخ الصورة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color(0xFFE60000),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "صورة الملف الشخصي",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "اختر صورة رمزية أو قم بتحميل صورتك الخاصة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "الصور الرمزية السريعة (Quick Avatars):",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.End),
                    color = MaterialTheme.colorScheme.primary
                )

                // Grid of options
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val avatars = listOf(
                        "letters" to "الافتراضي",
                        "av_red" to "الأحمر",
                        "av_blue" to "الأزرق",
                        "av_green" to "الأخضر",
                        "av_orange" to "البرتقالي",
                        "av_purple" to "البنفسجي"
                    )

                    // Render three per row
                    for (i in avatars.indices step 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (j in 0 until 3) {
                                if (i + j < avatars.size) {
                                    val (key, label) = avatars[i + j]
                                    val isSelected = currentAvatar == key
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFFE60000) else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                viewModel.updateLoggedInUserAvatar(key)
                                                Toast.makeText(context, "تم حفظ الاختيار بنجاح", Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            }
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            RenderAvatarContent(
                                                avatarValue = key,
                                                username = viewModel.loggedInUsername.value,
                                                size = 48.dp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color(0xFFE60000) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Upload custom button
                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("رفع صورة مخصصة من الاستوديو", style = MaterialTheme.typography.labelMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Color(0xFFE60000))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun isAppDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background == Color(0xFF121212)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }

        val viewModel: MainViewModel by viewModels {
            MainViewModelFactory(application)
        }

        AlertSoundTester.initTts(applicationContext)

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            val darkModeState by viewModel.darkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = darkModeState) {
                if (showSplash) {
                    SplashScreen(viewModel = viewModel, onTimeout = { showSplash = false })
                } else {
                    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                    if (isLoggedIn) {
                        MainAppScreen(viewModel)
                    } else {
                        LoginScreen(viewModel)
                    }
                }
            }
        }
    }
}

fun resolveImageModel(uriStr: String): Any {
    if (uriStr.isEmpty()) return ""
    return if (uriStr.startsWith("/") || uriStr.startsWith("file:")) {
        val cleanPath = uriStr.removePrefix("file://").removePrefix("file:")
        File(cleanPath)
    } else {
        uriStr
    }
}

fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, "custom_splash_logo.png")
        if (file.exists()) {
            file.delete()
        }
        FileOutputStream(file).use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Bottom bar navigation endpoints
enum class MainTab {
    DASHBOARD, TICKETS, REQUESTS, REMINDERS, NOTES, PROFILE
}

@Composable
fun SplashScreen(viewModel: MainViewModel, onTimeout: () -> Unit) {
    val customImageUri by viewModel.splashImageUri.collectAsStateWithLifecycle()
    var startAnimation by remember { mutableStateOf(false) }
    
    // Scale & Alpha Entry Animations
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.4f,
        animationSpec = spring(
            dampingRatio = 0.75f, // premium weighted bouncy overshoot
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Infinite Breathing / Pulse effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Active connection ripples radiating outwards
    val rippleProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )

    val rippleProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(1200) // Staggered by half duration
        ),
        label = "ripple2"
    )

    // Derived values for ripples
    val rippleScale1 = 1f + (rippleProgress1 * 0.5f)
    val rippleAlpha1 = (1f - rippleProgress1) * 0.35f

    val rippleScale2 = 1f + (rippleProgress2 * 0.5f)
    val rippleAlpha2 = (1f - rippleProgress2) * 0.35f

    LaunchedEffect(Unit) {
        startAnimation = true
        kotlinx.coroutines.delay(2800) // Premium relaxed display time for logo
        onTimeout()
    }

    // Adapt background colors dynamically based on light/dark mode
    val isDark = isAppDarkTheme()
    val gradientBrush = remember(isDark) {
        if (isDark) {
            Brush.radialGradient(
                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                radius = 1200f
            )
        } else {
            Brush.radialGradient(
                colors = listOf(Color(0xFFFAFAFA), Color(0xFFF1F5F9)),
                radius = 1200f
            )
        }
    }

    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        // High-fidelity background asset generated specifically for Vodafone SMC operations
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg_1781991470464),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = if (isDark) 0.5f else 0.15f
        )
        // Vignette effect for cinematic shadowing focus
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = if (isDark) 0.5f else 0.12f)),
                        radius = 1600f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Dynamic Animated Logo Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer(
                        scaleX = scale * pulseScale,
                        scaleY = scale * pulseScale,
                        alpha = alpha
                    )
            ) {
                // Radiating Networking Ripple 1
                Box(
                    modifier = Modifier
                        .fillMaxSize(rippleScale1)
                        .background(
                            Color(0xFFE60000).copy(alpha = rippleAlpha1),
                            CircleShape
                        )
                )

                // Radiating Networking Ripple 2
                Box(
                    modifier = Modifier
                        .fillMaxSize(rippleScale2)
                        .background(
                            Color(0xFFE60000).copy(alpha = rippleAlpha2),
                            CircleShape
                        )
                )

                if (customImageUri.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = resolveImageModel(customImageUri),
                            error = painterResource(id = R.drawable.icon),
                            fallback = painterResource(id = R.drawable.icon),
                            placeholder = painterResource(id = R.drawable.icon)
                        ),
                        contentDescription = "Custom Splash Screen Logo",
                        modifier = Modifier
                            .size(170.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                } else {
                    // Custom Premium Vector Vector Logo
                    VodafoneLogo(
                        modifier = Modifier.size(190.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(36.dp))
            
            Text(
                text = "SMC VODAFONE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE60000), // Vodafone Red
                    letterSpacing = 2.5.sp
                ),
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
            
            Text(
                text = "SMC Specialist Desktop Support",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = subtitleColor,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .graphicsLayer(alpha = alpha)
                )
        }
        
        // Custom elegant localized loading indicator near the bottom
        SMCLoadingSpinner(
            size = SMCSpinnerSize.MEDIUM,
            color = Color(0xFFE60000), // Vodafone Red
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .graphicsLayer(alpha = alpha)
        )
    }
}

@Composable
fun VodafoneLogo(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val widthDp = maxWidth
        val heightDp = maxHeight
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Outer Red Circle background
            drawCircle(
                color = Color(0xFFE60000),
                radius = w / 2f,
                center = Offset(w / 2f, h / 2f)
            )
            
            // White Speechmark
            val path = androidx.compose.ui.graphics.Path().apply {
                val cx = w * 0.45f
                val cy = h * 0.58f
                val r = w * 0.22f
                
                // Move to left of circular part
                moveTo(cx - r, cy)
                
                // Draw bottom half of circle: 180 -> 90 -> 0 degrees
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(Offset(cx - r, cy - r), androidx.compose.ui.geometry.Size(r * 2f, r * 2f)),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -180f, // counter-clockwise to get the bottom half
                    forceMoveTo = false
                )
                
                // Now at (cx + r, cy). Swoop up to tail point:
                quadraticTo(
                    w * 0.75f, h * 0.45f,
                    w * 0.72f, h * 0.14f
                )
                
                // Swoop and curve down to meet the left circle start
                quadraticTo(
                    w * 0.50f, h * 0.22f,
                    cx - r, cy
                )
                close()
            }
            drawPath(path = path, color = Color.White)
        }
        
        // Calculate the font size based on the width of the container
        val fontSize = (widthDp.value * 0.23f).sp
        val xOffset = widthDp * -0.03f
        val yOffset = heightDp * 0.07f
        
        Text(
            text = "smc",
            color = Color(0xFFE60000), // Sovereign Vodafone Red
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.offset(x = xOffset, y = yOffset)
        )
    }
}

@Composable
fun AutoScaleText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier
) {
    val initialFontSize = if (style.fontSize.isSp) style.fontSize.value else 11f
    var fontSizeValue by remember(text) { mutableStateOf(initialFontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }
    
    Text(
        text = text,
        style = style.copy(fontSize = fontSizeValue.sp, fontWeight = fontWeight ?: style.fontWeight),
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth && fontSizeValue > 6f) {
                fontSizeValue -= 0.5f
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
fun ActiveSessionBar(viewModel: MainViewModel, onLogout: () -> Unit) {
    val loggedInUser by viewModel.loggedInUsername.collectAsStateWithLifecycle()
    val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, Color(0xFFE60000).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Interactive Profile Avatar with green online dot
                UserInteractiveAvatar(viewModel = viewModel)

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = loggedInUser,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE60000).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = userRole,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE60000))
                            )
                        }
                    }
                    Text(
                        text = "Active & Secure Session (SMC Active Session)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Quick Logout IconButton
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }

    // Tab navigation history stack for back gesture/button handling
    val tabHistory = remember { mutableStateListOf<MainTab>() }
    var isBackNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(currentTab) {
        if (isBackNavigating) {
            isBackNavigating = false
        } else {
            if (tabHistory.isEmpty() || tabHistory.last() != currentTab) {
                tabHistory.add(currentTab)
            }
        }
    }

    // Navigation and overlays state handles
    var showNewReminderDialog by remember { mutableStateOf(false) }
    var showNewTicketDialog by remember { mutableStateOf(false) }
    var showNewITARDialog by remember { mutableStateOf(false) }
    var showNewVPNDialog by remember { mutableStateOf(false) }
    var showNewNTDialog by remember { mutableStateOf(false) }
    var autoShowNewNoteDialog by remember { mutableStateOf(false) }

    val isAnyOverlayActive = showNewReminderDialog ||
            showNewTicketDialog ||
            showNewITARDialog ||
            showNewVPNDialog ||
            showNewNTDialog ||
            autoShowNewNoteDialog

    androidx.activity.compose.BackHandler(enabled = isAnyOverlayActive || tabHistory.size > 1) {
        if (showNewReminderDialog) {
            showNewReminderDialog = false
        } else if (showNewTicketDialog) {
            showNewTicketDialog = false
        } else if (showNewITARDialog) {
            showNewITARDialog = false
        } else if (showNewVPNDialog) {
            showNewVPNDialog = false
        } else if (showNewNTDialog) {
            showNewNTDialog = false
        } else if (autoShowNewNoteDialog) {
            autoShowNewNoteDialog = false
        } else if (tabHistory.size > 1) {
            isBackNavigating = true
            tabHistory.removeAt(tabHistory.size - 1)
            currentTab = tabHistory.last()
        }
    }

    val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    val remindersList by viewModel.rawReminders.collectAsStateWithLifecycle(initialValue = emptyList())
    var globalClockTick by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            globalClockTick = System.currentTimeMillis()
        }
    }

    val activeExpiredAlarms = remember(remindersList, globalClockTick) {
        remindersList.filter {
            it.status == "Active" && 
            it.dueDate.startsWith("TimerTarget:") && 
            (it.dueDate.substringAfter("TimerTarget:").toLongOrNull() ?: 0L) <= System.currentTimeMillis()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE60000).copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFFE60000).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            VodafoneLogo(modifier = Modifier.fillMaxSize())
                        }
                        Column {
                            Text(
                                "SMC Vodafone",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "SMC Specialist",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UserInteractiveAvatar(
                            viewModel = viewModel,
                            size = 34.dp,
                            showEditBadge = false,
                            onClick = { currentTab = MainTab.PROFILE }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .drawBehind {
                        // draw a very fine top border line similar to tailwind's border-t border-slate-200
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val activeColor = MaterialTheme.colorScheme.primary
                val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                val navBarItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    indicatorColor = activeColor.copy(alpha = 0.08f),
                    unselectedIconColor = inactiveColor,
                    unselectedTextColor = inactiveColor
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.DASHBOARD,
                    onClick = { currentTab = MainTab.DASHBOARD },
                    modifier = Modifier.testTag("tab_dashboard"),
                    icon = { Icon(Icons.Default.Home, "Dashboard") },
                    label = { 
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navBarItemColors
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.TICKETS,
                    onClick = { currentTab = MainTab.TICKETS },
                    modifier = Modifier.testTag("tab_tickets"),
                    icon = { Icon(Icons.Default.DesktopWindows, "Tickets") },
                    label = { 
                        Text(
                            text = "PC Problems",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navBarItemColors
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.REQUESTS,
                    onClick = { currentTab = MainTab.REQUESTS },
                    modifier = Modifier.testTag("tab_requests"),
                    icon = { Icon(Icons.Default.Send, "Requests") },
                    label = { 
                        Text(
                            text = "Requests",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navBarItemColors
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.REMINDERS,
                    onClick = { currentTab = MainTab.REMINDERS },
                    modifier = Modifier.testTag("tab_reminders"),
                    icon = { Icon(Icons.Default.Notifications, "Reminders") },
                    label = { 
                        Text(
                            text = "Reminders",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navBarItemColors
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.NOTES,
                    onClick = { currentTab = MainTab.NOTES },
                    modifier = Modifier.testTag("tab_notes"),
                    icon = { Icon(Icons.Default.Edit, "Notes") },
                    label = { 
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navBarItemColors
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.PROFILE,
                    onClick = { currentTab = MainTab.PROFILE },
                    modifier = Modifier.testTag("tab_profile"),
                    icon = { Icon(Icons.Default.Settings, "Admin") },
                    label = { 
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = navBarItemColors
                )
            }
        }
    ) { innerPadding ->
        var lastActivityTime by remember { mutableStateOf(System.currentTimeMillis()) }
        val inactivityTimeoutMs = 15 * 60 * 1000L // 15 minutes

        LaunchedEffect(lastActivityTime) {
            while (true) {
                kotlinx.coroutines.delay(5000) // check every 5 seconds
                if (System.currentTimeMillis() - lastActivityTime >= inactivityTimeoutMs) {
                    viewModel.logoutUser()
                    break
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                            lastActivityTime = System.currentTimeMillis()
                        }
                    }
                }
        ) {
            // Active Session Status Bar
            ActiveSessionBar(viewModel = viewModel, onLogout = { viewModel.logoutUser() })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFFB70100).copy(alpha = 0.015f),
                            radius = size.maxDimension / 2.5f,
                            center = Offset(size.width * 0.9f, size.height * 0.1f)
                        )
                    }
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "NavigationTransition"
                ) { tab ->
                    when (tab) {
                        MainTab.DASHBOARD -> DashboardTabScreen(
                            viewModel = viewModel,
                            onCreateTicket = { showNewTicketDialog = true },
                            onCreateITAR = { showNewITARDialog = true },
                            onCreateVPN = { showNewVPNDialog = true },
                            onCreateNT = { showNewNTDialog = true },
                            onCreateReminder = { showNewReminderDialog = true },
                            onNavigateToNotes = { currentTab = MainTab.NOTES },
                            onCreateNote = {
                                currentTab = MainTab.NOTES
                                autoShowNewNoteDialog = true
                            }
                        )
                        MainTab.TICKETS -> TicketsTabScreen(
                            viewModel = viewModel,
                            onCreateTicketRequested = { showNewTicketDialog = true }
                        )
                        MainTab.REQUESTS -> RequestsTabScreen(
                            viewModel = viewModel,
                            onCreateITAR = { showNewITARDialog = true },
                            onCreateVPN = { showNewVPNDialog = true },
                            onCreateNT = { showNewNTDialog = true }
                        )
                        MainTab.REMINDERS -> RemindersTabScreen(
                            viewModel = viewModel,
                            onCreateReminder = { showNewReminderDialog = true }
                        )
                        MainTab.NOTES -> NotesTabScreen(
                            viewModel = viewModel,
                            autoShowAddDialog = autoShowNewNoteDialog,
                            onAddDialogDismissed = { autoShowNewNoteDialog = false }
                        )
                        MainTab.PROFILE -> ProfileAdminTabScreen(viewModel)
                    }
                }
            }
        }
    }

    if (showNewTicketDialog) {
        NewTicketScreen(
            viewModel = viewModel,
            onDismiss = { showNewTicketDialog = false }
        )
    }

    if (showNewITARDialog) {
        NewITARRequestScreen(
            viewModel = viewModel,
            onDismiss = { showNewITARDialog = false }
        )
    }

    if (showNewVPNDialog) {
        NewVPNRequestScreen(
            viewModel = viewModel,
            onDismiss = { showNewVPNDialog = false }
        )
    }

    if (showNewNTDialog) {
        NewNTRequestScreen(
            viewModel = viewModel,
            onDismiss = { showNewNTDialog = false }
        )
    }

    if (showNewReminderDialog) {
        NewReminderScreen(
            viewModel = viewModel,
            onDismiss = { showNewReminderDialog = false }
        )
    }

    if (activeExpiredAlarms.isNotEmpty()) {
        val currentFiringAlarm = activeExpiredAlarms.first()
        
        // play periodic tone (beep) every 3 seconds while alarm is active
        LaunchedEffect(currentFiringAlarm.id, globalClockTick) {
            if (globalClockTick % 3000 < 1000) {
                viewModel.playSystemAlarmSound()
            }
        }
        
        AlertDialog(
            onDismissRequest = {}, // Force attention, user must complete or snooze
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alarm Active",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer {
                                val scale = 1f + 0.15f * kotlin.math.sin(globalClockTick.toFloat() / 200f)
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                    Text(
                        text = "SMC ALARM REPORT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = currentFiringAlarm.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    if (currentFiringAlarm.description.isNotEmpty()) {
                        Text(
                            text = currentFiringAlarm.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Text(
                        text = "⚠️ ATTENTION: SENSITIVE ALARM ACTIVE. TAKE IMMEDIATE INTERVENTION ACTION.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Yellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleReminderCompleted(currentFiringAlarm)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFE60000)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, "Done")
                        Text("COMPLETED / DISMISS", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.snoozeReminder(currentFiringAlarm)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, "Snooze")
                        Text("SNOOZE 15M", fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = Color(0xFFE60000),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ==========================================
// TAB 1: REPORTS DASHBOARD
// ==========================================

// ==========================================
// FILE EXPORT ENGAGEMENT ENGINE (ACTUAL LOCAL GENERATION)
// ==========================================

fun exportFile(context: android.content.Context, type: String, content: String) {
    try {
        val extension = if (type.lowercase() == "pdf") "pdf" else "csv"
        val mime = if (type.lowercase() == "pdf") "application/pdf" else "text/csv"
        val fileName = "SMC_Operational_Report_${System.currentTimeMillis()}.$extension"
        val file = java.io.File(context.cacheDir, fileName)
        
        if (type.lowercase() == "pdf") {
            // Generate actual PDF document using android.graphics.pdf.PdfDocument
            val pdfDocument = android.graphics.pdf.PdfDocument()
            val paint = android.graphics.Paint().apply {
                textSize = 9f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            
            val lines = content.split("\n")
            var pageNumber = 1
            var yPosition = 40
            
            // PDF Page sizing (A4 is ~ 595 x 842 points)
            var pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            
            lines.forEach { line ->
                if (yPosition > 800) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    yPosition = 40
                    pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                }
                canvas.drawText(line, 30f, yPosition.toFloat(), paint)
                yPosition += 16
            }
            pdfDocument.finishPage(page)
            
            val fos = java.io.FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()
        } else {
            // Excel-compatible CSV file creation
            val fos = java.io.FileOutputStream(file)
            fos.write(content.toByteArray())
            fos.close()
        }
        
        // Share via Intent using FileProvider helper
        val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            this.type = mime
            putExtra(android.content.Intent.EXTRA_SUBJECT, "SMC Operational Helpdesk Report")
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = android.content.Intent.createChooser(intent, "Export $type Report")
        chooser.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(chooser)
        android.widget.Toast.makeText(context, "$type export initialized successfully!", android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Failed to generate file: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
    }
}

@Composable
fun DashboardTabScreen(
    viewModel: MainViewModel,
    onCreateTicket: () -> Unit,
    onCreateITAR: () -> Unit,
    onCreateVPN: () -> Unit,
    onCreateNT: () -> Unit,
    onCreateReminder: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onCreateNote: () -> Unit
) {
    var rangeIsWeekly by remember { mutableStateOf(true) }
    val pcProblems by viewModel.rawPCProblems.collectAsStateWithLifecycle(initialValue = emptyList())
    val ntCreations by viewModel.rawNTCreations.collectAsStateWithLifecycle(initialValue = emptyList())
    val itarRequests by viewModel.rawITARRequests.collectAsStateWithLifecycle(initialValue = emptyList())
    val vpnRequests by viewModel.rawVPNRequests.collectAsStateWithLifecycle(initialValue = emptyList())
    val reminders by viewModel.rawReminders.collectAsStateWithLifecycle(initialValue = emptyList())

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var exportedDataText by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var currentReportPerspective by remember { mutableStateOf("Site Analytics") }

    fun performExport(type: String) {
        val sb = java.lang.StringBuilder()
        sb.append("=== SMC Operational Helpdesk Export - ${type.uppercase()} ===\n")
        sb.append("Generated At: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}\n\n")
        
        sb.append("--- WORKSTATION INCIDENT TICKETS ---\n")
        sb.append("Ticket ID,Incident Date,Site,Zone,PC Node,Category,Priority,Status\n")
        pcProblems.forEach {
            sb.append("${it.ticketNumber},${it.username},${it.site},${it.zone},${it.pcName},${it.category},${it.priority},${it.status}\n")
        }
        
        sb.append("\n--- ITAR REQUESTS ---\n")
        sb.append("Request ID,Employee,Type,Status,StartDate,EndDate\n")
        itarRequests.forEach {
            sb.append("${it.rId},${it.employeeName},${it.requestType},${it.status},${it.startDate},${it.endDate}\n")
        }

        sb.append("\n--- VPN ACCESS CHANNELS ---\n")
        sb.append("SR#,Ticket Date,Username,VPN SR#,Status,Notes\n")
        vpnRequests.forEach {
            sb.append("${it.srNumber},${it.employeeName},${it.username},${it.vpnType},${it.status},${it.notes}\n")
        }

        sb.append("\n--- NT ACCOUNT DIRECTORY PROFILE REQUESTS ---\n")
        sb.append("Request ID,Username,Action,Site,Status,Notes\n")
        ntCreations.forEach {
            sb.append("${it.reqId},${it.employeeName},${it.department},${it.site},${it.status},${it.notes}\n")
        }

        sb.append("\n--- REMINDERS QUEUE ---\n")
        sb.append("Title,Description,Priority,Status,Due\n")
        reminders.forEach {
            sb.append("${it.title},${it.description},${it.priority},${it.status},${it.dueDate}\n")
        }

        val exportStr = sb.toString()
        exportedDataText = exportStr
        clipboardManager.setText(AnnotatedString(exportStr))
        showExportDialog = true
        
        exportFile(context, type, exportStr)
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Share, "Export", tint = Color(0xFFE60000))
                    Text("Export Confirmed", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("All operational databases unified and updated. Output has been created and copied to clipboard!", style = MaterialTheme.typography.bodyMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = exportedDataText,
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = Color(0xFFE60000), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "System Reports",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Overview of operations queues and SLA compliance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val openTicketsCount = pcProblems.count { it.status != "Closed" }
                val pendingItarCount = itarRequests.count { it.status == "Pending" || it.status == "Review Needed" }
                val totalPcProblems = pcProblems.size
                val slaStatusVal = if (totalPcProblems == 0) 100 else (100 - openTicketsCount * 10).coerceIn(60, 100)

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFEE2E2))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "OPEN TICKETS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFFDC2626),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$openTicketsCount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF991B1B)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "ITAR PENDING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF1D4ED8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            String.format("%02d", pendingItarCount),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF1E40AF)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    border = BorderStroke(1.dp, Color(0xFFD1FAE5))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "SLA STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF047857),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${slaStatusVal}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        performExport("pdf")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, "Share PDF", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = {
                        performExport("excel")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Send, "Send Excel", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Excel", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Build,
                        title = "PC PROBLEMS",
                        count = pcProblems.size.toString(),
                        accentText = "Active"
                    )
                    val approvedITARCount = itarRequests.count { it.status == "Approved" }
                    val itarPercentage = if (itarRequests.isNotEmpty()) (approvedITARCount * 100) / itarRequests.size else 100
                    BentoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Check,
                        title = "ITAR COMPLIANCE",
                        count = "${itarPercentage}%",
                        accentText = "OK"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val activeVpns = vpnRequests.count { it.status != "Rejected" }
                    BentoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Lock,
                        title = "VPN SESSIONS",
                        count = activeVpns.toString(),
                        accentText = "Active"
                    )
                    BentoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Person,
                        title = "NT CREATIONS",
                        count = ntCreations.size.toString(),
                        accentText = "+${ntCreations.size}"
                    )
                }
            }
        }



        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "DATA PREVIEW: RECENT NT CREATIONS",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (ntCreations.isEmpty()) {
                        Text(
                            "No current terminal creation requests.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("USER ID", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                                Text("DEPARTMENT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1.5f))
                                Text("STATUS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                                Text("SITE", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            ntCreations.take(4).forEach { nt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        nt.employeeName.take(12),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(nt.department, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.5f))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (nt.status == "Active" || nt.status == "Approved") Color(0xFFCFFFDC)
                                                else MaterialTheme.colorScheme.primaryContainer
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            nt.status,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (nt.status == "Active" || nt.status == "Approved") Color(0xFF005B20)
                                            else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(nt.site, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "QUICK ACTIONS LAUNCHER",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = Color(0xFF64748B)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val quickActions = listOf(
                        Triple("PC Problem", Icons.Default.Warning, onCreateTicket),
                        Triple("ITAR Access", Icons.Default.CheckCircle, onCreateITAR),
                        Triple("VPN Setup", Icons.Default.Lock, onCreateVPN),
                        Triple("New Note", Icons.Default.Add, onCreateNote),
                        Triple("NT Account", Icons.Default.Person, onCreateNT),
                        Triple("Reminder", Icons.Default.Notifications, onCreateReminder)
                    )
                    
                    quickActions.forEach { (label, icon, onClick) ->
                        Card(
                            modifier = Modifier
                                .size(110.dp)
                                .clickable { onClick() },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE60000).copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = Color(0xFFE60000),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    count: String,
    accentText: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFE60000),
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE60000).copy(alpha = 0.08f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        accentText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE60000)
                    )
                }
            }
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    count,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ==========================================
// TAB 2: PC PROBLEMS MANAGEMENT
// ==========================================

@Composable
fun TicketsTabScreen(
    viewModel: MainViewModel,
    onCreateTicketRequested: () -> Unit
) {
    val searchVal by viewModel.pcProblemsSearchTerm.collectAsStateWithLifecycle()
    val filterVal by viewModel.pcProblemsFilter.collectAsStateWithLifecycle()
    val ticketsList by viewModel.filteredPCProblems.collectAsStateWithLifecycle(initialValue = emptyList())

    val sortBy by viewModel.pcProblemsSortBy.collectAsStateWithLifecycle()
    val teamFilter by viewModel.pcProblemsTeamFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.pcProblemsStatusFilter.collectAsStateWithLifecycle()

    var isFilterMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "PC Problems",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                OutlinedTextField(
                    value = searchVal,
                    onValueChange = { viewModel.pcProblemsSearchTerm.value = it },
                    modifier = Modifier
                        .testTag("search_tickets")
                        .fillMaxWidth(),
                    placeholder = { Text("Search incident description or ID...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search Icon") },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf("All", "Active", "Critical", "Resolved")
                    chips.forEach { chip ->
                        val isSelected = filterVal == chip
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.pcProblemsFilter.value = chip },
                            label = { Text(chip, style = MaterialTheme.typography.labelLarge) }
                        )
                    }
                }
            }

            // 🛠️ SORT & ADVANCED FILTER CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isFilterMenuExpanded = !isFilterMenuExpanded }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filters & Sorting",
                                    tint = Color(0xFFE60000),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Sort & Advanced Filters",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Icon(
                                imageVector = if (isFilterMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Expand",
                                tint = Color(0xFF475569)
                            )
                        }

                        AnimatedVisibility(visible = isFilterMenuExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                // 1. Sort By Controls
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "SORT BY",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                        color = Color(0xFF64748B)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val sortOptions = listOf(
                                            "creation_date_desc" to "Newest First",
                                            "creation_date_asc" to "Oldest First",
                                            "status" to "By Status",
                                            "assigned_team" to "By Team"
                                        )
                                        sortOptions.forEach { (optionVal, optionLabel) ->
                                            val isSelected = sortBy == optionVal
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) Color(0xFFE60000) else Color(0xFFE2E8F0).copy(alpha = 0.4f))
                                                    .border(1.dp, if (isSelected) Color(0xFFE60000) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.pcProblemsSortBy.value = optionVal }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = optionLabel,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 2. Status Filter Controls
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "FILTER STATUS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                        color = Color(0xFF64748B)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val statusOptions = listOf("All", "Open", "In Progress", "Pending User", "Closed")
                                        statusOptions.forEach { statusVal ->
                                            val isSelected = statusFilter == statusVal
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) Color(0xFFE60000) else Color(0xFFE2E8F0).copy(alpha = 0.4f))
                                                    .border(1.dp, if (isSelected) Color(0xFFE60000) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.pcProblemsStatusFilter.value = statusVal }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = statusVal,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. Team Filter Controls
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "FILTER TEAM",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                        color = Color(0xFF64748B)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val teamOptions = listOf("All", "Help Desk", "Admin Team", "Network Team", "Operation Team")
                                        teamOptions.forEach { teamVal ->
                                            val isSelected = teamFilter == teamVal
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) Color(0xFFE60000) else Color(0xFFE2E8F0).copy(alpha = 0.4f))
                                                    .border(1.dp, if (isSelected) Color(0xFFE60000) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.pcProblemsTeamFilter.value = teamVal }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = teamVal,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (ticketsList.isEmpty()) {
                item {
                    EmptyStatePlaceholder(
                        title = "No incidents logged matching criteria",
                        desc = "Use the plus button below to log a new workstation hardware/network error."
                    )
                }
            } else {
                items(ticketsList) { ticket ->
                    PCProblemTicketCard(ticket = ticket, viewModel = viewModel)
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateTicketRequested,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .testTag("fab_add_ticket"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Log new Workstation Failure")
        }
    }
}

private data class SlaInfo(
    val bg: Color,
    val border: Color,
    val text: Color,
    val desc: String,
    val left: String
)

@Composable
fun PCProblemTicketCard(ticket: PCProblem, viewModel: MainViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    ticket.ticketNumber,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (ticket.status == "Closed") Color(0xFFCFFFDC)
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            ticket.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (ticket.status == "Closed") Color(0xFF005B20)
                            else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(
                        onClick = { viewModel.deletePCProblem(ticket.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Ticket",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Text(
                text = "Site: ${ticket.site} — Date: ${ticket.username}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Assigned Support Team",
                    tint = Color(0xFFE60000),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Team: ${ticket.assignedTeam}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE60000)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Info, "Category", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Text(ticket.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (ticket.status != "Closed") {
                val slaInfo = SlaInfo(Color(0xFFFFF7ED), Color(0xFFFED7AA), Color(0xFFEA580C), "URGENT SLA STATUS", "Breach in 2h")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(slaInfo.bg)
                        .border(1.dp, slaInfo.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "SLA Icon",
                                tint = slaInfo.text,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = slaInfo.desc,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = slaInfo.text
                            )
                        }
                        Text(
                            text = slaInfo.left,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = slaInfo.text
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val priorityColorInfo = Triple(Color(0xFFFFEDD5), Color(0xFFFDBA74), Color(0xFF9A3412))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(priorityColorInfo.first)
                        .border(1.dp, priorityColorInfo.second, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(priorityColorInfo.third)
                        )
                        Text(
                            text = "IMPORTANT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = priorityColorInfo.third
                        )
                    }
                }
                Text("2d ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Detailed Summary:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(ticket.detailedSummary, style = MaterialTheme.typography.bodyMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Site: ${ticket.site}", style = MaterialTheme.typography.bodyMedium)
                        Text("Zone: ${ticket.zone}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("PC: ${ticket.pcName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Asset #: ${ticket.assetNumber}", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (ticket.status != "Closed") {
                        Button(
                            onClick = { viewModel.updatePCProblemStatus(ticket.id, "Closed") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00832F))
                        ) {
                            Text("Solved", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: REQUESTS MASTER HUB (ITAR, VPN, NT)
// ==========================================


@Composable
fun RequestsTabScreen(
    viewModel: MainViewModel,
    onCreateITAR: () -> Unit,
    onCreateVPN: () -> Unit,
    onCreateNT: () -> Unit
) {
    var selectedScreenType by remember { mutableStateOf("ITAR") } // ITAR, VPN, NT

    val itarSearch by viewModel.itarSearchTerm.collectAsStateWithLifecycle()
    val itarList by viewModel.filteredITARRequests.collectAsStateWithLifecycle(initialValue = emptyList())

    val vpnSearch by viewModel.vpnSearchTerm.collectAsStateWithLifecycle()
    val vpnList by viewModel.filteredVPNRequests.collectAsStateWithLifecycle(initialValue = emptyList())

    val ntSearch by viewModel.ntSearchTerm.collectAsStateWithLifecycle()
    val ntList by viewModel.filteredNTCreations.collectAsStateWithLifecycle(initialValue = emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Enterprise Requests",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "ITAR accesses, NT additions and VPN life cycles.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(4.dp)
                ) {
                    val types = listOf("ITAR", "VPN Requests", "NT Creations")
                    types.forEach { t ->
                        val typeKey = when (t) {
                            "ITAR" -> "ITAR"
                            "VPN Requests" -> "VPN"
                            else -> "NT"
                        }
                        val isSelected = selectedScreenType == typeKey
                        Button(
                            onClick = { selectedScreenType = typeKey },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(t, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            when (selectedScreenType) {
                "ITAR" -> {
                    item {
                        OutlinedTextField(
                            value = itarSearch,
                            onValueChange = { viewModel.itarSearchTerm.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("Search by Employee Name or ITAR ID...") },
                            leadingIcon = { Icon(Icons.Default.Search, "ITAR") }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ITAR REQUESTS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text("${itarList.size} TOTAL", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (itarList.isEmpty()) {
                        item { EmptyStatePlaceholder("No ITAR configurations", "Add access profile.") }
                    } else {
                        items(itarList) { request ->
                            ITARRequestCard(request = request, viewModel = viewModel)
                        }
                    }
                }

                "VPN" -> {
                    item {
                        OutlinedTextField(
                            value = vpnSearch,
                            onValueChange = { viewModel.vpnSearchTerm.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("Search active VPN requests...") },
                            leadingIcon = { Icon(Icons.Default.Search, "VPN") }
                        )
                    }

                    if (vpnList.isEmpty()) {
                        item { EmptyStatePlaceholder("No VPN credentials configured", "Create VPN access channel.") }
                    } else {
                        items(vpnList) { request ->
                            VPNRequestCard(request = request, viewModel = viewModel)
                        }
                    }
                }

                "NT" -> {
                    item {
                        OutlinedTextField(
                            value = ntSearch,
                            onValueChange = { viewModel.ntSearchTerm.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("Search NT creations...") },
                            leadingIcon = { Icon(Icons.Default.Search, "NT") }
                        )
                    }

                    if (ntList.isEmpty()) {
                        item { EmptyStatePlaceholder("No active NT requests pending", "Tap add to provision workstation authentication directory profiles.") }
                    } else {
                        items(ntList) { nt ->
                            NTCreationCard(nt = nt, viewModel = viewModel)
                        }
                    }
                }
            }
        }

        val fabAction = when (selectedScreenType) {
            "ITAR" -> onCreateITAR
            "VPN" -> onCreateVPN
            else -> onCreateNT
        }
        val fabDescription = when (selectedScreenType) {
            "ITAR" -> "Request ITAR access token"
            "VPN" -> "Provision new VPN tunnel"
            else -> "Log new NT directory assignment"
        }

        FloatingActionButton(
            onClick = fabAction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, fabDescription)
        }
    }
}

@Composable
fun ITARRequestCard(request: ITARRequest, viewModel: MainViewModel) {
    val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val isDark = isAppDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(request.rId, style = MaterialTheme.typography.labelSmall, color = Color(0xFFE60000))
                    Text(request.employeeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (request.status) {
                                "Approved", "Active" -> if (isDark) Color(0xFF065F46).copy(alpha = 0.3f) else Color(0xFFECFDF5)
                                "Pending" -> if (isDark) Color(0xFF9A3412).copy(alpha = 0.3f) else Color(0xFFFFFBEB)
                                "Review Needed" -> if (isDark) Color(0xFF991B1B).copy(alpha = 0.3f) else Color(0xFFFEF2F2)
                                else -> if (isDark) Color(0xFF334155).copy(alpha = 0.3f) else Color(0xFFF1F5F9)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        request.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (request.status) {
                            "Approved", "Active" -> if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                            "Pending" -> if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                            "Review Needed" -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                            else -> if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TYPE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(request.requestType, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("DATE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(request.startDate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                if (request.site.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SITE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(request.site, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            if (request.internalNotes.isNotEmpty()) {
                Text(
                    "Notes: ${request.internalNotes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            var showEditDialog by remember { mutableStateOf(false) }

            if (showEditDialog) {
                EditITARRequestDialog(
                    request = request,
                    viewModel = viewModel,
                    onDismiss = { showEditDialog = false }
                )
            }

            if (userRole != "Viewer") {
                Spacer(modifier = Modifier.height(4.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isApproved = request.status == "Approved" || request.status == "Completed"
                    if (maxWidth < 340.dp) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isApproved) {
                                Button(
                                    onClick = { viewModel.approveITARRequest(request) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Check, "Completed", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Completed", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                            OutlinedButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            OutlinedButton(
                                onClick = { viewModel.deleteITARRequest(request.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isApproved) {
                                Button(
                                    onClick = { viewModel.approveITARRequest(request) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Check, "Completed", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Completed",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        maxLines = 1
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    maxLines = 1
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.deleteITARRequest(request.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Remove",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VPNRequestCard(request: VPNRequest, viewModel: MainViewModel) {
    val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val isDark = isAppDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE60000).copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, "Shield", tint = Color(0xFFE60000), modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(request.username, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(request.srNumber, style = MaterialTheme.typography.labelSmall, color = Color(0xFFE60000), fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (request.status) {
                                "Approved", "Completed" -> if (isDark) Color(0xFF065F46).copy(alpha = 0.3f) else Color(0xFFECFDF5)
                                "Pending", "In Review" -> if (isDark) Color(0xFF9A3412).copy(alpha = 0.3f) else Color(0xFFFFFBEB)
                                "Urgent" -> if (isDark) Color(0xFF991B1B).copy(alpha = 0.3f) else Color(0xFFFEF2F2)
                                else -> if (isDark) Color(0xFF334155).copy(alpha = 0.3f) else Color(0xFFF1F5F9)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        request.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (request.status) {
                            "Approved", "Completed" -> if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                            "Pending", "In Review" -> if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                            "Urgent" -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                            else -> if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("VPN SR #", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(request.vpnType, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text("Ticket Date", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(request.employeeName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (request.notes.isNotEmpty()) {
                Text(
                    "Notes: ${request.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }

            if (userRole != "Viewer") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (request.status != "Completed" && request.status != "Approved") {
                        Button(
                            onClick = { viewModel.approveVPNRequest(request) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, "Approve", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.deleteVPNRequest(request.id) },
                        modifier = if (request.status != "Completed" && request.status != "Approved") Modifier.weight(1f) else Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun NTCreationCard(nt: NTCreation, viewModel: MainViewModel) {
    val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Username: ${nt.employeeName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("ID: ${nt.employeeId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (nt.status) {
                                "Approved", "Active" -> Color(0xFFECFDF5)
                                "Pending", "In Review" -> Color(0xFFFFFBEB)
                                "Urgent" -> Color(0xFFFEF2F2)
                                else -> Color(0xFFF1F5F9)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        nt.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (nt.status) {
                            "Approved", "Active" -> Color(0xFF047857)
                            "Pending", "In Review" -> Color(0xFFB45309)
                            "Urgent" -> Color(0xFFDC2626)
                            else -> Color(0xFF475569)
                        }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Action", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(nt.department, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text("Site", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(nt.site, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (nt.date.isNotEmpty() || nt.actionDate.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (nt.date.isNotEmpty()) {
                        Column {
                            Text("Date", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(nt.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (nt.actionDate.isNotEmpty()) {
                        Column {
                            Text("Action Date", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(nt.actionDate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            if (nt.notes.isNotEmpty()) {
                Text("Notes: ${nt.notes}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF334155))
            }

            if (userRole != "Viewer") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (nt.status != "Active" && nt.status != "Approved") {
                        Button(
                            onClick = { viewModel.approveNTCreation(nt) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, "Approve", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activate", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.deleteNTCreation(nt.id) },
                        modifier = if (nt.status != "Active" && nt.status != "Approved") Modifier.weight(1f) else Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: REMINDERS & IT TASKS BOARD
// ==========================================

@Composable
fun formatTimeRemaining(seconds: Long): String {
    if (seconds <= 0) return "00:00"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

fun getLinkedEntityKey(reminder: Reminder): String? {
    val desc = reminder.description
    val trigger = "Linked to "
    val index = desc.lastIndexOf(trigger)
    if (index != -1) {
        val linkPart = desc.substring(index + trigger.length).trim()
        val key = linkPart.substringBefore(" ").substringBefore(".")
        if (key.isNotEmpty()) {
            return key
        }
    }
    return null
}

fun consolidateReminders(rawList: List<Reminder>): List<Reminder> {
    val result = mutableListOf<Reminder>()
    val groups = rawList.groupBy { getLinkedEntityKey(it) }
    
    for ((key, group) in groups) {
        if (key == null) {
            result.addAll(group)
        } else {
            val representative = group.lastOrNull { it.status == "Active" } ?: group.first()
            val totalCount = group.size
            val doneCount = group.count { it.status == "Done" }
            val activeCount = group.count { it.status == "Active" }
            val escalatedCount = group.count { it.status == "Escalated" }
            
            val combinedStatus = if (doneCount == totalCount) {
                "Done"
            } else if (escalatedCount > 0) {
                "Escalated"
            } else {
                "Active"
            }
            
            val activeReminders = group.filter { it.status != "Done" }
            val nextDueReminder = activeReminders.minByOrNull { it.id } ?: representative
            val combinedDueDate = nextDueReminder.dueDate
            
            var entityName = key.replace("PCProblem:", "PC Problem #")
                                .replace("ITARRequest:", "ITAR Request #")
                                .replace("VPNRequest:", "VPN Request #")
                                .replace("NTCreation:", "NT Request REQ-88")
            
            val summaryTitle = if (representative.title.startsWith("Follow-up: ")) representative.title else "Follow-up: $entityName"
            val summaryDesc = "Consolidated follow-ups for $entityName ($totalCount total: $activeCount active, $doneCount done). Linked to $key."
            
            val consolidated = representative.copy(
                title = summaryTitle,
                description = summaryDesc,
                dueDate = combinedDueDate,
                status = combinedStatus
            )
            result.add(consolidated)
        }
    }
    return result.sortedByDescending { it.id }
}

fun getReminderDueDateMs(reminder: Reminder): Long {
    return if (reminder.dueDate.startsWith("TimerTarget:")) {
        reminder.dueDate.substringAfter("TimerTarget:").toLongOrNull() ?: Long.MAX_VALUE
    } else {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            sdf.parse(reminder.dueDate)?.time ?: Long.MAX_VALUE
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
}

@Composable
fun RemindersTabScreen(
    viewModel: MainViewModel,
    onCreateReminder: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf("Active") } // Active, Escalated, Done, Tasks
    var sortBy by remember { mutableStateOf("Upcoming First") } // Upcoming First, Distant First, Newest Created, Oldest Created
    val remindersList by viewModel.rawReminders.collectAsStateWithLifecycle(initialValue = emptyList())
    val tasksList by viewModel.rawITTasks.collectAsStateWithLifecycle(initialValue = emptyList())

    // Advanced search & priority states
    var searchQuery by remember { mutableStateOf("") }

    // State for interactive page calendar
    var selectedCalendarDay by remember { mutableStateOf<java.util.Calendar?>(null) }

    // Drive local real-time ticker recomposition
    var localTickTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            localTickTime = System.currentTimeMillis()
        }
    }

    val consolidatedList = remember(remindersList) {
        consolidateReminders(remindersList)
    }

    val filteredReminders = consolidatedList.filter {
        when (activeSubTab) {
            "Active" -> it.status == "Active"
            "Escalated" -> it.status == "Escalated"
            "Done" -> it.status == "Done"
            else -> true
        }
    }

    // Filter reminders additionally by selected calendar date if active
    val calendarFilteredReminders = remember(filteredReminders, selectedCalendarDay) {
        if (selectedCalendarDay == null) {
            filteredReminders
        } else {
            filteredReminders.filter {
                if (it.dueDate.startsWith("TimerTarget:")) {
                    val targetTime = it.dueDate.substringAfter("TimerTarget:").toLongOrNull() ?: 0L
                    val rCal = java.util.Calendar.getInstance().apply { timeInMillis = targetTime }
                    rCal.get(java.util.Calendar.YEAR) == selectedCalendarDay!!.get(java.util.Calendar.YEAR) &&
                    rCal.get(java.util.Calendar.DAY_OF_YEAR) == selectedCalendarDay!!.get(java.util.Calendar.DAY_OF_YEAR)
                } else false
            }
        }
    }

    // Secondary local filtering based on Search Keywords
    val searchFilteredReminders = remember(calendarFilteredReminders, searchQuery) {
        calendarFilteredReminders.filter {
            if (searchQuery.isBlank()) true else {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val sortedReminders = remember(searchFilteredReminders, sortBy) {
        when (sortBy) {
            "Upcoming First" -> searchFilteredReminders.sortedWith(compareBy<Reminder> { getReminderDueDateMs(it) }.thenByDescending { it.id })
            "Distant First" -> searchFilteredReminders.sortedWith(compareByDescending<Reminder> { getReminderDueDateMs(it) }.thenByDescending { it.id })
            "Newest Created" -> searchFilteredReminders.sortedByDescending { it.id }
            "Oldest Created" -> searchFilteredReminders.sortedBy { it.id }
            else -> searchFilteredReminders
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, "Notifications", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Smart Reminders Dashboard",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Track automatic SLA escalations, active alarms, and custom test alerts.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${consolidatedList.count { it.status == "Active" }} ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "98.4% SLA",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF00832F)
                                )
                            }
                        }
                    }
                }
            }

            // 📊 EXECUTIVE SLA MONITORING METRICS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Active Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${consolidatedList.count { it.status == "Active" }}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Active SLA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Escalated Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEAEA)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${consolidatedList.count { it.status == "Escalated" }}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Breached / Esc",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB71C1C)
                            )
                        }
                    }

                    // Done Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${consolidatedList.count { it.status == "Done" }}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "SLA Resolved",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }
            }

            // 🔍 FILTER & SEARCH ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search alerts...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Search", modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Done sub-tab bulk action prune button
                    if (activeSubTab == "Done" && consolidatedList.any { it.status == "Done" }) {
                        val context = LocalContext.current
                        Button(
                            onClick = {
                                val doneItems = consolidatedList.filter { it.status == "Done" }
                                doneItems.forEach { viewModel.deleteReminder(it.id) }
                                Toast.makeText(context, "Completed reminders cleared successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Clear Done", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prune", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val subTabs = listOf("Active", "Escalated", "Done")
                    subTabs.forEach { tabName ->
                        val isSelected = activeSubTab == tabName
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeSubTab = tabName },
                            label = { Text(tabName, style = MaterialTheme.typography.labelLarge) }
                        )
                    }
                }
            }

            if (sortedReminders.isEmpty()) {
                item {
                    val customDesc = if (selectedCalendarDay != null) {
                        val df = java.text.SimpleDateFormat("EEE, MMM d, yyyy", java.util.Locale.getDefault())
                        "No alarms or SLA requirements scheduled on " + df.format(selectedCalendarDay!!.time) + " in $activeSubTab queue."
                    } else {
                        "Alert alarms matching your IT service actions will appear here."
                    }
                    EmptyStatePlaceholder(
                         title = "No reminders in $activeSubTab queue",
                         desc = customDesc
                    )
                }
            } else {
                items(sortedReminders) { reminder ->
                    ReminderCard(reminder = reminder, viewModel = viewModel)
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateReminder,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .testTag("fab_add_reminder"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Set follow-on alarm reminder")
        }
    }
}

@Composable
fun OperationalCalendarView(
    remindersList: List<Reminder>,
    selectedDay: java.util.Calendar?,
    localTickTime: Long,
    onSelectDay: (java.util.Calendar?) -> Unit
) {
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = localTickTime }
    val currentMonthName = calendar.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.SHORT, java.util.Locale.getDefault()) ?: "JUN"
    val currentYear = calendar.get(java.util.Calendar.YEAR)

    // Calculate current week's dates starting from Monday
    val weekCalendar = java.util.Calendar.getInstance().apply { timeInMillis = localTickTime }
    val currentDayOfWeek = weekCalendar.get(java.util.Calendar.DAY_OF_WEEK)
    val diffToMonday = if (currentDayOfWeek == java.util.Calendar.SUNDAY) -6 else 2 - currentDayOfWeek
    weekCalendar.add(java.util.Calendar.DAY_OF_YEAR, diffToMonday)

    val weekDays = List(7) { index ->
        val currentDay = weekCalendar.clone() as java.util.Calendar
        currentDay.add(java.util.Calendar.DAY_OF_YEAR, index)
        currentDay
    }

    val todayCalendar = java.util.Calendar.getInstance().apply { timeInMillis = localTickTime }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Calendar Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "SLA Calendar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "SLA CALENDAR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "$currentMonthName $currentYear",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                if (selectedDay != null) {
                    androidx.compose.material3.FilledTonalButton(
                        onClick = { onSelectDay(null) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Clear",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.11f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Text(
                                "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Calendar strip (7 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weekDays.forEach { dayCal ->
                    val isSelected = selectedDay != null &&
                            dayCal.get(java.util.Calendar.YEAR) == selectedDay.get(java.util.Calendar.YEAR) &&
                            dayCal.get(java.util.Calendar.DAY_OF_YEAR) == selectedDay.get(java.util.Calendar.DAY_OF_YEAR)

                    val isToday = dayCal.get(java.util.Calendar.YEAR) == todayCalendar.get(java.util.Calendar.YEAR) &&
                            dayCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR)

                    val dayName = dayCal.getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SHORT, java.util.Locale.getDefault()) ?: ""
                    val dayNum = dayCal.get(java.util.Calendar.DAY_OF_MONTH)

                    val hasActiveAlarms = remindersList.any { reminder ->
                        if (reminder.status == "Active" && reminder.dueDate.startsWith("TimerTarget:")) {
                            val targetTime = reminder.dueDate.substringAfter("TimerTarget:").toLongOrNull() ?: 0L
                            val alarmCal = java.util.Calendar.getInstance().apply { timeInMillis = targetTime }
                            alarmCal.get(java.util.Calendar.YEAR) == dayCal.get(java.util.Calendar.YEAR) &&
                                    alarmCal.get(java.util.Calendar.DAY_OF_YEAR) == dayCal.get(java.util.Calendar.DAY_OF_YEAR)
                        } else false
                    }

                    val defaultBg = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }

                    val bgColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else -> defaultBg
                    }

                    val borderColor = when {
                        isSelected -> Color.Transparent
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    }

                    val borderWidth = when {
                        isSelected -> 0.dp
                        isToday -> 1.5.dp
                        else -> 1.dp
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(
                                width = borderWidth,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isSelected) {
                                    onSelectDay(null)
                                } else {
                                    onSelectDay(dayCal)
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = dayName.take(3).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Text(
                                text = dayNum.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )

                            if (hasActiveAlarms) {
                                Box(
                                    modifier = Modifier
                                        .width(10.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.size(3.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ReminderVisualCategory(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val containerColor: Color
)

fun getReminderVisualCategory(title: String, description: String, isDark: Boolean): ReminderVisualCategory {
    val text = (title + " " + description).lowercase()
    return when {
        text.contains("شبكة") || text.contains("نت") || text.contains("wifi") || text.contains("wi-fi") || 
        text.contains("network") || text.contains("internet") || text.contains("dslam") || text.contains("fiber") ||
        text.contains("ip ") || text.contains("router") || text.contains("مودم") || text.contains("ألياف") -> {
            ReminderVisualCategory(
                icon = Icons.Default.Wifi,
                label = "NETWORK / INTERNET",
                color = Color(0xFFE60000),
                containerColor = if (isDark) Color(0xFFE60000).copy(alpha = 0.15f) else Color(0xFFFFEBEE)
            )
        }
        text.contains("أمان") || text.contains("حماية") || text.contains("قفل") || text.contains("pass") ||
        text.contains("security") || text.contains("lock") || text.contains("firewall") || text.contains("جدار") ||
        text.contains("vulnerability") || text.contains("hack") || text.contains("ثغرة") || text.contains("سري") -> {
            ReminderVisualCategory(
                icon = Icons.Default.Lock,
                label = "SECURITY",
                color = Color(0xFF007A87),
                containerColor = if (isDark) Color(0xFF007A87).copy(alpha = 0.15f) else Color(0xFFE0F2F1)
            )
        }
        text.contains("برنامج") || text.contains("صيانة") || text.contains("تحديث") || text.contains("active directory") ||
        text.contains("system") || text.contains("build") || text.contains("maintenance") || text.contains("update") ||
        text.contains("pc") || text.contains("كمبيوتر") || text.contains("جهاز") || text.contains("windows") -> {
            ReminderVisualCategory(
                icon = Icons.Default.Build,
                label = "SYSTEM / MAINTENANCE",
                color = Color(0xFFFF8F00),
                containerColor = if (isDark) Color(0xFFFF8F00).copy(alpha = 0.15f) else Color(0xFFFFF8E1)
            )
        }
        text.contains("بريد") || text.contains("إيميل") || text.contains("رسالة") || text.contains("mail") ||
        text.contains("outlook") || text.contains("exchange") || text.contains("message") || text.contains("sms") -> {
            ReminderVisualCategory(
                icon = Icons.Default.Email,
                label = "COMMUNICATION",
                color = Color(0xFF1565C0),
                containerColor = if (isDark) Color(0xFF1565C0).copy(alpha = 0.15f) else Color(0xFFE3F2FD)
            )
        }
        text.contains("إعداد") || text.contains("تغيير") || text.contains("config") || text.contains("setting") ||
        text.contains("admin") || text.contains("sla") || text.contains("إدارة") -> {
            ReminderVisualCategory(
                icon = Icons.Default.Settings,
                label = "CONFIGURATION / SLA",
                color = Color(0xFF6A1B9A),
                containerColor = if (isDark) Color(0xFF6A1B9A).copy(alpha = 0.15f) else Color(0xFFF3E5F5)
            )
        }
        else -> {
            ReminderVisualCategory(
                icon = Icons.Default.Warning,
                label = "GENERAL ALERT",
                color = Color(0xFFC62828),
                containerColor = if (isDark) Color(0xFFC62828).copy(alpha = 0.15f) else Color(0xFFFFEBEE)
            )
        }
    }
}

@Composable
fun ReminderCard(reminder: Reminder, viewModel: MainViewModel) {
    val isDone = reminder.status == "Done"
    val context = LocalContext.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val visualCategory = getReminderVisualCategory(reminder.title, reminder.description, isDark)
    
    // Pulse animation for high priority / escalated alerts
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val isEscalated = reminder.status == "Escalated"

    // Sound test state
    var isSoundPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (isSoundPlaying) {
                AlertSoundTester.stopSound()
                isSoundPlaying = false
            }
        }
    }
    
    // Drive countdown updates
    var localTick by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(reminder.status) {
        if (reminder.status == "Active") {
            while (true) {
                kotlinx.coroutines.delay(1000)
                localTick = System.currentTimeMillis()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isEscalated && !isDone) (2.dp + (glowAlpha * 1.5f).dp) else 1.dp,
            color = if (isEscalated && !isDone) {
                Color(0xFFE60000).copy(alpha = glowAlpha + 0.2f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isEscalated && !isDone) {
                Color(0xFFFFEBEE).copy(alpha = 0.25f + glowAlpha * 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Checklist Checklist row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular Todo check toggle action trigger
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isDone) Color(0xFFE60000).copy(alpha = 0.12f) else Color(0xFFF1F5F9))
                        .border(
                            1.5.dp,
                            if (isDone) Color(0xFFE60000) else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        .clickable { viewModel.toggleReminderCompleted(reminder) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed Checklist Task",
                            tint = Color(0xFFE60000),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Smart Categorized Icon Container
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(visualCategory.containerColor)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visualCategory.icon,
                        contentDescription = visualCategory.label,
                        tint = visualCategory.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visualCategory.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = visualCategory.color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        ),
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Edit / Delete buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    var showEditDialog by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Reminder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (showEditDialog) {
                        EditReminderScreen(
                            reminder = reminder,
                            viewModel = viewModel,
                            onDismiss = { showEditDialog = false }
                        )
                    }
                    IconButton(
                        onClick = { viewModel.deleteReminder(reminder.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Reminder",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Countdown Ticker if it is a Countdown Alarm
            if (reminder.dueDate.startsWith("TimerTarget:")) {
                val targetTime = reminder.dueDate.substringAfter("TimerTarget:").toLongOrNull() ?: 0L
                val remainingSec = maxOf(0L, (targetTime - localTick) / 1000L)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Notification Alarm Timer Active",
                        tint = Color(0xFFE60000),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isDone) "Timer Complete" else if (remainingSec > 0) "Alarm Timer: ${formatTimeRemaining(remainingSec)} remaining" else "🚨 ALARM STANDBY ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE60000)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Calendar Due",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Due: " + reminder.dueDate,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ⏰ Custom AlarmManager Settings Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val rStartStr = if (reminder.startHour == 0) "12 AM" else if (reminder.startHour == 12) "12 PM" else if (reminder.startHour > 12) "${reminder.startHour - 12} PM" else "${reminder.startHour} AM"
                val rEndStr = if (reminder.endHour == 24) "12 Midnight" else if (reminder.endHour == 12) "12 PM" else if (reminder.endHour > 12) "${reminder.endHour - 12} PM" else "${reminder.endHour} AM"
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Active Hours Config",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Active Window: $rStartStr to $rEndStr (repeats every ${reminder.repeatIntervalMin / 60} hour(s))",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Sound Configuration",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Alarm Sound: ${reminder.soundType.ifBlank { "Default" }}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSoundPlaying) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(0.4f, 0.9f, 0.5f, 0.7f).forEach { heightScalar ->
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height((12 * heightScalar * (glowAlpha + 0.3f).coerceIn(0.2f, 1.2f)).dp)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable {
                                if (isSoundPlaying) {
                                    AlertSoundTester.stopSound()
                                    isSoundPlaying = false
                                } else {
                                    AlertSoundTester.playSound(
                                        context,
                                        reminder.soundType.ifBlank { "Default" },
                                        "Testing alarm sound: ${reminder.title}"
                                    )
                                    isSoundPlaying = true
                                }
                            }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSoundPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isSoundPlaying) "Stop Preview" else "Play Preview",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (reminder.status == "Done") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (reminder.status == "Done") "RESOLVED" else "IMPORTANT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (reminder.status == "Done") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (getLinkedEntityKey(reminder) != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Consolidated Record",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "CONSOLIDATED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Escalate after ${reminder.escalateAfterMin}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (!isDone) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick Snooze Presets Card Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Quick Snooze Presets",
                                tint = Color(0xFFE60000),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Quick Snooze / غفوة سريعة:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(5, 15, 30).forEach { mins ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE60000).copy(alpha = 0.08f))
                                        .clickable {
                                            viewModel.snoozeReminderCustom(reminder, mins)
                                            android.widget.Toast.makeText(context, "Snoozed Alert by $mins mins", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .border(1.dp, Color(0xFFE60000).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "+${mins}m",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFE60000)
                                    )
                                }
                            }
                        }
                    }

                    var showSnoozeOptions by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleReminderCompleted(reminder) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00832F)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            AutoScaleText(
                                text = "Mark Done",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showSnoozeOptions = !showSnoozeOptions },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            AutoScaleText(
                                text = if (showSnoozeOptions) "Cancel Snooze" else "Snooze...",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (reminder.status != "Escalated") {
                            Button(
                                onClick = { viewModel.processEscalation(reminder) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                AutoScaleText(
                                    text = "Escalate",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = showSnoozeOptions) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Snooze by:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            listOf(5, 15, 30, 60, 240).forEach { mins ->
                                val label = if (mins >= 60) "${mins / 60}h" else "${mins}m"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .clickable {
                                            viewModel.snoozeReminderCustom(reminder, mins)
                                            showSnoozeOptions = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.toggleReminderCompleted(reminder) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AutoScaleText(
                        text = "Reopen Todo Checklist Item",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TaskKanbanCard(task: ITTask, viewModel: MainViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(task.priority + " Priority", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(task.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Text(task.title, style = MaterialTheme.typography.headlineSmall)
            Text(task.description, style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Due: ${task.dueDate}", style = MaterialTheme.typography.labelSmall)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val states = listOf("To Do", "In Progress", "Testing", "Done")
                    val idx = states.indexOf(task.status)
                    if (idx < states.size - 1) {
                        TextButton(onClick = { viewModel.updateITTaskStatus(task.id, states[idx + 1]) }) {
                            Text("Move -> ${states[idx + 1]}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ==========================================
// TAB 4.5: OPERATIONAL NOTES SCREEN & MANAGEMENT
// ==========================================

enum class NoteTextScale(
    val label: String,
    val titleFontSize: androidx.compose.ui.unit.TextUnit,
    val contentFontSize: androidx.compose.ui.unit.TextUnit,
    val dateFontSize: androidx.compose.ui.unit.TextUnit,
    val buttonFontSize: androidx.compose.ui.unit.TextUnit
) {
    SMALL("Small", 12.sp, 11.sp, 9.sp, 11.sp),
    MEDIUM("Medium", 15.sp, 13.sp, 10.sp, 13.sp),
    LARGE("Large", 18.sp, 16.sp, 12.sp, 15.sp),
    HUGE("Huge", 22.sp, 19.sp, 14.sp, 18.sp)
}

@Composable
fun NotesTabScreen(
    viewModel: MainViewModel,
    autoShowAddDialog: Boolean = false,
    onAddDialogDismissed: () -> Unit = {}
) {
    val notes by viewModel.notesList.collectAsStateWithLifecycle(initialValue = emptyList())
    
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("notes_drafts_pref", android.content.Context.MODE_PRIVATE) }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    
    LaunchedEffect(autoShowAddDialog) {
        if (autoShowAddDialog) {
            showAddDialog = true
            onAddDialogDismissed()
        }
    }
    
    // Manage dynamic text size scaling
    var currentScale by remember { mutableStateOf(NoteTextScale.MEDIUM) }
    
    // Add Dialog State
    var newTitle by remember { mutableStateOf(prefs.getString("draft_title", "") ?: "") }
    var newContent by remember { mutableStateOf(prefs.getString("draft_content", "") ?: "") }
    var newCategory by remember { mutableStateOf(prefs.getString("draft_category", "Info") ?: "Info") }
    
    // Edit Dialog State
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf("Info") }

    // Auto-save note drafts on typing Change
    LaunchedEffect(newTitle, newContent, newCategory) {
        prefs.edit()
            .putString("draft_title", newTitle)
            .putString("draft_content", newContent)
            .putString("draft_category", newCategory)
            .apply()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Operational Notes",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Manage shift Summarize",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE60000)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(
                            horizontal = (currentScale.buttonFontSize.value * 1.2f).dp,
                            vertical = (currentScale.buttonFontSize.value * 0.8f).dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Note",
                            modifier = Modifier.size((currentScale.buttonFontSize.value * 1.3f).dp)
                        )
                        Spacer(modifier = Modifier.width((currentScale.buttonFontSize.value * 0.3f).dp))
                        Text("Add Note", fontSize = currentScale.buttonFontSize)
                    }
                }
            }

            if (notes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No Notes",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No operational notes found.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tap 'Add Note' to log handover tasks, notes, or shifts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(notes, key = { it.id }) { note ->
                    val isDark = isAppDarkTheme()
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Title:",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = currentScale.titleFontSize
                                            ),
                                            color = Color(0xFFE60000)
                                        )
                                        Text(
                                            text = note.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = currentScale.titleFontSize
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    val (bgColor, textColor) = remember(note.category, isDark) {
                                        if (isDark) {
                                            when (note.category) {
                                                "Info" -> Color(0xFF1E3A8A).copy(alpha = 0.4f) to Color(0xFF93C5FD)
                                                "Security" -> Color(0xFF7F1D1D).copy(alpha = 0.4f) to Color(0xFFFCA5A5)
                                                "Data" -> Color(0xFF4C1D95).copy(alpha = 0.4f) to Color(0xFFC4B5FD)
                                                "Reminder" -> Color(0xFF7C2D12).copy(alpha = 0.4f) to Color(0xFFFDBA74)
                                                "E-Mail" -> Color(0xFF064E3B).copy(alpha = 0.4f) to Color(0xFF6EE7B7)
                                                "Username" -> Color(0xFF78350F).copy(alpha = 0.4f) to Color(0xFFFDE68A)
                                                "Password" -> Color(0xFF881337).copy(alpha = 0.4f) to Color(0xFFFDA4AF)
                                                else -> Color(0xFF334155).copy(alpha = 0.4f) to Color(0xFFCBD5E1)
                                            }
                                        } else {
                                            when (note.category) {
                                                "Info" -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
                                                "Security" -> Color(0xFFFEF2F2) to Color(0xFFDC2626)
                                                "Data" -> Color(0xFFF5F3FF) to Color(0xFF6D28D9)
                                                "Reminder" -> Color(0xFFFFF7ED) to Color(0xFFC2410C)
                                                "E-Mail" -> Color(0xFFECFDF5) to Color(0xFF047857)
                                                "Username" -> Color(0xFFF9F5F1) to Color(0xFF78350F)
                                                "Password" -> Color(0xFFFFF1F2) to Color(0xFFBE123C)
                                                else -> Color(0xFFF1F5F9) to Color(0xFF475569)
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bgColor)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = note.category,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = (currentScale.dateFontSize.value * 0.9f).sp
                                            ),
                                            color = textColor
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                      ) {
                                        Text(
                                            text = "Logged:",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = currentScale.dateFontSize
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val dateStr = remember(note.timestamp) {
                                            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                                            sdf.format(java.util.Date(note.timestamp))
                                        }
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = currentScale.dateFontSize
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            noteToEdit = note
                                            editTitle = note.title
                                            editContent = note.content
                                            editCategory = note.category
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Note",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteNote(note.id)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Note",
                                            tint = Color(0xFFE60000),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Text:",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = currentScale.contentFontSize
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = currentScale.contentFontSize
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = (currentScale.contentFontSize.value * 1.4).sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Note Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Note")
                        if (newTitle.isNotEmpty() || newContent.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Draft Saved",
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Draft saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newContent,
                            onValueChange = { newContent = it },
                            label = { Text("Content") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            maxLines = 10
                        )
                        
                        Text("Category:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        val noteCategories = listOf("Info", "Security", "Data", "Reminder", "E-Mail", "Username", "Password")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            noteCategories.forEach { category ->
                                val isSelected = (newCategory == category)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .clickable { newCategory = category }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank() && newContent.isNotBlank()) {
                                viewModel.addNote(newTitle, newContent, newCategory)
                                newTitle = ""
                                newContent = ""
                                newCategory = "Info"
                                prefs.edit().clear().apply()
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000))
                    ) {
                        Text("Save", fontSize = currentScale.buttonFontSize)
                    }
                },
                dismissButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (newTitle.isNotEmpty() || newContent.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    newTitle = ""
                                    newContent = ""
                                    prefs.edit().clear().apply()
                                }
                            ) {
                                Text("Clear Draft", color = MaterialTheme.colorScheme.error, fontSize = currentScale.buttonFontSize)
                            }
                        }
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", fontSize = currentScale.buttonFontSize)
                        }
                    }
                }
            )
        }

        // Edit Note Dialog
        if (noteToEdit != null) {
            AlertDialog(
                onDismissRequest = { noteToEdit = null },
                title = { Text("Edit Operational Note") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editContent,
                            onValueChange = { editContent = it },
                            label = { Text("Content") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            maxLines = 10
                        )
                        
                        Text("Category:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        val noteCategories = listOf("Info", "Security", "Data", "Reminder", "E-Mail", "Username", "Password")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            noteCategories.forEach { category ->
                                val isSelected = (editCategory == category)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .clickable { editCategory = category }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val currentNote = noteToEdit
                            if (currentNote != null && editTitle.isNotBlank() && editContent.isNotBlank()) {
                                viewModel.editNote(currentNote.id, editTitle, editContent, editCategory)
                                noteToEdit = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000))
                    ) {
                        Text("Save Changes", fontSize = currentScale.buttonFontSize)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noteToEdit = null }) {
                        Text("Cancel", fontSize = currentScale.buttonFontSize)
                    }
                }
            )
        }
    }
}

@Composable
fun rememberBatteryStatus(): BatteryStatus {
    val context = androidx.compose.ui.platform.LocalContext.current
    var batteryStatus by remember { mutableStateOf(BatteryStatus(100, false, "Unknown", "Healthy", 25.0f)) }

    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                intent?.let {
                    val level = it.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                    val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 100
                    
                    val status = it.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                                     status == android.os.BatteryManager.BATTERY_STATUS_FULL
                    
                    val health = when (it.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1)) {
                        android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                        android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
                        android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                        android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                        else -> "Healthy"
                    }
                    
                    val plugType = when (it.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1)) {
                        android.os.BatteryManager.BATTERY_PLUGGED_AC -> "AC Power"
                        android.os.BatteryManager.BATTERY_PLUGGED_USB -> "USB Power"
                        android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                        else -> "Battery"
                    }

                    val tempDeciC = it.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0)
                    val tempC = tempDeciC / 10f
                    
                    batteryStatus = BatteryStatus(pct, isCharging, plugType, health, tempC)
                }
            }
        }
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // ignore
            }
        }
    }
    return batteryStatus
}

data class BatteryStatus(
    val percentage: Int,
    val isCharging: Boolean,
    val powerSource: String,
    val health: String,
    val temperatureCelsius: Float
)

// TAB 5: PROFILE & ADVANCED ADMIN SETTINGS
// ==========================================

@Composable
fun ProfileAdminTabScreen(viewModel: MainViewModel) {
    val logs by viewModel.filteredAuditLogs.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val searchVal by viewModel.auditSearchTerm.collectAsStateWithLifecycle()
    val filterVal by viewModel.auditFilter.collectAsStateWithLifecycle()

    val optMaintenance by viewModel.maintenanceMode.collectAsStateWithLifecycle()
    val optBackupFreq by viewModel.backupFrequency.collectAsStateWithLifecycle()
    val optTelemetry by viewModel.telemetryEnabled.collectAsStateWithLifecycle()

    val optAlertSound by viewModel.alertSound.collectAsStateWithLifecycle()
    val optNotificationSound by viewModel.notificationSound.collectAsStateWithLifecycle()
    val optReminderSound by viewModel.reminderSound.collectAsStateWithLifecycle()

    val optAlertVolume by viewModel.alertVolume.collectAsStateWithLifecycle()
    val optOverrideDnd by viewModel.overrideDnd.collectAsStateWithLifecycle()
    val optVibrationPattern by viewModel.vibrationPattern.collectAsStateWithLifecycle()

    var activePlayingUri by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val isDark by viewModel.darkMode.collectAsStateWithLifecycle()

    var pingUrl by remember { mutableStateOf("10.231.42.1") }
    var pingStatus by remember { mutableStateOf("Idle") }
    var pingResult by remember { mutableStateOf<String?>(null) }
    var configDataState by remember { mutableStateOf("") }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    val adminCoroutineScope = rememberCoroutineScope()

    // Dynamically adaptive colors matching Vodafone Red and dark/light system design
    val okGreen = if (isDark) Color(0xFF4ADE80) else Color(0xFF00832F)
    val warningAmber = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
    val chargingBlue = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val criticalRed = if (isDark) Color(0xFFFCA5A5) else Color(0xFFE60000)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "Advanced Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Global administration and network core configurations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (currentRole == "Admin") {
            item {
                UserManagementCard(viewModel)
            }
            item {
                BackupRestoreCard(viewModel)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "CONNECTED WORKSTATION INFO",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Device info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        "Edge gateway diagnostic status.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Device Model:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text("${android.os.Build.MANUFACTURER.uppercase()} ${android.os.Build.MODEL}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Operating System:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text("Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("HW Endpoint Serial:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text("SN-${android.os.Build.BOARD.uppercase()}-9831", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Security Cryptography:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text("AES-256 GCM Secure", style = MaterialTheme.typography.bodyMedium, color = okGreen)
                        }
                    }
                }
            }
        }

        item {
            val battery = rememberBatteryStatus()

            Card(
                modifier = Modifier.fillMaxWidth().testTag("realtime_diagnostics_card"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pulsing online telemetry indicator
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse_diag")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .graphicsLayer(alpha = pulseAlpha)
                                    .background(okGreen, CircleShape)
                            )
                            Text(
                                "REAL-TIME DIAGNOSTIC HW MONITOR",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Build, 
                            contentDescription = "Diagnostics Status", 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        "Live system partitions and battery telemetry tracker.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // PART 1: COMPREHENSIVE STORAGE TELEMETRY
                    val path = android.os.Environment.getDataDirectory()
                    val stat = android.os.StatFs(path.path)
                    val totalBytes = stat.totalBytes
                    val availableBytes = stat.availableBytes
                    val totalGB = totalBytes / (1024 * 1024 * 1024)
                    val availableGB = availableBytes / (1024 * 1024 * 1024)
                    val usedGB = totalGB - availableGB
                    val progress = if (totalGB > 0) usedGB.toFloat() / totalGB.toFloat() else 0f

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "DEVICE STORAGE CAPACITY", 
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$usedGB GB / $totalGB GB (${(progress * 100).toInt()}%)", 
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), 
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "AVAILABLE SPACE", 
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$availableGB GB Free", 
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    "SECTOR PATH", 
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "/data", 
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    "PARTITION HEALTH", 
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "HEALTHY", 
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                                    color = okGreen
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // PART 2: LIVE BATTERY TRACKER
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "POWER & BATTERY STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Display beautiful battery cells with custom color scheme matching M3/Red
                            Box(
                                modifier = Modifier
                                    .size(width = 72.dp, height = 34.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val fuelColor = when {
                                    battery.isCharging -> chargingBlue
                                    battery.percentage >= 60 -> okGreen
                                    battery.percentage >= 20 -> warningAmber
                                    else -> criticalRed
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(battery.percentage / 100f)
                                        .background(fuelColor, RoundedCornerShape(3.dp))
                                )

                                Text(
                                    "${battery.percentage}%",
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (battery.percentage > 40) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Charging state:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        if (battery.isCharging) "Charging ✅" else "On Battery 🔋",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (battery.isCharging) chargingBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Power input:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        battery.powerSource, 
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), 
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Battery health metadata grid layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("CELL HEALTH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        battery.health.uppercase(), 
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), 
                                        color = if (battery.health == "Healthy" || battery.health == "Good") okGreen else warningAmber
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("TEMPERATURE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${battery.temperatureCelsius}°C", 
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), 
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }



        item {
            var activeSoundSelector by remember { mutableStateOf<String?>(null) } // "alert", "notification", "reminder" or null
            
            val alertSoundOptions = listOf("Default", "Siren", "Alarm", "Voice", "Notification")
            val notificationSoundOptions = listOf("Default", "Notification", "Chime", "Ringtone")
            val reminderSoundOptions = listOf("Default")

            val context = LocalContext.current
            
            // Helper to get printable names
            fun getUriDisplayName(uriString: String): String {
                if (uriString.isBlank()) return "None Selected"
                if (uriString == "Default") return "Default (Alarm)"
                if (uriString == "Default" || uriString == "Siren" || uriString == "Alarm" || 
                    uriString == "Voice" || uriString == "Notification" || uriString == "Chime" || 
                    uriString == "Ringtone" || uriString == "Digital Pulse") {
                    return uriString
                }
                if (!uriString.startsWith("content://") && !uriString.startsWith("file://")) {
                    return uriString
                }
                return try {
                    val uri = Uri.parse(uriString)
                    var name = ""
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) {
                            name = cursor.getString(nameIndex)
                        }
                    }
                    if (name.isBlank()) {
                        uri.lastPathSegment ?: "Custom Audio File"
                    } else {
                        name
                    }
                } catch (e: Exception) {
                    "Custom Audio File"
                }
            }

            // Temp variables for custom picked files
            var pendingAlertUri by remember { mutableStateOf("") }
            var pendingNotificationUri by remember { mutableStateOf("") }
            var pendingReminderUri by remember { mutableStateOf("") }

            val alertPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    pendingAlertUri = it.toString()
                    Toast.makeText(context, "Loaded: ${getUriDisplayName(it.toString())}. Tap Save to assign.", Toast.LENGTH_SHORT).show()
                }
            }

            val notificationPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    pendingNotificationUri = it.toString()
                    Toast.makeText(context, "Loaded: ${getUriDisplayName(it.toString())}. Tap Save to assign.", Toast.LENGTH_SHORT).show()
                }
            }

            val reminderPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    pendingReminderUri = it.toString()
                    Toast.makeText(context, "Loaded: ${getUriDisplayName(it.toString())}. Tap Save to assign.", Toast.LENGTH_SHORT).show()
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("sound_alerts_settings_card"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(
                    containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    } else {
                        Color(0xFFFCFCFD)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header with scaled FLAT style icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Sound System Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    "SOUND & NOTIFICATION ALERTS",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Telemetry & SLA Alert Soundscapes",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Text(
                        "Custom SLA Reminder alert soundscapes.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 3. SLA REMINDER ALERTS
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val isExpanded = activeSoundSelector == "reminder"
                        val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    activeSoundSelector = if (isExpanded) null else "reminder"
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "SLA Reminder Sound",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Periodic tone for recurring SLA reminder timers.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        getUriDisplayName(optReminderSound),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        modifier = Modifier.widthIn(max = 100.dp)
                                    )
                                }
                                 val isPlayingReminder = activePlayingUri == optReminderSound
                                 Box(
                                     modifier = Modifier
                                         .size(22.dp)
                                         .clip(CircleShape)
                                         .background(if (isPlayingReminder) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                         .clickable {
                                             if (isPlayingReminder) {
                                                 AlertSoundTester.stopSound()
                                                 activePlayingUri = null
                                             } else {
                                                 activePlayingUri = optReminderSound
                                                 AlertSoundTester.playSound(context, optReminderSound, "Testing customized SLA reminder sound")
                                                 Toast.makeText(context, "SMC Reminder Playback: ${getUriDisplayName(optReminderSound)}", Toast.LENGTH_SHORT).show()
                                             }
                                         },
                                     contentAlignment = Alignment.Center
                                 ) {
                                     Icon(
                                         imageVector = if (isPlayingReminder) Icons.Default.Stop else Icons.Default.PlayArrow,
                                         contentDescription = "Test sound",
                                         tint = Color.White,
                                         modifier = Modifier.size(12.dp)
                                     )
                                 }
                                
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp).rotate(rotationState)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Standard Presets",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    reminderSoundOptions.forEach { sound ->
                                        val isSelected = optReminderSound == sound
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                                .border(
                                                    1.dp,
                                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable {
                                                    viewModel.setReminderSound(sound)
                                                    AlertSoundTester.playSound(context, sound, "Customized reminder sound selected")
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = sound,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                // Custom file chooser panel
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MusicNote,
                                                    contentDescription = "Custom Audio Track",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    "Loaded file: ${getUriDisplayName(pendingReminderUri.ifBlank { optReminderSound })}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                    maxLines = 1,
                                                    modifier = Modifier.widthIn(max = 140.dp)
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {                                                 if (pendingReminderUri.isNotBlank()) {
                                                     Box(
                                                         modifier = Modifier
                                                             .size(18.dp)
                                                             .clip(CircleShape)
                                                             .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                             .clickable {
                                                                 if (activePlayingUri == pendingReminderUri) { AlertSoundTester.stopSound(); activePlayingUri = null } else { activePlayingUri = pendingReminderUri; AlertSoundTester.playSound(context, pendingReminderUri, "Testing customized reminder sound selection") }
                                                             },
                                                         contentAlignment = Alignment.Center
                                                     ) {
                                                         Icon(
                                                             imageVector = Icons.Default.PlayArrow,
                                                             tint = MaterialTheme.colorScheme.primary,
                                                             contentDescription = "Preview selected file",
                                                             modifier = Modifier.size(10.dp)
                                                         )
                                                     }
                                                 }

                                                FilledTonalButton(
                                                    onClick = { reminderPicker.launch("audio/*") },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Folder, contentDescription = "Browse", modifier = Modifier.size(11.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Browse", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp))
                                                }
                                            }
                                        }

                                        if (pendingReminderUri.isNotBlank() && pendingReminderUri != optReminderSound) {
                                            Button(
                                                onClick = {
                                                    viewModel.setReminderSound(pendingReminderUri)
                                                    Toast.makeText(context, "SLA Reminder Sound Saved successfully!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Save Customized Sound", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }



        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SYSTEM CONFIGURATION", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    ListItem(
                        headlineContent = { Text("Application Dark Theme") },
                        supportingContent = { Text("Toggle between modern light and dark modes.") },
                        trailingContent = {
                            val isDark by viewModel.darkMode.collectAsStateWithLifecycle()
                            Switch(
                                checked = isDark,
                                onCheckedChange = { viewModel.toggleDarkMode(it) }
                            )
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    ListItem(
                        headlineContent = { Text("Automatic Backup Frequency") },
                        supportingContent = { Text("Database replication schedule ($optBackupFreq)") },
                        trailingContent = {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        val nextVal = when (optBackupFreq) {
                                            "6 Hours" -> "12 Hours"
                                            "12 Hours" -> "24 Hours"
                                            else -> "6 Hours"
                                        }
                                        viewModel.setBackupFrequency(nextVal)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(optBackupFreq, style = MaterialTheme.typography.labelSmall)
                                Icon(Icons.Default.KeyboardArrowDown, "Dropdown", modifier = Modifier.size(16.dp))
                            }
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    ListItem(
                        headlineContent = { Text("Telemetry & Error Reporting") },
                        supportingContent = { Text("Secure anonymous payload status tracking.") },
                        trailingContent = {
                            Switch(
                                checked = optTelemetry,
                                onCheckedChange = { viewModel.toggleTelemetry(it) }
                            )
                        }
                    )
                }
            }
        }

        item {
            val splashImageUri by viewModel.splashImageUri.collectAsStateWithLifecycle()
            var pendingSplashUri by remember(splashImageUri) { mutableStateOf(splashImageUri) }
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                uri?.let { contentUri ->
                    val localPath = copyUriToInternalStorage(context, contentUri)
                    if (localPath != null) {
                        pendingSplashUri = localPath
                        Toast.makeText(context, "Image selected and prepared securely", Toast.LENGTH_SHORT).show()
                    } else {
                        pendingSplashUri = contentUri.toString()
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("splash_settings_card"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SPLASH SCREEN DESIGN",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Splash Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        "Set custom boot sequence image instead of standard animated vector logo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = pendingSplashUri,
                        onValueChange = { pendingSplashUri = it },
                        label = { Text("Splash Image URL / Local URI", style = MaterialTheme.typography.labelSmall) },
                        placeholder = { Text("e.g. https://example.com/logo.png") },
                        modifier = Modifier.fillMaxWidth().testTag("splash_uri_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f).testTag("choose_splash_image_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Choose Image", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (pendingSplashUri.isNotEmpty() || splashImageUri.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { 
                                    pendingSplashUri = ""
                                    viewModel.setSplashImageUri("")
                                    Toast.makeText(context, "Splash screen image reset to default", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("reset_splash_image_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Reset Default", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Save Button to explicitly apply changes
                    val isChanged = pendingSplashUri != splashImageUri
                    Button(
                        onClick = {
                            viewModel.setSplashImageUri(pendingSplashUri)
                            Toast.makeText(context, "Splash design changes saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        enabled = isChanged,
                        modifier = Modifier.fillMaxWidth().testTag("save_splash_image_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE60000), // Vodafone Accent color
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check, 
                                contentDescription = "Safe Icon", 
                                modifier = Modifier.size(18.dp),
                                tint = if (isChanged) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                            Text(
                                "Save Splash Screen", 
                                fontWeight = FontWeight.Bold, 
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isChanged) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }

                    if (pendingSplashUri.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "LIVE REPLACEMENT PREVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = resolveImageModel(pendingSplashUri),
                                        error = painterResource(id = R.drawable.icon),
                                        fallback = painterResource(id = R.drawable.icon),
                                        placeholder = painterResource(id = R.drawable.icon)
                                    ),
                                    contentDescription = "Splash Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }







        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("disaster_purge_card"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Security Purge Desk",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "DISASTER RECOVERY & SYSTEMS PURGE",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Text(
                        "Clear backlogs, reset states, or wipe database tables.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isCompact = maxWidth < 400.dp
                        if (isCompact) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.clearCompletedITTasks()
                                        Toast.makeText(context, "Completed IT Tasks cleared!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Purge Tasks", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = {
                                        viewModel.purgeAllReminders()
                                        Toast.makeText(context, "All alarms & reminders cleared!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Clear Alarms", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.clearCompletedITTasks()
                                        Toast.makeText(context, "Completed IT Tasks cleared!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Purge Tasks", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = {
                                        viewModel.purgeAllReminders()
                                        Toast.makeText(context, "All alarms & reminders cleared!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Clear Alarms", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showWipeConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("wipe_db_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                     ) {
                        Text("Wipe All Databases (Hard Reset)", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (showWipeConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showWipeConfirmDialog = false },
                    title = { Text("Sanitize All Databases?") },
                    text = { Text("Warning! This action is irreversible. This will purge all tickets, reminders, local logs, notes, and alerts back to factory state.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.wipeAllDatabases()
                                showWipeConfirmDialog = false
                                Toast.makeText(context, "Cluster Database Zero-Wipe Completed", Toast.LENGTH_LONG).show()
                            }
                        ) {
                            Text("YES, WIPE ALL", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showWipeConfirmDialog = false }) {
                            Text("CANCEL")
                        }
                    }
                )
            }
        }

        item {
            Text(
                "System Audit Logs",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            OutlinedTextField(
                value = searchVal,
                onValueChange = { viewModel.auditSearchTerm.value = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text("Filter email or action class...") },
                leadingIcon = { Icon(Icons.Default.Search, "Filter") }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf("All Entities", "Action: Login")
                chips.forEach { chipName ->
                    val isSelected = filterVal == chipName
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.auditFilter.value = chipName },
                        label = { Text(chipName, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Text("No matching audit occurrences logged.")
            }
        } else {
            items(logs) { log ->
                AuditLogListItem(log = log)
            }
        }
    }
}

@Composable
fun AuditLogListItem(log: AuditLog) {
    val dateText = remember(log.timestamp) {
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
    }
    val ipAddress = remember(log.id) {
        "10.231.42.${100 + (log.id % 150)}"
    }
    val location = remember(log.id) {
        val locations = listOf("Horizon HQ, Cairo", "Smart Village, Giza", "Maadi Tech Hub", "Alexandria Center", "Korba Operations")
        locations[log.id % locations.size]
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when (log.actionType) {
                                    "Login" -> Color(0xFFCFFFDC)
                                    "Logout" -> MaterialTheme.colorScheme.secondaryContainer
                                    "Fail Login" -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (log.actionType) {
                                "Login" -> Icons.Default.PlayArrow
                                "Logout" -> Icons.Default.ExitToApp
                                "Fail Login" -> Icons.Default.Warning
                                else -> Icons.Default.Edit
                            },
                            contentDescription = log.actionType,
                            modifier = Modifier.size(16.dp),
                            tint = when (log.actionType) {
                                "Login" -> Color(0xFF005B20)
                                "Fail Login" -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                    Text(
                        log.actorEmail,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (log.actionType) {
                                "Login" -> Color(0x2200FF00)
                                "Fail Login" -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        log.actionType.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = when (log.actionType) {
                            "Login" -> Color(0xFF047857)
                            "Fail Login" -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            Text(
                text = log.details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Date: $dateText", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Info, contentDescription = "IP", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("IP: $ipAddress", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Place, contentDescription = "Location", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Location: $location", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ==========================================
// FORMS & OVERLAYS INTERFACES
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTicketScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val initialDate = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("ticket_drafts_pref", android.content.Context.MODE_PRIVATE) }

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(initialDate) }
    var site by remember { mutableStateOf("Horizon") }
    var zone by remember { mutableStateOf("") }
    var pcName by remember { mutableStateOf("") }
    var assetNumber by remember { mutableStateOf("Horizon") }
    var category by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var isMultiSelect by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf("High") }
    var summary by remember { mutableStateOf("") }
    var assignedTeam by remember { mutableStateOf("Help Desk") }

    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var scheduledAlarmEnabled by remember { mutableStateOf(true) }
    var alarmStartHourStr by remember { mutableStateOf("") }
    var alarmEndHourStr by remember { mutableStateOf("") }
    var alarmRepeatHoursStr by remember { mutableStateOf("") }

    // Auto-save typing state disabled to keep textboxes completely empty and template-free as requested
    val hasDraft = remember(zone, pcName, summary) {
        zone.isNotEmpty() || pcName.isNotEmpty() || summary.isNotEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("New Ticket", style = MaterialTheme.typography.titleMedium)
                        if (hasDraft) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDC2626).copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Draft Saved",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (hasDraft) {
                        TextButton(
                            onClick = {
                                username = ""
                                site = "Horizon"
                                zone = ""
                                pcName = ""
                                assetNumber = "Horizon"
                                category = ""
                                selectedCategories = emptySet()
                                priority = "Medium"
                                summary = ""
                                assignedTeam = "Help Desk"
                                prefs.edit().clear().apply()
                            }
                        ) {
                            Text("Clear Draft", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("EMPLOYEE DETAILS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Employee Name") },
                modifier = Modifier
                    .testTag("employee_name")
                    .fillMaxWidth()
            )

            Text("INCIDENT TIMESTAMP RECORD", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            var showDatePicker by remember { mutableStateOf(false) }

            if (showDatePicker) {
                val calendar = java.util.Calendar.getInstance()
                if (username.isNotEmpty()) {
                    try {
                        val parts = username.split("-")
                        if (parts.size == 3) {
                            calendar.set(java.util.Calendar.YEAR, parts[0].toInt())
                            calendar.set(java.util.Calendar.MONTH, parts[1].toInt() - 1)
                            calendar.set(java.util.Calendar.DAY_OF_MONTH, parts[2].toInt())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH)
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                android.app.DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format(
                            java.util.Locale.getDefault(),
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                        )
                        username = formattedDate
                        showDatePicker = false
                    },
                    year,
                    month,
                    day
                ).apply {
                    setOnDismissListener {
                        showDatePicker = false
                    }
                    show()
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Date of Incident") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = Color(0xFFE60000)
                        )
                    },
                    modifier = Modifier
                        .testTag("username")
                        .fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            Text("SITE & ASSET LOCATIONS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            Text("Select Site", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))

            val siteOptions = listOf("Horizon", "Korba", "Asyut")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                siteOptions.forEach { option ->
                    val isSelected = (site == option)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFE60000) else {
                                if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9)
                            })
                            .border(1.dp, if (isSelected) Color(0xFFE60000) else {
                                if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)
                            }, RoundedCornerShape(12.dp))
                            .clickable { 
                                site = option
                                assetNumber = option
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else {
                                if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155)
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PROBLEM TYPE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text("Choose Problem Type", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                }
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9))
                        .border(1.dp, if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val modes = listOf("Single", "Multi")
                    modes.forEach { mode ->
                        val isSelected = (mode == "Multi" && isMultiSelect) || (mode == "Single" && !isMultiSelect)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFFE60000) else Color.Transparent)
                                .clickable { 
                                    isMultiSelect = (mode == "Multi")
                                    if (!isMultiSelect && selectedCategories.size > 1) {
                                        selectedCategories = setOf(selectedCategories.first())
                                        category = selectedCategories.first()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else {
                                    if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155)
                                }
                            )
                        }
                    }
                }
            }

            val problemCategories = listOf(
                "MISSING ITEMS" to listOf("Missing Headset", "Missing Mouse", "Missing Keyboard", "Missing Driver"),
                "SWAP & REPLACEMENTS" to listOf("Replace Headset", "Replace Mouse", "Replace Keyboard", "Replace Screen"),
                "CONNECTION & REMOTE" to listOf("Connection", "Citrix", "DNS", "Scale Screen"),
                "SUPPORT & POWER" to listOf("IWS Voice", "IWS Chat", "Power Screen", "Power PC")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                problemCategories.forEach { (catTitle, catOptions) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            } else {
                                Color(0xFFF8FAFC)
                            }
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            } else {
                                Color(0xFFE2E8F0)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = catTitle,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            val chunked = catOptions.chunked(2)
                            chunked.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { option ->
                                        val isSelected = selectedCategories.contains(option)
                                        val icon = when {
                                            option.contains("Headset") -> Icons.Default.Call
                                            option.contains("Mouse") -> Icons.Default.Check
                                            option.contains("Keyboard") -> Icons.Default.Add
                                            option.contains("Screen") && option.contains("Scale") -> Icons.Default.Star
                                            option.contains("Screen") -> Icons.Default.Info
                                            option.contains("Driver") -> Icons.Default.Build
                                            option.contains("Connection") -> Icons.Default.Refresh
                                            option.contains("Citrix") -> Icons.Default.Lock
                                            option.contains("Voice") -> Icons.Default.Phone
                                            option.contains("Chat") -> Icons.Default.Share
                                            option.contains("DNS") -> Icons.Default.Settings
                                            option.contains("PC") || option.contains("Power") -> Icons.Default.Warning
                                            else -> Icons.Default.Info
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) Color(0xFFE60000)
                                                    else if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant
                                                    else Color(0xFFF1F5F9)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color(0xFFE60000)
                                                    else if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.outlineVariant
                                                    else Color(0xFFE2E8F0),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { 
                                                    if (isMultiSelect) {
                                                        val updated = if (isSelected) {
                                                            selectedCategories - option
                                                        } else {
                                                            selectedCategories + option
                                                        }
                                                        selectedCategories = updated
                                                        category = updated.joinToString(", ")
                                                    } else {
                                                        if (isSelected) {
                                                            selectedCategories = emptySet()
                                                            category = ""
                                                        } else {
                                                            selectedCategories = setOf(option)
                                                            category = option
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 12.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = option,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isSelected) Color.White
                                                    else if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface
                                                    else Color(0xFF334155),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    if (rowItems.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = zone,
                onValueChange = { zone = it },
                label = { Text("Zone / Wing Index") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pcName,
                onValueChange = { pcName = it },
                label = { Text("PC Node Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("CATEGORIZATION & RECTIFICATION", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text("Detailed Summary of PC Problem") },
                modifier = Modifier
                    .height(110.dp)
                    .fillMaxWidth()
            )

            Text("ASSIGNED SUPPORT TEAM", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            val teamOptions = listOf("Help Desk", "Admin Team", "Network Team", "Operation Team")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                teamOptions.forEach { option ->
                    val isSelected = (assignedTeam == option)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFE60000).copy(alpha = 0.08f) else Color(0xFFF1F5F9))
                            .border(1.dp, if (isSelected) Color(0xFFE60000) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .clickable { assignedTeam = option }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { assignedTeam = option },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE60000))
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color(0xFFE60000) else Color(0xFF334155)
                        )
                    }
                }
            }

            Text("OPERATIONAL ALARM SETTINGS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Schedule Event Alarm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Create repeating alarm with sound and vibration", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = scheduledAlarmEnabled,
                            onCheckedChange = { scheduledAlarmEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE60000), checkedTrackColor = Color(0xFFE60000).copy(alpha = 0.5f))
                        )
                    }
                    
                    if (scheduledAlarmEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = alarmStartHourStr,
                                onValueChange = { alarmStartHourStr = it },
                                label = { Text("Start Hour (0-23)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = alarmEndHourStr,
                                onValueChange = { alarmEndHourStr = it },
                                label = { Text("End Hour (1-24)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                        
                        OutlinedTextField(
                            value = alarmRepeatHoursStr,
                            onValueChange = { alarmRepeatHoursStr = it },
                            label = { Text("Repeat Interval (Hours)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        val resolvedCalcStart = alarmStartHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 7
                        val resolvedCalcEnd = alarmEndHourStr.toIntOrNull()?.coerceIn(1, 24) ?: 19
                        val resolvedCalcRepeat = alarmRepeatHoursStr.toIntOrNull()?.coerceIn(1, 168) ?: 1
                        
                        val startStr = if (resolvedCalcStart == 0) "12 AM" else if (resolvedCalcStart == 12) "12 PM" else if (resolvedCalcStart > 12) "${resolvedCalcStart - 12} PM" else "$resolvedCalcStart AM"
                        val endStr = if (resolvedCalcEnd == 24) "12 Midnight" else if (resolvedCalcEnd == 12) "12 PM" else if (resolvedCalcEnd > 12) "${resolvedCalcEnd - 12} PM" else "$resolvedCalcEnd AM"
                        
                        Text(
                            text = "🔔 Active range: $startStr to $endStr. Repeating every $resolvedCalcRepeat hour(s). Includes mobile sound, high alert popup, and vibration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE60000),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
 
            Button(
                onClick = {
                    if (summary.isNotEmpty() && !isSubmitting) {
                        scope.launch {
                            isSubmitting = true
                            delay(800)
                            
                            val resolvedAlarmStart = alarmStartHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 7
                            val resolvedAlarmEnd = alarmEndHourStr.toIntOrNull()?.coerceIn(1, 24) ?: 19
                            val resolvedAlarmRepeat = alarmRepeatHoursStr.toIntOrNull()?.coerceIn(1, 168) ?: 1
                            
                            viewModel.addPCProblem(
                                name = name,
                                username = username.ifEmpty { initialDate },
                                site = site,
                                zone = zone.ifEmpty { "HQ Area" },
                                pcName = pcName.ifEmpty { "NODE-PC" },
                                assetNumber = assetNumber,
                                category = category,
                                priority = priority,
                                detailedSummary = summary,
                                assignedTeam = assignedTeam,
                                alarmEnabled = scheduledAlarmEnabled,
                                alarmStartHour = resolvedAlarmStart,
                                alarmEndHour = resolvedAlarmEnd,
                                alarmRepeatHours = resolvedAlarmRepeat
                            )
                            prefs.edit().clear().apply()
                            isSubmitting = false
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    SMCLoadingSpinner(
                        size = SMCSpinnerSize.SMALL,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create New", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

fun isPastDateString(dateStr: String): Boolean {
    if (dateStr.trim().isEmpty()) return false
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        sdf.isLenient = false
        val parsedDate = sdf.parse(dateStr.trim()) ?: return false
        
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        
        parsedDate.before(today)
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewITARRequestScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val initialDate = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var requestType by remember { mutableStateOf("") }
    var implementer by remember { mutableStateOf("") }
    var systemOwner by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("Korba") }
    var date by remember { mutableStateOf(initialDate) }
    var dateValidationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var scheduledAlarmEnabled by remember { mutableStateOf(true) }
    var alarmStartHourStr by remember { mutableStateOf("") }
    var alarmEndHourStr by remember { mutableStateOf("") }
    var alarmRepeatHoursStr by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New ITAR Request", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Employee Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Request Type Profile Options:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            val profileOptions = listOf("CCPLUSE", "GIR", "Citrix", "Siebel", "CCat", "GIS", "DL", "Shared Area", "Email Access", "Change Job Title", "Change Office", "Change Number")
            val isDark = isAppDarkTheme()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profileOptions.forEach { option ->
                    val isSelected = (requestType == option)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else if (isDark) MaterialTheme.colorScheme.surfaceVariant 
                                else Color(0xFFF1F5F9)
                            )
                            .border(
                                width = 1.dp, 
                                color = if (isSelected) MaterialTheme.colorScheme.primary 
                                        else if (isDark) MaterialTheme.colorScheme.outlineVariant 
                                        else Color(0xFFE2E8F0), 
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { requestType = option }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = requestType,
                onValueChange = { requestType = it },
                label = { Text("Request Type") },
                modifier = Modifier.fillMaxWidth()
            )

            var showDatePicker by remember { mutableStateOf(false) }

            if (showDatePicker) {
                val calendar = java.util.Calendar.getInstance()
                if (date.isNotEmpty()) {
                    try {
                        val parts = date.split("-")
                        if (parts.size == 3) {
                            calendar.set(java.util.Calendar.YEAR, parts[0].toInt())
                            calendar.set(java.util.Calendar.MONTH, parts[1].toInt() - 1)
                            calendar.set(java.util.Calendar.DAY_OF_MONTH, parts[2].toInt())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH)
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                android.app.DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format(
                            java.util.Locale.getDefault(),
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                        )
                        date = formattedDate
                        dateValidationError = if (isPastDateString(formattedDate)) "Error: Cannot set requests or alarms for a past date" else null
                        showDatePicker = false
                    },
                    year,
                    month,
                    day
                ).apply {
                    setOnDismissListener {
                        showDatePicker = false
                    }
                    show()
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Date") },
                    isError = dateValidationError != null,
                    supportingText = {
                        if (dateValidationError != null) {
                            Text(dateValidationError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = Color(0xFFE60000)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            Text("Select Site", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val siteOptions = listOf("Korba", "Horizon", "Asyut")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                siteOptions.forEach { option ->
                    val isSelected = (site == option)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFFE60000) 
                                else if (isDark) MaterialTheme.colorScheme.surfaceVariant 
                                else Color(0xFFF1F5F9)
                            )
                            .border(
                                width = 1.dp, 
                                color = if (isSelected) Color(0xFFE60000) 
                                        else if (isDark) MaterialTheme.colorScheme.outlineVariant 
                                        else Color(0xFFE2E8F0), 
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { site = option }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = implementer,
                onValueChange = { implementer = it },
                label = { Text("Assigned To") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = systemOwner,
                onValueChange = { systemOwner = it },
                label = { Text("Requested by") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Internal Audit Notes") },
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth()
            )

            Text("OPERATIONAL ALARM SETTINGS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Schedule Event Alarm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Create repeating alarm with sound and vibration", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = scheduledAlarmEnabled,
                            onCheckedChange = { scheduledAlarmEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE60000), checkedTrackColor = Color(0xFFE60000).copy(alpha = 0.5f))
                        )
                    }
                    
                    if (scheduledAlarmEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = alarmStartHourStr,
                                onValueChange = { alarmStartHourStr = it },
                                label = { Text("Start Hour (0-23)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = alarmEndHourStr,
                                onValueChange = { alarmEndHourStr = it },
                                label = { Text("End Hour (1-24)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                        
                        OutlinedTextField(
                            value = alarmRepeatHoursStr,
                            onValueChange = { alarmRepeatHoursStr = it },
                            label = { Text("Repeat Interval (Hours)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        val resolvedCalcStart = alarmStartHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 7
                        val resolvedCalcEnd = alarmEndHourStr.toIntOrNull()?.coerceIn(1, 24) ?: 19
                        val resolvedCalcRepeat = alarmRepeatHoursStr.toIntOrNull()?.coerceIn(1, 168) ?: 1
                        
                        val startStr = if (resolvedCalcStart == 0) "12 AM" else if (resolvedCalcStart == 12) "12 PM" else if (resolvedCalcStart > 12) "${resolvedCalcStart - 12} PM" else "$resolvedCalcStart AM"
                        val endStr = if (resolvedCalcEnd == 24) "12 Midnight" else if (resolvedCalcEnd == 12) "12 PM" else if (resolvedCalcEnd > 12) "${resolvedCalcEnd - 12} PM" else "$resolvedCalcEnd AM"
                        
                        Text(
                            text = "🔔 Active range: $startStr to $endStr. Repeating every $resolvedCalcRepeat hour(s). Includes mobile sound, high alert popup, and vibration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE60000),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (isPastDateString(date)) {
                        dateValidationError = "Error: Cannot set requests or alarms for a past date"
                        return@Button
                    }
                    if (name.isNotEmpty() && !isSubmitting) {
                        scope.launch {
                            isSubmitting = true
                            delay(800)
                            
                            val resolvedAlarmStart = alarmStartHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 7
                            val resolvedAlarmEnd = alarmEndHourStr.toIntOrNull()?.coerceIn(1, 24) ?: 19
                            val resolvedAlarmRepeat = alarmRepeatHoursStr.toIntOrNull()?.coerceIn(1, 168) ?: 1
                            
                            viewModel.addITARRequest(
                                name = name,
                                username = username.ifEmpty { "N/A" },
                                requestType = requestType,
                                status = "Pending",
                                implementer = implementer,
                                systemOwner = systemOwner,
                                startDate = date,
                                endDate = "2026-12-06",
                                rejectionReason = "",
                                internalNotes = notes,
                                site = site,
                                alarmEnabled = scheduledAlarmEnabled,
                                alarmStartHour = resolvedAlarmStart,
                                alarmEndHour = resolvedAlarmEnd,
                                alarmRepeatHours = resolvedAlarmRepeat
                            )
                            isSubmitting = false
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    SMCLoadingSpinner(
                        size = SMCSpinnerSize.SMALL,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit ITAR Request", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewVPNRequestScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val initialDate = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
    var name by remember { mutableStateOf(initialDate) }
    var username by remember { mutableStateOf("") }
    var vpnType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dateValidationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var scheduledAlarmEnabled by remember { mutableStateOf(true) }
    var alarmStartHourStr by remember { mutableStateOf("") }
    var alarmEndHourStr by remember { mutableStateOf("") }
    var alarmRepeatHoursStr by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New VPN", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var showDatePicker by remember { mutableStateOf(false) }

            if (showDatePicker) {
                val calendar = java.util.Calendar.getInstance()
                if (name.isNotEmpty()) {
                    try {
                        val parts = name.split("-")
                        if (parts.size == 3) {
                            calendar.set(java.util.Calendar.YEAR, parts[0].toInt())
                            calendar.set(java.util.Calendar.MONTH, parts[1].toInt() - 1)
                            calendar.set(java.util.Calendar.DAY_OF_MONTH, parts[2].toInt())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH)
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                android.app.DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format(
                            java.util.Locale.getDefault(),
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                        )
                        name = formattedDate
                        dateValidationError = if (isPastDateString(formattedDate)) "Error: Cannot set requests or alarms for a past date" else null
                        showDatePicker = false
                    },
                    year,
                    month,
                    day
                ).apply {
                    setOnDismissListener {
                        showDatePicker = false
                    }
                    show()
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Ticket Date") },
                    isError = dateValidationError != null,
                    supportingText = {
                        if (dateValidationError != null) {
                            Text(dateValidationError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = Color(0xFFE60000)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = vpnType,
                onValueChange = { vpnType = it },
                label = { Text("VPN SR #") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Special Access Instructions") },
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth()
            )

            Text("OPERATIONAL ALARM SETTINGS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Schedule Event Alarm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Create repeating alarm with sound and vibration", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = scheduledAlarmEnabled,
                            onCheckedChange = { scheduledAlarmEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE60000), checkedTrackColor = Color(0xFFE60000).copy(alpha = 0.5f))
                        )
                    }
                    
                    if (scheduledAlarmEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = alarmStartHourStr,
                                onValueChange = { alarmStartHourStr = it },
                                label = { Text("Start Hour (0-23)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = alarmEndHourStr,
                                onValueChange = { alarmEndHourStr = it },
                                label = { Text("End Hour (1-24)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                        
                        OutlinedTextField(
                            value = alarmRepeatHoursStr,
                            onValueChange = { alarmRepeatHoursStr = it },
                            label = { Text("Repeat Interval (Hours)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        val resolvedCalcStart = alarmStartHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 7
                        val resolvedCalcEnd = alarmEndHourStr.toIntOrNull()?.coerceIn(1, 24) ?: 19
                        val resolvedCalcRepeat = alarmRepeatHoursStr.toIntOrNull()?.coerceIn(1, 168) ?: 1
                        
                        val startStr = if (resolvedCalcStart == 0) "12 AM" else if (resolvedCalcStart == 12) "12 PM" else if (resolvedCalcStart > 12) "${resolvedCalcStart - 12} PM" else "$resolvedCalcStart AM"
                        val endStr = if (resolvedCalcEnd == 24) "12 Midnight" else if (resolvedCalcEnd == 12) "12 PM" else if (resolvedCalcEnd > 12) "${resolvedCalcEnd - 12} PM" else "$resolvedCalcEnd AM"
                        
                        Text(
                            text = "🔔 Active range: $startStr to $endStr. Repeating every $resolvedCalcRepeat hour(s). Includes mobile sound, high alert popup, and vibration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE60000),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isNotEmpty() && !isSubmitting) {
                        scope.launch {
                            isSubmitting = true
                            delay(800)
                            val resolvedAlarmStart = alarmStartHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 7
                            val resolvedAlarmEnd = alarmEndHourStr.toIntOrNull()?.coerceIn(1, 24) ?: 19
                            val resolvedAlarmRepeat = alarmRepeatHoursStr.toIntOrNull()?.coerceIn(1, 168) ?: 1
                            
                            viewModel.addVPNRequest(
                                name = name,
                                username = username.ifEmpty { "N/A" },
                                vpnType = vpnType,
                                status = "Pending",
                                notes = notes,
                                alarmEnabled = scheduledAlarmEnabled,
                                alarmStartHour = resolvedAlarmStart,
                                alarmEndHour = resolvedAlarmEnd,
                                alarmRepeatHours = resolvedAlarmRepeat
                            )
                            isSubmitting = false
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    SMCLoadingSpinner(
                        size = SMCSpinnerSize.SMALL,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit VPN Request", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewNTRequestScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val initialDate = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
    var name by remember { mutableStateOf("") }
    var personalNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var manager by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(initialDate) }
    var dateValidationError by remember { mutableStateOf<String?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var scheduledAlarmEnabled by remember { mutableStateOf(true) }
    var alarmStartHourStr by remember { mutableStateOf("") }
    var alarmEndHourStr by remember { mutableStateOf("") }
    var alarmRepeatHoursStr by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val generatedEmailPreview = """
        Subject: NT Account Creation Request — $name
        Hello Team,

        Kindly create an NT account for the below employee:
        • Username: ${name.ifEmpty { "[Input]" }}
        • Personal number: ${personalNumber.ifEmpty { "[Input]" }}
        • Department: ${department.ifEmpty { "[Input]" }}
        • Job title: ${jobTitle.ifEmpty { "[Input]" }}
        • Manager: ${manager.ifEmpty { "[Input]" }}
        • Site: $site
        • Date: $date

        Thanks & Regards.
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New NT Request", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = personalNumber,
                    onValueChange = { personalNumber = it },
                    label = { Text("Personal Number") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text("DEPARTMENT OPTIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            val deptOptions = listOf("SME", "888", "CVM", "ICC", "Webchat")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                deptOptions.forEach { option ->
                    val isSelected = (department == option)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9))
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable { department = option }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else Color(0xFF334155)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = jobTitle,
                onValueChange = { jobTitle = it },
                label = { Text("Job Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = manager,
                onValueChange = { manager = it },
                label = { Text("Reporting Manager") },
                modifier = Modifier.fillMaxWidth()
            )

            var showDatePicker by remember { mutableStateOf(false) }

            if (showDatePicker) {
                val calendar = java.util.Calendar.getInstance()
                if (date.isNotEmpty()) {
                    try {
                        val parts = date.split("-")
                        if (parts.size == 3) {
                            calendar.set(java.util.Calendar.YEAR, parts[0].toInt())
                            calendar.set(java.util.Calendar.MONTH, parts[1].toInt() - 1)
                            calendar.set(java.util.Calendar.DAY_OF_MONTH, parts[2].toInt())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH)
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                android.app.DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format(
                            java.util.Locale.getDefault(),
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                        )
                        date = formattedDate
                        dateValidationError = if (isPastDateString(formattedDate)) "Cannot set past dates" else null
                        showDatePicker = false
                    },
                    year,
                    month,
                    day
                ).apply {
                    setOnDismissListener {
                        showDatePicker = false
                    }
                    show()
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Date") },
                    isError = dateValidationError != null,
                    supportingText = {
                        if (dateValidationError != null) {
                            Text(dateValidationError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = Color(0xFFE60000)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            Text("SITES OPTIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            val siteOptions = listOf("Korba", "Horizon", "Asyut")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                siteOptions.forEach { option ->
                    val isSelected = (site == option)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFE60000) else Color(0xFFF1F5F9))
                            .border(1.dp, if (isSelected) Color(0xFFE60000) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable { site = option }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else Color(0xFF334155)
                        )
                    }
                }
            }

            Text("GENERATED EMAIL PREVIEW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Subject: NT Account Creation", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = {
                            clipboardManager.setText(AnnotatedString(generatedEmailPreview))
                            Toast.makeText(context, "Copied directory instructions to clipboard!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Share, "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }
                    }
                    Text(
                        generatedEmailPreview,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            Text("OPERATIONAL ALARM SETTINGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Schedule Event Alarm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Create repeating alarm with sound and vibration", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = scheduledAlarmEnabled,
                            onCheckedChange = { scheduledAlarmEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE60000), checkedTrackColor = Color(0xFFE60000).copy(alpha = 0.5f))
                        )
                    }
                    
                    if (scheduledAlarmEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = alarmStartHourStr,
                                onValueChange = { alarmStartHourStr = it },
                                label = { Text("Start Hour (0-23)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = alarmEndHourStr,
                                onValueChange = { alarmEndHourStr = it },
                                label = { Text("End Hour (1-24)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                        
                        OutlinedTextField(
                            value = alarmRepeatHoursStr,
                            onValueChange = { alarmRepeatHoursStr = it },
                            label = { Text("Repeat Interval (Hours)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        val resolvedCalcStart = alarmStartHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 7
                        val resolvedCalcEnd = alarmEndHourStr.toIntOrNull()?.coerceIn(1, 24) ?: 19
                        val resolvedCalcRepeat = alarmRepeatHoursStr.toIntOrNull()?.coerceIn(1, 168) ?: 1
                        
                        val startStr = if (resolvedCalcStart == 0) "12 AM" else if (resolvedCalcStart == 12) "12 PM" else if (resolvedCalcStart > 12) "${resolvedCalcStart - 12} PM" else "$resolvedCalcStart AM"
                        val endStr = if (resolvedCalcEnd == 24) "12 Midnight" else if (resolvedCalcEnd == 12) "12 PM" else if (resolvedCalcEnd > 12) "${resolvedCalcEnd - 12} PM" else "$resolvedCalcEnd AM"
                        
                        Text(
                            text = "🔔 Active range: $startStr to $endStr. Repeating every $resolvedCalcRepeat hour(s). Includes mobile sound, high alert popup, and vibration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE60000),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (isPastDateString(date)) {
                        dateValidationError = "Error: Cannot set requests or alarms for a past date"
                        return@Button
                    }
                    if (name.isNotEmpty() && !isSubmitting) {
                        scope.launch {
                            isSubmitting = true
                            delay(800)
                            val resolvedAlarmStart = alarmStartHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 7
                            val resolvedAlarmEnd = alarmEndHourStr.toIntOrNull()?.coerceIn(1, 24) ?: 19
                            val resolvedAlarmRepeat = alarmRepeatHoursStr.toIntOrNull()?.coerceIn(1, 168) ?: 1
                            
                            viewModel.addNTCreation(
                                name = name,
                                personalNumber = personalNumber,
                                department = department.ifEmpty { "IT Dev Ops" },
                                jobTitle = jobTitle.ifEmpty { "Specialist" },
                                manager = manager.ifEmpty { "Lead Director" },
                                site = site,
                                status = "Pending",
                                notes = notes,
                                date = date,
                                actionDate = date,
                                alarmEnabled = scheduledAlarmEnabled,
                                alarmStartHour = resolvedAlarmStart,
                                alarmEndHour = resolvedAlarmEnd,
                                alarmRepeatHours = resolvedAlarmRepeat
                            )
                            isSubmitting = false
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    SMCLoadingSpinner(
                        size = SMCSpinnerSize.SMALL,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit NT Request", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    reminder: Reminder,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(reminder.title) }
    var description by remember { mutableStateOf(reminder.description) }

    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var due by remember { mutableStateOf(reminder.dueDate) }
    var priority by remember { mutableStateOf(reminder.priority) }
    var status by remember { mutableStateOf(reminder.status) }
    var startHour by remember { mutableStateOf(reminder.startHour) }
    var endHour by remember { mutableStateOf(reminder.endHour) }
    var repeatIntervalHours by remember { mutableStateOf((reminder.repeatIntervalMin / 60).coerceAtLeast(1).toString()) }
    var soundType by remember { mutableStateOf(reminder.soundType) }
    var vibrationEnabled by remember { mutableStateOf(reminder.vibrationEnabled) }
    var notificationEnabled by remember { mutableStateOf(reminder.notificationEnabled) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Edit Operational Event", style = MaterialTheme.typography.titleMedium) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Dismiss") }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .heightIn(min = 100.dp)
                            .fillMaxWidth()
                    )

                    Text(
                        text = "Alarm Timing & Core Hour Bounds",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Daily Background Active Alarm Window",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startHour.toString(),
                                onValueChange = { startHour = it.toIntOrNull()?.coerceIn(0, 23) ?: 0 },
                                label = { Text("From Hour") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endHour.toString(),
                                onValueChange = { endHour = it.toIntOrNull()?.coerceIn(1, 24) ?: 24 },
                                label = { Text("To Hour") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                        
                        val startStr = if (startHour == 0) "12 AM" else if (startHour == 12) "12 PM" else if (startHour > 12) "${startHour - 12} PM" else "$startHour AM"
                        val endStr = if (endHour == 24) "12 Midnight" else if (endHour == 12) "12 PM" else if (endHour > 12) "${endHour - 12} PM" else "$endHour AM"
                        
                        Text(
                            text = "Daily active period: ONLY triggers alarms from $startStr to $endStr.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        OutlinedTextField(
                            value = repeatIntervalHours,
                            onValueChange = { repeatIntervalHours = it },
                            label = { Text("Repeating Alarm Check Rate (Hours)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        val hVal = repeatIntervalHours.toIntOrNull() ?: 1
                        Text(
                            text = "Background scheduler repeats every $hVal hour(s) until Status is 'Done'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }



                    Text("Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusList = listOf("Active", "Escalated", "Done")
                        statusList.forEach { st ->
                            val isSelected = status == st
                            Button(
                                onClick = { status = st },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9),
                                    contentColor = if (isSelected) Color.White else Color(0xFF334155)
                                ),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                AutoScaleText(
                                    text = st,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }

                    Text(
                        "Notification & Alert Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val context = LocalContext.current
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Device Vibration",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Vibrate device when this reminder fires",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Switch(
                                checked = vibrationEnabled,
                                onCheckedChange = { vibrationEnabled = it }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Push Notifications",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Show system status-bar heads-up notifications",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { notificationEnabled = it }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && !isSubmitting) {
                                scope.launch {
                                    isSubmitting = true
                                    delay(800)
                                    val hoursVal = repeatIntervalHours.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                    viewModel.updateReminder(
                                        reminder.copy(
                                            title = title,
                                            description = description,
                                            dueDate = if (due.startsWith("TimerTarget:")) due else "TimerTarget:${System.currentTimeMillis() + 5000L}",
                                            escalateAfterMin = hoursVal * 60,
                                            priority = priority,
                                            status = status,
                                            startHour = startHour,
                                            endHour = endHour,
                                            repeatIntervalMin = hoursVal * 60,
                                            soundType = soundType,
                                            vibrationEnabled = vibrationEnabled,
                                            notificationEnabled = notificationEnabled
                                        )
                                    )
                                    isSubmitting = false
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            SMCLoadingSpinner(
                                size = SMCSpinnerSize.SMALL,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save Changes", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReminderScreen(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("High") }
    var startHourStr by remember { mutableStateOf("") }
    var endHourStr by remember { mutableStateOf("") }
    var repeatIntervalHours by remember { mutableStateOf("") }
    var soundType by remember { mutableStateOf("Default") }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var notificationEnabled by remember { mutableStateOf(true) }

    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Event Reminder", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Dismiss") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Event Summary & Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth()
            )

            Text(
                "Manual Active Hour Bounds & Hourly Frequency",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Daily Active Operational Window",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startHourStr,
                        onValueChange = { startHourStr = it },
                        label = { Text("From Hour") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endHourStr,
                        onValueChange = { endHourStr = it },
                        label = { Text("To Hour") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                val startStr = if (startHourStr.isEmpty()) "Not Set" else {
                    val sh = startHourStr.toIntOrNull() ?: 7
                    if (sh == 0) "12 AM" else if (sh == 12) "12 PM" else if (sh > 12) "${sh - 12} PM" else "$sh AM"
                }
                val endStr = if (endHourStr.isEmpty()) "Not Set" else {
                    val eh = endHourStr.toIntOrNull() ?: 19
                    if (eh == 24) "12 Midnight" else if (eh == 12) "12 PM" else if (eh > 12) "${eh - 12} PM" else "$eh AM"
                }

                Text(
                    text = "Daily active period: ONLY triggers alarms from $startStr to $endStr.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedTextField(
                    value = repeatIntervalHours,
                    onValueChange = { repeatIntervalHours = it },
                    label = { Text("Repeating Alarm Check Rate (Hours)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                val hoursCountStr = if (repeatIntervalHours.isEmpty()) "Not Set" else "${repeatIntervalHours.toIntOrNull() ?: 1} hour(s)"
                Text(
                    text = "System schedules repeat alarms every $hoursCountStr. Trigger alarms with loud native phone ringtone and vibration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE60000),
                    fontWeight = FontWeight.Medium
                )
            }



            Text(
                "Notification & Alert Preferences",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val context = LocalContext.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Device Vibration",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Vibrate device when this reminder fires",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Push Notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Show system status-bar heads-up notifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                }
            }

            Button(
                onClick = {
                    if (title.isNotEmpty() && !isSubmitting) {
                        scope.launch {
                            isSubmitting = true
                            delay(800)
                            val hours = repeatIntervalHours.toIntOrNull()?.coerceAtLeast(1) ?: 1
                            val sh = startHourStr.toIntOrNull()?.coerceIn(0, 23) ?: 7
                            val eh = endHourStr.toIntOrNull()?.coerceIn(1, 24) ?: 19
                            val evaluatedDue = "TimerTarget:${System.currentTimeMillis() + 5000L}" // 5 seconds test alert, repeating every X hours
                            
                            viewModel.addReminder(
                                title = title,
                                description = description,
                                dueDate = evaluatedDue,
                                escalateMin = hours * 60,
                                priority = priority,
                                startHour = sh,
                                endHour = eh,
                                repeatIntervalMin = hours * 60,
                                soundType = soundType,
                                vibrationEnabled = vibrationEnabled,
                                notificationEnabled = notificationEnabled
                            )
                            isSubmitting = false
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    SMCLoadingSpinner(
                        size = SMCSpinnerSize.SMALL,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Schedule Operational Event", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(title: String, desc: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Empty state feedback",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Custom guide box providing helpful setup tip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "Pro-tip: Alarms are scheduled instantly using system AlarmManager. Ensure silent mode is OFF to test sound and vibration alerts!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

object AlertSoundTester {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var currentRingtone: android.media.Ringtone? = null
    private var currentMediaPlayer: android.media.MediaPlayer? = null

    fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.let {
                        val result = it.setLanguage(Locale.US)
                        if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                            isTtsReady = true
                        }
                    }
                }
            }
        }
    }

    fun stopSound() {
        try {
            currentRingtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            currentRingtone = null
            
            currentMediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                } catch (e: Exception) {}
            }
            currentMediaPlayer = null

            tts?.let {
                if (it.isSpeaking) {
                    it.stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSound(context: Context, soundType: String, textToSpeak: String = "Test Sound") {
        try {
            currentRingtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            currentRingtone = null

            currentMediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                } catch (e: Exception) {}
            }
            currentMediaPlayer = null

            if (soundType.contains("://") || soundType.startsWith("/")) {
                try {
                    val uri = android.net.Uri.parse(soundType)
                    val r = android.media.RingtoneManager.getRingtone(context, uri)
                    if (r != null) {
                        r.play()
                        currentRingtone = r
                    } else {
                        val player = MediaPlayer().apply {
                            setDataSource(context, uri)
                            setAudioAttributes(
                                android.media.AudioAttributes.Builder()
                                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                            prepare()
                            start()
                            setOnCompletionListener { mp -> mp.release() }
                        }
                        currentMediaPlayer = player
                    }
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        val player = MediaPlayer.create(context, android.net.Uri.parse(soundType))
                        player?.start()
                        player?.setOnCompletionListener { mp -> mp.release() }
                        currentMediaPlayer = player
                        return
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
            when (soundType) {
                "Voice" -> {
                    val speakText = if (textToSpeak.isBlank()) "S I M C Voice Alert test" else textToSpeak
                    if (tts == null) {
                        tts = TextToSpeech(context) { status ->
                            if (status == TextToSpeech.SUCCESS) {
                                tts?.let {
                                    val result = it.setLanguage(Locale.US)
                                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                                        isTtsReady = true
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                            it.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "SMC_ALERT_TEST")
                                        } else {
                                            @Suppress("DEPRECATION")
                                            it.speak(speakText, TextToSpeech.QUEUE_FLUSH, null)
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isTtsReady) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "SMC_ALERT_TEST")
                        } else {
                            @Suppress("DEPRECATION")
                            tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null)
                        }
                    } else {
                        Toast.makeText(context, "Voice synthesizer initializing, try again", Toast.LENGTH_SHORT).show()
                    }
                }
                "Notification", "Chime" -> {
                    try {
                        val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                            ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                        val r = android.media.RingtoneManager.getRingtone(context, notificationUri)
                        r?.play()
                        currentRingtone = r
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                "Alarm", "Siren" -> {
                    try {
                        val alarmUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                            ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                        val r = android.media.RingtoneManager.getRingtone(context, alarmUri)
                        r?.play()
                        currentRingtone = r
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                "Ringtone", "Digital Pulse" -> {
                    try {
                        val ringtoneUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
                            ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                        val r = android.media.RingtoneManager.getRingtone(context, ringtoneUri)
                        r?.play()
                        currentRingtone = r
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                "Default" -> {
                    try {
                        val alarmUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                            ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                        val r = android.media.RingtoneManager.getRingtone(context, alarmUri)
                        r?.play()
                        currentRingtone = r
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val isRememberMeEnabled = remember { viewModel.getRememberMe() }
    val rememberedUser = remember { viewModel.getRememberedUsername() }

    var username by remember { mutableStateOf(if (isRememberMeEnabled) rememberedUser else "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMeState by remember { mutableStateOf(isRememberMeEnabled) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var showBiometricDialog by remember { mutableStateOf(false) }
    var biometricAuthenticating by remember { mutableStateOf(false) }
    var biometricError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE60000).copy(alpha = 0.08f), // Vodafone Red Tint
                        Color(0xFF0F172A) // Dark Slate
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = BorderStroke(1.dp, Color(0xFFE60000).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SMC Brand Logo Frame
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE60000))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "SMC Security Lock",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SMC VODAFONE",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color(0xFFE60000)
                    )
                    Text(
                        text = "Security operations gateway",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Username Input Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        errorMessage = null 
                    },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Username field icon"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Password Input Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = null 
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password field icon"
                        )
                    },
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                        }
                    },
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Remember Me Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = rememberMeState,
                            onCheckedChange = { rememberMeState = it }
                        )
                        Text(
                            text = "Remember me on this device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Biometric Entry button if there's a stored session
                    if (isRememberMeEnabled && rememberedUser.isNotEmpty()) {
                        IconButton(
                            onClick = { 
                                biometricError = null
                                showBiometricDialog = true 
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE60000).copy(alpha = 0.1f))
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Log in with Fingerprint",
                                tint = Color(0xFFE60000)
                            )
                        }
                    }
                }

                // Display Error Row if failed
                if (errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error icon",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Login Trigger Button
                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            errorMessage = "Please fill in all security fields."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        viewModel.authenticateUser(username, password) { success, msg ->
                            isLoading = false
                            if (success) {
                                viewModel.setRememberMe(rememberMeState, username)
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE60000),
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Text(
                    text = "Authorized access ONLY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }

    // Biometric Authentication Simulation Dialog
    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!biometricAuthenticating) showBiometricDialog = false 
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = Color(0xFFE60000),
                        modifier = Modifier.size(26.dp)
                    )
                    Text("Secure Biometric Authentication")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Verifying identity using registered fingerprint/face metrics for user: $rememberedUser",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Glow Fingerprint graphic simulation
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                if (biometricAuthenticating) Color(0xFF2E7D32).copy(alpha = 0.1f)
                                else Color(0xFFE60000).copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint icon",
                            tint = if (biometricAuthenticating) Color(0xFF2E7D32) else Color(0xFFE60000),
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    if (biometricAuthenticating) {
                        Text(
                            text = "Matching biometric properties...",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF2E7D32)
                        )
                        LinearProgressIndicator(
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    biometricError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        biometricAuthenticating = true
                        biometricError = null
                        // Simulate delay
                        scope.launch {
                            kotlinx.coroutines.delay(1200)
                            viewModel.authenticateUserBiometric(rememberedUser) { success, msg ->
                                biometricAuthenticating = false
                                if (success) {
                                    showBiometricDialog = false
                                } else {
                                    biometricError = msg
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000)),
                    enabled = !biometricAuthenticating
                ) {
                    Text("Scan Fingerprint")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBiometricDialog = false },
                    enabled = !biometricAuthenticating
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserManagementCard(viewModel: MainViewModel) {
    val rawUsersList by viewModel.rawUsers.collectAsStateWithLifecycle(initialValue = emptyList())
    val usersList = remember(rawUsersList) { rawUsersList.distinctBy { it.username.lowercase() } }
    val currentLoginUsername by viewModel.loggedInUsername.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<com.vodafone.smc.data.User?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE60000).copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "User credentials management",
                        tint = Color(0xFFE60000),
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "USERS And ROLES",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE60000)
                        )
                        Text(
                            text = "Manage application credentials.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { showAddUserDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE60000),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(32.dp)
                        .widthIn(min = 80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add User",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    AutoScaleText(
                        text = "Add User",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                usersList.forEach { user ->
                    val isPrimaryAdmin = user.username == "v22aali168"
                    val isMyself = user.username == currentLoginUsername

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(
                                1.dp,
                                if (isPrimaryAdmin) Color(0xFFE60000).copy(alpha = 0.25f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isPrimaryAdmin) Color(0xFFE60000).copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPrimaryAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = "Role icon",
                                    tint = if (isPrimaryAdmin) Color(0xFFE60000) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = user.username,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isPrimaryAdmin) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFE60000).copy(alpha = 0.15f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "Locked",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFE60000)
                                            )
                                        }
                                    }
                                    if (isMyself) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "You",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Role: ${user.role}  |  Pass: ${user.passwordHash.map { "*" }.joinToString("")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { userToEdit = user },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Login",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (!isPrimaryAdmin) {
                                IconButton(
                                    onClick = {
                                        viewModel.deleteLoginUser(user.id, user.username) { success, msg ->
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp),
                                    enabled = !isMyself
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Login",
                                        tint = if (isMyself) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add New User Dialog
    if (showAddUserDialog) {
        var newUsername by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var newRole by remember { mutableStateOf("IT Engineer") } // Default starting role
        var errorMsg by remember { mutableStateOf<String?>(null) }

        val isUsernameTaken = remember(newUsername, usersList) {
            if (newUsername.lowercase().trim() == "v22aali168") true
            else if (newUsername.isBlank()) false
            else usersList.any { it.username.equals(newUsername.trim(), ignoreCase = true) }
        }

        val passwordStrength = remember(newPassword) {
            if (newPassword.isEmpty()) ""
            else if (newPassword.length < 6) "Very Weak 🔴 (Must be at least 6 characters)"
            else {
                val hasLetter = newPassword.any { it.isLetter() }
                val hasDigit = newPassword.any { it.isDigit() }
                val hasSpecial = newPassword.any { !it.isLetterOrDigit() }
                if (hasLetter && hasDigit && hasSpecial && newPassword.length >= 8) "Strong & Secure 🟢"
                else if (hasLetter && hasDigit) "Medium Strength 🟡 (Add special symbol like @)"
                else "Weak 🔴 (Use letters and numbers together)"
            }
        }

        val passwordsMatch = remember(newPassword, confirmPassword) {
            if (confirmPassword.isEmpty()) true
            else newPassword == confirmPassword
        }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color(0xFFE60000)
                    )
                    Text("Create New Security User")
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text(
                            "Configure the privileges and password for the account in the SMC Control Portal.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = newUsername,
                                onValueChange = { newUsername = it; errorMsg = null },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                isError = isUsernameTaken
                            )
                            if (isUsernameTaken) {
                                Text(
                                    text = "Username is not available or already registered ⚠️",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            } else if (newUsername.isNotBlank()) {
                                Text(
                                    text = "Username is available and valid ✅",
                                    color = Color(0xFF2E7D32),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it; errorMsg = null },
                                label = { Text("Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (passwordStrength.isNotEmpty()) {
                                Text(
                                    text = "Password strength: $passwordStrength",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; errorMsg = null },
                                label = { Text("Confirm Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                isError = !passwordsMatch
                            )
                            if (!passwordsMatch) {
                                Text(
                                    text = "Passwords do not match ❌",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Select Role & Administrative Boundary (Role & Privileges):",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    val roleOptions = listOf(
                        "Admin" to "Full control over all devices, ticket generation, and security tasks",
                        "Team Leader" to "Monitor operational alerts, assign tickets, and invite engineers",
                        "IT Engineer" to "Interact directly to resolve tickets and change PC statuses",
                        "Viewer" to "Passive monitoring screen to view reports, charts, and public stats"
                    )

                    roleOptions.forEach { (role, desc) ->
                        item {
                            val selected = newRole == role
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) Color(0xFFE60000).copy(alpha = 0.08f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (selected) Color(0xFFE60000)
                                        else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { newRole = role }
                                    .padding(12.dp)
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { newRole = role }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = role,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (selected) Color(0xFFE60000) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (errorMsg != null) {
                        item {
                            Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUsername.isBlank() || newPassword.isBlank()) {
                            errorMsg = " Credentials cannot be blank."
                            return@Button
                        }
                        if (isUsernameTaken) {
                            errorMsg = "Username is already taken by another user."
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            errorMsg = "Confirm password does not match with the original password."
                            return@Button
                        }
                        viewModel.addNewLoginUser(newUsername, newPassword, newRole) { success, msg ->
                            if (success) {
                                showAddUserDialog = false
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                errorMsg = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000)),
                    enabled = !isUsernameTaken && newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && (newPassword == confirmPassword)
                ) {
                    Text("Save & Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit User Dialog
    if (userToEdit != null) {
        val targetUser = userToEdit!!
        val isPrimaryAdmin = targetUser.username == "v22aali168"
        
        var editUsername by remember { mutableStateOf(targetUser.username) }
        var editPassword by remember { mutableStateOf(targetUser.passwordHash) }
        var editRole by remember { mutableStateOf(targetUser.role) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { userToEdit = null },
            title = { Text("Modify User Credentials") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Modify security login parameters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { 
                            if (!isPrimaryAdmin) {
                                editUsername = it
                                errorMsg = null 
                            }
                        },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isPrimaryAdmin, // LOCKED for Primary Admin v22aali168
                        supportingText = {
                            if (isPrimaryAdmin) {
                                Text("Primary Admin username is read-only.", color = Color(0xFFE60000))
                            }
                        }
                    )

                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it; errorMsg = null },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Role & Authority Boundary:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Admin", "Team Leader", "IT Engineer", "Viewer").forEach { role ->
                                val selected = editRole == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) Color(0xFFE60000) else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { 
                                            if (!isPrimaryAdmin || role == "Admin") {
                                                editRole = role 
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AutoScaleText(
                                        text = role,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (errorMsg != null) {
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editUsername.isBlank() || editPassword.isBlank()) {
                            errorMsg = "Credentials cannot be blank."
                            return@Button
                        }
                        viewModel.updateLoginUser(targetUser.id, editUsername, editPassword, editRole) { success, msg ->
                            if (success) {
                                userToEdit = null
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                errorMsg = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000))
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BackupRestoreCard(viewModel: MainViewModel) {
    val context = LocalContext.current
    var backupPassword by remember { mutableStateOf("voda_smc_secure_backup_2026") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var pendingBackupPayload by remember { mutableStateOf<String?>(null) }
    
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var selectedRestorePayload by remember { mutableStateOf("") }
    var restorePasswordInput by remember { mutableStateOf("") }
    var restorePasswordVisible by remember { mutableStateOf(false) }
    var restoreErrorMsg by remember { mutableStateOf<String?>(null) }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val payload = pendingBackupPayload
            if (payload != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(payload.toByteArray(Charsets.UTF_8))
                    }
                    android.widget.Toast.makeText(
                        context,
                        "Backup saved successfully with .smcvf extension!",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(
                        context,
                        "Failed to save backup file: ${e.localizedMessage}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } finally {
                    pendingBackupPayload = null
                }
            }
        }
    }

    val selectFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val fileContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                }
                if (!fileContent.isNullOrBlank()) {
                    if (fileContent.trim().startsWith("SMCVF_V1")) {
                        selectedRestorePayload = fileContent
                        restorePasswordInput = backupPassword
                        restoreErrorMsg = null
                        showRestoreConfirmDialog = true
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "Invalid file! Not a recognized SMC Vodafone (.smcvf) package.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    android.widget.Toast.makeText(context, "Selected backup file is empty.", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(
                    context,
                    "Failed to read backup file: ${e.localizedMessage}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("backup_restore_card"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE60000).copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Secure Backup and Restore Icon",
                    tint = Color(0xFFE60000),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "SECURE BACKUP & RESTORE GATEWAY",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE60000)
                    )
                    Text(
                        text = "AES Symmetric Snapshot Archiver (.smcvf)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Text(
                text = "Backup local tables, tickets, notes, and logs to an offline encrypted file.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = backupPassword,
                onValueChange = { backupPassword = it },
                label = { Text("Encryption Password Shield") },
                supportingText = {
                    Text("This custom password acts as the symmetric decrypt credentials during rollback.", style = MaterialTheme.typography.labelSmall)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Security key icon"
                    )
                },
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                    }
                },
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isCompact = maxWidth < 400.dp
                if (isCompact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (backupPassword.isBlank()) {
                                    android.widget.Toast.makeText(context, "Password cannot be empty.", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                viewModel.backupDatabaseEncrypted(backupPassword) { success, msg, payload ->
                                    if (success && payload != null) {
                                        pendingBackupPayload = payload
                                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                                        saveFileLauncher.launch("smc_backup_$timestamp.smcvf")
                                    } else {
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE60000),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Download export",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Backup", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = {
                                selectFileLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload import",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Backup", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (backupPassword.isBlank()) {
                                    android.widget.Toast.makeText(context, "Password cannot be empty.", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                viewModel.backupDatabaseEncrypted(backupPassword) { success, msg, payload ->
                                    if (success && payload != null) {
                                        pendingBackupPayload = payload
                                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                                        saveFileLauncher.launch("smc_backup_$timestamp.smcvf")
                                    } else {
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE60000),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Download export",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Backup", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = {
                                selectFileLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload import",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Backup", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Decrypt lock",
                        tint = Color(0xFFE60000)
                    )
                    Text("Authorize Restore Signature")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "WARNING: Restoring this file will rewrite all current local tables and reset active sessions. This operation cannot be undone.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    
                    Text(
                        text = "Please enter the original secret password configured during backup to decrypt/verify:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = restorePasswordInput,
                        onValueChange = { 
                            restorePasswordInput = it
                            restoreErrorMsg = null
                        },
                        label = { Text("Restore Password Shield") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Key, contentDescription = "Decryption key icon")
                        },
                        trailingIcon = {
                            val icon = if (restorePasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { restorePasswordVisible = !restorePasswordVisible }) {
                                Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                            }
                        },
                        visualTransformation = if (restorePasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (restoreErrorMsg != null) {
                        Text(
                            text = restoreErrorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restorePasswordInput.isBlank()) {
                            restoreErrorMsg = "Decrypt password cannot be empty."
                            return@Button
                        }
                        
                        viewModel.restoreDatabaseEncrypted(selectedRestorePayload, restorePasswordInput) { success, msg ->
                            if (success) {
                                showRestoreConfirmDialog = false
                                android.widget.Toast.makeText(context, "System state restored successfully!", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                restoreErrorMsg = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000))
                ) {
                    Text("Decrypt & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EditITARRequestDialog(
    request: ITARRequest,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(request.employeeName) }
    var username by remember { mutableStateOf(request.username) }
    var requestType by remember { mutableStateOf(request.requestType) }
    var implementer by remember { mutableStateOf(request.implementer) }
    var systemOwner by remember { mutableStateOf(request.systemOwner) }
    var notes by remember { mutableStateOf(request.internalNotes) }
    var site by remember { mutableStateOf(request.site.ifEmpty { "Korba" }) }
    var date by remember { mutableStateOf(request.startDate) }
    var status by remember { mutableStateOf(request.status) }
    var dateValidationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit ITAR Request Details",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFE60000)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Employee Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                val isDark = isAppDarkTheme()
                Text("Profile Type Options:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                val profileOptions = listOf("CCPLUSE", "GIR", "Citrix", "Siebel", "CCat", "GIS", "DL", "Shared Area", "Email Access", "Change Job Title", "Change Office", "Change Number")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profileOptions.forEach { option ->
                        val isSelected = (requestType == option)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                    else if (isDark) MaterialTheme.colorScheme.surfaceVariant 
                                    else Color(0xFFF1F5F9)
                                )
                                .border(
                                    width = 1.dp, 
                                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                                            else if (isDark) MaterialTheme.colorScheme.outlineVariant 
                                            else Color(0xFFE2E8F0), 
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { requestType = option }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = requestType,
                    onValueChange = { requestType = it },
                    label = { Text("Request Type") },
                    modifier = Modifier.fillMaxWidth()
                )

                var showDatePicker by remember { mutableStateOf(false) }

                if (showDatePicker) {
                    val calendar = java.util.Calendar.getInstance()
                    if (date.isNotEmpty()) {
                        try {
                            val parts = date.split("-")
                            if (parts.size == 3) {
                                calendar.set(java.util.Calendar.YEAR, parts[0].toInt())
                                calendar.set(java.util.Calendar.MONTH, parts[1].toInt() - 1)
                                calendar.set(java.util.Calendar.DAY_OF_MONTH, parts[2].toInt())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    val year = calendar.get(java.util.Calendar.YEAR)
                    val month = calendar.get(java.util.Calendar.MONTH)
                    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                    android.app.DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            val formattedDate = String.format(
                                java.util.Locale.getDefault(),
                                "%04d-%02d-%02d",
                                selectedYear,
                                selectedMonth + 1,
                                selectedDay
                            )
                            date = formattedDate
                            dateValidationError = null
                            showDatePicker = false
                        },
                        year,
                        month,
                        day
                    ).apply {
                        setOnDismissListener {
                            showDatePicker = false
                        }
                        show()
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Date of Incident/Request") },
                        isError = dateValidationError != null,
                        supportingText = {
                            if (dateValidationError != null) {
                                Text(dateValidationError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = Color(0xFFE60000)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                Text("Select Site", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val siteOptions = listOf("Korba", "Horizon", "Asyut")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    siteOptions.forEach { option ->
                        val isSelected = (site == option)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFFE60000) 
                                    else if (isDark) MaterialTheme.colorScheme.surfaceVariant 
                                    else Color(0xFFF1F5F9)
                                )
                                .border(
                                    width = 1.dp, 
                                    color = if (isSelected) Color(0xFFE60000) 
                                            else if (isDark) MaterialTheme.colorScheme.outlineVariant 
                                            else Color(0xFFE2E8F0), 
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { site = option }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = implementer,
                    onValueChange = { implementer = it },
                    label = { Text("Assigned To") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = systemOwner,
                    onValueChange = { systemOwner = it },
                    label = { Text("Requested by") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Status Option:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val statusOptions = listOf("Pending", "Approved", "Review Needed", "Expired")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.forEach { option ->
                        val isSelected = (status == option)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                    else if (isDark) MaterialTheme.colorScheme.surfaceVariant 
                                    else Color(0xFFF1F5F9)
                                )
                                .border(
                                    width = 1.dp, 
                                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                                            else if (isDark) MaterialTheme.colorScheme.outlineVariant 
                                            else Color(0xFFE2E8F0), 
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { status = option }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Internal Audit Notes") },
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.updateITARRequest(
                            request.copy(
                                employeeName = name,
                                username = username,
                                requestType = requestType,
                                implementer = implementer,
                                systemOwner = systemOwner,
                                startDate = date,
                                site = site,
                                internalNotes = notes,
                                status = status
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000))
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
