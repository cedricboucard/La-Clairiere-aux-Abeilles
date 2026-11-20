package fr.clairiere.abeilles.game

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.clairiere.abeilles.audio.SoftSoundPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
  private val prefs = application.getSharedPreferences("clairiere_prefs", Context.MODE_PRIVATE)
  private val saveKey = "clairiere_abeilles_v1"

  private val _state = MutableStateFlow(loadOrCreateState())
  val state: StateFlow<GameState> = _state.asStateFlow()

  private var saveAccumulator = 0f

  init {
    SoftSoundPlayer.enabled = _state.value.soundEnabled
    startLoop()
  }

  private fun startLoop() {
    viewModelScope.launch {
      var lastNs = System.nanoTime()
      while (isActive) {
        val nowNs = System.nanoTime()
        val dt = ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastNs = nowNs

        _state.update { current -> updateSimulation(current, dt) }
        saveAccumulator += dt
        if (saveAccumulator >= 5f) {
          saveAccumulator = 0f
          saveGame(_state.value)
        }
        delay(16L)
      }
    }
  }

  fun toggleSound() {
    _state.update { s ->
      val next = !s.soundEnabled
      SoftSoundPlayer.enabled = next
      s.copy(soundEnabled = next)
    }
    saveGame(_state.value)
  }

  fun resetGame() {
    prefs.edit().remove(saveKey).apply()
    _state.value = createInitialState()
    saveGame(_state.value)
  }

  fun clearCarnetBadge() {
    _state.update { it.copy(carnetBadge = false) }
  }

  fun selectSpecies(species: Species) {
    _state.update {
      it.copy(
        selectedSpecies = species,
        whisper = "Graine sélectionnée : ${species.displayName}. Touche un emplacement pour la semer."
      )
    }
    saveGame(_state.value)
  }

  fun quickPlantNearestSpot() {
    val s = _state.value
    if (s.seeds <= 0) {
      _state.update {
        it.copy(whisper = "Il te faut des graines : touche d'abord un fruit ambré mûr sur l'arbre.")
      }
      return
    }
    val emptySpot = s.spots.firstOrNull {
      it.unlocked && it.flower == null && (it.zone != SpotZone.RIVE || s.bridgeRepaired)
    }
    if (emptySpot != null) {
      handleWorldTap(emptySpot.x, emptySpot.y)
    } else {
      _state.update {
        it.copy(whisper = "Toutes les parcelles ouvertes sont fleuries ! Explore la forêt pour en découvrir d'autres.")
      }
    }
  }

  fun harvestHoney() {
    _state.update { s ->
      if (s.honeyReserve < 0.2f) return@update s
      val gainedNectar = (s.honeyReserve * 60).toInt().coerceAtLeast(10)
      val gainedSeeds = if (s.honeyReserve >= 0.8f) 2 else 1
      SoftSoundPlayer.harvest()
      s.copy(
        honeyReserve = 0f,
        nectar = s.nectar + gainedNectar,
        seeds = s.seeds + gainedSeeds,
        royalPollen = s.royalPollen + 8,
        whisper = "Récolte de la ruche : +$gainedNectar nectar et +$gainedSeeds graine(s) !"
      )
    }
    saveGame(_state.value)
  }

  fun interactFlight(flightId: String) {
    _state.update { s ->
      val updated = s.flights.map { fl ->
        if (fl.id != flightId) return@map fl
        when {
          fl.ready -> {
            SoftSoundPlayer.discover()
            fl.copy(ready = false, active = true, progress = 0.05f)
          }
          !fl.active -> {
            SoftSoundPlayer.plant()
            fl.copy(active = true, progress = 0.05f, ready = false)
          }
          else -> fl
        }
      }
      val target = s.flights.firstOrNull { it.id == flightId }
      val bonusSeeds = if (target?.ready == true) 1 else 0
      val bonusNectar = if (target?.ready == true) 25 else 0
      val bonusPollen = if (target?.ready == true) 10 else 0
      s.copy(
        flights = updated,
        seeds = s.seeds + bonusSeeds,
        nectar = s.nectar + bonusNectar,
        royalPollen = s.royalPollen + bonusPollen
      )
    }
    saveGame(_state.value)
  }

  fun upgradeBlessing(blessingId: String) {
    _state.update { s ->
      val target = s.blessings.firstOrNull { it.id == blessingId } ?: return@update s
      if (target.level >= target.maxLevel || s.royalPollen < target.costPollen) return@update s
      SoftSoundPlayer.discover()
      val updated = s.blessings.map {
        if (it.id == blessingId) it.copy(level = it.level + 1, costPollen = it.costPollen + 20)
        else it
      }
      s.copy(
        royalPollen = s.royalPollen - target.costPollen,
        blessings = updated
      )
    }
    saveGame(_state.value)
  }

  fun handleWorldTap(wx: Float, wy: Float) {
    _state.update { s ->
      // 1. Cliquer sur un fruit mûr de l'arbre
      val ripeBranch = s.branches.firstOrNull {
        it.state == BranchState.RIPE && hypot(wx - it.x, wy - (it.y + 10f)) < 38f
      }
      if (ripeBranch != null) {
        return@update robotGoTo(
          s,
          ripeBranch.x,
          (ripeBranch.y + 160f).coerceIn(415f, 480f),
          RobotAction(RobotActionType.PICK, targetId = ripeBranch.id, duration = 0.45f)
        )
      }

      // 2. Cliquer sur les fougères pour dégager la mare
      if (!s.fernsCleared && wx in 595f..745f && wy in 355f..550f) {
        return@update robotGoTo(
          s,
          615f,
          455f,
          RobotAction(RobotActionType.CLEAR_FERNS, duration = 1.1f)
        )
      }

      // 3. Cliquer sur la passerelle endommagée
      if (s.fernsCleared && !s.bridgeRepaired && wx in 980f..1140f && wy in 385f..525f) {
        return@update robotGoTo(
          s,
          985f,
          458f,
          RobotAction(RobotActionType.REPAIR_BRIDGE, duration = 1.4f)
        )
      }

      // 4. Cliquer sur une plante sauvage de l'autre rive
      if (s.bridgeRepaired) {
        val wild = s.wildPlants.firstOrNull {
          !it.collected && hypot(wx - it.x, wy - it.y) < 42f
        }
        if (wild != null) {
          return@update robotGoTo(
            s,
            wild.x - 24f,
            wild.y + 10f,
            RobotAction(RobotActionType.COLLECT_WILD, targetId = wild.id, duration = 0.85f)
          )
        }
      }

      // 5. Cliquer sur un emplacement de jardin pour semer
      val chosenSpot = s.spots
        .filter { it.unlocked && it.flower == null && (it.zone != SpotZone.RIVE || s.bridgeRepaired) }
        .minByOrNull { hypot(wx - it.x, wy - it.y) }
        ?.takeIf { hypot(wx - it.x, wy - it.y) < 48f }

      if (chosenSpot != null) {
        if (s.seeds <= 0) {
          return@update s.copy(
            whisper = "Il te faut des graines : touche d'abord un fruit ambré mûr sur l'arbre.",
            floatingTexts = s.floatingTexts + FloatingText(
              chosenSpot.x, chosenSpot.y - 18f, "Pas de graines", Color(0xFFFFD4A8)
            )
          )
        }
        val speciesToPlant = pickAppropriateSpecies(s, chosenSpot)
        val standX = if (chosenSpot.zone == SpotZone.EAU) chosenSpot.x - 75f else chosenSpot.x - 16f
        val standY = if (chosenSpot.zone == SpotZone.EAU) chosenSpot.y else chosenSpot.y + 8f
        return@update robotGoTo(
          s,
          standX,
          standY,
          RobotAction(
            RobotActionType.PLANT,
            targetId = chosenSpot.id,
            species = speciesToPlant,
            duration = 0.45f
          )
        )
      }

      // 6. Sinon, promener simplement le petit robot dans les zones accessibles
      val maxX = if (s.fernsCleared) (if (s.bridgeRepaired) 1320f else 990f) else 605f
      val clampedX = wx.coerceIn(120f, maxX)
      val clampedY = wy.coerceIn(365f, 685f)
      val inPond = clampedX in 780f..995f && clampedY in 365f..580f
      if (!inPond) {
        return@update robotGoTo(s, clampedX, clampedY, null)
      }
      s
    }
  }

  private fun pickAppropriateSpecies(s: GameState, spot: PlantingSpot): Species {
    if (spot.zone == SpotZone.EAU) return Species.NENUPHAR
    val sel = s.selectedSpecies
    if (spot.zone == SpotZone.MARE && sel !in listOf(Species.IRIS, Species.MENTHE, Species.CAMPANULE, Species.BRUYERE)) {
      if (Species.IRIS in s.discoveredSpecies) return Species.IRIS
    }
    if (sel.zone == null || sel.zone == spot.zone || spot.zone == SpotZone.RIVE || spot.zone == SpotZone.MARE) {
      return sel
    }
    return Species.AMBRE
  }

  private fun robotGoTo(s: GameState, x: Float, y: Float, action: RobotAction?): GameState {
    val queue = mutableListOf<RobotWaypoint>()
    val crossFromLeft = s.robot.x < 1040f && x >= 1040f
    val crossFromRight = s.robot.x >= 1040f && x < 1040f
    if ((crossFromLeft || crossFromRight) && s.bridgeRepaired) {
      queue.add(RobotWaypoint(990f, 455f, null))
      queue.add(RobotWaypoint(1125f, 455f, null))
    }
    queue.add(RobotWaypoint(x, y, action))
    return s.copy(robot = s.robot.copy(queue = queue, action = null))
  }

  private fun completeRobotAction(s: GameState, act: RobotAction): GameState {
    return when (act.type) {
      RobotActionType.PICK -> {
        val branch = s.branches.firstOrNull { it.id == act.targetId } ?: return s
        if (branch.state != BranchState.RIPE) return s
        SoftSoundPlayer.pick()
        val newBranches = s.branches.map {
          if (it.id == branch.id) it.copy(state = BranchState.BLOSSOM, progress = 0f) else it
        }
        val nextStep = if (s.guideStep == 0) 1 else s.guideStep
        val nextWhisper = if (s.guideStep == 0) {
          "Choisis maintenant un emplacement au pied du tronc pour semer tes graines."
        } else s.whisper
        s.copy(
          branches = newBranches,
          seeds = s.seeds + 2,
          guideStep = nextStep,
          whisper = nextWhisper,
          floatingTexts = s.floatingTexts + FloatingText(branch.x, branch.y - 10f, "+2 graines", Color(0xFFFFE28A))
        ).also { saveGame(it) }
      }

      RobotActionType.PLANT -> {
        val spot = s.spots.firstOrNull { it.id == act.targetId } ?: return s
        if (spot.flower != null || s.seeds <= 0) return s
        val sp = act.species ?: Species.AMBRE
        SoftSoundPlayer.plant()
        val newSpots = s.spots.map {
          if (it.id == spot.id) {
            it.copy(
              flower = FlowerInstance(
                species = sp,
                bloom = 0.05f,
                swayOffset = Random.nextFloat() * (PI.toFloat() * 2f)
              )
            )
          } else it
        }
        val nextStep = if (s.guideStep == 1) 2 else s.guideStep
        val nextWhisper = if (s.guideStep == 1) {
          "Patiente un instant… Quand la fleur s'ouvre, regarde où s'envole l'abeille."
        } else s.whisper

        val updated = s.copy(
          spots = newSpots,
          seeds = s.seeds - 1,
          guideStep = nextStep,
          whisper = nextWhisper,
          floatingTexts = s.floatingTexts + FloatingText(spot.x, spot.y - 18f, sp.displayName, Color(0xFFD8F5C4))
        )
        checkAutoUnlocks(updated).also { saveGame(it) }
      }

      RobotActionType.CLEAR_FERNS -> {
        SoftSoundPlayer.discover()
        val newSpots = s.spots.map {
          if (it.zone == SpotZone.MARE || it.zone == SpotZone.EAU) it.copy(unlocked = true) else it
        }
        var next = s.copy(
          fernsCleared = true,
          fernsHinted = true,
          spots = newSpots,
          guideStep = if (s.guideStep <= 3) 3 else s.guideStep,
          whisper = "Tu as découvert la mare ! Fleuris la berge ou récolte les fruits mûrs sous l'arbre.",
          floatingTexts = s.floatingTexts + FloatingText(680f, 420f, "La Mare découverte !", Color(0xFFB8F2FF))
        )
        next = unlockSpecies(next, Species.IRIS)
        next = unlockSpecies(next, Species.NENUPHAR)
        saveGame(next)
        next
      }

      RobotActionType.REPAIR_BRIDGE -> {
        SoftSoundPlayer.repair()
        val newSpots = s.spots.map {
          if (it.zone == SpotZone.RIVE) it.copy(unlocked = true) else it
        }
        s.copy(
          bridgeRepaired = true,
          spots = newSpots,
          guideStep = 4,
          whisper = "La passerelle mène à l'autre rive ! Approche les plantes sauvages pour les ajouter à ton carnet.",
          floatingTexts = s.floatingTexts + FloatingText(1065f, 420f, "Passerelle réparée !", Color(0xFFFFE8A6))
        ).also { saveGame(it) }
      }

      RobotActionType.COLLECT_WILD -> {
        val wp = s.wildPlants.firstOrNull { it.id == act.targetId } ?: return s
        if (wp.collected) return s
        SoftSoundPlayer.discover()
        val newWild = s.wildPlants.map { if (it.id == wp.id) it.copy(collected = true) else it }
        var next = s.copy(
          wildPlants = newWild,
          seeds = s.seeds + 2,
          whisper = "${wp.species.displayName} rejoint ton carnet ! Tu peux maintenant la semer partout dans la clairière.",
          floatingTexts = s.floatingTexts + FloatingText(
            wp.x, wp.y - 20f, "${wp.species.displayName} ajoutée !", Color(0xFFFFF1B8)
          )
        )
        next = unlockSpecies(next, wp.species)
        saveGame(next)
        next
      }
    }
  }

  private fun unlockSpecies(s: GameState, sp: Species): GameState {
    if (sp in s.discoveredSpecies) return s
    return s.copy(
      discoveredSpecies = s.discoveredSpecies + sp,
      carnetBadge = true,
      selectedSpecies = sp
    )
  }

  private fun checkAutoUnlocks(s: GameState): GameState {
    val flowers = s.flowerCount
    val newSpots = s.spots.map { spot ->
      if (spot.zone == SpotZone.CLAIRIERE && !spot.unlocked && spot.revealAt > 0 && flowers >= spot.revealAt) {
        spot.copy(unlocked = true)
      } else spot
    }
    var next = s.copy(spots = newSpots)
    if (flowers >= 2 && Species.TREFLE !in next.discoveredSpecies) {
      next = unlockSpecies(next, Species.TREFLE).copy(
        whisper = "Le trèfle rose s'invite dans l'herbe ! Consulte ton Carnet ou choisis ta graine en bas."
      )
    }
    return next
  }

  private fun updateSimulation(s: GameState, dt: Float): GameState {
    val newTime = s.time + dt

    // 1. Saisons
    var seasonProg = s.seasonProgress + dt / s.seasonDuration
    var seasonIdx = s.seasonIndex
    var dayCount = s.dayCount
    var whisper = s.whisper
    if (seasonProg >= 1f) {
      seasonProg -= 1f
      seasonIdx = (seasonIdx + 1) % Season.entries.size
      dayCount += 3
      whisper = "La forêt entre doucement en ${Season.entries[seasonIdx].displayName.lowercase()}…"
    }

    // Blessings boost
    val danceLevel = s.blessings.firstOrNull { it.id == "dance" }?.level ?: 1
    val bloomBoost = 1f + (danceLevel - 1) * 0.15f

    // 2. Croissance des fleurs et des fruits
    var newlyBloomed = false
    var irisCount = 0
    var mentheCount = 0
    val newSpots = s.spots.map { spot ->
      val fl = spot.flower ?: return@map spot
      if (fl.species == Species.IRIS) irisCount++
      if (fl.species == Species.MENTHE) mentheCount++
      if (fl.bloom < 1f) {
        val nextBloom = (fl.bloom + dt * 0.42f * bloomBoost).coerceAtMost(1f)
        if (nextBloom >= 1f) {
          newlyBloomed = true
          SoftSoundPlayer.bloom()
        }
        spot.copy(flower = fl.copy(bloom = nextBloom))
      } else spot
    }

    if (newlyBloomed && s.guideStep == 2 && !s.fernsHinted) {
      whisper = "Une abeille vient butiner ! Suis son trajet jusqu'aux fougères…"
    }

    val fruitSpeedBoost = 1f + irisCount * 0.16f + mentheCount * 0.22f
    val newFloating = s.floatingTexts.mapNotNull { ft ->
      val nl = ft.life - dt
      if (nl <= 0f) null else ft.copy(life = nl, y = ft.y - 24f * dt)
    }.toMutableList()

    val newBranches = s.branches.map { b ->
      if (b.state == BranchState.GROWING) {
        val np = b.progress + dt * 0.085f * fruitSpeedBoost
        if (np >= 1f) {
          newFloating.add(FloatingText(b.x, b.y - 8f, "Fruit mûr !", Color(0xFFFFD670)))
          b.copy(progress = 1f, state = BranchState.RIPE)
        } else {
          b.copy(progress = np)
        }
      } else b
    }

    // 3. Robot déplacement & file d'actions
    var robot = s.robot
    var nextBlink = robot.blinkTimer - dt
    if (nextBlink <= -0.15f) nextBlink = 2.4f + Random.nextFloat() * 2.5f
    robot = robot.copy(blinkTimer = nextBlink)

    var pendingFinishedAction: RobotAction? = null
    if (robot.action != null) {
      val act = robot.action!!
      val nt = act.timer + dt
      if (nt >= act.duration) {
        pendingFinishedAction = act
        robot = robot.copy(action = null)
      } else {
        robot = robot.copy(action = act.copy(timer = nt))
      }
    } else if (robot.queue.isNotEmpty()) {
      val nextWp = robot.queue.first()
      val dx = nextWp.x - robot.x
      val dy = nextWp.y - robot.y
      val dist = hypot(dx, dy)
      if (dist > 4f) {
        val speed = 215f
        robot = robot.copy(
          x = robot.x + (dx / dist) * speed * dt,
          y = robot.y + (dy / dist) * speed * dt,
          bob = robot.bob + dt * 14f,
          facing = if (abs(dx) > 2f) (if (dx >= 0f) 1 else -1) else robot.facing
        )
      } else {
        val remaining = robot.queue.drop(1)
        robot = robot.copy(
          x = nextWp.x,
          y = nextWp.y,
          queue = remaining,
          action = nextWp.action?.copy(timer = 0f)
        )
      }
    } else if (robot.bob != 0f) {
      robot = robot.copy(bob = 0f)
    }

    // 4. Abeilles & trajectoires
    val bloomedSpots = newSpots.filter { it.flower != null && it.flower.bloom >= 1f }
    val desiredBees = (3 + (bloomedSpots.size * 0.65f).toInt()).coerceIn(3, 16)
    val beesList = s.bees.toMutableList()
    while (beesList.size < desiredBees) {
      beesList.add(createBee(380f + (Random.nextFloat() - 0.5f) * 80f, 210f, beesList.size))
    }

    var pollinations = s.pollinations
    var fernsHinted = s.fernsHinted
    var honeyReserve = (s.honeyReserve + dt * 0.004f * beesList.size).coerceAtMost(1f)
    var royalPollen = s.royalPollen

    val mutableBranches = newBranches.toMutableList()
    val updatedBees = beesList.mapIndexed { idx, rawBee ->
      var bee = rawBee.copy(
        wingPhase = rawBee.wingPhase + dt * 38f,
        timer = rawBee.timer - dt
      )

      // Trail
      val trail = bee.trail.mapNotNull { pt ->
        val nl = pt.life - dt * 0.72f
        if (nl <= 0f) null else pt.copy(life = nl)
      }.toMutableList()
      if (Random.nextFloat() < 0.55f) {
        trail.add(BeeTrailPoint(bee.x, bee.y, 1f))
        if (trail.size > 24) trail.removeAt(0)
      }
      bee = bee.copy(trail = trail)

      when (bee.state) {
        BeeState.ORBIT_TREE -> {
          val na = bee.angle + dt * (1.2f + (idx % 3) * 0.25f)
          val ox = 385f + cos(na) * (105f + (idx % 3) * 28f)
          val oy = 220f + sin(na * 1.3f) * 52f
          bee = steerBee(bee.copy(angle = na), ox, oy, 145f, dt, newTime).first
          if (bloomedSpots.isNotEmpty() && bee.timer <= 0f) {
            val target = bloomedSpots[Random.nextInt(bloomedSpots.size)]
            bee = bee.copy(targetSpotId = target.id, state = BeeState.TO_FLOWER, timer = 6f)
          }
        }

        BeeState.TO_FLOWER -> {
          val target = newSpots.firstOrNull { it.id == bee.targetSpotId && it.flower != null }
          if (target == null) {
            bee = bee.copy(state = BeeState.ORBIT_TREE)
          } else {
            val (steered, dist) = steerBee(bee, target.x, target.y - 24f, 165f, dt, newTime)
            bee = steered
            if (dist < 12f) {
              bee = bee.copy(state = BeeState.POLLINATE, timer = 1.3f, hasPollen = true)
            }
          }
        }

        BeeState.POLLINATE -> {
          val target = newSpots.firstOrNull { it.id == bee.targetSpotId }
          if (target == null) {
            bee = bee.copy(state = BeeState.ORBIT_TREE)
          } else {
            val tx = target.x + sin(newTime * 6f + idx) * 6f
            val ty = target.y - 22f + cos(newTime * 5f + idx) * 4f
            bee = bee.copy(
              x = bee.x + (tx - bee.x) * 8f * dt,
              y = bee.y + (ty - bee.y) * 8f * dt
            )
            if (bee.timer <= 0f) {
              pollinations++
              royalPollen += 1
              honeyReserve = (honeyReserve + 0.03f).coerceAtMost(1f)
              if (!s.fernsCleared && !fernsHinted) {
                fernsHinted = true
                bee = bee.copy(state = BeeState.TO_FERN, timer = 4.2f)
                whisper = "Suis l'abeille du regard jusqu'aux fougères… Touche les fougères pour ouvrir le passage !"
              } else if (!s.fernsCleared && Random.nextFloat() < 0.32f) {
                bee = bee.copy(state = BeeState.TO_FERN, timer = 3.2f)
              } else if (Random.nextFloat() < 0.25f) {
                val emptySpots = newSpots.filter {
                  it.unlocked && it.flower == null && (it.zone != SpotZone.RIVE || s.bridgeRepaired)
                }
                if (emptySpots.isNotEmpty()) {
                  val t = emptySpots[Random.nextInt(emptySpots.size)]
                  bee = bee.copy(targetSpotId = t.id, state = BeeState.SCOUT, timer = 2.6f)
                } else {
                  bee = sendBeeToTree(bee, mutableBranches)
                }
              } else {
                bee = sendBeeToTree(bee, mutableBranches)
              }
            }
          }
        }

        BeeState.TO_FERN -> {
          val fx = 665f + sin(newTime * 2f + idx) * 20f
          val fy = 445f + cos(newTime * 2.5f + idx) * 22f
          bee = steerBee(bee, fx, fy, 155f, dt, newTime).first
          if (bee.timer <= 0f || s.fernsCleared) {
            bee = sendBeeToTree(bee, mutableBranches)
          }
        }

        BeeState.SCOUT -> {
          val target = newSpots.firstOrNull { it.id == bee.targetSpotId && it.flower == null }
          if (target == null || bee.timer <= 0f) {
            bee = sendBeeToTree(bee, mutableBranches)
          } else {
            bee = steerBee(bee, target.x, target.y - 26f, 150f, dt, newTime).first
          }
        }

        BeeState.TO_TREE -> {
          val bIdx = mutableBranches.indexOfFirst { it.id == bee.targetBranchId }
          if (bIdx < 0) {
            bee = bee.copy(state = BeeState.ORBIT_TREE)
          } else {
            val b = mutableBranches[bIdx]
            val (steered, dist) = steerBee(bee, b.x, b.y - 10f, 170f, dt, newTime)
            bee = steered
            if (dist < 14f) {
              if (bee.hasPollen && b.state == BranchState.BLOSSOM) {
                mutableBranches[bIdx] = b.copy(state = BranchState.GROWING, progress = 0.1f)
                newFloating.add(FloatingText(b.x, b.y - 15f, "Nouveau fruit", Color(0xFFFFE8A3)))
              } else if (bee.hasPollen && b.state == BranchState.GROWING) {
                mutableBranches[bIdx] = b.copy(progress = (b.progress + 0.25f).coerceAtMost(1f))
              }
              bee = bee.copy(
                hasPollen = false,
                state = BeeState.ORBIT_TREE,
                timer = 2.5f + Random.nextFloat() * 3.5f
              )
            }
          }
        }
      }
      bee
    }

    // 5. Particules d'ambiance saisonnières
    val pType = Season.entries[seasonIdx].particleType
    val newParticles = s.ambientParticles.map { p ->
      val np = p.phase + dt * 1.8f
      var nx = if (pType == ParticleType.FIREFLY) {
        p.x + cos(np) * 14f * dt
      } else {
        p.x + (p.vx + sin(np) * 18f) * dt
      }
      var ny = if (pType == ParticleType.FIREFLY) {
        p.y + sin(np * 1.3f) * 12f * dt
      } else {
        p.y + p.vy * dt
      }
      if (ny > WORLD_H + 10f) {
        ny = -10f
        nx = Random.nextFloat() * WORLD_W
      }
      if (nx < -20f) nx = WORLD_W + 20f
      if (nx > WORLD_W + 20f) nx = -20f
      p.copy(x = nx, y = ny, phase = np)
    }

    // 6. Vols de butinage de la Ruche
    val newFlights = s.flights.map { fl ->
      if (fl.active && !fl.ready) {
        val np = fl.progress + dt * 0.025f
        if (np >= 1f) fl.copy(progress = 1f, ready = true, active = false)
        else fl.copy(progress = np)
      } else fl
    }

    val updated = s.copy(
      time = newTime,
      dayCount = dayCount,
      seasonIndex = seasonIdx,
      seasonProgress = seasonProg,
      fernsHinted = fernsHinted,
      pollinations = pollinations,
      honeyReserve = honeyReserve,
      royalPollen = royalPollen,
      whisper = whisper,
      robot = robot,
      spots = newSpots,
      branches = mutableBranches,
      bees = updatedBees,
      floatingTexts = newFloating,
      ambientParticles = newParticles,
      flights = newFlights
    )

    return if (pendingFinishedAction != null) {
      completeRobotAction(updated, pendingFinishedAction)
    } else updated
  }

  private fun sendBeeToTree(bee: Bee, branches: List<TreeBranch>): Bee {
    val candidates = branches.filter { it.state == BranchState.BLOSSOM || it.state == BranchState.GROWING }
    return if (candidates.isNotEmpty()) {
      val b = candidates[Random.nextInt(candidates.size)]
      bee.copy(targetBranchId = b.id, state = BeeState.TO_TREE)
    } else {
      bee.copy(state = BeeState.ORBIT_TREE, timer = 2.5f + Random.nextFloat() * 2.5f)
    }
  }

  private fun steerBee(bee: Bee, tx: Float, ty: Float, speed: Float, dt: Float, time: Float): Pair<Bee, Float> {
    val dx = tx - bee.x
    val dy = ty - bee.y
    val dist = hypot(dx, dy)
    if (dist > 1f) {
      val wave = sin(time * 7f + bee.id) * 22f
      val nx = dx / dist
      val ny = dy / dist
      val vx = nx * speed - ny * wave
      val vy = ny * speed + nx * wave
      return bee.copy(
        vx = vx,
        vy = vy,
        x = bee.x + vx * dt,
        y = bee.y + vy * dt
      ) to dist
    }
    return bee to dist
  }

  private fun createBee(x: Float, y: Float, id: Int): Bee {
    return Bee(
      id = id,
      x = x,
      y = y,
      angle = Random.nextFloat() * (PI.toFloat() * 2f),
      wingPhase = Random.nextFloat() * 10f
    )
  }

  private fun createInitialState(): GameState {
    val spots = listOf(
      PlantingSpot("s1", 315f, 505f, SpotZone.CLAIRIERE, unlocked = true),
      PlantingSpot("s2", 390f, 538f, SpotZone.CLAIRIERE, unlocked = true),
      PlantingSpot("s3", 475f, 515f, SpotZone.CLAIRIERE, unlocked = true),
      PlantingSpot("s4", 255f, 470f, SpotZone.CLAIRIERE, unlocked = true),
      PlantingSpot("s5", 205f, 535f, SpotZone.CLAIRIERE, unlocked = false, revealAt = 1),
      PlantingSpot("s6", 335f, 595f, SpotZone.CLAIRIERE, unlocked = false, revealAt = 1),
      PlantingSpot("s7", 455f, 592f, SpotZone.CLAIRIERE, unlocked = false, revealAt = 2),
      PlantingSpot("s8", 548f, 545f, SpotZone.CLAIRIERE, unlocked = false, revealAt = 2),
      PlantingSpot("s9", 175f, 425f, SpotZone.CLAIRIERE, unlocked = false, revealAt = 3),
      PlantingSpot("s10", 560f, 440f, SpotZone.CLAIRIERE, unlocked = false, revealAt = 3),
      PlantingSpot("s11", 265f, 635f, SpotZone.CLAIRIERE, unlocked = false, revealAt = 4),
      PlantingSpot("s12", 525f, 642f, SpotZone.CLAIRIERE, unlocked = false, revealAt = 4),
      PlantingSpot("m1", 750f, 430f, SpotZone.MARE),
      PlantingSpot("m2", 745f, 525f, SpotZone.MARE),
      PlantingSpot("m3", 805f, 598f, SpotZone.MARE),
      PlantingSpot("m4", 910f, 618f, SpotZone.MARE),
      PlantingSpot("m5", 830f, 345f, SpotZone.MARE),
      PlantingSpot("m6", 935f, 340f, SpotZone.MARE),
      PlantingSpot("w1", 855f, 455f, SpotZone.EAU),
      PlantingSpot("w2", 925f, 510f, SpotZone.EAU),
      PlantingSpot("r1", 1175f, 410f, SpotZone.RIVE),
      PlantingSpot("r2", 1215f, 505f, SpotZone.RIVE),
      PlantingSpot("r3", 1285f, 445f, SpotZone.RIVE),
      PlantingSpot("r4", 1190f, 595f, SpotZone.RIVE),
      PlantingSpot("r5", 1295f, 565f, SpotZone.RIVE)
    )

    val branches = listOf(
      TreeBranch("b1", 295f, 300f, BranchState.RIPE, 1f, true),
      TreeBranch("b2", 468f, 295f, BranchState.RIPE, 1f, true),
      TreeBranch("b3", 350f, 225f, BranchState.GROWING, 0.55f, false),
      TreeBranch("b4", 430f, 205f, BranchState.BLOSSOM, 0f, false),
      TreeBranch("b5", 265f, 235f, BranchState.BLOSSOM, 0f, false),
      TreeBranch("b6", 500f, 245f, BranchState.BLOSSOM, 0f, false),
      TreeBranch("b7", 388f, 155f, BranchState.BLOSSOM, 0f, false)
    )

    val wildPlants = listOf(
      WildPlant("wp_menthe", Species.MENTHE, 1210f, 345f),
      WildPlant("wp_campanule", Species.CAMPANULE, 1300f, 380f),
      WildPlant("wp_bruyere", Species.BRUYERE, 1255f, 635f)
    )

    val bees = (0 until 3).map { i -> createBee(380f + (i - 1) * 45f, 220f + i * 20f, i) }
    val particles = (0 until 36).map {
      AmbientParticle(
        x = Random.nextFloat() * WORLD_W,
        y = Random.nextFloat() * WORLD_H,
        vx = (Random.nextFloat() - 0.5f) * 18f,
        vy = 10f + Random.nextFloat() * 18f,
        size = 2.5f + Random.nextFloat() * 3.5f,
        phase = Random.nextFloat() * (PI.toFloat() * 2f)
      )
    }

    val flights = listOf(
      ForagingFlight("f1", "Vers le Bosquet de Trèfles", "Nectar de Trèfle + 1 graine", 0.65f, ready = false, active = true),
      ForagingFlight("f2", "Autour de la Mare aux Iris", "De retour chargé de nectar floral !", 1f, ready = true, active = false),
      ForagingFlight("f3", "L'Autre Rive fleurie", "Emplacement libre pour explorer", 0f, ready = false, active = false)
    )

    val blessings = listOf(
      HiveBlessing("dance", "Danse des Fleurs", "Accélère la floraison des boutons d'ambre (+15%).", 1, 5, 25),
      HiveBlessing("wax", "Cire parfumée", "Protège les jeunes pousses de la fraîche rosée.", 1, 5, 30),
      HiveBlessing("light", "Nectar de Lumière", "Illumine les sous-bois à la tombée du soir.", 0, 3, 45)
    )

    return GameState(
      spots = spots,
      branches = branches,
      wildPlants = wildPlants,
      bees = bees,
      ambientParticles = particles,
      flights = flights,
      blessings = blessings
    )
  }

  private fun saveGame(s: GameState) {
    try {
      val root = JSONObject().apply {
        put("dayCount", s.dayCount)
        put("seasonIndex", s.seasonIndex)
        put("seasonProgress", s.seasonProgress.toDouble())
        put("seeds", s.seeds)
        put("nectar", s.nectar)
        put("royalPollen", s.royalPollen)
        put("honeyReserve", s.honeyReserve.toDouble())
        put("selectedSpecies", s.selectedSpecies.id)
        put("discoveredSpecies", JSONArray(s.discoveredSpecies.map { it.id }))
        put("fernsCleared", s.fernsCleared)
        put("fernsHinted", s.fernsHinted)
        put("bridgeRepaired", s.bridgeRepaired)
        put("pollinations", s.pollinations)
        put("guideStep", s.guideStep)
        put("soundEnabled", s.soundEnabled)
        put("beeCount", s.bees.size)

        val spotsArr = JSONArray()
        s.spots.forEach { sp ->
          spotsArr.put(JSONObject().apply {
            put("id", sp.id)
            put("unlocked", sp.unlocked)
            if (sp.flower != null) {
              put("flowerSpecies", sp.flower.species.id)
              put("flowerBloom", sp.flower.bloom.toDouble())
            }
          })
        }
        put("spots", spotsArr)

        val branchesArr = JSONArray()
        s.branches.forEach { b ->
          branchesArr.put(JSONObject().apply {
            put("id", b.id)
            put("state", b.state.name)
            put("progress", b.progress.toDouble())
          })
        }
        put("branches", branchesArr)

        val wildArr = JSONArray()
        s.wildPlants.forEach { w ->
          wildArr.put(JSONObject().apply {
            put("id", w.id)
            put("collected", w.collected)
          })
        }
        put("wildPlants", wildArr)
      }
      prefs.edit().putString(saveKey, root.toString()).apply()
    } catch (_: Throwable) {
    }
  }

  private fun loadOrCreateState(): GameState {
    val base = createInitialState()
    val raw = prefs.getString(saveKey, null) ?: return base
    return try {
      val root = JSONObject(raw)
      val discArr = root.optJSONArray("discoveredSpecies")
      val discovered = mutableListOf<Species>()
      if (discArr != null) {
        for (i in 0 until discArr.length()) {
          discovered.add(Species.fromId(discArr.getString(i)))
        }
      }
      if (discovered.isEmpty()) discovered.add(Species.AMBRE)

      val spotsMap = mutableMapOf<String, Pair<Boolean, FlowerInstance?>>()
      val spotsArr = root.optJSONArray("spots")
      if (spotsArr != null) {
        for (i in 0 until spotsArr.length()) {
          val obj = spotsArr.getJSONObject(i)
          val id = obj.getString("id")
          val unlocked = obj.optBoolean("unlocked", false)
          val fl = if (obj.has("flowerSpecies")) {
            FlowerInstance(
              species = Species.fromId(obj.getString("flowerSpecies")),
              bloom = obj.optDouble("flowerBloom", 1.0).toFloat()
            )
          } else null
          spotsMap[id] = unlocked to fl
        }
      }

      val branchesMap = mutableMapOf<String, Pair<BranchState, Float>>()
      val bArr = root.optJSONArray("branches")
      if (bArr != null) {
        for (i in 0 until bArr.length()) {
          val obj = bArr.getJSONObject(i)
          branchesMap[obj.getString("id")] =
            BranchState.valueOf(obj.getString("state")) to obj.optDouble("progress", 0.0).toFloat()
        }
      }

      val wildMap = mutableMapOf<String, Boolean>()
      val wArr = root.optJSONArray("wildPlants")
      if (wArr != null) {
        for (i in 0 until wArr.length()) {
          val obj = wArr.getJSONObject(i)
          wildMap[obj.getString("id")] = obj.optBoolean("collected", false)
        }
      }

      val fernsCleared = root.optBoolean("fernsCleared", false)
      val bridgeRepaired = root.optBoolean("bridgeRepaired", false)
      val guideStep = root.optInt("guideStep", 0)
      val flowerCount = spotsMap.values.count { it.second != null }

      val whisper = when {
        guideStep == 0 && flowerCount == 0 -> "Cueille un fruit ambré sur une branche basse de l'arbre."
        !fernsCleared -> "Fais éclore des fleurs et observe les trajets des abeilles vers les fougères."
        !bridgeRepaired -> "Fleuris la berge de la mare ou répare la passerelle pour atteindre l'autre rive."
        else -> "La clairière vit à ton rythme au fil des saisons."
      }

      val targetBees = root.optInt("beeCount", 3).coerceIn(3, 16)
      val bees = base.bees.toMutableList()
      while (bees.size < targetBees) {
        bees.add(createBee(380f, 210f, bees.size))
      }

      base.copy(
        dayCount = root.optInt("dayCount", 1),
        seasonIndex = root.optInt("seasonIndex", 0),
        seasonProgress = root.optDouble("seasonProgress", 0.0).toFloat(),
        seeds = root.optInt("seeds", 0),
        nectar = root.optInt("nectar", 40),
        royalPollen = root.optInt("royalPollen", 15),
        honeyReserve = root.optDouble("honeyReserve", 0.35).toFloat(),
        selectedSpecies = Species.fromId(root.optString("selectedSpecies", "ambre")),
        discoveredSpecies = discovered,
        fernsCleared = fernsCleared,
        fernsHinted = root.optBoolean("fernsHinted", false),
        bridgeRepaired = bridgeRepaired,
        pollinations = root.optInt("pollinations", 0),
        guideStep = guideStep,
        soundEnabled = root.optBoolean("soundEnabled", true),
        whisper = whisper,
        bees = bees,
        spots = base.spots.map { sp ->
          spotsMap[sp.id]?.let { (u, fl) -> sp.copy(unlocked = u, flower = fl) } ?: sp
        },
        branches = base.branches.map { b ->
          branchesMap[b.id]?.let { (st, pr) -> b.copy(state = st, progress = pr) } ?: b
        },
        wildPlants = base.wildPlants.map { w ->
          wildMap[w.id]?.let { col -> w.copy(collected = col) } ?: w
        }
      )
    } catch (_: Throwable) {
      base
    }
  }
}
