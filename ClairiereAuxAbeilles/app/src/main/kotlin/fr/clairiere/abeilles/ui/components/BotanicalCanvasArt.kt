package fr.clairiere.abeilles.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import fr.clairiere.abeilles.game.BranchState
import fr.clairiere.abeilles.game.GameState
import fr.clairiere.abeilles.game.ParticleType
import fr.clairiere.abeilles.game.PlantingSpot
import fr.clairiere.abeilles.game.Species
import fr.clairiere.abeilles.game.SpotZone
import fr.clairiere.abeilles.game.WORLD_H
import fr.clairiere.abeilles.game.WORLD_W
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FlowerIllustration(species: Species, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val cx = size.width / 2f
    val cy = size.height * 0.42f
    val r = size.minDimension * 0.14f

    // Stem
    drawLine(
      color = Color(0xFF4F7E43),
      start = Offset(cx, size.height * 0.88f),
      end = Offset(cx, cy),
      strokeWidth = size.minDimension * 0.08f,
      cap = StrokeCap.Round
    )
    // Leaves
    drawOval(
      color = Color(0xFF6B9E59),
      topLeft = Offset(cx - r * 1.6f, size.height * 0.62f),
      size = Size(r * 1.4f, r * 0.7f)
    )
    drawOval(
      color = Color(0xFF6B9E59),
      topLeft = Offset(cx + r * 0.2f, size.height * 0.56f),
      size = Size(r * 1.4f, r * 0.7f)
    )
    // Petals
    for (i in 0 until species.petals) {
      val a = (i * 2.0 * PI) / species.petals
      val px = cx + (cos(a) * r * 1.35f).toFloat()
      val py = cy + (sin(a) * r * 1.35f).toFloat()
      drawCircle(color = species.petalColor, radius = r * 0.88f, center = Offset(px, py))
    }
    // Center
    drawCircle(color = species.centerColor, radius = r * 0.78f, center = Offset(cx, cy))
  }
}

fun DrawScope.drawGameWorld(
  state: GameState,
  scale: Float,
  offsetX: Float,
  offsetY: Float
) {
  val season = state.currentSeason
  drawRect(color = season.bgTree)

  withTransform({
    translate(offsetX, offsetY)
    scale(scale, scale, Offset.Zero)
  }) {
    clipRect(0f, 0f, WORLD_W, WORLD_H) {
      drawSkyAndForestBackdrop(state)
      drawMeadowAndPaths(state)
      drawPondAndStream(state)
      drawBridge(state)
      drawAmbientDecorByProgress(state)
      drawPlantingSpots(state)
      drawWildPlants(state)
      drawGreatTree(state)
      if (!state.fernsCleared) drawFernsBarrier(state)
      drawRobot(state)
      drawBees(state)
      drawSeasonParticles(state)
      drawFloatingTexts(state)
    }
  }
}

private fun DrawScope.drawSkyAndForestBackdrop(state: GameState) {
  val season = state.currentSeason
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(season.skyTop, season.skyBottom),
      startY = 0f,
      endY = 420f
    ),
    topLeft = Offset.Zero,
    size = Size(WORLD_W, 420f)
  )

  // Soft sunbeams
  val beamAlpha = if (season.id == "summer") 0.16f else 0.09f
  for (i in 0 until 4) {
    val bx = 180f + i * 310f
    val path = Path().apply {
      moveTo(bx, 0f)
      lineTo(bx + 120f, 0f)
      lineTo(bx + 260f, 520f)
      lineTo(bx + 60f, 520f)
      close()
    }
    drawPath(path, color = Color(0xFFFFF4CF).copy(alpha = beamAlpha))
  }

  // Distant tree silhouettes
  val bgTrunks = listOf(70f, 155f, 250f, 585f, 675f, 790f, 960f, 1140f, 1265f, 1345f)
  bgTrunks.forEachIndexed { idx, tx ->
    val h = 230f + (idx % 3) * 45f
    drawRect(
      color = season.bgTree.copy(alpha = 0.42f),
      topLeft = Offset(tx - 11f, 385f - h),
      size = Size(22f, h + 15f)
    )
    drawCircle(
      color = season.bgTree.copy(alpha = 0.42f),
      radius = 62f + (idx % 2) * 18f,
      center = Offset(tx, 385f - h)
    )
  }
}

