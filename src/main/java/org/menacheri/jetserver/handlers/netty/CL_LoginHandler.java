package org.menacheri.jetserver.handlers.netty;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import ocm.cookinglegend.app.CL_Player;
import ocm.cookinglegend.domain.PlayerInfo;
import ocm.cookinglegend.handlers.CL_LoginProtocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.menacheri.jetserver.app.GameRoom;
import org.menacheri.jetserver.app.Player;
import org.menacheri.jetserver.app.PlayerSession;
import org.menacheri.jetserver.communication.NettyTCPMessageSender;
import org.menacheri.jetserver.event.Events;
import org.menacheri.jetserver.server.netty.AbstractNettyServer;
import org.menacheri.jetserver.service.LookupService;
import org.menacheri.jetserver.service.SessionRegistryService;
import org.menacheri.jetserver.util.NettyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Sharable
public class CL_LoginHandler extends SimpleChannelUpstreamHandler
{
	private static final Logger LOG = LoggerFactory
			.getLogger(LoginHandler.class);

	protected LookupService lookupService;
	protected SessionRegistryService sessionRegistryService;
	/**
	 * Used for book keeping purpose. It will count all open channels. Currently
	 * closed channels will not lead to a decrement.
	 */
	private static final AtomicInteger CHANNEL_COUNTER =  new AtomicInteger(0);

	private static final String PLAYER_SESSION_REFKEY = "playerSessions";
	
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e)
			throws Exception
	{
		/**TODO:
		 * For the login scenes, there may exist previous PVP sessions,
		 * this may be handled by the handleGameROomJoin function
		 * 
		 * */
		ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
		ByteArrayInputStream in = new ByteArrayInputStream(buffer.array());
		Channel channel = e.getChannel();
		
		PlayerInfo playerInfo = new CL_LoginProtocol().decodePlayer(in);
		if (playerInfo !=null) {
			//Ugly wrapper
			Player player = new CL_Player();
			player.setPlayerInfo(playerInfo);
			handleGameRoomJoin(player, channel);						
		}
		else{
			//FIXME: Gather some statistics here				
			LOG.error("Failed login|signIn request {} sent from remote address {}. "
					+ "Going to close channel {}",
					new Object[] { buffer.toString(), channel.getRemoteAddress(),
							channel.getId()});
			closeChannelWithLoginFailure(channel);
			
		}
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		AbstractNettyServer.ALL_CHANNELS.add(e.getChannel());
		LOG.debug("Added Channel with id: {} as the {}th open channel", e
				.getChannel().getId(), CHANNEL_COUNTER.incrementAndGet());
	}
	
//	public Player lookupPlayer(final ChannelBuffer buffer, final Channel channel)
//	{
//		Credentials credentials = new SimpleCredentials(buffer);
//		Player player = lookupService.playerLookup(credentials);
//		if(null == player){
//			LOG.error("Invalid credentials provided by user: {}",credentials);
//		}
//		return player;
//	}
	
//	public void handleResponse(Player player,Channel channel)
//	{
//		if (null != player)
//		{
//			channel.write(NettyUtils
//					.createBufferForOpcode(Events.LOG_IN_SUCCESS));
//		}
//		else
//		{
//			// Write future and close channel
//			closeChannelWithLoginFailure(channel);
//		}
//	}
	
	/**
	 * Helper method which will close the channel after writing
	 * {@link Events#LOG_IN_FAILURE} to remote connection.
	 * 
	 * @param channel
	 *            The tcp connection to remote machine that will be closed.
	 */
	private void closeChannelWithLoginFailure(Channel channel)
	{
		ChannelFuture future = channel.write(NettyUtils
				.createBufferForOpcode(Events.LOG_IN_FAILURE));
		future.addListener(ChannelFutureListener.CLOSE);
	}
	
	public void handleGameRoomJoin(Player player, Channel channel)
	{
		//FIXME: the service name should be encoded in the protocol
		//		String refKey = NettyUtils.readString(buffer);
		String refKey = PLAYER_SESSION_REFKEY;
		//Game room can be some service names here grouped by the refKey;
		GameRoom gameRoom = lookupService.gameRoomLookup(refKey);
		if(null != gameRoom)
		{
			PlayerSession playerSession = gameRoom.createPlayerSession(player);
			gameRoom.onLogin(playerSession);
			LOG.trace("Sending GAME_ROOM_JOIN_SUCCESS to channel {}", channel.getId());
			ChannelFuture future = channel.write(NettyUtils.createBufferForOpcode(Events.GAME_ROOM_JOIN_SUCCESS));
			connectToGameRoom(gameRoom, playerSession, future);
			//TODO: NO Udp currently
			//  loginUdp(playerSession, buffer);
		}
		else
		{
			// Write failure and close channel.
			ChannelFuture future = channel.write(NettyUtils.createBufferForOpcode(Events.GAME_ROOM_JOIN_FAILURE));
			future.addListener(ChannelFutureListener.CLOSE);
			LOG.error("Invalid ref key provided by client: {}. Channel {} will be closed",refKey,channel.getId());
		}
	}
	
	public void connectToGameRoom(final GameRoom gameRoom, final PlayerSession playerSession, ChannelFuture future)
	{
		future.addListener(new ChannelFutureListener()
		{
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception
			{
				Channel channel = future.getChannel();
				LOG.trace("Sending GAME_ROOM_JOIN_SUCCESS to channel {} completed", channel.getId());
				if (future.isSuccess())
				{
					LOG.trace("Going to clear pipeline");
					// Clear the existing pipeline
					NettyUtils.clearPipeline(channel.getPipeline());
					// Set the tcp channel on the session. 
					NettyTCPMessageSender sender = new NettyTCPMessageSender(channel);
					playerSession.setTcpSender(sender);
					// Added by wuxueming for easy multi-login look-up.
					playerSession.setId(playerSession.getPlayer().getId());
					// Connect the pipeline to the game room.
					gameRoom.connectSession(playerSession);
					// Send the connect event so that it will in turn send the START event.
					playerSession.onEvent(Events.connectEvent(sender));
				}
				else
				{
					LOG.error("GAME_ROOM_JOIN_SUCCESS message sending to client was failure, channel will be closed");
					channel.close();
				}
			}
		});
	}
	
	/**
	 * This method adds the player session to the
	 * {@link SessionRegistryService}. The key being the remote udp address of
	 * the client and the session being the value.
	 * 
	 * @param playerSession
	 * @param buffer
	 *            Used to read the remote address of the client which is
	 *            attempting to connect via udp.
	 */
	protected void loginUdp(PlayerSession playerSession, ChannelBuffer buffer)
	{
		InetSocketAddress remoteAdress = NettyUtils.readSocketAddress(buffer);
		if(null != remoteAdress)
		{
			sessionRegistryService.putSession(remoteAdress, playerSession);
		}
	}
	
	public LookupService getLookupService()
	{
		return lookupService;
	}

	public void setLookupService(LookupService lookupService)
	{
		this.lookupService = lookupService;
	}

	public SessionRegistryService getSessionRegistryService()
	{
		return sessionRegistryService;
	}

	public void setSessionRegistryService(
			SessionRegistryService sessionRegistryService)
	{
		this.sessionRegistryService = sessionRegistryService;
	}

}
