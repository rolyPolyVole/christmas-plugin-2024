package host.carbon.event.minigame.engine

import host.carbon.event.ChristmasEventPlugin
import host.carbon.event.minigame.games.BlockParty
import host.carbon.event.minigame.world.GameRegion
import host.carbon.event.minigame.world.MapSinglePoint
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.Material
import kotlin.reflect.KClass

enum class GameConfig(
    val gameClass: KClass<out EventMiniGame>,
    val menuName: Component,
    val colour: TextColor,
    val spawnPoints: List<MapSinglePoint>,
    val spectatorSpawnLocations: List<MapSinglePoint>,
    val spectatorCameraLocations: List<Location>,
    val overviewLocations: List<Location>,
    val instructions: Component,
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
            MapSinglePoint(0, 0, 0, 0, 0)
        ),
        listOf(
            MapSinglePoint(0, 0, 0, 0, 0),
        ),
        listOf(
            Location(ChristmasEventPlugin.getInstance().serverWorld, 0.0, 0.0, 0.0),
        ),
        listOf(
            MapSinglePoint(0, 0, 0, 0, 0),
        ),
        Component.text(
            "• Dance around the map until the Christmas music stops. \n" +
                    "• Run to the color which has been chosen and stand on it before the timer ends. \n" +
                    "• After the timer, the map will clear all except the chosen color. To win, survive the most rounds!",
            NamedTextColor.GRAY
        ),
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