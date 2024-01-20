package games.arknova;

import core.AbstractGameState;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import games.arknova.actions.PlaceBuilding;
import games.arknova.components.ArkNovaMap;
import games.arknova.components.Building;
import java.util.ArrayList;
import java.util.List;

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
    for (int i = 0; i < nPlayers; i++) {
      gs.maps[i] = new ArkNovaMap(ArkNovaMap.MapData.Map7);
    }
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
    boolean isBuildUpgraded = false;
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
