package ocm.cookinglegend.app.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import ocm.cookinglegend.app.CL_GameRoom;
import ocm.cookinglegend.app.CL_PlayerSession;
import ocm.cookinglegend.domain.PlayerInfo;

import org.menacheri.jetserver.app.Game;
import org.menacheri.jetserver.app.GameRoom;
import org.menacheri.jetserver.app.Player;
import org.menacheri.jetserver.app.Session;
import org.menacheri.jetserver.app.impl.DefaultSession;
import org.menacheri.jetserver.concurrent.LaneStrategy;
import org.menacheri.jetserver.concurrent.LaneStrategy.LaneStrategies;
import org.menacheri.jetserver.event.Event;
import org.menacheri.jetserver.event.EventHandler;
import org.menacheri.jetserver.event.NetworkEvent;
import org.menacheri.jetserver.event.impl.EventDispatchers;
import org.menacheri.jetserver.event.impl.NetworkEventListener;
import org.menacheri.jetserver.protocols.Protocol;
import org.menacheri.jetserver.service.GameStateManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class CL_GameRoomSession extends DefaultSession implements CL_GameRoom
{
	private static final Logger LOG = LoggerFactory.getLogger(CL_GameRoomSession.class);
	
	/**
	 * The name of the game room, preferably unique across multiple games.
	 */
	protected String gameRoomName;
	/**
	 * The parent {@link SimpleGame} reference of this game room.
	 */
	protected Game parentGame;
	/**
	 * Each game room has separate state manager instances. This variable will
	 * manage the state for all the {@link CL_Player}s connected to this game room.
	 */
	protected GameStateManagerService stateManager;

	/**
	 * The set of sessions in this object.
	 */
	protected Set<CL_PlayerSession> sessions;
	
	/**
	 * Each game room has its own protocol for communication with client.
	 */
	protected Protocol protocol;
	
	protected CL_GameRoomSession(GameRoomSessionBuilder gameRoomSessionBuilder)
	{
		super(gameRoomSessionBuilder);
		this.sessions = gameRoomSessionBuilder.sessions;
		this.parentGame = gameRoomSessionBuilder.parentGame;
		this.gameRoomName = gameRoomSessionBuilder.gameRoomName;
		this.protocol = gameRoomSessionBuilder.protocol;
		if(null == gameRoomSessionBuilder.eventDispatcher)
		{
			this.eventDispatcher = EventDispatchers.newJetlangEventDispatcher(
					this, gameRoomSessionBuilder.laneStrategy);
		}
	}
	
	public static class GameRoomSessionBuilder extends SessionBuilder
	{
		protected Set<CL_PlayerSession> sessions;
		protected Game parentGame;
		protected String gameRoomName;
		protected Protocol protocol;
		protected LaneStrategy<String, ExecutorService, GameRoom> laneStrategy;
		
		@Override
		protected void validateAndSetValues()
		{
			if (null == id)
			{
				id = String.valueOf(SESSION_ID.incrementAndGet());
			}
			if(null == sessionAttributes)
			{
				sessionAttributes = new HashMap<String, Object>();
			}
			if (null == sessions)
			{
				sessions = new HashSet<CL_PlayerSession>();
			}
			if (null == laneStrategy)
			{
				laneStrategy = LaneStrategies.GROUP_BY_ROOM;
			}
			creationTime = System.currentTimeMillis();
		}
		
		public GameRoomSessionBuilder sessions(Set<CL_PlayerSession> sessions)
		{
			this.sessions = sessions;
			return this;
		}
		
		public GameRoomSessionBuilder parentGame(Game parentGame)
		{
			this.parentGame = parentGame;
			return this;
		}
		
		public GameRoomSessionBuilder gameRoomName(String gameRoomName)
		{
			this.gameRoomName = gameRoomName;
			return this;
		}
		
		public GameRoomSessionBuilder protocol(Protocol protocol)
		{
			this.protocol = protocol;
			return this;
		}
		
		public GameRoomSessionBuilder laneStrategy(
				LaneStrategy<String, ExecutorService, GameRoom> laneStrategy)
		{
			this.laneStrategy = laneStrategy;
			return this;
		}
	}
	
//	@Override
//	public CL_PlayerSession createCL_PlayerSession(Player player)
//	{
//		CL_PlayerSession playerSession = getSessionInstance(player);
//		return playerSession;
//	}
	
	@Override
	public CL_PlayerSession createCL_PlayerSession(PlayerInfo player)
	{
		CL_PlayerSession playerSession = getSessionInstance(player);
		return playerSession;
	}
	
	@Override
	public abstract void onLogin(CL_PlayerSession playerSession);
	
	@Override
	public synchronized boolean connectSession(CL_PlayerSession playerSession)
	{
		if (!isShuttingDown)
		{
			playerSession.setStatus(Session.Status.CONNECTING);
			sessions.add(playerSession);
			playerSession.setGameRoom(this);
			LOG.trace("Protocol to be applied is: {}",protocol.getClass().getName());
			protocol.applyProtocol(playerSession,true);
			createAndAddEventHandlers(playerSession);
			playerSession.setStatus(Session.Status.CONNECTED);
			afterSessionConnect(playerSession);
			return true;
			// TODO send event to all other sessions?
		}
		else
		{
			LOG.warn("Game Room is shutting down, playerSession {} {}",
					playerSession,"will not be connected!");
			return false;
		}
	}

	@Override
	public void afterSessionConnect(CL_PlayerSession playerSession)
	{
		//Only here we sent back the RPC result for success login?
		//and early results should only be failures ? Ok fine for that!
	}
	
	public synchronized boolean disconnectSession(CL_PlayerSession playerSession)
	{
		final boolean removeHandlers = this.eventDispatcher.removeHandlersForSession(playerSession);
		playerSession.getEventDispatcher().clear(); // remove network handlers of the session.
		return (removeHandlers && sessions.remove(playerSession));
	}

	@Override
	public void send(Event event) {
		onEvent(event);
	}
	
	@Override
	public void sendBroadcast(NetworkEvent networkEvent)
	{
		onEvent(networkEvent);
	}

	@Override
	public synchronized void close()
	{
		isShuttingDown = true;
		for(CL_PlayerSession session: sessions)
		{
			session.close();
		}
	}
	
	public CL_PlayerSession getSessionInstance(PlayerInfo player)
	{
		CL_PlayerSession playerSession = new PlayerSessionBuilder().parentGameRoom(this).player(player).build();
		return playerSession;
	}
	
	@Override
	public Set<CL_PlayerSession> getSessions()
	{
		return sessions;
	}

	@Override
	public void setSessions(Set<CL_PlayerSession> sessions)
	{
		this.sessions = sessions;
	}
	
	@Override
	public String getGameRoomName()
	{
		return gameRoomName;
	}

	@Override
	public void setGameRoomName(String gameRoomName)
	{
		this.gameRoomName = gameRoomName;
	}

	@Override
	public Game getParentGame()
	{
		return parentGame;
	}

	@Override
	public void setParentGame(Game parentGame)
	{
		this.parentGame = parentGame;
	}

	@Override
	public void setStateManager(GameStateManagerService stateManager)
	{
		this.stateManager = stateManager;
	}
	
	@Override
	public GameStateManagerService getStateManager()
	{
		return stateManager;
	}

	@Override
	public Protocol getProtocol()
	{
		return protocol;
	}

	@Override
	public void setProtocol(Protocol protocol)
	{
		this.protocol = protocol;
	}
	
	@Override
	public boolean isShuttingDown()
	{
		return isShuttingDown;
	}

	public void setShuttingDown(boolean isShuttingDown)
	{
		this.isShuttingDown = isShuttingDown;
	}

	/**
	 * Method which will create and add event handlers of the player session to
	 * the Game Room's EventDispatcher.
	 * 
	 * @param playerSession
	 *            The session for which the event handlers are created.
	 */
	protected void createAndAddEventHandlers(CL_PlayerSession playerSession)
	{
		// Create a network event listener for the player session.
		EventHandler networkEventHandler = new NetworkEventListener(playerSession);
		// Add the handler to the game room's EventDispatcher so that it will
		// pass game room network events to player session session.
		this.eventDispatcher.addHandler(networkEventHandler);
		LOG.trace("Added Network handler to "
				+ "EventDispatcher of GameRoom {}, for session: {}", this,
				playerSession);
	}
}
