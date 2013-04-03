package org.zeromeaner.dbo;

import java.io.IOException;
import java.util.Properties;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class Mappers {
	private static SqlSessionFactory factory;
	
	public static SqlSessionFactory getFactory() {
		if(factory == null) {
			SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
			Properties p = new Properties();
			try {
				p.load(Mappers.class.getResourceAsStream("0mino.properties"));
			} catch(IOException ioe) {
			}
			factory = builder.build(Mappers.class.getResourceAsStream("map_config.xml"), p);
		}
		return factory;
	}
	
	private Mappers() {}
}
