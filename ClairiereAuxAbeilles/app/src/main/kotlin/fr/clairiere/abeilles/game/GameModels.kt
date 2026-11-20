package fr.clairiere.abeilles.game

import androidx.compose.ui.graphics.Color

const val WORLD_W = 1400f
const val WORLD_H = 820f

enum class Season(
  val id: String,
  val displayName: String,
  val subtitle: String,
  val quote: String,
  val dotColor: Color,
  val skyTop: Color,
  val skyBottom: Color,
  val grass1: Color,
  val grass2: Color,
  val treeLeaf1: Color,
  val treeLeaf2: Color,
  val bgTree: Color,
  val water1: Color,
  val water2: Color,
  val particleType: ParticleType
) {
  PRINTEMPS(
    id = "spring",
    displayName = "Printemps",
    subtitle = "Saison du réveil des bourgeons",
    quote = "« La clairière s'éveille doucement avec l'arrivée des premières abeilles. »",
    dotColor = Color(0xFF7EC86F),
    skyTop = Color(0xFFCDE8D5),
    skyBottom = Color(0xFFE9F3DF),
    grass1 = Color(0xFF7DA865),
    grass2 = Color(0xFF6B9656),
    treeLeaf1 = Color(0xFF6FA159),
    treeLeaf2 = Color(0xFF8CC06F),
    bgTree = Color(0xFF4B7148),
    water1 = Color(0xFF6AA8B3),
    water2 = Color(0xFF4D8694),
    particleType = ParticleType.PETAL
  ),
  ETE(
    id = "summer",
    displayName = "Été",
    subtitle = "Chaleur dorée et vols longs",
    quote = "« Les lucioles dansent au crépuscule autour des fleurs gorgées de nectar. »",
    dotColor = Color(0xFFE6B83E),
    skyTop = Color(0xFFD7ECC8),
    skyBottom = Color(0xFFF5F0CF),
    grass1 = Color(0xFF709D52),
    grass2 = Color(0xFF5D8743),
    treeLeaf1 = Color(0xFF588B48),
    treeLeaf2 = Color(0xFF79AD59),
    bgTree = Color(0xFF3F613B),
    water1 = Color(0xFF5B9EAD),
    water2 = Color(0xFF407D8E),
    particleType = ParticleType.FIREFLY
  ),
  AUTOMNE(
    id = "autumn",
    displayName = "Automne",
    subtitle = "Feuilles d'ambre et miel doux",
    quote = "« Chaque feuille dorée qui tombe nourrit la terre pour l'année suivante. »",
    dotColor = Color(0xFFD97838),
    skyTop = Color(0xFFE8CFB2),
    skyBottom = Color(0xFFF3DEC2),
    grass1 = Color(0xFF9B9352),
    grass2 = Color(0xFF837B43),
    treeLeaf1 = Color(0xFFC77237),
    treeLeaf2 = Color(0xFFE09A45),
    bgTree = Color(0xFF704D34),
    water1 = Color(0xFF5F8C94),
    water2 = Color(0xFF466E78),
    particleType = ParticleType.LEAF
  ),
  HIVER(
    id = "winter",
    displayName = "Hiver",
    subtitle = "Repos paisible sous la neige",
    quote = "« La ruche sommeille au chaud tandis que les campanules luisent dans le givre. »",
    dotColor = Color(0xFF9BC4DE),
    skyTop = Color(0xFFC8DAE8),
    skyBottom = Color(0xFFE5EFF5),
    grass1 = Color(0xFF9BB3AB),
    grass2 = Color(0xFF849E98),
    treeLeaf1 = Color(0xFF6D8C85),
    treeLeaf2 = Color(0xFF95B2AB),
    bgTree = Color(0xFF546B6C),
    water1 = Color(0xFF79A4B5),
    water2 = Color(0xFF5A8396),
    particleType = ParticleType.SNOW
  )
}

enum class ParticleType { PETAL, FIREFLY, LEAF, SNOW }

enum class SpotZone { CLAIRIERE, MARE, EAU, RIVE }

