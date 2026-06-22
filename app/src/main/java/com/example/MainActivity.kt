package com.example

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dynamically initialize and check Firebase secure runtime environment variables
        AuraEnvConfig.initializeFirebaseSafely(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

// Global active sub-tabs
enum class AppTab {
    HOME, STORE, GAMES, AI_COPILOT, ADMIN_DECK, PROFILE
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppContainer() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Observe DB States
    val currentUser by AuraRepository.currentUserState.collectAsStateWithLifecycle()
    val banners by AuraRepository.bannersState.collectAsStateWithLifecycle()
    val creators by AuraRepository.creatorsState.collectAsStateWithLifecycle()
    val products by AuraRepository.productsState.collectAsStateWithLifecycle()
    val news by AuraRepository.newsState.collectAsStateWithLifecycle()
    val posts by AuraRepository.postsState.collectAsStateWithLifecycle()
    val chatMessages by AuraRepository.chatMessagesState.collectAsStateWithLifecycle()
    val games by AuraRepository.gamesState.collectAsStateWithLifecycle()
    val notifications by AuraRepository.notificationsState.collectAsStateWithLifecycle()
    val usersList by AuraRepository.usersListState.collectAsStateWithLifecycle()

    // NEW Social states
    val videos by AuraRepository.videosState.collectAsStateWithLifecycle()
    val liveStreams by AuraRepository.liveStreamsState.collectAsStateWithLifecycle()

    // Screen navigation layout states
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var showNotificationsDrawer by remember { mutableStateOf(false) }
    var activeProductDetailId by remember { mutableStateOf<String?>(null) }
    var activeCreatorDetailId by remember { mutableStateOf<String?>(null) }
    
    var activeVideoId by remember { mutableStateOf<String?>(null) }
    var activeLiveStreamId by remember { mutableStateOf<String?>(null) }
    var showCreationMenu by remember { mutableStateOf(false) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showUploadVideoDialog by remember { mutableStateOf(false) }
    var showStartLiveDialog by remember { mutableStateOf(false) }
    var pipVideoId by remember { mutableStateOf<String?>(null) }
    var pipLiveStreamId by remember { mutableStateOf<String?>(null) }

    // Onboarding form draft state
    var onboardingStep by remember { mutableStateOf(1) }
    var draftEmail by remember { mutableStateOf("") }
    var draftPassword by remember { mutableStateOf("") }
    var draftConfirmPass by remember { mutableStateOf("") }
    var draftDisplayName by remember { mutableStateOf("") }
    var draftProfilePic by remember { mutableStateOf("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200") }
    var draftUsername by remember { mutableStateOf("") }
    var draftBio by remember { mutableStateOf("") }
    var draftDob by remember { mutableStateOf("2000-01-01") }
    var draftCountry by remember { mutableStateOf("United States") }
    val draftInterests = remember { mutableStateListOf<String>() }

    // If user is banned, display banned message immediately
    if (currentUser.isBanned) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF080B10))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "🚨 ACCESS PERMANENTLY REVOKED",
                    color = Color(0xFFFF007A),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                    border = BorderStroke(1.dp, Color(0xFFFF007A).copy(alpha = 0.5f))
                ) {
                    Text(
                        "Your account (${currentUser.email}) was flagged and banned by the moderator or admin for violating community guidelines. Contact support@auracommunity.com to challenge.",
                        color = Color.LightGray,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        AuraRepository.resetToDefaults()
                        Toast.makeText(context, "Resetting sandbox state", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("Re-validate Profile Sandbox", color = Color.Black)
                }
            }
        }
        return
    }

    // MANDATORY ONBOARDING COMPLIANCE
    // "After signup, users must complete onboarding in multiple steps."
    // "Only after completing all steps should the user be redirected to Home."
    if (!currentUser.isOnboarded) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFF080B10)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF121620), Color(0xFF080B10))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AURA COMMUNITY PROFILE SETUP",
                        color = Color(0xFF00E5FF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Progress bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 1..6) {
                            val barColor = if (i <= onboardingStep) Color(0xFFBD00FF) else Color(0xFF2B3245)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(barColor)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Step $onboardingStep of 6",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedContent(
                        targetState = onboardingStep,
                        transitionSpec = {
                            fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
                        },
                        label = "onboard_steps"
                    ) { step ->
                        when (step) {
                            1 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("ENTER SECURITY CREDENTIALS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = draftEmail,
                                        onValueChange = { draftEmail = it },
                                        label = { Text("Email Address") },
                                        modifier = Modifier.fillMaxWidth().testTag("onboard_email_input"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                                    )
                                    OutlinedTextField(
                                        value = draftPassword,
                                        onValueChange = { draftPassword = it },
                                        label = { Text("Password") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth().testTag("onboard_pass_input"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                                    )
                                    OutlinedTextField(
                                        value = draftConfirmPass,
                                        onValueChange = { draftConfirmPass = it },
                                        label = { Text("Confirm Password") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth().testTag("onboard_confirmpass_input"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            if (draftEmail.isBlank() || draftPassword.isBlank()) {
                                                Toast.makeText(context, "Fill in all credentials", Toast.LENGTH_SHORT).show()
                                            } else if (draftPassword != draftConfirmPass) {
                                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                            } else {
                                                onboardingStep = 2
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                                        modifier = Modifier.fillMaxWidth().testTag("onboard_step1_next")
                                    ) {
                                        Text("Continue (Next Step)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            2 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("CUSTOMIZE AVATAR & NAME", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                                    // Display name input
                                    OutlinedTextField(
                                        value = draftDisplayName,
                                        onValueChange = { draftDisplayName = it },
                                        label = { Text("Display Name") },
                                        modifier = Modifier.fillMaxWidth().testTag("onboard_name_input"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Pick Preset Cyber Profile Avatar", color = Color.LightGray, fontSize = 13.sp)

                                    // Interactive presets for profile picture
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val avatarPresets = listOf(
                                            "https://images.unsplash.com/photo-1566492031773-4f4e44671857?q=80&w=200", // Noob preset
                                            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200", // Admin style
                                            "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?q=80&w=200", // Pro gamer presets
                                            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200"  // Cyber girl preset
                                        )
                                        avatarPresets.forEachIndexed { idx, url ->
                                            val isSelected = draftProfilePic == url
                                            Box(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(CircleShape)
                                                    .border(2.dp, if (isSelected) Color(0xFF00E5FF) else Color.Transparent)
                                                    .clickable { draftProfilePic = url }
                                            ) {
                                                AsyncImage(
                                                    model = url,
                                                    contentDescription = "preset $idx",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = { onboardingStep = 1 },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2230)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Back")
                                        }
                                        Button(
                                            onClick = {
                                                if (draftDisplayName.isBlank()) {
                                                    Toast.makeText(context, "Please enter a display name", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onboardingStep = 3
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                                            modifier = Modifier.weight(1.5f).testTag("onboard_step2_next")
                                        ) {
                                            Text("Continue")
                                        }
                                    }
                                }
                            }
                            3 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("CLAIM USERNAME & BIO", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = draftUsername,
                                        onValueChange = { draftUsername = it },
                                        label = { Text("Unique Gaming Username") },
                                        prefix = { Text("@") },
                                        modifier = Modifier.fillMaxWidth().testTag("onboard_user_input"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                                    )
                                    OutlinedTextField(
                                        value = draftBio,
                                        onValueChange = { draftBio = it },
                                        label = { Text("Aura Gaming Bio (Short desc)") },
                                        modifier = Modifier.fillMaxWidth().testTag("onboard_bio_input"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1D2230))
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = { onboardingStep = 2 },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2230)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Back")
                                        }
                                        Button(
                                            onClick = {
                                                if (draftUsername.isBlank() || draftBio.isBlank()) {
                                                    Toast.makeText(context, "Fill tags & details", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onboardingStep = 4
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                                            modifier = Modifier.weight(1.5f).testTag("onboard_step3_next")
                                        ) {
                                            Text("Continue")
                                        }
                                    }
                                }
                            }
                            4 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("BIRTHDAY & AUTO AGE ENFORCEMENT", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                                    Text(
                                        "Selected DOB: $draftDob",
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 18.sp
                                    )

                                    // DOB Date Dialog Picker
                                    Button(
                                        onClick = {
                                            val calendar = Calendar.getInstance()
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, dayOfMonth ->
                                                    val formattedMonth = String.format("%02d", month + 1)
                                                    val formattedDay = String.format("%02d", dayOfMonth)
                                                    draftDob = "$year-$formattedMonth-$formattedDay"
                                                },
                                                calendar.get(Calendar.YEAR) - 20,
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2230)),
                                        border = BorderStroke(1.dp, Color(0xFF00E5FF))
                                    ) {
                                        Text("📅 Trigger Date Dialog Picker", color = Color(0xFF00E5FF))
                                    }

                                    // Dynamic age calculation display
                                    val computedAge = 2026 - draftDob.split("-")[0].toInt()
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2230))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Auto Calculated Age:", color = Color.LightGray, fontSize = 11.sp)
                                            Text("$computedAge Years Old", color = Color(0xFFFF007A), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = { onboardingStep = 3 },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2230)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Back")
                                        }
                                        Button(
                                            onClick = { onboardingStep = 5 },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                                            modifier = Modifier.weight(1.5f).testTag("onboard_step4_next")
                                        ) {
                                            Text("Continue")
                                        }
                                    }
                                }
                            }
                            5 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("GEOGRAPHY & GAME PLAY INTERESTS", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                                    // Country
                                    OutlinedTextField(
                                        value = draftCountry,
                                        onValueChange = { draftCountry = it },
                                        label = { Text("Country Location") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                                    )

                                    Text("Select Core Interests (Multi-Click Tags):", color = Color.LightGray, fontSize = 13.sp)

                                    val availableInterests = listOf("Gaming Laptops", "Gaming Phones", "Gaming Keyboards", "Mini Games", "Online Matchmaking", "FPS Tournaments", "Retro Arcade")

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        availableInterests.forEach { tag ->
                                            val isSelected = draftInterests.contains(tag)
                                            val chipBg = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1D2230)
                                            val chipTx = if (isSelected) Color.Black else Color.White
                                            Box(
                                                modifier = Modifier
                                                    .border(1.dp, Color(0xFF2B3245), RoundedCornerShape(16.dp))
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(chipBg)
                                                    .clickable {
                                                        if (isSelected) draftInterests.remove(tag) else draftInterests.add(tag)
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(tag, color = chipTx, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = { onboardingStep = 4 },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2230)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Back")
                                        }
                                        Button(
                                            onClick = {
                                                if (draftInterests.isEmpty()) {
                                                    Toast.makeText(context, "Select at least 1 interest tag", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onboardingStep = 6
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                                            modifier = Modifier.weight(1.5f).testTag("onboard_step5_next")
                                        ) {
                                            Text("Continue")
                                        }
                                    }
                                }
                            }
                            6 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("VERIFY PROFILE DETAILS", color = Color(0xFF00FF85), fontSize = 18.sp, fontWeight = FontWeight.Bold)

                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                    ) {
                                        AsyncImage(model = draftProfilePic, contentDescription = "avatar", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                                        border = BorderStroke(1.dp, Color(0xFF2B3245))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("Email: $draftEmail", color = Color.White)
                                            Text("Display Name: $draftDisplayName", color = Color.White)
                                            Text("Username: @$draftUsername", color = Color(0xFF00E5FF))
                                            Text("Bio: $draftBio", color = Color.LightGray)
                                            Text("DOB / Age: $draftDob", color = Color.White)
                                            Text("Country: $draftCountry", color = Color.White)
                                            Text("Interests: ${draftInterests.joinToString()}", color = Color(0xFFBD00FF))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = { onboardingStep = 5 },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2230)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Back")
                                        }
                                        Button(
                                            onClick = {
                                                // Create profile and navigate
                                                val profile = UserProfile(
                                                    id = "user_" + System.currentTimeMillis(),
                                                    email = draftEmail,
                                                    displayName = draftDisplayName,
                                                    username = draftUsername,
                                                    bio = draftBio,
                                                    profilePic = draftProfilePic,
                                                    birthDate = draftDob,
                                                    country = draftCountry,
                                                    interests = draftInterests.toList(),
                                                    role = UserRole.COMMUNITY_MEMBER, // Automatically becomes active community member
                                                    isOnboarded = true
                                                )
                                                AuraRepository.createAndOnboardUser(profile)
                                                Toast.makeText(context, "Aura profile synced! Welcome.", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF85)),
                                            modifier = Modifier.weight(1.5f).testTag("onboard_step6_finish")
                                        ) {
                                            Text("Launch Account", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // --- MAIN LOGGED-IN SYSTEM TEMPLATE ---
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        containerColor = Color(0xFF080B10),
        topBar = {
            Column {
                // Top Navbar Title
                Surface(
                    color = Color(0xFF121620),
                    border = BorderStroke(1.dp, Color(0xFF2B3245).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .statusBarsPadding()
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title/Logo
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "ACT",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.testTag("app_logo")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Aura Gaming",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Badge with active switcher role
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFBD00FF).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFBD00FF).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentUser.role.name,
                                    color = Color(0xFFBD00FF),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Notifications drawer & Chat shortcuts
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Notifications badge trigger
                            Box {
                                IconButton(
                                    onClick = { showNotificationsDrawer = !showNotificationsDrawer },
                                    modifier = Modifier.testTag("top_notif_icon")
                                ) {
                                    Icon(
                                        Icons.Outlined.Notifications,
                                        contentDescription = "Notifications Drawer",
                                        tint = if (notifications.isNotEmpty()) Color(0xFF00E5FF) else Color.White
                                    )
                                }
                                if (notifications.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                            .size(8.dp)
                                            .background(Color(0xFFFF007A), CircleShape)
                                    )
                                }
                            }
                            IconButton(onClick = { currentTab = AppTab.AI_COPILOT }) {
                                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "AI Copilot Launcher", tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF121620),
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { currentTab = AppTab.HOME },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Community") },
                    label = { Text("Community", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF00E5FF), indicatorColor = Color(0xFF1D2230))
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.STORE,
                    onClick = { currentTab = AppTab.STORE },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Store") },
                    label = { Text("Store", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF00E5FF), indicatorColor = Color(0xFF1D2230))
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.GAMES,
                    onClick = { currentTab = AppTab.GAMES },
                    icon = { Icon(Icons.Default.VideogameAsset, contentDescription = "Arcade") },
                    label = { Text("Arcade", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF00E5FF), indicatorColor = Color(0xFF1D2230))
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.AI_COPILOT,
                    onClick = { currentTab = AppTab.AI_COPILOT },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Aura AI") },
                    label = { Text("Aura AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF00E5FF), indicatorColor = Color(0xFF1D2230))
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.ADMIN_DECK,
                    onClick = { currentTab = AppTab.ADMIN_DECK },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Admin") },
                    label = { Text("Admin", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFBD00FF), indicatorColor = Color(0xFF1D2230))
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.PROFILE,
                    onClick = { currentTab = AppTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF00E5FF), indicatorColor = Color(0xFF1D2230))
                )
            }
        }
    ) { innerPadding ->

        // Global sliding drawer/dismiss overlay for real-time notifications
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main tab router content
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "app_tabs_anim"
            ) { tab ->
                when (tab) {
                    AppTab.HOME -> CommunityTabScreen(
                        banners = banners,
                        creators = creators,
                        products = products,
                        newsList = news,
                        posts = posts,
                        videos = videos,
                        liveStreams = liveStreams,
                        currentUser = currentUser,
                        onClickCreator = { activeCreatorDetailId = it },
                        onClickProduct = { activeProductDetailId = it },
                        onClickViewAllStore = { currentTab = AppTab.STORE },
                        onClickVideo = { activeVideoId = it },
                        onClickLive = { activeLiveStreamId = it }
                    )
                    AppTab.STORE -> StoreTabScreen(
                        products,
                        onProductClick = { activeProductDetailId = it }
                    )
                    AppTab.GAMES -> GamesTabScreen(games)
                    AppTab.AI_COPILOT -> AIHelperTabScreen()
                    AppTab.ADMIN_DECK -> AdminDashboardTabScreen(
                        usersList, products, games, news, posts, banners, creators, notifications,
                        onSwitchToAdmin = { AuraRepository.switchUserRole(UserRole.ADMIN) }
                    )
                    AppTab.PROFILE -> ProfileTabScreen(currentUser, posts, onTriggerCreationMenu = { showCreationMenu = true })
                }
            }

            // Real-time broadcast notification logs overlay shelf
            if (showNotificationsDrawer) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showNotificationsDrawer = false }
                ) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(310.dp)
                            .fillMaxHeight(0.7f)
                            .padding(8.dp)
                            .clickable(enabled = false) {},
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                        border = BorderStroke(1.dp, Color(0xFF2B3245))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("NOTIFICATIONS", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                TextButton(onClick = { AuraRepository.resetToDefaults() }) {
                                    Text("Clear", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (notifications.isEmpty()) {
                                Text("All caught up! No recent alerts.", color = Color.Gray, fontSize = 12.sp)
                            }
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(notifications) { notif ->
                                    val icon = when (notif.type) {
                                        "shop" -> "🛒"
                                        "game" -> "🎮"
                                        "chat" -> "💬"
                                        "news" -> "⚡"
                                        else -> "🤖"
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1D2230), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(icon)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(notif.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(notif.description, color = Color.LightGray, fontSize = 11.sp)
                                            Text(notif.timestamp, color = Color.Gray, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Products display modal overlay
            activeProductDetailId?.let { pId ->
                val prd = products.find { it.id == pId }
                if (prd != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { activeProductDetailId = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(16.dp)
                                .clickable(enabled = false) {},
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = prd.image,
                                        contentDescription = "product pic",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(36.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .clickable { AuraRepository.toggleProductWishlist(prd.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (prd.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "wish",
                                            tint = if (prd.isWishlisted) Color.Red else Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(prd.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(prd.category, color = Color(0xFFBD00FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = "stars", tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                                    Text(" ${prd.rating} rating", color = Color.White, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(prd.description, color = Color.LightGray, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "$${prd.price}",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(
                                        onClick = {
                                            AuraRepository.purchaseProductToInventory(prd)
                                            Toast.makeText(context, "Direct Checkout Secured! $${prd.price} pre-approved and purchased to inventory.", Toast.LENGTH_LONG).show()
                                            activeProductDetailId = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF85))
                                    ) {
                                        Text("Buy Direct", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Creator profile detail modal overlay
            activeCreatorDetailId?.let { cId ->
                val cr = creators.find { it.id == cId }
                if (cr != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { activeCreatorDetailId = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(16.dp)
                                .clickable(enabled = false) {},
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                            border = BorderStroke(1.dp, Color(0xFFBD00FF))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = if (cr.banner.isNotBlank()) cr.banner else "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=600",
                                        contentDescription = "banner",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Row(
                                    modifier = Modifier.offset(y = (-30).dp).padding(horizontal = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color(0xFFBD00FF), CircleShape)
                                    ) {
                                        AsyncImage(model = cr.profilePic, contentDescription = "creator", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    }
                                    Column(modifier = Modifier.padding(top = 34.dp, start = 8.dp)) {
                                        Text(cr.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text("${cr.followers} followers", color = Color.LightGray, fontSize = 12.sp)
                                    }
                                }

                                Text("About Creator:", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(cr.bio, color = Color.LightGray, fontSize = 13.sp)

                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            AuraRepository.toggleFollowCreator(cr.id)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (cr.isFollowed) Color(0xFF1D2230) else Color(0xFFBD00FF)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (cr.isFollowed) "Following" else "Follow Creator", color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            currentTab = AppTab.AI_COPILOT
                                            activeCreatorDetailId = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Send Message", color = Color.Black)
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

// ==========================================
//                 HOME TAB SCREEN
// ==========================================

@Composable
fun HomeTabScreen(
    banners: List<HeroBanner>,
    creators: List<Creator>,
    products: List<Product>,
    newsList: List<NewsItem>,
    posts: List<Post>,
    currentUser: UserProfile,
    onClickCreator: (String) -> Unit,
    onClickProduct: (String) -> Unit,
    onClickViewAllStore: () -> Unit
) {
    var newPostText by remember { mutableStateOf("") }
    var selectedPostBg by remember { mutableStateOf<String?>(null) }
    val postBgs = listOf(
        "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=300",
        "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=300",
        "https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?q=80&w=300"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // 1. HERO BANNERS FLOW
        if (banners.isNotEmpty()) {
            item {
                Text("💥 HOT EVENTS TODAY", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(banners) { banner ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .height(160.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(model = banner.image, contentDescription = "banner", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                ) {
                                    Text(banner.headline, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(banner.description, color = Color.LightGray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(banner.ctaText, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. HORIZONTAL CREATORS BAR
        if (creators.isNotEmpty()) {
            item {
                Text("🔥 PRO GAMING CREATORS", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(creators) { cr ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onClickCreator(cr.id) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (cr.isFollowed) Color(0xFF00E5FF) else Color(0xFFBD00FF), CircleShape)
                            ) {
                                AsyncImage(
                                    model = cr.profilePic,
                                    contentDescription = "creator profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cr.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${cr.followers} follow", color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        // 3. PRODUCTS PREVIEW ROW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚡ CHROMA GEAR SHOP", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                TextButton(onClick = onClickViewAllStore) {
                    Text("VIEW ALL STORE", color = Color(0xFF00E5FF), fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products.take(4)) { prd ->
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .clickable { onClickProduct(prd.id) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620))
                    ) {
                        Column {
                            AsyncImage(
                                model = prd.image,
                                contentDescription = "gear preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(prd.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("$${prd.price}", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. LATEST GAMING NEWS
        item {
            Text("⚡ LATEST ACT NEWS TILES", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                newsList.forEach { newsItem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                        border = BorderStroke(1.dp, Color(0xFF2B3245).copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AsyncImage(
                                model = newsItem.image,
                                contentDescription = "news pic",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(newsItem.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(newsItem.description, color = Color.LightGray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(newsItem.publishDate, color = Color.Gray, fontSize = 9.sp)
                                    Text("Official News", color = Color(0xFFBD00FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. COMMUNITY POST PUBLISHER CREATOR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                border = BorderStroke(1.dp, Color(0xFFBD00FF).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📢 SHARE TO THE COMM COMMUNITY", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPostText,
                        onValueChange = { newPostText = it },
                        placeholder = { Text("What gaming is on your mind?", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("community_post_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFBD00FF))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick backdrop selector
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            postBgs.forEach { bgUrl ->
                                val isSelected = selectedPostBg == bgUrl
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, if (isSelected) Color(0xFF00E5FF) else Color.Transparent)
                                        .clickable { selectedPostBg = if (isSelected) null else bgUrl }
                                ) {
                                    AsyncImage(model = bgUrl, contentDescription = "bg", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (newPostText.isNotBlank()) {
                                    AuraRepository.createPost(newPostText, selectedPostBg)
                                    newPostText = ""
                                    selectedPostBg = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                            modifier = Modifier.testTag("submit_post_btn")
                        ) {
                            Text("Post Now", color = Color.White)
                        }
                    }
                }
            }
        }

        // 6. REAL-TIME PLAY FEED
        item {
            Text("📢 COMMUNITY STREAM FEED", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(posts) { post ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                border = BorderStroke(1.dp, Color(0xFF2B3245))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        ) {
                            AsyncImage(model = post.authorAvatar, contentDescription = "av", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(post.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                if (post.authorRole == UserRole.ADMIN) {
                                    Text("ADMIN", color = Color(0xFFFF007A), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("@author", color = Color.Gray, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(post.content, color = Color.White, fontSize = 13.sp)

                    post.image?.let { img ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(model = img, contentDescription = "post asset", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF2B3245), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { AuraRepository.toggleLikePost(post.id) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (post.isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUp,
                                    tint = if (post.isLiked) Color(0xFF00E5FF) else Color.Gray,
                                    contentDescription = "like"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${post.likes}", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                        IconButton(onClick = {}) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Comment, tint = Color.Gray, contentDescription = "comment")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${post.comments.size}", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                        IconButton(onClick = {}) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, tint = Color.Gray, contentDescription = "share")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${post.shares}", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
//                 STORE TAB
// ==========================================

@Composable
fun StoreTabScreen(
    products: List<Product>,
    onProductClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Categories") }

    val categories = listOf(
        "All Categories", "Gaming Phones", "Gaming Laptops",
        "Gaming Keyboards", "Gaming Mouse", "Gaming Headsets",
        "Gaming Controllers", "Gaming Chairs", "Gaming Accessories"
    )

    val filteredProducts = products.filter {
        (selectedCategory == "All Categories" || it.category == selectedCategory) &&
                (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("store_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🛒 CHROMA STATIONS STORE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // Search Bar input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search gaming gear, laptops, chairs...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search icon") },
            modifier = Modifier.fillMaxWidth().testTag("product_search_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
        )

        // Categories selector
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .border(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF2B3245), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(cat, color = if (isSelected) Color(0xFF00E5FF) else Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Divider(color = Color(0xFF2B3245), thickness = 0.5.dp)

        // Product Grid list
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No items found. Change filters or search terms.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredProducts) { prd ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(prd.id) }
                    ) {
                        Column {
                            Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                                AsyncImage(
                                    model = prd.image,
                                    contentDescription = "product picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("$${prd.price}", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(prd.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(prd.category, color = Color.Gray, fontSize = 9.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = "rating", tint = Color(0xFFFFB800), modifier = Modifier.size(12.dp))
                                        Text(" ${prd.rating}", color = Color.White, fontSize = 10.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFBD00FF).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .border(1.dp, Color(0xFFBD00FF), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("View Specs", color = Color(0xFFBD00FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
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

// ==========================================
//                 GAMES TAB SCREEN
// ==========================================

@Composable
fun GamesTabScreen(
    games: List<GameItem>
) {
    var activeArcadeGame by remember { mutableStateOf<String?>(null) } // "snake", "racer", "memory", or "multip"
    var matchLogs by remember { mutableStateOf(listOf<String>()) }
    var inMatchmaking by remember { mutableStateOf(false) }

    LaunchedEffect(inMatchmaking) {
        if (inMatchmaking) {
            matchLogs = listOf("🔍 Pinging ACT servers...", "📡 Found matchmaking pools...")
            delay(1500)
            matchLogs = matchLogs + "🔗 Paired with Opponent: 'SniperApex_99'"
            delay(1200)
            matchLogs = matchLogs + "🚦 Match Loaded! Multi Racing ready!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("games_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🎮 WINZO-STYLE ARCADE STATIONS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        if (activeArcadeGame != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◀ RETURN TO GAMING HUB",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { activeArcadeGame = null }.testTag("btn_close_game"),
                    fontSize = 13.sp
                )
            }
            Divider(color = Color(0xFF2B3245))

            when (activeArcadeGame) {
                "snake" -> NeonSnakeGame(onGameFinished = {})
                "racer" -> CyberStreetRacerGame(onGameFinished = {})
                "memory" -> MemoryMatchTilesGame(onGameFinished = {})
                "multip" -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                        border = BorderStroke(1.dp, Color(0xFFBD00FF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Text("🎮 TIC-TAC-TOE MULTIPLAYER ARENA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (!inMatchmaking) {
                                Button(
                                    onClick = { inMatchmaking = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                                    modifier = Modifier.testTag("trigger_matchmaking")
                                ) {
                                    Text("Find Live Opponent")
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    matchLogs.forEach { log ->
                                        Text(log, color = Color(0xFF00E5FF), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    }
                                    if (matchLogs.size >= 4) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("❌ [Simulation Match] Opponent connection timed out. Restart matchmaking.", color = Color.LightGray, fontSize = 12.sp)
                                        Button(onClick = { inMatchmaking = false }) {
                                            Text("Retry Search")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return
        }

        // Mini games selector list
        Text("Playable Classical Retro Games:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
            border = BorderStroke(1.dp, Color(0xFF2B3245))
        ) {
            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.clickable { activeArcadeGame = "snake" }) {
                    Box(modifier = Modifier.size(50.dp).background(Color(0xFF1D2230), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("🐍", fontSize = 24.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Neon Snake Arcade", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Grid arena snake with speed increments.", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = "play", tint = Color(0xFF00E5FF))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.clickable { activeArcadeGame = "racer" }) {
                    Box(modifier = Modifier.size(50.dp).background(Color(0xFF1D2230), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("🏎️", fontSize = 24.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cyber Street Racer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Fast reflex obstacle dodge tracker.", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = "play", tint = Color(0xFF00E5FF))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.clickable { activeArcadeGame = "memory" }) {
                    Box(modifier = Modifier.size(50.dp).background(Color(0xFF1D2230), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("🧠", fontSize = 24.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Match-Tiles Memory Match", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Coordinate identical cyber symbols.", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = "play", tint = Color(0xFF00E5FF))
                }
            }
        }

        // Matchmaking Board
        Text("Online Board Tournaments:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
            modifier = Modifier.clickable { activeArcadeGame = "multip" }
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("⚔️", fontSize = 24.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tic-Tac-Toe Online", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Queue matching, challenge friends or AI clones.", color = Color.LightGray, fontSize = 11.sp)
                }
                Icon(Icons.Default.People, contentDescription = "online", tint = Color(0xFFBD00FF))
            }
        }

        // Dynamically added list of games without coding
        Text("Dynamically Spawned / Admin Added Games:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val customGamesOnly = games.filter { it.category == "Admin Added" }
            if (customGamesOnly.isEmpty()) {
                item {
                    Text("No custom games loaded. Log in as admin to upload game files.", color = Color.Gray, fontSize = 11.sp)
                }
            } else {
                items(customGamesOnly) { gm ->
                    Card(
                        modifier = Modifier.width(140.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF172030))
                    ) {
                        Column {
                            AsyncImage(model = gm.thumbnail, contentDescription = "thumb", modifier = Modifier.height(70.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
                            Text(gm.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                            Button(
                                onClick = {
                                    AuraRepository.addGameToInventory(gm)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF85)),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp).height(24.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Claim Game", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
//               GEMINI AI SCREEN
// ==========================================

@Composable
fun AIHelperTabScreen() {
    var promptInput by remember { mutableStateOf("") }
    var responsesLog by remember { mutableStateOf(listOf(Pair("AI COPILOT", "Welcome cyber warrior! I'm Aura AI. Ask me strategy patterns, cyber gear checklist specs, or generate custom games trivia trivia."))) }
    var isLoadingResponse by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("ai_helper_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🤖 AURA CO-PILOT (INTELLIGENCE MODEL)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Powered directly by models/gemini-3.5-flash", color = Color(0xFFBD00FF), fontSize = 11.sp)

        // Conversation history log
        Box(
            modifier = Modifier
                .background(Color(0xFF0F131C), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF2B3245), RoundedCornerShape(8.dp))
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true
            ) {
                // Display in reverse order for messaging logic
                items(responsesLog.reversed()) { chat ->
                    val isUser = chat.first == "YOU"
                    val bubbleColor = if (isUser) Color(0xFFBD00FF).copy(alpha = 0.2f) else Color(0xFF1D2230)
                    val bubbleAlign = if (isUser) Alignment.End else Alignment.Start
                    val bubbleBorder = if (isUser) Color(0xFFBD00FF) else Color(0xFF00E5FF)

                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = bubbleAlign) {
                        Text(chat.first, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = bubbleColor),
                            border = BorderStroke(1.dp, bubbleBorder),
                            modifier = Modifier.widthIn(max = 240.dp)
                        ) {
                            Text(chat.second, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }

            if (isLoadingResponse) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).background(Color.Black).padding(6.dp)) {
                    Text("🤖 Simulating logic and querying Gemini cloud servers...", color = Color(0xFF00E5FF), fontSize = 11.sp)
                }
            }
        }

        // Input send box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Ask gaming advice, VCT meta stats, etc...") },
                modifier = Modifier.weight(1f).testTag("ai_prompt_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
            )
            Button(
                onClick = {
                    if (promptInput.isNotBlank()) {
                        val prompt = promptInput
                        responsesLog = responsesLog + Pair("YOU", prompt)
                        promptInput = ""
                        isLoadingResponse = true

                        scope.launch {
                            val res = AuraGeminiService.askGeminiCopilot(prompt)
                            responsesLog = responsesLog + Pair("AI COPILOT", res)
                            isLoadingResponse = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                modifier = Modifier.size(54.dp).testTag("ai_send_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "send prompt", tint = Color.Black)
            }
        }
    }
}

// ==========================================
//               ADMIN TAB SCREEN
// ==========================================

@Composable
fun AdminDashboardTabScreen(
    usersList: List<UserProfile>,
    products: List<Product>,
    games: List<GameItem>,
    news: List<NewsItem>,
    posts: List<Post>,
    banners: List<HeroBanner>,
    creators: List<Creator>,
    notifications: List<AppNotification>,
    onSwitchToAdmin: () -> Unit
) {
    var adminLogText by remember { mutableStateOf("Admin Command deck ready. Banners & database synched.") }

    // Admin forms state
    var inputPrdName by remember { mutableStateOf("") }
    var inputPrdPrice by remember { mutableStateOf("") }
    var inputPrdCategory by remember { mutableStateOf("Gaming Keyboards") }
    var inputPrdDesc by remember { mutableStateOf("") }

    var inputNewsTitle by remember { mutableStateOf("") }
    var inputNewsDesc by remember { mutableStateOf("") }

    var inputGameName by remember { mutableStateOf("") }
    var inputGameThumb by remember { mutableStateOf("https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=200") }

    val user = AuraRepository.currentUserState.value
    if (user.role != UserRole.ADMIN) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("admin_lock_view"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🔒 ACCESS RESTRICTED", color = Color(0xFFFF007A), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "This control deck is restricted exclusively to Admins. Your active role is ${user.role}.",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSwitchToAdmin,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                    modifier = Modifier.testTag("unlock_admin_btn")
                ) {
                    Text("Trigger Mock Administrator Credentials Profile")
                }
            }
        }
        return
    }

    // Full dashboard details
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("🎛️ ACT ADMINISTRATIVE REALTIME DECK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Logged in as Head Admin: auracommunityact@gmail.com", color = Color(0xFF00E5FF), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }

        // Live stats counters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Pair("Total Members", "${usersList.size}"),
                    Pair("Online Matches", "2 Active"),
                    Pair("Products Catalog", "${products.size}"),
                    Pair("Arcades", "${games.size}")
                ).forEach { stats ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stats.first, color = Color.LightGray, fontSize = 9.sp, maxLines = 1)
                            Text(stats.second, color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Command logger terminal
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07090F)),
                border = BorderStroke(1.dp, Color(0xFF2B3245))
            ) {
                Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                    Text("ACT CONTROLLER LOGGER SYSTEM", color = Color(0xFFFF007A), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(adminLogText, color = Color(0xFF00FF85), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // User accounts management panel
        item {
            Text("👮 MEMBER USERS LIST", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121620))) {
                Column(modifier = Modifier.padding(10.dp)) {
                    usersList.forEach { target ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(target.displayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(target.email, color = Color.LightGray, fontSize = 10.sp)
                                Text("Lvl: ${target.level} | Banned: ${target.isBanned}", color = Color.Gray, fontSize = 9.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        AuraRepository.banUserToggle(target.email)
                                        adminLogText = "Swapped Ban status for ${target.email}"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (target.isBanned) Color(0xFF00FF85) else Color(0xFFFF007A)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(if (target.isBanned) "Revoke Ban" else "Ban Account", fontSize = 9.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Product Form Creator
        item {
            Text("➕ REGISTER PRODUCT TO CATALOGUE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                border = BorderStroke(1.dp, Color(0xFF2B3245))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputPrdName,
                        onValueChange = { inputPrdName = it },
                        label = { Text("Product name text") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_prd_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                    )
                    OutlinedTextField(
                        value = inputPrdPrice,
                        onValueChange = { inputPrdPrice = it },
                        label = { Text("Price tag double") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_prd_price_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                    )
                    OutlinedTextField(
                        value = inputPrdDesc,
                        onValueChange = { inputPrdDesc = it },
                        label = { Text("Product specifications description") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_prd_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                    )
                    Button(
                        onClick = {
                            if (inputPrdName.isNotBlank() && inputPrdPrice.isNotBlank()) {
                                val priceVal = inputPrdPrice.toDoubleOrNull() ?: 99.99
                                AuraRepository.addProduct(
                                    Product(
                                        id = "p_a_" + System.currentTimeMillis(),
                                        name = inputPrdName,
                                        description = inputPrdDesc,
                                        price = priceVal,
                                        rating = 4.8f,
                                        image = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?q=80&w=400",
                                        category = inputPrdCategory
                                    )
                                )
                                adminLogText = "Successfully synchronized product catalogue: $inputPrdName"
                                inputPrdName = ""
                                inputPrdPrice = ""
                                inputPrdDesc = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        modifier = Modifier.fillMaxWidth().testTag("add_product_admin_submit")
                    ) {
                        Text("Save & Publish Product", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Add Game Form Creator
        item {
            Text("🎮 REGISTER GAME DYNAMICALLY WITHOUT CODE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121620))) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputGameName,
                        onValueChange = { inputGameName = it },
                        label = { Text("Game Name") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_game_name_input")
                    )
                    OutlinedTextField(
                        value = inputGameThumb,
                        onValueChange = { inputGameThumb = it },
                        label = { Text("Game Thumbnail Image URL") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_game_image_input")
                    )
                    Button(
                        onClick = {
                            if (inputGameName.isNotBlank()) {
                                AuraRepository.addGame(
                                    GameItem(
                                        id = "g_admin_" + System.currentTimeMillis(),
                                        name = inputGameName,
                                        thumbnail = inputGameThumb,
                                        category = "Admin Added",
                                        isFeatured = true
                                    )
                                )
                                adminLogText = "Successfully mapped game system config: $inputGameName"
                                inputGameName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        modifier = Modifier.fillMaxWidth().testTag("add_game_submit_btn")
                    ) {
                        Text("Add Game Dynamically", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Global Alert broadcast trigger button
        item {
            Text("🚨 COMMS ALERTS TRANSMITTER", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121620))) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            AuraRepository.broadcastGlobalNotification("System Scheduled Maintenance Warning", "ACT servers will update matchmaking protocols tomorrow at 04:00 UTC.")
                            adminLogText = "Broadcasting maintenance notification globally."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                        modifier = Modifier.weight(1f).testTag("alert_mnt_btn")
                    ) {
                        Text("Maint-Alert", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            AuraRepository.broadcastGlobalNotification("Esports League Final Match Signups Open", "Register custom team details for the retro arcade snake battle tomorrow.")
                            adminLogText = "Broadcasting tournaments notification globally."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007A)),
                        modifier = Modifier.weight(1f).testTag("alert_tournament_btn")
                    ) {
                        Text("Tournaments", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
//               PROFILE TAB SCREEN
// ==========================================

@Composable
fun ProfileTabScreen(
    currentUser: UserProfile,
    myPosts: List<Post>,
    onTriggerCreationMenu: () -> Unit
) {
    var chatMessageInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val chatMessages by AuraRepository.chatMessagesState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_view"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Cover & avatar profiles card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                border = BorderStroke(1.dp, Color(0xFF2B3245))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Cover photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        AsyncImage(model = currentUser.coverPhoto, contentDescription = "cover pc", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }

                    // Avatar photo floating
                    Box(
                        modifier = Modifier
                            .offset(y = (-40).dp)
                            .size(74.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF00E5FF), CircleShape)
                    ) {
                        AsyncImage(model = currentUser.profilePic, contentDescription = "profile pc", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }

                    Column(
                        modifier = Modifier.offset(y = (-30).dp).padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentUser.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("@${currentUser.username}", color = Color(0xFFBD00FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentUser.bio, color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Level gage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Aura Level: ${currentUser.level}", color = Color(0xFF00FF85), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("1,240 XP / 2,000 XP", color = Color.LightGray, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF2B3245))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.62f)
                                    .height(6.dp)
                                    .background(Color(0xFF00FF85))
                            )
                        }
                    }
                }
            }
        }

        // Stats rows counter elements
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121620))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        Pair("Followers", "${currentUser.followersCount}"),
                        Pair("Following", "${currentUser.followingCount}"),
                        Pair("Publications", "${currentUser.postsCount}"),
                        Pair("Score Likes", "${currentUser.likesCount}")
                    ).forEach { stateItem ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stateItem.second, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(stateItem.first, color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Live direct message simulator box in Profile!
        item {
            Text("💬 LIVE CHAT LOBBY CHANNEL (REAL-TIME)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Scrolling messages container
                    Box(modifier = Modifier.height(130.dp).fillMaxWidth().background(Color(0xFF0A0F1D)).padding(8.dp)) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(chatMessages) { chat ->
                                Text(
                                    "[${chat.senderName}]: ${chat.messageText}",
                                    color = if (chat.senderId == currentUser.id) Color(0xFF00E5FF) else Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Message typing row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessageInput,
                            onValueChange = { chatMessageInput = it },
                            placeholder = { Text("Send global lobby ping...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("chat_input_profile_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF))
                        )
                        Button(
                            onClick = {
                                if (chatMessageInput.isNotBlank()) {
                                    AuraRepository.sendChatMessage(chatMessageInput)
                                    chatMessageInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(48.dp).testTag("chat_send_profile_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "send message", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // User Inventories Cabinet from Firestore Sync
        item {
            val userInventories by AuraRepository.userInventoryState.collectAsStateWithLifecycle()
            Text("🎒 MY CLOUD CABINET & INVENTORIES (FIRESTORE SYNCED)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131E)),
                border = BorderStroke(1.dp, Color(0xFF00FF85).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (userInventories.isEmpty()) {
                        Text(
                            text = "Your digital cabinet is empty. Head to the Shop to buy products, or play Mini Games to claim virtual keys!",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        ) {
                            items(userInventories) { item ->
                                Card(
                                    modifier = Modifier.width(110.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171B26)),
                                    border = BorderStroke(1.dp, Color(0xFF2B3245))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        ) {
                                            AsyncImage(
                                                model = item.itemImage,
                                                contentDescription = "item image",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.itemName,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (item.itemType == "Product") "Product" else "Game Claimed",
                                            color = if (item.itemType == "Product") Color(0xFF00E5FF) else Color(0xFF00FF85),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sandbox System settings with switchers
        item {
            Text("⚙️ ACCESSIBLE EVALUATION SETTINGS", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF172030))) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Evaluate Permissions and Role Actions:", color = Color.LightGray, fontSize = 12.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { AuraRepository.switchUserRole(UserRole.NORMAL) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (currentUser.role == UserRole.NORMAL) Color(0xFF00E5FF) else Color(0xFF2B3245)),
                            modifier = Modifier.testTag("switch_to_normal_btn")
                        ) {
                            Text("Casual", color = if (currentUser.role == UserRole.NORMAL) Color.Black else Color.White, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { AuraRepository.switchUserRole(UserRole.COMMUNITY_MEMBER) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (currentUser.role == UserRole.COMMUNITY_MEMBER) Color(0xFF00E5FF) else Color(0xFF2B3245)),
                            modifier = Modifier.testTag("switch_to_member_btn")
                        ) {
                            Text("Member", color = if (currentUser.role == UserRole.COMMUNITY_MEMBER) Color.Black else Color.White, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { AuraRepository.switchUserRole(UserRole.ADMIN) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (currentUser.role == UserRole.ADMIN) Color(0xFFBD00FF) else Color(0xFF2B3245)),
                            modifier = Modifier.testTag("switch_to_admin_btn")
                        ) {
                            Text("Admin", color = Color.White, fontSize = 10.sp)
                        }
                    }

                    Divider(color = Color(0xFF2B3245), thickness = 0.5.dp)

                    Text("🔐 RUNTIME ENVIRONMENT SECRETS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val geminiStatus = AuraEnvConfig.getGeminiApiKey() != null
                        val firebaseKeyStatus = AuraEnvConfig.getFirebaseApiKey() != null
                        val firebaseProjStatus = AuraEnvConfig.getFirebaseProjectId() != null

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GEMINI AI KEY:", color = Color.Gray, fontSize = 9.sp)
                            Text(
                                text = if (geminiStatus) "Configured (${AuraEnvConfig.maskSecret(BuildConfig.GEMINI_API_KEY)})" else "Not Configured (Placeholder)",
                                color = if (geminiStatus) Color(0xFF00FF85) else Color(0xFFFF007A),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FIREBASE PROJ ID:", color = Color.Gray, fontSize = 9.sp)
                            Text(
                                text = AuraEnvConfig.maskSecret(try { BuildConfig.FIREBASE_PROJECT_ID } catch (e: Exception) { "" }),
                                color = if (firebaseProjStatus) Color(0xFF00FF85) else Color(0xFFFF007A),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FIREBASE KEY:", color = Color.Gray, fontSize = 9.sp)
                            Text(
                                text = AuraEnvConfig.maskSecret(try { BuildConfig.FIREBASE_API_KEY } catch (e: Exception) { "" }),
                                color = if (firebaseKeyStatus) Color(0xFF00FF85) else Color(0xFFFF007A),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "⚠️ Security Warning: I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. Do not share this APK file publicly or with unauthorized individuals to prevent potential misuse.",
                        color = Color(0xFFFFC107),
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Divider(color = Color(0xFF2B3245), thickness = 0.5.dp)

                    Button(
                        onClick = {
                            AuraRepository.resetToDefaults()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007A)),
                        modifier = Modifier.fillMaxWidth().testTag("sandbox_factory_reset_btn")
                    ) {
                        Text("Factory Reset Profile Sandbox Data", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

        // Floating "+" button - visible only for Admin and Community Members
        if (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.COMMUNITY_MEMBER) {
            FloatingActionButton(
                onClick = { onTriggerCreationMenu() },
                containerColor = Color(0xFFBD00FF),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("profile_create_fab"),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Content Option", modifier = Modifier.size(24.dp))
            }
        }
    }
}

// =========================================================================
//  COMMUNITY TAB SCREEN - INSTAGRAM + YOUTUBE + FACEBOOK COLLAGE HYBRID
// =========================================================================

@Composable
fun CommunityTabScreen(
    banners: List<HeroBanner>,
    creators: List<Creator>,
    products: List<Product>,
    newsList: List<NewsItem>,
    posts: List<Post>,
    videos: List<VideoItem>,
    liveStreams: List<LiveStreamItem>,
    currentUser: UserProfile,
    onClickCreator: (String) -> Unit,
    onClickProduct: (String) -> Unit,
    onClickViewAllStore: () -> Unit,
    onClickVideo: (String) -> Unit,
    onClickLive: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubTab by remember { mutableStateOf("All") } // All, Posts, Videos, Live, Trending, Following
    var activeCategoryTag by remember { mutableStateOf<String?>(null) } // Gaming, Hardware, Esports
    
    // Comment inputs map to simulate typing on inline post containers
    var inlineCommentInputs = remember { mutableStateMapOf<String, String>() }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080B10))
    ) {
        // 1. NEON GAMER SEARCH BAR HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Posts, Videos, Live streams, Tags...", fontSize = 12.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search icon", tint = Color.LightGray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "clear search", tint = Color.LightGray)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("community_search_bar_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF2B3245),
                    focusedContainerColor = Color(0xFF121620),
                    unfocusedContainerColor = Color(0xFF0A0F1D)
                ),
                singleLine = true
            )
            
            // Neon category shorthand filter
            Box(
                modifier = Modifier
                    .background(
                        if (activeCategoryTag != null) Color(0xFFFF007A) else Color(0xFF121620),
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, Color(0xFFBD00FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        activeCategoryTag = if (activeCategoryTag == null) "Gaming" else null
                        Toast
                            .makeText(
                                context,
                                if (activeCategoryTag != null) "Filtered by #Gaming" else "Removed tag filter",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text("#Gaming", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 2. SOCIAL MEDIA MATRIX NAVIGATION TABS (All, Posts, Videos, Live, Trending, Following)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val subTabs = listOf("All", "Posts", "Videos", "Live", "Trending", "Following")
            subTabs.forEach { tab ->
                val isSelected = selectedSubTab == tab
                val glowColor = when (tab) {
                    "Live" -> Color(0xFFFF007A)
                    "Videos" -> Color(0xFF00E5FF)
                    "Trending" -> Color(0xFF00FF85)
                    else -> Color(0xFFBD00FF)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) glowColor else Color(0xFF121620))
                        .border(
                            1.dp,
                            if (isSelected) Color.White else Color(0xFF2B3245),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedSubTab = tab }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("subtab_$tab")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (tab == "Live") {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                        Text(
                            text = tab.uppercase(),
                            color = if (isSelected) Color.Black else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. RENDER CORE CHANNELS FEED
        val filteredPosts = posts.filter {
            (searchQuery.isEmpty() || it.content.contains(searchQuery, ignoreCase = true)) &&
            (activeCategoryTag == null || it.content.contains(activeCategoryTag!!, ignoreCase = true))
        }

        val filteredVideos = videos.filter {
            (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)) &&
            (activeCategoryTag == null || it.title.contains(activeCategoryTag!!, ignoreCase = true) || it.description.contains(activeCategoryTag!!, ignoreCase = true))
        }

        val filteredLive = liveStreams.filter {
            (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)) &&
            (activeCategoryTag == null || it.category.contains(activeCategoryTag!!, ignoreCase = true))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp, start = 14.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // --- TAB ALL ---
            if (selectedSubTab == "All") {
                // Horizontal live broadcasts bar
                if (filteredLive.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔴 PRO BROADCASTING", color = Color(0xFFFF007A), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF007A))
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filteredLive) { stream ->
                                Card(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .clickable { onClickLive(stream.id) },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                                    border = BorderStroke(1.dp, Color(0xFFFF007A).copy(alpha = 0.5f))
                                ) {
                                    Box {
                                        AsyncImage(
                                            model = stream.thumbnail,
                                            contentDescription = "stream thumb",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp)
                                                .background(Color(0xFFFF007A), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("👁️ ${stream.viewerCount}", color = Color.White, fontSize = 8.sp)
                                        }
                                    }
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(stream.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("by ${stream.creatorName}", color = Color.LightGray, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Pro Gaming Creators Scroll
                item {
                    Text("🔥 GAMING CHIEFTAINS / CREATORS", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(creators) { cr ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable { onClickCreator(cr.id) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (cr.isFollowed) Color(0xFF00E5FF) else Color(0xFFBD00FF), CircleShape)
                                ) {
                                    AsyncImage(model = cr.profilePic, contentDescription = "cr", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(cr.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (cr.isFollowed) "Following" else "+ Follow", color = if (cr.isFollowed) Color(0xFF00FF85) else Color.LightGray, fontSize = 8.sp)
                            }
                        }
                    }
                }

                // Mixed layout units
                item {
                    Text("📢 UNIFIED GAME CHANNELS FEED", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                items(filteredPosts) { post ->
                    CommunityPostItemCard(
                        post = post,
                        inlineCommentInputs = inlineCommentInputs,
                        currentUser = currentUser,
                        onClickCreator = onClickCreator
                    )
                }
            }

            // --- TAB POSTS ---
            if (selectedSubTab == "Posts") {
                if (filteredPosts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No posts available matching filters.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
                items(filteredPosts) { post ->
                    CommunityPostItemCard(
                        post = post,
                        inlineCommentInputs = inlineCommentInputs,
                        currentUser = currentUser,
                        onClickCreator = onClickCreator
                    )
                }
            }

            // --- TAB VIDEOS ---
            if (selectedSubTab == "Videos") {
                if (filteredVideos.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No gameplay replays or video highlights yet.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    // YouTube-Style Dual Column Grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            filteredVideos.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { video ->
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onClickVideo(video.id) },
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                                            border = BorderStroke(1.dp, Color(0xFF2B3245))
                                        ) {
                                            Column {
                                                Box {
                                                    AsyncImage(
                                                        model = video.thumbnail,
                                                        contentDescription = "thumbnail",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(100.dp),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .padding(4.dp)
                                                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(video.duration, color = Color.White, fontSize = 9.sp)
                                                    }
                                                    if (video.isYoutubeImport) {
                                                        Box(
                                                            modifier = Modifier
                                                                .align(Alignment.TopStart)
                                                                .padding(4.dp)
                                                                .background(Color.Red, RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text("YOUTUBE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text(video.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("by ${video.creatorName}", color = Color.LightGray, fontSize = 9.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Text("🤍 ${video.likes}", color = Color.Gray, fontSize = 9.sp)
                                                        Text("💬 ${video.comments.size}", color = Color.Gray, fontSize = 9.sp)
                                                    }
                                                }
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

            // --- TAB LIVE ---
            if (selectedSubTab == "Live") {
                if (filteredLive.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF171B26)),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No Active Broadcasters", color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Go to your profile settings and click '+' to start broadcasting a simulated live loop now!", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(filteredLive) { stream ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClickLive(stream.id) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                            border = BorderStroke(1.dp, Color(0xFFFF007A))
                        ) {
                            Box {
                                AsyncImage(
                                    model = stream.thumbnail,
                                    contentDescription = "poster",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .background(Color(0xFFFF007A), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("LIVE NOW", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Viewer Spectators: ${stream.viewerCount}", color = Color.White, fontSize = 10.sp)
                                }
                            }
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(stream.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Category: ${stream.category}", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stream.description, color = Color.LightGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }

            // --- TAB TRENDING ---
            if (selectedSubTab == "Trending") {
                // Sort by likes descending
                val popularPosts = posts.sortedByDescending { it.likes }.take(3)
                val popularVideos = videos.sortedByDescending { it.likes }.take(3)

                item {
                    Text("🔥 MOST LIKED VIDEOS", color = Color(0xFF00FF85), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                items(popularVideos) { video ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickVideo(video.id) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box {
                                AsyncImage(
                                    model = video.thumbnail,
                                    contentDescription = "th",
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.Black, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("🔥 #${video.likes} likes", color = Color(0xFF00FF85), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("by ${video.creatorName}", color = Color.LightGray, fontSize = 11.sp)
                                Text(video.description, color = Color.Gray, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                item {
                    Text("🔥 HIGHEST ENGAGEMENT DISCUSSIONS", color = Color(0xFF00FF85), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                items(popularPosts) { post ->
                    CommunityPostItemCard(
                        post = post,
                        inlineCommentInputs = inlineCommentInputs,
                        currentUser = currentUser,
                        onClickCreator = onClickCreator
                    )
                }
            }

            // --- TAB FOLLOWING ---
            if (selectedSubTab == "Following") {
                val followedCreatorNames = creators.filter { it.isFollowed }.map { it.name }
                val followedPosts = posts.filter { followedCreatorNames.contains(it.authorName) }
                
                if (followedPosts.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF171B26)),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Your Timeline is Quiet", color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("You are not currently following any creators who have active publications. Tap followed creators in our chieftains banner above to populate details!", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(followedPosts) { post ->
                        CommunityPostItemCard(
                            post = post,
                            inlineCommentInputs = inlineCommentInputs,
                            currentUser = currentUser,
                            onClickCreator = onClickCreator
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityPostItemCard(
    post: Post,
    inlineCommentInputs: MutableMap<String, String>,
    currentUser: UserProfile,
    onClickCreator: (String) -> Unit
) {
    val context = LocalContext.current
    var isCommentsExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
        border = BorderStroke(1.dp, Color(0xFF2B3245))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                // Find creator and click
                                AuraRepository.creatorsState.value
                                    .find { it.name == post.authorName }
                                    ?.let { onClickCreator(it.id) }
                            }
                    ) {
                        AsyncImage(model = post.authorAvatar, contentDescription = "av", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable {
                                    AuraRepository.creatorsState.value
                                        .find { it.name == post.authorName }
                                        ?.let { onClickCreator(it.id) }
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (post.authorRole == UserRole.ADMIN) {
                                Text("ADMIN", color = Color(0xFFFF007A), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("@author", color = Color.Gray, fontSize = 10.sp)
                    }
                }

                // Delete or pin action context menu for moderate role
                if (currentUser.role == UserRole.ADMIN) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {
                            AuraRepository.pinPostToggle(post.id)
                            Toast.makeText(context, if (post.isPinned) "Post unpinned!" else "Post pinned to top!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Pin Content",
                                tint = if (post.isPinned) Color(0xFF00E5FF) else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = {
                            AuraRepository.deletePost(post.id)
                            Toast.makeText(context, "Post deleted by Admin moderate", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Content", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(post.content, color = Color.White, fontSize = 13.sp)

            post.image?.let { img ->
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(model = img, contentDescription = "post graphic", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action bars (Likes, comments indicators)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.clickable { AuraRepository.toggleLikePost(post.id) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "like",
                            tint = if (post.isLiked) Color.Red else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("${post.likes}", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier.clickable { isCommentsExpanded = !isCommentsExpanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Comment, contentDescription = "comment", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        Text("${post.comments.size}", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
                
                if (post.isPinned) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.PushPin, contentDescription = "pinned icon", tint = Color(0xFF00FF85), modifier = Modifier.size(14.dp))
                        Text("PINNED", color = Color(0xFF00FF85), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Inline comment system expandable drawer
            if (isCommentsExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color(0xFF2B3245), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.comments.forEach { comment ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(comment.authorName, color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(comment.commentText, color = Color.White, fontSize = 11.sp)
                        }
                    }

                    if (post.comments.isEmpty()) {
                        Text("No comments yet. Be first to comment details!", color = Color.Gray, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Comment input textfield
                    val commentText = inlineCommentInputs[post.id] ?: ""
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { inlineCommentInputs[post.id] = it },
                            placeholder = { Text("Write comment...", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF)),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    AuraRepository.addCommentToPost(post.id, commentText)
                                    inlineCommentInputs[post.id] = ""
                                    Toast.makeText(context, "Comment submitted!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Send", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreationOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// =========================================================================
//  CREATOR PROFILE SCREEN - GIGANTIC TABS DETAIL SCREEN WITH CONTENT CODES
// =========================================================================

@Composable
fun CreatorProfileScreen(
    creatorId: String,
    onClose: () -> Unit,
    currentUser: UserProfile
) {
    val creators by AuraRepository.creatorsState.collectAsStateWithLifecycle()
    val posts by AuraRepository.postsState.collectAsStateWithLifecycle()
    val videos by AuraRepository.videosState.collectAsStateWithLifecycle()

    val cr = creators.find { it.id == creatorId } ?: return

    var selectedCreatorTab by remember { mutableStateOf("Posts") } // Posts, Videos, Live Replays, About

    val creatorPosts = posts.filter { it.authorName == cr.name }
    val creatorVideos = videos.filter { it.creatorId == cr.id && !it.title.contains("[LIVE REPLAY]") }
    val creatorReplays = videos.filter { it.creatorId == cr.id && it.title.contains("[LIVE REPLAY]") }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080B10))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Cover Photo & Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = if (cr.banner.isNotBlank()) cr.banner else "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=600",
                    contentDescription = "cover banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = Color.White)
                }
            }

            // 2. Avatar profile header row
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-30).dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFFBD00FF), CircleShape)
                ) {
                    AsyncImage(model = cr.profilePic, contentDescription = "logo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                
                Column(modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)) {
                    Text(cr.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("@${cr.name.lowercase().replace(" ", "")}", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 3. Stats section block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-15).dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    Pair("Followers", "${cr.followers}"),
                    Pair("Posts", "${creatorPosts.size}"),
                    Pair("Videos", "${creatorVideos.size}"),
                    Pair("Replays", "${creatorReplays.size}")
                ).forEach { stat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stat.second, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(stat.first, color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }

            // Bio description
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(cr.bio, color = Color.LightGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Follow / Send Message CTA buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            AuraRepository.toggleFollowCreator(cr.id)
                            Toast.makeText(context, if (cr.isFollowed) "Unfollowed ${cr.name}" else "Now following ${cr.name}!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (cr.isFollowed) Color(0xFF1D2230) else Color(0xFFBD00FF)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (cr.isFollowed) "Following" else "Follow Creator", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. SUB-TABS (Posts, Videos, Live Replays, About)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Posts", "Videos", "Live Replays", "About").forEach { tabName ->
                    val isTabSelected = selectedCreatorTab == tabName
                    Box(
                        modifier = Modifier
                            .clickable { selectedCreatorTab = tabName }
                            .padding(vertical = 8.dp)
                            .border(
                                width = if (isTabSelected) 1.5.dp else 0.dp,
                                color = if (isTabSelected) Color(0xFF00E5FF) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                color = if (isTabSelected) Color(0xFF00E5FF).copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            tabName,
                            color = if (isTabSelected) Color(0xFF00E5FF) else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFF2B3245), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Tab layouts container
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                when (selectedCreatorTab) {
                    "Posts" -> {
                        if (creatorPosts.isEmpty()) {
                            Text("No posts published yet by this creator.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                        } else {
                            creatorPosts.forEach { post ->
                                CommunityPostItemCard(
                                    post = post,
                                    inlineCommentInputs = remember { mutableStateMapOf() },
                                    currentUser = currentUser,
                                    onClickCreator = {}
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                    "Videos" -> {
                        if (creatorVideos.isEmpty()) {
                            Text("No video highlights uploaded by this creator.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                        } else {
                            creatorVideos.forEach { video ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        AsyncImage(model = video.thumbnail, contentDescription = "v", modifier = Modifier.size(70.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                                        Column {
                                            Text(video.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(video.duration, color = Color(0xFF00E5FF), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Live Replays" -> {
                        if (creatorReplays.isEmpty()) {
                            Text("No finished broadcast logs saved yet.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                        } else {
                            creatorReplays.forEach { video ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1220)),
                                    border = BorderStroke(1.dp, Color(0xFFFF007A).copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        AsyncImage(model = video.thumbnail, contentDescription = "v", modifier = Modifier.size(70.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                                        Column {
                                            Text(video.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Live session replay saved", color = Color(0xFFFF007A), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "About" -> {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121620))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("CREATOR CREDENTIALS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Bio Details: ${cr.bio}", color = Color.LightGray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Verified ACT Community Creator since June 2026.", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// =========================================================================
//  YOUTUBE-STYLE VIDEO PLAYER SCREEN OVERLAY (CONTROLS, PIP, COMMENTS)
// =========================================================================

@Composable
fun VideoPlayerScreen(
    videoId: String,
    onClose: () -> Unit,
    onTriggerPip: () -> Unit
) {
    val videos by AuraRepository.videosState.collectAsStateWithLifecycle()
    val video = videos.find { it.id == videoId } ?: return

    var isPaused by remember { mutableStateOf(false) }
    var speedSetting by remember { mutableStateOf("1.0x") }
    var qualitySetting by remember { mutableStateOf("1080p") }
    var currentProgressSeconds by remember { mutableStateOf(44) }
    var volumeScale by remember { mutableStateOf(0.85f) }
    var showingSettingsDrawer by remember { mutableStateOf(false) }

    var draftComment by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Audio/Video player loop timer simulator
    LaunchedEffect(isPaused) {
        while (!isPaused) {
            delay(1000)
            if (currentProgressSeconds < 340) {
                currentProgressSeconds += 1
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. YouTube style responsive black viewport video player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Color(0xFF030303))
            ) {
                // Background visual poster
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = "visual track",
                    modifier = Modifier.fillMaxSize().alpha(if (isPaused) 0.5f else 0.85f),
                    contentScale = ContentScale.Crop
                )

                // Seek bar / Title bar overlay controls
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: Title bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = onClose) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = Color.White)
                            }
                            Text(video.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(180.dp))
                        }
                        
                        Row {
                            IconButton(onClick = onTriggerPip) {
                                Icon(Icons.Default.PictureInPicture, contentDescription = "PiP Miniaturize", tint = Color.White)
                            }
                            IconButton(onClick = { showingSettingsDrawer = !showingSettingsDrawer }) {
                                Icon(Icons.Default.Settings, contentDescription = "Preferences", tint = Color.White)
                            }
                        }
                    }

                    // Center: Large Play/Pause Toggle Indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(54.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable { isPaused = !isPaused },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "play-pause-toggle",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Bottom: Seekbar layout
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val curMins = String.format("%02d:%02d", currentProgressSeconds / 60, currentProgressSeconds % 60)
                            Text(curMins, color = Color.White, fontSize = 9.sp)
                            Text(video.duration, color = Color.LightGray, fontSize = 9.sp)
                        }
                        // Simple seek slider trace
                        Slider(
                            value = currentProgressSeconds.toFloat() / 340f,
                            onValueChange = { currentProgressSeconds = (it * 340f).toInt() },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E5FF),
                                activeTrackColor = Color(0xFF00E5FF),
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.height(14.dp)
                        )
                    }
                }
            }

            // 2. Details drawers below (Title, creator badge, description details, comment feeds)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF080B10))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Volume Adjuster row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (volumeScale == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "volume",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Volume: ${(volumeScale * 100).toInt()}%", color = Color.LightGray, fontSize = 10.sp)
                    Slider(
                        value = volumeScale,
                        onValueChange = { volumeScale = it },
                        modifier = Modifier.weight(1f).height(12.dp),
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFBD00FF), activeTrackColor = Color(0xFFBD00FF))
                    )
                }

                Text(video.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                // Creator Detail block
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121620))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(34.dp).clip(CircleShape)) {
                                AsyncImage(model = video.creatorAvatar, contentDescription = "av", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                            Column {
                                Text(video.creatorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Pro Creator", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = {
                                AuraRepository.toggleLikeVideo(video.id)
                                Toast.makeText(context, "Acknowledged matching feedback!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("💖 LIKE VIDEO (${video.likes})", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                // Expandable Description Info Card
                Column {
                    Text("Description Info:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(video.description, color = Color.Gray, fontSize = 12.sp)
                }

                Divider(color = Color(0xFF2B3245), thickness = 0.5.dp)

                // 3. Comments block section
                Text("💬 DISCUSSION BOARD (${video.comments.size})", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                
                // Add commentary row
                if (video.allowComments) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = draftComment,
                            onValueChange = { draftComment = it },
                            placeholder = { Text("Post community feedback...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFBD00FF)),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (draftComment.isNotBlank()) {
                                    AuraRepository.commentOnVideo(video.id, draftComment)
                                    draftComment = ""
                                    Toast.makeText(context, "Opinion saved!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF))
                        ) {
                            Text("Post")
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1D1620), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Interactive comments are disabled by the moderate publisher on this release.", color = Color.LightGray, fontSize = 12.sp)
                    }
                }

                // Render video comment lists
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    video.comments.forEach { comment ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D))) {
                            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray)) {
                                    AsyncImage(model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=150", contentDescription = "av", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                }
                                Column {
                                    Text(comment.authorName, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(comment.commentText, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Configuration Preferences Settings Drawer
        if (showingSettingsDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showingSettingsDrawer = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF))
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("VIDEO OPTIONS CONFIGURATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        // Select playback speeds
                        Text("Playback Speed Selection:", color = Color.LightGray, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("0.5x", "1.0x", "1.5x", "2.0x").forEach { spd ->
                                val active = speedSetting == spd
                                Box(
                                    modifier = Modifier
                                        .background(if (active) Color(0xFF00E5FF) else Color(0xFF1D2230), RoundedCornerShape(4.dp))
                                        .clickable { speedSetting = spd }
                                        .padding(8.dp)
                                ) {
                                    Text(spd, color = if (active) Color.Black else Color.White, fontSize = 11.sp)
                                }
                            }
                        }

                        // Select HD resolution quality options
                        Text("Select Stream Quality:", color = Color.LightGray, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1080p", "720p", "480p").forEach { res ->
                                val active = qualitySetting == res
                                Box(
                                    modifier = Modifier
                                        .background(if (active) Color(0xFFBD00FF) else Color(0xFF1D2230), RoundedCornerShape(4.dp))
                                        .clickable { qualitySetting = res }
                                        .padding(8.dp)
                                ) {
                                    Text(res, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showingSettingsDrawer = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirm Preferences", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
//  LIVE STREAM BROADCAST VIEWPORT AND CHAT CLIENT (hearts, end controls)
// =========================================================================

@Composable
fun LiveStreamPlayerScreen(
    streamId: String,
    onClose: () -> Unit,
    currentUser: UserProfile
) {
    val streams by AuraRepository.liveStreamsState.collectAsStateWithLifecycle()
    val stream = streams.find { it.id == streamId } ?: return

    var inputChatMsg by remember { mutableStateOf("") }
    var reactionsCount by remember { mutableStateOf(0) }
    
    // Heart coordinates list for beautiful floating animations
    val floatingHeartsCoordinates = remember { mutableStateListOf<Pair<Float, Float>>() }

    val context = LocalContext.current

    // Simulating background viewer commenting flow in live stream
    LaunchedEffect(Unit) {
        val listMsgs = listOf(
            "NO WAY! The keyboard testing speed is crisp!",
            "Chroma gears are sick tbh.",
            "Can we moderate the keyboard inputs?",
            "NINJA is online on next match",
            "This co-op arena is legendary!",
            "OMG! PENTA KILL!"
        )
        while (stream.isLive) {
            delay(5000)
            val randomText = listMsgs.random()
            val sender = listOf("EliteGamer99", "DuckyCaps", "ChromaGlow", "AeroVans").random()
            
            // Append random commenter
            AuraRepository.addLiveChatMessage(
                streamId = streamId,
                content = "[$sender]: $randomText"
            )
            
            // Randomly flash hearts coordinates
            val randX = (20..150).random().toFloat()
            val randY = (150..340).random().toFloat()
            floatingHeartsCoordinates.add(Pair(randX, randY))
            if (floatingHeartsCoordinates.size > 8) {
                floatingHeartsCoordinates.removeAt(0)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Live camera viewfinder video overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFF020202))
            ) {
                AsyncImage(model = stream.thumbnail, contentDescription = "viewfinder", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

                // Render pulsing RED indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF007A), RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                                Text("LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👁️ ${stream.viewerCount} watching", color = Color.White, fontSize = 10.sp)
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "close", tint = Color.White)
                    }
                }

                // Creator Control overlay if creator matches
                val isOwner = currentUser.id == stream.creatorId
                if (isOwner && stream.isLive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Button(
                            onClick = {
                                AuraRepository.endLiveStream(stream.id)
                                Toast.makeText(context, "Stream Converted & Replay video published!", Toast.LENGTH_LONG).show()
                                onClose()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("END STREAM (SAVE TO VIDEOS REPLAY)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (!stream.isLive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔴 BROADCAST FINISHED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Saved as Replay Video in community feed catalog.", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }

                // Render floating animated coordinate hearts!
                floatingHeartsCoordinates.forEach { coords ->
                    Box(
                        modifier = Modifier
                            .offset(x = coords.first.dp, y = coords.second.dp)
                            .size(20.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "floating heart", tint = Color(0xFFFF007A))
                    }
                }
            }

            // 2. Stream titles details with live chat messages scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF0F121C))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stream.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Category: ${stream.category}", color = Color(0xFFFF007A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1D2230), CircleShape)
                            .clickable {
                                reactionsCount += 1
                                floatingHeartsCoordinates.add(Pair((40..170).random().toFloat(), (120..220).random().toFloat()))
                                Toast.makeText(context, "Reaction sent!", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "heart pulse", tint = Color(0xFFFF007A))
                    }
                }

                // Admin block / Pin controls
                if (currentUser.role == UserRole.ADMIN) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF22161A))) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("STREAM CHAT MODERATION:", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        AuraRepository.pinLiveMessage(stream.id, "Follow stream guidelines or get kicked by ACT community moderators!")
                                        Toast.makeText(context, "Guidelines pinned to stream!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Pin Rules", fontSize = 9.sp, color = Color.Black)
                                }
                                Button(
                                    onClick = {
                                        AuraRepository.muteLiveChatToggle(stream.id)
                                        Toast.makeText(context, "Mute toggle action applied!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007A)),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Toggle Mute", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }

                // Live stream pinned message box
                stream.pinnedMessage?.let { pin ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF172030), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PushPin, contentDescription = "pin", tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                            Text(pin, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // List of Chat commentary
                Text("🔴 LIVE DISCUSSION FEED:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF0A0F1D))
                        .padding(10.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(stream.liveChat) { chatComment ->
                            Text(
                                text = if (chatComment.authorName.contains("[")) chatComment.commentText else "[${chatComment.authorName}]: ${chatComment.commentText}",
                                color = if (chatComment.authorName == currentUser.displayName) Color(0xFFFF007A) else Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Chat typing input box
                if (stream.isLive && !stream.isMutedChat) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputChatMsg,
                            onValueChange = { inputChatMsg = it },
                            placeholder = { Text("Click to chat on live stream...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF007A)),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (inputChatMsg.isNotBlank()) {
                                    AuraRepository.addLiveChatMessage(
                                        streamId = stream.id,
                                        content = "[${currentUser.displayName}]: $inputChatMsg"
                                    )
                                    inputChatMsg = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007A)),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "send chat", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else if (stream.isMutedChat) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF22161A), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text("Interactive comments have been moderated/muted on this stream.", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
