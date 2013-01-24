package org.menacheri.jetserver.event.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
<<<<<<< HEAD
=======
import java.util.List;

>>>>>>> 4b5272e55f0e887c1ee5f78e9efca909d94cd5a9
import org.jboss.netty.buffer.ChannelBuffer;
import org.menacheri.jetserver.event.CL_MessageCodec;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class CL_DefaultMessage implements CL_MessageCodec {	
	public String apiName;
	public int messageId;
	public int sessionToken;
<<<<<<< HEAD
	public CL_MessageCodec embedder;

	public CL_DefaultMessage (ChannelBuffer cb) {}
=======
	private ChannelBuffer source;
	public  CL_MessageCodec embedder;

	public  CL_DefaultMessage (ChannelBuffer cb) {
	}
>>>>>>> 4b5272e55f0e887c1ee5f78e9efca909d94cd5a9
	
	public String toString() {
		return "CL_DefaultMessage to String";
	}

	@Override
	public CL_MessageCodec decode(ByteArrayInputStream in) {
		assert this!=null;
		MessagePack mgp = new MessagePack();
		Unpacker up = mgp.createUnpacker(in);
		try {
			apiName = up.readString();
			messageId = up.readInt();
			sessionToken = up.readInt();
			if(embedder!=null)
				embedder.decode(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return this;
	}

	@Override
	public byte[] encode(ByteArrayOutputStream out) 
	{
		if (out==null) {
			out = new ByteArrayOutputStream();
		}
		MessagePack mgp = new MessagePack();
		Packer packer = mgp.createPacker(out);
		try {
			packer.write(apiName);
			packer.write(messageId);
			packer.write(sessionToken);
			if (embedder==null) {
				packer.writeNil();
			}else
				embedder.encode(out);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return out.toByteArray();
	}
}
