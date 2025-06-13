package org.sunbird.common;

import com.datastax.driver.core.Session;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import play.test.WithApplication;
import org.sunbird.utils.CassandraConnector;

public class CassandraTestSetup extends WithApplication {

	private static Session session = null;

	@AfterClass
	public static void afterTest() throws Exception {
		tearEmbeddedCassandraSetup();
	}

	@BeforeClass
	public static void before() throws Exception {
		setupEmbeddedCassandra();
	}

	protected static Session getSession() {
		return session;
	}

	private static void setupEmbeddedCassandra() throws Exception {
		EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra-unit.yaml", 100000L);
		session = CassandraConnector.getSession();
	}

	private static void tearEmbeddedCassandraSetup() {
		try {
			if (null != session && !session.isClosed())
				session.close();
			EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected static void executeScript(String... querys) throws Exception {
		session = CassandraConnector.getSession();
		for (String query : querys) {
			session.execute(query);
		}
	}

}
