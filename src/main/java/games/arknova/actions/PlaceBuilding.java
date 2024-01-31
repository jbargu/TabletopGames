package games.arknova.actions;

import games.arknova.ArkNovaConstants;
import games.arknova.ArkNovaGameState;
import games.arknova.components.Building;

public class PlaceBuilding extends ArkNovaAction {

  Building building;

  public PlaceBuilding(Building building) {
    this.building = building;
  }

  @Override
  public boolean _execute(ArkNovaGameState gs) {
    gs.getCurrentPlayerMap().addBuilding(building);

    //    System.out.println(String.format("Add building: " + building));
    System.out.format("[%s] Add Building: %s\n", gs.getCurrentPlayer(), building);
    gs.getAppeal(gs.getCurrentPlayer()).increment(gs.getCurrentPlayer());
    gs.getIcon(gs.getCurrentPlayer(), ArkNovaConstants.Icon.ASIA)
        .increment(2 * gs.getCurrentPlayer() + 1);

    return super._execute(gs);
  }

  @Override
  public String toString() {
    return String.format("Place Building: %s", this.building);
  }
}
