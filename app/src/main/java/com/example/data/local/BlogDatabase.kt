package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AdConfig
import com.example.data.model.BlogPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [BlogPost::class, AdConfig::class], version = 1, exportSchema = false)
abstract class BlogDatabase : RoomDatabase() {
    abstract fun blogDao(): BlogDao

    companion object {
        @Volatile
        private var INSTANCE: BlogDatabase? = null

        fun getDatabase(context: Context): BlogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlogDatabase::class.java,
                    "blog_ads_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultData(database.blogDao())
                    }
                }
            }

            private suspend fun populateDefaultData(dao: BlogDao) {
                // Populate default ads
                dao.insertAd(
                    AdConfig(
                        title = "Unleash AI Power",
                        description = "Get started with the fastest AI Models for your enterprise. Try Gemini Pro today with $100 in free credits!",
                        type = "BANNER",
                        placement = "FEED_TOP",
                        imageUrl = "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=500&auto=format&fit=crop&q=60",
                        targetUrl = "https://ai.google.dev",
                        isEnabled = true,
                        orderIndex = 0
                    )
                )

                dao.insertAd(
                    AdConfig(
                        title = "Stay Styled & Structured",
                        description = "Upgrade to the premium Material 3 leather notebook designed for modern engineers and creatives.",
                        type = "INLINE",
                        placement = "ARTICLE_INLINE",
                        imageUrl = "https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=500&auto=format&fit=crop&q=60",
                        targetUrl = "https://m3.material.io",
                        isEnabled = true,
                        orderIndex = 1
                    )
                )

                dao.insertAd(
                    AdConfig(
                        title = "Master Clean Coding",
                        description = "Learn modern Android design, architectural patterns, and coroutines in our premium 12-week course. Click to enroll!",
                        type = "POPUP",
                        placement = "ARTICLE_BOTTOM",
                        imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500&auto=format&fit=crop&q=60",
                        targetUrl = "https://developer.android.com",
                        isEnabled = true,
                        orderIndex = 2
                    )
                )

                // Populate default blog posts
                dao.insertPost(
                    BlogPost(
                        title = "The Evolution of Jetpack Compose in 2026",
                        summary = "Discover how Jetpack Compose has transformed Android layouts, styling systems, and multiplatform UI integration.",
                        content = """
                            Jetpack Compose has come a long way since its inception. Today, it stands as the absolute gold standard for building modern, high-fidelity native Android applications. With the release of modern Material Design 3 and responsive layout APIs, developers can target any form factor with a single codebase.
                            
                            In this article, we explore the major advancements that make Jetpack Compose a joy to work with, including:
                            
                            1. Seamless Window Size Class Handling: Out-of-the-box support for foldable tablets and dual-screen form factors allows fluid, grid-based adaptation.
                            2. Built-in Shared Element Transitions: Smooth motion choreography that runs at 120 FPS natively without any performance lag.
                            3. Type-Safe Navigation Compose: Defining application routing directly using Kotlin @Serializable classes, fully compiling and validating navigation graphs at compile-time.
                            
                            As developers, adopting Compose is no longer an optional upgrade—it is the foundational toolchain for building fast, beautiful, and accessible user interfaces. Let's embrace the future of modern Android development together!
                        """.trimIndent(),
                        category = "Tech",
                        imageUrl = "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?w=800&auto=format&fit=crop&q=60",
                        author = "Devin Carter",
                        readsCount = 145
                    )
                )

                dao.insertPost(
                    BlogPost(
                        title = "Mastering Personal Finance & Tech Investments",
                        summary = "An essential guide to aligning your technical career growth with smart automated investment strategies.",
                        content = """
                            Building a successful career in technology is only half the battle. Managing the wealth you generate is where real financial independence begins. Many software developers and technology professionals earn high incomes but struggle to optimize their savings and long-term investment vectors.
                            
                            Here are three simple, highly actionable rules to build lasting tech wealth:
                            
                            - Automate Your Savings: Treat your savings like a fixed bill. Configure direct deposits to automatically transfer 20-30% of your earnings to high-yield savings accounts or diversified low-cost index funds immediately.
                            - Invest in What You Understand: While technology stocks, SaaS companies, and AI registries are exciting, ensure your portfolio is well-balanced across broad market ETFs to mitigate localized industry risks.
                            - Leverage Tax-Advantaged Accounts: Always maximize contributions to traditional or Roth retirement funds to capture compounding tax-free growth over decades.
                            
                            Remember, the best financial asset you have is your own earning capacity. Invest in continuously learning new skills, maintaining a high-fidelity workspace, and building passive income streams that allow you complete freedom of time.
                        """.trimIndent(),
                        category = "Finance",
                        imageUrl = "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=800&auto=format&fit=crop&q=60",
                        author = "Sarah Jenkins",
                        readsCount = 98
                    )
                )

                dao.insertPost(
                    BlogPost(
                        title = "Crafting the Perfect Remote Work Oasis",
                        summary = "How to style, organize, and equip your home office to maximize deep focus, ergonomics, and creativity.",
                        content = """
                            Working from home has transitioned from a temporary arrangement to a lifestyle choice. To sustain peak productivity and protect your health, your remote workspace must be treated as a sacred productivity temple. Designing a high-performance home office is not just about a fancy standing desk—it is about managing environment variables.
                            
                            To establish a premium workspace, prioritize these key aspects:
                            
                            - Ergononic Seating & Desk: Invest in an adjustable ergonomic chair that fully supports your lumbar spine. Alternate between sitting and standing throughout the day to keep your circulation active and combat fatigue.
                            - Lighting and Atmosphere: Position your desk near natural light, but offset from direct glare. Use adjustable warm LED accent lighting behind your monitors to reduce eye strain during late-night coding sessions.
                            - Noise & Interruption Control: Pair a clean ambient synth with noise-canceling headphones to enter a flow state easily. Keep the workspace minimalist and free of non-work clutter.
                            
                            Your workspace directly influences your cognitive output. By investing in comfort, aesthetics, and organization, you are investing in your focus, well-being, and professional craftsmanship.
                        """.trimIndent(),
                        category = "Lifestyle",
                        imageUrl = "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=800&auto=format&fit=crop&q=60",
                        author = "Liam Vance",
                        readsCount = 210
                    )
                )
            }
        }
    }
}