private fun DrawScope.drawMeadowAndPaths(state: GameState) {
  val season = state.currentSeason
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(season.grass1, season.grass2),
      startY = 350f,
      endY = WORLD_H
    ),
    topLeft = Offset(0f, 360f),
    size = Size(WORLD_W, WORLD_H - 360f)
  )

  // Soft earth under the great tree
  drawOval(
    color = Color(0x3D866C48),
    topLeft = Offset(385f - 215f, 515f - 105f),
    size = Size(430f, 210f)
  )

  if (state.fernsCleared) {
    drawOval(
      color = Color(0x73B69B72),
      topLeft = Offset(675f - 95f, 458f - 34f),
      size = Size(190f, 68f)
    )
    listOf(625f, 665f, 705f).forEachIndexed { i, sx ->
      drawOval(
        color = Color(0xFFB7AD9A),
        topLeft = Offset(sx - 14f, 456f + (i % 2) * 7f - 9f),
        size = Size(28f, 18f)
      )
    }
  }

  // Other bank
  drawOval(
    color = Color(0x0FFFFFFF),
    topLeft = Offset(1255f - 145f, 500f - 185f),
    size = Size(290f, 370f)
  )
}

private fun DrawScope.drawPondAndStream(state: GameState) {
  val season = state.currentSeason
  val dimAlpha = if (!state.fernsCleared) 0.55f else 1f

  // Sandy bank
  drawOval(
    color = Color(0xFF8A8267).copy(alpha = dimAlpha),
    topLeft = Offset(895f - 148f, 480f - 128f),
    size = Size(296f, 256f)
  )

  // Stream
  val streamPath = Path().apply {
    moveTo(995f, 355f)
    quadraticTo(1065f, 440f, 1045f, 690f)
    lineTo(1110f, 690f)
    quadraticTo(1120f, 440f, 1045f, 355f)
    close()
  }
  drawPath(streamPath, color = season.water2.copy(alpha = dimAlpha))

  // Main pond
  drawOval(
    brush = Brush.radialGradient(
      colors = listOf(season.water1.copy(alpha = dimAlpha), season.water2.copy(alpha = dimAlpha)),
      center = Offset(875f, 460f),
      radius = 140f
    ),
    topLeft = Offset(895f - 134f, 480f - 114f),
    size = Size(268f, 228f)
  )

  // Animated ripples
  for (i in 0 until 3) {
    val r = ((state.time * 16f + i * 26f) % 75f) + 10f
    val a = (if (!state.fernsCleared) 0.2f else 0.35f) * (1f - r / 90f)
    drawOval(
      color = Color.White.copy(alpha = a.coerceIn(0f, 1f)),
      topLeft = Offset(885f - r * 1.15f, 482f - r * 0.72f),
      size = Size(r * 2.3f, r * 1.44f),
      style = Stroke(width = 2f)
    )
  }

  // Reeds
  val reeds = listOf(785f to 395f, 795f to 402f, 970f to 560f, 982f to 552f)
  reeds.forEachIndexed { idx, (rx, ry) ->
    val sway = sin(state.time * 2f + idx) * 4f
    val p = Path().apply {
      moveTo(rx, ry + 15f)
      quadraticTo(rx + sway, ry - 15f, rx + sway * 1.4f, ry - 36f)
    }
    drawPath(p, color = Color(0xFF4D6B3C).copy(alpha = dimAlpha), style = Stroke(width = 3f))
  }

  if (!state.fernsCleared) {
    drawRect(
      color = Color(0x6B1F2D22),
      topLeft = Offset(720f, 330f),
      size = Size(680f, 490f)
    )
  }
}

private fun DrawScope.drawBridge(state: GameState) {
  val bx = 1005f
  val by = 435f
  val alpha = if (!state.fernsCleared) 0.45f else 1f

  drawRect(Color(0xFF6E4E32).copy(alpha = alpha), Offset(bx, by), Size(118f, 8f))
  drawRect(Color(0xFF6E4E32).copy(alpha = alpha), Offset(bx, by + 44f), Size(118f, 8f))

  if (state.bridgeRepaired) {
    for (i in 0 until 7) {
      drawRect(
        color = if (i % 2 == 0) Color(0xFF9C7148) else Color(0xFF8C633D),
        topLeft = Offset(bx + 4f + i * 16f, by + 3f),
        size = Size(13f, 46f)
      )
    }
    drawRect(Color(0xFF593D25), Offset(bx + 4f, by - 12f), Size(7f, 16f))
    drawRect(Color(0xFF593D25), Offset(bx + 106f, by - 12f), Size(7f, 16f))
  } else {
    listOf(0, 1, 5, 6).forEach { i ->
      val cx = bx + 10f + i * 16f
      val cy = by + 25f
      rotate(degrees = if (i % 2 == 0) 8f else -10f, pivot = Offset(cx, cy)) {
        drawRect(
          color = Color(0xFF7A5738).copy(alpha = alpha),
          topLeft = Offset(cx - 6f, cy - 22f),
          size = Size(12f, 42f)
        )
      }
    }
    if (state.fernsCleared) {
      drawRect(
        color = Color(0x8CFFE8A3),
        topLeft = Offset(bx - 6f, by - 10f),
        size = Size(130f, 72f),
        style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
      )
    }
  }
}

