package io.github.godlegendshot.psychics.ability.supernova

import io.github.monun.psychics.AbilityConcept
import io.github.monun.psychics.AbilityType
import io.github.monun.psychics.ActiveAbility
import io.github.monun.psychics.PsychicProjectile
import io.github.monun.psychics.damage.DamageType
import io.github.monun.psychics.damage.psychicDamage
import io.github.monun.psychics.util.TargetFilter
import io.github.monun.tap.config.Config
import io.github.monun.tap.config.Name
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.Movement
import io.github.monun.tap.fake.Trail
import io.github.monun.tap.math.normalizeAndLength
import io.github.monun.tap.trail.TrailSupport
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.max

@Name("Supernova")
class AbilityConceptSupernova :  AbilityConcept(){

    @Config
    var lightSpeed = 4.0

    @Config
    var maxExplosionRange = 3.0

    @Config
    var maxExplosionDamage = 500.0

    @Config
    var manacharge = 0.1

    init {
        type = AbilityType.ACTIVE
        displayName = "빛의 검: 슈퍼노바"
        wand = ItemStack(Material.DIAMOND_SWORD)
        cost = 500.0
        cooldownTime = 10000L
        range = 128.0
        description = listOf(
            text("충전하여 발사하세요!").color(NamedTextColor.BLUE),
            text("강력한 탄환으로 적들을 일격에 쓸어버리세요!").color(NamedTextColor.BLUE),
            text("웅크리기를 통해 충전하실 수 있습니다."),
            text("탄환이 터질 시 주변에 큰 피해를 줍니다."),
            text("충전된 수준의 데미지가 상대에게 들어가게 됩니다.")
        )
    }
}

class AbilitySupernova : ActiveAbility<AbilityConceptSupernova>(), Listener{
    private var chargeStart:Boolean = false
    private var chargeper:Double = 0.0
    private var consumemana = 0.0
    private var charging :Boolean = false
    private var perfectcharge :Boolean = false

    override fun onEnable() {
        psychic.registerEvents(this)
        psychic.runTaskTimer({
            if (chargeStart){
                if (chargeper < 100.0){
                    perfectcharge = false
                    esper.player.sendActionBar(text("마력 충전 중..! ${chargeper.toInt()}%").color(NamedTextColor.BLUE))
                }
                else if (chargeper >= 100.0){
                    chargeper = 100.0
                    perfectcharge = true
                    esper.player.sendActionBar(text("마력 충전 100%").color(NamedTextColor.GOLD))
                }
            }
            if (charging && chargeper < 100) chargeper += concept.manacharge
        },0L, 0L)


    }

    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?){
        if (!chargeStart){
            chargeStart = true
            perfectcharge = false
        }
        else{
            if (chargeper > 0) {
                consumemana = concept.cost * chargeper / 100
                psychic.consumeMana(consumemana)
                fire()
                cooldownTime = concept.cooldownTime
                chargeStart = false
            }
        }


    }

    @EventHandler
    fun onPlayerCharging(event: PlayerToggleSneakEvent){
        if(chargeStart){
            charging = event.isSneaking
        }
    }

    private fun fire(){
        val player = esper.player
        val location = player.eyeLocation
        val projectile = LightProjectile().apply {
            light =
                this@AbilitySupernova.psychic.spawnFakeEntity(
                    location, ArmorStand::class.java
                ).apply {
                    velocity = location.direction.multiply(concept.lightSpeed)
                    updateMetadata {
                        isVisible = false
                        isMarker = true
                    }
                    updateEquipment {
                        helmet = ItemStack(Material.SEA_LANTERN)
                    }
                }
            }
        psychic.launchProjectile(location, projectile)
    }



    inner class LightProjectile : PsychicProjectile(1200, concept.range){
        lateinit var light: FakeEntity<ArmorStand>

        override fun onMove(movement: Movement) {
            light.moveTo(movement.to.clone().apply { y -= 1.62 })
        }

        override fun onTrail(trail: Trail) {
            trail.velocity?.let{ v ->
                val start = trail.from
                val world = start.world
                if (!perfectcharge){
                    TrailSupport.trail(start, trail.to, 1.0) { w,x,y,z ->
                        w.spawnParticle(Particle.REDSTONE, x, y, z, 25, 0.5, 0.5, 0.5, 0.25, Particle.DustOptions(Color.PURPLE, 1.0f), true)
                        w.spawnParticle(Particle.REDSTONE, x, y, z, 15, 1.0, 1.0, 1.0, 0.25, Particle.DustOptions(Color.AQUA, 1.5f), true)
                    }
                }
                else if(perfectcharge){
                    TrailSupport.trail(start, trail.to, 1.0) { w,x,y,z ->
                        w.spawnParticle(Particle.REDSTONE, x, y, z, 50, 0.7, 0.7, 0.7, 0.25, Particle.DustOptions(Color.PURPLE, 1.0f), true)
                        w.spawnParticle(Particle.REDSTONE, x, y, z, 10, 1.5, 1.5, 1.5, 0.25, Particle.DustOptions(Color.AQUA, 1.5f), true)
                    }
                }

                val length = v.normalizeAndLength()

                if (length > 0.0) {


                    world.rayTrace(
                        start, v, length, FluidCollisionMode.NEVER, true, 1.0, TargetFilter(esper.player)
                    )?.let { result ->
                        remove()

                        val hitLocation = result.hitPosition.toLocation(world)
                        val radius = max(1.0, concept.maxExplosionRange)
                        var damage = concept.maxExplosionDamage * (chargeper / 100)
                        chargeper = 0.0
                        perfectcharge = false
                        world.spawnParticle(Particle.EXPLOSION_HUGE, hitLocation, 10, 2.0, 2.0, 2.0, 0.25, null, true)
                        world.playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f)
                        world.getNearbyEntities(hitLocation, radius, radius, radius, TargetFilter(esper.player)).forEach { target ->
                            if (target is LivingEntity) {
                                target.psychicDamage(this@AbilitySupernova, DamageType.BLAST, damage, esper.player, hitLocation)
                            }
                        }
                    }
                }
            }
        }

        override fun onRemove() {
            light.remove()
        }
    }
}