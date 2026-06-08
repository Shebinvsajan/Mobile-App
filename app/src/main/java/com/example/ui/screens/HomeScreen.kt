package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MediaRow
import com.example.ui.components.SpotlightBannerCarousel
import com.example.ui.viewmodel.MediaViewModel

@Composable
fun HomeScreen(
    viewModel: MediaViewModel,
    onNavigateToDetails: (id: Int, type: String) -> Unit,
    onNavigateToPlayer: (id: Int, type: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingMovies by viewModel.trendingMovies.collectAsState()
    val trendingTv by viewModel.trendingTv.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()
    val topRatedTv by viewModel.topRatedTv.collectAsState()
    
    val isLoading by viewModel.isLoadingHome.collectAsState()
    val error by viewModel.homeError.collectAsState()

    // Select up to 5 beautiful spotlight items for Cineverse carousel slider
    val spotlightItems = remember(trendingMovies, trendingTv) {
        val combined = (trendingMovies + trendingTv).distinctBy { it.id }
        combined.take(5)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0C))
    ) {
        if (isLoading && spotlightItems.isEmpty()) {
            CircularProgressIndicator(
                color = Color(0xFFE50914),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        } else if (error != null && spotlightItems.isEmpty()) {
            Text(
                text = error ?: "Unknown error",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Smooth-sliding Carousel Banner
                if (spotlightItems.isNotEmpty()) {
                    SpotlightBannerCarousel(
                        items = spotlightItems,
                        onPlayClick = { media ->
                            onNavigateToPlayer(media.id, media.resolvedMediaType)
                        },
                        onDetailsClick = { media ->
                            onNavigateToDetails(media.id, media.resolvedMediaType)
                        },
                        onSearchClick = onNavigateToSearch
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Top Ten Movies section
                val topTenMovies = remember(trendingMovies) {
                    trendingMovies.take(10)
                }
                MediaRow(
                    title = "Top 10 Movies Today",
                    items = topTenMovies,
                    onMediaClick = onNavigateToDetails
                )

                // Trending Series section
                MediaRow(
                    title = "Trending Animated & TV Series",
                    items = trendingTv,
                    onMediaClick = onNavigateToDetails
                )

                // Popular Movies section
                MediaRow(
                    title = "Popular Blockbusters",
                    items = popularMovies,
                    onMediaClick = onNavigateToDetails
                )

                // Top Rated Series section
                MediaRow(
                    title = "Top Rated TV Series",
                    items = topRatedTv,
                    onMediaClick = onNavigateToDetails
                )

                Spacer(modifier = Modifier.height(96.dp)) // generous space for floating capsule dock
            }
        }
    }
}