private fun DrawScope.drawAmbientDecorByProgress(state: GameState) {
  val flowers = state.flowerCount
  val season = state.currentSeason
  if (flowers >= 3) {
    drawTinyMushroom(200f, 385f)
    drawTinyMushroom(218f, 392f)
  }
  if (flowers >= 6) {
    drawRect(Color(0xFF5C432B), Offset(142f, 575f), Size(7f, 36f))
    drawCircle(season.treeLeaf2, radius = 22f, center = Offset(145f, 565f))
  }
  if (flowers >= 10) {
    val glow = (0.25f + sin(state.time * 2.5f) * 0.1f).coerceIn(0.05f, 0.45f)
    drawCircle(Color(0xFFFFD46E).copy(alpha = glow), radius = 32f, center = Offset(385f, 445f))
  }
}

private fun DrawScope.drawTinyMushroom(x: Float, y: Float) {
  drawRect(Color(0xFFEFE6D2), Offset(x - 2f, y), Size(4f, 9f))
  drawArc(
    color = Color(0xFFD97B48),
    startAngle = 180f,
    sweepAngle = 180f,
    useCenter = true,
    topLeft = Offset(x - 8f, y - 8f),
    size = Size(16f, 16f)
  )
}

private fun DrawScope.drawGreatTree(state: GameState) {
  val season = state.currentSeason
  val tx = 385f
  val ty = 465f

  // Ground shadow
  drawOval(
    color = Color(0x3D222A1A),
    topLeft = Offset(tx - 130f, ty + 6f - 34f),
    size = Size(260f, 68f)
  )

  // Trunk & roots
  val trunkPath = Path().apply {
    moveTo(tx - 65f, ty + 10f)
    quadraticTo(tx - 32f, ty - 20f, tx - 30f, ty - 190f)
    lineTo(tx + 34f, ty - 190f)
    quadraticTo(tx + 36f, ty - 20f, tx + 72f, ty + 10f)
    quadraticTo(tx + 20f, ty - 2f, tx, ty + 6f)
    quadraticTo(tx - 25f, ty - 4f, tx - 65f, ty + 10f)
    close()
  }
  drawPath(trunkPath, color = Color(0xFF684B33))

  // Main branches
  val branchPath = Path().apply {
    moveTo(tx - 12f, ty - 170f)
    quadraticTo(tx - 75f, ty - 200f, tx - 115f, ty - 175f)
    moveTo(tx + 12f, ty - 170f)
    quadraticTo(tx + 75f, ty - 205f, tx + 115f, ty - 180f)
  }
  drawPath(branchPath, color = Color(0xFF684B33), style = Stroke(width = 18f, cap = StrokeCap.Round))

  // Foliage puffs
  val puffs = listOf(
    Triple(tx - 105f, ty - 220f, 74f to season.treeLeaf1),
    Triple(tx + 105f, ty - 225f, 76f to season.treeLeaf1),
    Triple(tx - 45f, ty - 285f, 86f to season.treeLeaf2),
    Triple(tx + 55f, ty - 280f, 82f to season.treeLeaf1),
    Triple(tx, ty - 335f, 78f to season.treeLeaf2),
    Triple(tx, ty - 235f, 92f to season.treeLeaf2)
  )
  puffs.forEachIndexed { idx, (px, py, radCol) ->
    val sway = sin(state.time * 1.2f + idx) * 2.5f
    drawCircle(color = radCol.second, radius = radCol.first, center = Offset(px + sway, py))
  }

  // Blossoms & amber fruits
  state.branches.forEach { b ->
    val sway = sin(state.time * 2f + b.x * 0.02f) * 2f
    val bx = b.x + sway
    val by = b.y

    drawLine(Color(0xFF5B412B), Offset(bx, by - 10f), Offset(bx, by + 6f), strokeWidth = 2.5f)

    if (b.state == BranchState.BLOSSOM) {
      for (p in 0 until 5) {
        val a = (p * 2.0 * PI) / 5.0
        drawCircle(
          color = Color(0xFFFFF3DC),
          radius = 4.2f,
          center = Offset(bx + (cos(a) * 6f).toFloat(), by + (sin(a) * 6f).toFloat())
        )
      }
      drawCircle(Color(0xFFF2B84B), radius = 3.2f, center = Offset(bx, by))
    } else {
      val isRipe = b.state == BranchState.RIPE
      val r = if (isRipe) 13f else 6f + b.progress * 6.5f
      if (isRipe) {
        val pulse = (0.28f + sin(state.time * 3.5f + b.x) * 0.1f).coerceIn(0.15f, 0.5f)
        drawCircle(
          color = Color(0xFFFFCE5E).copy(alpha = pulse),
          radius = r + 9f,
          center = Offset(bx, by + 10f)
        )
      }
      drawCircle(
        brush = Brush.radialGradient(
          colors = if (isRipe) listOf(Color(0xFFFFF2A6), Color(0xFFF0A02B), Color(0xFFB55D12))
          else listOf(Color(0xFFD8E37B), Color(0xFF9DC255), Color(0xFF6C9138)),
          center = Offset(bx - 3f, by + 6f),
          radius = r
        ),
        radius = r,
        center = Offset(bx, by + 10f)
      )
    }
  }
}

