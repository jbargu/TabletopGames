package games.arknova.actions;

import games.arknova.ArkNovaConstants;
import games.arknova.ArkNovaGameState;
import games.arknova.components.Building;
import java.util.Objects;

public class PlaceBuilding extends ArkNovaAction {

  Building building;

  public PlaceBuilding(int playerId, Building building, boolean isFree) {
    super(playerId, 0, isFree);
    this.building = building;
  }

  @Override
  public boolean _execute(ArkNovaGameState gs) {
    gs.getMaps()[playerId].addBuilding(building);

    if (!isFree) {
      gs.incMoney(
          playerId, -ArkNovaConstants.MONEY_PER_ONE_BUILDING_HEX * building.getLayout().size());
    }

    return true;
  }

  @Override
  public ArkNovaAction copy() {
    return new PlaceBuilding(playerId, building, isFree);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    PlaceBuilding that = (PlaceBuilding) o;
    return Objects.equals(building, that.building);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), building);
  }

  @Override
  public String toString() {
    return String.format("Place Building: %s", this.building);
  }
}
