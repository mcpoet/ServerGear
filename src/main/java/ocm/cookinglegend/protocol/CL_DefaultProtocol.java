package ocm.cookinglegend.protocol;

import ocm.cookinglegend.handlers.CL_DefaultToServerHandler;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.menacheri.jetserver.app.PlayerSession;
import org.menacheri.jetserver.protocols.AbstractNettyProtocol;
import org.menacheri.jetserver.util.NettyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple protocol which would just strip out the length bytes from the buffer
 * and return the <a href="http://www.jboss.org/netty">Netty</a> ChannelBuffer
 * to the next decoder or game handler in the {@link ChannelPipeline}. For out
 * going messages it will set the length using the {@link LengthFieldPrepender}
 * encoder. The binary packets it receives will be of the format
 * [OPCODE][LENGTH][PAYLOAD] will find this protocol the most useful.
 * 
 * @author Abraham Menacherry
 * 
 */
public class CL_DefaultProtocol extends AbstractNettyProtocol
{
	private static final Logger LOG = LoggerFactory.getLogger(CL_DefaultProtocol.class);
	/**
	 * Utility handler provided by netty to add the length of the outgoing
	 * message to the message as a header.
	 */
	private LengthFieldPrepender lengthFieldPrepender;
	
	public CL_DefaultProtocol()
	{
		super("CHANNEL_BUFFER_PROTOCOL");
	}

	@Override
	public void applyProtocol(PlayerSession playerSession)
	{
		LOG.trace("Going to apply protocol on session: {}" ,playerSession);
		
		ChannelPipeline pipeline = NettyUtils
				.getPipeLineOfConnection(playerSession);
		// Upstream handlers or encoders (i.e towards server) are added to
		// pipeline now.
		pipeline.addLast("lengthDecoder", createLengthBasedFrameDecoder());
		pipeline.addLast("eventHandler", new CL_DefaultToServerHandler(
				playerSession));

		// Downstream handlers - Filter for data which flows from server to
		// client. Note that the last handler added is actually the first
		// handler for outgoing data.
		pipeline.addLast("lengthFieldPrepender", lengthFieldPrepender);

	}

	public LengthFieldPrepender getLengthFieldPrepender()
	{
		return lengthFieldPrepender;
	}

	public void setLengthFieldPrepender(LengthFieldPrepender lengthFieldPrepender)
	{
		this.lengthFieldPrepender = lengthFieldPrepender;
	}


}
