package org.hibernate.forge.datasource.h2;

import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.Assert;

import org.h2.tools.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class H2DataSourceTest {
	
	private Server server;
	
	@Before
	public void startH2() throws Exception {
		server = Server.createTcpServer().start();
        Class.forName("org.h2.Driver");
        DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:test;USER=foo;PASSWORD=bar");
	}
	
	@Test
	public void testConnection() {
		try {
			Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:test", "foo", "bar");
			Assert.assertNotNull(conn);
			conn.close();
		} catch (Exception e) {
			// should never happen
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}
	
	@After
	public void stopH2() throws Exception {
		server.stop();
	}

}
