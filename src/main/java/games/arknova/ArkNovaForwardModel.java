package games.arknova;

import core.AbstractGameState;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Counter;
import games.arknova.actions.PlaceBuilding;
import games.arknova.components.ArkNovaMap;
import games.arknova.components.Building;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The forward model contains all the game rules and logic. It is mainly responsible for declaring
 * rules for:
 *
 * <ol>
 *   <li>Game setup
 *   <li>Actions available to players in a given game state
 *   <li>Game events or rules applied after a player's action
 *   <li>Game end
 * </ol>
 */
public class ArkNovaForwardModel extends StandardForwardModel {

  /**
   * Initializes all variables in the given game state. Performs initial game setup according to
   * game rules, e.g.:
   *
   * <ul>
   *   <li>Sets up decks of cards and shuffles them
   *   <li>Gives player cards
   *   <li>Places tokens on boards
   *   <li>...
   * </ul>
   *
   * @param firstState - the state to be modified to the initial game state.
   */
  @Override
  protected void _setup(AbstractGameState firstState) {
    // TODO: perform initialization of variables and game setup

    ArkNovaGameState gs = (ArkNovaGameState) firstState;

    int nPlayers = gs.getNPlayers();

    gs.maps = new ArkNovaMap[nPlayers];
    gs.playerIcons = new HashMap[nPlayers];
    gs.playerResources = new HashMap[nPlayers];
    gs.actionOrder = new ArrayList[nPlayers];
    gs.actionLevel = new HashMap[nPlayers];
    for (int i = 0; i < nPlayers; i++) {
      gs.maps[i] = new ArkNovaMap(ArkNovaMap.MapData.Map7);
      gs.actionOrder[i] = new ArrayList<>();
      gs.actionLevel[i] = new HashMap<>();

      gs.playerIcons[i] = new HashMap<>();
      gs.playerResources[i] = new HashMap<>();

      // Set actions
      gs.actionOrder[i].add(ArkNovaConstants.MainAction.ANIMALS);
      List<ArkNovaConstants.MainAction> shuffledActionsWithoutAnimals =
          Arrays.stream(ArkNovaConstants.MainAction.values())
              .filter(mainAction -> mainAction != ArkNovaConstants.MainAction.ANIMALS)
              .collect(Collectors.toList());

      Collections.shuffle(shuffledActionsWithoutAnimals);
      gs.actionOrder[i].addAll(shuffledActionsWithoutAnimals);

      for (ArkNovaConstants.MainAction action : ArkNovaConstants.MainAction.values()) {
        gs.actionLevel[i].put(action, ArkNovaConstants.MainActionLevel.BASE);
      }

      // Set all resource to the initial value
      gs.playerResources[i].put(
          ArkNovaConstants.Resource.MONEY,
          new Counter(ArkNovaConstants.STARTING_MONEY, 0, Integer.MAX_VALUE, "MoneyCounter"));
      gs.playerResources[i].put(
          ArkNovaConstants.Resource.APPEAL,
          new Counter(i, 0, ArkNovaConstants.MAXIMUM_APPEAL, "AppealCounter"));

      gs.playerResources[i].put(
          ArkNovaConstants.Resource.CONSERVATION_POINTS,
          new Counter(
              0, 0, ArkNovaConstants.MAXIMUM_CONSERVATION_POINTS, "ConservationPointsCounter"));

      gs.playerResources[i].put(
          ArkNovaConstants.Resource.REPUTATION,
          new Counter(1, 1, ArkNovaConstants.MAXIMUM_REPUTATION, "ReputationCounter"));
      gs.playerResources[i].put(
          ArkNovaConstants.Resource.X_TOKEN,
          new Counter(0, 0, ArkNovaConstants.MAXIMUM_X_TOKEN, "XTokenCounter"));
      gs.playerResources[i].put(
          ArkNovaConstants.Resource.WORKER,
          new Counter(1, 1, ArkNovaConstants.MAXIMUM_WORKERS, "WorkerCounter"));

      // Set all icons to 0
      for (ArkNovaConstants.Icon icon : ArkNovaConstants.Icon.values()) {
        gs.playerIcons[i].put(icon, new Counter(icon.name() + "IconCounter"));
      }
    }
    gs.breakCounter =
        new Counter(0, 0, ArkNovaConstants.MAXIMUM_BREAK[nPlayers - 2], "BreakCounter");
  }

  /**
   * Calculates the list of currently available actions, possibly depending on the game phase.
   *
   * @return - List of AbstractAction objects.
   */
  @Override
  protected List<AbstractAction> _computeAvailableActions(AbstractGameState gs) {
    List<AbstractAction> actions = new ArrayList<>();
    // TODO: create action classes for the current player in the given game state and add them to
    // the list. Below just an example that does nothing, remove.
    ArkNovaGameState state = (ArkNovaGameState) gs;

    int i = state.getCurrentPlayer();

    boolean hasDiversityResearcher = false;
    boolean isBuildUpgraded = true;
    ArrayList<Building> legalBuildingsPlacements =
        state
            .getCurrentPlayerMap()
            .getLegalBuildingsPlacements(isBuildUpgraded, hasDiversityResearcher);

    System.out.format(
        "[%s] Legal buildings placements: %s \n",
        state.getCurrentPlayer(), legalBuildingsPlacements.size());
    for (Building building : legalBuildingsPlacements) {
      actions.add(new PlaceBuilding(building));
    }
    return actions;
  }

  @Override
  protected void _afterAction(AbstractGameState currentState, AbstractAction actionTaken) {

    endPlayerTurn(currentState);
    super._afterAction(currentState, actionTaken);
  }
}