enum class Species(
  val id: String,
  val numberLabel: String,
  val displayName: String,
  val zone: SpotZone?, // null = any
  val zoneLabel: String,
  val petalColor: Color,
  val centerColor: Color,
  val petals: Int,
  val desc: String,
  val bonus: String
) {
  AMBRE(
    id = "ambre",
    numberLabel = "N°01",
    displayName = "Bouton d'Ambre",
    zone = SpotZone.CLAIRIERE,
    zoneLabel = "Sous l'arbre",
    petalColor = Color(0xFFF7BC42),
    centerColor = Color(0xFFB55D17),
    petals = 6,
    desc = "Née des fruits de l'arbre premier, elle attire aussitôt les abeilles.",
    bonus = "Butinage classique"
  ),
  TREFLE(
    id = "trefle",
    numberLabel = "N°02",
    displayName = "Trèfle Rose",
    zone = SpotZone.CLAIRIERE,
    zoneLabel = "Clairière",
    petalColor = Color(0xFFE88DA4),
    centerColor = Color(0xFFFFF2C6),
    petals = 8,
    desc = "Doux tapis fleuri qui s'étend à mesure que les abeilles explorent l'herbe.",
    bonus = "Attire de nouvelles abeilles"
  ),
  IRIS(
    id = "iris",
    numberLabel = "N°03",
    displayName = "Iris des Berges",
    zone = SpotZone.MARE,
    zoneLabel = "Berge mare",
    petalColor = Color(0xFF7885E0),
    centerColor = Color(0xFFFFD66B),
    petals = 5,
    desc = "Fleur élancée aimant l'humidité fraîche du bord de l'eau.",
    bonus = "Accélère la pousse des fruits"
  ),
  NENUPHAR(
    id = "nenuphar",
    numberLabel = "N°04",
    displayName = "Nénuphar Étoilé",
    zone = SpotZone.EAU,
    zoneLabel = "Eaux calmes",
    petalColor = Color(0xFFFDF4DF),
    centerColor = Color(0xFFF0A932),
    petals = 8,
    desc = "Flotte paisiblement sur l'eau calme et offre un repos aux abeilles.",
    bonus = "Fleurit la surface de la mare"
  ),
  MENTHE(
    id = "menthe",
    numberLabel = "N°05",
    displayName = "Menthe Sauvage",
    zone = null,
    zoneLabel = "L'autre rive",
    petalColor = Color(0xFF8ED4B2),
    centerColor = Color(0xFF488F70),
    petals = 7,
    desc = "Plante parfumée cueillie de l'autre côté de la passerelle.",
    bonus = "Double l'élan pollinisateur"
  ),
  CAMPANULE(
    id = "campanule",
    numberLabel = "N°06",
    displayName = "Campanule Lunaire",
    zone = null,
    zoneLabel = "L'autre rive",
    petalColor = Color(0xFF9CC5F7),
    centerColor = Color(0xFFFFF6CC),
    petals = 5,
    desc = "Ses clochettes luisent doucement même lorsque l'hiver recouvre la forêt.",
    bonus = "Rayonne en toute saison"
  ),
  BRUYERE(
    id = "bruyere",
    numberLabel = "N°07",
    displayName = "Bruyère Dorée",
    zone = null,
    zoneLabel = "L'autre rive",
    petalColor = Color(0xFFD97BB5),
    centerColor = Color(0xFFFFCF59),
    petals = 6,
    desc = "Plante rustique de l'autre rive qui garde la clairière vivante toute l'année.",
    bonus = "Miel abondant d'automne"
  );

  companion object {
    fun fromId(id: String): Species = entries.firstOrNull { it.id == id } ?: AMBRE
  }
}

data class FlowerInstance(
  val species: Species,
  val bloom: Float = 0.05f,
  val swayOffset: Float = 0f,
  val pollinatedCount: Int = 0
)

data class PlantingSpot(
  val id: String,
  val x: Float,
  val y: Float,
  val zone: SpotZone,
  val unlocked: Boolean = false,
  val revealAt: Int = 0,
  val flower: FlowerInstance? = null
)

enum class BranchState { BLOSSOM, GROWING, RIPE }

