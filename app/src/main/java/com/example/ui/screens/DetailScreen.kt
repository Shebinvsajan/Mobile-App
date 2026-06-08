package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.viewmodel.MediaViewModel
import com.example.data.api.TmdbEpisode
import com.example.data.api.TmdbSeason
import com.example.data.api.TmdbGenre
import com.example.data.api.TmdbCast
import com.example.data.api.TmdbMedia

private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"
private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
private const val STILL_BASE_URL = "https://image.tmdb.org/t/p/w300"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    mediaId: Int,
    mediaType: String,
    viewModel: MediaViewModel,
    onBack: () -> Unit,
    onPlayMedia: (id: Int, type: String, season: Int?, episode: Int?) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDetails: ((Int, String) -> Unit)? = null
) {
    // Select media details on launch
    LaunchedEffect(mediaId, mediaType) {
        viewModel.selectMedia(mediaId, mediaType)
    }

    val movieDetail by viewModel.selectedMovieDetail.collectAsState()
    val tvDetail by viewModel.selectedTvDetail.collectAsState()
    val episodes by viewModel.selectedSeasonEpisodes.collectAsState()
    val selectedSeasonNum by viewModel.selectedSeasonNumber.collectAsState()

    val isLoading by viewModel.isLoadingDetail.collectAsState()
    val error by viewModel.detailError.collectAsState()
    val isFavorite by viewModel.isCurrentFavorite.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color(0xFFE50914),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        } else if (error != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error ?: "Unknown Detail Error",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.selectMedia(mediaId, mediaType) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
                ) {
                    Text("Retry", color = Color.White)
                }
            }
        } else {
            // Success State
            val title = if (mediaType == "movie") movieDetail?.title ?: "" else tvDetail?.name ?: ""
            val overview = if (mediaType == "movie") movieDetail?.overview ?: "" else tvDetail?.overview ?: ""
            val backdropPath = if (mediaType == "movie") movieDetail?.backdrop_path else tvDetail?.backdrop_path
            val posterPath = if (mediaType == "movie") movieDetail?.poster_path else tvDetail?.poster_path
            val rating = if (mediaType == "movie") movieDetail?.vote_average ?: 0.0 else tvDetail?.vote_average ?: 0.0
            val releaseDate = if (mediaType == "movie") movieDetail?.release_date ?: "" else tvDetail?.first_air_date ?: ""
            val genresList = if (mediaType == "movie") movieDetail?.genres else tvDetail?.genres

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Large backdrop visual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    val imageUrl = (backdropPath ?: posterPath)?.let { "$BACKDROP_BASE_URL$it" }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlays
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        Color(0xFF121212)
                                    )
                                )
                            )
                    )
                }

                // Title + Main Metadata Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rating
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFDF00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", rating),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Year
                        val year = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else "N/A"
                        Text(
                            text = year,
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (mediaType == "movie") {
                            movieDetail?.runtime?.let { runtime ->
                                if (runtime > 0) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "${runtime}m",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Genres Row
                    if (!genresList.isNullOrEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            genresList.forEach { genre ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFF222222),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = genre.name,
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Play Button / Save to History action
                    if (mediaType == "movie") {
                        Button(
                            onClick = {
                                viewModel.saveToHistory(mediaId, title, posterPath, "movie")
                                onPlayMedia(mediaId, "movie", null, null)
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE50914),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Movie")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Stream", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Synopsis / Overview
                    Text(
                        text = "Synopsis",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = overview.ifEmpty { "No overview database available." },
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // TV SERIES EPISODES SELECTOR
                    if (mediaType == "tv" && tvDetail != null) {
                        tvDetail?.seasons?.let { seasonsList ->
                            if (seasonsList.isNotEmpty()) {
                                var dropdownExpanded by remember { mutableStateOf(false) }
                                val currentSeasonObj = seasonsList.find { it.season_number == selectedSeasonNum }
                                    ?: seasonsList.first()

                                Text(
                                    text = "Select Episodes",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Season dropdown trigger
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
                                        .clickable { dropdownExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = currentSeasonObj.name ?: "Season ${currentSeasonObj.season_number}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            tint = Color.White
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = dropdownExpanded,
                                        onDismissRequest = { dropdownExpanded = false },
                                        modifier = Modifier.background(Color(0xFF1E1E1E))
                                    ) {
                                        seasonsList.forEach { season ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = season.name ?: "Season ${season.season_number}",
                                                        color = Color.White,
                                                        fontSize = 14.sp
                                                    )
                                                },
                                                onClick = {
                                                    dropdownExpanded = false
                                                    viewModel.selectSeason(mediaId, season.season_number)
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Episodes horizontally scrolling list or direct listings row
                                if (episodes.isEmpty()) {
                                    Text(
                                        text = "Loading episodes...",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(bottom = 12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(episodes) { episode ->
                                            EpisodeCard(
                                                episode = episode,
                                                onClick = {
                                                    viewModel.saveToHistory(
                                                        id = mediaId,
                                                        title = "$title: S${episode.season_number}E${episode.episode_number}",
                                                        posterPath = posterPath,
                                                        mediaType = "tv",
                                                        season = episode.season_number,
                                                        episode = episode.episode_number
                                                    )
                                                    onPlayMedia(mediaId, "tv", episode.season_number, episode.episode_number)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cast List Rendering
                    val casts by viewModel.selectedCast.collectAsState()
                    if (casts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Cast & Crew",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(casts) { cast ->
                                CastItem(cast = cast)
                            }
                        }
                    }

                    // Similar Media Rendering
                    val similarList by viewModel.similarMedia.collectAsState()
                    if (similarList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (mediaType == "movie") "Similar Movies" else "Similar Series",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(similarList) { media ->
                                SimilarMediaItem(
                                    media = media,
                                    onClick = {
                                        if (onNavigateToDetails != null) {
                                            onNavigateToDetails.invoke(media.id, media.resolvedMediaType)
                                        } else {
                                            viewModel.selectMedia(media.id, media.resolvedMediaType)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // Overlaid sticky top buttons (Back and Favorite) for cinema design experience
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back"
                )
            }

            // Favorites trigger toggle matching TMDB entity parameters
            val detailTitle = if (mediaType == "movie") movieDetail?.title ?: "" else tvDetail?.name ?: ""
            val detailPoster = if (mediaType == "movie") movieDetail?.poster_path else tvDetail?.poster_path
            val detailBack = if (mediaType == "movie") movieDetail?.backdrop_path else tvDetail?.backdrop_path
            val detailRating = if (mediaType == "movie") movieDetail?.vote_average ?: 0.0 else tvDetail?.vote_average ?: 0.0
            val detailOverview = if (mediaType == "movie") movieDetail?.overview ?: "" else tvDetail?.overview ?: ""

            if (!isLoading && error == null && detailTitle.isNotEmpty()) {
                IconButton(
                    onClick = {
                        viewModel.toggleFavorite(
                            id = mediaId,
                            title = detailTitle,
                            posterPath = detailPoster,
                            backdropPath = detailBack,
                            mediaType = mediaType,
                            rating = detailRating,
                            overview = detailOverview
                        )
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = if (isFavorite) Color(0xFFE50914) else Color.White
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite"
                    )
                }
            }
        }
    }
}

@Composable
fun EpisodeCard(
    episode: TmdbEpisode,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                val imageUrl = episode.still_path?.let { "$STILL_BASE_URL$it" }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = episode.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Quick play button overlay on thumbnail center
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Streaming Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Ep Indicator Badge on Bottom Start
                Text(
                    text = "Episode ${episode.episode_number}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = episode.name ?: "Episode ${episode.episode_number}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = episode.overview?.trim()?.ifEmpty { "No overview available." } ?: "No overview available.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private const val PROFILE_BASE_URL = "https://image.tmdb.org/t/p/w185"

@Composable
fun CastItem(
    cast: TmdbCast,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(76.dp)
    ) {
        val imageUrl = cast.profile_path?.let { "$PROFILE_BASE_URL$it" }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = cast.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = cast.name,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        cast.character?.let { character ->
            if (character.isNotEmpty()) {
                Text(
                    text = character,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SimilarMediaItem(
    media: TmdbMedia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(100.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.DarkGray)
        ) {
            val imageUrl = media.poster_path?.let { "$POSTER_BASE_URL$it" }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = media.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Star rating badge overlay
            media.vote_average?.let { rating ->
                if (rating > 0) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                Color.Black.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFDF00),
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", rating),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = media.displayTitle,
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

