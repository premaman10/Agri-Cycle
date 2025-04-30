# Agricycle - Smart Farming Assistant

## Overview
Agricycle is a comprehensive Android application designed to assist farmers with weather monitoring, field analysis, and crop management.

## Key Features

### 1. Weather Monitoring
- Real-time weather data using Open-Meteo API
- 7-day weather forecasts
- Soil temperature and moisture tracking
- Precipitation predictions
- Interactive weather charts

### 2. Field Analysis
- Field mapping and management
- Soil health monitoring
- Crop growth tracking
- Field activity logging
- Custom field naming and organization

### 3. Account Management
- Google Sign-In integration
- User profile management
- Multi-language support (English/Hindi)
- Secure authentication
- Personalized settings

## Technical Stack

### Frontend
- Jetpack Compose for modern UI
- Material3 Design System
- Vico Charts for data visualization
- Coil for image loading
- Navigation Compose for routing

### Backend
- Firebase Authentication
- Firebase Realtime Database
- Open-Meteo API for weather data
- Google Maps API for field mapping

### Architecture
- MVVM (Model-View-ViewModel) pattern
- Repository pattern for data management
- Kotlin Coroutines for asynchronous operations
- StateFlow for reactive programming
- Dependency Injection with Hilt

## API Integration

### Open-Meteo API
- Endpoint: https://api.open-meteo.com/v1/forecast
- Features:
  - Hourly weather forecasts
  - Soil temperature data
  - Soil moisture levels
  - Precipitation predictions
  - Historical weather data

### GNews API
- Endpoint: https://gnews.io/api/v4
- Features:
  - Real-time agricultural news
  - Customizable news categories
  - Location-based news filtering
  - Multiple language support
  - News search functionality
- Integration:
  ```kotlin
  data class NewsResponse(
      val articles: List<Article>,
      val totalArticles: Int
  )

  data class Article(
      val title: String,
      val description: String,
      val content: String,
      val url: String,
      val image: String?,
      val publishedAt: String,
      val source: Source
  )

  data class Source(
      val name: String,
      val url: String
  )
  ```

### Firebase Services
- Authentication
  - Google Sign-In
  - User profile management
  - Secure session handling
- Realtime Database
  - Field data storage
  - User preferences
  - Activity logs

### Google Maps API
- Field mapping
- Location services
- Geocoding
- Distance calculations

## Data Models

### Weather Data
```kotlin
data class WeatherData(
    val hourly: HourlyData,
    val daily: DailyData
)

data class HourlyData(
    val time: List<String>,
    val temperature2m: List<Float>,
    val soilTemperature0cm: List<Float>,
    val soilMoisture0To1cm: List<Float>,
    val precipitation: List<Float>
)
```

### Field Data
```kotlin
data class Field(
    val id: String,
    val name: String,
    val location: LatLng,
    val area: Double,
    val cropType: String,
    val lastActivity: String
)
```

## UI Components

### 1. Weather Screen
- Current weather conditions
- Temperature charts
- Soil moisture graphs
- Precipitation forecasts
- Interactive data visualization

### 2. Analysis Screen
- Field overview
- Soil health metrics
- Crop growth tracking
- Activity timeline
- Data analytics

### 3. Account Screen
- User profile
- Language settings
- Account management
- Sign-out functionality

## Security Features
- Secure authentication
- Data encryption
- Protected API keys
- User session management
- Privacy controls

## Future Enhancements
- Machine learning for crop prediction
- Advanced soil analysis
- Weather pattern recognition
- Crop disease detection
- Market price tracking
- Community features

## Conclusion
Agricycle provides farmers with a powerful tool for modern agriculture, combining weather data, field analysis, and smart farming techniques to improve crop yields and farming efficiency. 