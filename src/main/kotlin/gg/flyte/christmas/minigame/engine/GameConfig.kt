package gg.flyte.christmas.minigame.engine

import gg.flyte.christmas.minigame.games.BlockParty
import gg.flyte.christmas.minigame.games.KingHill
import gg.flyte.christmas.minigame.games.MusicalMinecarts
import gg.flyte.christmas.minigame.games.PaintWars
import gg.flyte.christmas.minigame.world.MapRegion
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.util.style
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import kotlin.collections.listOf
import kotlin.reflect.KClass

enum class GameConfig(
    val gameClass: KClass<out EventMiniGame>,
    val displayName: Component,
    val smallDisplayName: Component,
    val menuMaterial: Material,
    val colour: TextColor,
    val instructions: String,
    val minPlayers: Int,
    val centrePoint: MapSinglePoint,
    val spawnPoints: List<MapRegion>,
    val overviewLocations: List<MapSinglePoint>,
    val spectatorSpawnLocations: List<MapSinglePoint>,
    val spectatorCameraLocations: List<MapSinglePoint>,
) {
    //region BLOCK_PARTY
    BLOCK_PARTY(
        BlockParty::class,
        "<colour:#e05c0b>Block Party".style(),
        "<colour:#e05c0b>ʙʟᴏᴄᴋ ᴘᴀʀᴛʏ".style(),
        Material.MAGENTA_GLAZED_TERRACOTTA,
        TextColor.color(224, 92, 111),
        " • Dance around the colourful floor until the christmas music stops.\n\n" +
                " • Run and stand on the colour which has been chosen (check hotbar) before the timer ends.\n\n" +
                " • After the timer, the map will clear all blocks except the chosen colour blocks. To win, survive the most rounds!",
        1,
        MapSinglePoint(615.5, 180.0, 800.5, 0F, 90F),
        listOf(
            MapRegion(
                MapSinglePoint(624, 111, 808),
                MapSinglePoint(604, 111, 788)
            )
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
        )
    ),
    //endregion

    //region KING_OF_THE_HILL
    KING_OF_THE_HILL(
        KingHill::class,
        "<colour:#f70123>King of the Hill".style(),
        "<colour:#f70123>ᴋɪɴɢ ᴏғ ᴛʜᴇ ʜɪʟʟ".style(),
        Material.STICK,
        TextColor.color(247, 1, 35),
        " • Be the King of The Hill! Stand in the centre of the map to gain points every second.\n\n" +
                " • Knock other players off the map with your knockback stick. Avoid falling off yourself!\n\n" +
                " • The player stood in the centre of the map for the longest time, wins!",
        1,
        MapSinglePoint(833.5, 180.0, 615.5, 90F, 90F),
        listOf(
            MapRegion(
                MapSinglePoint(826.5, 88, 680.5, 180, 0),
                MapSinglePoint(836.5, 88, 675.5, 180, 0)
            ),
            MapRegion(
                MapSinglePoint(861.5, 85, 656.5, 135, 0),
                MapSinglePoint(855.5, 85, 660.5, 135, 0)
            ),
            MapRegion.single(MapSinglePoint(798.5, 86, 662.5, -135, 0)),
            MapRegion.single(MapSinglePoint(787.5, 86, 630.5, -90, 0)),
            MapRegion(
                MapSinglePoint(792.5, 85, 601.5, -45, 0),
                MapSinglePoint(797.5, 85, 604.5, -45, 0)
            ),
            MapRegion(
                MapSinglePoint(820.5, 85, 587.5, 0, 0),
                MapSinglePoint(825.5, 85, 588.5, 0, 0)
            ),
            MapRegion(
                MapSinglePoint(857.5, 85, 597.5, 45, 0),
                MapSinglePoint(853.5, 85, 601.5, 45, 0)
            ),
            MapRegion(
                MapSinglePoint(867.5, 86, 627.5, 90, 0),
                MapSinglePoint(866.5, 86, 626.5, 90, 0)
            ),
            MapRegion(
                MapSinglePoint(861.5, 85, 660.5, 135, 0),
                MapSinglePoint(857.5, 85, 656.5, 135, 0)
            )
        ),
        listOf(
            MapSinglePoint(827, 88, 587, 179.57143F, 74.42298F),
            MapSinglePoint(827, 88, 594, 179.85931F, 67.05221F),
            MapSinglePoint(827, 88, 602, 179.89876F, 56.5931F),
            MapSinglePoint(827, 88, 610, 179.67241F, 46.63012F),
            MapSinglePoint(827, 88, 618, 179.56989F, 35.635384F),
            MapSinglePoint(827, 88, 626, -179.87363F, 24.453075F),
            MapSinglePoint(827, 88, 634, -179.41673F, 15.988203F),
            MapSinglePoint(826, 88, 642, -176.94571F, 9.467426F),
            MapSinglePoint(821, 88, 648, -164.89076F, 6.4826865F),
            MapSinglePoint(814, 88, 650, -149.47144F, 6.4154325F),
            MapSinglePoint(808, 88, 646, -131.84286F, 7.456919F),
            MapSinglePoint(802, 88, 641, -114.1622F, 7.854647F),
            MapSinglePoint(799, 88, 634, -97.394775F, 7.868369F),
            MapSinglePoint(798, 88, 626, -78.14661F, 8.061364F),
            MapSinglePoint(799, 88, 618, -62.56653F, 7.6483693F),
            MapSinglePoint(803, 88, 611, -48.001343F, 7.379387F),
            MapSinglePoint(808, 88, 605, -33.123077F, 7.502257F),
            MapSinglePoint(815, 88, 600, -17.081573F, 8.007061F),
            MapSinglePoint(823, 88, 598, -4.5716248F, 8.615353F),
            MapSinglePoint(833, 90, 605, 13.289856F, 13.035935F),
            MapSinglePoint(841, 96, 611, 44.075134F, 23.785404F),
            MapSinglePoint(847, 101, 617, 70.41861F, 34.081383F),
            MapSinglePoint(850, 107, 625, 90.50464F, 43.07204F),
            MapSinglePoint(850, 113, 633, 105.48248F, 49.530624F),
            MapSinglePoint(849, 118, 640, 119.14905F, 54.422348F),
            MapSinglePoint(845, 124, 648, 131.03406F, 57.888565F),
            MapSinglePoint(842, 125, 652, 139.14163F, 60.207817F),
        ),
        listOf(),
        listOf(
            MapSinglePoint(827, 90, 630, 90, 90),
            MapSinglePoint(847, 93, 664, 150, 25),
            MapSinglePoint(814, 93, 667, -160, 25),
            MapSinglePoint(840, 93, 595, 22, 25),
            MapSinglePoint(809, 93, 596, -27, 25)
        ),
    ),
    //endregion

    //region MUSICAL_MINECARTS 142, 0, 3
    MUSICAL_MINECARTS(
        MusicalMinecarts::class,
        "<colour:#8e0003>Musical Minecarts".style(),
        "<colour:#8e0003>ᴍᴜꜱɪᴄᴀʟ ᴍɪɴᴇᴄᴀʀᴛꜱ".style(),
        Material.MINECART,
        TextColor.color(142, 0, 3),
        " • Dance around the platform until the christmas music stops.\n\n" +
                " • Run to the nearest minecart and get inside before the timer ends.\n\n" +
                " • If you fail to find a minecart before the timer ends, you will be ELIMINATED!\n\n" +
                " • Do NOT click the minecarts until the music has stopped... or else you will be STUNNED!",
        1,
        MapSinglePoint(615.5, 180.0, 800.5, 0F, 90F),
        listOf(MapRegion.single(MapSinglePoint(553, 203, 593))),
        listOf(
            MapSinglePoint(535, 212, 612, -134.63545F, 36.587585F),
            MapSinglePoint(535, 212, 609, -128.16087F, 37.477802F),
            MapSinglePoint(536, 212, 598, -107.6864F, 42.090744F),
            MapSinglePoint(536, 212, 589, -81.384834F, 44.35675F),
            MapSinglePoint(538, 212, 580, -58.80621F, 43.871178F),
            MapSinglePoint(542, 212, 574, -30.723955F, 40.553097F),
            MapSinglePoint(548, 212, 569, -26.5966F, 40.22938F),
            MapSinglePoint(556, 212, 566, -2.6418605F, 38.772663F),
            MapSinglePoint(564, 212, 569, 25.683008F, 39.339165F),
            MapSinglePoint(572, 212, 573, 42.354305F, 39.66288F),
            MapSinglePoint(576, 212, 581, 63.07208F, 40.067524F),
            MapSinglePoint(576, 212, 591, 83.627594F, 41.443314F),
            MapSinglePoint(576, 212, 600, 102.32168F, 41.362385F),
            MapSinglePoint(574, 212, 608, 122.87687F, 42.738174F),
            MapSinglePoint(567, 212, 613, 136.55391F, 45.408825F),
            MapSinglePoint(557, 212, 609, 168.43948F, 53.74449F),
            MapSinglePoint(554, 212, 597, -128.1127F, 75.918816F),
            MapSinglePoint(554, 212, 594, -89.91446F, 88.057785F),
            MapSinglePoint(554, 206, 594, -89.83353F, 88.219635F),
            MapSinglePoint(554, 204, 594, -88.70047F, 88.381485F)
        ),
        listOf(MapSinglePoint(554, 204, 594, -88.70047F, 88.381485F)),
        listOf(MapSinglePoint(554, 204, 594, -88.70047F, 88.381485F)),
    ),
    //endregion

//    MUSICAL_MINECARTS, // 142, 0, 3
//    SLED_RACING, // 24, 48, 15
//    SPLEEF, // 64, 86, 40

    //region PAINT_WARS
    PAINT_WARS(
        PaintWars::class,
        "<colour:#549c19>Paint Wars".style(),
        "<colour:#549c19>ᴘᴀɪɴᴛ ᴡᴀʀꜱ".style(),
        Material.BRUSH,
        TextColor.color(84, 156, 25),
        " • Use your Paint Gun to shoot blocks around the map into your chosen block.\n\n" +
                " • You can shoot over other player's blocks to reduce their score!\n\n" +
                " • To win, cover the most amount of the map with your block/colour!",
        1,
        MapSinglePoint(1550, 93, 437, 180, 0),
        listOf(
            MapRegion.single(MapSinglePoint(1529, 82, 410)),
            MapRegion.single(MapSinglePoint(1524, 81, 455)),
            MapRegion.single(MapSinglePoint(1567, 82, 418)),
            MapRegion.single(MapSinglePoint(1557, 83, 401)),
            MapRegion.single(MapSinglePoint(1558, 84, 502)),
            MapRegion.single(MapSinglePoint(1593, 74, 481)),
            MapRegion.single(MapSinglePoint(1496, 78, 419)),
            MapRegion.single(MapSinglePoint(1475, 83, 448)),
            MapRegion.single(MapSinglePoint(1559, 85, 520)),
            MapRegion.single(MapSinglePoint(1583, 78, 468)),
            MapRegion.single(MapSinglePoint(1567, 81, 427)),
            MapRegion.single(MapSinglePoint(1555, 88, 381)),
            MapRegion.single(MapSinglePoint(1551, 87, 522)),
            MapRegion.single(MapSinglePoint(1526, 88, 501)),
            MapRegion.single(MapSinglePoint(1639, 77, 472)),
            MapRegion.single(MapSinglePoint(1623, 80, 518)),
            MapRegion.single(MapSinglePoint(1518, 81, 412)),
            MapRegion.single(MapSinglePoint(1558, 81, 461)),
            MapRegion.single(MapSinglePoint(1607, 74, 479)),
            MapRegion.single(MapSinglePoint(1574, 83, 512))
        ),
        listOf(
            MapSinglePoint(1500, 164, 549, -157.2309F, 41.443565F),
            MapSinglePoint(1501, 160, 545, -159.65887F, 40.310562F),
            MapSinglePoint(1508, 138, 525, -161.68219F, 36.749695F),
            MapSinglePoint(1518, 115, 497, -165.1623F, 35.940407F),
            MapSinglePoint(1521, 93, 485, -154.39825F, 28.333118F),
            MapSinglePoint(1523, 85, 479, -156.5025F, 17.48867F),
            MapSinglePoint(1526, 85, 472, -151.48468F, 17.48867F),
            MapSinglePoint(1526, 85, 464, -147.59991F, 16.922169F),
            MapSinglePoint(1525, 85, 456, -139.66852F, 16.598454F),
            MapSinglePoint(1524, 85, 448, -129.87567F, 16.598454F),
            MapSinglePoint(1521, 85, 441, -112.55609F, 16.274738F),
            MapSinglePoint(1523, 85, 434, -96.9361F, 16.679382F),
            MapSinglePoint(1527, 85, 427, -86.57672F, 18.297958F),
            MapSinglePoint(1533, 85, 422, -77.67413F, 19.916534F),
            MapSinglePoint(1540, 85, 417, -65.21051F, 20.806751F),
            MapSinglePoint(1548, 85, 414, -52.018494F, 20.806751F),
            MapSinglePoint(1556, 85, 413, -37.04596F, 20.078392F),
            MapSinglePoint(1564, 85, 414, -22.720886F, 19.107246F),
            MapSinglePoint(1571, 85, 417, -7.1818237F, 18.055172F),
            MapSinglePoint(1578, 85, 422, 9.32843F, 15.951024F),
            MapSinglePoint(1583, 85, 428, 25.353088F, 13.927828F),
            MapSinglePoint(1586, 85, 436, 42.591736F, 10.3670025F),
            MapSinglePoint(1585, 86, 444, 48.418884F, 10.44793F),
            MapSinglePoint(1581, 91, 450, 47.36676F, 11.257209F),
            MapSinglePoint(1576, 91, 456, 45.262512F, 12.471127F),
            MapSinglePoint(1570, 91, 461, 43.562927F, 13.765972F),
            MapSinglePoint(1562, 91, 464, 34.903137F, 17.164955F),
            MapSinglePoint(1555, 91, 464, 9.490356F, 23.720188F),
            MapSinglePoint(1548, 91, 462, -79.53168F, 33.51256F),
            MapSinglePoint(1543, 91, 465, -128.89948F, 28.575905F),
            MapSinglePoint(1539, 91, 472, -148.8089F, 25.257824F),
            MapSinglePoint(1536, 91, 479, -157.87335F, 22.344387F),
        ),
        listOf(
            MapSinglePoint(1578, 113, 546, 151, 18.2),
        ),
        listOf(
            MapSinglePoint(1578, 113, 546, 151, 18.2),
            MapSinglePoint(1494, 118, 501, -125, 38),
            MapSinglePoint(1489, 92, 412, -68, 27),
            MapSinglePoint(1577, 140, 354, 16, 43),
            MapSinglePoint(1600, 86, 505, 139, -6),
            MapSinglePoint(1571, 96, 427, 85, 9),
            MapSinglePoint(1548, 86, 444, 24.5, 5.5),
            MapSinglePoint(1600, 171, 502, 137, 25),
            MapSinglePoint(1623, 136, 406, 64, 33),
            MapSinglePoint(1558, 98, 416, -8, 26)
        ),
    )
    //endregion

}
// TODO fill in spectatorLocations for non-eliminating games as well.