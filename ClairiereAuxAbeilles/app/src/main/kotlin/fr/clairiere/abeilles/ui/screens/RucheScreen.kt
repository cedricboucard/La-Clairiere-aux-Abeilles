package fr.clairiere.abeilles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.clairiere.abeilles.game.ForagingFlight
import fr.clairiere.abeilles.game.GameState
import fr.clairiere.abeilles.game.HiveBlessing
import fr.clairiere.abeilles.ui.components.HiveIllustration
import fr.clairiere.abeilles.ui.theme.AmberDark
import fr.clairiere.abeilles.ui.theme.AmberPrimary
import fr.clairiere.abeilles.ui.theme.BorderGold
import fr.clairiere.abeilles.ui.theme.CreamBg
import fr.clairiere.abeilles.ui.theme.CreamSurface
import fr.clairiere.abeilles.ui.theme.CreamWarm
import fr.clairiere.abeilles.ui.theme.ForestGreen
import fr.clairiere.abeilles.ui.theme.LeafGreen
import fr.clairiere.abeilles.ui.theme.TextEspresso
import fr.clairiere.abeilles.ui.theme.TextMuted

@Composable
fun RucheScreen(
  state: GameState,
  onHarvestHoney: () -> Unit,
  onInteractFlight: (String) -> Unit,
  onUpgradeBlessing: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val honeyPct = (state.honeyReserve * 100).toInt().coerceIn(0, 100)
  val activeFlightsCount = state.flights.count { it.active || it.ready }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CreamBg)
      .statusBarsPadding(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Status pill & Header
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(LeafGreen.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
          Text(
            text = "●  Reine Éveillée • Bourdonnement serein",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = ForestGreen
          )
        }
        Text("Le Cœur de la Ruche", style = MaterialTheme.typography.headlineMedium)
        Text(
          "Le doux murmure des abeilles au retour du butinage parfumé.",
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }

    // Main Hive Card
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(22.dp))
          .background(CreamSurface)
          .border(1.dp, BorderGold, RoundedCornerShape(22.dp))
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
        ) {
          HiveIllustration(time = state.time, modifier = Modifier.fillMaxSize())
          Box(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(10.dp)
              .clip(RoundedCornerShape(999.dp))
              .background(CreamSurface.copy(alpha = 0.9f))
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text("☀️ Chaleur dorée • 22°C", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextEspresso)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("💧 Réserve de Miel Ambré", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextEspresso)
          Text("$honeyPct%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AmberDark)
        }

        LinearProgressIndicator(
          progress = { state.honeyReserve.coerceIn(0f, 1f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(9.dp)
            .clip(RoundedCornerShape(5.dp)),
          color = AmberPrimary,
          trackColor = CreamWarm
        )

        Button(
          onClick = onHarvestHoney,
          enabled = state.honeyReserve >= 0.2f,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          shape = RoundedCornerShape(24.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
        ) {
          Text(
            text = if (state.honeyReserve >= 0.2f) "🍯  Récolter le nectar ambré" else "En cours de maturation…",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        // 3 Mini Stats
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          HiveStatBox("🐝", "${state.bees.size}", "Ouvrières", Modifier.weight(1f))
          HiveStatBox("💧", "${state.nectar}", "Nectar", Modifier.weight(1f))
          HiveStatBox("✨", "${state.royalPollen}", "Pollen royal", Modifier.weight(1f))
        }
      }
    }

    // Vols de Butinage
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("🪽  Vols de Butinage", style = MaterialTheme.typography.titleLarge)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(LeafGreen.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text("$activeFlightsCount / 3 en vol", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
        }
      }
    }

    items(state.flights.size) { idx ->
      FlightCard(
        flight = state.flights[idx],
        onClick = { onInteractFlight(state.flights[idx].id) }
      )
    }

    // Bénédictions de la Reine
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("✨  Bénédictions de la Reine", style = MaterialTheme.typography.titleLarge)
        Text("Harmonie passive", style = MaterialTheme.typography.labelSmall)
      }
    }

    items(state.blessings.size) { idx ->
      val bl = state.blessings[idx]
      BlessingCard(
        blessing = bl,
        canAfford = state.royalPollen >= bl.costPollen && bl.level < bl.maxLevel,
        onUpgrade = { onUpgradeBlessing(bl.id) }
      )
    }

    // Quote card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(CreamSurface)
          .border(1.dp, BorderGold, RoundedCornerShape(18.dp))
          .padding(16.dp)
      ) {
        Text(
          text = "« Chaque goutte de miel renferme le souvenir d'un millier de pétales caressés par le vent. »",
          style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
          color = TextMuted
        )
      }
    }
  }
}

@Composable
private fun HiveStatBox(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(CreamWarm)
      .padding(vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(icon, fontSize = 16.sp)
    Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextEspresso)
    Text(label, fontSize = 11.sp, color = TextMuted)
  }
}

@Composable
private fun FlightCard(flight: ForagingFlight, onClick: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(CreamSurface)
      .border(1.dp, BorderGold, RoundedCornerShape(18.dp))
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(flight.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(flight.rewardText, fontSize = 12.5.sp, color = TextMuted)
      }
      if (!flight.active && !flight.ready) {
        OutlinedButton(
          onClick = onClick,
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold)
        ) {
          Text("Envoyer", fontSize = 12.5.sp, color = TextEspresso, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    if (flight.active) {
      LinearProgressIndicator(
        progress = { flight.progress.coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = ForestGreen,
        trackColor = CreamWarm
      )
    }

    if (flight.ready) {
      Button(
        onClick = onClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
      ) {
        Text("🌾  Accueillir l'essaim", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }
    }
  }
}

@Composable
private fun BlessingCard(
  blessing: HiveBlessing,
  canAfford: Boolean,
  onUpgrade: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(CreamSurface)
      .border(1.dp, BorderGold, RoundedCornerShape(18.dp))
      .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(CreamWarm),
      contentAlignment = Alignment.Center
    ) {
      Text(if (blessing.level > 0) "✨" else "🔒", fontSize = 18.sp)
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(blessing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (blessing.level > 0) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(999.dp))
              .background(LeafGreen.copy(alpha = 0.2f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text("Niv. ${blessing.level}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
          }
        }
      }
      Text(blessing.desc, fontSize = 12.5.sp, color = TextMuted)
    }
    Spacer(modifier = Modifier.width(8.dp))
    if (blessing.level < blessing.maxLevel) {
      OutlinedButton(
        onClick = onUpgrade,
        enabled = canAfford,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (canAfford) AmberPrimary else BorderGold),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Text(
          text = "${blessing.costPollen} ✨",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = if (canAfford) AmberDark else TextMuted
        )
      }
    } else {
      Text("Max ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
    }
  }
}
