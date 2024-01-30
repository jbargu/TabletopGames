package games.arknova.gui;

import games.arknova.ArkNovaConstants;
import games.arknova.ArkNovaGameState;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import utilities.ImageIO;

/** A panel with player's resources and icons. */
public class PlayerOverviewPanel extends JPanel {

  ArkNovaGameState gs;
  ArkNovaGUIManager gui;

  int playerId;

  JLabel scoreLabel;
  Map<ArkNovaConstants.Icon, JLabel> iconLabels;
  Map<ArkNovaConstants.Resource, JLabel> resourceLabels;

  public PlayerOverviewPanel(ArkNovaGUIManager gui, ArkNovaGameState gs, int playerId) {
    this.gs = gs;
    this.gui = gui;
    this.playerId = playerId;

    this.setBorder(BorderFactory.createTitledBorder(String.format("Player %d", playerId)));

    scoreLabel = new JLabel();

    JPanel resourcesPanel = createResourcesPanel();
    JPanel iconsPanel = createIconsPanel();

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    add(scoreLabel);
    add(resourcesPanel);
    add(iconsPanel);

    // Set to initial values
    update();
  }

  private JPanel createResourcesPanel() {
    final JPanel resourcesPanel = new JPanel();
    resourcesPanel.setLayout(new BoxLayout(resourcesPanel, BoxLayout.Y_AXIS));

    resourceLabels = new HashMap<>();
    for (ArkNovaConstants.Resource resource : ArkNovaConstants.Resource.values()) {
      JLabel resourceLabel =
          new JLabel(
              String.format(
                  "%s: %d",
                  resource.name(), gs.getPlayerResources()[playerId].get(resource).getValue()));
      resourceLabels.put(resource, resourceLabel);
      resourcesPanel.add(resourceLabel);
    }
    return resourcesPanel;
  }

  private JPanel createIconsPanel() {
    iconLabels = new HashMap<>();

    // Create 3x5 grid
    JPanel iconsPanel = new JPanel();
    iconsPanel.setLayout(new GridLayout(3, 5));

    for (ArkNovaConstants.Icon icon : ArkNovaConstants.Icon.values()) {
      Image iconImage =
          ImageIO.GetInstance()
              .getImage(icon.getImagePath())
              .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      JLabel iconLabel = new JLabel(new ImageIcon(iconImage));
      iconLabel.setText(String.valueOf(gs.getPlayerIcons()[playerId].get(icon).getValue()));
      iconLabels.put(icon, iconLabel);
      iconsPanel.add(iconLabel);
    }
    return iconsPanel;
  }

  public void update() {
    scoreLabel.setText(String.format("Score: %d", (int) gs.getGameScore(playerId)));

    for (Map.Entry<ArkNovaConstants.Resource, JLabel> entry : resourceLabels.entrySet()) {
      ArkNovaConstants.Resource resource = entry.getKey();
      JLabel label = entry.getValue();

      int value = gs.getPlayerResources()[playerId].get(resource).getValue();
      SwingUtilities.invokeLater(
          () ->
              label.setText(
                  String.format(
                      "%s: %d",
                      resource.name(),
                      gs.getPlayerResources()[playerId].get(resource).getValue())));
    }

    for (Map.Entry<ArkNovaConstants.Icon, JLabel> entry : iconLabels.entrySet()) {
      ArkNovaConstants.Icon icon = entry.getKey();
      JLabel label = entry.getValue();

      int value = gs.getPlayerIcons()[playerId].get(icon).getValue();
      SwingUtilities.invokeLater(() -> label.setText(String.valueOf(value)));
    }
  }
}
