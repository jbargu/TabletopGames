package games.arknova.components;

import core.CoreConstants;
import core.components.Component;

import java.awt.*;
import java.util.HashMap;

public class ArkNovaMap extends Component {
    final static int WIDTH = 8;
    final static int HEIGHT = 5;
    // Mapping between axial coords and the underlying hex
    protected HashMap<HexTile, HexTile> grid;
    protected MapName mapName;

    public ArkNovaMap(MapName mapName) {
        super(CoreConstants.ComponentType.BOARD, "Map");
        this.mapName = mapName;

        grid = new HashMap<>();
        for (int q = 0; q <= WIDTH; q++) {
            int q_offset = (int) Math.floor((q + 1) / 2.0); // or q>>1
            for (int r = -q_offset; r <= HEIGHT - q_offset; r++) {
                grid.put(new HexTile(q, r), new HexTile(q, r));
            }

            if (q % 2 == 1) {
                int newR = HEIGHT - q_offset + 1;
                grid.put(new HexTile(q, newR), new HexTile(q, newR));
            }
        }
    }

    public HexTile pixelToHex(Point pixel, int hexSize) {
        double q = (2. / 3 * pixel.x) / hexSize;
        double r = (-1. / 3 * pixel.x + Math.sqrt(3) / 3 * pixel.y) / hexSize;

        HexTile roundedHex = axialHexRound(q, r);
        return grid.getOrDefault(roundedHex, null);
    }

    public HexTile axialHexRound(double q, double r) {
        double s = -q - r;
        int qi = (int) (Math.round(q));
        int ri = (int) (Math.round(r));
        int si = (int) (Math.round(s));

        double q_diff = Math.abs(qi - q);
        double r_diff = Math.abs(ri - r);
        double s_diff = Math.abs(si - s);

        if (q_diff > r_diff && q_diff > s_diff) {
            qi = -ri - si;
        } else if (r_diff > s_diff) {
            ri = -qi - si;
        } else {
            si = -qi - ri;
        }
        return new HexTile(qi, ri);
    }

    public HashMap<HexTile, HexTile> getGrid() {
        return grid;
    }

    @Override
    public Component copy() {
        return null;
    }

    public enum MapName {
        Map5("Park Restaurant");

        String fullName;

        MapName(String fullName) {
            this.fullName = fullName;
        }
    }
}
