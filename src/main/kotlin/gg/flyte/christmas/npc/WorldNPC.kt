package gg.flyte.christmas.npc

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.npc.NPC
import com.github.retrooper.packetevents.protocol.player.TextureProperty
import com.github.retrooper.packetevents.protocol.player.UserProfile
import com.github.retrooper.packetevents.util.MojangAPIUtil
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.util.packetObj
import gg.flyte.christmas.util.sendPacket
import gg.flyte.christmas.util.style
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

/**
 * Wrapper for a Packet-based ServerPlayer (NPC) through PacketEvent's [NPC] implementation.
 */
class WorldNPC private constructor(displayName: String, textureProperties: List<TextureProperty?>?, val location: Location) {

    private val userProfile: UserProfile = UserProfile(UUID.randomUUID(), displayName, textureProperties)
    private var id: Int = SpigotReflectionUtil.generateEntityId()
    private val tablistName = "NPC-$id".style()
    val npc: NPC = NPC(userProfile, id, tablistName)

    /**
     * Spawns this NPC for the given player via packets
     */
    fun spawnFor(player: Player) {
        this.npc.location = location.packetObj()
        this.npc.spawn(PacketEvents.getAPI().playerManager.getUser(player).channel)
        WrapperPlayServerEntityMetadata(id, listOf(EntityData(17, EntityDataTypes.BYTE, 127.toByte()))).sendPacket(player)
    }

    /**
     * Spawns this NPC for all players via packets
     */
    fun spawnForAll() {
        for (player in Bukkit.getOnlinePlayers()) spawnFor(player)
    }

    /**
     * Despawns this NPC for the given player via packets
     */
    fun despawnFor(player: Player) = this.npc.despawn(PacketEvents.getAPI().playerManager.getUser(player).channel)


    /**
     * Despawns this NPC for all players via packets
     */
    fun despawnForAll() {
        for (player in Bukkit.getOnlinePlayers()) despawnFor(player)
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
            worldNPCs.remove(leaderBoardNPCs[position])

            leaderBoardNPCs[position] = createFromUniqueId("lol", uniqueId, leaderboardPositionToLocation[position]!!)
            for (player in Bukkit.getOnlinePlayers()) leaderBoardNPCs[position]?.spawnFor(player)
        }

        /**
         * Creates a new [WorldNPC] modelled from an existing [Player] reference.
         */
        fun createFromLive(displayName: String, modelAfter: Player, location: Location): WorldNPC {
            val textureProperties = PacketEvents.getAPI().playerManager.getUser(modelAfter).profile.textureProperties
            return WorldNPC(displayName, textureProperties, location).also { worldNPCs += it }
        }

        /**
         * Creates a new [WorldNPC] modelled from a [UUID] reference.
         */
        fun createFromUniqueId(displayName: String, modelAfter: UUID, location: Location): WorldNPC {
            // fetch texture properties from Mojang using player name
            val textureProperty = MojangAPIUtil.requestPlayerTextureProperties(modelAfter)
            return WorldNPC(displayName, textureProperty, location).also { worldNPCs += it }
        }
    }
}
