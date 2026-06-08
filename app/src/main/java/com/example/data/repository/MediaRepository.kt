package com.example.data.repository

import com.example.data.api.TmdbMedia
import com.example.data.api.TmdbMovieDetail
import com.example.data.api.TmdbSeasonResponse
import com.example.data.api.TmdbService
import com.example.data.api.TmdbTvDetail
import com.example.data.api.TmdbCreditResponse
import com.example.data.db.FavoriteDao
import com.example.data.db.FavoriteMedia
import com.example.data.db.HistoryDao
import com.example.data.db.WatchHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MediaRepository(
    private val api: TmdbService,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao
) {
    // API Queries
    fun getTrendingMovies() = flow {
        emit(api.getTrendingMovies().results)
    }

    fun getTrendingTv() = flow {
        emit(api.getTrendingTv().results)
    }

    fun getPopularMovies() = flow {
        emit(api.getPopularMovies().results)
    }

    fun getTopRatedMovies() = flow {
        emit(api.getTopRatedMovies().results)
    }

    fun getPopularTv() = flow {
        emit(api.getPopularTv().results)
    }

    fun getTopRatedTv() = flow {
        emit(api.getTopRatedTv().results)
    }

    fun searchMulti(query: String) = flow {
        if (query.trim().isEmpty()) {
            emit(emptyList<TmdbMedia>())
        } else {
            emit(api.searchMulti(query).results)
        }
    }

    suspend fun getMovieDetails(movieId: Int): TmdbMovieDetail {
        return api.getMovieDetails(movieId)
    }

    suspend fun getTvDetails(tvId: Int): TmdbTvDetail {
        return api.getTvDetails(tvId)
    }

    suspend fun getSeasonEpisodes(tvId: Int, seasonNumber: Int): TmdbSeasonResponse {
        return api.getSeasonEpisodes(tvId, seasonNumber)
    }

    suspend fun getMovieCredits(movieId: Int): TmdbCreditResponse {
        return api.getMovieCredits(movieId)
    }

    suspend fun getTvCredits(tvId: Int): TmdbCreditResponse {
        return api.getTvCredits(tvId)
    }

    suspend fun getSimilarMovies(movieId: Int): List<TmdbMedia> {
        return api.getSimilarMovies(movieId).results
    }

    suspend fun getSimilarTv(tvId: Int): List<TmdbMedia> {
        return api.getSimilarTv(tvId).results
    }

    // Local Favorites
    val favorites: Flow<List<FavoriteMedia>> = favoriteDao.getAllFavorites()

    suspend fun isFavorite(id: Int): Boolean {
        return favoriteDao.getFavoriteById(id) != null
    }

    suspend fun addFavorite(media: FavoriteMedia) {
        favoriteDao.insertFavorite(media)
    }

    suspend fun removeFavorite(id: Int) {
        favoriteDao.deleteFavoriteById(id)
    }

    // Local History
    val historyItems: Flow<List<WatchHistory>> = historyDao.getAllHistory()

    suspend fun insertHistory(history: WatchHistory) {
        // Remove or replace custom checks to prevent duplicating continuous views if needed
        historyDao.insertHistory(history)
    }

    suspend fun deleteHistory(localId: Int) {
        historyDao.deleteHistoryById(localId)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}
