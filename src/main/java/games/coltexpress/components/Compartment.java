package games.coltexpress.components;

import core.components.Component;
import core.components.PartialObservableDeck;
import utilities.Utils;

import java.util.HashSet;
import java.util.Set;


public class Compartment extends Component {
    public Set<Integer> playersInsideCompartment = new HashSet<>();
    public Set<Integer> playersOnTopOfCompartment = new HashSet<>();

    public boolean containsMarshal = false;

    public PartialObservableDeck<Loot> getLootInside() {
        return lootInside;
    }

    public PartialObservableDeck<Loot> getLootOnTop() {
        return lootOnTop;
    }

    public PartialObservableDeck<Loot> lootInside;
    public PartialObservableDeck<Loot> lootOnTop;
    public final int id;
    private final int nPlayers;

    enum CompartmentType {
        COMPARTMENT_1,
        COMPARTMENT_2,
        COMPARTMENT_3,
        COMPARTMENT_4,
        COMPARTMENT_5,
        COMPARTMENT_6,
    }

    private Compartment(int nPlayers, int id){
        super(Utils.ComponentType.BOARD_NODE);
        this.lootInside = new PartialObservableDeck<>("lootInside", nPlayers);
        this.lootOnTop = new PartialObservableDeck<>("lootOntop", nPlayers);
        this.id = id;
        this.nPlayers = nPlayers;
    }

    Compartment(Train train, int nPlayers, int id, CompartmentType type){
        this(nPlayers, id);

        switch (type){
            case COMPARTMENT_1:
                lootInside.add(train.getRandomPurse());
                break;
            case COMPARTMENT_2:
                lootInside.add(train.getRandomPurse());
                lootInside.add(train.getRandomPurse());
                break;
            case COMPARTMENT_3:
                lootInside.add(train.getRandomPurse());
                lootInside.add(train.getRandomPurse());
                lootInside.add(train.getRandomPurse());
                break;
            case COMPARTMENT_4:
                lootInside.add(train.getRandomPurse());
                lootInside.add(Loot.createJewel());
                break;
            case COMPARTMENT_5:
                lootInside.add(train.getRandomPurse());
                lootInside.add(train.getRandomPurse());
                lootInside.add(train.getRandomPurse());
                lootInside.add(train.getRandomPurse());
                lootInside.add(Loot.createJewel());
                break;
            case COMPARTMENT_6:
                lootInside.add(Loot.createJewel());
                lootInside.add(Loot.createJewel());
                lootInside.add(Loot.createJewel());
                break;
            default:
                throw new IllegalArgumentException("CompartmentType " + type + " not defined");
        }
    }

    public boolean containsPlayer(int playerID) {
        if (playersInsideCompartment.contains(playerID))
            return true;
        return playersOnTopOfCompartment.contains(playerID);
    }

    static Compartment createLocomotive(int nPlayers, int id){
        Compartment locomotive = new Compartment(nPlayers, id);
        locomotive.lootInside.add(new Loot(Loot.LootType.Strongbox, 1000));
        locomotive.containsMarshal = true;
        return locomotive;
    }

    public void addPlayerInside(int playerID){
        playersInsideCompartment.add(playerID);
    }

    public void removePlayerInside(int playerID){
        playersInsideCompartment.remove(playerID);
    }

    public void addPlayerOnTop(int playerID){
        playersOnTopOfCompartment.add(playerID);
    }

    public void removePlayerOnTop(int playerID){
        playersOnTopOfCompartment.remove(playerID);
    }

    @Override
    public Component copy() {
        Compartment newCompartment = new Compartment(this.nPlayers, this.id);
        for (Loot loot : this.lootInside.getComponents())
            newCompartment.lootInside.add((Loot) loot.copy());
        for (Loot loot : this.lootOnTop.getComponents())
            newCompartment.lootOnTop.add((Loot) loot.copy());
        return newCompartment;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Compartment: Inside=");
        sb.append(playersInsideCompartment.toString());
        sb.append("; Outside=");
        sb.append(playersOnTopOfCompartment.toString());
        sb.append("; Marshal=");
        sb.append(containsMarshal);
        sb.append("; LootInside=");
        sb.append(lootInside.toString());
        sb.append("; LootOntop=");
        sb.append(lootOnTop.toString());

        return sb.toString();
    }
}