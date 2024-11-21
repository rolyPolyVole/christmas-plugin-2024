package gg.flyte.christmas.donation

/**
 * Enum class representing the donation tiers. Each implementation of [gg.flyte.christmas.minigame.engine.EventMiniGame] will
 * have different actions depending on the tier of the donation. (Passed through [gg.flyte.christmas.minigame.engine.EventMiniGame.handleDonation]
 */
enum class DonationTier(
    val min: Double,
    val max: Double
) {
    /**
     * Represents the LOW donation tier.
     */
    LOW(0.0, 15.0),

    /**
     * Represents the MEDIUM donation tier.
     */
    MEDIUM(15.01, 35.0),

    /**
     * Represents the HIGH donation tier.
     */
    HIGH(35.01, Double.MAX_VALUE);

    companion object {
        /**
         * Returns the mapped donation tier based on the donation amount.
         * @param donation The donation amount.
         * @return The donation tier. Defaults to [LOW] in the rare case that a tier could not be found.
         */
        fun getTier(donation: Double): DonationTier {
            return DonationTier.entries.find { donation in it.min..it.max } ?: LOW // Default to LOW if no tier is found (
        }
    }
}