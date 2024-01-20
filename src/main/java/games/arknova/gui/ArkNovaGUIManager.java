package games.arknova.gui;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import games.arknova.ArkNovaGameState;
import gui.AbstractGUIManager;
import gui.GamePanel;
import players.human.ActionController;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * This class allows the visualisation of the game. The game components (accessible through {@link
 * Game#getGameState()} should be added into {@link javax.swing.JComponent} subclasses (e.g. {@link
 * javax.swing.JLabel}, {@link javax.swing.JPanel}, {@link javax.swing.JScrollPane}; or custom
 * subclasses such as those in {@link gui} package). These JComponents should then be added to the
 * <code>`parent`</code> object received in the class constructor.
 *
 * <p>An appropriate layout should be set for the parent GamePanel as well, e.g. {@link
 * javax.swing.BoxLayout} or {@link java.awt.BorderLayout} or {@link java.awt.GridBagLayout}.
 *
 * <p>Check the super class for methods that can be overwritten for a more custom look, or {@link
 * games.terraformingmars.gui.TMGUI} for an advanced game visualisation example.
 *
 * <p>A simple implementation example can be found in {@link
 * games.tictactoe.gui.TicTacToeGUIManager}.
 */
public class ArkNovaGUIManager extends AbstractGUIManager {

  public static boolean DEBUG = true;

  int currentlyObservedPlayer;

  Image mapImage;

  ArkNovaMapView mapView;

  SpinnerModel model;
  SpinnerModel xModel;
  SpinnerModel yModel;

  JLabel playerNumber;

  public ArkNovaGUIManager(GamePanel parent, Game game, ActionController ac, Set<Integer> human) {
    super(parent, game, ac, human);
    if (game == null) return;

    currentlyObservedPlayer = game.getGameState().getCurrentPlayer();

    JPanel mainPane = new JPanel();
    mainPane.setOpaque(false);
    mainPane.setLayout(new FlowLayout());

    // TODO: set up GUI components and add to `parent`
    mapView = new ArkNovaMapView(this, (ArkNovaGameState) game.getGameState());
    mainPane.add(mapView);

    parent.setLayout(new BorderLayout());
    parent.add(mainPane);

    model =
        new SpinnerNumberModel(
            0, // initial value
            -360, // min
            360, // max
            60);

    xModel =
        new SpinnerNumberModel(
            0, // initial value
            -360, // min
            360, // max
            5);

    yModel =
        new SpinnerNumberModel(
            0, // initial value
            -360, // min
            360, // max
            5);
    JPanel panel = new JPanel();
    panel.setSize(200, 300);
    JSpinner spinner = new JSpinner(model);

    mainPane.add(spinner);
    mainPane.add(new JSpinner(xModel));
    mainPane.add(new JSpinner(yModel));

    playerNumber = new JLabel();
    mainPane.add(playerNumber);

    JButton nextPlayer = new JButton("Observe next player");
    nextPlayer.addActionListener(
        e ->
            currentlyObservedPlayer =
                (currentlyObservedPlayer + 1) % game.getGameState().getNPlayers());

    mainPane.add(nextPlayer);

    parent.revalidate();
    parent.setVisible(true);
    parent.repaint();
  }

  /**
   * Defines how many action button objects will be created and cached for usage if needed. Less is
   * better, but should not be smaller than the number of actions available to players in any game
   * state.
   *
   * @return maximum size of the action space (maximum actions available to a player for any
   *     decision point in the game)
   */
  @Override
  public int getMaxActionSpace() {
    // TODO
    return 10;
  }

  public int getCurrentlyObservedPlayer() {
    return currentlyObservedPlayer;
  }

  /**
   * Updates all GUI elements given current game state and player that is currently acting.
   *
   * @param player - current player acting.
   * @param gameState - current game state to be used in updating visuals.
   */
  @Override
  protected void _update(AbstractPlayer player, AbstractGameState gameState) {
    playerNumber.setText("Player  " + currentlyObservedPlayer);

    parent.repaint();
  }
}
