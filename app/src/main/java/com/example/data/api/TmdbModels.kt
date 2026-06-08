package com.example.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbResponse<T>(
    val page: Int?,
    val results: List<T>
)

@JsonClass(generateAdapter = true)
data class TmdbGenre(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class TmdbSeason(
    val id: Int,
    val name: String?,
    val season_number: Int,
    val episode_count: Int?,
    val poster_path: String?
)

@JsonClass(generateAdapter = true)
data class TmdbEpisode(
    val id: Int,
    val name: String?,
    val episode_number: Int,
    val season_number: Int,
    val overview: String?,
    val still_path: String?,
    val vote_average: Double?
)

@JsonClass(generateAdapter = true)
data class TmdbMedia(
    val id: Int,
    val title: String?, // Movies uses this
    val name: String?,  // TV shows uses this
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val media_type: String?, // "movie" or "tv"
    val vote_average: Double?,
    val release_date: String?,
    val first_air_date: String?
) {
    // Utility helpers
    val displayTitle: String
        get() = title ?: name ?: "Untitled"

    val displayDate: String
        get() = release_date ?: first_air_date ?: ""

    val resolvedMediaType: String
        get() = media_type ?: if (title != null) "movie" else "tv"
}

@JsonClass(generateAdapter = true)
data class TmdbMovieDetail(
    val id: Int,
    val title: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double?,
    val release_date: String?,
    val runtime: Int?,
    val genres: List<TmdbGenre>?
)

@JsonClass(generateAdapter = true)
data class TmdbTvDetail(
    val id: Int,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double?,
    val first_air_date: String?,
    val seasons: List<TmdbSeason>?,
    val genres: List<TmdbGenre>?
)

@JsonClass(generateAdapter = true)
data class TmdbSeasonResponse(
    val id: Int,
    val season_number: Int,
    val episodes: List<TmdbEpisode>?
)

@JsonClass(generateAdapter = true)
data class TmdbCast(
    val id: Int,
    val name: String,
    val character: String?,
    val profile_path: String?
)

@JsonClass(generateAdapter = true)
data class TmdbCreditResponse(
    val id: Int,
    val cast: List<TmdbCast>?
)

