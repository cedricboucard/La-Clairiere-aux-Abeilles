package fr.clairiere.abeilles.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.clairiere.abeilles.game.GameState
import fr.clairiere.abeilles.game.Species
import fr.clairiere.abeilles.game.SpotZone
import fr.clairiere.abeilles.game.WORLD_H
import fr.clairiere.abeilles.game.WORLD_W
import fr.clairiere.abeilles.ui.components.drawGameWorld
import fr.clairiere.abeilles.ui.theme.AmberPrimary
import fr.clairiere.abeilles.ui.theme.BorderGold
import fr.clairiere.abeilles.ui.theme.CreamBg
import fr.clairiere.abeilles.ui.theme.CreamPill
import fr.clairiere.abeilles.ui.theme.CreamSurface
import fr.clairiere.abeilles.ui.theme.ForestGreen
import fr.clairiere.abeilles.ui.theme.LeafGreen
import fr.clairiere.abeilles.ui.theme.TextEspresso
import fr.clairiere.abeilles.ui.theme.TextMuted
import fr.clairiere.abeilles.ui.theme.WhisperDark
import fr.clairiere.abeilles.ui.theme.WhisperText

@Composable
fun ClairiereScreen(
  state: GameState,
  onWorldTap: (Float, Float) -> Unit,
  onSelectSpecies: (Species) -> Unit,
  onQuickPlant: () -> Unit,
  onToggleSound: () -> Unit,
  onOpenHome: () -> Unit,
  onOpenCarnet: () -> Unit,
  modifier: Modifier = Modifier
) {
  var canvasSize by remember { mutableStateOf(IntSize.Zero) }
  var panOffsetX by remember { mutableFloatStateOf(0f) }
  var initializedPan by remember { mutableStateOf(false) }

  val scale = remember(canvasSize) {
    if (canvasSize.height > 0) canvasSize.height.toFloat() / WORLD_H else 1f
  }
  val minPanX = remember(canvasSize, scale) {
    if (canvasSize.width > 0) (canvasSize.width - WORLD_W * scale).coerceAtMost(0f) else 0f
  }

  if (!initializedPan && canvasSize.width > 0) {
    // Center initially around the Great Tree & Robot (x = 410)
    val desired = (canvasSize.width / 2f) - 410f * scale
    panOffsetX = desired.coerceIn(minPanX, 0f)
    initializedPan = true
  }

  val openSpotsCount = state.spots.count {
    it.unlocked && it.flower == null && (it.zone != SpotZone.RIVE || state.bridgeRepaired)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CreamBg)
  ) {
    // Top App Header (compact Stitch style)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(CreamSurface)
            .border(1.dp, BorderGold, CircleShape)
            .clickable { onOpenHome() },
          contentAlignment = Alignment.Center
        ) {
          Text("🏡", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "La Clairière",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 18.sp
          )
          Text(
            text = "Jeu & Exploration",
            style = MaterialTheme.typography.labelSmall
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(CreamSurface)
            .border(1.dp, BorderGold, CircleShape)
            .clickable { onToggleSound() },
          contentAlignment = Alignment.Center
        ) {
          Text(if (state.soundEnabled) "🔊" else "🔇", fontSize = 16.sp)
        }
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(CreamSurface)
            .border(1.dp, BorderGold, CircleShape)
            .clickable { onOpenCarnet() },
          contentAlignment = Alignment.Center
        ) {
          Text("📖", fontSize = 16.sp)
        }
      }
    }

    // Main Interactive Game Viewport Card
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .shadow(6.dp, RoundedCornerShape(24.dp))
        .clip(RoundedCornerShape(24.dp))
        .border(1.dp, BorderGold, RoundedCornerShape(24.dp))
        .onSizeChanged { canvasSize = it }
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(scale, minPanX) {
            detectHorizontalDragGestures { _, dragAmount ->
              panOffsetX = (panOffsetX + dragAmount).coerceIn(minPanX, 0f)
            }
          }
          .pointerInput(scale, panOffsetX) {
            detectTapGestures { offset ->
              val wx = (offset.x - panOffsetX) / scale
              val wy = offset.y / scale
              onWorldTap(wx, wy)
            }
          }
      ) {
        drawGameWorld(
          state = state,
          scale = scale,
          offsetX = panOffsetX,
          offsetY = 0f
        )
      }

      // Top-Left HUD Pills
      Column(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Season Pill
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(CreamPill)
            .border(1.dp, BorderGold, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(state.currentSeason.dotColor)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = state.currentSeason.displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextEspresso
          )
        }

        // Resource Counters Row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          HudStatPill("🌱", "${state.seeds}")
          HudStatPill("🌸", "${state.flowerCount}")
          HudStatPill("🐝", "${state.bees.size}")
        }
      }

      // Bottom Overlay inside Viewport: Whisper + Seed Bar
      Column(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Whisper Bubble
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(WhisperDark)
            .border(1.dp, Color(0x40FFE09E), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Text(
            text = "✨  « ${state.whisper} »",
            fontSize = 12.5.sp,
            fontStyle = FontStyle.Italic,
            color = WhisperText
          )
        }

        // Seed Selector Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CreamPill)
            .border(1.dp, BorderGold, RoundedCornerShape(24.dp))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          state.discoveredSpecies.forEach { sp ->
            val selected = sp == state.selectedSpecies
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (selected) AmberPrimary.copy(alpha = 0.25f) else Color.Transparent)
                .border(
                  width = 1.dp,
                  color = if (selected) AmberPrimary else Color.Transparent,
                  shape = RoundedCornerShape(20.dp)
                )
                .clickable { onSelectSpecies(sp) }
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(sp.petalColor)
                  .border(1.dp, Color(0x33000000), CircleShape)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = sp.displayName,
                fontSize = 12.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = TextEspresso
              )
            }
          }
        }
      }
    }

    // Parcelle Quick-Action Card (Stitch Screen 2 bottom card)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(CreamSurface)
        .border(1.dp, BorderGold, RoundedCornerShape(18.dp))
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(LeafGreen.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Text("🌼", fontSize = 18.sp)
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (openSpotsCount > 0) "Parcelles de la clairière" else "Clairière fleurie",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = TextEspresso,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "$openSpotsCount emplacement(s) libre(s) • ${state.seeds} graine(s)",
          fontSize = 12.sp,
          color = TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = onQuickPlant,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
        modifier = Modifier.height(36.dp)
      ) {
        Text("⊕ Semer", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
      }
    }
  }
}

@Composable
private fun HudStatPill(icon: String, value: String) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(999.dp))
      .background(CreamPill)
      .border(1.dp, BorderGold, RoundedCornerShape(999.dp))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(icon, fontSize = 13.sp)
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = TextEspresso
    )
  }
}
