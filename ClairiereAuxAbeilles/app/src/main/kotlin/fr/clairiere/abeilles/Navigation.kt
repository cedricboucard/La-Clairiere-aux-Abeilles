package fr.clairiere.abeilles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.clairiere.abeilles.game.GameViewModel
import fr.clairiere.abeilles.ui.components.AppTab
import fr.clairiere.abeilles.ui.components.BotanicalBottomNavBar
import fr.clairiere.abeilles.ui.screens.AccueilScreen
import fr.clairiere.abeilles.ui.screens.CarnetScreen
import fr.clairiere.abeilles.ui.screens.ClairiereScreen
import fr.clairiere.abeilles.ui.screens.RucheScreen
import fr.clairiere.abeilles.ui.theme.CreamBg

@Composable
fun MainNavigation(gameViewModel: GameViewModel = viewModel()) {
  val state by gameViewModel.state.collectAsStateWithLifecycle()
  var showAccueil by remember { mutableStateOf(true) }
  var currentTab by remember { mutableStateOf(AppTab.CLAIRIERE) }

  if (showAccueil) {
    AccueilScreen(
      state = state,
      onContinue = {
        showAccueil = false
        currentTab = AppTab.CLAIRIERE
      },
      onNewGame = {
        gameViewModel.resetGame()
        showAccueil = false
        currentTab = AppTab.CLAIRIERE
      }
    )
  } else {
    Scaffold(
      containerColor = CreamBg,
      bottomBar = {
        BotanicalBottomNavBar(
          currentTab = currentTab,
          carnetBadge = state.carnetBadge,
          onSelectTab = { tab ->
            if (tab == AppTab.CARNET) {
              gameViewModel.clearCarnetBadge()
            }
            currentTab = tab
          }
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = innerPadding.calculateBottomPadding())
      ) {
        when (currentTab) {
          AppTab.CLAIRIERE -> {
            ClairiereScreen(
              state = state,
              onWorldTap = { wx, wy -> gameViewModel.handleWorldTap(wx, wy) },
              onSelectSpecies = { sp -> gameViewModel.selectSpecies(sp) },
              onQuickPlant = { gameViewModel.quickPlantNearestSpot() },
              onToggleSound = { gameViewModel.toggleSound() },
              onOpenHome = { showAccueil = true },
              onOpenCarnet = {
                gameViewModel.clearCarnetBadge()
                currentTab = AppTab.CARNET
              }
            )
          }

          AppTab.CARNET -> {
            CarnetScreen(
              state = state,
              onSelectSpeciesToPlant = { sp ->
                gameViewModel.selectSpecies(sp)
                currentTab = AppTab.CLAIRIERE
              },
              onBackToClairiere = { currentTab = AppTab.CLAIRIERE }
            )
          }

          AppTab.RUCHE -> {
            RucheScreen(
              state = state,
              onHarvestHoney = { gameViewModel.harvestHoney() },
              onInteractFlight = { id -> gameViewModel.interactFlight(id) },
              onUpgradeBlessing = { id -> gameViewModel.upgradeBlessing(id) }
            )
          }
        }
      }
    }
  }
}
