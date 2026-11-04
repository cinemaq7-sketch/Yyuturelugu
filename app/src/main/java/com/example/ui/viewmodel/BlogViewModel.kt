package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.BlogDatabase
import com.example.data.model.AdConfig
import com.example.data.model.BlogPost
import com.example.data.remote.Content
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.GenerationConfig
import com.example.data.remote.Part
import com.example.data.remote.RetrofitClient
import com.example.data.repository.BlogRepository
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface BlogUiState {
    object Idle : BlogUiState
    object Loading : BlogUiState
    data class Success(val message: String) : BlogUiState
    data class Error(val message: String) : BlogUiState
}

@JsonClass(generateAdapter = true)
data class GeminiBlogResult(
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    val imageUrl: String
)

class BlogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BlogRepository
    val postsState: StateFlow<List<BlogPost>>
    val adsState: StateFlow<List<AdConfig>>

    private val _uiState = MutableStateFlow<BlogUiState>(BlogUiState.Idle)
    val uiState: StateFlow<BlogUiState> = _uiState.asStateFlow()

    // Navigation and selection state
    private val _currentScreen = MutableStateFlow<String>("feed") // "feed", "article_details", "admin_dashboard", "admin_create_blog", "admin_ad_manager"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedPost = MutableStateFlow<BlogPost?>(null)
    val selectedPost: StateFlow<BlogPost?> = _selectedPost.asStateFlow()

    private val _selectedAd = MutableStateFlow<AdConfig?>(null)
    val selectedAd: StateFlow<AdConfig?> = _selectedAd.asStateFlow()

    // Filter categories
    private val _selectedCategory = MutableStateFlow<String>("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Admin toggle (shows quick switch banner/button)
    private val _isAdminMode = MutableStateFlow<Boolean>(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    init {
        val database = BlogDatabase.getDatabase(application)
        repository = BlogRepository(database.blogDao())
        
        postsState = repository.allPosts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        adsState = repository.allAds.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
        if (_isAdminMode.value) {
            _currentScreen.value = "admin_dashboard"
        } else {
            _currentScreen.value = "feed"
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun navigateTo(screen: String, post: BlogPost? = null, ad: AdConfig? = null) {
        _currentScreen.value = screen
        if (post != null) {
            _selectedPost.value = post
            incrementPostRead(post)
        }
        if (ad != null) {
            _selectedAd.value = ad
        }
        _uiState.value = BlogUiState.Idle
    }

    // --- BLOG ACTIONS ---

    fun uploadBlogPost(title: String, summary: String, content: String, category: String, imageUrl: String) {
        if (title.isBlank() || content.isBlank() || summary.isBlank()) {
            _uiState.value = BlogUiState.Error("Title, Summary, and Content fields cannot be empty.")
            return
        }

        val finalImg = imageUrl.ifBlank {
            // Select standard beautiful default placeholder image based on category
            when (category.lowercase()) {
                "tech" -> "https://images.unsplash.com/photo-1518770660439-4636190af475?w=600&auto=format&fit=crop&q=60"
                "finance" -> "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=600&auto=format&fit=crop&q=60"
                "lifestyle" -> "https://images.unsplash.com/photo-1511556532299-8f662fc26c06?w=600&auto=format&fit=crop&q=60"
                else -> "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=600&auto=format&fit=crop&q=60"
            }
        }

        viewModelScope.launch {
            try {
                val post = BlogPost(
                    title = title.trim(),
                    summary = summary.trim(),
                    content = content.trim(),
                    category = category,
                    imageUrl = finalImg,
                    author = "Admin Staff"
                )
                repository.insertPost(post)
                _uiState.value = BlogUiState.Success("Article uploaded successfully!")
                _currentScreen.value = "admin_dashboard"
            } catch (e: Exception) {
                _uiState.value = BlogUiState.Error(e.localizedMessage ?: "Failed to save blog post.")
            }
        }
    }

    fun deleteBlogPost(post: BlogPost) {
        viewModelScope.launch {
            repository.deletePost(post)
            if (_selectedPost.value?.id == post.id) {
                _selectedPost.value = null
            }
        }
    }

    fun incrementPostRead(post: BlogPost) {
        viewModelScope.launch {
            val updated = post.copy(readsCount = post.readsCount + 1)
            repository.updatePost(updated)
        }
    }

    // --- AD ACTIONS ---

    fun saveAdConfig(
        id: Int = 0,
        title: String,
        description: String,
        type: String,
        placement: String,
        imageUrl: String,
        targetUrl: String,
        isEnabled: Boolean,
        orderIndex: Int
    ) {
        if (title.isBlank() || description.isBlank()) {
            _uiState.value = BlogUiState.Error("Ad Title and Description cannot be empty.")
            return
        }

        val finalImg = imageUrl.ifBlank {
            "https://images.unsplash.com/photo-1542744094-3a31f103e35f?w=500&auto=format&fit=crop&q=60"
        }

        val finalTarget = targetUrl.ifBlank { "https://google.com" }

        viewModelScope.launch {
            try {
                val ad = AdConfig(
                    id = if (id == 0) 0 else id,
                    title = title.trim(),
                    description = description.trim(),
                    type = type,
                    placement = placement,
                    imageUrl = finalImg,
                    targetUrl = finalTarget,
                    isEnabled = isEnabled,
                    orderIndex = orderIndex,
                    clicksCount = if (id == 0) 0 else (_selectedAd.value?.clicksCount ?: 0)
                )
                repository.insertAd(ad)
                _uiState.value = BlogUiState.Success("Ad configuration saved successfully!")
                _currentScreen.value = "admin_ad_manager"
            } catch (e: Exception) {
                _uiState.value = BlogUiState.Error(e.localizedMessage ?: "Failed to save ad configuration.")
            }
        }
    }

    fun toggleAd(ad: AdConfig) {
        viewModelScope.launch {
            val updated = ad.copy(isEnabled = !ad.isEnabled)
            repository.updateAd(updated)
        }
    }

    fun deleteAd(ad: AdConfig) {
        viewModelScope.launch {
            repository.deleteAd(ad)
            if (_selectedAd.value?.id == ad.id) {
                _selectedAd.value = null
            }
        }
    }

    fun incrementAdClick(ad: AdConfig) {
        viewModelScope.launch {
            val updated = ad.copy(clicksCount = ad.clicksCount + 1)
            repository.updateAd(updated)
        }
    }

    // --- GEMINI AI INTEGRATION ---

    fun generateBlogPostFromAi(promptTopic: String, category: String) {
        if (promptTopic.isBlank()) {
            _uiState.value = BlogUiState.Error("Please enter a topic or focus keywords for the AI.")
            return
        }

        _uiState.value = BlogUiState.Loading

        viewModelScope.launch {
            try {
                val result = generateBlogFromAiApi(promptTopic, category)
                if (result != null) {
                    val imageUrl = when (category.lowercase()) {
                        "tech" -> "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800&auto=format&fit=crop&q=60"
                        "finance" -> "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800&auto=format&fit=crop&q=60"
                        "lifestyle" -> "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop&q=60"
                        else -> "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800&auto=format&fit=crop&q=60"
                    }
                    val post = BlogPost(
                        title = result.title,
                        summary = result.summary,
                        content = result.content,
                        category = result.category,
                        imageUrl = imageUrl,
                        author = "AI Copilot"
                    )
                    repository.insertPost(post)
                    _uiState.value = BlogUiState.Success("AI blog post generated & uploaded successfully!")
                    _currentScreen.value = "admin_dashboard"
                } else {
                    _uiState.value = BlogUiState.Error("Failed to parse the generated blog from Gemini.")
                }
            } catch (e: Exception) {
                _uiState.value = BlogUiState.Error(
                    e.localizedMessage ?: "Connection error with Gemini. Please verify your API Key."
                )
            }
        }
    }

    private suspend fun generateBlogFromAiApi(topic: String, category: String): GeminiBlogResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key is missing! Please configure your Gemini API Key in the Secrets panel in Google AI Studio to use AI Blog Generation features.")
        }

        val systemInstructionText = """
            You are an expert, award-winning content writer and SEO copywriter.
            Your job is to write a highly engaging, fully fleshed-out, comprehensive blog post about the topic provided by the user.
            You must tailor your writing style to the selected category: $category.
            Make sure the blog has multiple distinct sections or points, reads naturally, is grammatically flawless, and uses professional, persuasive vocabulary.
            
            You MUST return a JSON object that adheres strictly to the following schema:
            {
              "title": "A highly catchy, clickable, and interesting SEO-optimized headline",
              "summary": "A 1-2 sentence compelling summary/subtitle of what this article covers",
              "content": "The full blog post text. Use newline characters (\n) to separate multiple paragraphs, numbered points, or headers to make it look clean and spacious.",
              "category": "$category",
              "imageUrl": ""
            }
            Do not wrap the response in markdown blocks like ```json. Return ONLY the raw JSON string.
        """.trimIndent()

        val prompt = "Write a full article on the topic/keywords: '$topic'."

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val adapter = RetrofitClient.moshiInstance.adapter(GeminiBlogResult::class.java)
                adapter.fromJson(jsonText)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
