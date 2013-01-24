package ocm.cookinglegend.test;

import java.util.ArrayList;
import java.util.List;

@org.msgpack.annotation.Message
public class embedMessage{
	public int apiNum;
	public String nameString;
	public double factor;
	public List<String> strings;
	public embedMessage() {
		// TODO Auto-generated constructor stub
		apiNum = 312;
		nameString = "asdfasdf";
		factor = 2.1212;
		strings = new ArrayList<String>();
	}
	public String toString()
	{
		return "embedded object"+
				" #"+apiNum+
				" #"+nameString+
				" @"+factor;
	}
}