package com.example.data.repository

import com.example.data.local.BlogDao
import com.example.data.model.AdConfig
import com.example.data.model.BlogPost
import kotlinx.coroutines.flow.Flow

class BlogRepository(private val blogDao: BlogDao) {
    val allPosts: Flow<List<BlogPost>> = blogDao.getAllPosts()
    val allAds: Flow<List<AdConfig>> = blogDao.getAllAds()

    suspend fun getPostById(id: Int): BlogPost? {
        return blogDao.getPostById(id)
    }

    suspend fun insertPost(post: BlogPost): Long {
        return blogDao.insertPost(post)
    }

    suspend fun updatePost(post: BlogPost) {
        blogDao.updatePost(post)
    }

    suspend fun deletePost(post: BlogPost) {
        blogDao.deletePost(post)
    }

    suspend fun deletePostById(id: Int) {
        blogDao.deletePostById(id)
    }

    // Ads repository operations
    suspend fun getAdById(id: Int): AdConfig? {
        return blogDao.getAdById(id)
    }

    suspend fun insertAd(ad: AdConfig): Long {
        return blogDao.insertAd(ad)
    }

    suspend fun updateAd(ad: AdConfig) {
        blogDao.updateAd(ad)
    }

    suspend fun deleteAd(ad: AdConfig) {
        blogDao.deleteAd(ad)
    }

    suspend fun deleteAdById(id: Int) {
        blogDao.deleteAdById(id)
    }
}
