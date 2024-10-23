package gg.flyte.christmas.minigame.engine

import gg.flyte.christmas.minigame.games.BlockParty
import gg.flyte.christmas.minigame.world.GameRegion
import gg.flyte.christmas.minigame.world.MapRegion
import gg.flyte.christmas.minigame.world.MapSinglePoint
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import kotlin.reflect.KClass

enum class GameConfig(
    val gameClass: KClass<out EventMiniGame>,
    val displayName: Component,
    val colour: TextColor,
    val spawnPoints: List<MapRegion>,
    val spectatorSpawnLocations: List<MapSinglePoint>,
    val spectatorCameraLocations: List<MapSinglePoint>,
    val overviewLocations: List<MapSinglePoint>,
    val instructions: String,
    val menuMaterial: Material,
    val gameRegion: GameRegion,
    val minPlayers: Int,
) {
//    BAUBLE_TAG

    BLOCK_PARTY(
        BlockParty::class,
        Component.text("Block Party").color(TextColor.color(224, 92, 111)),
        TextColor.color(224, 92, 111),
        listOf(
            MapRegion(
                MapSinglePoint(624, 111, 808),
                MapSinglePoint(604, 111, 788)
            )
        ),
        listOf(
            MapSinglePoint(645.5, 112, 799.5, 90, 0),
            MapSinglePoint(636.5, 113, 820.5, 135, 0),
            MapSinglePoint(616.5, 112, 829.5, -180, 0),
            MapSinglePoint(596.5, 113, 820.5, -135, 0),
            MapSinglePoint(587.5, 112, 800.5, -90, 0),
            MapSinglePoint(596.5, 113, 780.5, -45, 0),
            MapSinglePoint(616.5, 112, 771.5, 0, 0),
            MapSinglePoint(636.5, 113, 780.5, 45, 0)
        ),
        listOf(
            MapSinglePoint(616.5, 118, 819.5, 180, 35),
            MapSinglePoint(597.5, 118, 800.5, -90, 35),
            MapSinglePoint(616.5, 118, 781.5, 0, 35),
            MapSinglePoint(635.5, 118, 800.5, 90, 35)
        ),
        listOf(
            MapSinglePoint(677, 148, 755, 48.056213F, 85.83555F),
            MapSinglePoint(673, 142, 760, 52.780945F, 64.90654F),
            MapSinglePoint(667, 137, 765, 57.357727F, 32.46672F),
            MapSinglePoint(663, 132, 772, 63.205994F, 14.779753F),
            MapSinglePoint(660, 132, 780, 69.97186F, 10.741306F),
            MapSinglePoint(657, 132, 787, 75.96295F, 17.760796F),
            MapSinglePoint(653, 132, 794, 82.90668F, 27.45739F),
            MapSinglePoint(648, 132, 801, 93.19208F, 34.85472F),
            MapSinglePoint(642, 132, 807, 103.764404F, 38.339962F),
            MapSinglePoint(635, 132, 811, 114.30951F, 40.91138F),
            MapSinglePoint(627, 132, 814, 127.10217F, 44.83077F),
            MapSinglePoint(619, 132, 815, 144.70087F, 50.181454F),
            MapSinglePoint(611, 132, 814, 163.02673F, 54.77303F),
            MapSinglePoint(604, 132, 811, -172.36804F, 56.458115F),
            MapSinglePoint(599, 130, 805, -134.58829F, 57.185158F),
            MapSinglePoint(598, 127, 797, -98.27765F, 52.314674F),
            MapSinglePoint(602, 121, 791, -68.66922F, 44.33963F),
            MapSinglePoint(609, 117, 787, -40.830444F, 34.319355F),
            MapSinglePoint(617, 117, 787, -5.60672F, 21.597097F),
        ),

        " • Dance around the colourful floor until the christmas music stops.\n\n" +
                " • Run and stand on the colour which has been chosen (check hotbar) and stand on it before the timer ends.\n\n" +
                " • After the timer, the map will clear all blocks except the chosen colour blocks. To win, survive the most rounds!",
        Material.MAGENTA_GLAZED_TERRACOTTA,
        GameRegion(),
        1,
    ),


//    KING_OF_THE_HILL, // 247, 1, 35
//    MUSICAL_MINECARTS, // 142, 0, 3
//    SLED_RACING, // 24, 48, 15
//    SPLEEF, // 64, 86, 40

    // 174, 211, 216

}
