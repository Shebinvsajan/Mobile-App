package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {

    @GET("3/trending/movie/day")
    suspend fun getTrendingMovies(): TmdbResponse<TmdbMedia>

    @GET("3/trending/tv/day")
    suspend fun getTrendingTv(): TmdbResponse<TmdbMedia>

    @GET("3/movie/popular")
    suspend fun getPopularMovies(): TmdbResponse<TmdbMedia>

    @GET("3/movie/top_rated")
    suspend fun getTopRatedMovies(): TmdbResponse<TmdbMedia>

    @GET("3/tv/popular")
    suspend fun getPopularTv(): TmdbResponse<TmdbMedia>

    @GET("3/tv/top_rated")
    suspend fun getTopRatedTv(): TmdbResponse<TmdbMedia>

    @GET("3/search/multi")
    suspend fun searchMulti(
        @Query("query") query: String
    ): TmdbResponse<TmdbMedia>

    @GET("3/movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int
    ): TmdbMovieDetail

    @GET("3/tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int
    ): TmdbTvDetail

    @GET("3/tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonEpisodes(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int
    ): TmdbSeasonResponse

    @GET("3/movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int
    ): TmdbCreditResponse

    @GET("3/tv/{tv_id}/credits")
    suspend fun getTvCredits(
        @Path("tv_id") tvId: Int
    ): TmdbCreditResponse

    @GET("3/movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") movieId: Int
    ): TmdbResponse<TmdbMedia>

    @GET("3/tv/{tv_id}/similar")
    suspend fun getSimilarTv(
        @Path("tv_id") tvId: Int
    ): TmdbResponse<TmdbMedia>

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/"

        // Hardcoded safety fallback token provided by user
        private const val DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwNjgyYmM3YWIyNzcyYTM3MDA4MTRiZjA5N2M4OWNkOCIsIm5iZiI6MTc1NjcwMTU1MC40ODUsInN1YiI6IjY4YjUyMzZlNjU5MDhiMGZhODIwMGJjOSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.q2U0zu7xyDWGZS4wCtUKikavY9Q8q41dqTd4jEhA8_s"

        fun create(): TmdbService {
            // Get token from BuildConfig, fallback if empty, null, or matching placeholder
            val tokenFromConfig = try {
                BuildConfig.TMDB_READ_ACCESS_TOKEN
            } catch (e: Exception) {
                ""
            }
            
            val validToken = if (tokenFromConfig.isNullOrEmpty() || tokenFromConfig.contains("YOUR_") || tokenFromConfig == "TMDB_READ_ACCESS_TOKEN") {
                DEFAULT_TOKEN
            } else {
                tokenFromConfig
            }

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Authorization", "Bearer $validToken")
                        .header("accept", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(TmdbService::class.java)
        }
    }
}
