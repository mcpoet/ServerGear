package ocm.cookinglegend.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ocm.cookinglegend.dao.PlayerInfoMapper;
import ocm.cookinglegend.domain.PlayerInfo;
import ocm.cookinglegend.sqlSessionManager.SqlSessionManager;

import org.apache.ibatis.session.SqlSession;
import org.menacheri.jetserver.event.CL_MessageCodec;
import org.msgpack.MessagePack;
import org.msgpack.unpacker.Unpacker;

public class CL_LoginProtocol implements CL_MessageCodec {

	public PlayerInfo decodePlayer(ByteArrayInputStream in) {
		
		SqlSession session = SqlSessionManager.openSession();
		PlayerInfoMapper pmapper = session.getMapper(PlayerInfoMapper.class);		
		try{			
			Unpacker up = new MessagePack().createUnpacker(in);
			String api = up.readString();
			if (api.equals("login")) {
				int playerId = up.readInt();
				String userName = up.readString();
				String platform = up.readString();
				String platformToken = up.readString();
				//May use the username and token verify the user here by;
				//And may be done in a asynchronic thread;
				PlayerInfo pInfo = new PlayerInfo();
				pInfo.setName(userName);
				pInfo.setPlatform(platform);
				pInfo.setPassword(platformToken);
				//PlayerInfo playerInfo = pmapper.selectByPlayerInfo(pInfo);
				PlayerInfo playerInfo = pmapper.selectByPrimaryKey(playerId);
				return playerInfo;			
			}
			else if (api.equals("signin")){
				String userName = up.readString();
				String platform = up.readString();
				String platformToken = up.readString();
				//May use the username and token verify the user here by;
				PlayerInfo pInfo = new PlayerInfo();
				pInfo.setName(userName);
				pInfo.setPlatform(platform);
				pInfo.setPassword(platformToken);
//				int id = pmapper.insertSelective(pInfo);				
				int id = pmapper.insert(pInfo);
				pInfo.setId(id);
				return pInfo;			
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}	
		return null;		
	}
	@Override
	public CL_MessageCodec decode(ByteArrayInputStream in) {
		return null;
	}

	@Override
	public byte[] encode(ByteArrayOutputStream out) {
		return null;
	}

}
