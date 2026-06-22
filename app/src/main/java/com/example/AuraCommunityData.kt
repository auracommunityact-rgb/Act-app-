package com.example

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Models ---

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
    var isLiked: Boolean = false
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

    init {
        resetToDefaults()
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
    }

    // --- Actions ---

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
        _productsState.value = _productsState.value.filter { it.id != product.id } + product
        addSystemNotification("Shop Catalogue Expanded", "Product '${product.name}' was restocked.")
    }

    fun deleteProduct(id: String) {
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
        _gamesState.value = _gamesState.value + game
        addSystemNotification("Arcade Dashboard Updated", "${game.name} is now instantly playable.")
    }

    fun removeGame(id: String) {
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
                it.copy(isWishlisted = !it.isWishlisted)
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
}
