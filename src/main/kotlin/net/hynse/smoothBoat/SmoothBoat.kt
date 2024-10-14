package net.hynse.smoothBoat

//import net.kyori.adventure.text.Component
//import net.kyori.adventure.text.format.NamedTextColor
//import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Boat
//import org.bukkit.entity.Display
//import org.bukkit.entity.TextDisplay
//import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
//import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class SmoothBoat : JavaPlugin(), Listener {

//    private val boatDisplays = mutableMapOf<Boat, BoatDisplays>()
    private val updateInterval = 1L // Update every tick
//    private val movementThreshold = 0.01 // Minimum movement to trigger an update
    private var updateTask: BukkitTask? = null

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        startUpdateTask()
    }

//    override fun onDisable() {
//        boatDisplays.values.forEach { it.removeAll() }
//        boatDisplays.clear()
//    }

    private fun startUpdateTask() {
        updateTask = object : BukkitRunnable() {
            override fun run() {
                server.worlds.forEach { world ->
                    world.entities.filterIsInstance<Boat>().forEach { updateBoat(it) }
                }
            }
        }.runTaskTimer(this, 0L, updateInterval)
    }


    private fun updateBoat(boat: Boat) {
        val location = boat.location
//        val displays = boatDisplays.getOrPut(boat) { BoatDisplays() }

        // Check if the boat has moved significantly
//        if (!displays.hasMovedSignificantly(location)) {
//            return
//        }

        val maxDistance = 1.3
        val horizontalCount = 3
        val verticalCount = 2
        val rayStartHeight = 0.52

//        var rayIndex = 0
        val rayStartLocation = location.clone().add(0.0, rayStartHeight, 0.0)
        for (direction in pieShieldShapeRays(horizontalCount, verticalCount, boat.location.direction)) {
            val rayTraceResult = raycastGround(rayStartLocation, direction, maxDistance)

//            val hitPoint = rayTraceResult?.hitPosition ?: rayStartLocation.toVector().add(direction.multiply(maxDistance))
            val hitBlock = rayTraceResult?.hitBlock
//            val hitLocation = hitPoint.toLocation(location.world)
//            val distance = rayStartLocation.distance(hitLocation)

//            displays.updateRayDisplay(rayIndex, rayStartLocation, hitLocation) { debugDisplay ->
                if ( hitBlock?.type == Material.WATER) {
                        val waterBlockLocation = hitBlock.location
                        val waterBlockData = hitBlock.blockData
                        val waterLevel = when (waterBlockData) {
                            is Levelled -> {
                                // Get the actual water level from the block data
                                waterBlockLocation.y + 1.0 - (waterBlockData.level / 8.0)
                            }
                            else -> waterBlockLocation.y + 1.0
                        }

//                        debugDisplay.text(Component.text("■", NamedTextColor.AQUA)
//                            .append(Component.text(" Water", NamedTextColor.AQUA))
//                            .append(Component.newline())
//                            .append(Component.text("Level: ${String.format("%.2f", waterLevel)}", NamedTextColor.WHITE))
//                            .append(Component.newline())
//                            .append(Component.text(String.format("%.2f", distance), NamedTextColor.WHITE))
//                            .append(Component.text("m", NamedTextColor.WHITE))
//                        )
//                        debugDisplay.viewRange = 64.0f


                        val currentSpeed = boat.velocity.length()

                        val speedFactor = 0.1 + (currentSpeed * 0.1).coerceAtMost(0.5)
                        val verticalVelocity = waterLevel * speedFactor

// Calculate the maximum vertical velocity based on the current speed
                        val maxVerticalVelocity = 0.2 + (currentSpeed * 0.05).coerceAtMost(0.6)

// Get the current velocity and modify its Y component
                        val newVelocity = boat.velocity.clone()
                        newVelocity.y = verticalVelocity.coerceIn(-maxVerticalVelocity, maxVerticalVelocity)

// Apply the new velocity to the boat
                        boat.velocity = newVelocity

                    }
//                    hitBlock?.type?.isSolid == true -> {
//                        debugDisplay.text(Component.text("■", NamedTextColor.RED)
//                            .append(Component.text(" ${hitBlock.type}", NamedTextColor.RED))
//                            .append(Component.newline())
//                            .append(Component.text(String.format("%.2f", distance), NamedTextColor.WHITE))
//                            .append(Component.text("m", NamedTextColor.WHITE))
//                        )
//                        debugDisplay.viewRange = 64.0f
//                    }
//                    hitBlock?.type == Material.AIR || hitBlock == null -> {
//                        debugDisplay.text(Component.text("■", NamedTextColor.WHITE)
//                            .append(Component.text(" Air", NamedTextColor.WHITE))
//                            .append(Component.newline())
//                            .append(Component.text(String.format("%.2f", distance), NamedTextColor.WHITE))
//                            .append(Component.text("m", NamedTextColor.WHITE))
//                        )
//                        debugDisplay.viewRange = 64.0f
//                    }
//                    else -> {
//                        debugDisplay.text(Component.text("■", NamedTextColor.YELLOW)
//                            .append(Component.text(" ${hitBlock.type}", NamedTextColor.YELLOW))
//                            .append(Component.newline())
//                            .append(Component.text(String.format("%.2f", distance), NamedTextColor.WHITE))
//                            .append(Component.text("m", NamedTextColor.WHITE))
//                        )
//                        debugDisplay.viewRange = 64.0f
//                    }
                }
            }
