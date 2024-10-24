package gg.flyte.christmas.npc

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.npc.NPC
import com.github.retrooper.packetevents.protocol.player.TextureProperty
import com.github.retrooper.packetevents.protocol.player.UserProfile
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.google.gson.JsonParser
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.UUID

/**
 * Wrapper for a Packet-based ServerPlayer (NPC) through PacketEvent's [NPC] implementation.
 */
class WorldNPC private constructor(displayName: String, textureProperties: List<TextureProperty?>?, val location: Location) {

    private val userProfile: UserProfile = UserProfile(UUID.randomUUID(), displayName, textureProperties)
    var id: Int = SpigotReflectionUtil.generateEntityId()
    private val tablistName = Component.text("NPC-$id")
    private val npc: NPC = NPC(userProfile, id, tablistName)

    /**
     * Spawns this NPC for the given player via packets
     */
    fun spawnFor(player: Player) {
        val user = PacketEvents.getAPI().playerManager.getUser(player)

        this.npc.location = SpigotConversionUtil.fromBukkitLocation(location)
        this.npc.spawn(user.channel)
        user.sendPacket(WrapperPlayServerEntityMetadata(id, listOf(EntityData(17, EntityDataTypes.BYTE, 127.toByte()))))
    }

    /**
     * Despawns this NPC for the given player via packets
     */
    fun despawnFor(player: Player) {
        this.npc.despawn(PacketEvents.getAPI().playerManager.getUser(player).channel)
    }

    companion object {
        private val worldNPCs: MutableSet<WorldNPC> = HashSet()
        private val leaderBoardNPCs = HashMap<Int, WorldNPC>()
        private val leaderboardPositionToLocation = HashMap<Int, Location>().apply {
            // TODO mark 1st, 2nd, 3rd, etc. lb positions to corresponding locations on the map.
        }

        /**
         * TODO
         */
        fun setLeaderBoardNPC(position: Int, player: Player) {
            // remove existing leader, if any, and spawn new leader
            for (player in Bukkit.getOnlinePlayers()) leaderBoardNPCs[position]?.despawnFor(player)

            leaderBoardNPCs[position] = createFromLive(player, leaderboardPositionToLocation[position]!!)

            for (player in Bukkit.getOnlinePlayers()) leaderBoardNPCs[position]?.spawnFor(player)
        }

        /**
         * Creates a new [WorldNPC] from an existing [Player] reference.
         */
        fun createFromLive(player: Player, location: Location): WorldNPC {
            // use the live player's texture properties
            val user = PacketEvents.getAPI().playerManager.getUser(player)
            val textureProperties = user.profile.textureProperties
            var randomColour: String = listOf("4", "c", "6", "2", "a", "9").random()

            return WorldNPC("§$randomColour ${player.name}", textureProperties, location).also { worldNPCs += it }
        }

        /**
         * Creates a new [WorldNPC] from a player name.
         *
         * @see getDataFromName
         */
        fun createFromName(playerName: String, location: Location): WorldNPC {
            // fetch texture properties from Mojang using player name
            val textureData = getDataFromName(playerName)
            if (textureData == null) throw RuntimeException("COULD NOT GET TEXTURE DATA FOR NPC WITH NAME: $playerName")
            val textureProperty = TextureProperty("textures", textureData[0]!!, textureData[1]!!)

            var randomColour: String = listOf("4", "c", "6", "2", "a", "9").random()

            return WorldNPC("§$randomColour$playerName", listOf(textureProperty), location).also { worldNPCs += it }
        }

        /**
         * Fetches texture data from Mojang's API using a player name.
         */
        private fun getDataFromName(name: String?): Array<String?>? {
            try {
                val mojangProfileAPIUrl = URL("https://api.mojang.com/users/profiles/minecraft/$name")
                val profileInputStream = InputStreamReader(mojangProfileAPIUrl.openStream())
                val uniqueId = JsonParser().parse(profileInputStream).asJsonObject.get("id").asString

                val mojangSessionAPIURL = URL("https://sessionserver.mojang.com/session/minecraft/profile/$uniqueId?unsigned=false")
                val sessionInputStream = InputStreamReader(mojangSessionAPIURL.openStream())
                val textureProperty = JsonParser().parse(sessionInputStream).asJsonObject.get("properties").asJsonArray.get(0).asJsonObject
                val texture = textureProperty.get("value").asString
                val signature = textureProperty.get("signature").asString

                return arrayOf(texture, signature)
            } catch (e: IOException) {
                System.err.println("Could not get skin data from session servers!")
                e.printStackTrace()
                return null
            }
        }
    }
}
