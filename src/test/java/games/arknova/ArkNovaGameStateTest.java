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
  public void testGetGameScore() {
    ArkNovaGameState gameState = (ArkNovaGameState) game.getGameState();

    assertEquals(gameState.getGameScore(0), -14);
    assertEquals(gameState.getGameScore(1), -13);
    assertEquals(gameState.getGameScore(2), -12);

    // Random value check
    gameState.getConservationPoints(0).setValue(9);
    gameState.getAppeal(0).setValue(62);

    assertEquals(gameState.getGameScore(0), 66);

    // Edge case check
    gameState.getConservationPoints(0).setValue(41);
    gameState.getAppeal(0).setValue(113);

    assertEquals(gameState.getGameScore(0), 212);
  }
}
