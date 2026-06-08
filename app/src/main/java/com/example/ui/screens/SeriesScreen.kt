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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.MediaRow
import com.example.ui.components.SpotlightBannerCarousel
import com.example.ui.viewmodel.MediaViewModel

@Composable
fun SeriesScreen(
    viewModel: MediaViewModel,
    onNavigateToDetails: (id: Int, type: String) -> Unit,
    onNavigateToPlayer: (id: Int, type: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingTv by viewModel.trendingTv.collectAsState()
    val popularTv by viewModel.popularTv.collectAsState()
    val topRatedTv by viewModel.topRatedTv.collectAsState()
    val isLoading by viewModel.isLoadingHome.collectAsState()

    // Up to 5 TV series spotlights
    val seriesSpotlights = remember(trendingTv) {
        trendingTv.take(5)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0C))
    ) {
        if (isLoading && seriesSpotlights.isEmpty()) {
            CircularProgressIndicator(
                color = Color(0xFFE50914),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // TV top trending banner
                if (seriesSpotlights.isNotEmpty()) {
                    SpotlightBannerCarousel(
                        items = seriesSpotlights,
                        onPlayClick = { media ->
                            onNavigateToPlayer(media.id, "tv")
                        },
                        onDetailsClick = { media ->
                            onNavigateToDetails(media.id, "tv")
                        },
                        onSearchClick = onNavigateToSearch
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                MediaRow(
                    title = "Trending Animated & TV Series",
                    items = trendingTv,
                    onMediaClick = onNavigateToDetails
                )

                MediaRow(
                    title = "Popular TV Shows",
                    items = popularTv,
                    onMediaClick = onNavigateToDetails
                )

                MediaRow(
                    title = "Critics Choice TV",
                    items = topRatedTv,
                    onMediaClick = onNavigateToDetails
                )

                Spacer(modifier = Modifier.height(96.dp)) // space for floating bottom bar navigation dock
            }
        }
    }
}