private fun DrawScope.drawFernsBarrier(state: GameState) {
  val fx = 665f
  val fy = 450f
  if (state.fernsHinted) {
    val pulse = (0.25f + sin(state.time * 4f) * 0.12f).coerceIn(0.1f, 0.5f)
    drawOval(
      color = Color(0xFFFFE082).copy(alpha = pulse),
      topLeft = Offset(fx - 68f, fy - 86f),
      size = Size(136f, 172f)
    )
  }

  val fronds = listOf(
    Triple(640f, 480f, -26f),
    Triple(660f, 495f, -9f),
    Triple(685f, 490f, 12f),
    Triple(705f, 475f, 28f),
    Triple(645f, 425f, -29f),
    Triple(670f, 410f, 3f),
    Triple(698f, 425f, 24f)
  )
  fronds.forEachIndexed { idx, (x, y, deg) ->
    val sway = sin(state.time * 2f + idx) * 3f
    rotate(degrees = deg + sway, pivot = Offset(x, y)) {
      drawOval(
        color = if (idx % 2 == 0) Color(0xFF436E3B) else Color(0xFF558749),
        topLeft = Offset(x - 14f, y - 74f),
        size = Size(28f, 80f)
      )
      drawLine(Color(0xFF76A866), Offset(x, y), Offset(x, y - 64f), strokeWidth = 2f)
    }
  }
}

private fun DrawScope.drawPlantingSpots(state: GameState) {
  state.spots.forEach { s ->
    if (!s.unlocked) return@forEach
    if (s.zone == SpotZone.RIVE && !state.bridgeRepaired) return@forEach

    if (s.flower == null) {
      val canPlant = state.seeds > 0
      val pulse = (if (canPlant) 0.45f + sin(state.time * 4f + s.x) * 0.2f else 0.25f).coerceIn(0.1f, 0.8f)
      if (s.zone == SpotZone.EAU) {
        drawOval(
          color = Color(0xFF74B26E).copy(alpha = pulse),
          topLeft = Offset(s.x - 17f, s.y - 11f),
          size = Size(34f, 22f)
        )
      } else {
        drawOval(
          color = Color(0x85735635),
          topLeft = Offset(s.x - 17f, s.y - 9f),
          size = Size(34f, 18f)
        )
        if (canPlant) {
          drawOval(
            color = Color(0xFFFFE282).copy(alpha = pulse),
            topLeft = Offset(s.x - 20f, s.y - 11f),
            size = Size(40f, 22f),
            style = Stroke(width = 1.8f)
          )
        }
      }
    } else {
      drawFlowerInWorld(s, state.time)
    }
  }
}

