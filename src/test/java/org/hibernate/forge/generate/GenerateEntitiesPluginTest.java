package org.hibernate.forge.generate;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.h2.tools.Server;
import org.hibernate.forge.connections.ConnectionProfile;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.forge.test.MavenArtifactResolver;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class GenerateEntitiesPluginTest extends AbstractShellTest {

	@Deployment
	public static JavaArchive getDeployment() {
		return AbstractShellTest.getDeployment()
				.addPackages(true, GenerateEntitiesPlugin.class.getPackage())
				.addPackages(true, ConnectionProfile.class.getPackage());

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
						+ " --user foo" // + " --password bar"
						+ " --entityPackage com.test.model");

		server.stop();

	}

	@Test
	public void testSchemaTestH2() throws Exception {

		Server server = Server.createTcpServer().start();
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager
				.getConnection("jdbc:h2:tcp://localhost/mem:test;USER=foo;PASSWORD=bar");
		createSampleSchemasAndTables(conn);

		Project javaProject = initializeJavaProject();
		queueInputLines("");
		getShell().execute("project install-facet forge.spec.jpa");
		queueInputLines("bar");
		getShell().execute(
				"generate-entities" + " --driver org.h2.Driver"
						+ " --pathToDriver " + resolveH2DriverJarPath()
						+ " --url jdbc:h2:tcp://localhost/mem:test"
						+ " --dialect org.hibernate.dialect.H2Dialect"
						+ " --user foo" // + " --password bar"
						+ " --entityPackage com.test.model --schema TEST*");

		DirectoryResource srcModelDir = javaProject.getProjectRoot()
				.getChildDirectory("src/main/java/com/test/model");
		List<Resource<?>> generatedSrcFiles = srcModelDir.listResources();
		StringBuilder entities = new StringBuilder();
		for (Resource<?> resource : generatedSrcFiles) {
			entities.append(resource.getName()).append(",");
		}

		Assert.assertEquals(
				"Generation was wrong: 5 entities were expected - current: '"
						+ entities.toString() + "'", generatedSrcFiles.size(),
				5);

		for (Resource<?> resource : generatedSrcFiles) {
			Assert.assertTrue("Generation was wrong: " + resource.getName()
					+ " was not expected!",
					resource.getName().startsWith("Client")
							|| resource.getName().startsWith("Customer")
							|| resource.getName().startsWith("Batatas"));

		}

		server.stop();

	}

	@Test
	public void testSchemaTest1H2() throws Exception {

		Server server = Server.createTcpServer().start();
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager
				.getConnection("jdbc:h2:tcp://localhost/mem:test;USER=foo;PASSWORD=bar");
		createSampleSchemasAndTables(conn);

		Project javaProject = initializeJavaProject();
		queueInputLines("");
		getShell().execute("project install-facet forge.spec.jpa");
		queueInputLines("bar");
		getShell().execute(
				"generate-entities" + " --driver org.h2.Driver"
						+ " --pathToDriver " + resolveH2DriverJarPath()
						+ " --url jdbc:h2:tcp://localhost/mem:test"
						+ " --dialect org.hibernate.dialect.H2Dialect"
						+ " --user foo" // + " --password bar"
						+ " --entityPackage com.test.model --schema TEST2");

		DirectoryResource srcModelDir = javaProject.getProjectRoot()
				.getChildDirectory("src/main/java/com/test/model");
		List<Resource<?>> generatedSrcFiles = srcModelDir.listResources();

		StringBuilder entities = new StringBuilder();
		for (Resource<?> resource : generatedSrcFiles) {
			entities.append(resource.getName()).append(",");
		}
		Assert.assertEquals(
				"Generation was wrong: 1 entity was expected - current: '"
						+ entities.toString() + "'", generatedSrcFiles.size(),
				1);
		for (Resource<?> resource : generatedSrcFiles) {
			Assert.assertTrue("Generation was wrong: " + resource.getName()
					+ " was not expected!",
					resource.getName().equals("Batatas.java"));
		}

		server.stop();

	}

	@Test
	public void testTableNamesH2() throws Exception {

		Server server = Server.createTcpServer().start();
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager
				.getConnection("jdbc:h2:tcp://localhost/mem:test;USER=foo;PASSWORD=bar");
		createSampleSchemasAndTables(conn);

		Project javaProject = initializeJavaProject();
		queueInputLines("");
		getShell().execute("project install-facet forge.spec.jpa");
		queueInputLines("bar");
		getShell().execute(
				"generate-entities" + " --driver org.h2.Driver"
						+ " --pathToDriver " + resolveH2DriverJarPath()
						+ " --url jdbc:h2:tcp://localhost/mem:test"
						+ " --dialect org.hibernate.dialect.H2Dialect"
						+ " --user foo" // + " --password bar"
						+ " --entityPackage com.test.model --table CUSTOMER*");

		DirectoryResource srcModelDir = javaProject.getProjectRoot()
				.getChildDirectory("src/main/java/com/test/model");
		List<Resource<?>> generatedSrcFiles = srcModelDir.listResources();
		StringBuilder entities = new StringBuilder();
		for (Resource<?> resource : generatedSrcFiles) {
			entities.append(resource.getName()).append(",");
		}
		Assert.assertEquals(
				"Generation was wrong: 2 entities were expected - current: '"
						+ entities.toString() + "'", generatedSrcFiles.size(),
				2);
		for (Resource<?> resource : generatedSrcFiles) {
			Assert.assertTrue("Generation was wrong: " + resource.getName()
					+ " was not expected!",
					resource.getName().startsWith("Customer"));

		}

		server.stop();

	}

	private void createSampleSchemasAndTables(Connection conn)
			throws SQLException {
		conn.createStatement().execute("CREATE SCHEMA TEST1");
		conn.createStatement().execute("CREATE SCHEMA TEST2");
		conn.createStatement().execute("CREATE SCHEMA ANOTHERTEST");
		conn.commit();

		conn.createStatement().execute(
				"CREATE TABLE TEST1.customer1(" + "	id INTEGER PRIMARY KEY,"
						+ "	first_name VARCHAR(256),"
						+ "	last_name VARCHAR(256))");
		conn.createStatement().execute(
				"CREATE TABLE TEST1.customer2(" + "	id INTEGER PRIMARY KEY,"
						+ "	first_name VARCHAR(256),"
						+ "	last_name VARCHAR(256))");
		conn.createStatement().execute(
				"CREATE TABLE TEST1.client1(" + "	id INTEGER PRIMARY KEY,"
						+ "	first_name VARCHAR(256),"
						+ "	last_name VARCHAR(256))");
		conn.createStatement().execute(
				"CREATE TABLE TEST1.client2(" + "	id INTEGER PRIMARY KEY,"
						+ "	first_name VARCHAR(256),"
						+ "	last_name VARCHAR(256))");

		conn.createStatement().execute(
				"CREATE TABLE TEST2.batatas(" + "	id INTEGER PRIMARY KEY,"
						+ "	first_name VARCHAR(256),"
						+ "	last_name VARCHAR(256))");

		conn.createStatement().execute(
				"CREATE TABLE ANOTHERTEST.foo(" + "	id INTEGER PRIMARY KEY,"
						+ "	first_name VARCHAR(256),"
						+ "	last_name VARCHAR(256))");

		conn.commit();
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
