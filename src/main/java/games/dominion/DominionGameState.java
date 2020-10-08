package games.dominion;

import core.*;
import core.actions.DrawCard;
import core.components.*;
import core.interfaces.IGamePhase;
import games.dominion.cards.*;
import utilities.Utils;

import java.util.*;

public class DominionGameState extends AbstractGameState {

    public enum DominionGamePhase implements IGamePhase {
        Play,
        Buy
    }

    Random rnd;
    int playerCount;

    // Counts of cards on the table should be fine
    Map<CardType, Integer> cardsAvailable;

    // Then Decks for each player - Hand, Discard and Draw
    // TODO: Convert these to use PartialObservableDecks
    Deck<DominionCard>[] playerHands;
    Deck<DominionCard>[] playerDrawPiles;
    Deck<DominionCard>[] playerDiscards;

    int buysLeftForCurrentPlayer = 0;
    int actionsLeftForCurrentPlayer = 0;
    int spentSoFar = 0;

    // Trash pile and other global decks
    Deck<DominionCard> trashPile;

    /**
     * Constructor. Initialises some generic game state variables.
     *
     * @param gameParameters - game parameters.
     * @param nPlayers       - number of players
     */
    public DominionGameState(AbstractParameters gameParameters, int nPlayers) {
        super(gameParameters, new DominionTurnOrder(nPlayers));
        rnd = new Random(gameParameters.getRandomSeed());
        playerCount = nPlayers;
        _reset();
    }

    public boolean removeCardFromTable(CardType type) {
        if (cardsAvailable.getOrDefault(type, 0) > 0) {
            cardsAvailable.put(type, cardsAvailable.get(type));
        }
        return false;
    }

    public void endOfTurnCleanUp(int playerID) {
        if (playerID != getCurrentPlayer())
            throw new AssertionError("Not yet supported");
        // 1) put hand and cards played into discard
        // 2) draw 5 new cards
        // 3) shuffle and move discard if we run out
        Deck<DominionCard> hand = playerHands[playerID];
        Deck<DominionCard> discard = playerDiscards[playerID];
        Deck<DominionCard> draw = playerDrawPiles[playerID];

        discard.add(hand);
        hand.clear();
        for (int i = 0 ; i < 5; i++) {
            if (draw.getSize() == 0) {
                draw.add(discard);
                discard.clear();
            }
            hand.add(draw.draw());
        }
        actionsLeftForCurrentPlayer = 1;
        spentSoFar = 0;
        buysLeftForCurrentPlayer = 1;
    }

    public boolean gameOver() {
        return cardsAvailable.get(CardType.PROVINCE) == 0 ||
                cardsAvailable.values().stream().filter(i -> i == 0).count() >= 3;
    }

    public int actionsLeft() {return actionsLeftForCurrentPlayer;}
    public void changeActions(int delta) {actionsLeftForCurrentPlayer += delta;}
    public int buysLeft() {return buysLeftForCurrentPlayer;}
    public void changeBuys(int delta) {buysLeftForCurrentPlayer += delta;}
    public void spend(int delta) {spentSoFar += delta;}
    public int availableSpend(int playerID) {
        if (playerID != getCurrentPlayer())
            throw new AssertionError("Not yet supported");
        int totalTreasureInHand = (int) Utils.summariseDeck(playerHands[playerID], DominionCard::treasureValue);
        return totalTreasureInHand - spentSoFar;
    }

    /**
     * Returns all components used in the game and referred to by componentId from actions or rules.
     * This method is called after initialising the game state.
     *
     * @return - List of components in the game.
     */
    @Override
    protected List<Component> _getAllComponents() {
        List<Component> components = new ArrayList<>();
        components.addAll(Arrays.asList(playerHands));
        components.addAll(Arrays.asList(playerDiscards));
        components.addAll(Arrays.asList(playerDrawPiles));
        components.add(trashPile);
        return components;
    }