//            rayIndex++
        }

//        displays.removeExcessRayDisplays(rayIndex)

//        displays.updateInfoDisplay(location.clone().add(0.0, 2.0, 0.0)) { infoDisplay ->
//            infoDisplay.text(Component.text()
//                .append(Component.text("Boat Info", NamedTextColor.GOLD))
//                .append(Component.newline())
//                .append(Component.text("Speed: ${String.format("%.2f", boat.velocity.length())}m/s", NamedTextColor.WHITE))
//                .append(Component.newline())
//                .append(Component.text("Pos: ${String.format("%.2f, %.2f, %.2f", location.x, location.y, location.z)}", NamedTextColor.WHITE))
//                .build()
//            )
//        }
//        displays.updateLastLocation(location)
    private fun raycastGround(location: Location, direction: Vector, maxDistance: Double): RayTraceResult? {
        return location.world!!.rayTraceBlocks(location, direction, maxDistance, FluidCollisionMode.ALWAYS, false)
    }
//    private fun sphereRays(
//        horizontalCount: Int,
//        vertMinAngle: Double,
//        vertMaxAngle: Double,
//        vertCount: Int
//    ) = iterator {
//        val horizontalStep = Math.PI * 2 / horizontalCount
//        val verticalStep = (vertMaxAngle - vertMinAngle) / vertCount
//        for (horizontalIndex in 0 until horizontalCount) {
//            val horizontalAngle = horizontalIndex * horizontalStep
//            for (verticalIndex in 0 until vertCount) {
//                val verticalAngle = vertMinAngle + verticalIndex * verticalStep
//                yield(Vector(
//                    cos(horizontalAngle) * cos(verticalAngle),
//                    sin(verticalAngle),
//                    sin(horizontalAngle) * cos(verticalAngle)
//                ))
//            }
//        }
//    }
//    private fun frontHalfSphereRays(
//        horizontalCount: Int,
//        vertMinAngle: Double,
//        vertMaxAngle: Double,
//        vertCount: Int,
//        boatDirection: Vector
//    ) = iterator {
//        val horizontalStep = Math.PI / horizontalCount
//        val verticalStep = (vertMaxAngle - vertMinAngle) / vertCount
//        val rightVector = boatDirection.clone().crossProduct(Vector(0, 1, 0)).normalize()
//        val upVector = rightVector.clone().crossProduct(boatDirection).normalize()
//
//        for (horizontalIndex in -horizontalCount/2..horizontalCount/2) {
//            val horizontalAngle = horizontalIndex * horizontalStep
//            for (verticalIndex in 0 until vertCount) {
//                val verticalAngle = vertMinAngle + verticalIndex * verticalStep
//                val direction = boatDirection.clone()
//                    .multiply(cos(verticalAngle) * cos(horizontalAngle))
//                    .add(rightVector.clone().multiply(sin(horizontalAngle)))
//                    .add(upVector.clone().multiply(sin(verticalAngle)))
//                    .normalize()
//                yield(direction)
//            }
//        }
//    }
//
//    private fun shieldShapeRays(
//        horizontalCount: Int,
//        verticalCount: Int,
//        boatDirection: Vector
//    ) = iterator {
//        val horizontalStep = 2.0 / (horizontalCount - 1)
//        val verticalStep = 1.0 / (verticalCount - 1)
//        val rightVector = boatDirection.clone().crossProduct(Vector(0, 1, 0)).normalize()
//        val upVector = rightVector.clone().crossProduct(boatDirection).normalize()
//
//        for (horizontalIndex in 0 until horizontalCount) {
//            val horizontalOffset = -1.0 + horizontalIndex * horizontalStep
//            for (verticalIndex in 0 until verticalCount) {
//                val verticalOffset = verticalIndex * verticalStep
//
//                // Create a curved shape by adjusting the vertical offset
//                val adjustedVerticalOffset = (1 - (horizontalOffset * horizontalOffset)) * verticalOffset
//
//                val direction = boatDirection.clone()
//                    .add(rightVector.clone().multiply(horizontalOffset))
//                    .add(upVector.clone().multiply(adjustedVerticalOffset))
//                    .normalize()
//
//                // Only yield directions that are not pointing downwards
//                if (direction.y >= 0) {
//                    yield(direction)
//                }
//            }
//        }
//    }

    private fun pieShieldShapeRays(
        horizontalCount: Int,
        verticalCount: Int,
        boatDirection: Vector
    ) = iterator {
        val horizontalArcAngle = Math.toRadians(55.0) // 85 degrees arc in front of the boat
        val verticalMaxAngle = Math.toRadians(25.0) // 45 degrees vertically
        val horizontalStep = horizontalArcAngle / (horizontalCount - 1)
        val verticalStep = verticalMaxAngle / (verticalCount - 1)
        val rightVector = boatDirection.clone().crossProduct(Vector(0, 1, 0)).normalize()
        val upVector = rightVector.clone().crossProduct(boatDirection).normalize()

        for (horizontalIndex in 0 until horizontalCount) {
            val horizontalAngle = -horizontalArcAngle / 2 + horizontalIndex * horizontalStep
            for (verticalIndex in 0 until verticalCount) {
                val verticalAngle = verticalIndex * verticalStep

                val direction = boatDirection.clone()
                    .multiply(cos(verticalAngle) * cos(horizontalAngle))
                    .add(rightVector.clone().multiply(sin(horizontalAngle)))
                    .add(upVector.clone().multiply(sin(verticalAngle)))
                    .normalize()

                if (direction.y >= 0) {
                    yield(direction)
                }
            }
        }
    }

