package org.menacheri.jetserver.handlers.netty;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.menacheri.jetserver.event.Events;
import org.menacheri.jetserver.event.impl.CL_DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.msgpack.*;
import org.msgpack.unpacker.Unpacker;


@Sharable
public class CL_MsgUnpacker extends OneToOneDecoder {
	private static final Logger LOG = LoggerFactory.getLogger(EventDecoder.class);
	private Object MessageDecoder;
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception
	{
		if(null == msg)
		{
			LOG.error("Null msg received in EventDecoder");
			return msg;
		}
		ChannelBuffer buffer = (ChannelBuffer)msg;
		MessagePack msgpack = new MessagePack();
    	byte[] bytes = buffer.array();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        Unpacker up = msgpack.createUnpacker(in);
        String rpcName = up.readString();
        //from the map get the handler and process the rest of the data;
//        MessageDecoder m = ((Object) MessageDecoder).find(rpcName).decode(in);
        
//		List<Object> message = new ArrayList<Object>();
//		msgpack.read(buffer.toByteBuffer(), message);
//		int opcode = buffer.readUnsignedByte();
//		if(Events.LOG_IN == opcode){
//			buffer.readUnsignedByte();// To read-destroy the protocol version byte.
//		}
//		return Events.event(buffer, opcode);
        
//		return CL_DefaultMessage.decode(buffer.toByteBuffer());
		return MessageDecoder;
	}
	
}


