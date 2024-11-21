package gg.flyte.christmas.donation


enum class DonationTier(
    val min: Double,
    val max: Double
) {
    LOW(0.0, 15.0),
    MEDIUM(15.01, 35.0),
    HIGH(35.01, Double.MAX_VALUE);

    companion object {
        fun getTier(donation: Double): DonationTier {
            return DonationTier.entries.find { donation in it.min..it.max } ?: LOW // Default to LOW if no tier is found (
        }
    }
}