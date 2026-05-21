package io.github.godlegendshot.psychics.ability.hinafinale


import io.github.monun.psychics.AbilityConcept
import io.github.monun.psychics.AbilityType
import io.github.monun.psychics.ActiveAbility
import io.github.monun.psychics.PsychicProjectile
import io.github.monun.psychics.attribute.EsperAttribute
import io.github.monun.psychics.attribute.EsperStatistic
import io.github.monun.psychics.damage.Damage
import io.github.monun.psychics.damage.DamageType
import io.github.monun.psychics.smoothteleport.teleportSmoothly
import io.github.monun.psychics.util.TargetFilter
import io.github.legendshot414.tap.config.Config
import io.github.legendshot414.tap.config.Name
import io.github.legendshot414.tap.fake.FakeEntity
import io.github.legendshot414.tap.fake.Movement
import io.github.legendshot414.tap.fake.Trail
import io.github.legendshot414.tap.math.normalizeAndLength
import io.github.legendshot414.tap.trail.TrailSupport
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

@Name("finale-ish-bosheth")
class AbilityConceptHinafinale : AbilityConcept() {

    @Config
    var ammo = 3

    @Config
    var ammoSpeed = 5.0

    @Config
    var ammoDamage = 30.0

    init {
        type = AbilityType.ACTIVE
        displayName = "종막 : 이스보셋"
        wand = ItemStack(Material.STICK)
        cost = 700.0
        damage = Damage.of(DamageType.RANGED, EsperStatistic.of(EsperAttribute.ATTACK_DAMAGE to ammoDamage))
        durationTime = 10000L
        cooldownTime = 50000L
        range = 256.0
        description = listOf(
            text("강력한 탄환을 발사합니다!").color(NamedTextColor.LIGHT_PURPLE),
            text("발사 후 일정 시간 동안 조준 모드가 활성화됩니다."),
            text("조준 모드에서는 남은 탄환 수가 표시됩니다."),
            text("※조준 모드에서 지속 시간이 다 되면 스킬이 종료됩니다").color(NamedTextColor.RED),
            text("\"이제부터가 진짜야\"").color(NamedTextColor.DARK_PURPLE)
        )
    }
}

class AbilityHinafinale : ActiveAbility<AbilityConceptHinafinale>(), Listener {
    var remain: Int = 0
    var aimMode: Boolean = false
    private var aimStart : Observe? = null

    override fun onEnable() {
        psychic.runTaskTimer({
            if (aimMode) {
                esper.player.sendActionBar(text("남은 탄환 : ${remain}발").color(NamedTextColor.LIGHT_PURPLE))
                update()
            }
        }, 0L, 1L)
        psychic.registerEvents(this)
    }

    override fun onDisable() {
        onStopConcentrate()
    }

    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?) {
        if (durationTime > 0L && aimMode) {
            if (remain >= 1){
                remain -= 1
                fire()
                if (remain == 0) onStopConcentrate()
            }
            return
        }
        onConcentrate()
    }

    private fun onStopConcentrate(){
        durationTime = 0L
        cooldownTime = concept.cooldownTime
        aimMode = false
        aimStart?.onRemove()
        aimStart = null
    }

    private fun onConcentrate(){
        aimStart = Observe(esper.player.location)
        val location = esper.player.location
        val world = location.world
        aimMode = true
        remain = concept.ammo
        durationTime = concept.durationTime
        cooldownTime = 500L
        world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0F, 1.0F)
        psychic.consumeMana(concept.cost)
    }

    private fun update(){
        if (durationTime > 0L && aimMode){
            aimStart?.onUpdate()
        }
        else onStopConcentrate()
    }

    inner class Observe(location: Location){
        private val stand: FakeEntity<ArmorStand> = psychic.spawnFakeEntity(location, ArmorStand::class.java).apply {
            updateMetadata {
                isVisible = false
                isMarker = true
            }
        }

        fun onUpdate() {
            val location = stand.location
            val world = location.world

            teleportSmoothly(esper.player,location)

            world.spawnParticle(
                Particle.DUST,
                location.x, location.y, location.z,
                50,
                2.0, 0.0, 2.0,
                1.0, Particle.DustOptions(Color.fromRGB(220, 208, 255), 1.0f), true
            )

            world.spawnParticle(
                Particle.DUST,
                location.x, location.y + 0.9, location.z,
                10,
                2.0, 1.0, 2.0,
                1.0, Particle.DustOptions(Color.fromRGB(220, 208, 255), 1.0f), true
            )
        }

        fun onRemove() {
            stand.remove()
        }
    }

    private fun fire() {
        val player = esper.player
        val location = player.eyeLocation
        val projectile = MythammoProjectile().apply {
            ammo =
                this@AbilityHinafinale.psychic.spawnFakeEntity(
                    location, ArmorStand::class.java
                ).apply {
                    velocity = location.direction.multiply(concept.ammoSpeed)
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

    inner class MythammoProjectile : PsychicProjectile(1200, concept.range) {
        lateinit var ammo: FakeEntity<ArmorStand>

        override fun onMove(movement: Movement) {
            ammo.moveTo(movement.to.clone().apply { y -= 1.62 })
        }

        override fun onTrail(trail: Trail) {
            trail.velocity?.let { v ->
                val start = trail.from
                val world = start.world
                TrailSupport.trail(start, trail.to, 1.0) { w, x, y, z ->
                    w.spawnParticle(
                        Particle.DUST,
                        x,
                        y,
                        z,
                        20,
                        0.2,
                        0.2,
                        0.2,
                        0.5,
                        Particle.DustOptions(Color.fromRGB(220, 208, 255), 1.0f),
                        true
                    )
                }

                val length = v.normalizeAndLength()

                if (length > 0.0) {
                    world.rayTrace(
                        start, v, length, FluidCollisionMode.NEVER, true, 1.0, TargetFilter(esper.player)
                    )?.let { result ->
                        remove()

                        val hitLocation = result.hitPosition.toLocation(world)

                        result.hitEntity?.let { entity ->
                            if (entity is LivingEntity) {
                                entity.psychicDamage()
                                world.playSound(hitLocation, Sound.ENTITY_PLAYER_HURT, 0.5f, 0.5f)
                            }
                        }
                    }
                }
            }
        }

        override fun onRemove() {
            ammo.remove()
        }
    }
}
