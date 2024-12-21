package gg.flyte.christmas.minigame.engine

import dev.shreyasayyengar.menuapi.menu.MenuItem
import gg.flyte.christmas.minigame.games.*
import gg.flyte.christmas.minigame.world.MapRegion
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.util.style
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

enum class GameConfig(
    val gameClass: KClass<out EventMiniGame>,
    val displayName: Component,
    val menuItem: ItemStack,
    val colour: TextColor,
    val instructions: String,
    val minPlayers: Int,
    val centrePoint: MapSinglePoint,
    val cameraSlideParameters: Pair<Int, Double>, // duration, epsilon
    val spawnPoints: List<MapRegion>,
    val overviewLocations: List<MapSinglePoint>,
    val spectatorSpawnLocations: List<MapSinglePoint>,
    val spectatorCameraLocations: List<MapSinglePoint>,
    val eliminateOnLeave: Boolean = true,
) {
    //region AVALANCHE
    AVALANCHE(
        Avalanche::class,
        "<colour:#33ff8b>ᴀᴠᴀʟᴀɴᴄʜᴇ".style(),
        ItemStack(Material.SNOWBALL),
        TextColor.fromHexString("#33ff8b")!!,
        " • ᴅᴀɴᴄᴇ ᴀʀᴏᴜɴᴅ ᴛʜᴇ ɪᴄʏ ᴘʟᴀᴛꜰᴏʀᴍ ᴜɴᴛɪʟ ᴛʜᴇ ᴄʜʀɪѕᴛᴍᴀѕ ᴍᴜѕɪᴄ ѕᴛᴏᴘѕ.\n\n" +
                " • ʀᴜɴ ᴀɴᴅ ѕᴛᴀɴᴅ ᴜɴᴅᴇʀ ѕʜᴇʟᴛᴇʀ ᴀѕ ᴛʜᴇ ѕɴᴏᴡʙᴀʟʟѕ ꜰᴀʟʟ ꜰʀᴏᴍ ᴛʜᴇ ѕᴋʏ.\n\n" +
                " • ɪꜰ ʏᴏᴜ ᴀʀᴇ ʜɪᴛ ʙʏ ᴀ ꜰᴀʟʟɪɴɢ ѕɴᴏᴡʙᴀʟʟ, ʏᴏᴜ ᴡɪʟʟ ʙᴇ ᴇʟɪᴍɪɴᴀᴛᴇᴅ!",
        2,
        MapSinglePoint(616.5, 140.0, 800.5, 0F, 90F),
        70 to 0.5,
        listOf(
            MapRegion(
                MapSinglePoint(624, 111, 808),
                MapSinglePoint(604, 111, 788)
            )
        ),
        listOf(
            MapSinglePoint(615, 170, 730, -0.9721985F, 39.654617F),
            MapSinglePoint(617, 122, 773, -4.1285706F, 19.746199F),
            MapSinglePoint(619, 116, 795, -1.700592F, 12.624509F),
            MapSinglePoint(619, 116, 809, 31.07602F, 13.271932F),
            MapSinglePoint(612, 116, 818, 98.003815F, 11.572447F),
            MapSinglePoint(607, 116, 820, 173.26697F, 3.2368581F),
            MapSinglePoint(602, 116, 815, -170.06134F, -7.7693725F),
            MapSinglePoint(591, 116, 809, -132.10449F, -23.631304F),
            MapSinglePoint(589, 116, 796, -89.04901F, -30.267466F),
            MapSinglePoint(598, 116, 785, -53.115173F, -28.891676F),
            MapSinglePoint(613, 116, 782, -20.256775F, -24.683378F),
            MapSinglePoint(626, 116, 788, 28.464172F, -28.163317F),
            MapSinglePoint(631, 116, 800, 75.80859F, -31.724184F),
            MapSinglePoint(624, 116, 810, 103.648865F, -26.625673F),
            MapSinglePoint(621, 116, 812, 116.031555F, -17.56166F),
            MapSinglePoint(621, 116, 813, 117.73114F, -15.619374F),
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

    //region BAUBLE_TAG
    BAUBLE_TAG(
        BaubleTag::class,
        "<colour:#aed3d8>ʙᴀᴜʙʟᴇ ᴛᴀɢ".style(),
        MenuItem(Material.PLAYER_HEAD).setSkullTexture("1e3ad039e903e30f90daa68cebfc5cee72b5ed84d6044382409c67f374d1732b").itemStack,
        TextColor.fromHexString("#aed3d8")!!,
        " • ʀᴜɴ ᴀᴡᴀʏ ꜰʀᴏᴍ ᴛʜᴇ ᴘʟᴀʏᴇʀѕ ᴡɪᴛʜ ʙᴀᴜʙʟᴇѕ ᴏɴ ᴛʜᴇɪʀ ʜᴇᴀᴅ.\n\n" +
                " • ɪꜰ ʏᴏᴜ ᴀʀᴇ ᴛᴀɢɢᴇᴅ, ʏᴏᴜ ᴡɪʟʟ ʙᴇᴄᴏᴍᴇ ᴛʜᴇ ɴᴇᴡ ʙᴀᴜʙʟᴇ ʜᴏʟᴅᴇʀ.\n\n" +
                " • ᴛʜᴇ ʙᴀᴜʙʟᴇѕ ᴡɪʟʟ ѕʜᴀᴛᴛᴇʀ ᴀᴛ ᴛʜᴇ ᴇɴᴅ ᴏꜰ ᴇᴀᴄʜ ʀᴏᴜɴᴅ.\n\n" +
                " • ᴛᴏ ᴡɪɴ, ѕᴜʀᴠɪᴠᴇ ᴛʜᴇ ᴍᴏѕᴛ ʀᴏᴜɴᴅѕ!",
        2,
        MapSinglePoint(208, 170, 1282, 123, 90),
        400 to 0.7,
        listOf(
            MapRegion.single(MapSinglePoint(139, 131, 1239, -67.351654F, 24.116606F)),
            MapRegion.single(MapSinglePoint(151, 130, 1236, -47.20056F, 8.821184F)),
            MapRegion.single(MapSinglePoint(169, 128, 1243, 40.282806F, 7.4454055F)),
            MapRegion.single(MapSinglePoint(184, 127, 1244, 11.957977F, 16.913998F)),
            MapRegion.single(MapSinglePoint(200, 127, 1252, 40.36374F, 7.0407586F)),
            MapRegion.single(MapSinglePoint(205, 127, 1269, 96.36594F, 8.254676F)),
            MapRegion.single(MapSinglePoint(195, 127, 1286, -35.30432F, 7.607246F)),
            MapRegion.single(MapSinglePoint(200, 130, 1306, -1.7998962F, -1.6994951F)),
            MapRegion.single(MapSinglePoint(188, 132, 1325, 93.93817F, 26.058868F)),
            MapRegion.single(MapSinglePoint(181, 135, 1315, 169.52527F, 17.318624F)),
            MapRegion.single(MapSinglePoint(166, 137, 1297, 155.60559F, 17.318623F)),
            MapRegion.single(MapSinglePoint(143, 129, 1280, -93.44159F, 3.0752602F)),
            MapRegion.single(MapSinglePoint(147, 129, 1262, -92.308655F, 19.584616F)),
            MapRegion.single(MapSinglePoint(172, 127, 1266, 90.50836F, -0.161888F)),
            MapRegion.single(MapSinglePoint(203, 128, 1218, -26.706787F, -0.080960006F)),
            MapRegion.single(MapSinglePoint(240, 129, 1208, 50.7417F, 3.075232F)),
            MapRegion.single(MapSinglePoint(217, 127, 1258, 55.43579F, 9.063919F)),
        ),
        listOf(
            MapSinglePoint(248, 233, 1142, 24.004883F, 30.517729F),
            MapSinglePoint(222, 199, 1200, 23.59961F, 30.4368F),
            MapSinglePoint(226, 160, 1263, 51.595703F, 31.246086F),
            MapSinglePoint(210, 157, 1298, 97.62744F, 30.113083F),
            MapSinglePoint(197, 151, 1308, 114.29199F, 28.089863F),
            MapSinglePoint(182, 145, 1315, 135.24316F, 25.014568F),
            MapSinglePoint(167, 140, 1315, 162.66211F, 18.135622F),
            MapSinglePoint(152, 139, 1309, 177.06396F, 11.742289F),
            MapSinglePoint(140, 139, 1298, -154.29785F, 11.2567215F),
            MapSinglePoint(136, 139, 1284, -131.57227F, 15.950537F),
            MapSinglePoint(140, 138, 1271, -110.94336F, 19.187681F),
            MapSinglePoint(152, 133, 1262, -108.106445F, 13.037126F),
            MapSinglePoint(167, 129, 1257, -107.53906F, 4.135039F),
            MapSinglePoint(178, 128, 1253, -133.11133F, 8.586087F),
            MapSinglePoint(181, 128, 1252, -133.11133F, 8.586087F),
        ),
        listOf(MapSinglePoint(147, 200, 1270, -92.046814F, 49.535908F)),
        listOf(MapSinglePoint(176, 142, 1235, -13.384574F, 23.072657F)), // TODO not working?!
    ),
    //endregion

    //region BLOCK_PARTY
    BLOCK_PARTY(
        BlockParty::class,
        "<colour:#e05c6f>ʙʟᴏᴄᴋ ᴘᴀʀᴛʏ".style(),
        ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA),
        TextColor.fromHexString("#e05c6f")!!,
        " • ᴅᴀɴᴄᴇ ᴀʀᴏᴜɴᴅ ᴛʜᴇ ᴄᴏʟᴏᴜʀꜰᴜʟ ꜰʟᴏᴏʀ ᴜɴᴛɪʟ ᴛʜᴇ ᴄʜʀɪѕᴛᴍᴀѕ ᴍᴜѕɪᴄ ѕᴛᴏᴘѕ.\n\n" +
                " • ʀᴜɴ ᴀɴᴅ ѕᴛᴀɴᴅ ᴏɴ ᴛʜᴇ ᴄᴏʟᴏᴜʀ ᴡʜɪᴄʜ ʜᴀѕ ʙᴇᴇɴ ᴄʜᴏѕᴇɴ (ᴄʜᴇᴄᴋ ʜᴏᴛʙᴀʀ) ʙᴇꜰᴏʀᴇ ᴛʜᴇ ᴛɪᴍᴇʀ ᴇɴᴅѕ.\n\n" +
                " • ᴀꜰᴛᴇʀ ᴛʜᴇ ᴛɪᴍᴇʀ, ᴛʜᴇ ᴍᴀᴘ ᴡɪʟʟ ᴄʟᴇᴀʀ ᴀʟʟ ʙʟᴏᴄᴋѕ ᴇxᴄᴇᴘᴛ ᴛʜᴇ ᴄʜᴏѕᴇɴ ᴄᴏʟᴏᴜʀ ʙʟᴏᴄᴋѕ. ᴛᴏ ᴡɪɴ, ѕᴜʀᴠɪᴠᴇ ᴛʜᴇ ᴍᴏѕᴛ ʀᴏᴜɴᴅѕ!",
        2,
        MapSinglePoint(616.5, 140.0, 800.5, 0F, 90F),
        70 to 0.5,
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
        "<colour:#f70123>ᴋɪɴɢ ᴏꜰ ᴛʜᴇ ʜɪʟʟ".style(),
        ItemStack(Material.STICK),
        TextColor.fromHexString("#f70123")!!,
        " • ʙᴇ ᴛʜᴇ ᴋɪɴɢ ᴏꜰ ᴛʜᴇ ʜɪʟʟ! ѕᴛᴀɴᴅ ɪɴ ᴛʜᴇ ᴄᴇɴᴛʀᴇ ᴏꜰ ᴛʜᴇ ᴍᴀᴘ ᴛᴏ ɢᴀɪɴ ᴘᴏɪɴᴛѕ ᴇᴠᴇʀʏ ѕᴇᴄᴏɴᴅ.\n\n" +
                " • ᴋɴᴏᴄᴋ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀѕ ᴏꜰꜰ ᴛʜᴇ ᴍᴀᴘ ᴡɪᴛʜ ʏᴏᴜʀ ᴋɴᴏᴄᴋʙᴀᴄᴋ ѕᴛɪᴄᴋ. ᴀᴠᴏɪᴅ ꜰᴀʟʟɪɴɢ ᴏꜰꜰ ʏᴏᴜʀѕᴇʟꜰ!\n\n" +
                " • ᴛʜᴇ ᴘʟᴀʏᴇʀ ѕᴛᴏᴏᴅ ɪɴ ᴛʜᴇ ᴄᴇɴᴛʀᴇ ᴏꜰ ᴛʜᴇ ᴍᴀᴘ ꜰᴏʀ ᴛʜᴇ ʟᴏɴɢᴇѕᴛ ᴛɪᴍᴇ, ᴡɪɴѕ!",
        1,
        MapSinglePoint(827.5, 111, 630.5, -90F, 90F),
        70 to 0.5,
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
        listOf(MapSinglePoint(827, 120, 630)),
        listOf(
            MapSinglePoint(827, 90, 630, 90, 90),
            MapSinglePoint(847, 93, 664, 150, 25),
            MapSinglePoint(814, 93, 667, -160, 25),
            MapSinglePoint(840, 93, 595, 22, 25),
            MapSinglePoint(809, 93, 596, -27, 25)
        ),
        false
    ),
    //endregion

    //region MUSICAL_MINECARTS
    MUSICAL_MINECARTS(
        MusicalMinecarts::class,
        "<colour:#a1a4ff>ᴍᴜꜱɪᴄᴀʟ ᴍɪɴᴇᴄᴀʀᴛꜱ".style(),
        ItemStack(Material.MINECART),
        TextColor.fromHexString("#a1a4ff")!!,
        " • ᴅᴀɴᴄᴇ ᴀʀᴏᴜɴᴅ ᴛʜᴇ ᴘʟᴀᴛꜰᴏʀᴍ ᴜɴᴛɪʟ ᴛʜᴇ ᴄʜʀɪѕᴛᴍᴀѕ ᴍᴜѕɪᴄ ѕᴛᴏᴘѕ.\n\n" +
                " • ʀᴜɴ ᴛᴏ ᴛʜᴇ ɴᴇᴀʀᴇѕᴛ ᴍɪɴᴇᴄᴀʀᴛ ᴀɴᴅ ɢᴇᴛ ɪɴѕɪᴅᴇ ʙᴇꜰᴏʀᴇ ᴛʜᴇ ᴛɪᴍᴇʀ ᴇɴᴅѕ.\n\n" +
                " • ɪꜰ ʏᴏᴜ ꜰᴀɪʟ ᴛᴏ ꜰɪɴᴅ ᴀ ᴍɪɴᴇᴄᴀʀᴛ ʙᴇꜰᴏʀᴇ ᴛʜᴇ ᴛɪᴍᴇʀ ᴇɴᴅѕ, ʏᴏᴜ ᴡɪʟʟ ʙᴇ <b>ᴇʟɪᴍɪɴᴀᴛᴇᴅ!\n\n" +
                " • ᴅᴏ <b>ɴᴏᴛ</b> ᴄʟɪᴄᴋ ᴛʜᴇ ᴍɪɴᴇᴄᴀʀᴛѕ ᴜɴᴛɪʟ ᴛʜᴇ ᴍᴜѕɪᴄ ʜᴀѕ ѕᴛᴏᴘᴘᴇᴅ... ᴏʀ ᴇʟѕᴇ ʏᴏᴜ ᴡɪʟʟ ʙᴇ <b>ѕᴛᴜɴɴᴇᴅ!",
        2,
        MapSinglePoint(616.5, 140.0, 800.5, 0F, 90F),
        70 to 0.5,
        listOf(
            MapRegion(MapSinglePoint(597, 113, 819), MapSinglePoint(635, 113, 781))
        ),
        listOf(
            MapSinglePoint(581, 122, 805, -111.17755F, 32.429695F),
            MapSinglePoint(591, 122, 799, -94.262634F, 44.97366F),
            MapSinglePoint(595, 122, 792, -67.55487F, 50.719604F),
            MapSinglePoint(603, 122, 785, -45.5412F, 54.28047F),
            MapSinglePoint(617, 122, 786, 0.7520752F, 58.326912F),
            MapSinglePoint(629, 122, 793, 35.310303F, 60.754776F),
            MapSinglePoint(631, 122, 806, 87.59271F, 64.39655F),
            MapSinglePoint(623, 122, 817, 126.925964F, 64.63933F),
            MapSinglePoint(609, 122, 820, 179.69403F, 60.431057F),
            MapSinglePoint(604, 122, 811, -142.9961F, 59.864555F),
            MapSinglePoint(608, 119, 797, -89.015564F, 50.7196F),
            MapSinglePoint(618, 119, 788, -11.158569F, 42.950436F),
            MapSinglePoint(623, 119, 794, 38.534058F, 44.73087F),
            MapSinglePoint(619, 119, 804, 103.84668F, 41.089073F),
            MapSinglePoint(607, 119, 803, 140.50916F, 39.95607F),
            MapSinglePoint(601, 119, 793, -132.65015F, 36.47613F),
            MapSinglePoint(608, 119, 786, -54.22644F, 34.938484F),
            MapSinglePoint(617, 119, 790, 7.7679443F, 48.12988F),
            MapSinglePoint(620, 122, 792, 38.19861F, 61.078487F),
            MapSinglePoint(621, 126, 793, 53.575928F, 67.63352F),
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

    //region PAINTBALL
    PAINTBALL(
        Paintball::class,
        "<colour:#89bdf5>ᴘᴀɪɴᴛʙᴀʟʟ".style(),
        ItemStack(Material.HEART_OF_THE_SEA),
        TextColor.fromHexString("#89bdf5")!!,
        " • ѕʜᴏᴏᴛ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀѕ ᴡɪᴛʜ ʏᴏᴜʀ ᴘᴀɪɴᴛʙᴀʟʟ ɢᴜɴ ᴛᴏ ɢᴇᴛ ᴀ ᴘᴏɪɴᴛ.\n\n" +
                " • ᴀᴠᴏɪᴅ ʙᴇɪɴɢ ѕʜᴏᴛ ʙʏ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀѕ ᴛᴏ ѕᴜʀᴠɪᴠᴇ ᴛʜᴇ ʟᴏɴɢᴇѕᴛ.\n\n" +
                " • ᴛᴏ ᴡɪɴ, ʜɪᴛ ᴛʜᴇ ᴍᴏѕᴛ ᴘʟᴀʏᴇʀѕ ᴀɴᴅ ɢᴇᴛ ᴛʜᴇ ᴍᴏѕᴛ ᴘᴏɪɴᴛѕ!",
        2,
        MapSinglePoint(181.5, 140.0, 300.5, -135, 90),
        200 to 0.6,
        listOf(
            MapRegion.single(MapSinglePoint(193, 80, 193, 0, 0)),
            MapRegion.single(MapSinglePoint(207, 82, 221, 0, 0)),
            MapRegion.single(MapSinglePoint(206, 80, 251, 0, 0)),
            MapRegion.single(MapSinglePoint(188, 76, 262, 0, 0)),
            MapRegion.single(MapSinglePoint(191, 72, 290, 0, 0)),
            MapRegion.single(MapSinglePoint(209, 71, 295, 0, 0)),
            MapRegion.single(MapSinglePoint(229, 73, 300, 0, 0)),
            MapRegion.single(MapSinglePoint(250, 76, 294, 0, 0)),
            MapRegion.single(MapSinglePoint(265, 78, 304, 0, 0)),
            MapRegion.single(MapSinglePoint(271, 76, 319, 0, 0)),
            MapRegion.single(MapSinglePoint(277, 75, 334, 0, 0)),
            MapRegion.single(MapSinglePoint(285, 75, 350, 0, 0)),
            MapRegion.single(MapSinglePoint(298, 75, 370, 0, 0)),
            MapRegion.single(MapSinglePoint(291, 75, 383, 0, 0)),
            MapRegion.single(MapSinglePoint(281, 75, 398, 0, 0)),
            MapRegion.single(MapSinglePoint(262, 75, 403, 0, 0)),
            MapRegion.single(MapSinglePoint(249, 77, 415, 0, 0)),
            MapRegion.single(MapSinglePoint(233, 79, 422, 0, 0)),
            MapRegion.single(MapSinglePoint(211, 81, 428, 0, 0)),
            MapRegion.single(MapSinglePoint(201, 77, 407, 0, 0)),
            MapRegion.single(MapSinglePoint(194, 76, 394, 0, 0)),
            MapRegion.single(MapSinglePoint(201, 74, 378, 0, 0)),
            MapRegion.single(MapSinglePoint(206, 72, 364, 0, 0)),
            MapRegion.single(MapSinglePoint(208, 71, 347, 0, 0)),
            MapRegion.single(MapSinglePoint(199, 71, 333, 0, 0)),
            MapRegion.single(MapSinglePoint(172, 73, 298, 0, 0)),
            MapRegion.single(MapSinglePoint(143, 79, 279, 0, 0)),
            MapRegion.single(MapSinglePoint(141, 77, 244, 0, 0)),
            MapRegion.single(MapSinglePoint(97, 89, 254, 0, 0)),
            MapRegion.single(MapSinglePoint(114, 86, 215, 0, 0))
        ),
        listOf(
            MapSinglePoint(173, 99, 307, 175.59639F, 24.933584F),
            MapSinglePoint(172, 93, 293, 172.44003F, 21.049002F),
            MapSinglePoint(172, 88, 278, 150.75089F, 10.609256F),
            MapSinglePoint(166, 86, 264, 148.64664F, 3.6494362F),
            MapSinglePoint(156, 86, 252, -178.25386F, 1.3834488F),
            MapSinglePoint(147, 88, 239, -110.35533F, 3.8112924F),
            MapSinglePoint(151, 93, 226, -85.26666F, 10.4474F),
            MapSinglePoint(161, 99, 215, -74.25983F, 10.932967F),
            MapSinglePoint(175, 105, 206, -61.796204F, 15.060287F),
            MapSinglePoint(190, 109, 204, -24.243927F, 27.28051F),
            MapSinglePoint(198, 105, 214, -11.05191F, 30.517662F),
            MapSinglePoint(202, 100, 229, -11.132843F, 25.25729F),
            MapSinglePoint(206, 94, 245, -13.479889F, 19.349497F),
            MapSinglePoint(209, 88, 260, -12.2659F, 17.73092F),
            MapSinglePoint(213, 83, 276, -16.474396F, 14.493787F),
            MapSinglePoint(218, 77, 292, -21.168488F, 12.065951F),
            MapSinglePoint(223, 73, 307, -24.32486F, 4.2968564F)
        ),
        listOf(MapSinglePoint(157, 127, 312)),
        listOf(MapSinglePoint(212, 82, 284, 0, 38)),
        false
    ),
    //endregion

    //region PAINT_WARS
    PAINT_WARS(
        PaintWars::class,
        "<colour:#75e01d>ᴘᴀɪɴᴛ ᴡᴀʀꜱ".style(),
        ItemStack(Material.BRUSH),
        TextColor.fromHexString("#75e01d")!!,
        " • ᴜѕᴇ ʏᴏᴜʀ ᴘᴀɪɴᴛ ɢᴜɴ ᴛᴏ ѕʜᴏᴏᴛ ʙʟᴏᴄᴋѕ ᴀʀᴏᴜɴᴅ ᴛʜᴇ ᴍᴀᴘ ɪɴᴛᴏ ʏᴏᴜʀ ᴄʜᴏѕᴇɴ ʙʟᴏᴄᴋ.\n\n" +
                " • ʏᴏᴜ ᴄᴀɴ ѕʜᴏᴏᴛ ᴏᴠᴇʀ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀ'ѕ ʙʟᴏᴄᴋѕ ᴛᴏ ʀᴇᴅᴜᴄᴇ ᴛʜᴇɪʀ ѕᴄᴏʀᴇ!\n\n" +
                " • ᴛᴏ ᴡɪɴ, ᴄᴏᴠᴇʀ ᴛʜᴇ ᴍᴏѕᴛ ᴀᴍᴏᴜɴᴛ ᴏꜰ ᴛʜᴇ ᴍᴀᴘ ᴡɪᴛʜ ʏᴏᴜʀ ʙʟᴏᴄᴋ/ᴄᴏʟᴏᴜʀ!",
        1,
        MapSinglePoint(622.5, 98, -150.5, 180, 90),
        450 to 0.7,
        listOf(
            MapRegion.single(MapSinglePoint(630, 94, -208, 10.461055F, 8.056724F)),
            MapRegion.single(MapSinglePoint(652, 94, -192, 47.526287F, 9.432502F)),
            MapRegion.single(MapSinglePoint(684, 93, -179, 65.33065F, 7.5712233F)),
            MapRegion.single(MapSinglePoint(654, 91, -163, 51.485233F, 12.211981F)),
            MapRegion.single(MapSinglePoint(673, 88, -139, 119.86949F, -7.129863F)),
            MapRegion.single(MapSinglePoint(670, 93, -121, 128.20517F, 6.3042083F)),
            MapRegion.single(MapSinglePoint(630, 94, -108, 174.73938F, 8.246563F)),
            MapRegion.single(MapSinglePoint(606, 104, -83, -163.81433F, 16.339424F)),
            MapRegion.single(MapSinglePoint(573, 102, -96, -132.90025F, 14.397221F)),
            MapRegion.single(MapSinglePoint(548, 116, -127, -128.69142F, 16.744123F)),
            MapRegion.single(MapSinglePoint(535, 94, -147, -102.38962F, 4.8477216F)),
            MapRegion.single(MapSinglePoint(634, 91, -152, -87.49902F, 7.0329227F)),
            MapRegion.single(MapSinglePoint(617, 91, -144, 47.328888F, 5.2525268F)),
            MapRegion.single(MapSinglePoint(592, 100, -256, -36.994843F, -1.9498547F)),
            MapRegion.single(MapSinglePoint(621, 94, -196, -8.994965F, 21.6811F))
        ),
        listOf(
            MapSinglePoint(574, 180, -28, -164.78236F, 45.797764F),
            MapSinglePoint(581, 159, -51, -164.6205F, 45.878693F),
            MapSinglePoint(591, 127, -90, -164.45863F, 46.12148F),
            MapSinglePoint(604, 97, -136, -166.40102F, 40.861107F),
            MapSinglePoint(609, 97, -155, -160.16922F, 30.906864F),
            MapSinglePoint(613, 97, -165, -134.02844F, 24.513489F),
            MapSinglePoint(619, 97, -172, -108.37331F, 19.495903F),
            MapSinglePoint(626, 97, -174, -84.17459F, 17.553612F),
            MapSinglePoint(634, 97, -173, -62.646515F, 17.877327F),
            MapSinglePoint(641, 97, -170, -55.848175F, 17.63454F),
            MapSinglePoint(647, 97, -166, -30.273499F, 16.177822F),
            MapSinglePoint(652, 97, -159, -8.98822F, 15.44947F),
            MapSinglePoint(653, 97, -151, 10.840271F, 14.721119F),
            MapSinglePoint(652, 97, -144, 40.056915F, 11.888644F),
            MapSinglePoint(647, 97, -137, 58.914215F, 12.212356F),
            MapSinglePoint(641, 97, -133, 74.53418F, 12.131428F),
            MapSinglePoint(633, 97, -130, 92.09656F, 12.697923F),
            MapSinglePoint(625, 97, -130, 109.335205F, 13.669057F),
            MapSinglePoint(618, 97, -132, 126.25012F, 13.992768F),
            MapSinglePoint(610, 97, -133, 150.93454F, 14.802047F),
            MapSinglePoint(602, 97, -135, 178.9372F, 13.426273F),
            MapSinglePoint(596, 97, -140, -164.22882F, 13.102562F),
            MapSinglePoint(589, 97, -144, -146.18085F, 12.940706F),
            MapSinglePoint(582, 97, -149, -128.53754F, 11.7267885F),
            MapSinglePoint(577, 97, -155, -108.385315F, 10.836582F),
            MapSinglePoint(571, 97, -160, -93.574646F, 11.241221F),
            MapSinglePoint(565, 97, -166, -77.06439F, 10.755654F),
            MapSinglePoint(560, 98, -173, -68.32367F, 11.7267885F),
            MapSinglePoint(561, 103, -180, -59.906677F, 16.25875F),
            MapSinglePoint(564, 109, -187, -52.29901F, 21.600052F),
            MapSinglePoint(569, 114, -193, -47.92865F, 26.698566F),
            MapSinglePoint(574, 120, -199, -44.20575F, 29.450146F),
            MapSinglePoint(580, 126, -205, -40.15912F, 30.987793F),
            MapSinglePoint(586, 131, -210, -34.412903F, 32.282654F),
            MapSinglePoint(593, 137, -215, -29.556946F, 33.33473F)
        ),
        listOf(MapSinglePoint(565, 183, -208)),
        listOf(MapSinglePoint(655, 107, -141, 108.178734F, 30.104355F)),
        false
    ),
    //endregion

    //region SLED_RACING
    SLED_RACING(
        SledRacing::class,
        "<colour:#80ffea>ꜱʟᴇᴅ ʀᴀᴄɪɴɢ".style(),
        ItemStack(Material.PAPER).apply {
            itemMeta = itemMeta.apply {
                setCustomModelData(1)
            }
        },
        TextColor.fromHexString("#80ffea")!!,
        " • ꜰᴏʟʟᴏᴡ ᴛʜᴇ ɪᴄʏ ᴘᴀᴛʜ ᴛᴏ ᴛʜᴇ ᴇɴᴅ ᴏꜰ ᴛʜᴇ ᴍᴀᴘ ᴡɪᴛʜ ʏᴏᴜʀ sʟᴇᴅ.\n\n" +
                " • 3 ʟᴀᴘs ᴍᴜsᴛ ʙᴇ ᴄᴏᴍᴘʟᴇᴛᴇᴅ. ᴛᴏ ʀᴇsᴇᴛ ᴛᴏ ʏᴏᴜʀ ᴄʜᴇᴄᴋᴘᴏɪɴᴛ, ᴘʀᴇss <white><b><key:key.sneak></b></white> (sɴᴇᴀᴋ).\n\n" +
                " • ᴛᴏ ᴡɪɴ, ʙᴇ ᴛʜᴇ ꜰɪʀsᴛ ᴘʟᴀʏᴇʀ ᴛᴏ ꜰɪɴɪsʜ ᴀʟʟ 3 ʟᴀᴘs!\n\n" +
                " • \uD83C\uDFB5 ᴄᴏᴍᴇ ᴏɴ, ɪᴛ's ʟᴏᴠᴇʟʏ ᴡᴇᴀᴛʜᴇʀ ꜰᴏʀ ᴀ sʟᴇɪɢʜ ʀɪᴅᴇ ᴛᴏɢᴇᴛʜᴇʀ ᴡɪᴛʜ ʏᴏᴜ! \uD83C\uDFB6 🛷",
        1,
        MapSinglePoint(523, 150, 1842, 127F, 90F),
        850 to 0.775,
        listOf(
            MapRegion(MapSinglePoint(671, 149, 1811, 116.8, 0), MapSinglePoint(651, 149, 1791, 116.8, 0))
        ),
        listOf(
            MapSinglePoint(606, 203, 1612, 16.841675F, 29.505898F),
            MapSinglePoint(577, 126, 1708, 15.384888F, 8.545502F),
            MapSinglePoint(549, 125, 1808, 15.384888F, 8.545502F),
            MapSinglePoint(538, 125, 1849, 15.546753F, 8.62643F),
            MapSinglePoint(528, 107, 1883, 27.038696F, 9.03107F),
            MapSinglePoint(512, 110, 1914, 44.115112F, 8.5455F),
            MapSinglePoint(495, 110, 1944, 91.78198F, 27.320837F),
            MapSinglePoint(480, 110, 1959, 126.41931F, 27.887333F),
            MapSinglePoint(452, 104, 1946, 146.73242F, 26.26877F),
            MapSinglePoint(434, 110, 1918, 131.8418F, 25.37856F),
            MapSinglePoint(410, 95, 1890, 131.8418F, 25.37856F),
            MapSinglePoint(383, 73, 1869, 121.15906F, 24.731133F),
            MapSinglePoint(349, 81, 1847, 122.37305F, 22.384212F),
            MapSinglePoint(320, 87, 1831, 48.08081F, 27.725473F),
            MapSinglePoint(280, 108, 1828, 10.934692F, 34.118805F),
            MapSinglePoint(261, 123, 1818, -37.460327F, 33.30953F),
            MapSinglePoint(249, 133, 1809, -80.02869F, 26.430634F),
        ),
        listOf(MapSinglePoint(537, 166, 1876)),
        listOf(MapSinglePoint(458, 151, 1876, -116.8, 33.3)),
        false
    ),
    //endregion

    //region SPLEEF
    SPLEEF(
        Spleef::class,
        "<colour:#ffc642>ꜱᴘʟᴇᴇꜰ".style(),
        ItemStack(Material.DIAMOND_SHOVEL),
        TextColor.fromHexString("#ffc642")!!,
        " • ᴅᴇѕᴛʀᴏʏ ᴛʜᴇ ѕɴᴏᴡ ʙʟᴏᴄᴋѕ ᴜɴᴅᴇʀ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀѕ ᴛᴏ ᴇʟɪᴍɪɴᴀᴛᴇ ᴛʜᴇᴍ.\n\n" +
                " • ᴀᴠᴏɪᴅ ꜰᴀʟʟɪɴɢ ɪɴᴛᴏ ᴛʜᴇ ʟᴀᴠᴀ ʙʏ ᴊᴜᴍᴘɪɴɢ ᴏᴠᴇʀ ʜᴏʟᴇѕ ɪɴ ᴛʜᴇ ɢʀᴏᴜɴᴅ.\n\n" +
                " • ᴛᴏ ᴡɪɴ, ʙᴇ ᴛʜᴇ ʟᴀѕᴛ ᴘʟᴀʏᴇʀ ѕᴛᴀɴᴅɪɴɢ ᴏɴ ᴛʜᴇ ᴍᴀᴘ!",
        2,
        MapSinglePoint(616.5, 140.0, 800.5, 0F, 90F),
        70 to 0.5,
        listOf(
            MapRegion(MapSinglePoint(597, 113, 819), MapSinglePoint(635, 113, 781))
        ),
        listOf(
            MapSinglePoint(624, 30, 791, 53.888947F, 88.462425F),
            MapSinglePoint(624, 62, 791, 53.888947F, 67.17887F),
            MapSinglePoint(624, 80, 791, 54.860138F, 57.95312F),
            MapSinglePoint(624, 82, 791, 56.397858F, 49.536526F),
            MapSinglePoint(624, 88, 791, 57.207184F, 42.819435F),
            MapSinglePoint(624, 93, 791, 59.31143F, 33.75541F),
            MapSinglePoint(624, 99, 791, 60.120758F, 28.98061F),
            MapSinglePoint(624, 105, 791, 59.635162F, 23.55838F),
            MapSinglePoint(624, 110, 791, 57.854645F, 13.685094F),
            MapSinglePoint(625, 116, 792, 63.681793F, 15.384578F),
            MapSinglePoint(628, 121, 798, 75.25516F, 25.257872F),
            MapSinglePoint(630, 121, 806, 90.87515F, 27.44295F),
            MapSinglePoint(630, 121, 813, 114.183044F, 28.980597F),
            MapSinglePoint(627, 121, 821, 136.11444F, 30.1136F),
            MapSinglePoint(621, 121, 825, 157.23659F, 31.651247F),
            MapSinglePoint(614, 121, 823, 176.49736F, 31.084745F),
            MapSinglePoint(608, 121, 818, -164.80821F, 31.084745F),
            MapSinglePoint(604, 121, 811, -139.72075F, 30.841959F),
            MapSinglePoint(604, 117, 805, -132.518F, 26.14809F),
            MapSinglePoint(610, 114, 802, -144.0911F, 14.57532F),
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
            MapSinglePoint(635.5, 118, 800.5, 90, 35),
            MapSinglePoint(635.5, 107, 800.5, 90, 35),
            MapSinglePoint(635.5, 94, 800.5, 90, 35),
        ),
    ),
    //endregion

    //region TREE_DECORATE
    TREE_DECORATE(
        TreeDecorate::class,
        "<colour:#c293db>ᴅᴇᴄᴏʀᴀᴛᴇ ᴛʜᴇ ᴛʀᴇᴇ".style(),
        ItemStack(Material.OAK_LEAVES),
        TextColor.fromHexString("#c293db")!!,
        " • ᴛʜᴇ ʟᴀsᴛ ᴇᴠᴇɴᴛ! ᴅᴇᴄᴏʀᴀᴛᴇ ᴛʜᴇ ʟᴏʙʙʏ ᴄʜʀɪsᴛᴍᴀs ᴛʀᴇᴇ ᴡɪᴛʜ ᴀssᴏʀᴛᴇᴅ ᴅᴇᴄᴏʀᴀᴛɪᴏɴs.\n\n" +
                " • ᴅᴇᴄᴏʀᴀᴛɪᴏɴs ᴡɪʟʟ ʙᴇ ᴀᴠᴀɪʟᴀʙʟᴇ ᴛʜʀᴏᴜɢʜ ʏᴏᴜʀ ʜᴏᴛʙᴀʀ. ᴍᴏʀᴇ ᴄᴀɴ ʙᴇ ʙʀᴏᴜɢʜᴛ ɪɴ ᴛʜʀᴏᴜɢʜ ᴛʜᴇ ᴄʀᴇᴀᴛɪᴠᴇ ɪɴᴠᴇɴᴛᴏʀʏ!\n\n" +
                " • ᴛʜᴀɴᴋ ʏᴏᴜ ꜰᴏʀ ᴊᴏɪɴɪɴɢ ᴜs ꜰᴏʀ ᴛʜɪs ᴇᴠᴇɴᴛ :]",
        1,
        MapSinglePoint(545, 115, 384, 0, -34),
        70 to 0.5,
        listOf(
            MapRegion.single(MapSinglePoint(586, 209, 442, 89.26306F, 2.8403294F)),
            MapRegion.single(MapSinglePoint(575, 209, 461, 124.62903F, 0.25062752F)),
            MapRegion.single(MapSinglePoint(562, 209, 475, 146.48047F, 2.5975444F)),
            MapRegion.single(MapSinglePoint(543, 209, 483, -179.44702F, 3.164041F)),
            MapRegion.single(MapSinglePoint(519, 209, 475, -139.14319F, 3.4068255F)),
            MapRegion.single(MapSinglePoint(507, 209, 461, -116.7251F, 4.7826056F)),
            MapRegion.single(MapSinglePoint(501, 209, 444, -95.68262F, 3.406826F)),
            MapRegion.single(MapSinglePoint(503, 209, 427, -65.33313F, 4.7826056F)),
            MapRegion.single(MapSinglePoint(517, 209, 409, -28.266235F, 6.1583858F)),
            MapRegion.single(MapSinglePoint(539, 209, 403, -12.079834F, 3.3258967F)),
            MapRegion.single(MapSinglePoint(559, 209, 410, 22.962524F, 6.1583834F)),
            MapRegion.single(MapSinglePoint(583, 180, 432, 75.00098F, 0.4934106F)),
            MapRegion.single(MapSinglePoint(571, 180, 460, 131.89368F, -1.1251532F)),
            MapRegion.single(MapSinglePoint(497, 138, 453, -98.29736F, 5.7537436F)),
            MapRegion.single(MapSinglePoint(524, 242, 448, -92.470215F, 6.967665F)),
            MapRegion.single(MapSinglePoint(575, 242, 445, 87.837524F, 4.782604F)),
        ),
        listOf(
            MapSinglePoint(548, 121, 384, 2.602295F, -21.333181F),
            MapSinglePoint(555, 126, 384, 9.967163F, -18.257893F),
            MapSinglePoint(563, 132, 389, 16.03711F, -14.211476F),
            MapSinglePoint(571, 138, 400, 16.03711F, -14.211476F),
            MapSinglePoint(585, 160, 426, 30.92871F, -9.679501F),
            MapSinglePoint(592, 166, 448, 75.1156F, -4.581027F),
            MapSinglePoint(580, 185, 473, 104.33093F, 2.2169394F),
            MapSinglePoint(546, 211, 496, 136.37854F, 7.6391277F),
            MapSinglePoint(526, 216, 498, 171.74475F, 11.280895F),
            MapSinglePoint(512, 217, 490, -156.93433F, 12.494814F),
            MapSinglePoint(508, 212, 477, -145.36096F, 12.737597F),
            MapSinglePoint(510, 206, 465, -122.13342F, 9.824187F),
            MapSinglePoint(508, 201, 456, -109.508545F, 9.824187F),
            MapSinglePoint(506, 195, 448, -90.81641F, 7.4772716F),
            MapSinglePoint(505, 189, 440, -83.77905F, 8.286553F),
            MapSinglePoint(506, 184, 432, -64.761475F, 3.3499343F),
            MapSinglePoint(511, 168, 422, -54.88965F, 1.245802F),
            MapSinglePoint(521, 145, 408, -34.98291F, -6.1995897F),
            MapSinglePoint(531, 139, 399, -9.330566F, -9.679501F),
            MapSinglePoint(537, 134, 392, -1.9699707F, -12.107337F),
            MapSinglePoint(543, 134, 386, 3.2045898F, -9.355779F),
        ),
        listOf(
            MapSinglePoint(559.5, 103, 518.5, 135, 0)
        ),
        listOf(
            MapSinglePoint(561, 269, 419, 35.624023F, 26.92271F),
            MapSinglePoint(561, 234, 419, 35.624023F, 26.92271F),
            MapSinglePoint(576, 205, 438, 78.59668F, 24.009295F),
            MapSinglePoint(577, 180, 467, 119.5459F, 30.40262F),
            MapSinglePoint(566, 154, 491, 149.0852F, 28.379414F),
            MapSinglePoint(555, 129, 500, 175.46826F, 13.407703F),
        ),
        false
    )
    //endregion
}
