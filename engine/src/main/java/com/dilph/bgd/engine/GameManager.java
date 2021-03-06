package com.dilph.bgd.engine;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pseudo
 * Date: 8/3/13
 * Time: 11:12 AM
 * To change this template use File | Settings | File Templates.
 */


public class GameManager {


    Turn  turn;
    private ArrayList<Player> players;
    private Player currentPlayer;
    private int currentPlayerIndex;

    public void start()
    {
        CounterManager.getInstance().put("draws", new Counter(true,0));


        GameAction lastActions = new GameAction("- Discard to 7 cards \n- Infect cities", new EndTurnEvent());
        Decision isSecondDrawDecision = new Decision(new CounterCondition("draws", CounterCondition.Condition.GTE,2));
        GameAction pull1cardAction  = new GameAction("- Draw 1 card from the player deck",
                                                          new Decision("Is Epidemic?",
                                                                    new GameAction("- Raise Inf. Rate \n- Draw bottom IC \n- Shuffle Deck",isSecondDrawDecision),
                                                                  isSecondDrawDecision),
                                                          new CounterAction("draws", CounterAction.Action.INCREMENT)

                                                       );


        isSecondDrawDecision.setTrueTurnEvent(lastActions);
        isSecondDrawDecision.setFalseTurnEvent(pull1cardAction);

        turn = new Turn( new GameAction("Do 4 Actions", pull1cardAction) );
        turn.start();
    }

    public void proceed()
    {
        turn.next(false);
    }

    public void response(boolean response)
    {
        turn.next(response);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public TurnEvent getCurrentEvent()
    {
        TurnEvent current = turn.getCurrentTurnEvent();


        if(current == null)  // Means next turn
        {
            turn.reset();
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            currentPlayer = players.get(currentPlayerIndex);

        }

        while(current instanceof Decision && !((Decision)current).requiresHumanInteraction())
        {
            turn.next(false);
            current = turn.getCurrentTurnEvent();
        }


        return turn.getCurrentTurnEvent();
    }

    public void endTurn() {
        turn.reset();
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        currentPlayer = players.get(currentPlayerIndex);
    }

    public void setPlayers(Set<String> pls)
    {
        players = new ArrayList<Player>();
        for(String pl: pls)
        {
            players.add(new Player(pl));
        }
        Collections.reverse(players);
        currentPlayer = players.get(0);
    }
}
