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
fun MoviesScreen(
    viewModel: MediaViewModel,
    onNavigateToDetails: (id: Int, type: String) -> Unit,
    onNavigateToPlayer: (id: Int, type: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingMovies by viewModel.trendingMovies.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()
    val topRatedMovies by viewModel.topRatedMovies.collectAsState()
    val isLoading by viewModel.isLoadingHome.collectAsState()

    // Up to 5 movie spotlight banners
    val movieSpotlights = remember(trendingMovies) {
        trendingMovies.take(5)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0C))
    ) {
        if (isLoading && movieSpotlights.isEmpty()) {
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
                // Multi-movie spotlight sliding banner
                if (movieSpotlights.isNotEmpty()) {
                    SpotlightBannerCarousel(
                        items = movieSpotlights,
                        onPlayClick = { media ->
                            onNavigateToPlayer(media.id, "movie")
                        },
                        onDetailsClick = { media ->
                            onNavigateToDetails(media.id, "movie")
                        },
                        onSearchClick = onNavigateToSearch
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                MediaRow(
                    title = "Trending Movies Today",
                    items = trendingMovies,
                    onMediaClick = onNavigateToDetails
                )

                MediaRow(
                    title = "Popular Blockbuster Releases",
                    items = popularMovies,
                    onMediaClick = onNavigateToDetails
                )

                MediaRow(
                    title = "Top Rated Movies of All Time",
                    items = topRatedMovies,
                    onMediaClick = onNavigateToDetails
                )

                Spacer(modifier = Modifier.height(96.dp)) // space for floating bottom bar navigation dock
            }
        }
    }
}
