package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteMedia(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val mediaType: String, // "movie" or "tv"
    val rating: Double,
    val overview: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class WatchHistory(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: Int, // TMDB ID
    val title: String,
    val posterPath: String?,
    val mediaType: String, // "movie" or "tv"
    val season: Int? = null,
    val episode: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
