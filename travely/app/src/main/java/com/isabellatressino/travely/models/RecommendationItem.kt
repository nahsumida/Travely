data class RecommendationItem(
    val id: String,
    val name: String,
    val predictedInterest: Double,
    val subtypes: List<String>
)