//    private fun createDebugDisplay(location: Location): TextDisplay {
//        return location.world?.spawn(location, TextDisplay::class.java) { display ->
//            display.billboard = Display.Billboard.CENTER
//            display.viewRange = 16.0f
//            display.isVisibleByDefault = true
//            display.backgroundColor = Color.fromARGB(0, 0, 0, 0) // Transparent background
//            display.alignment = TextDisplay.TextAlignment.CENTER
//            display.isDefaultBackground = false
//            display.isSeeThrough = true
//        } ?: throw IllegalStateException("Failed to create TextDisplay")
//    }
//
//    @EventHandler
//    fun onVehicleDestroy(event: VehicleDestroyEvent) {
//        if (event.vehicle is Boat) {
//            val boat = event.vehicle as Boat
//            boatDisplays[boat]?.removeAll()
//            boatDisplays.remove(boat)
//        }
//    }
//
//    private inner class BoatDisplays {
//        private val rayDisplays = mutableListOf<TextDisplay>()
//        private var infoDisplay: TextDisplay? = null
//        private var lastLocation: Location? = null
//
//        fun hasMovedSignificantly(newLocation: Location): Boolean {
//            return lastLocation == null || lastLocation!!.distance(newLocation) > movementThreshold
//        }
//
//        fun updateLastLocation(location: Location) {
//            lastLocation = location.clone()
//        }
//
//        fun updateRayDisplay(index: Int, boatLocation: Location, targetLocation: Location, update: (TextDisplay) -> Unit) {
//            if (index >= rayDisplays.size) {
//                val newDisplay = createDebugDisplay(boatLocation)
//                rayDisplays.add(newDisplay)
//                update(newDisplay)
//            }
//            val display = rayDisplays[index]
//            display.teleportAsync(targetLocation).thenRun {
//                update(display)
//            }
//        }
//
//        fun updateInfoDisplay(location: Location, update: (TextDisplay) -> Unit) {
//            if (infoDisplay == null) {
//                infoDisplay = createDebugDisplay(location)
//                update(infoDisplay!!)
//            } else {
//                infoDisplay!!.teleportAsync(location).thenRun {
//                    update(infoDisplay!!)
//                }
//            }
//        }
//
//        fun removeExcessRayDisplays(fromIndex: Int) {
//            while (fromIndex < rayDisplays.size) {
//                rayDisplays.last().remove()
//                rayDisplays.removeLast()
//            }
//        }
//
//        fun removeAll() {
//            rayDisplays.forEach { it.remove() }
//            rayDisplays.clear()
//            infoDisplay?.remove()
//            infoDisplay = null
//        }
