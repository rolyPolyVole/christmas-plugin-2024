package gg.flyte.christmas.util

import com.github.retrooper.packetevents.util.Quaternion4f
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.twilight.scheduler.delay
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.*
import kotlin.math.withSign

class CameraSequence(
    private val players: Collection<Player>,
    private val component: Component?,
    private val locations: List<Location>,
    private val totalDuration: Int,
    private val onComplete: (() -> Unit)? = null
) {
    init {
        teleportPlayersThroughLocations()
    }

    private fun teleportPlayersThroughLocations() {
        if (locations.isEmpty()) {
            players.forEach { it.sendMessage("${ChatColor.RED}No locations provided for teleportation.") }
            return
        }

        // Fixed teleportDuration between 1 and 59 ticks for movement interpolation
        val teleportDuration = 10 // Adjust as needed (must be between 1 and 59)

        // Calculate the number of steps based on totalDuration and teleportDuration
        val numberOfSteps = totalDuration / teleportDuration

        // Generate a smooth path with the calculated number of steps (positions and rotations)
        val smoothPath = generateSmoothPath(locations, numberOfSteps)

        // Start moving the players along the smooth path
        SmoothPathTeleportTask(players, smoothPath, teleportDuration).runTaskTimer(
            ChristmasEventPlugin.getInstance(),
            0L,
            teleportDuration.toLong()
        )
    }

    private fun generateSmoothPath(controlPoints: List<Location>, totalSamples: Int): List<Location> {
        val smoothPath = mutableListOf<Location>()

        if (controlPoints.size < 2) {
            return smoothPath
        }

        // Generate positions using Catmull-Rom spline
        val positionPath = mutableListOf<Vector>()
        val numSegments = controlPoints.size - 1

        val samplesPerSegment = totalSamples / numSegments
        val remainder = totalSamples % numSegments

        for (i in 0 until numSegments) {
            val p0 = if (i > 0) controlPoints[i - 1].toVector() else controlPoints[i].toVector()
            val p1 = controlPoints[i].toVector()
            val p2 = controlPoints[i + 1].toVector()
            val p3 = if (i + 2 < controlPoints.size) controlPoints[i + 2].toVector() else controlPoints[i + 1].toVector()

            // Adjust samples for this segment to distribute the remainder
            val segmentSamples = samplesPerSegment + if (i < remainder) 1 else 0

            for (j in 0 until segmentSamples) {
                val t = j.toDouble() / segmentSamples
                val point = catmullRomSpline(p0, p1, p2, p3, t, 0.0) // Use standard tension for position
                positionPath.add(point)
            }
        }

        // Generate quaternions from yaw and pitch
        val controlQuaternions = controlPoints.map { quaternionFromYawPitch(it.yaw, it.pitch) }

        // Interpolate quaternions along the path
        val quaternionPath = interpolateQuaternions(controlQuaternions, positionPath.size)

        // Combine position and rotation into smoothPath
        for (i in positionPath.indices) {
            val loc = positionPath[i].toLocation(controlPoints[0].world)

            // Convert quaternion back to yaw and pitch
            val (yaw, pitch) = yawPitchFromQuaternion(quaternionPath[i])
            loc.yaw = yaw
            loc.pitch = pitch

            smoothPath.add(loc)
        }

        return smoothPath
    }

    private fun quaternionFromYawPitch(yaw: Float, pitch: Float): Quaternion4f {
        val yawRad = Math.toRadians(-yaw.toDouble()) // Negative yaw to match Minecraft's coordinate system
        val pitchRad = Math.toRadians(-pitch.toDouble())

        // Create quaternion from Euler angles
        val cy = cos(yawRad * 0.5)
        val sy = sin(yawRad * 0.5)
        val cp = cos(pitchRad * 0.5)
        val sp = sin(pitchRad * 0.5)

        val w = (cy * cp).toFloat()
        val x = (cy * sp).toFloat()
        val y = (sy * cp).toFloat()
        val z = (-sy * sp).toFloat()

        return Quaternion4f(x, y, z, w)
    }

    private fun yawPitchFromQuaternion(q: Quaternion4f): Pair<Float, Float> {
        // Normalize quaternion
        val qNorm = normalizeQuaternion(q)

        // Convert quaternion to Euler angles
        val sinPitch = -2.0 * (qNorm.y * qNorm.z - qNorm.w * qNorm.x)
        val pitchRad = when {
            abs(sinPitch) >= 1 -> (Math.PI / 2).withSign(sinPitch)
            else -> asin(sinPitch)
        }

        val yawRad = atan2(
            2.0 * (qNorm.x * qNorm.z + qNorm.w * qNorm.y),
            (qNorm.w * qNorm.w - qNorm.x * qNorm.x - qNorm.y * qNorm.y + qNorm.z * qNorm.z).toDouble()
        )

        var yaw = Math.toDegrees(-yawRad).toFloat() // Negative to match Minecraft's coordinate system
        val pitch = Math.toDegrees(-pitchRad).toFloat()

        // Normalize yaw to [0,360)
        yaw = ((yaw % 360) + 360) % 360

        return Pair(yaw, pitch)
    }

    private fun interpolateQuaternions(controlQuaternions: List<Quaternion4f>, totalSamples: Int): List<Quaternion4f> {
        val quaternionPath = mutableListOf<Quaternion4f>()

        val numSegments = controlQuaternions.size - 1

        if (numSegments <= 0) {
            // Not enough quaternions to interpolate
            return quaternionPath
        }

        val samplesPerSegment = totalSamples / numSegments
        val remainder = totalSamples % numSegments

        for (i in 0 until numSegments) {
            var q1 = controlQuaternions[i]
            var q2 = controlQuaternions[i + 1]

            // Adjust samples for this segment to distribute the remainder
            val segmentSamples = samplesPerSegment + if (i < remainder) 1 else 0

            for (j in 0 until segmentSamples) {
                val t = j.toDouble() / segmentSamples
                val interpolated = slerp(q1, q2, t)
                quaternionPath.add(interpolated)
            }
        }

        // Ensure the path has exactly totalSamples quaternions
        if (quaternionPath.size > totalSamples) {
            return quaternionPath.subList(0, totalSamples)
        }

        return quaternionPath
    }

    private fun slerp(q1: Quaternion4f, q2: Quaternion4f, t: Double): Quaternion4f {
        // Compute the cosine of the angle between the two quaternions
        var q2Var = q2
        var dot = q1.x * q2Var.x + q1.y * q2Var.y + q1.z * q2Var.z + q1.w * q2Var.w

        // If the dot product is negative, negate one quaternion to take the shorter path
        if (dot < 0.0f) {
            q2Var = Quaternion4f(-q2Var.x, -q2Var.y, -q2Var.z, -q2Var.w)
            dot = -dot
        }

        val DOT_THRESHOLD = 0.9995
        if (dot > DOT_THRESHOLD) {
            // The quaternions are very close, use linear interpolation
            val result = Quaternion4f(
                q1.x + (t * (q2Var.x - q1.x)).toFloat(),
                q1.y + (t * (q2Var.y - q1.y)).toFloat(),
                q1.z + (t * (q2Var.z - q1.z)).toFloat(),
                q1.w + (t * (q2Var.w - q1.w)).toFloat()
            )
            return normalizeQuaternion(result)
        }

        // Perform SLERP
        val theta0 = acos(dot.toDouble())        // Angle between input quaternions
        val theta = theta0 * t                      // Angle between q1 and result
        val sinTheta = sin(theta)                // Compute this value only once
        val sinTheta0 = sin(theta0)              // Compute this value only once

        val s0 = cos(theta) - dot * sinTheta / sinTheta0
        val s1 = sinTheta / sinTheta0

        return Quaternion4f(
            (s0 * q1.x + s1 * q2Var.x).toFloat(),
            (s0 * q1.y + s1 * q2Var.y).toFloat(),
            (s0 * q1.z + s1 * q2Var.z).toFloat(),
            (s0 * q1.w + s1 * q2Var.w).toFloat()
        )
    }

    private fun normalizeQuaternion(q: Quaternion4f): Quaternion4f {
        val mag = sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w)

        if (mag == 0f) {
            return Quaternion4f(0f, 0f, 0f, 1f)
        }

        return Quaternion4f(
            q.x / mag,
            q.y / mag,
            q.z / mag,
            q.w / mag
        )
    }

    private fun catmullRomSpline(p0: Vector, p1: Vector, p2: Vector, p3: Vector, t: Double, tension: Double): Vector {
        val t2 = t * t
        val t3 = t2 * t

        val m0 = p2.clone().subtract(p0).multiply((1 - tension) / 2)
        val m1 = p3.clone().subtract(p1).multiply((1 - tension) / 2)

        val a0 = p1.clone().multiply(2 * t3 - 3 * t2 + 1)
        val a1 = m0.clone().multiply(t3 - 2 * t2 + t)
        val a2 = m1.clone().multiply(t3 - t2)
        val a3 = p2.clone().multiply(-2 * t3 + 3 * t2)

        return a0.add(a1).add(a2).add(a3)
    }

    private inner class SmoothPathTeleportTask(
        private val players: Collection<Player>,
        private val path: List<Location>,
        private val teleportDuration: Int
    ) : BukkitRunnable() {
        private var currentIndex = 0
        private var itemDisplay: ItemDisplay? = null
        private var textDisplay: TextDisplay? = null

        /**
         * Due to the atrocities of the Bukkit API, this runnable must maintain a very specific
         * code execution order, so that the ItemDisplay continues to interpolate, the player
         * continues to spectate, and the TextDisplay is continually mounted onto the ItemDisplay.
         *
         * If the order of execution is changed, there will be breakages with an unknown number of API components.
         *
         * The current order is such that:
         * - ItemDisplay's teleportDuration must be set in the spawn lambda
         * - TextDisplay's transformation must be set in the spawn lambda
         * - The spawn lambdas for both entities must be separate.
         * - Player must be teleported and set to spectator at least 5 ticks after the ItemDisplay is spawned.
         * - The TextDisplay must be removed before each ItemDisplay teleporation.
         * - The TextDisplay must be re-added as a passenger after each ItemDisplay teleportation.
         */
        // TODO remove ItemDisplay and just use TextDisplay?
        override fun run() {
            if (currentIndex >= path.size) {
                this.cancel()
                itemDisplay?.remove()
                textDisplay?.remove()
                onComplete?.invoke()
                return
            }

            val nextPosition = path[currentIndex]

            if (itemDisplay == null) {
                // Spawn a single ItemDisplay entity at the starting location
                itemDisplay = nextPosition.world.spawn(nextPosition, ItemDisplay::class.java) { itemDisplay ->
                    itemDisplay.setItemStack(ItemStack(Material.AIR))
                    itemDisplay.setRotation(nextPosition.yaw, nextPosition.pitch)
                    itemDisplay.teleportDuration = this.teleportDuration

                    if (component != null) {
                        textDisplay = nextPosition.world.spawn(nextPosition, TextDisplay::class.java) { textDisplay ->
                            textDisplay.text(component)
                            textDisplay.alignment = TextDisplay.TextAlignment.LEFT
                            textDisplay.lineWidth = 300
                            textDisplay.backgroundColor = Color.fromARGB(225, 38, 38, 38)
                            textDisplay.billboard = Display.Billboard.CENTER
                            textDisplay.isSeeThrough = true
                            textDisplay.interpolationDuration = 50 // 50 ticks to move up
                            textDisplay.interpolationDelay = 50 // 50 ticks to stay down
                            textDisplay.transformation = textDisplay.transformation.apply {
                                translation.add(0F, -10F, -3F)
                            }

                            delay(5) {
                                textDisplay.transformation = textDisplay.transformation.apply {
                                    translation.add(0F, 8.5F, 0F)
                                }
                            }
                        }
                    }
                    players.forEach { player ->
                        player.gameMode = GameMode.SPECTATOR
                        player.teleport(itemDisplay!!.location);
                        delay(5) {
                            player.spectatorTarget = itemDisplay;
                        }
                    }
                }

            } else {
                itemDisplay?.apply {
                    if (textDisplay != null) removePassenger(textDisplay!!)
                    teleport(nextPosition)
                    if (textDisplay != null) addPassenger(textDisplay!!)
                }
            }

            currentIndex++
        }
    }
}