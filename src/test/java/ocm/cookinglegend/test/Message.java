package ocm.cookinglegend.test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

//@org.msgpack.annotation.Message
public class Message {
	
	public Integer apiNum;
	public String apiName;
	public List<String> params;
//	public List<embedMessage> embed;
//	public List<List<String>> embedStrings;
	private String palo;
	public Message() {
		// TODO Auto-generated constructor stub
		params = new ArrayList<String>();
		params.add("one");
		params.add("two");
		params.add("three");
//		embed = new ArrayList<embedMessage>();
//		embed.add(new embedMessage());
//		embed.add(new embedMessage());
//		embed.add(new embedMessage());
		List<String> tmp1 = new ArrayList<String>();
		tmp1.add("new");
		List<String> tmp2 = new ArrayList<String>();
//		embedStrings = new ArrayList<List<String>>();
//		embedStrings.add(tmp1);
//		embedStrings.add(tmp2);
		apiNum = 312;
		apiName = "MyAPI";
		palo = "My little Corner";
	}
	
	public String toString()
	{
		return "#"+apiNum
				+" #"+apiName
				+" #param:"+params.toString()
//				+" #embed:"+embed.toString()
//				+" #EmbedString:"+embedStrings.toString()
				+" #priva:"+palo;
	}
	public byte[] encode() {
		MessagePack msgpack = new MessagePack();
        //
        // Serialize
        //
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);
        try {
//            packer.write(apiNum);
//            packer.write(apiName);
//            packer.write(params);
//			packer.write(embed);
//			packer.write(embedStrings);
        	Class usrcls = Message.class;
        	Field[] fs = usrcls.getDeclaredFields();
        	for (Field field : fs) {
        		System.out.println("Declared field : "+field.getName()+field.get(this));
        		packer.write(field.get(this));
			}
        	

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        byte[] bytes = out.toByteArray();
 
		return bytes;
	}
	
	public static Message decode(byte[] bytes)
	{
		Message m = new Message();
		//
        // Deserialize
        //
		MessagePack msgpack = new MessagePack();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        Unpacker unpacker = msgpack.createUnpacker(in);
        try {
//			System.out.println(m.toString());
	       	Class usrcls = Message.class;
        	Field[] fs = usrcls.getDeclaredFields();
        	for (Field field : fs) {
        		System.out.println("Declared field : "+field.getName());        		
        		field.set(m, unpacker.read(field.getType()));
			}
 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return m;
	}

//	public String getPrivacy() {
//		return privacy;
//	}
//
//	public void setPrivacy(String privacy) {
//		this.privacy = privacy;
//	}
}

