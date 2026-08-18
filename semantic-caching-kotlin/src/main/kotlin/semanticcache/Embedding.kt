package semanticcache

import kotlin.math.sqrt

class Embedding private constructor(
    private val components: List<Double>,
) {
    val dimension: Int
        get() = components.size

    fun cosineSimilarity(other: Embedding): Double {
        require(dimension == other.dimension) {
            "Embedding dimensions must match: $dimension != ${other.dimension}"
        }

        val dotProduct = components.indices.sumOf { components[it] * other.components[it] }
        return (dotProduct / (magnitude * other.magnitude)).coerceIn(0.0, 1.0)
    }

    private val magnitude = sqrt(components.sumOf { it * it })

    override fun equals(other: Any?): Boolean =
        this === other || other is Embedding && components == other.components

    override fun hashCode(): Int = components.hashCode()

    override fun toString(): String = "Embedding(dimension=$dimension)"

    companion object {
        fun of(vararg components: Double): Embedding = of(components.asList())

        fun of(components: List<Double>): Embedding {
            require(components.isNotEmpty()) { "Embedding must not be empty" }
            require(components.all { it.isFinite() }) { "Embedding components must be finite" }
            require(components.all { it >= 0.0 }) { "Embedding components must be non-negative" }
            require(components.any { it > 0.0 }) { "Embedding must not be a zero vector" }
            return Embedding(components.toList())
        }
    }
}
