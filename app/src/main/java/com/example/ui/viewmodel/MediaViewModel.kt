package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.TmdbEpisode
import com.example.data.api.TmdbMedia
import com.example.data.api.TmdbMovieDetail
import com.example.data.api.TmdbSeason
import com.example.data.api.TmdbService
import com.example.data.api.TmdbTvDetail
import com.example.data.api.TmdbCast
import com.example.data.api.TmdbCreditResponse
import com.example.data.db.AppDatabase
import com.example.data.db.FavoriteMedia
import com.example.data.db.WatchHistory
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MediaRepository

    init {
        val database = AppDatabase.getDatabase(application)
        val apiService = TmdbService.create()
        repository = MediaRepository(
            api = apiService,
            favoriteDao = database.favoriteDao(),
            historyDao = database.historyDao()
        )
    }

    // Home Page sections
    private val _trendingMovies = MutableStateFlow<List<TmdbMedia>>(emptyList())
    val trendingMovies = _trendingMovies.asStateFlow()

    private val _trendingTv = MutableStateFlow<List<TmdbMedia>>(emptyList())
    val trendingTv = _trendingTv.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<TmdbMedia>>(emptyList())
    val popularMovies = _popularMovies.asStateFlow()

    private val _topRatedMovies = MutableStateFlow<List<TmdbMedia>>(emptyList())
    val topRatedMovies = _topRatedMovies.asStateFlow()

    private val _popularTv = MutableStateFlow<List<TmdbMedia>>(emptyList())
    val popularTv = _popularTv.asStateFlow()

    private val _topRatedTv = MutableStateFlow<List<TmdbMedia>>(emptyList())
    val topRatedTv = _topRatedTv.asStateFlow()

    private val _isLoadingHome = MutableStateFlow(false)
    val isLoadingHome = _isLoadingHome.asStateFlow()

    private val _homeError = MutableStateFlow<String?>(null)
    val homeError = _homeError.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoadingHome.value = true
            _homeError.value = null
            try {
                // Fetch trending and categories in parallel or simple sequential lists
                repository.getTrendingMovies().collect { _trendingMovies.value = it }
                repository.getTrendingTv().collect { _trendingTv.value = it }
                repository.getPopularMovies().collect { _popularMovies.value = it }
                repository.getTopRatedMovies().collect { _topRatedMovies.value = it }
                repository.getPopularTv().collect { _popularTv.value = it }
                repository.getTopRatedTv().collect { _topRatedTv.value = it }
            } catch (e: Exception) {
                _homeError.value = "Failed to load media. Please check your internet connection."
            } finally {
                _isLoadingHome.value = false
            }
        }
    }

    // Search Mode
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<TmdbMedia>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                repository.searchMulti(query).collect { results ->
                    // Multi-search can return person items. Keep only media (movie/tv) with titles.
                    _searchResults.value = results.filter { it.title != null || it.name != null }
                }
            } catch (e: Exception) {
                // Fail silently on keystroke error
            } finally {
                _isSearching.value = false
            }
        }
    }

    // Detail Page Mode
    private val _selectedMovieDetail = MutableStateFlow<TmdbMovieDetail?>(null)
    val selectedMovieDetail = _selectedMovieDetail.asStateFlow()

    private val _selectedTvDetail = MutableStateFlow<TmdbTvDetail?>(null)
    val selectedTvDetail = _selectedTvDetail.asStateFlow()

    private val _selectedSeasonEpisodes = MutableStateFlow<List<TmdbEpisode>>(emptyList())
    val selectedSeasonEpisodes = _selectedSeasonEpisodes.asStateFlow()

    private val _selectedCast = MutableStateFlow<List<TmdbCast>>(emptyList())
    val selectedCast = _selectedCast.asStateFlow()

    private val _similarMedia = MutableStateFlow<List<TmdbMedia>>(emptyList())
    val similarMedia = _similarMedia.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail = _isLoadingDetail.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError = _detailError.asStateFlow()

    private val _isCurrentFavorite = MutableStateFlow(false)
    val isCurrentFavorite = _isCurrentFavorite.asStateFlow()

    private val _selectedSeasonNumber = MutableStateFlow<Int?>(null)
    val selectedSeasonNumber = _selectedSeasonNumber.asStateFlow()

    fun selectMedia(id: Int, type: String) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _detailError.value = null
            _selectedMovieDetail.value = null
            _selectedTvDetail.value = null
            _selectedSeasonEpisodes.value = emptyList()
            _selectedSeasonNumber.value = null
            _selectedCast.value = emptyList()
            _similarMedia.value = emptyList()
            
            // Check favorite state
            _isCurrentFavorite.value = repository.isFavorite(id)

            try {
                if (type == "movie") {
                    val detail = repository.getMovieDetails(id)
                    _selectedMovieDetail.value = detail
                    
                    // Fetch credits and similar movies in background of detail selection
                    launch {
                        try {
                            _selectedCast.value = repository.getMovieCredits(id).cast?.take(15) ?: emptyList()
                        } catch (e: Exception) {
                            _selectedCast.value = emptyList()
                        }
                    }
                    launch {
                        try {
                            _similarMedia.value = repository.getSimilarMovies(id).take(15)
                        } catch (e: Exception) {
                            _similarMedia.value = emptyList()
                        }
                    }
                } else {
                    val tvDetail = repository.getTvDetails(id)
                    _selectedTvDetail.value = tvDetail
                    
                    // Fetch credits and similar TV shows in background of detail selection
                    launch {
                        try {
                            _selectedCast.value = repository.getTvCredits(id).cast?.take(15) ?: emptyList()
                        } catch (e: Exception) {
                            _selectedCast.value = emptyList()
                        }
                    }
                    launch {
                        try {
                            _similarMedia.value = repository.getSimilarTv(id).take(15)
                        } catch (e: Exception) {
                            _similarMedia.value = emptyList()
                        }
                    }

                    // Auto-select first season if available
                    val seasons = tvDetail.seasons
                    if (!seasons.isNullOrEmpty()) {
                        // Find the first non-zero or first seasonal index
                        val firstSeason = seasons.firstOrNull { it.season_number > 0 } ?: seasons.first()
                        selectSeason(id, firstSeason.season_number)
                    }
                }
            } catch (e: Exception) {
                _detailError.value = "Failed to load media details."
            } finally {
                _isLoadingDetail.value = false
            }
        }
    }

    fun selectSeason(tvId: Int, seasonNumber: Int) {
        _selectedSeasonNumber.value = seasonNumber
        viewModelScope.launch {
            try {
                val response = repository.getSeasonEpisodes(tvId, seasonNumber)
                _selectedSeasonEpisodes.value = response.episodes ?: emptyList()
            } catch (e: Exception) {
                _selectedSeasonEpisodes.value = emptyList()
            }
        }
    }

    fun toggleFavorite(id: Int, title: String, posterPath: String?, backdropPath: String?, mediaType: String, rating: Double, overview: String) {
        viewModelScope.launch {
            if (_isCurrentFavorite.value) {
                repository.removeFavorite(id)
                _isCurrentFavorite.value = false
            } else {
                val fav = FavoriteMedia(
                    id = id,
                    title = title,
                    posterPath = posterPath,
                    backdropPath = backdropPath,
                    mediaType = mediaType,
                    rating = rating,
                    overview = overview
                )
                repository.addFavorite(fav)
                _isCurrentFavorite.value = true
            }
        }
    }

    // Local lists directly exposed from Flow for Profiles, Favorites, History
    val favoriteList: StateFlow<List<FavoriteMedia>> = repository.favorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val historyList: StateFlow<List<WatchHistory>> = repository.historyItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Save playing to history
    fun saveToHistory(id: Int, title: String, posterPath: String?, mediaType: String, season: Int? = null, episode: Int? = null) {
        viewModelScope.launch {
            val history = WatchHistory(
                id = id,
                title = title,
                posterPath = posterPath,
                mediaType = mediaType,
                season = season,
                episode = episode,
                timestamp = System.currentTimeMillis()
            )
            repository.insertHistory(history)
        }
    }

    fun deleteHistoryItem(localId: Int) {
        viewModelScope.launch {
            repository.deleteHistory(localId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
