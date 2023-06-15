package core;

import core.actions.AbstractAction;
import core.actions.ActionSpace;
import core.actions.DoNothing;
import core.interfaces.IOrderedActionSpace;
import core.interfaces.IVectorisable;
import games.GameType;
import players.human.HumanGUIPlayer;
import players.python.PythonAgent;
import players.simple.RandomPlayer;
import utilities.ActionTreeNode;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;


public class GYMEnv {
    private Game game;
    // root of the action tree
    private ActionTreeNode root;
    // list of leaf nodes
    private List<ActionTreeNode> leaves;
    private AbstractGameState gameState;
    private AbstractForwardModel forwardModel;
    private List<AbstractPlayer> players;
    private int turnPause = 0;
    private int tick;
    private int lastPlayer; // used to track actions per 'turn'
    private List<AbstractAction> availableActions;

    boolean isNormalized; // Bool for whether you want obersvations to be normalized

    private Random seedRandom; // Random used for setting the seed for each episode
    private long lastSeed;


    public GYMEnv(GameType gameToPlay, String parameterConfigFile, List<AbstractPlayer> players, long seed, boolean isNormalized) throws Exception {

        // boolean randomizeParameters, List<IGameListener> listeners
        this.seedRandom = new Random(seed);
        this.isNormalized = isNormalized;
        this.players = players;
        // Creating game instance (null if not implemented)
        if (parameterConfigFile != null) {
            AbstractParameters params = AbstractParameters.createFromFile(gameToPlay, parameterConfigFile);
            this.game = gameToPlay.createGameInstance(players.size(), seed, params);
        } else game = gameToPlay.createGameInstance(players.size(), seed);

        assert game != null;
        game.getCoreParameters().actionSpace = new ActionSpace(ActionSpace.Structure.Tree);
        if (!(game.gameState instanceof IVectorisable && game.forwardModel instanceof IOrderedActionSpace)) {
            throw new Exception("Game has not implemented Reinforcement Learning Interface");
        }
//        if (game != null) {
//            if (listeners != null)
//                listeners.forEach(game::addListener);
//
//            // Randomize parameters
//            if (randomizeParameters) {
//                AbstractParameters gameParameters = game.getGameState().getGameParameters();
//                gameParameters.randomize();
//            }
//
//        }

    }

    // --Wrappers for interface functions--

    // Gets observations in JSON
    public String getObservationJson() throws Exception {
        if (gameState instanceof IVectorisable) {
            return ((IVectorisable) gameState).getObservationJson();
        }
        else throw new Exception("Function is not implemented");
    }

    // Gets the observation space as an integer
    public int getObservationSpace() throws Exception {
        if (gameState instanceof IVectorisable) {
            return ((IVectorisable) gameState).getObservationSpace();
        }
        else throw new Exception("Function is not implemented");
    }

    public double[] getObservationVector() throws Exception {
        AbstractGameState gs = gameState.copy(gameState.getCurrentPlayer());
        if (gs instanceof IVectorisable) {
            if (isNormalized) return ((IVectorisable) gs).getNormalizedObservationVector();
            else return ((IVectorisable) gs).getObservationVector();
        }
        else throw new Exception("Function is not implemented");
    }

    // Gets the action space as an integer
    public int getActionSpace() throws Exception {
        if (forwardModel instanceof IOrderedActionSpace) {
            return ((IOrderedActionSpace) forwardModel).getActionSpace();
        }
        else throw new Exception("Function is not implemented");
    }

    // Gets the actions as an integer array
    public int[] getFixedActionSpace() throws Exception {
        if (forwardModel instanceof IOrderedActionSpace) {
            return ((IOrderedActionSpace) forwardModel).getFixedActionSpace();
        }
        else throw new Exception("Function is not implemented");
    }

    // Gets the action mask as a boolean array
    public int[] getActionMask() throws Exception {
        if (forwardModel instanceof IOrderedActionSpace) {
            return ((IOrderedActionSpace) forwardModel).getActionMask(gameState);
        }
        else throw new Exception("Function is not implemented");
    }

    public String getActionMaskJson() throws Exception {
        if (forwardModel.root != null) {
            return forwardModel.root.toJsonString();
        }
        else throw new Exception("Game does not implement action trees");
    }

