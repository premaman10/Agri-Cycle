package com.example.agricycle.data.network

import com.example.agricycle.data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {
    @GET("search")
    suspend fun getNews(
        @Query("q") query: String = "agriculture",
        @Query("lang") lang: String = "en",
        @Query("country") country: String = "in",
        @Query("max") max: Int = 10,
        @Query("apikey") apikey: String = "a4f7689214de80a0fdff760ea8a9c940"
    ): Response<NewsResponse>
}

object NewsApi {
    const val BASE_URL = "https://gnews.io/api/v4/"
}