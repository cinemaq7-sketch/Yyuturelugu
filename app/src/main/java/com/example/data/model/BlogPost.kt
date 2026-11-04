package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "blog_posts")
@JsonClass(generateAdapter = true)
data class BlogPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val summary: String,
    val content: String,
    val category: String, // e.g., "Tech", "Lifestyle", "Finance", "Travel"
    val imageUrl: String,
    val author: String = "Admin",
    val readsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ad_configs")
@JsonClass(generateAdapter = true)
data class AdConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val type: String, // "BANNER", "INLINE", "POPUP"
    val placement: String, // "FEED_TOP", "FEED_MIDDLE", "ARTICLE_INLINE", "ARTICLE_BOTTOM"
    val imageUrl: String,
    val targetUrl: String,
    val isEnabled: Boolean = true,
    val orderIndex: Int = 0,
    val clicksCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
