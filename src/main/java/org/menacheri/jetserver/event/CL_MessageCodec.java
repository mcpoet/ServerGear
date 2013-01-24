package org.menacheri.jetserver.event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public interface CL_MessageCodec {
	public abstract CL_MessageCodec decode(ByteArrayInputStream in);
	public abstract byte[] encode(ByteArrayOutputStream out);
}
