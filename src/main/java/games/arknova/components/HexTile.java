package games.arknova.components;


import java.util.Objects;

public class HexTile {

    public int q;

    public int r;

    public HexTile(int q, int r) {
        this.q = q;
        this.r = r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HexTile hexTile = (HexTile) o;
        return q == hexTile.q && r == hexTile.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }

    @Override
    public String toString() {
        return "HexTile{" +
                "q=" + q +
                ", r=" + r +
                '}';
    }
}
