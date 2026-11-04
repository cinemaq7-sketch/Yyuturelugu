package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Announcement
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.AdConfig
import com.example.data.model.BlogPost
import com.example.ui.theme.*
import com.example.ui.viewmodel.BlogUiState
import com.example.ui.viewmodel.BlogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: BlogViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedPost by viewModel.selectedPost.collectAsState()
    val posts by viewModel.postsState.collectAsState()
    val ads by viewModel.adsState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val context = LocalContext.current

    // Active popup ad state helper
    var showPopupAdDialog by remember { mutableStateOf<AdConfig?>(null) }
    
    // Check for popup ads when viewing article details
    LaunchedEffect(currentScreen, ads) {
        if (currentScreen == "article_details") {
            val popupAd = ads.firstOrNull { it.type == "POPUP" && it.isEnabled }
            if (popupAd != null) {
                showPopupAdDialog = popupAd
            }
        } else {
            showPopupAdDialog = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "App Logo",
                            tint = CyberBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "BLOGCRAFT PRO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    if (currentScreen != "feed" && currentScreen != "admin_dashboard") {
                        IconButton(
                            onClick = {
                                if (isAdminMode) {
                                    viewModel.navigateTo("admin_dashboard")
                                } else {
                                    viewModel.navigateTo("feed")
                                }
                            },
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = CyberBlue
                            )
                        }
                    }
                },
                actions = {
                    // Admin Switch Button
                    Button(
                        onClick = { viewModel.toggleAdminMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdminMode) AlertRed.copy(alpha = 0.2f) else CyberBlue.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, if (isAdminMode) AlertRed else CyberBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("admin_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = "Toggle Mode",
                            tint = if (isAdminMode) AlertRed else CyberBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAdminMode) "ADMIN PANEL" else "USER PANEL",
                            color = if (isAdminMode) AlertRed else CyberBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepSlate,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "feed" -> UserFeedScreen(
                        viewModel = viewModel,
                        posts = posts,
                        ads = ads
                    )
                    "article_details" -> selectedPost?.let { post ->
                        ArticleDetailsScreen(
                            post = post,
                            ads = ads,
                            onAdClick = { ad -> viewModel.incrementAdClick(ad) }
                        )
                    } ?: run {
                        viewModel.navigateTo("feed")
                        Box(modifier = Modifier.fillMaxSize())
                    }
                    "admin_dashboard" -> AdminDashboardScreen(
                        viewModel = viewModel,
                        posts = posts,
                        ads = ads
                    )
                    "admin_create_blog" -> AdminCreateBlogScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    "admin_ad_manager" -> AdminAdManagerScreen(
                        viewModel = viewModel,
                        ads = ads,
                        uiState = uiState
                    )
                }
            }

            // Interactive popup ad overlay
            showPopupAdDialog?.let { ad ->
                PopupAdDialog(
                    ad = ad,
                    onDismiss = { showPopupAdDialog = null },
                    onAction = {
                        viewModel.incrementAdClick(ad)
                        showPopupAdDialog = null
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.targetUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening link: ${ad.targetUrl}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

// ================= USER MODULES =================

@Composable
fun UserFeedScreen(
    viewModel: BlogViewModel,
    posts: List<BlogPost>,
    ads: List<AdConfig>
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = listOf("All", "Tech", "Finance", "Lifestyle", "Travel")

    // Filter posts
    val filteredPosts = if (selectedCategory == "All") {
        posts
    } else {
        posts.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    // Get Banner Ads
    val topBannerAd = ads.firstOrNull { it.type == "BANNER" && it.placement == "FEED_TOP" && it.isEnabled }
    val middleInlineAd = ads.firstOrNull { it.type == "INLINE" && it.placement == "FEED_MIDDLE" && it.isEnabled }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Category Pills Row
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberBlue,
                            selectedLabelColor = Color.Black,
                            containerColor = SurfaceSlate,
                            labelColor = MutedSlate
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = BorderSlate,
                            selectedBorderColor = CyberBlue
                        )
                    )
                }
            }
        }

        // FEED TOP BANNER AD (Configureable)
        topBannerAd?.let { ad ->
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    ClickableAdCard(ad = ad, onAdClick = { viewModel.incrementAdClick(ad) })
                }
            }
        }

        // Highlight Hero post
        if (filteredPosts.isNotEmpty() && selectedCategory == "All") {
            item {
                val heroPost = filteredPosts.first()
                HeroBlogCard(
                    post = heroPost,
                    onClick = { viewModel.navigateTo("article_details", post = heroPost) }
                )
            }
        }

        // Main Feed list with arranged Inline ad insertion
        val feedPosts = if (selectedCategory == "All" && filteredPosts.isNotEmpty()) {
            filteredPosts.drop(1)
        } else {
            filteredPosts
        }

        if (feedPosts.isEmpty() && filteredPosts.isEmpty()) {
            item {
                EmptyStateView("No articles published in this category yet.")
            }
        } else {
            itemsIndexed(feedPosts) { index, post ->
                // After every 2 articles, we insert the MIDDLE_FEED inline ad if configured
                val showMiddleAd = middleInlineAd != null && index > 0 && index % 2 == 0
                
                if (showMiddleAd && middleInlineAd != null) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ClickableAdCard(ad = middleInlineAd, onAdClick = { viewModel.incrementAdClick(middleInlineAd) })
                    }
                }

                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    BlogItemRow(
                        post = post,
                        onClick = { viewModel.navigateTo("article_details", post = post) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBlogCard(post: BlogPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Category badge overlay
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberBlue)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = post.category.uppercase(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                // Overlay Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = post.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = post.summary,
                    color = MutedSlate,
                    fontSize = 13.sp,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "By ${post.author} • ${formatDate(post.timestamp)}",
                        color = MutedSlate,
                        fontSize = 11.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveRedEye,
                            contentDescription = "Reads",
                            tint = CyberBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${post.readsCount}",
                            color = CyberBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BlogItemRow(post: BlogPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.title,
                modifier = Modifier
                    .size(85.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = post.category.uppercase(),
                            color = CyberBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = formatDate(post.timestamp),
                        color = MutedSlate,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.summary,
                    color = MutedSlate,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Read post",
                tint = MutedSlate,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ArticleDetailsScreen(
    post: BlogPost,
    ads: List<AdConfig>,
    onAdClick: (AdConfig) -> Unit
) {
    // Collect active inline ad specifically configured for internal article viewing
    val articleInlineAd = ads.firstOrNull { it.type == "INLINE" && it.placement == "ARTICLE_INLINE" && it.isEnabled }
    val articleBottomAd = ads.firstOrNull { it.type == "BANNER" && it.placement == "ARTICLE_BOTTOM" && it.isEnabled }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = post.category.uppercase(),
                        color = CyberBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 30.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Published by ${post.author} on ${formatDate(post.timestamp)}",
                    color = MutedSlate,
                    fontSize = 12.sp
                )
            }
        }

        item {
            // Hero image
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        item {
            // Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                border = BorderStroke(1.dp, BorderSlate),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Summary",
                        tint = CyberBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = post.summary,
                        color = TextSlate,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Body Content (Split to elegantly place custom Inline Ad)
        val paragraphs = post.content.split("\n\n")
        if (paragraphs.size > 1) {
            // Print first paragraph
            item {
                Text(
                    text = paragraphs[0],
                    color = TextSlate,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            // ARTICLE INLINE AD (Arrangable / configured)
            articleInlineAd?.let { ad ->
                item {
                    ClickableAdCard(ad = ad, onAdClick = onAdClick)
                }
            }

            // Print the rest of the paragraphs
            items(paragraphs.drop(1)) { para ->
                Text(
                    text = para,
                    color = TextSlate,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        } else {
            item {
                Text(
                    text = post.content,
                    color = TextSlate,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            articleInlineAd?.let { ad ->
                item {
                    ClickableAdCard(ad = ad, onAdClick = onAdClick)
                }
            }
        }

        // ARTICLE BOTTOM AD (Banner configuration)
        articleBottomAd?.let { ad ->
            item {
                Divider(color = BorderSlate, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                ClickableAdCard(ad = ad, onAdClick = onAdClick)
            }
        }
    }
}

@Composable
fun ClickableAdCard(ad: AdConfig, onAdClick: (AdConfig) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onAdClick(ad)
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.targetUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast
                        .makeText(context, "Sponsoring Ad click: ${ad.targetUrl}", Toast.LENGTH_SHORT)
                        .show()
                }
            },
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CyberBlue.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                AsyncImage(
                    model = ad.imageUrl,
                    contentDescription = ad.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Sponsor Badge
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "SPONSORED AD",
                        color = GlowCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ad.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ad.description,
                        color = MutedSlate,
                        fontSize = 11.sp,
                        maxLines = 2,
                        lineHeight = 14.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberBlue)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "VISIT",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PopupAdDialog(
    ad: AdConfig,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GlowCyan)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    AsyncImage(
                        model = ad.imageUrl,
                        contentDescription = ad.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Sponsor label
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AlertRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "SPECIAL OFFER",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Ad",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = ad.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = ad.description,
                        color = MutedSlate,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedSlate),
                            border = BorderStroke(1.dp, BorderSlate)
                        ) {
                            Text("No Thanks", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onAction,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                        ) {
                            Text("Claim Deal", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ================= ADMIN MODULES =================

@Composable
fun AdminDashboardScreen(
    viewModel: BlogViewModel,
    posts: List<BlogPost>,
    ads: List<AdConfig>
) {
    // Analytical calculations
    val totalReads = posts.sumOf { it.readsCount }
    val totalClicks = ads.sumOf { it.clicksCount }
    val ctr = if (totalReads > 0) {
        String.format(Locale.US, "%.1f%%", (totalClicks.toFloat() / totalReads.toFloat()) * 100)
    } else {
        "0.0%"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            // Admin Hero Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SurfaceSlate, DeepSlate)
                        )
                    )
                    .border(BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ADMIN CONTROL PANEL",
                        color = AlertRed,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Manage Blogs & Ads Live",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Upload, write with Gemini AI, configure banner and pop-up ad placements, and track active click metrics in real-time.",
                        color = MutedSlate,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Statistics Block
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatCard("Total Posts", "${posts.size}", Icons.Outlined.Article, Modifier.weight(1f))
                AdminStatCard("Blog Reads", "$totalReads", Icons.Outlined.Visibility, Modifier.weight(1f))
                AdminStatCard("Ad Clicks", "$totalClicks", Icons.Outlined.Announcement, Modifier.weight(1f))
                AdminStatCard("Click CTR", ctr, Icons.Outlined.Analytics, Modifier.weight(1f))
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo("admin_create_blog") },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Blog", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Blog", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.navigateTo("admin_ad_manager") },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowCyan),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Manage Ads", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Arrange Ads", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Blog Archive Header
        item {
            Text(
                text = "PUBLISHED BLOG ARCHIVES (${posts.size})",
                color = MutedSlate,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = "Empty History",
                            tint = BorderSlate,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No articles published. Click 'New Blog' to upload!",
                            color = MutedSlate,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(posts) { post ->
                AdminPostRow(
                    post = post,
                    onDelete = { viewModel.deleteBlogPost(post) }
                )
            }
        }
    }
}

@Composable
fun AdminStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = CyberBlue, modifier = Modifier.size(16.dp))
            Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(text = label, color = MutedSlate, fontSize = 9.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AdminPostRow(post: BlogPost, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.title,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${post.category} • ${post.readsCount} reads",
                    color = MutedSlate,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed.copy(alpha = 0.8f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateBlogScreen(
    viewModel: BlogViewModel,
    uiState: BlogUiState
) {
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tech") }
    var imageUrl by remember { mutableStateOf("") }

    // Gemini generator auxiliary prompt helper
    var aiPromptTopic by remember { mutableStateOf("") }
    var showAiAssistPanel by remember { mutableStateOf(false) }

    val categories = listOf("Tech", "Finance", "Lifestyle", "Travel")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PUBLISH NEW BLOG",
                        color = CyberBlue,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Upload Post details",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // AI Copilot toggle button
                Button(
                    onClick = { showAiAssistPanel = !showAiAssistPanel },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAiAssistPanel) GlowCyan.copy(alpha = 0.2f) else DeepSlate
                    ),
                    border = BorderStroke(1.dp, GlowCyan)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "AI Copilot", tint = GlowCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI WRITER", color = GlowCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Gemini AI generation drawer
        if (showAiAssistPanel) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    border = BorderStroke(1.dp, GlowCyan)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = GlowCyan)
                            Text("Gemini AI Blog Copilot", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Text(
                            text = "Provide a blog topic or outline, select your category below, and let Gemini write a comprehensive professional blog post for you instantly!",
                            color = MutedSlate,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = aiPromptTopic,
                            onValueChange = { aiPromptTopic = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Benefits of automation in software testing, or travel guide to Kyoto", color = MutedSlate) },
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlowCyan,
                                unfocusedBorderColor = BorderSlate,
                                cursorColor = GlowCyan
                            )
                        )

                        Button(
                            onClick = { viewModel.generateBlogPostFromAi(aiPromptTopic, category) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GlowCyan),
                            shape = RoundedCornerShape(8.dp),
                            enabled = uiState !is BlogUiState.Loading
                        ) {
                            if (uiState is BlogUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                            } else {
                                Text("WRITE & PUBLISH VIA AI", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Standard Form Inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Category Selection Pills
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Category Selection", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.forEach { cat ->
                                val isSelected = category == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberBlue,
                                        selectedLabelColor = Color.Black,
                                        containerColor = DeepSlate,
                                        labelColor = MutedSlate
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = BorderSlate,
                                        selectedBorderColor = CyberBlue
                                    )
                                )
                            }
                        }
                    }

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Article Title") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberBlue,
                            unfocusedBorderColor = BorderSlate,
                            cursorColor = CyberBlue
                        )
                    )

                    // Summary
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Brief Summary / Subtitle") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberBlue,
                            unfocusedBorderColor = BorderSlate,
                            cursorColor = CyberBlue
                        )
                    )

                    // Image URL
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Hero Image URL (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Leave blank for high-quality default placeholder") },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberBlue,
                            unfocusedBorderColor = BorderSlate,
                            cursorColor = CyberBlue
                        )
                    )

                    // Content Body
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Full Article Content Body") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberBlue,
                            unfocusedBorderColor = BorderSlate,
                            cursorColor = CyberBlue
                        )
                    )

                    // Error display if any
                    if (uiState is BlogUiState.Error) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x26EF4444)),
                            border = BorderStroke(1.dp, AlertRed)
                        ) {
                            Text(
                                text = uiState.message,
                                color = AlertRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Save Publish button
                    Button(
                        onClick = { viewModel.uploadBlogPost(title, summary, content, category, imageUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("PUBLISH & UPLOAD BLOG", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAdManagerScreen(
    viewModel: BlogViewModel,
    ads: List<AdConfig>,
    uiState: BlogUiState
) {
    // Ad dialog state
    var showAdEditor by remember { mutableStateOf<AdConfig?>(null) }
    var isNewAdForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AD MANAGER & PLACEMENTS",
                        color = GlowCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Arrange & Position Ads",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Add Custom Ad Button
                Button(
                    onClick = {
                        isNewAdForm = true
                        showAdEditor = AdConfig(title = "", description = "", type = "BANNER", placement = "FEED_TOP", imageUrl = "", targetUrl = "")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add custom ad", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Ad", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (ads.isEmpty()) {
            item {
                EmptyStateView("No advertisements configured yet. Create one now!")
            }
        } else {
            items(ads) { ad ->
                Box(modifier = Modifier.padding(vertical = 2.dp)) {
                    AdminAdConfigCard(
                        ad = ad,
                        onToggle = { viewModel.toggleAd(ad) },
                        onEdit = {
                            isNewAdForm = false
                            showAdEditor = ad
                        },
                        onDelete = { viewModel.deleteAd(ad) }
                    )
                }
            }
        }
    }

    // Modal Sheet Editor for Ad configuration
    showAdEditor?.let { ad ->
        AdEditorDialog(
            ad = ad,
            isNew = isNewAdForm,
            onDismiss = { showAdEditor = null },
            onSave = { updatedAd ->
                viewModel.saveAdConfig(
                    id = updatedAd.id,
                    title = updatedAd.title,
                    description = updatedAd.description,
                    type = updatedAd.type,
                    placement = updatedAd.placement,
                    imageUrl = updatedAd.imageUrl,
                    targetUrl = updatedAd.targetUrl,
                    isEnabled = updatedAd.isEnabled,
                    orderIndex = updatedAd.orderIndex
                )
                showAdEditor = null
            }
        )
    }
}

@Composable
fun AdminAdConfigCard(
    ad: AdConfig,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        border = BorderStroke(1.dp, if (ad.isEnabled) GlowCyan.copy(alpha = 0.5f) else BorderSlate)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (ad.isEnabled) GlowCyan.copy(alpha = 0.15f) else BorderSlate)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ad.type,
                            color = if (ad.isEnabled) GlowCyan else MutedSlate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DeepSlate)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ad.placement,
                            color = MutedSlate,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Switch(
                    checked = ad.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GlowCyan,
                        checkedTrackColor = GlowCyan.copy(alpha = 0.3f)
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ad.imageUrl,
                    contentDescription = ad.title,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = ad.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Target: ${ad.targetUrl}", color = MutedSlate, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // Clicks stats indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DeepSlate)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${ad.clicksCount}", color = GlowCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(text = "CLICKS", color = MutedSlate, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Divider(color = BorderSlate, thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Arrangement index: ${ad.orderIndex}",
                    color = MutedSlate,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        border = BorderStroke(1.dp, BorderSlate),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MutedSlate, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = MutedSlate, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = AlertRed, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdEditorDialog(
    ad: AdConfig,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (AdConfig) -> Unit
) {
    var title by remember { mutableStateOf(ad.title) }
    var description by remember { mutableStateOf(ad.description) }
    var type by remember { mutableStateOf(ad.type) } // "BANNER", "INLINE", "POPUP"
    var placement by remember { mutableStateOf(ad.placement) } // "FEED_TOP", "FEED_MIDDLE", "ARTICLE_INLINE", "ARTICLE_BOTTOM"
    var imageUrl by remember { mutableStateOf(ad.imageUrl) }
    var targetUrl by remember { mutableStateOf(ad.targetUrl) }
    var isEnabled by remember { mutableStateOf(ad.isEnabled) }
    var orderIndex by remember { mutableStateOf(ad.orderIndex.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSlate)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (isNew) "CREATE AD PLACEMENT" else "CONFIGURE AD PLACEMENT",
                        color = GlowCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Ad Title / Catchphrase") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlowCyan)
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Ad Description Body") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlowCyan)
                    )
                }

                // Type Dropdown Pills
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Ad Format / Type", color = MutedSlate, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("BANNER", "INLINE", "POPUP").forEach { t ->
                                val isSel = type == t
                                FilterChip(
                                    selected = isSel,
                                    onClick = { type = t },
                                    label = { Text(t, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GlowCyan,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }

                // Placement Dropdown Pills
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Placement Location", color = MutedSlate, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("FEED_TOP", "FEED_MIDDLE", "ARTICLE_INLINE", "ARTICLE_BOTTOM").forEach { p ->
                                val isSel = placement == p
                                FilterChip(
                                    selected = isSel,
                                    onClick = { placement = p },
                                    label = { Text(p, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GlowCyan,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Ad Banner Image URL") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlowCyan)
                    )
                }

                item {
                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = { targetUrl = it },
                        label = { Text("Action / Target Link URL") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlowCyan)
                    )
                }

                item {
                    OutlinedTextField(
                        value = orderIndex,
                        onValueChange = { orderIndex = it },
                        label = { Text("Arrangement Priority Index") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlowCyan)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active immediately?", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GlowCyan)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, BorderSlate)
                        ) {
                            Text("Cancel", color = MutedSlate)
                        }

                        Button(
                            onClick = {
                                val idx = orderIndex.toIntOrNull() ?: 0
                                onSave(
                                    ad.copy(
                                        title = title,
                                        description = description,
                                        type = type,
                                        placement = placement,
                                        imageUrl = imageUrl,
                                        targetUrl = targetUrl,
                                        isEnabled = isEnabled,
                                        orderIndex = idx
                                    )
                                )
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                        ) {
                            Text("SAVE AD", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ================= GENERAL COMMON REUSABLE COMPONENTS =================

@Composable
fun EmptyStateView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = "No data",
                tint = BorderSlate,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                color = MutedSlate,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Just now"
    }
}