    public List<ActionTreeNode> getFlattenedTree() throws Exception {
        if (forwardModel.root != null) {
            return forwardModel.root.flattenTree();
        }
        else throw new Exception("Game does not implement action trees");
    }

    // Plays an action given an actionID
    public void executeAction(int actionID) throws Exception {
        if (forwardModel instanceof IOrderedActionSpace) {
            ActionTreeNode node = leaves.get(actionID);
            AbstractAction action = node.getAction();
            forwardModel.next(gameState, action);
        }
//            ((IOrderedActionSpace) forwardModel).nextPython(gameState, actionID);
//        }
        else throw new Exception("Function is not implemented");
    }

    // --End of Wrapper Functions--


    public void reset(){
        // Reset game instance, run built-in agents until a python agent is required to make a decision
        this.game.reset(players);
        this.turnPause = 0;
        this.tick = 0;
        this.game.setTurnPause(turnPause);
        this.gameState = game.getGameState();
        this.lastSeed = seedRandom.nextLong();
        gameState.gameParameters.setRandomSeed(this.lastSeed);
        this.forwardModel = game.getForwardModel();
        this.availableActions = forwardModel.computeAvailableActions(gameState);
        this.root = ((IOrderedActionSpace)this.forwardModel).generateActionTree(this.gameState);

        // execute the game if needed until Python agent is required to make a decision
        int activePlayer = gameState.getCurrentPlayer();
        AbstractPlayer currentPlayer = players.get(activePlayer);
        while ( !(currentPlayer instanceof PythonAgent)){
            AbstractGameState observation = gameState.copy(activePlayer);
            List<core.actions.AbstractAction> observedActions = forwardModel.computeAvailableActions(observation);

            if (isDone()){
                // game is over
                return;
            }

            // Start the timer for this decision
            gameState.playerTimer[activePlayer].resume();

            // Either ask player which action to use or, in case no actions are available, report the updated observation
            core.actions.AbstractAction action = null;
            if (observedActions.size() > 0) {
                if (observedActions.size() == 1 && (!(currentPlayer instanceof HumanGUIPlayer) || observedActions.get(0) instanceof DoNothing)) {
                    // Can only do 1 action, so do it.
                    action = observedActions.get(0);
                    currentPlayer.registerUpdatedObservation(observation);
                } else {
                    // Get action from player, and time it
                    action = currentPlayer.getAction(observation);
                }
            } else {
                currentPlayer.registerUpdatedObservation(observation);
            }

            // End the timer for this decision
            gameState.playerTimer[activePlayer].pause();
            gameState.playerTimer[activePlayer].incrementAction();

            if (gameState.coreGameParameters.verbose && !(action == null)) {
                System.out.println(action);
            }
            if (action == null)
                throw new AssertionError("We have a NULL action in the Game loop");

            // Check player timeout
            forwardModel.next(gameState, action);
            tick++;

            lastPlayer = activePlayer;
            activePlayer = gameState.getCurrentPlayer();
            currentPlayer = players.get(gameState.getCurrentPlayer());
        }
        AbstractGameState observation = gameState.copy(activePlayer);
        this.availableActions = forwardModel.computeAvailableActions(observation);
    }

    public int getPlayerID(){
        return gameState.getCurrentPlayer();
    }

    public boolean isDone(){
        return !gameState.isNotTerminal();
    }

    public double getReward(){
        return gameState.getGameScore(gameState.getCurrentPlayer());
    }

    public List<AbstractAction> getActions(){
        return availableActions;
    }


