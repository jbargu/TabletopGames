package games.explodingkittens;

import core.AbstractGameState;
import core.AbstractForwardModel;
import core.AbstractGame;
import players.RandomPlayer;
import core.AbstractPlayer;

import java.util.*;

public class ExplodingKittensGame extends AbstractGame {

    public ExplodingKittensGame(List<AbstractPlayer> agents, AbstractForwardModel model, AbstractGameState gameState) {
        super(agents, model, gameState);
    }

    public static void main(String[] args){
        ArrayList<AbstractPlayer> agents = new ArrayList<>();
        agents.add(new RandomPlayer());
        agents.add(new RandomPlayer());
        agents.add(new RandomPlayer());
        agents.add(new RandomPlayer());

        ExplodingKittenParameters params = new ExplodingKittenParameters();
        AbstractForwardModel forwardModel = new ExplodingKittensForwardModel();

        for (int i=0; i<1000; i++) {
            AbstractGame game = new ExplodingKittensGame(agents, forwardModel, new ExplodingKittensGameState(params, forwardModel, agents.size()));
            game.run(null);
            ExplodingKittensGameState gameState = (ExplodingKittensGameState) game.getGameState();

            gameState.print((ExplodingKittenTurnOrder) gameState.getTurnOrder());
            // ((IPrintable) gameState.getObservation(null)).PrintToConsole();
            System.out.println(Arrays.toString(gameState.getPlayerResults()));

            for (int j = 0; j < gameState.getNPlayers(); j++){
                System.out.println("Player " + j + ": " + gameState.getPlayerResults()[j]);
            }
        }
    }
}