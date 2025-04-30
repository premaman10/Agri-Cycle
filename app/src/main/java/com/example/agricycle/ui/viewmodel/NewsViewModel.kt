package com.example.agricycle.ui.viewmodel

import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.example.agricycle.AgricycleApplication
import androidx.lifecycle.viewModelScope

import com.example.agricycle.data.model.Article
import com.example.agricycle.data.model.NewsResponse
import com.example.agricycle.data.network.NewsApi
import com.example.agricycle.data.network.NewsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val _newsArticles = MutableStateFlow<List<Article>>(emptyList())
    val newsArticles: StateFlow<List<Article>> = _newsArticles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _language = MutableStateFlow("English")
    val language: StateFlow<String> = _language.asStateFlow()

    private val newsService = Retrofit.Builder()
        .baseUrl(NewsApi.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NewsService::class.java)

    init {
        fetchNews()
    }
    
    fun setLanguage(lang: String) {
        _language.value = lang
        fetchNews() // Refresh news with new language
    }

    private fun fetchNews() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
    
                // Check network connectivity first
                if (!isNetworkAvailable()) {
                    _error.value = "No internet connection available"
                    return@launch
                }
    
                val response = newsService.getNews(
                    query = "agriculture",
                    lang = _language.value,
                    country = "in",
                    max = 30,
                    apikey = "a4f7689214de80a0fdff760ea8a9c940"
                )
    
                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        if (newsResponse.articles.isNotEmpty()) {
                            _newsArticles.value = newsResponse.articles.shuffled().take(30)
                            _error.value = null
                        } else {
                            _error.value = "No news articles found"
                        }
                    } ?: run {
                        _error.value = "Empty response from server"
                    }
                } else {
                    _error.value = "Failed to fetch news: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = when (e) {
                    is java.net.UnknownHostException -> "Unable to resolve host. Please check your internet connection."
                    is java.net.SocketTimeoutException -> "Connection timed out. Please check your network connection."
                    else -> "Network error: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<AgricycleApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    fun retryFetchNews() {
        fetchNews()
    }
}