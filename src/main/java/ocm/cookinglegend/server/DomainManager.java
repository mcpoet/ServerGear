package ocm.cookinglegend.server;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import ocm.cookinglegend.dao.PlayerInfoMapper;
import ocm.cookinglegend.domain.PlayerInfo;

public class DomainManager {
	/**
	 * scope: application
	 */
	private static DomainManager _singleton;
	public DomainManager instance()
	{
		if(_singleton==null){
			_singleton = new DomainManager();
		}
		return _singleton;
	}
	private static SqlSessionFactory sqlSessionFactory = null;

	public DomainManager() {
		System.out.println("DomainManager constructor");
		String resource = "jetserver/configs/configuration.xml";
		Reader reader = null;

		try {
			reader = Resources.getResourceAsReader(resource);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ;
		}

//		sqlSessionFactory.getConfiguration().addMapper(PlayerInfoMapper.class);
		SqlSession session = sqlSessionFactory.openSession();

		try {
			PlayerInfo c = new PlayerInfo();
			c.setName("messy");
			c.setPassword("asdfasdf");
			c.setPlatform("ewa");
			c.setLevel(10);
			c.setCoins(10);
			c.setExperience(10);
			c.setGolds(10);
			c.setGenueId(0);
			c.setEnergy(10);
			c.setServer(1);
			c.setGangId(1);
			c.setDeleted(false);
			c.setReservedInt1(1);
			c.setGroupId(1);
			PlayerInfoMapper mapper = session.getMapper(PlayerInfoMapper.class);
			PlayerInfo p = mapper.selectByPrimaryKey(1);
			System.out.println(p.toString());
			mapper.insert(c);
			session.commit();
			
		} finally {
			session.close();
		}
	}
}

