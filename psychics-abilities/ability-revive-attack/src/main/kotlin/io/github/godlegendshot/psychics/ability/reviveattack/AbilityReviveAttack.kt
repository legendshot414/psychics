package io.github.godlegendshot.psychics.ability.reviveattack

import io.github.monun.psychics.AbilityConcept
import io.github.monun.psychics.AbilityType
import io.github.monun.psychics.ActiveAbility
import io.github.monun.psychics.attribute.EsperStatistic
import io.github.monun.psychics.tooltip.TooltipBuilder
import io.github.monun.psychics.util.Times
import io.github.legendshot414.tap.config.Config
import io.github.legendshot414.tap.config.Name
import io.github.legendshot414.tap.event.EntityProvider
import io.github.legendshot414.tap.event.TargetEntity
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.max

//부활 시 강인한 사신의 힘으로 초월적으로 공격합니다
@Name("revive-attack")
class AbilityConceptReviveAttack : AbilityConcept() {

    @Config
    var timer :Long = 60000L

    @Config
    var killfeverTime : Long = 10000L

    @Config
    var speedAmplifier = 4

    @Config
    var witherAmplifier = 4

    init {
        type = AbilityType.ACTIVE
        displayName = "불사의 공격"
        wand = ItemStack(Material.STICK)
        cost = 40.0
        durationTime = 10000L
        cooldownTime = 95000L
        description = listOf(
            text("복수하고 싶다는 일념으로"),
            text("일시적으로 부활할 수 있습니다."),
            text("상대에게 복수를 선사하세요!"),
            text("※주의※ 해당 능력은 부활 후 데미지를 입습니다.").color(NamedTextColor.RED),
            text("몹을 죽일 때 마다 체력이 회복됩니다.").color(NamedTextColor.YELLOW)
        )
    }

    override fun onRenderTooltip(tooltip: TooltipBuilder, stats: (EsperStatistic) -> Double) {
        tooltip.header(
            text(
                "부활 후 버프 지속 시간 "
            ).color(
                NamedTextColor.AQUA
            ).decoration(
                TextDecoration.ITALIC, false
            ).decoration(
                TextDecoration.BOLD, true
            ).append(
                text(
                    (
                        timer/1000L
                            ).toInt().toString()).decoration(
                    TextDecoration.BOLD, false
                ).color(
                    NamedTextColor.WHITE
                )
            ).append(
                text().content(
                    "초"
                ).decoration(
                    TextDecoration.BOLD, false
                ).color(
                    NamedTextColor.WHITE
                )
            )
        )
        tooltip.header(
            text(
                "부활 시 킬당 배율 상승이 상승합니다."
            ).color(
                NamedTextColor.DARK_RED
            ).decoration(
                TextDecoration.ITALIC, false
            ).decoration(
                TextDecoration.BOLD, true
            )
        )

        tooltip.header(
            text(
                "부활 시 위더 "
            ).color(
                NamedTextColor.BLACK
            ).decoration(
                TextDecoration.ITALIC, false
            ).decoration(
                TextDecoration.BOLD, true
            ).append(
                text(
                        (
                            witherAmplifier + 1
                                ).toString()
                ).decoration(
                    TextDecoration.BOLD, false
                ).color(
                    NamedTextColor.WHITE
                )
            )
        )

        tooltip.header(
            text(
                "부활 시 신속 "
            ).color(
                NamedTextColor.WHITE
            ).decoration(
                TextDecoration.ITALIC, false
            ).decoration(
                TextDecoration.BOLD, true
            ).append(
                text(
                    (
                        speedAmplifier + 1
                            ).toString()
                ).decoration(
                    TextDecoration.BOLD, false
                ).color(
                    NamedTextColor.WHITE
                )
            )
        )
    }

}

