package gg.flyte.christmas.minigame.engine

enum class GameState {
    /**
     * The game has been selected, but is not running.
     */
    IDLE,
    /**
     * The game is waiting for players to join.
     */
    WAITING_FOR_PLAYERS,
    /**
     * The game is counting down to start.
     */
    COUNTDOWN,
    /**
     * The game is running.
     */
    LIVE,
}
