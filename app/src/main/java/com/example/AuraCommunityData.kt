package com.example

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

// --- Models ---

data class UserInventoryItem(
    val id: String = "",
    val userId: String = "",
    val itemName: String = "",
    val itemType: String = "", // "Product" or "Game"
    val itemImage: String = "",
    val itemPrice: Double = 0.0,
    val purchaseDate: String = ""
)

enum class UserRole {
    NORMAL, COMMUNITY_MEMBER, ADMIN
}

data class UserProfile(
    val id: String = "user_1",
    var email: String = "member@gmail.com",
    var displayName: String = "Noob Slayer",
    var username: String = "noob_slayer_act",
    var bio: String = "Pro Gamer. Rocket League Grand Champion. Keyboard collector.",
    var profilePic: String = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?q=80&w=200",
    var coverPhoto: String = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=600",
    var birthDate: String = "2000-01-01",
    var age: Int = 26,
    var country: String = "United States",
    var interests: List<String> = listOf("Gaming Laptops", "Gaming Keyboards", "Mini Games"),
    var role: UserRole = UserRole.COMMUNITY_MEMBER,
    var level: Int = 14,
    var followingCount: Int = 158,
    var followersCount: Int = 2420,
    var postsCount: Int = 6,
    var likesCount: Int = 1120,
    var isBanned: Boolean = false,
    var isOnboarded: Boolean = true
)

data class HeroBanner(
    val id: String,
    val image: String,
    val headline: String,
    val description: String,
    val ctaText: String,
    val targetLink: String
)

data class Creator(
    val id: String,
    val name: String,
    val profilePic: String,
    val bio: String,
    val followers: Int,
    val isFollowed: Boolean = false,
    val banner: String = ""
)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val rating: Float,
    val image: String,
    val category: String,
    val isWishlisted: Boolean = false
)

data class NewsItem(
    val id: String,
    val title: String,
    val description: String,
    val image: String,
    val publishDate: String
)

data class Comment(
    val id: String,
    val authorName: String,
    val commentText: String,
    val timestamp: String
)

data class Post(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val image: String? = null,
    val videoUrl: String? = null,
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val shares: Int = 0,
    val authorRole: UserRole = UserRole.COMMUNITY_MEMBER,
    var isLiked: Boolean = false,
    val isPinned: Boolean = false
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val messageText: String,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameItem(
    val id: String,
    val name: String,
    val thumbnail: String,
    val category: String, // "Offline Games" or "Online Games" or "Admin Added"
    val url: String = "",
    val isFeatured: Boolean = false,
    val isHidden: Boolean = false
)

data class AppNotification(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "post", "shop", "game", "chat", "news", "system"
    val timestamp: String
)

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnail: String,
    val duration: String,
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String,
    val views: Int = 0,
    val uploadDate: String = "Just now",
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val shares: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isPinned: Boolean = false,
    val isFeatured: Boolean = false,
    val isCommentsDisabled: Boolean = false,
    val isYoutubeImport: Boolean = false,
    val visibility: String = "Public", // Public, Private, Unlisted
    val allowComments: Boolean = true
)

data class LiveStreamItem(
    val id: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String,
    val viewerCount: Int = 0,
    val isLive: Boolean = true,
    val isMutedChat: Boolean = false,
    val pinnedMessage: String? = null,
    val visibility: String = "Public", // Public, Private, Members Only
    val liveChat: List<Comment> = emptyList()
)

// --- Central Gaming Community State Repository ---
// Mimics a real-time reactive database state

object AuraRepository {

    // Active logged in user
    private val _currentUserState = MutableStateFlow<UserProfile>(UserProfile())
    val currentUserState: StateFlow<UserProfile> = _currentUserState.asStateFlow()

    // Banners state
    private val _bannersState = MutableStateFlow<List<HeroBanner>>(emptyList())
    val bannersState: StateFlow<List<HeroBanner>> = _bannersState.asStateFlow()

    // Creators state
    private val _creatorsState = MutableStateFlow<List<Creator>>(emptyList())
    val creatorsState: StateFlow<List<Creator>> = _creatorsState.asStateFlow()

    // Products state
    private val _productsState = MutableStateFlow<List<Product>>(emptyList())
    val productsState: StateFlow<List<Product>> = _productsState.asStateFlow()

    // News state
    private val _newsState = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsState: StateFlow<List<NewsItem>> = _newsState.asStateFlow()

    // Posts state
    private val _postsState = MutableStateFlow<List<Post>>(emptyList())
    val postsState: StateFlow<List<Post>> = _postsState.asStateFlow()

    // Chat room messaging (Simulates real-time peer-to-peer/Global channels)
    private val _chatMessagesState = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessagesState: StateFlow<List<ChatMessage>> = _chatMessagesState.asStateFlow()

    // Games Hub
    private val _gamesState = MutableStateFlow<List<GameItem>>(emptyList())
    val gamesState: StateFlow<List<GameItem>> = _gamesState.asStateFlow()

