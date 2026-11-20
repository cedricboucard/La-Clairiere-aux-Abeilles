package fr.clairiere.abeilles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.clairiere.abeilles.game.GameState
import fr.clairiere.abeilles.game.Season
import fr.clairiere.abeilles.game.Species
import fr.clairiere.abeilles.ui.components.FlowerIllustration
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
fun CarnetScreen(
  state: GameState,
  onSelectSpeciesToPlant: (Species) -> Unit,
  onBackToClairiere: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Flore, 1 = Lieux & Saisons

  val discoveredCount = state.discoveredSpecies.size
  val totalSpecies = Species.entries.size
  val progressPct = ((discoveredCount.toFloat() / totalSpecies) * 100).toInt()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CreamBg)
      .statusBarsPadding(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(CreamSurface)
              .border(1.dp, BorderGold, CircleShape)
              .clickable { onBackToClairiere() },
            contentAlignment = Alignment.Center
          ) {
            Text("←", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextEspresso)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("Mon Carnet Botanique", style = MaterialTheme.typography.titleLarge)
            Text("Carnet du Petit Robot", style = MaterialTheme.typography.bodyMedium)
          }
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(CreamSurface)
            .border(1.dp, BorderGold, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text("🌿 $progressPct%", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
        }
      }
    }

    // Progress Summary Card
    item {
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
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            "🌾 $discoveredCount / $totalSpecies fleurs trouvées",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextEspresso
          )
          Text(
            "🧭 ${state.unlockedZonesCount} / 4 clairières",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ForestGreen
          )
        }
        LinearProgressIndicator(
          progress = { discoveredCount.toFloat() / totalSpecies.toFloat() },
          modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = AmberPrimary,
          trackColor = CreamWarm
        )
      }
    }

    // Segmented Tab Toggle: Flore | Lieux & Saisons
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(999.dp))
          .background(CreamWarm)
          .border(1.dp, BorderGold, RoundedCornerShape(999.dp))
          .padding(4.dp)
      ) {
        SegmentedTabButton(
          label = "🌿  Flore",
          selected = selectedSubTab == 0,
          onClick = { selectedSubTab = 0 },
          modifier = Modifier.weight(1f)
        )
        SegmentedTabButton(
          label = "🗺️  Lieux & Saisons",
          selected = selectedSubTab == 1,
          onClick = { selectedSubTab = 1 },
          modifier = Modifier.weight(1f)
        )
      }
    }

    if (selectedSubTab == 0) {
      // Screen 3: Flore Botanique (2-column grid)
      val rows = Species.entries.chunked(2)
      items(rows.size) { rowIdx ->
        val pair = rows[rowIdx]
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          pair.forEach { sp ->
            val unlocked = sp in state.discoveredSpecies
            SpeciesCard(
              species = sp,
              unlocked = unlocked,
              onPlantClick = { onSelectSpeciesToPlant(sp) },
              modifier = Modifier.weight(1f)
            )
          }
          if (pair.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }

      // Bottom Pollen Summary
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CreamSurface)
            .border(1.dp, BorderGold, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            "✨ Pollen royal : ${state.royalPollen} grains",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextEspresso
          )
          Text(
            "Ruche active",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
          )
        }
      }
    } else {
      // Screen 4: Lieux & Saisons
      item {
        CycleNaturelCard(state = state)
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("🗺️  Lieux de la Clairière", style = MaterialTheme.typography.titleLarge)
          Text("${state.unlockedZonesCount} sur 4", style = MaterialTheme.typography.bodyMedium)
        }
      }

      val places = listOf(
        PlaceInfo(
          title = "Le Premier Arbre",
          status = "Découvert ✓",
          unlocked = true,
          zoneTag = "Tronc Centenaire",
          desc = "Cueillir les fruits ambrés et semer au pied du tronc."
        ),
        PlaceInfo(
          title = "Les Fougères & La Mare",
          status = if (state.fernsCleared) "Découvert ✓" else "À explorer",
          unlocked = state.fernsCleared,
          zoneTag = "Eau claire & mousses",
          desc = if (state.fernsCleared) "Passage ouvert ! Ses berges accueillent iris et nénuphars."
          else "Suis le vol des abeilles jusqu'aux fougères et touche-les."
        ),
        PlaceInfo(
          title = "La Passerelle de Bois",
          status = if (state.bridgeRepaired) "Réparée ✓" else if (state.fernsCleared) "À réparer ⚒️" else "Verrouillé 🔒",
          unlocked = state.bridgeRepaired,
          highlightRepair = state.fernsCleared && !state.bridgeRepaired,
          zoneTag = "Ruisseau sauvage",
          desc = if (state.bridgeRepaired) "La passerelle sécurisée mène vers l'autre rive."
          else "Touche la passerelle abîmée au bord du ruisseau pour la réparer."
        ),
        PlaceInfo(
          title = "L'Autre Rive Sauvage",
          status = if (state.bridgeRepaired) "Découvert ✓" else "Verrouillé 🔒",
          unlocked = state.bridgeRepaired,
          zoneTag = "Territoire Fleuri",
          desc = "Découvre la Menthe, la Campanule Lunaire et la Bruyère Dorée."
        )
      )

      items(places.size) { idx ->
        PlaceCard(place = places[idx])
      }
    }
  }
}

