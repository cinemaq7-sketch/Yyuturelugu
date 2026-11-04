package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BlogPost
import com.example.data.model.AdConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface BlogDao {
    // Blog Posts
    @Query("SELECT * FROM blog_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<BlogPost>>

    @Query("SELECT * FROM blog_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Int): BlogPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: BlogPost): Long

    @Update
    suspend fun updatePost(post: BlogPost)

    @Delete
    suspend fun deletePost(post: BlogPost)

    @Query("DELETE FROM blog_posts WHERE id = :id")
    suspend fun deletePostById(id: Int)

    // Ad Configurations
    @Query("SELECT * FROM ad_configs ORDER BY orderIndex ASC, timestamp DESC")
    fun getAllAds(): Flow<List<AdConfig>>

    @Query("SELECT * FROM ad_configs WHERE id = :id LIMIT 1")
    suspend fun getAdById(id: Int): AdConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: AdConfig): Long

    @Update
    suspend fun updateAd(ad: AdConfig)

    @Delete
    suspend fun deleteAd(ad: AdConfig)

    @Query("DELETE FROM ad_configs WHERE id = :id")
    suspend fun deleteAdById(id: Int)
}
