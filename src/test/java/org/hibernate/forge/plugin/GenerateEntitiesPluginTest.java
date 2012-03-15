package org.hibernate.forge.plugin;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.forge.test.MavenArtifactResolver;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class GenerateEntitiesPluginTest extends AbstractShellTest  {
	
   @Deployment
   public static JavaArchive getDeployment()
   {
      return AbstractShellTest.getDeployment()
               .addPackages(true, GenerateEntitiesPlugin.class.getPackage());
   }
   
	@Test
	public void test() throws Exception {
		initializeJavaProject();
		getShell().execute("project install-facet forge.spec.jpa");
		getShell().execute(
				"generate-entities" +
				" --driver org.hsqldb.jdbc.JDBCDriver" +
				" --pathToDriverJar " + resolveDriverJarPath() +
				" --url jdbc:hsqldb:res:/db/testDB" +
				" --dialect org.hibernate.dialect.HSQLDialect" +
				" --entityPackage com.test.model");
	}
	
	private String resolveDriverJarPath() {
		File file = MavenArtifactResolver.resolve("org.hsqldb", "hsqldb", "2.2.8");
		return file == null ? "" : file.getAbsolutePath();
	}

}
