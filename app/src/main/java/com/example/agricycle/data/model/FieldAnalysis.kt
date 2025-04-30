package com.example.agricycle.data.model

data class FieldAnalysis(
    val soilType: String,
    val soilPh: Double,
    val nitrogenLevel: Double,
    val phosphorusLevel: Double,
    val potassiumLevel: Double,
    val organicMatter: Double,
    val moistureContent: Double,
    val temperature: Double,
    val rainfall: Double,
    val sunlightHours: Double
)

data class CropPrediction(
    val recommendedCrops: List<RecommendedCrop>,
    val yieldPrediction: Double,
    val confidence: Double
)

data class RecommendedCrop(
    val name: String,
    val suitability: Double,
    val expectedYield: Double,
    val plantingSeason: String,
    val waterRequirement: String,
    val fertilizerRequirement: String
) 