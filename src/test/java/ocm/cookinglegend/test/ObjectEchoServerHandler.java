/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package ocm.cookinglegend.test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.msgpack.MessagePack;


/**
 * Handles both client-side and server-side handler depending on which
 * constructor was called.
 */
public class ObjectEchoServerHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(
            ObjectEchoServerHandler.class.getName());

    private final AtomicLong transferredMessages = new AtomicLong();

    public long getTransferredMessages() {
        return transferredMessages.get();
    }

    @Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent &&
            ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
            logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {
        // Echo back the received object to the client.
        transferredMessages.incrementAndGet();        
    	ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
    	byte[] b = buffer.array();
    	MessagePack msgpck = new MessagePack();
   	 	Message m = Message.decode(b);
        System.out.println("Server receivedddd "+m.toString());
            	
//        e.getChannel().write(e.getMessage());
        
        
        
//     // Send back the received message to the remote peer.

//        transferredBytes.addAndGet(buffer.readableBytes());
//        System.out.println("Server get message :"+buffer.toString(CharsetUtil.UTF_8)+"from CHannel %"+ctx.getChannel().getId());        
//        String json = buffer.toString(CharsetUtil.UTF_8);
//        //	Parse the json string
//        Gson gson = new Gson();
//        JsonParser parser = new JsonParser();
//        JsonArray array = parser.parse(json).getAsJsonArray();
//        String message = gson.fromJson(array.get(0), String.class);
//        int number = gson.fromJson(array.get(1), int.class);
//        //  MyClass event = gson.fromJson(array.get(2), MyClass.class);
//        System.out.printf("Using Gson.fromJson() to get: %s, %d\n", message, number);
//        e.getChannel().write(e.getMessage());
//        //After parsing the message we would do some pile up work...
//        //Like some big switch case for some cocurrent queues,
//        // But now we would only execute them serializingly.
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }
}
