package org.hibernate.forge.database;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.hibernate.forge.database.ConnectionProfileType;
import org.junit.Test;

public class ConnectionProfileTypeTest {

	@Test
	public void test() throws Exception {
		Map<String, ConnectionProfileType> allTypes = ConnectionProfileType.allTypes();
		Assert.assertEquals(2, allTypes.size());
		
		ConnectionProfileType hsqlType = allTypes.get("HSQL");
		Assert.assertNotNull(hsqlType);
		Assert.assertEquals("HSQL", hsqlType.getName());
		Assert.assertEquals("org.hibernate.dialect.HSQLDialect", hsqlType.getDialect());
		Map<String, List<String>> drivers = hsqlType.getDrivers();
		Assert.assertEquals(1, drivers.size());
		Assert.assertTrue(drivers.containsKey("org.hsqldb.jdbcDriver"));
		List<String> urls = drivers.get("org.hsqldb.jdbcDriver");
		Assert.assertEquals(3, urls.size());
	}
	
}
