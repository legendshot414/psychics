package io.github.godlegendshot.psychics.ability.invincible

import io.github.monun.psychics.Ability
import io.github.monun.psychics.AbilityConcept
import io.github.legendshot414.tap.config.Name
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerVelocityEvent



//project-invincible
@Name("invincible")
class AbilityConceptInvincible : AbilityConcept() {
    init {
        displayName = "무적"
        description = listOf(
            text("무적의 힘으로 데미지를 입지 않는다."),
            text("절★대★무★적").color(NamedTextColor.YELLOW)
        )
    }
}

class  AbilityInvincible : Ability<AbilityConceptInvincible>() ,Listener {
    override fun onEnable() {
        psychic.registerEvents(this)
    }
    @EventHandler(ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        val damage = 0.0
        event.damage = damage
        val player = esper.player
        val location = player.location.apply { y += 1.8 }
        val world = location.world
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, location, 20, 0.5, 0.0, 0.5, 0.0, null, true)
        world.spawnParticle(Particle.FIREWORK, location, 20, 0.5,0.0,0.5,0.0, null, true)
    }

    @EventHandler(ignoreCancelled = true)
    fun onVelocity(event: PlayerVelocityEvent) {
        event.isCancelled = true
    }
}