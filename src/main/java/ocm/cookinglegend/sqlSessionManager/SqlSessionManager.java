package ocm.cookinglegend.sqlSessionManager;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SqlSessionManager {
	
	static SqlSessionFactory sqlSessionFactory_ = null;
	private static void initFactory()
	{
		String resource = "org/mybatis/example/configuration.xml";
		Reader reader = null;
		try {
			reader = Resources.getResourceAsReader(resource);
			sqlSessionFactory_ = new SqlSessionFactoryBuilder().build(reader);
		} catch (IOException e) {
			e.printStackTrace();
			sqlSessionFactory_ = null;
		}

	}
	public static SqlSession openSession()
	{
		if(sqlSessionFactory_==null)
			initFactory();
		return (sqlSessionFactory_!=null?sqlSessionFactory_.openSession():null);		
	}
	
	public static SqlSessionFactory getFactory() {
		if(sqlSessionFactory_==null)
			initFactory();
		return sqlSessionFactory_;		
		
	}
}