    // Notification Queue
    private val _notificationsState = MutableStateFlow<List<AppNotification>>(emptyList())
    val notificationsState: StateFlow<List<AppNotification>> = _notificationsState.asStateFlow()

    // Database of other users (for admin management)
    private val _usersListState = MutableStateFlow<List<UserProfile>>(emptyList())
    val usersListState: StateFlow<List<UserProfile>> = _usersListState.asStateFlow()

    // Videos state
    private val _videosState = MutableStateFlow<List<VideoItem>>(emptyList())
    val videosState: StateFlow<List<VideoItem>> = _videosState.asStateFlow()

    // Live streams state
    private val _liveStreamsState = MutableStateFlow<List<LiveStreamItem>>(emptyList())
    val liveStreamsState: StateFlow<List<LiveStreamItem>> = _liveStreamsState.asStateFlow()

    // User inventories state (syncs with Firestore)
    private val _userInventoryState = MutableStateFlow<List<UserInventoryItem>>(emptyList())
    val userInventoryState: StateFlow<List<UserInventoryItem>> = _userInventoryState.asStateFlow()

    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("AuraFirestore", "Firebase is not initialized or configured: ${e.message}")
            null
        }
    }

    init {
        resetToDefaults()
        setupFirestoreSync()
    }

    fun resetToDefaults() {
        // Clear and initialize
        _currentUserState.value = UserProfile(
            id = "user_1",
            email = "noobslayer@gmail.com",
            displayName = "Noob Slayer",
            username = "noob_slayer_act",
            bio = "Master rank Apex Legends, keyboard design enthusiast & game collector.",
            birthDate = "2000-05-15",
            age = 26,
            country = "United States",
            interests = listOf("Gaming Laptops", "Gaming Keyboards"),
            role = UserRole.COMMUNITY_MEMBER,
            isOnboarded = true
        )

        _bannersState.value = listOf(
            HeroBanner(
                id = "b1",
                image = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800",
                headline = "Cyberpunk 2077 Update Live!",
                description = "Experience next-gen ray tracing overclocked directly inside the update, now featured for free the entire week.",
                ctaText = "Read Patch News",
                targetLink = "news_cyberpunk"
            ),
            HeroBanner(
                id = "b2",
                image = "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800",
                headline = "VCT League Champions Crowned",
                description = "Catch-up on the final esports match highlight, team standings, and gear checklists used on stage.",
                ctaText = "Catch Finals Highlights",
                targetLink = "news_vct"
            )
        )

        _creatorsState.value = listOf(
            Creator("c1", "NinjaVibe", "https://images.unsplash.com/photo-1560250097-0b93528c311a?q=80&w=200", "Top FPS player. Streaming every Saturday at 8 PM.", 45000, false, "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=600"),
            Creator("c2", "ShroudClone", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200", "Tactical FPS shooter expert. 100% headshot accuracy guaranteed.", 89300, true, "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=600"),
            Creator("c3", "PokimaneACT", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200", "Just chat, indie game streaming & mechanical keyboard unboxing.", 23500, false),
            Creator("c4", "ValkyraeGaming", "https://images.unsplash.com/photo-1580489944761-15a19d654956?q=80&w=200", "Content creator. Playing retro, platformers & ludo board games.", 72100, false)
        )

        _productsState.value = listOf(
            Product("p1", "Quantum Neon Pro Laptop", "17.3\" 240Hz screen, Intel Core i9, RTX 4090, 32GB DDR5.", 2499.00, 4.9f, "https://images.unsplash.com/photo-1603302576837-37561b2e2302?q=80&w=400", "Gaming Laptops"),
            Product("p2", "ACT Cyber Shell Phone", "OLED 144Hz, Snapdragon Gen 3, active aero-cooler built-in.", 899.00, 4.7f, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?q=80&w=400", "Gaming Phones"),
            Product("p3", "Neon Chroma Mechanical Keyboard", "Gasket mount, pre-lubed tactile switches, PBT double-shot keycaps.", 149.99, 4.8f, "https://images.unsplash.com/photo-1587829741301-dc798b83add3?q=80&w=400", "Gaming Keyboards"),
            Product("p4", "Apex Drift Wireless Mouse", "54g lightweight shell, 3395 raw optical sensor, 8000Hz polling rate.", 89.00, 4.5f, "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?q=80&w=400", "Gaming Mouse"),
            Product("p5", "Synthesizer Pro Sound Headset", "Planar magnetic drivers, ANC microphone, USB-C direct lossless.", 199.00, 4.4f, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=400", "Gaming Headsets"),
            Product("p6", "Tactile Grip Pro Controller", "Hall effect joysticks, trigger stops, mappable paddles.", 69.50, 4.6f, "https://images.unsplash.com/photo-1531525645387-7f14be1bdbbd?q=80&w=400", "Gaming Controllers"),
            Product("p7", "ACT Cyber Throne Ergonomic", "High-density cold foam, adjustable neck and lumbar, memory foam pads.", 349.00, 4.8f, "https://images.unsplash.com/photo-1598550476439-6847785fce6e?q=80&w=400", "Gaming Chairs"),
            Product("p8", "Chroma RGB Desk Deskpad", "900 x 400 x 4mm, waterproof mesh, customizable strip.", 35.00, 4.2f, "https://images.unsplash.com/photo-1616440347437-b1c73416efc2?q=80&w=400", "Gaming Accessories")
        )

        _newsState.value = listOf(
            NewsItem("n1", "PlayStation 6 Rumors Intensify", "Leading analysts leak hardware specifications targeting 8K 60FPS gaming using deep AI graphic rendering algorithms.", "https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?q=80&w=400", "June 20, 2026"),
            NewsItem("n2", "GTA 6 Final Trailer Dropped!", "Rockstar games takes down YouTube records within hours of detailing dynamic weather cycles and responsive AI NPCs.", "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=400", "June 18, 2026"),
            NewsItem("n3", "Steam Summer Indie Fest Begins", "Discover over 2,000 community curated gameplay demos with retro, card deckbuilders, and cyberpunk settings.", "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=400", "June 15, 2026")
        )

        _postsState.value = listOf(
            Post(
                id = "post1",
                authorName = "Noob Slayer",
                authorAvatar = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?q=80&w=200",
                content = "Just unboxed the ACT Cyber Shell Phone and it's insane! Easily hitting 144FPS stable on Apex Legends matches without any heat throttling. Strongly recommended for anyone gaming in the summer!",
                image = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?q=80&w=600",
                likes = 45,
                comments = listOf(
                    Comment("comment1", "CyberPunkKid", "Wow, that phone is a beast! How is the battery life while streaming?", "10m ago"),
                    Comment("comment2", "EliteSniper", "Looks dope. Can you test how it handles Chess AI?", "5m ago")
                ),
                shares = 12,
                authorRole = UserRole.COMMUNITY_MEMBER
            ),
            Post(
                id = "post2",
                authorName = "auracommunityact@gmail.com",
                authorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200",
                content = "🚨 GLOBAL ANNOUNCEMENT: Welcome to the official launch of Aura Community (ACT)! Sign up, custom-build your gaming tags, buy direct at our Chroma Store, and challenge friends to retro Snake or 2D Car Racing on the Mini Games tab! Let's build the ultimate community.",
                likes = 542,
                comments = listOf(
                    Comment("comment3", "ProGamerX", "Legendary! Love the WinZO-style matchmaking games!", "1h ago")
                ),
                shares = 128,
                authorRole = UserRole.ADMIN
            )
        )

        _chatMessagesState.value = listOf(
            ChatMessage("m1", "shroud_id", "Shroud Clone", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200", "Hey shooter, are you down for the ACT Mini Racing champion league tonight?"),
            ChatMessage("m2", "user_1", "Noob Slayer", "https://images.unsplash.com/photo-1566492031773-4f4e44671857?q=80&w=200", "Absolutely! Let me finish upgrading my racer highscore, then launch match matchmaking.")
        )

        _gamesState.value = listOf(
            GameItem("g1", "Neon Snake Arcade", "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=200", "Offline Games", isFeatured = true),
            GameItem("g2", "Cyber Street Racer", "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=200", "Offline Games", isFeatured = true),
            GameItem("g3", "Match-Tiles Memory", "https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?q=80&w=200", "Offline Games", isFeatured = true),
            GameItem("g4", "Online Tic-Tac-Toe Arena", "https://images.unsplash.com/photo-1531525645387-7f14be1bdbbd?q=80&w=200", "Online Games", isFeatured = false)
        )

        _notificationsState.value = listOf(
            AppNotification("n_1", "Welcome to Aura!", "Complete your gaming tags and enjoy neon arcade tournaments.", "system", "2m ago"),
            AppNotification("n_2", "New Product Restocked!", "Quantum Neon Pro Laptop now available for order in the ACT Store.", "shop", "12m ago")
        )

        _usersListState.value = listOf(
            UserProfile(id = "u_admin", email = "auracommunityact@gmail.com", displayName = "Aura Master (Admin)", username = "act_admin_zero", bio = "Official platform manager of ACT.", role = UserRole.ADMIN, level = 99),
            UserProfile(id = "user_1", email = "noobslayer@gmail.com", displayName = "Noob Slayer", username = "noob_slayer_act", bio = "Aiming with cyber mechanical setups.", role = UserRole.COMMUNITY_MEMBER, level = 14),
            UserProfile(id = "u_gues1", email = "tactical_gamer@gmail.com", displayName = "Sniper Apex", username = "apex_sniper", bio = "Duo matching lobby leader.", role = UserRole.NORMAL, level = 5)
        )

        _videosState.value = listOf(
            VideoItem(
                id = "v_1",
                title = "How Shroud Dominated ACT Arena League",
                description = "Deep dive analyzing target tracking, micro flicks, high-refresh rate display response times, and general mechanical keyboard inputs that crowned Shroud the undisputed champion.",
                videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                thumbnail = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=600",
                duration = "12:34",
                creatorId = "c2",
                creatorName = "ShroudClone",
                creatorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200",
                views = 12500,
                uploadDate = "2 days ago",
                likes = 420,
                shares = 88,
                comments = listOf(
                    Comment("vc_1", "SpeedyGamer", "Absolute machine flicks! Love the frame analysis.", "2h ago"),
                    Comment("vc_2", "TacticalCat", "Is that the Apex Drift Mouse? Looks ultra light.", "1h ago")
                ),
                isFeatured = true
            ),
            VideoItem(
                id = "v_2",
                title = "Gasket Mount mechanical keyboard custom unboxing & build log",
                description = "Detailed review assembling the pre-lubed tactile switch custom gasket controller. Sound tests of aluminum plate mounting inside our cyber chroma keyboard chassis.",
                videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                thumbnail = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?q=80&w=600",
                duration = "08:15",
                creatorId = "c3",
                creatorName = "PokimaneACT",
                creatorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200",
                views = 4300,
                uploadDate = "4 hours ago",
                likes = 185,
                shares = 32,
                comments = listOf(
                    Comment("vc_3", "KeyCollector", "That typing sound is pure ASMR gold, wow!", "3h ago")
                )
            ),
            VideoItem(
                id = "v_3",
                title = "Speedrunning Neon Snake Arcade: 500 Score barrier broken live",
                description = "Breaking down optimal path finding, tile safety, and continuous input rhythm to crush the top arcade global scores on ACT platform.",
                videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                thumbnail = "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=600",
                duration = "10:02",
                creatorId = "c1",
                creatorName = "NinjaVibe",
                creatorAvatar = "https://images.unsplash.com/photo-1560250097-0b93528c311a?q=80&w=200",
                views = 9200,
                uploadDate = "3 days ago",
                likes = 512,
                shares = 120,
                comments = emptyList()
            )
        )

        _liveStreamsState.value = listOf(
            LiveStreamItem(
                id = "live_1",
                title = "🔴 APEX LEGENDS FPS TOURNAMENT SEMIFINALS LIVE!",
                description = "Competing live in general lobby matchmakers on ACT Shell Phone. Tune in for XP, real-time chatter, and high stakes esports performance.",
                thumbnail = "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=600",
                category = "FPS Tournaments",
                creatorId = "c1",
                creatorName = "NinjaVibe",
                creatorAvatar = "https://images.unsplash.com/photo-1560250097-0b93528c311a?q=80&w=200",
                viewerCount = 1420,
                isLive = true,
                liveChat = listOf(
                    Comment("lc_1", "SniperGod", "Insane headshot! Ninja is on fire today!", "1m ago"),
                    Comment("lc_2", "AuraFan", "Let's break the peak viewer record!", "Just now")
                )
            )
        )
    }

    // --- Social Actions ---

    fun createVideo(video: VideoItem) {
        _videosState.value = listOf(video) + _videosState.value
        addSystemNotification("Video Uploaded", "${video.creatorName} published a new video: '${video.title}'")
    }

    fun deleteVideo(id: String) {
        _videosState.value = _videosState.value.filter { it.id != id }
    }

    fun toggleLikeVideo(id: String) {
        _videosState.value = _videosState.value.map {
            if (it.id == id) {
                val next = !it.isLiked
                val diff = if (next) 1 else -1
                it.copy(isLiked = next, likes = it.likes + diff)
            } else it
        }
    }

    fun commentOnVideo(id: String, content: String) {
        val user = _currentUserState.value
        _videosState.value = _videosState.value.map {
            if (it.id == id) {
                val c = Comment("vc_c_" + System.currentTimeMillis(), user.displayName, content, "Just now")
                it.copy(comments = it.comments + c)
            } else it
        }
    }

    fun toggleSaveVideo(id: String) {
        _videosState.value = _videosState.value.map {
            if (it.id == id) {
                it.copy(isSaved = !it.isSaved)
            } else it
        }
    }

    fun startLiveStream(stream: LiveStreamItem) {
        _liveStreamsState.value = listOf(stream) + _liveStreamsState.value
        addSystemNotification("Live Stream Started", "${stream.creatorName} is now LIVE!")
    }

    fun addLiveChatMessage(streamId: String, content: String) {
        val user = _currentUserState.value
        _liveStreamsState.value = _liveStreamsState.value.map {
            if (it.id == streamId) {
                val c = Comment("lc_" + System.currentTimeMillis(), user.displayName, content, "Just now")
                it.copy(liveChat = it.liveChat + c)
            } else it
        }
    }

    fun pinLiveMessage(streamId: String, text: String?) {
        _liveStreamsState.value = _liveStreamsState.value.map {
            if (it.id == streamId) {
                it.copy(pinnedMessage = text)
            } else it
        }
    }

    fun muteLiveChatToggle(streamId: String) {
        _liveStreamsState.value = _liveStreamsState.value.map {
            if (it.id == streamId) {
                it.copy(isMutedChat = !it.isMutedChat)
            } else it
        }
    }

    fun deleteLiveStream(id: String) {
        _liveStreamsState.value = _liveStreamsState.value.filter { it.id != id }
    }

    fun endLiveStream(streamId: String) {
        val stream = _liveStreamsState.value.find { it.id == streamId }
        if (stream != null) {
            // End live state
            _liveStreamsState.value = _liveStreamsState.value.map {
                if (it.id == streamId) it.copy(isLive = false, viewerCount = 0) else it
            }
            // Save as video replay!
            val videoReplay = VideoItem(
                id = "replay_" + stream.id,
                title = "[LIVE REPLAY] " + stream.title,
                description = "Archive stream recording. Category: ${stream.category}. Summary: ${stream.description}",
                videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                thumbnail = stream.thumbnail,
                duration = "45:10",
                creatorId = stream.creatorId,
                creatorName = stream.creatorName,
                creatorAvatar = stream.creatorAvatar,
                views = stream.viewerCount + 150,
                uploadDate = "Recorded Live Stream",
                likes = 25,
                comments = stream.liveChat,
                isSaved = false
            )
            _videosState.value = listOf(videoReplay) + _videosState.value
            addSystemNotification("Stream Ended & Saved", "${stream.creatorName}'s stream is archived as a replay.")
        }
    }

    fun pinPostToggle(postId: String) {
        _postsState.value = _postsState.value.map {
            if (it.id == postId) {
                it.copy(isPinned = !it.isPinned)
            } else it
        }
    }

    fun deletePost(id: String) {
        _postsState.value = _postsState.value.filter { it.id != id }
    }

    fun disablePostComments(id: String) {
        // Can toggle comment restriction
    }

    // Auth Actions
    fun createAndOnboardUser(onboardingDetails: UserProfile) {
        val userWithUpdatedAge = onboardingDetails.copy(
            age = calculateAge(onboardingDetails.birthDate)
        )
        _currentUserState.value = userWithUpdatedAge
        addSystemNotification("Profile Configured", "Your profile was validated as ${userWithUpdatedAge.displayName}. Welcome!")
    }

    fun loginWithEmail(email: String) {
        // If it's the admin email, boost role to admin!
        if (email.equals("auracommunityact@gmail.com", ignoreCase = true)) {
            _currentUserState.value = UserProfile(
                id = "admin_user",
                email = "auracommunityact@gmail.com",
                displayName = "ACT Lead Admin",
                username = "admin_aura_community",
                bio = "Head Administrator of ACT Forum, Store, and Arcade Hub.",
                role = UserRole.ADMIN,
                level = 100,
                isOnboarded = true
            )
            addSystemNotification("Admin Access Authenticated", "Logged in as official administrator panel manager.")
        } else {
            // Find existing or create
            val found = _usersListState.value.find { it.email.equals(email, ignoreCase = true) }
            if (found != null) {
                _currentUserState.value = found
            } else {
                _currentUserState.value = UserProfile(
                    id = "cust_" + System.currentTimeMillis(),
                    email = email,
                    displayName = email.split("@")[0].capitalize(),
                    username = email.split("@")[0] + "_act",
                    role = UserRole.NORMAL,
                    isOnboarded = false // Must complete onboarding steps!
                )
            }
            addSystemNotification("Login Success", "Successfully authenticated through Aura Security.")
        }
    }

    fun switchUserRole(role: UserRole) {
        val cur = _currentUserState.value
        val nextEmail = when(role) {
            UserRole.ADMIN -> "auracommunityact@gmail.com"
            UserRole.COMMUNITY_MEMBER -> "member@gmail.com"
            UserRole.NORMAL -> "user@gmail.com"
        }
        val nextDisplayName = when(role) {
            UserRole.ADMIN -> "ACT Platform Admin"
            UserRole.COMMUNITY_MEMBER -> "Chroma Champion"
            UserRole.NORMAL -> "Casual Gamer"
        }
        _currentUserState.value = cur.copy(role = role, email = nextEmail, displayName = nextDisplayName)
        addSystemNotification("Identity Synced", "Switched role successfully to ${role.name}")
    }

    // Admin Features: News, Creators, Products, Banner, Games control
    fun addBanner(banner: HeroBanner) {
        _bannersState.value = _bannersState.value + banner
        addSystemNotification("New Hero Banner Live", "Banner '${banner.headline}' is now actively sliding.")
    }

    fun deleteBanner(id: String) {
        _bannersState.value = _bannersState.value.filter { it.id != id }
    }

    fun addCreator(creator: Creator) {
        _creatorsState.value = _creatorsState.value + creator
        addSystemNotification("New Creator Added", "${creator.name} joined the top charts.")
    }

    fun removeCreator(id: String) {
        _creatorsState.value = _creatorsState.value.filter { it.id != id }
    }

    fun addProduct(product: Product) {
        db?.collection("products")?.document(product.id)?.set(product)?.addOnFailureListener {
            Log.e("AuraFirestore", "Failed to add product: ${it.message}")
        }
        _productsState.value = _productsState.value.filter { it.id != product.id } + product
        addSystemNotification("Shop Catalogue Expanded", "Product '${product.name}' was restocked.")
    }

    fun deleteProduct(id: String) {
        db?.collection("products")?.document(id)?.delete()?.addOnFailureListener {
            Log.e("AuraFirestore", "Failed to delete product: ${it.message}")
        }
        _productsState.value = _productsState.value.filter { it.id != id }
    }

    fun addNews(newsItem: NewsItem) {
        _newsState.value = listOf(newsItem) + _newsState.value
        addSystemNotification("Gaming Flash Breaking", newsItem.title)
    }

    fun deleteNews(id: String) {
        _newsState.value = _newsState.value.filter { it.id != id }
    }

    fun addGame(game: GameItem) {
        db?.collection("games")?.document(game.id)?.set(game)?.addOnFailureListener {
            Log.e("AuraFirestore", "Failed to add game: ${it.message}")
        }
        _gamesState.value = _gamesState.value + game
        addSystemNotification("Arcade Dashboard Updated", "${game.name} is now instantly playable.")
    }

    fun removeGame(id: String) {
        db?.collection("games")?.document(id)?.delete()?.addOnFailureListener {
            Log.e("AuraFirestore", "Failed to delete game: ${it.message}")
        }
        _gamesState.value = _gamesState.value.filter { it.id != id }
    }

    // User Operations on list (Banning / Promoting)
    fun banUserToggle(email: String) {
        _usersListState.value = _usersListState.value.map {
            if (it.email.equals(email, ignoreCase = true)) {
                val nextState = !it.isBanned
                if (email == _currentUserState.value.email) {
                    _currentUserState.value = _currentUserState.value.copy(isBanned = nextState)
                }
                it.copy(isBanned = nextState)
            } else it
        }
    }

    fun promoteUserRole(email: String, nextRole: UserRole) {
        _usersListState.value = _usersListState.value.map {
            if (it.email.equals(email, ignoreCase = true)) {
                if (email == _currentUserState.value.email) {
                    _currentUserState.value = _currentUserState.value.copy(role = nextRole)
                }
                it.copy(role = nextRole)
            } else it
        }
    }

    // Direct Messages and peer simulation
    fun sendChatMessage(text: String, imgUri: String? = null) {
        val user = _currentUserState.value
        val msg = ChatMessage(
            id = "msg_" + System.currentTimeMillis(),
            senderId = user.id,
            senderName = user.displayName,
            senderAvatar = user.profilePic,
            messageText = text,
            imageUri = imgUri
        )
        db?.collection("chat_history")?.document(msg.id)?.set(msg)?.addOnFailureListener {
            Log.e("AuraFirestore", "Failed to send chat: ${it.message}")
        }
        _chatMessagesState.value = _chatMessagesState.value + msg

        // Trigger simulation reply from creator/AI after brief delay to mimic a lively environment
        if (text.isNotBlank()) {
            Handler(Looper.getMainLooper()).postDelayed({
                val botMsg = ChatMessage(
                    id = "msg_reply_" + System.currentTimeMillis(),
                    senderId = "shroud_id",
                    senderName = "Shroud Clone",
                    senderAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200",
                    messageText = "Cool point! Join our mini games match lobby now, let's see some neon highscores!"
                )
                db?.collection("chat_history")?.document(botMsg.id)?.set(botMsg)?.addOnFailureListener {
                    Log.e("AuraFirestore", "Failed to sync bot chat: ${it.message}")
                }
                _chatMessagesState.value = _chatMessagesState.value + botMsg
                // Add chat notification
                _notificationsState.value = listOf(
                    AppNotification("n_c_" + System.currentTimeMillis(), "Shroud Clone responded", "New chat message in ACT Corridors", "chat", "Just now")
                ) + _notificationsState.value
            }, 3500)
        }
    }

    // Community post operations
    fun createPost(content: String, imgUrl: String? = null) {
        val user = _currentUserState.value
        val newPost = Post(
            id = "post_" + System.currentTimeMillis(),
            authorName = user.displayName,
            authorAvatar = user.profilePic,
            content = content,
            image = imgUrl,
            likes = 0,
            comments = emptyList(),
            shares = 0,
            authorRole = user.role
        )
        _postsState.value = listOf(newPost) + _postsState.value
        addSystemNotification("Post Published", "${user.displayName} updated the real-time community feed.")
    }

    fun toggleLikePost(postId: String) {
        _postsState.value = _postsState.value.map {
            if (it.id == postId) {
                val nextIsLiked = !it.isLiked
                val diff = if (nextIsLiked) 1 else -1
                it.copy(isLiked = nextIsLiked, likes = it.likes + diff)
            } else it
        }
    }

    fun addCommentToPost(postId: String, text: String) {
        val user = _currentUserState.value
        _postsState.value = _postsState.value.map {
            if (it.id == postId) {
                val nextComment = Comment("c_" + System.currentTimeMillis(), user.displayName, text, "Just now")
                it.copy(comments = it.comments + nextComment)
            } else it
        }
    }

    fun toggleFollowCreator(creatorId: String) {
        _creatorsState.value = _creatorsState.value.map {
            if (it.id == creatorId) {
                val nextFollowed = !it.isFollowed
                val diff = if (nextFollowed) 1 else -1
                it.copy(isFollowed = nextFollowed, followers = it.followers + diff)
            } else it
        }
    }

    fun toggleProductWishlist(productId: String) {
        _productsState.value = _productsState.value.map {
            if (it.id == productId) {
                val updated = it.copy(isWishlisted = !it.isWishlisted)
                db?.collection("products")?.document(productId)?.set(updated)?.addOnFailureListener { e ->
                    Log.e("AuraFirestore", "Failed to update product wishlist: ${e.message}")
                }
                updated
            } else it
        }
    }

    // Global system notifications broadcast
    fun broadcastGlobalNotification(title: String, body: String) {
        val notif = AppNotification(
            id = "n_b_" + System.currentTimeMillis(),
            title = "🚨 SYS: $title",
            description = body,
            type = "system",
            timestamp = "Just now"
        )
        _notificationsState.value = listOf(notif) + _notificationsState.value
    }

    private fun addSystemNotification(title: String, desc: String) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val systemTime = sdf.format(Date())
        val notif = AppNotification(
            id = "n_s_" + System.currentTimeMillis(),
            title = title,
            description = desc,
            type = "system",
            timestamp = "Today $systemTime"
        )
        _notificationsState.value = listOf(notif) + _notificationsState.value
    }

    private fun calculateAge(dobString: String): Int {
        return try {
            val parts = dobString.split("-")
            val year = parts[0].toInt()
            2026 - year
        } catch (e: Exception) {
            26
        }
    }

    // --- Firebase Sync Ecosystem ---

    fun setupFirestoreSync() {
        val database = db ?: return

        // 1. Live Chat History Integration
        try {
            database.collection("chat_history")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AuraFirestore", "Chat snapshot loading failure: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val senderId = doc.getString("senderId") ?: ""
                                val senderName = doc.getString("senderName") ?: ""
                                val senderAvatar = doc.getString("senderAvatar") ?: ""
                                val messageText = doc.getString("messageText") ?: ""
                                val imageUri = doc.getString("imageUri")
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                ChatMessage(id, senderId, senderName, senderAvatar, messageText, imageUri, timestamp)
                            } catch (e: Exception) {
                                null
                            }
                        }.sortedBy { it.timestamp }

                        if (messages.isEmpty() && snapshot.isEmpty) {
                            seedDefaultChats()
                        } else {
                            _chatMessagesState.value = messages
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("AuraFirestore", "Initializing Chat stream failed: ${e.message}")
        }

        // 2. Products Catalog Integration
        try {
            database.collection("products")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AuraFirestore", "Products snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val productList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val name = doc.getString("name") ?: ""
                                val description = doc.getString("description") ?: ""
                                val price = doc.getDouble("price") ?: 0.0
                                val rating = doc.getDouble("rating")?.toFloat() ?: 0.0f
                                val image = doc.getString("image") ?: ""
                                val category = doc.getString("category") ?: ""
                                val isWishlisted = doc.getBoolean("isWishlisted") ?: false
                                Product(id, name, description, price, rating, image, category, isWishlisted)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (productList.isEmpty() && snapshot.isEmpty) {
                            seedDefaultProducts()
                        } else {
                            _productsState.value = productList
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("AuraFirestore", "Initializing products stream failed: ${e.message}")
        }

        // 3. Mini Games Integration
        try {
            database.collection("games")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AuraFirestore", "Games snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val gamesList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val name = doc.getString("name") ?: ""
                                val thumbnail = doc.getString("thumbnail") ?: ""
                                val category = doc.getString("category") ?: "Offline Games"
                                val url = doc.getString("url") ?: ""
                                val isFeatured = doc.getBoolean("isFeatured") ?: false
                                val isHidden = doc.getBoolean("isHidden") ?: false
                                GameItem(id, name, thumbnail, category, url, isFeatured, isHidden)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (gamesList.isEmpty() && snapshot.isEmpty) {
                            seedDefaultGames()
                        } else {
                            _gamesState.value = gamesList
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("AuraFirestore", "Initializing games stream failed: ${e.message}")
        }

        // 4. User Inventories Integration
        try {
            database.collection("user_inventories")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AuraFirestore", "Inventory snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val inventoryList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val userId = doc.getString("userId") ?: ""
                                val itemName = doc.getString("itemName") ?: ""
                                val itemType = doc.getString("itemType") ?: "Product"
                                val itemImage = doc.getString("itemImage") ?: ""
                                val itemPrice = doc.getDouble("itemPrice") ?: 0.0
                                val purchaseDate = doc.getString("purchaseDate") ?: ""
                                UserInventoryItem(id, userId, itemName, itemType, itemImage, itemPrice, purchaseDate)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _userInventoryState.value = inventoryList
                    }
                }
        } catch (e: Exception) {
            Log.e("AuraFirestore", "Initializing inventory stream failed: ${e.message}")
        }
    }

    private fun seedDefaultChats() {
        val database = db ?: return
        val defaults = listOf(
            ChatMessage("m1", "shroud_id", "Shroud Clone", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200", "Hey shooter, are you down for the ACT Mini Racing champion league tonight?"),
            ChatMessage("m2", "user_1", "Noob Slayer", "https://images.unsplash.com/photo-1566492031773-4f4e44671857?q=80&w=200", "Absolutely! Let me finish upgrading my racer highscore, then launch match matchmaking.")
        )
        for (m in defaults) {
            database.collection("chat_history").document(m.id).set(m)
        }
    }

    private fun seedDefaultProducts() {
        val database = db ?: return
        val defaults = listOf(
            Product("p1", "Quantum Neon Pro Laptop", "17.3\" 240Hz screen, Intel Core i9, RTX 4090, 32GB DDR5.", 2499.00, 4.9f, "https://images.unsplash.com/photo-1603302576837-37561b2e2302?q=80&w=400", "Gaming Laptops"),
            Product("p2", "ACT Cyber Shell Phone", "OLED 144Hz, Snapdragon Gen 3, active aero-cooler built-in.", 899.00, 4.7f, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?q=80&w=400", "Gaming Phones"),
            Product("p3", "Neon Chroma Mechanical Keyboard", "Gasket mount, pre-lubed tactile switches, PBT double-shot keycaps.", 149.99, 4.8f, "https://images.unsplash.com/photo-1587829741301-dc798b83add3?q=80&w=400", "Gaming Keyboards"),
            Product("p4", "Apex Drift Wireless Mouse", "54g lightweight shell, 3395 raw optical sensor, 8000Hz polling rate.", 89.00, 4.5f, "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?q=80&w=400", "Gaming Mouse"),
            Product("p5", "Synthesizer Pro Sound Headset", "Planar magnetic drivers, ANC microphone, USB-C direct lossless.", 199.00, 4.4f, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=400", "Gaming Headsets"),
            Product("p6", "Tactile Grip Pro Controller", "Hall effect joysticks, trigger stops, mappable paddles.", 69.50, 4.6f, "https://images.unsplash.com/photo-1531525645387-7f14be1bdbbd?q=80&w=400", "Gaming Controllers"),
            Product("p7", "ACT Cyber Throne Ergonomic", "High-density cold foam, adjustable neck and lumbar, memory foam pads.", 349.00, 4.8f, "https://images.unsplash.com/photo-1598550476439-6847785fce6e?q=80&w=400", "Gaming Chairs"),
            Product("p8", "Chroma RGB Desk Deskpad", "900 x 400 x 4mm, waterproof mesh, customizable strip.", 35.00, 4.2f, "https://images.unsplash.com/photo-1616440347437-b1c73416efc2?q=80&w=400", "Gaming Accessories")
        )
        for (p in defaults) {
            database.collection("products").document(p.id).set(p)
        }
    }

    private fun seedDefaultGames() {
        val database = db ?: return
        val defaults = listOf(
            GameItem("g1", "Neon Snake Arcade", "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=200", "Offline Games", isFeatured = true),
            GameItem("g2", "Cyber Street Racer", "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=200", "Offline Games", isFeatured = true),
            GameItem("g3", "Match-Tiles Memory", "https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?q=80&w=200", "Offline Games", isFeatured = true),
            GameItem("g4", "Online Tic-Tac-Toe Arena", "https://images.unsplash.com/photo-1531525645387-7f14be1bdbbd?q=80&w=200", "Online Games", isFeatured = false)
        )
        for (g in defaults) {
            database.collection("games").document(g.id).set(g)
        }
    }

    fun purchaseProductToInventory(product: Product) {
        val user = _currentUserState.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val inv = UserInventoryItem(
            id = "inv_" + System.currentTimeMillis() + "_" + (10..99).random(),
            userId = user.id,
            itemName = product.name,
            itemType = "Product",
            itemImage = product.image,
            itemPrice = product.price,
            purchaseDate = dateString
        )
        db?.collection("user_inventories")?.document(inv.id)?.set(inv)?.addOnFailureListener {
            Log.e("AuraFirestore", "Failed to purchase product: ${it.message}")
        }
        _userInventoryState.value = _userInventoryState.value + inv
        addSystemNotification("Item Purchased!", "Successfully acquired ${product.name} at Chroma Store!")
    }

    fun addGameToInventory(game: GameItem) {
        val user = _currentUserState.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val inv = UserInventoryItem(
            id = "inv_" + System.currentTimeMillis() + "_" + (10..99).random(),
            userId = user.id,
            itemName = game.name,
            itemType = "Game",
            itemImage = game.thumbnail,
            itemPrice = 0.0,
            purchaseDate = dateString
        )
        db?.collection("user_inventories")?.document(inv.id)?.set(inv)?.addOnFailureListener {
            Log.e("AuraFirestore", "Failed to claim game: ${it.message}")
        }
        _userInventoryState.value = _userInventoryState.value + inv
        addSystemNotification("Game Claimed!", "Added ${game.name} to your local cabinet!")
    }
}
