package fr.clairiere.abeilles.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.clairiere.abeilles.ui.theme.AmberPrimary
import fr.clairiere.abeilles.ui.theme.BadgeOrange
import fr.clairiere.abeilles.ui.theme.BorderGold
import fr.clairiere.abeilles.ui.theme.CreamSurface
import fr.clairiere.abeilles.ui.theme.TextEspresso
import fr.clairiere.abeilles.ui.theme.TextMuted

enum class AppTab(val label: String, val icon: String) {
  CLAIRIERE("Clairière", "🌳"),
  CARNET("Carnet", "📖"),
  RUCHE("Ruche", "🐝")
}

@Composable
fun BotanicalBottomNavBar(
  currentTab: AppTab,
  carnetBadge: Boolean,
  onSelectTab: (AppTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(10.dp, RoundedCornerShape(32.dp))
        .clip(RoundedCornerShape(32.dp))
        .background(CreamSurface.copy(alpha = 0.96f))
        .border(1.dp, BorderGold, RoundedCornerShape(32.dp))
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      AppTab.entries.forEach { tab ->
        val selected = tab == currentTab
        val bg = if (selected) AmberPrimary else Color.Transparent
        val contentColor = if (selected) Color.White else TextMuted

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .clickable { onSelectTab(tab) }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
              Text(
                text = tab.icon,
                fontSize = 18.sp
              )
              if (tab == AppTab.CARNET && carnetBadge && !selected) {
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BadgeOrange)
                )
              }
            }
            Text(
              text = tab.label,
              fontSize = 12.sp,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              color = if (selected) Color.White else TextEspresso.copy(alpha = 0.78f),
              maxLines = 1
            )
          }
        }
      }
    }
  }
}