private fun DrawScope.drawFlowerInWorld(spot: PlantingSpot, time: Float) {
  val f = spot.flower ?: return
  val sp = f.species
  val scale = f.bloom
  val sway = sin(time * 2.2f + f.swayOffset) * 3f * scale
  val x = spot.x
  val y = spot.y

  if (spot.zone == SpotZone.EAU) {
    drawOval(
      color = Color(0xFF5C9E58),
      topLeft = Offset(x - 20f * scale, y + 2f - 12f * scale),
      size = Size(40f * scale, 24f * scale)
    )
  } else {
    val stem = Path().apply {
      moveTo(x, y + 2f)
      quadraticTo(x + sway * 0.4f, y - 14f * scale, x + sway, y - 25f * scale)
    }
    drawPath(stem, color = Color(0xFF4C7C41), style = Stroke(width = 3f))
    drawOval(
      color = Color(0xFF5D914E),
      topLeft = Offset(x - 14f * scale, y - 11f * scale),
      size = Size(14f * scale, 7f * scale)
    )
    drawOval(
      color = Color(0xFF5D914E),
      topLeft = Offset(x, y - 14f * scale),
      size = Size(14f * scale, 7f * scale)
    )
  }

  val hx = if (spot.zone == SpotZone.EAU) x else x + sway
  val hy = if (spot.zone == SpotZone.EAU) y - 3f * scale else y - 27f * scale

  if (sp == Species.CAMPANULE && f.bloom >= 1f) {
    drawCircle(Color(0x40AAD7FF), radius = 20f, center = Offset(hx, hy))
  }

  for (i in 0 until sp.petals) {
    val a = (i * 2.0 * PI) / sp.petals + time * 0.12
    val px = hx + (cos(a) * 7.5f * scale).toFloat()
    val py = hy + (sin(a) * 7.5f * scale).toFloat()
    drawCircle(sp.petalColor, radius = 5.4f * scale, center = Offset(px, py))
  }
  drawCircle(sp.centerColor, radius = 4.5f * scale, center = Offset(hx, hy))
}

private fun DrawScope.drawWildPlants(state: GameState) {
  if (!state.fernsCleared) return
  state.wildPlants.forEach { wp ->
    if (wp.collected) return@forEach
    val sp = wp.species
    if (state.bridgeRepaired) {
      val pulse = (0.25f + sin(state.time * 3.5f + wp.x) * 0.12f).coerceIn(0.1f, 0.5f)
      drawCircle(Color(0xFFFFF0AA).copy(alpha = pulse), radius = 24f, center = Offset(wp.x, wp.y - 14f))
    }
    drawLine(Color(0xFF487542), Offset(wp.x, wp.y + 4f), Offset(wp.x - 4f, wp.y - 22f), strokeWidth = 3.2f)
    drawLine(Color(0xFF487542), Offset(wp.x, wp.y + 4f), Offset(wp.x + 5f, wp.y - 24f), strokeWidth = 3.2f)
    for (i in 0 until 6) {
      val a = (i * 2.0 * PI) / 6.0
      drawCircle(
        color = sp.petalColor,
        radius = 5.5f,
        center = Offset(wp.x + (cos(a) * 8f).toFloat(), wp.y - 23f + (sin(a) * 8f).toFloat())
      )
    }
    drawCircle(sp.centerColor, radius = 4.5f, center = Offset(wp.x, wp.y - 23f))
  }
}

