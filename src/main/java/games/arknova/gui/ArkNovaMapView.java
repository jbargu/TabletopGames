package games.arknova.gui;

import games.arknova.ArkNovaGameState;
import games.arknova.components.ArkNovaMap;
import games.arknova.components.HexTile;
import utilities.ImageIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static games.terraformingmars.gui.Utils.drawImage;

public class ArkNovaMapView extends JComponent {

    final Point HEX_GRID_OFFSET = new Point(270, 130);
    final Point MAP_SIZE = new Point(1500, 1500);
    final int HEX_TILE_SIZE = 50;
    Set<String> set = Stream.of("a", "b").collect(Collectors.toSet());
    ArkNovaGameState gs;
    Image mapImage;

    HexTile selectedHex;

    public ArkNovaMapView(ArkNovaGUIManager gui, ArkNovaGameState gs) {
        this.gs = gs;

        mapImage = ImageIO.GetInstance().getImage("data/arknova/Map5.png");

        System.out.println("ArkNovaMap");


        int currPlayer = gs.getCurrentPlayer();
        ArkNovaMap map = gs.getMaps()[currPlayer];

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Point pixel = e.getPoint();
                    pixel.translate(-HEX_GRID_OFFSET.x, -HEX_GRID_OFFSET.y);
                    selectedHex = map.pixelToHex(pixel, HEX_TILE_SIZE);
                }
            }
        });

    }


    public Dimension getPreferredSize() {
        return new Dimension(MAP_SIZE.x, MAP_SIZE.y);
    }

    @Override
    protected void paintComponent(Graphics g_base) {
        Graphics2D g = (Graphics2D) g_base.create();
        drawImage(g, mapImage, 0, 0, MAP_SIZE.y);
//        g2d.setColor(getBackground());
//        g2d.fillRect(0, 0, getWidth(), getHeight());
//        g2d.dispose();


        ArkNovaMap map = gs.getMaps()[0];

        if (ArkNovaGUIManager.DEBUG) {
            for (HexTile hexTile : map.getGrid().values()) {
                double center_x = HEX_TILE_SIZE * (3. / 2 * hexTile.q);
                double center_y = HEX_TILE_SIZE * (Math.sqrt(3) / 2 * hexTile.q + Math.sqrt(3) * hexTile.r);

                Polygon h = new Polygon();
                for (int i = 0; i < 6; i++) {
                    double angle = 2.0 * Math.PI * i / 6;
                    double offset_x = HEX_TILE_SIZE * Math.cos(angle);
                    double offset_y = HEX_TILE_SIZE * Math.sin(angle);

                    int hex_x = (int) (HEX_GRID_OFFSET.x + center_x + offset_x);
                    int hex_y = (int) (HEX_GRID_OFFSET.y + center_y + offset_y);

                    h.addPoint(hex_x, hex_y);

                }


                if (hexTile.equals(selectedHex)) {
                    g.setColor(new Color(0, 255, 0, 50));
                    g.fillPolygon(h);
                }

                g.setColor(Color.BLACK);
                g.drawPolygon(h);


                g.setStroke(new BasicStroke(5));
                g.setFont(new Font("Arial", Font.BOLD, 15));
                g.drawString(Integer.toString(hexTile.q) + ", " + Integer.toString(hexTile.r),
                        (int) center_x + HEX_GRID_OFFSET.x, (int) center_y + HEX_GRID_OFFSET.y);
            }
        }


        super.paintComponent(g);
    }
}
