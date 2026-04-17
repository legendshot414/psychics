package io.github.godlegendshot.psychics.ability.superjump

import io.github.monun.psychics.AbilityConcept
import io.github.monun.psychics.ActiveAbility
import io.github.monun.psychics.attribute.EsperAttribute
import io.github.monun.psychics.damage.Damage
import io.github.monun.psychics.damage.DamageType
import io.github.legendshot414.tap.config.Config
import io.github.legendshot414.tap.config.Name
import net.kyori.adventure.text.Component.text
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerVelocityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

// 지정한 대상에게 폭죽 효과와 피해를 입히는 능력
@Name("superjump")
class AbilityConceptSuperjump : AbilityConcept() {

    @Config
    var jumppower : Double = 5.0

    init {
        cooldownTime = 5000L
        range = 64.0
        cost = 10.0
        damage = Damage.of(DamageType.RANGED, EsperAttribute.ATTACK_DAMAGE to 4.0)
        description = listOf(
            text("지정한 대상에게 폭발을 일으킵니다.")
        )
        wand = ItemStack(Material.GOLD_INGOT)
    }
}

class AbilitySuperjump : ActiveAbility<AbilityConceptSuperjump>(), Listener {
    private var ablejump : Boolean = false
    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?) {
        cooldownTime =concept.cooldownTime
        ablejump = true
    }
    @EventHandler(ignoreCancelled = true)
    fun onVelocity(event: PlayerVelocityEvent) {

        if(ablejump){
            val player = esper.player
            val playerLoc: Location = player.location
            val direction: Vector = playerLoc.direction
            val targetLoc: Location = playerLoc.clone().add(direction.clone().multiply(5.0))
            player.velocity.add(targetLoc.toVector().subtract(playerLoc.toVector()).normalize().multiply(concept.jumppower))
            ablejump = false
        }

    }
}