data class TreeBranch(
  val id: String,
  val x: Float,
  val y: Float,
  val state: BranchState,
  val progress: Float,
  val low: Boolean
)

data class WildPlant(
  val id: String,
  val species: Species,
  val x: Float,
  val y: Float,
  val collected: Boolean = false
)

enum class BeeState { ORBIT_TREE, TO_FLOWER, POLLINATE, TO_FERN, TO_TREE, SCOUT }

data class BeeTrailPoint(
  val x: Float,
  val y: Float,
  val life: Float
)

data class Bee(
  val id: Int,
  val x: Float,
  val y: Float,
  val vx: Float = 0f,
  val vy: Float = 0f,
  val angle: Float = 0f,
  val state: BeeState = BeeState.ORBIT_TREE,
  val targetSpotId: String? = null,
  val targetBranchId: String? = null,
  val timer: Float = 0f,
  val hasPollen: Boolean = false,
  val trail: List<BeeTrailPoint> = emptyList(),
  val wingPhase: Float = 0f
)

enum class RobotActionType { PICK, PLANT, CLEAR_FERNS, REPAIR_BRIDGE, COLLECT_WILD }

data class RobotAction(
  val type: RobotActionType,
  val targetId: String? = null,
  val species: Species? = null,
  val timer: Float = 0f,
  val duration: Float = 0.5f
)

data class RobotWaypoint(
  val x: Float,
  val y: Float,
  val action: RobotAction? = null
)

data class RobotState(
  val x: Float = 375f,
  val y: Float = 472f,
  val facing: Int = 1,
  val bob: Float = 0f,
  val action: RobotAction? = null,
  val queue: List<RobotWaypoint> = emptyList(),
  val blinkTimer: Float = 2.5f
)

data class AmbientParticle(
  val x: Float,
  val y: Float,
  val vx: Float,
  val vy: Float,
  val size: Float,
  val phase: Float
)

data class FloatingText(
  val x: Float,
  val y: Float,
  val text: String,
  val color: Color,
  val life: Float = 1.6f,
  val maxLife: Float = 1.6f
)

data class ForagingFlight(
  val id: String,
  val title: String,
  val rewardText: String,
  val progress: Float, // 0..1
  val ready: Boolean,
  val active: Boolean
)

data class HiveBlessing(
  val id: String,
  val title: String,
  val desc: String,
  val level: Int,
  val maxLevel: Int,
  val costPollen: Int
)

data class GameState(
  val time: Float = 0f,
  val dayCount: Int = 1,
  val seasonIndex: Int = 0,
  val seasonProgress: Float = 0f,
  val seasonDuration: Float = 68f,
  val seeds: Int = 0,
  val nectar: Int = 40,
  val royalPollen: Int = 15,
  val honeyReserve: Float = 0.35f,
  val selectedSpecies: Species = Species.AMBRE,
  val discoveredSpecies: List<Species> = listOf(Species.AMBRE),
  val carnetBadge: Boolean = false,
  val fernsCleared: Boolean = false,
  val fernsHinted: Boolean = false,
  val bridgeRepaired: Boolean = false,
  val pollinations: Int = 0,
  val guideStep: Int = 0,
  val whisper: String = "Cueille un fruit ambré sur une branche basse de l'arbre.",
  val soundEnabled: Boolean = true,
  val robot: RobotState = RobotState(),
  val spots: List<PlantingSpot> = emptyList(),
  val branches: List<TreeBranch> = emptyList(),
  val wildPlants: List<WildPlant> = emptyList(),
  val bees: List<Bee> = emptyList(),
  val floatingTexts: List<FloatingText> = emptyList(),
  val ambientParticles: List<AmbientParticle> = emptyList(),
  val flights: List<ForagingFlight> = emptyList(),
  val blessings: List<HiveBlessing> = emptyList()
) {
  val currentSeason: Season get() = Season.entries[seasonIndex % Season.entries.size]
  val flowerCount: Int get() = spots.count { it.flower != null }
  val unlockedZonesCount: Int
    get() = 1 + (if (fernsCleared) 2 else 0) + (if (bridgeRepaired) 1 else 0)
}