private fun DrawScope.drawRobot(state: GameState) {
  val r = state.robot
  val bounce = abs(sin(r.bob)) * 4f
  val working = if (r.action != null) sin(state.time * 18f) * 3f else 0f
  val rx = r.x
  val ry = r.y - bounce

  // Shadow
  drawOval(
    color = Color(0x42191E14),
    topLeft = Offset(rx - 18f, ry + 8f + bounce),
    size = Size(36f, 12f)
  )
  // Wheel
  drawRoundRect(
    color = Color(0xFF4A433B),
    topLeft = Offset(rx - 13f, ry + 6f),
    size = Size(26f, 9f),
    cornerRadius = CornerRadius(4f, 4f)
  )
  // Seed satchel
  drawCircle(
    color = if (state.seeds > 0) Color(0xFFE89A2C) else Color(0xFF8E7D67),
    radius = 7f,
    center = Offset(rx - 13f * r.facing, ry - 4f)
  )
  // Body
  drawRoundRect(
    color = Color(0xFFF2EAD7),
    topLeft = Offset(rx - 15f, ry - 26f),
    size = Size(30f, 33f),
    cornerRadius = CornerRadius(12f, 12f)
  )
  drawRoundRect(
    color = Color(0xFF6E5D49),
    topLeft = Offset(rx - 15f, ry - 26f),
    size = Size(30f, 33f),
    cornerRadius = CornerRadius(12f, 12f),
    style = Stroke(width = 2f)
  )
  // Copper band
  drawRect(Color(0xFFC98B49), Offset(rx - 14f, ry - 7f), Size(28f, 4f))
  // Visor
  drawRoundRect(
    color = Color(0xFF2D2A26),
    topLeft = Offset(rx - 11f + r.facing * 2f, ry - 22f),
    size = Size(22f, 12f),
    cornerRadius = CornerRadius(6f, 6f)
  )
  // Glowing eyes
  if (r.blinkTimer < 0f) {
    drawRect(Color(0xFFFFD670), Offset(rx - 7f + r.facing * 2f, ry - 16f), Size(4f, 1.8f))
    drawRect(Color(0xFFFFD670), Offset(rx + 3f + r.facing * 2f, ry - 16f), Size(4f, 1.8f))
  } else {
    drawCircle(Color(0xFFFFD670), radius = 2.5f, center = Offset(rx - 5f + r.facing * 2f, ry - 16f))
    drawCircle(Color(0xFFFFD670), radius = 2.5f, center = Offset(rx + 5f + r.facing * 2f, ry - 16f))
  }
  // Sprout antenna
  val ant = Path().apply {
    moveTo(rx, ry - 26f)
    quadraticTo(rx + working, ry - 34f, rx + 2f + working, ry - 38f)
  }
  drawPath(ant, color = Color(0xFF5F8A4E), style = Stroke(width = 2.2f))
  drawOval(
    color = Color(0xFF7CB562),
    topLeft = Offset(rx + 1f + working, ry - 41f),
    size = Size(10f, 6f)
  )
}

private fun DrawScope.drawBees(state: GameState) {
  state.bees.forEach { bee ->
    if (bee.trail.size > 1) {
      for (i in 1 until bee.trail.size) {
        val p0 = bee.trail[i - 1]
        val p1 = bee.trail[i]
        val col = if (bee.hasPollen) {
          Color(0xFFFFDE70).copy(alpha = (p1.life * 0.55f).coerceIn(0f, 1f))
        } else {
          Color(0xFFFFFAE1).copy(alpha = (p1.life * 0.3f).coerceIn(0f, 1f))
        }
        drawLine(col, Offset(p0.x, p0.y), Offset(p1.x, p1.y), strokeWidth = 2.2f)
      }
    }

    val wingY = sin(bee.wingPhase) * 3.5f
    drawOval(
      color = Color(0xD9F5FCFF),
      topLeft = Offset(bee.x - 7f, bee.y - 7f + wingY * 0.3f),
      size = Size(10f, 6.4f)
    )
    drawOval(
      color = Color(0xD9F5FCFF),
      topLeft = Offset(bee.x - 3f, bee.y - 7f - wingY * 0.3f),
      size = Size(10f, 6.4f)
    )
    drawOval(
      color = Color(0xFFF2B830),
      topLeft = Offset(bee.x - 6.2f, bee.y - 4.4f),
      size = Size(12.4f, 8.8f)
    )
    drawRect(Color(0xFF36291A), Offset(bee.x - 1.5f, bee.y - 4f), Size(2.5f, 8f))
    if (bee.hasPollen) {
      drawCircle(Color(0xFFFFF199), radius = 2.3f, center = Offset(bee.x, bee.y + 4.5f))
    }
  }
}

private fun DrawScope.drawSeasonParticles(state: GameState) {
  val type = state.currentSeason.particleType
  state.ambientParticles.forEach { p ->
    when (type) {
      ParticleType.PETAL -> {
        rotate(degrees = (p.phase * 57.3f) % 360f, pivot = Offset(p.x, p.y)) {
          drawOval(
            color = Color(0xB8FFE8EC),
            topLeft = Offset(p.x - p.size, p.y - p.size * 0.6f),
            size = Size(p.size * 2f, p.size * 1.2f)
          )
        }
      }
      ParticleType.FIREFLY -> {
        val a = (0.35f + sin(p.phase * 2f) * 0.35f).coerceIn(0f, 0.8f)
        drawCircle(Color(0xFFFFEA82).copy(alpha = a), radius = p.size * 0.8f, center = Offset(p.x, p.y))
      }
      ParticleType.LEAF -> {
        val col = if (p.phase % 2f > 1f) Color(0xC7DA7A34) else Color(0xC7E8A844)
        rotate(degrees = (p.phase * 57.3f) % 360f, pivot = Offset(p.x, p.y)) {
          drawOval(
            color = col,
            topLeft = Offset(p.x - p.size * 1.3f, p.y - p.size * 0.7f),
            size = Size(p.size * 2.6f, p.size * 1.4f)
          )
        }
      }
      ParticleType.SNOW -> {
        drawCircle(Color(0xBFFFFFFF), radius = p.size * 0.65f, center = Offset(p.x, p.y))
      }
    }
  }
}

