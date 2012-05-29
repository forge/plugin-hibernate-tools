package org.hibernate.forge.generate;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.h2.tools.Server;
import org.hibernate.forge.datasource.DataSourceDescriptor;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.forge.test.MavenArtifactResolver;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class GenerateEntitiesPluginTest extends AbstractShellTest {

	@Deployment
	public static JavaArchive getDeployment() {
		return AbstractShellTest.getDeployment()
				.addPackages(true, GenerateEntitiesPlugin.class.getPackage())
				.addPackages(true, DataSourceDescriptor.class.getPackage());

	}

	@Test
	public void testH2() throws Exception {

		Server server = Server.createTcpServer().start();
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager
				.getConnection("jdbc:h2:tcp://localhost/mem:test;USER=foo;PASSWORD=bar");
		conn.createStatement().execute(
				"CREATE TABLE customer(" + "	id INTEGER PRIMARY KEY,"
						+ "	first_name VARCHAR(256),"
						+ "	last_name VARCHAR(256))");
		conn.commit();

		initializeJavaProject();
		queueInputLines("");
		getShell().execute("project install-facet forge.spec.jpa");
		queueInputLines("bar");
		getShell().execute(
				"generate-entities" + " --driver org.h2.Driver"
						+ " --pathToDriver " + resolveH2DriverJarPath()
						+ " --url jdbc:h2:tcp://localhost/mem:test"
						+ " --dialect org.hibernate.dialect.H2Dialect"
						+ " --user foo" //+ " --password bar"
						+ " --entityPackage com.test.model");

		server.stop();

	}

	@Test
	public void test() throws Exception {
		initializeJavaProject();
		queueInputLines("");
		getShell().execute("project install-facet forge.spec.jpa");
		queueInputLines("");
		getShell().execute(
				"generate-entities" + " --driver org.hsqldb.jdbc.JDBCDriver"
						+ " --pathToDriver " + resolveHsqlDriverJarPath()
						+ " --url jdbc:hsqldb:res:/db/testDB"
						+ " --dialect org.hibernate.dialect.HSQLDialect"
						+ " --user sa" + " --entityPackage com.test.model");
	}

	private String resolveHsqlDriverJarPath() {
		File file = MavenArtifactResolver.resolve("org.hsqldb", "hsqldb",
				"2.2.8");
		return file == null ? "" : file.getAbsolutePath();
	}

	private String resolveH2DriverJarPath() {
		File file = MavenArtifactResolver.resolve("com.h2database", "h2",
				"1.3.167");
		return file == null ? "" : file.getAbsolutePath();
	}

}