class AbilityReviveAttack : ActiveAbility<AbilityConceptReviveAttack>(), Listener {
    /**스킬 발동 후 죽을 시 발동되는 지속시간*/
    private var durationTime2: Long = 0L
        get() {
            return max(0L, field - Times.current)
        }
        set(value) {
            checkState()

            val times = max(0L, value)
            field = Times.current + times
        }
    /**부활 후 적을 죽일 시 생기는 보너스 타임*/
    private var feverTime: Long = 0L
        get() {
            return max(0L, field - Times.current)
        }
        set(value) {
            checkState()

            val times = max(0L, value)
            field = Times.current + times
        }

    private var killpoint = 0

    override fun onEnable() {
        psychic.registerEvents(this)
        psychic.runTaskTimer({
            if (durationTime2 > 0L){
                esper.player.sendActionBar(text("원래 대로까지 ${(durationTime2 / 1000L).toInt()}초...").color(NamedTextColor.DARK_RED)
                    .append(text("현재 ${(killpoint +2)}배 적용 중!").color(NamedTextColor.GREEN)
                        .append(text("피버 타임 끝까지 ${(feverTime / 1000L).toInt()}초...!").color(NamedTextColor.BLUE))))
                }},0L,0L)
    }

    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?){
        val player = esper.player
        val location = player.location
        player.playSound(location,Sound.ENTITY_ENDER_DRAGON_GROWL,1.0F,1.0F)
        val concept = concept
        durationTime = concept.durationTime
        cooldownTime = concept.cooldownTime
    }


    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val ticks = (concept.timer/50L).toInt()
        val player = esper.player
        val location = player.location
        if (durationTime > 0L) {
            event.isCancelled = true //죽음 무효
            durationTime2 = concept.timer //2차 지속 시간 설정
            durationTime = 0L //지속시간 초기화
            player.addPotionEffect(PotionEffect(
                PotionEffectType.SPEED,
                ticks,
                concept.speedAmplifier,
                true,
                true,
                true
                )
            )
            player.addPotionEffect(PotionEffect(
                PotionEffectType.WITHER,
                ticks,
                concept.witherAmplifier,
                true,
                true,
                true
                )
            )
            player.sendActionBar(text("중요한 것은 어떻게 끝으로 가는가다!!!").color(NamedTextColor.RED))
            player.playSound(location,Sound.ENTITY_ENDER_DRAGON_GROWL,1.0F,1.0F)
        }
        else{
            durationTime2 = 0L
        }

    }
    @EventHandler
    fun onReviveDamage(event: EntityDamageEvent) {
        if(durationTime2 > 0L){ //2차지속 시간내에 데미지를 입을 때 마다 이펙트 발현
            val player = esper.player
            val location = player.location.apply { y += 1.8 }
            val world = location.world
            world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 20, 0.5, 0.0, 0.5, 0.0, null, true)

        }
    }
    @EventHandler(ignoreCancelled = true)
    @TargetEntity(EntityProvider.EntityDamageByEntity.Damager::class)
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (durationTime2 > 0L){
            if (feverTime == 0L) {
            event.damage *= 2 // 기본 데미지 2배
            }else{
            event.damage *= (killpoint + 2) // 죽일 때 마다 데미지 배율 증가
            }
        }
    }
    @EventHandler
    @TargetEntity(EntityProvider.EntityDeath.Killer::class)
    fun onPlayerKill(event: EntityDeathEvent){
        val player = esper.player
        val location = player.location.apply { y += 1.8 }
        val world = location.world
        val potion4 = PotionEffect(PotionEffectType.REGENERATION, 10, 255, true, true, true)
        if (durationTime2 > 0L){
            feverTime = concept.killfeverTime
            killpoint += 1
            player.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL,1.0F,0.5F)
            player.addPotionEffect(potion4)
            world.spawnParticle(Particle.TOTEM_OF_UNDYING, location, 20, 0.5, 0.0, 0.5, 0.0, null, true)
            world.spawnParticle(Particle.COMPOSTER, location, 20, 0.5,0.0,0.5,0.0, null, true)
        }
        if (feverTime == 0L){
                killpoint = 0
        }
    }
}