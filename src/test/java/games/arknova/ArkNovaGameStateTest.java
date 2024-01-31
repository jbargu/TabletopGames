package games.arknova;

import static org.testng.Assert.assertEquals;

import core.AbstractPlayer;
import core.Game;
import games.GameType;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;
import players.simple.RandomPlayer;

public class ArkNovaGameStateTest {

  List<AbstractPlayer> players =
      Arrays.asList(new RandomPlayer(), new RandomPlayer(), new RandomPlayer());

  Game game =
      new Game(
          GameType.ArkNova,
          players,
          new ArkNovaForwardModel(),
          new ArkNovaGameState(new ArkNovaParameters(), players.size()));
  ArkNovaForwardModel fm = new ArkNovaForwardModel();

  @Test
  public void testSetup() {
    // TODO: add other checks
    ArkNovaGameState gs = (ArkNovaGameState) game.getGameState();

    assertEquals(gs.getBreakCounter().getMaximum(), 12);

    for (AbstractPlayer player : players) {
      assertEquals(gs.getMoney(player.getPlayerID()).getValue(), 25);
      assertEquals(gs.getConservationPoints(player.getPlayerID()).getValue(), 0);
      assertEquals(gs.getReputation(player.getPlayerID()).getValue(), 1);
      assertEquals(gs.getXTokens(player.getPlayerID()).getValue(), 0);
      assertEquals(gs.getWorkers(player.getPlayerID()).getValue(), 1);
    }

    assertEquals(gs.getAppeal(0).getValue(), 0);
    assertEquals(gs.getAppeal(1).getValue(), 1);
    assertEquals(gs.getAppeal(2).getValue(), 2);
  }

  @Test
  public void testGetGameScore() {
    ArkNovaGameState gs = (ArkNovaGameState) game.getGameState();

    assertEquals(gs.getGameScore(0), -14);
    assertEquals(gs.getGameScore(1), -13);
    assertEquals(gs.getGameScore(2), -12);

    // Random value check
    gs.getConservationPoints(0).setValue(9);
    gs.getAppeal(0).setValue(62);

    assertEquals(gs.getGameScore(0), 66);

    // Edge case check
    gs.getConservationPoints(0).setValue(41);
    gs.getAppeal(0).setValue(113);

    assertEquals(gs.getGameScore(0), 212);
  }
}