    /**
     * Create a copy of the game state containing only those components the given player can observe (if partial
     * observable).
     *
     * @param playerId - player observing this game state.
     */
    @Override
    protected AbstractGameState _copy(int playerId) {
        DominionGameState retValue = new DominionGameState(gameParameters.copy(), playerCount);
        for (CardType ct : cardsAvailable.keySet()) {
            retValue.cardsAvailable.put(ct, cardsAvailable.get(ct));
        }
        for (int p = 0; p < playerCount; p++) {
            retValue.playerHands[p] = playerHands[p].copy();
            retValue.playerDrawPiles[p] = playerDrawPiles[p].copy();
            retValue.playerDiscards[p] = playerDiscards[p].copy();
        }
        retValue.trashPile = trashPile.copy();
        retValue.buysLeftForCurrentPlayer = buysLeftForCurrentPlayer;
        retValue.actionsLeftForCurrentPlayer = actionsLeftForCurrentPlayer;
        retValue.spentSoFar = spentSoFar;

        return retValue;
    }

    /**
     * Provide a simple numerical assessment of the current game state, the bigger the better.
     * Subjective heuristic function definition.
     *
     * @param playerId - player observing the state.
     * @return - double, score of current state.
     */
    @Override
    protected double _getScore(int playerId) {
        double score = Utils.summariseDeck(playerDiscards[playerId], DominionCard::victoryPoints);
        score += Utils.summariseDeck(playerDrawPiles[playerId], DominionCard::victoryPoints);
        score += Utils.summariseDeck(playerHands[playerId], DominionCard::victoryPoints);
        return score;
    }


    /**
     * Provide a list of component IDs which are hidden in partially observable copies of games.
     * Depending on the game, in the copies these might be completely missing, or just randomized.
     *
     * @param playerId - ID of player observing the state.
     * @return - list of component IDs unobservable by the given player.
     */
    @Override
    protected ArrayList<Integer> _getUnknownComponentsIds(int playerId) {
        return new ArrayList<>();
    }

    /**
     * Resets variables initialised for this game state.
     */
    @Override
    protected void _reset() {
        playerHands = new Deck[playerCount];
        playerDrawPiles = new Deck[playerCount];
        playerDiscards = new Deck[playerCount];
        trashPile = new Deck("Trash");
        for (int i = 0; i < playerCount; i++) {
            playerHands[i] = new Deck<>("Hand of Player " + i + 1);
            playerDrawPiles[i]= new Deck<>("Drawpile of Player " + i+1);
            playerDiscards[i] = new Deck<>("Discard of Player " + i+1);
            for (int j = 0; j < 5; j++) {
                playerDrawPiles[i].add(DominionCard.create(CardType.COPPER));
                playerDrawPiles[i].add(DominionCard.create(CardType.ESTATE));
            }
            playerDrawPiles[i].shuffle(rnd);
            for (int k = 0; k < 5; k++) playerHands[i].add(playerDrawPiles[i].draw());
        }
        actionsLeftForCurrentPlayer = 0;
        buysLeftForCurrentPlayer = 0;
        spentSoFar = 0;

        cardsAvailable = new HashMap<>(16);
        cardsAvailable.put(CardType.PROVINCE, 12);
        cardsAvailable.put(CardType.DUCHY, 12);
        cardsAvailable.put(CardType.ESTATE, 12);
        cardsAvailable.put(CardType.GOLD, 1000);
        cardsAvailable.put(CardType.SILVER, 1000);
        cardsAvailable.put(CardType.COPPER, 1000);
        DominionParameters params = (DominionParameters) gameParameters;
        for (CardType ct : params.cardsUsed.keySet()) {
            cardsAvailable.put(ct, params.cardsUsed.get(ct));
        }
    }

    /**
     * Checks if the given object is the same as the current.
     *
     * @param o - other object to test equals for.
     * @return true if the two objects are equal, false otherwise
     */
    @Override
    protected boolean _equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DominionGameState)) return false;
        DominionGameState other = (DominionGameState) o;
        return cardsAvailable.equals(other.cardsAvailable) &&
                Arrays.equals(playerHands, other.playerHands) &&
                Arrays.equals(playerDiscards, other.playerDiscards) &&
                Arrays.equals(playerDrawPiles, other.playerDrawPiles) &&
                trashPile.equals(other.trashPile) &&
                buysLeftForCurrentPlayer == other.buysLeftForCurrentPlayer &&
                actionsLeftForCurrentPlayer == other.actionsLeftForCurrentPlayer &&
                spentSoFar == other.spentSoFar;
    }
}