    public AbstractGameState step(int actionId) throws Exception{
        // execute action and loop until a PythonAgent is required to make a decision
        if (isDone()){
            throw new Exception("Need to reset the environment after each finished episode");
        } else if (this.gameState == null){
            throw new Exception("Need to reset the environment before calling step");
        }
//        AbstractAction a_ = this.availableActions.get(a);
//        forwardModel.next(gameState, a_);
        executeAction(actionId);
        if (isDone()){
            // check if the game has just ended
            // game is over
            return gameState.copy(gameState.getCurrentPlayer());
        }

        int activePlayer = gameState.getCurrentPlayer();
        AbstractPlayer currentPlayer = players.get(activePlayer);
        while ( !(currentPlayer instanceof PythonAgent)){
            AbstractGameState observation = gameState.copy(activePlayer);
            List<core.actions.AbstractAction> observedActions = forwardModel.computeAvailableActions(observation);

            if (isDone()){
                // game is over
                return observation;
            }

            // Start the timer for this decision
            gameState.playerTimer[activePlayer].resume();

            // Either ask player which action to use or, in case no actions are available, report the updated observation
            core.actions.AbstractAction action = null;
            if (observedActions.size() > 0) {
                if (observedActions.size() == 1 && (!(currentPlayer instanceof HumanGUIPlayer) || observedActions.get(0) instanceof DoNothing)) {
                    // Can only do 1 action, so do it.
                    action = observedActions.get(0);
                    currentPlayer.registerUpdatedObservation(observation);
                } else {
                    // Get action from player, and time it
                    action = currentPlayer.getAction(observation);
                }
            } else {
                currentPlayer.registerUpdatedObservation(observation);
            }

            // End the timer for this decision
            gameState.playerTimer[activePlayer].pause();
            gameState.playerTimer[activePlayer].incrementAction();

            if (gameState.coreGameParameters.verbose && !(action == null)) {
                System.out.println(action);
            }
            if (action == null)
                throw new AssertionError("We have a NULL action in the Game loop");

            // Check player timeout
            forwardModel.next(gameState, action);
            tick++;

            lastPlayer = activePlayer;
            activePlayer = gameState.getCurrentPlayer();
            currentPlayer = players.get(gameState.getCurrentPlayer());

        }
        AbstractGameState observation = gameState.copy(activePlayer);
        this.availableActions = forwardModel.computeAvailableActions(observation);

        return observation;
    }

    public int getTick(){
        return this.tick;
    }

    public long getSeed(){
        return this.lastSeed;
    }

    public List getTreeShape(){
        return this.forwardModel.root.getTreeShape();
    }

    public CoreConstants.GameResult[] getPlayerResults(){
        return this.gameState.getPlayerResults();
    }

    public int sampleRNDAction(int[] mask, Random rnd){
        /* take the action mask and sample a valid random action */
        int[] trueIdx = IntStream.range(0, mask.length)
                .filter(i -> mask[i] == 1)
                .toArray();
        int rndIdx = trueIdx[rnd.nextInt(trueIdx.length)];
        return rndIdx;
    }

    public static void main(String[] args) {
        Long seed = new Long(2466);
        Random rnd = new Random(seed);
        ArrayList<AbstractPlayer> players = new ArrayList<>();

        // set up players
//        players.add(new MCTSPlayer());
        players.add(new PythonAgent());
        players.add(new RandomPlayer(rnd));

        boolean useGYM = true;

        int wins = 0;
        int episodes = 0;

        boolean done = false;
        int MAX_EPISODES = 100;
        int steps = 0;

        try {
            // Initialise the game
            GYMEnv env = new GYMEnv(GameType.valueOf("Stratego"), null, players, 343, true);
            if (!useGYM) env.game.getCoreParameters().actionSpace = new ActionSpace(ActionSpace.Structure.Default);

            // reset is always required before starting a new episode
            env.reset();
            while (!done){

                if (useGYM){

                    // get action mask and sample random action
                    int randomAction = env.sampleRNDAction(env.getActionMask(), rnd);

                    // get observation vector
                    double[] obs = env.getObservationVector();

                    // step the environment
                    env.step(randomAction);
                } else {
                    // this is the normal game loop
                    List<AbstractAction> actions = env.forwardModel.computeAvailableActions(env.gameState);
                    int randomAction = rnd.nextInt(actions.size());
                    env.forwardModel.next(env.gameState, actions.get(randomAction));

                }

                // update stats
                steps += 1;
                done = env.isDone();
                if (done){
                    episodes += 1;
                    System.out.println("episodes " + episodes + " is done in " + steps + " ; outcome:  " + env.getPlayerResults()[0].value);
                    if (env.getPlayerResults()[0] == CoreConstants.GameResult.WIN_GAME)wins += 1;
                    if (episodes == MAX_EPISODES)break;
                    env.reset();
                    done = false;
                    steps = 0;
                }
            }
        } catch (Exception e){
            System.out.println("Exception in GYMEnv init" + e);
        }
        System.out.println("Run finished won " + wins + " out of " + episodes);


    }

}
