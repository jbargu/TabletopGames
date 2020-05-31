package games.coltexpress.components;

import core.components.Component;
import utilities.Utils;

import java.util.*;

public class Train extends Component {

    private final LinkedList<Compartment> compartments = new LinkedList<>();
    private final LinkedList<Integer> remainingPurseValues;

    public Train(int nPlayers){
        super(Utils.ComponentType.BOARD);
        remainingPurseValues = new LinkedList<>(
                Arrays.asList(300,300,350,350,400,400,450,450,500,500));
        for (int i = 0; i < 8-nPlayers; i++){
            remainingPurseValues.add(250);
        }

        LinkedList<Compartment.CompartmentType> availableCompartments = new LinkedList<>();
        Collections.addAll(availableCompartments, Compartment.CompartmentType.values());

        Random random = new Random();
        for (int i = 0; i < nPlayers; i++) {
            Compartment.CompartmentType type = availableCompartments.remove(random.nextInt(availableCompartments.size()));
            compartments.add(new Compartment(this, nPlayers, i, type));
        }

        compartments.add(Compartment.createLocomotive(nPlayers, nPlayers));
    }

    public Train(Train original){
        super(Utils.ComponentType.BOARD);
        // todo implement copy constructor
        throw new UnsupportedOperationException();
    }

    public int getSize(){
        return compartments.size();
    }

    @Override
    public Component copy() {
        throw new UnsupportedOperationException("not implemented yet");
        //return new Train(this);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Train:\n");
        for (Compartment compartment : compartments)
        {
            sb.append(compartment.toString());
            sb.append("\n");
        }
        sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }

    public List<Compartment> getCompartments(){
        return compartments;
    }

    public Compartment getCompartment(int compartmentIndex){
        if (compartmentIndex < compartments.size() && compartmentIndex >= 0){
            return compartments.get(compartmentIndex);
        }
        throw new IllegalArgumentException("compartmentIndex out of bounds");
    }

    public Loot getRandomPurse(){
        return new Loot(Loot.LootType.Purse, remainingPurseValues.remove(
                new Random().nextInt(remainingPurseValues.size())));
    }
}