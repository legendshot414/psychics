package io.github.godlegendshot.psychics.ability.revivecrystal

import io.github.monun.psychics.AbilityConcept
import io.github.monun.psychics.ActiveAbility
import io.github.monun.tap.config.Name
import io.github.monun.tap.fake.FakeEntity
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EnderCrystal
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

// 수정을 생성하고 수정이 있을 때 부활
@Name("ReviveCrystal")
class AbilityConceptReviveCrystal : AbilityConcept() {
    init {
        cooldownTime = 5000L
        description = listOf(
            text("서있는 곳에 수정을 생성합니다.")
        )
        wand = ItemStack(Material.END_ROD)
        cost = 100.0
    }
}

class AbilityReviveCrystal : ActiveAbility<AbilityConceptReviveCrystal>(), Listener {
    private var rCrystal: FakeEntity<EnderCrystal>? = null
    private var observe: FakeEntity<ArmorStand>? = null
    private var chancelife = 0
    private var switch = 0
    private lateinit var teleportloc : Location

    override fun onEnable() {
        psychic.registerEvents(this)
        psychic.runTaskTimer({
            if (rCrystal != null) {
                esper.player.sendActionBar(text("잔여목숨 : ")
                    .color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true)
                    .append(text("${chancelife}")
                        .color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true)))
            }
            if (chancelife == 0) {
                deleteCrystal()
            }
            if (chancelife < switch){
                esper.player.teleport(teleportloc)
                switch = chancelife
            }
        }, 0L, 0L)
    }

    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?) {
        rCrystal = psychic.spawnFakeEntity(esper.player.location, EnderCrystal::class.java).apply {
            isVisible = true
            updateMetadata {
                setCustomName("ReviveCrystal-${esper.player.uniqueId}")
                setCustomNameVisible(true)
            }
        }
        observe = psychic.spawnFakeEntity(esper.player.location, ArmorStand::class.java).apply { updateMetadata {
            isMarker = true}
            isVisible = false
        }
        chancelife += 3
        cooldownTime = concept.cooldownTime
        teleportloc = esper.player.location
        switch = chancelife
    }

    override fun onDisable() {
        deleteCrystal()
    }


    /**수정을 제거합니다 */
    private fun deleteCrystal(){
        rCrystal?.let { fakeEntity ->
            fakeEntity.remove()
            this.rCrystal = null
        }
        observe?.let { fakeEntity ->
            fakeEntity.remove()
            this.observe = null
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (chancelife >= 1){
            event.isCancelled = true
            chancelife -= 1

        }else{
            event.isCancelled = false
        }
    }

    @EventHandler
    fun onEnemyAttackCrystal(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val entity = event.entity

        // 위치를 기준으로 수정 공격 확인
        if (damager == esper.player && entity.customName == "ReviveCrystal-${esper.player.uniqueId}") {
            chancelife = 0
            deleteCrystal()
            esper.player.sendMessage("수정이 공격받아 사라졌습니다!")
            event.isCancelled = true
        }
    }
}