@Composable
private fun SegmentedTabButton(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(999.dp))
      .background(if (selected) ForestGreen else Color.Transparent)
      .clickable { onClick() }
      .padding(vertical = 9.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 13.5.sp,
      fontWeight = FontWeight.Bold,
      color = if (selected) Color.White else TextEspresso
    )
  }
}

@Composable
private fun SpeciesCard(
  species: Species,
  unlocked: Boolean,
  onPlantClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .alpha(if (unlocked) 1f else 0.65f)
      .clip(RoundedCornerShape(18.dp))
      .background(CreamSurface)
      .border(1.dp, BorderGold, RoundedCornerShape(18.dp))
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(92.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(CreamWarm),
      contentAlignment = Alignment.Center
    ) {
      if (unlocked) {
        FlowerIllustration(species = species, modifier = Modifier.size(72.dp))
      } else {
        Text("🔒", fontSize = 28.sp)
      }
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(6.dp)
          .clip(RoundedCornerShape(999.dp))
          .background(CreamSurface.copy(alpha = 0.9f))
          .padding(horizontal = 8.dp, vertical = 2.dp)
      ) {
        Text(species.numberLabel, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextEspresso)
      }
    }

    Text(
      text = if (unlocked) species.zoneLabel else "Inconnu",
      fontSize = 11.sp,
      color = ForestGreen,
      fontWeight = FontWeight.SemiBold
    )

    Text(
      text = if (unlocked) species.displayName else "Plante mystérieuse",
      style = MaterialTheme.typography.titleMedium,
      fontSize = 15.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )

    Text(
      text = if (unlocked) species.desc else "Cherche du côté de : ${species.zoneLabel}",
      fontSize = 12.sp,
      lineHeight = 16.sp,
      color = TextMuted,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis
    )

    if (unlocked) {
      Button(
        onClick = onPlantClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(34.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
      ) {
        Text("✨ Semer", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(34.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(CreamWarm),
        contentAlignment = Alignment.Center
      ) {
        Text("Pétale scellé", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
      }
    }
  }
}

@Composable
private fun CycleNaturelCard(state: GameState) {
  val current = state.currentSeason
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(CreamSurface)
      .border(1.dp, BorderGold, RoundedCornerShape(20.dp))
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("●  CYCLE NATUREL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberPrimary)
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(999.dp))
          .background(CreamWarm)
          .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Text("Jour ${state.dayCount}", fontSize = 11.5.sp, color = TextEspresso, fontWeight = FontWeight.SemiBold)
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Season.entries.forEach { season ->
        val active = season == current
        Column(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) LeafGreen.copy(alpha = 0.2f) else CreamWarm)
            .border(
              1.dp,
              if (active) ForestGreen else Color.Transparent,
              RoundedCornerShape(14.dp)
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(12.dp)
              .clip(CircleShape)
              .background(season.dotColor)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = season.displayName,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color = TextEspresso
          )
          Text(
            text = if (active) "Actif" else "Cycle",
            fontSize = 10.sp,
            color = if (active) ForestGreen else TextMuted
          )
        }
      }
    }

    Text(
      text = current.quote,
      style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
      fontSize = 14.sp,
      color = TextEspresso
    )
  }
}

private data class PlaceInfo(
  val title: String,
  val status: String,
  val unlocked: Boolean,
  val zoneTag: String,
  val desc: String,
  val highlightRepair: Boolean = false
)

@Composable
private fun PlaceCard(place: PlaceInfo) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .alpha(if (place.unlocked || place.highlightRepair) 1f else 0.65f)
      .clip(RoundedCornerShape(18.dp))
      .background(CreamSurface)
      .border(1.dp, if (place.highlightRepair) AmberPrimary else BorderGold, RoundedCornerShape(18.dp))
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(place.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(999.dp))
          .background(
            when {
              place.unlocked -> LeafGreen.copy(alpha = 0.2f)
              place.highlightRepair -> AmberPrimary.copy(alpha = 0.22f)
              else -> CreamWarm
            }
          )
          .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Text(
          text = place.status,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = if (place.unlocked) ForestGreen else TextEspresso
        )
      }
    }
    Text(place.zoneTag, fontSize = 12.sp, color = ForestGreen, fontWeight = FontWeight.SemiBold)
    Text(place.desc, fontSize = 13.sp, color = TextMuted)
  }
}
