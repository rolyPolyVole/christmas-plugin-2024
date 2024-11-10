package gg.flyte.christmas.minigame.engine

enum class PlayerType {
    /**
     * A player who is actively participating in the event.
     */
    PARTICIPANT,

    /**
     * A player who is in the server/event but has opted out of participating in any minigames.
     */
    OPTED_OUT,

    /**
     * The designated camera player providing a live feed of the event.
     */
    CAMERA
}