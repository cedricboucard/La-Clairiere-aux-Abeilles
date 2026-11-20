package fr.clairiere.abeilles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.clairiere.abeilles.game.GameState
import fr.clairiere.abeilles.ui.components.AccueilHeroIllustration
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
fun AccueilScreen(
  state: GameState,
  onContinue: () -> Unit,
  onNewGame: () -> Unit
) {
  var showGuide by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamBg)
  ) {
    // Top Illustrated Hero Backdrop
    AccueilHeroIllustration(
      time = state.time,
      modifier = Modifier
        .fillMaxWidth()
        .height(420.dp)
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      // Top Badge: SANCTUAIRE BOTANIQUE
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(999.dp))
          .background(CreamSurface.copy(alpha = 0.88f))
          .border(1.dp, BorderGold, RoundedCornerShape(999.dp))
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        Text(
          text = "🌿  SANCTUAIRE BOTANIQUE",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 1.sp,
          color = TextEspresso
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "La Clairière aux\nabeilles",
        style = MaterialTheme.typography.displaySmall,
        textAlign = TextAlign.Center,
        color = TextEspresso
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Un jardin à ton rythme, au fil des\nsaisons",
        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
        textAlign = TextAlign.Center,
        color = TextMuted
      )

      Spacer(modifier = Modifier.weight(1f))

      // Status Pill: Ruche paisible • Brise douce
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(999.dp))
          .background(CreamSurface.copy(alpha = 0.92f))
          .border(1.dp, BorderGold, RoundedCornerShape(999.dp))
          .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(ForestGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "🐝 Ruche paisible • Brise douce",
          fontSize = 12.sp,
          color = ForestGreen,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Save Summary Card
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(4.dp, RoundedCornerShape(20.dp))
          .clip(RoundedCornerShape(20.dp))
          .background(CreamSurface)
          .border(1.dp, BorderGold, RoundedCornerShape(20.dp))
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(LeafGreen.copy(alpha = 0.22f)),
          contentAlignment = Alignment.Center
        ) {
          Text("🌸", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Clairière de l'Aube",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = state.currentSeason.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
          )
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(CreamWarm)
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = "Jour ${state.dayCount}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextEspresso
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Primary Amber CTA
      Button(
        onClick = onContinue,
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "‹  Continuer dans la clairière  ›",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "${state.currentSeason.displayName} • ${state.flowerCount} fleurs fleuries",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.9f)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Secondary CTA: Nouvelle clairière
      OutlinedButton(
        onClick = onNewGame,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          containerColor = CreamWarm,
          contentColor = TextEspresso
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold)
      ) {
        Text(
          text = "⊕  Nouvelle clairière",
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Guide link
      Text(
        text = "◐  Comment jouer • Grimoire du jardinier",
        fontSize = 13.sp,
        color = TextMuted,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
          .clickable { showGuide = true }
          .padding(8.dp)
      )

      Spacer(modifier = Modifier.height(18.dp))
    }
  }

  if (showGuide) {
    AlertDialog(
      onDismissRequest = { showGuide = false },
      containerColor = CreamSurface,
      title = {
        Text("📖 Grimoire du jardinier", style = MaterialTheme.typography.titleLarge)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("1. Touche un fruit ambré mûr sur le Grand Arbre pour récolter des graines.", style = MaterialTheme.typography.bodyMedium, color = TextEspresso)
          Text("2. Touche un emplacement libre au sol pour que le petit robot y sème une fleur.", style = MaterialTheme.typography.bodyMedium, color = TextEspresso)
          Text("3. Observe le vol des abeilles : elles pollinisent tes fleurs et te guident vers les fougères, la mare et la passerelle.", style = MaterialTheme.typography.bodyMedium, color = TextEspresso)
          Text("4. Consulte ton Carnet et visite La Ruche pour récolter du miel et bénir ta clairière.", style = MaterialTheme.typography.bodyMedium, color = TextEspresso)
        }
      },
      confirmButton = {
        TextButton(onClick = { showGuide = false }) {
          Text("Compris !", color = ForestGreen, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