private fun DrawScope.drawFloatingTexts(state: GameState) {
  val shadowPaint = Paint().apply {
    isAntiAlias = true
    textSize = 18f
    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    textAlign = Paint.Align.CENTER
  }
  val textPaint = Paint().apply {
    isAntiAlias = true
    textSize = 18f
    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    textAlign = Paint.Align.CENTER
  }
  state.floatingTexts.forEach { ft ->
    val alpha = ((ft.life / ft.maxLife).coerceIn(0f, 1f) * 255).toInt()
    shadowPaint.color = Color(0x8C191612).copy(alpha = (ft.life / ft.maxLife).coerceIn(0f, 1f) * 0.55f).toArgb()
    textPaint.color = ft.color.toArgb()
    textPaint.alpha = alpha
    drawContext.canvas.nativeCanvas.drawText(ft.text, ft.x + 1.5f, ft.y + 1.5f, shadowPaint)
    drawContext.canvas.nativeCanvas.drawText(ft.text, ft.x, ft.y, textPaint)
  }
}

@Composable
fun AccueilHeroIllustration(time: Float, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE7EEDB), Color(0xFFD9E6C8), Color(0xFFFBF4E4))
      )
    )
    // Soft sunbeams
    for (i in 0 until 3) {
      val bx = w * (0.15f + i * 0.3f)
      val beam = Path().apply {
        moveTo(bx, 0f)
        lineTo(bx + w * 0.14f, 0f)
        lineTo(bx + w * 0.28f, h * 0.85f)
        lineTo(bx + w * 0.04f, h * 0.85f)
        close()
      }
      drawPath(beam, color = Color(0x30FFF6D2))
    }

    // Great Tree silhouette
    val tx = w * 0.5f
    val ty = h * 0.82f
    val trunk = Path().apply {
      moveTo(tx - w * 0.16f, ty)
      quadraticTo(tx - w * 0.09f, ty - h * 0.16f, tx - w * 0.08f, ty - h * 0.44f)
      lineTo(tx + w * 0.08f, ty - h * 0.44f)
      quadraticTo(tx + w * 0.09f, ty - h * 0.16f, tx + w * 0.17f, ty)
      close()
    }
    drawPath(trunk, color = Color(0xFF7D6245))

    val puffs = listOf(
      Triple(tx - w * 0.24f, ty - h * 0.46f, w * 0.22f to Color(0xFF7FA96B)),
      Triple(tx + w * 0.24f, ty - h * 0.46f, w * 0.22f to Color(0xFF7FA96B)),
      Triple(tx - w * 0.1f, ty - h * 0.58f, w * 0.25f to Color(0xFF94BF7A)),
      Triple(tx + w * 0.12f, ty - h * 0.56f, w * 0.24f to Color(0xFF83B16C)),
      Triple(tx, ty - h * 0.47f, w * 0.26f to Color(0xFF9AC581))
    )
    puffs.forEachIndexed { idx, (px, py, rc) ->
      drawCircle(rc.second, radius = rc.first, center = Offset(px + sin(time + idx) * 3f, py))
    }

    // Glowing amber lanterns on tree
    listOf(0.22f to 0.32f, 0.78f to 0.30f, 0.34f to 0.42f, 0.67f to 0.41f).forEach { (lx, ly) ->
      drawCircle(Color(0x55FFCE5E), radius = w * 0.055f, center = Offset(w * lx, h * ly))
      drawCircle(Color(0xFFF3A833), radius = w * 0.025f, center = Offset(w * lx, h * ly))
    }

    // Sitting cute robot at the base
    val rx = w * 0.5f
    val ry = h * 0.72f
    drawOval(Color(0x2826221C), Offset(rx - w * 0.09f, ry + h * 0.07f), Size(w * 0.18f, h * 0.03f))
    drawRoundRect(
      color = Color(0xFFF6EFE0),
      topLeft = Offset(rx - w * 0.075f, ry - h * 0.06f),
      size = Size(w * 0.15f, h * 0.13f),
      cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
    )
    drawRoundRect(
      color = Color(0xFF7E6850),
      topLeft = Offset(rx - w * 0.075f, ry - h * 0.06f),
      size = Size(w * 0.15f, h * 0.13f),
      cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
      style = Stroke(width = 3f)
    )
    drawRoundRect(
      color = Color(0xFF332D26),
      topLeft = Offset(rx - w * 0.052f, ry - h * 0.04f),
      size = Size(w * 0.104f, h * 0.05f),
      cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
    )
    drawCircle(Color(0xFFFFD670), radius = w * 0.01f, center = Offset(rx - w * 0.022f, ry - h * 0.015f))
    drawCircle(Color(0xFFFFD670), radius = w * 0.01f, center = Offset(rx + w * 0.022f, ry - h * 0.015f))
    // Sprout on head
    drawLine(Color(0xFF5F8A4E), Offset(rx, ry - h * 0.06f), Offset(rx, ry - h * 0.10f), strokeWidth = 4f)
    drawOval(Color(0xFF7CB562), Offset(rx, ry - h * 0.115f), Size(w * 0.045f, h * 0.02f))

    // Soft bottom fade into cream
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color(0xFFFBF4E4)),
        startY = h * 0.65f,
        endY = h
      )
    )
  }
}

