package games.arknova.gui;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import games.arknova.ArkNovaGameState;
import gui.AbstractGUIManager;
import gui.GamePanel;
import java.awt.*;
import java.util.Set;
import javax.swing.*;
import players.human.ActionController;

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

  protected int currentlyObservedPlayer;

  Image mapImage;

  ArkNovaMapView mapView;
  SidebarPanel sidebar;

  JLabel playerNumber;

  public ArkNovaGUIManager(GamePanel parent, Game game, ActionController ac, Set<Integer> human) {
    super(parent, game, ac, human);
    if (game == null) return;

    ArkNovaGameState gs = (ArkNovaGameState) game.getGameState();

    currentlyObservedPlayer = gs.getCurrentPlayer();
    parent.setLayout(new BorderLayout());

    // Top notification bar
    playerNumber = new JLabel();
    parent.add(playerNumber, BorderLayout.PAGE_START);

    // left map view
    parent.add(getMapPane(), BorderLayout.LINE_START);

    // right sidebar for player overview
    sidebar = new SidebarPanel(this, gs);
    parent.add(sidebar, BorderLayout.LINE_END);

    parent.revalidate();
    parent.setVisible(true);
    parent.repaint();
  }

  private JPanel getMapPane() {
    JPanel mapPane = new JPanel();
    mapPane.setOpaque(false);
    mapPane.setLayout(new FlowLayout());

    mapView = new ArkNovaMapView(this, (ArkNovaGameState) game.getGameState());
    mapPane.add(mapView);

    return mapPane;
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

  public void setCurrentlyObservedPlayer(int currentlyObservedPlayer) {
    this.currentlyObservedPlayer = currentlyObservedPlayer;
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

    sidebar.update();

    parent.repaint();
  }
}
