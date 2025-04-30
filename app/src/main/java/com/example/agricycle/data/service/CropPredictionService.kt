package com.example.agricycle.data.service

import com.example.agricycle.data.model.CropPrediction
import com.example.agricycle.data.model.FieldAnalysis
import com.example.agricycle.data.model.RecommendedCrop
import kotlinx.coroutines.delay
import kotlin.random.Random

class CropPredictionService {
    suspend fun analyzeField(
        soilType: String,
        soilPh: Double,
        nitrogenLevel: Double,
        phosphorusLevel: Double,
        potassiumLevel: Double,
        organicMatter: Double,
        moistureContent: Double,
        temperature: Double,
        rainfall: Double,
        sunlightHours: Double
    ): FieldAnalysis {
        // Simulate API call delay
        delay(1000)
        
        return FieldAnalysis(
            soilType = soilType,
            soilPh = soilPh,
            nitrogenLevel = nitrogenLevel,
            phosphorusLevel = phosphorusLevel,
            potassiumLevel = potassiumLevel,
            organicMatter = organicMatter,
            moistureContent = moistureContent,
            temperature = temperature,
            rainfall = rainfall,
            sunlightHours = sunlightHours
        )
    }

    suspend fun predictCrops(fieldAnalysis: FieldAnalysis): CropPrediction {
        // Simulate API call delay
        delay(1500)

        // This is a simplified prediction algorithm
        // In a real app, this would use machine learning or call an external API
        val crops = listOf(
            RecommendedCrop(
                name = "Maize",
                suitability = calculateSuitability(fieldAnalysis, "Maize"),
                expectedYield = calculateExpectedYield(fieldAnalysis, "Maize"),
                plantingSeason = "Rainy Season",
                waterRequirement = "Moderate",
                fertilizerRequirement = "High"
            ),
            RecommendedCrop(
                name = "Rice",
                suitability = calculateSuitability(fieldAnalysis, "Rice"),
                expectedYield = calculateExpectedYield(fieldAnalysis, "Rice"),
                plantingSeason = "Wet Season",
                waterRequirement = "High",
                fertilizerRequirement = "Moderate"
            ),
            RecommendedCrop(
                name = "Wheat",
                suitability = calculateSuitability(fieldAnalysis, "Wheat"),
                expectedYield = calculateExpectedYield(fieldAnalysis, "Wheat"),
                plantingSeason = "Winter",
                waterRequirement = "Low",
                fertilizerRequirement = "Moderate"
            )
        ).sortedByDescending { it.suitability }

        return CropPrediction(
            recommendedCrops = crops,
            yieldPrediction = crops.first().expectedYield,
            confidence = Random.nextDouble(0.7, 0.95)
        )
    }

    private fun calculateSuitability(fieldAnalysis: FieldAnalysis, crop: String): Double {
        // Simplified suitability calculation
        var score = 0.0
        
        when (crop) {
            "Maize" -> {
                score += if (fieldAnalysis.soilPh in 5.5..7.5) 0.3 else 0.1
                score += if (fieldAnalysis.nitrogenLevel > 20) 0.2 else 0.1
                score += if (fieldAnalysis.temperature in 20.0..30.0) 0.3 else 0.1
                score += if (fieldAnalysis.rainfall in 500.0..1000.0) 0.2 else 0.1
            }
            "Rice" -> {
                score += if (fieldAnalysis.soilPh in 5.0..6.5) 0.3 else 0.1
                score += if (fieldAnalysis.moistureContent > 60) 0.3 else 0.1
                score += if (fieldAnalysis.temperature in 25.0..35.0) 0.2 else 0.1
                score += if (fieldAnalysis.rainfall > 1000.0) 0.2 else 0.1
            }
            "Wheat" -> {
                score += if (fieldAnalysis.soilPh in 6.0..7.5) 0.3 else 0.1
                score += if (fieldAnalysis.temperature in 15.0..25.0) 0.3 else 0.1
                score += if (fieldAnalysis.rainfall in 300.0..600.0) 0.2 else 0.1
                score += if (fieldAnalysis.sunlightHours > 6) 0.2 else 0.1
            }
        }
        
        return score
    }

    private fun calculateExpectedYield(fieldAnalysis: FieldAnalysis, crop: String): Double {
        // Simplified yield calculation
        val baseYield = when (crop) {
            "Maize" -> 3000.0
            "Rice" -> 4000.0
            "Wheat" -> 2500.0
            else -> 2000.0
        }
        
        val suitability = calculateSuitability(fieldAnalysis, crop)
        return baseYield * suitability
    }
} 