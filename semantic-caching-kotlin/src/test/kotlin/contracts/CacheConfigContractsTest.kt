package contracts

import kotlin.test.Test
import kotlin.test.assertFailsWith
import semanticcache.CacheConfig

class CacheConfigContractsTest {
    @Test
    fun `requires hit threshold to be strictly greater than edge threshold`() {
        invalidConfig(edgeThreshold = 0.5, hitThreshold = 0.5)
        invalidConfig(edgeThreshold = 0.8, hitThreshold = 0.7)
    }

    @Test
    fun `rejects thresholds outside zero through one`() {
        invalidConfig(edgeThreshold = -0.01, hitThreshold = 0.5)
        invalidConfig(edgeThreshold = 0.5, hitThreshold = 1.01)
        invalidConfig(edgeThreshold = 1.01, hitThreshold = 1.02)
        invalidConfig(edgeThreshold = Double.NaN, hitThreshold = 0.9)
    }

    private fun invalidConfig(edgeThreshold: Double, hitThreshold: Double) {
        assertFailsWith<IllegalArgumentException> {
            CacheConfig(edgeThreshold, hitThreshold, searchStarts = 1, neighborsPerStart = 1)
        }
    }
}
