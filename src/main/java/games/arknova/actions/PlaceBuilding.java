package games.arknova.actions;

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

    System.out.println("Add building:" + building);

    return super._execute(gs);
  }
}