@Composable
fun HiveIllustration(time: Float, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE7F0DA), Color(0xFFF6EBD3))
      )
    )
    // Meadow hill
    drawOval(
      color = Color(0xFF9AC07C),
      topLeft = Offset(-w * 0.1f, h * 0.62f),
      size = Size(w * 1.2f, h * 0.6f)
    )
    // Wooden hive box
    val hx = w * 0.34f
    val hy = h * 0.36f
    drawRoundRect(
      color = Color(0xFFC2955D),
      topLeft = Offset(hx - w * 0.13f, hy),
      size = Size(w * 0.26f, h * 0.36f),
      cornerRadius = CornerRadius(12f, 12f)
    )
    // Hive roof
    drawRoundRect(
      color = Color(0xFF8C633B),
      topLeft = Offset(hx - w * 0.15f, hy - h * 0.05f),
      size = Size(w * 0.30f, h * 0.08f),
      cornerRadius = CornerRadius(8f, 8f)
    )
    // Hive slats
    for (i in 1..3) {
      drawLine(
        color = Color(0xFF9B6F42),
        start = Offset(hx - w * 0.12f, hy + h * 0.08f * i),
        end = Offset(hx + w * 0.12f, hy + h * 0.08f * i),
        strokeWidth = 2.5f
      )
    }
    // Hive entrance
    drawRoundRect(
      color = Color(0xFF3B2A1A),
      topLeft = Offset(hx - w * 0.04f, hy + h * 0.26f),
      size = Size(w * 0.08f, h * 0.05f),
      cornerRadius = CornerRadius(6f, 6f)
    )

    // Honey jars on wooden bench
    val jx = w * 0.68f
    val jy = h * 0.58f
    drawRect(Color(0xFF9E744A), Offset(jx - w * 0.14f, jy + h * 0.11f), Size(w * 0.28f, h * 0.04f))
    listOf(-0.07f, 0f, 0.07f).forEachIndexed { idx, dx ->
      val jh = h * (0.12f - idx * 0.01f)
      drawRoundRect(
        color = Color(0xFFE89B2D),
        topLeft = Offset(jx + w * dx - w * 0.028f, jy + h * 0.11f - jh),
        size = Size(w * 0.056f, jh),
        cornerRadius = CornerRadius(8f, 8f)
      )
      drawRect(
        color = Color(0xFF7D5836),
        topLeft = Offset(jx + w * dx - w * 0.022f, jy + h * 0.11f - jh - 6f),
        size = Size(w * 0.044f, 6f)
      )
    }

    // Animated bees around hive
    for (i in 0 until 4) {
      val bx = hx + cos(time * 2f + i * 1.5f) * w * 0.18f
      val by = hy + sin(time * 2.5f + i) * h * 0.12f
      drawCircle(Color(0xFFF2B830), radius = 6f, center = Offset(bx, by))
      drawCircle(Color(0xCCFFFFFF), radius = 4f, center = Offset(bx - 3f, by - 5f))
    }
  }
}
