package games.arknova.gui;

import games.arknova.ArkNovaGameState;
import javax.swing.*;

public class SidebarPanel extends JPanel {

  ArkNovaGameState gs;
  ArkNovaGUIManager gui;
  JLabel breakCounterLabel;

  PlayerOverviewPanel[] playerPanels;

  public SidebarPanel(ArkNovaGUIManager gui, ArkNovaGameState gs) {
    this.gs = gs;
    this.gui = gui;

    playerPanels = new PlayerOverviewPanel[gs.getNPlayers()];

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    breakCounterLabel = new JLabel("Break counter");

    add(breakCounterLabel);

    for (int i = 0; i < gs.getNPlayers(); i++) {
      playerPanels[i] = new PlayerOverviewPanel(gui, gs, i);
      this.add(playerPanels[i]);
    }

    this.update();
    revalidate();
  }

  public void update() {
    this.breakCounterLabel.setText(
        String.format(
            "Break: %s / %s",
            this.gs.getBreakCounter().getValue(), this.gs.getBreakCounter().getMaximum()));

    for (PlayerOverviewPanel playerPanel : playerPanels) {
      playerPanel.update();
    }
  }
}
