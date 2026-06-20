package io.github.monun.psychics.smoothteleport

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.entity.Player
import org.bukkit.Location

fun teleportSmoothly(player: Player, loc: Location) {
    val newloc = loc.clone().apply{
        yaw = player.yaw
        pitch = player.pitch
    }
    player.teleport(
        newloc,
        TeleportFlag.Relative.YAW,
        TeleportFlag.Relative.PITCH
    )
}