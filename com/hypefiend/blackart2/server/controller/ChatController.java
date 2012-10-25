package com.hypefiend.blackart2.server.controller;

import com.hypefiend.blackart2.common.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * ChatController.java
 *
 * todo: finish chat controller and a client
 *
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public class ChatController extends GameController {
    protected Hashtable players;

    /** 
     * do ChatController specific initialization here 
     */
    public void initController(GameConfig gc) {
	log.info("initController");
	players = new Hashtable();
    }

    public String getGameName() {
	return "Chat";
    }

    /**
     * use the default Player class
     */
    public Player createPlayer() {
	Player p = new PlayerDefault();
	p.setSessionId(gameServer.nextSessionId());
	return p;
    }

    /**
     * use the default GameEvent class
     */
    public GameEvent createGameEvent() {
	return new GameEventDefault();
    }

    /** 
     * process events as they pop out of the Q 
     */
    public void processEvent(GameEvent e) {
	switch (e.getType()) {
	case GameEventDefault.C_LOGIN:
	    login(e);
	    break;
	case GameEventDefault.C_LOGOUT:
	    logout(e);
	    break;
	case GameEventDefault.C_JOIN_GAME:
	    join(e);
	    break;
	case GameEventDefault.C_QUIT_GAME:
	    quit(e);
	    break;
	case GameEventDefault.C_CHAT_MSG:
	    chat(e);
	}
    }

    protected void login(GameEvent e) {
	String pid = e.getPlayerId();
	
	Player p = gameServer.getPlayerById(pid);
	if (p == null) {
	    log.error("got login event for null player");
	    return;
	}
	
	if (p.loggedIn())
	    log.warn("got login event for already logged in player: " + pid);
	
	p.setLoggedIn(true);

	// add to our list
	players.put(pid, p);
	
	// send ACK to player
	GameEventDefault la = new GameEventDefault(GameEventDefault.S_LOGIN_ACK_OK);
	sendEvent(la, p);
	
	log.info("login(), player: " + p.hashCode() + ", channel: " + p.getChannel());	
    }

    protected void logout(GameEvent e) {
	//todo:!!!
    }

    protected void join(GameEvent e) {
    }
    protected void quit(GameEvent e) {
    }

    protected void chat(GameEvent e) {
	e.setType(GameEventDefault.SB_CHAT_MSG);
	e.setMessage(e.getPlayerId() + " says: " + e.getMessage());
	sendBroadcastEvent(e, players.values());
    }

}
