package org.hibernate.forge.migration;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.junit.Before;
import org.junit.Test;

public class MigrationPluginTest {
	
	private File baseDir, srcDir, binDir;
	private String[] script;
	
	@Before
	public void setUp() {
		baseDir = new File(System.getProperty("java.io.tmpdir"), "tmp");
		if (baseDir.exists()) {
			baseDir.delete();
		}
		baseDir.mkdir();
		srcDir = new File(baseDir, "src");
		srcDir.mkdir();
		binDir = new File(baseDir, "bin");
		binDir.mkdir();
	}

	@Test
	public void testSomething() {
		try {
			JDBCMetaDataConfiguration cfg = new JDBCMetaDataConfiguration();
			cfg.setProperties(createFromProperties());
			cfg.readFromJDBC();
			cfg.buildMappings();
			exportNewEntities(cfg);
			compileSourceFiles();
			generateScript(cfg);
			for (int i = 0; i < script.length; i++) {
				System.out.println(script[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void executeInNewUrlClassLoader(URL[] urls, Runnable runnable) {
		ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			URLClassLoader newClassLoader = new URLClassLoader(urls, savedClassLoader);
			Thread.currentThread().setContextClassLoader(newClassLoader);
			runnable.run();
		} finally {
			Thread.currentThread().setContextClassLoader(savedClassLoader);
		}
	}
	
	private void generateScript(final JDBCMetaDataConfiguration cfg) throws Exception {
		URL[] urls = new URL[] { binDir.toURI().toURL() };
		executeInNewUrlClassLoader(urls, new Runnable() {
			@Override
			public void run() {
				script = cfg.generateSchemaCreationScript(new H2Dialect());
			}			
		});
	}
	
	private void compileSourceFiles() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		File[] files = srcDir.listFiles();
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(files); // use alternative method
	    List<String> optionList = new ArrayList<String>();
        optionList.addAll(Arrays.asList("-d", binDir.getAbsolutePath()));
    	boolean result = compiler.getTask(null, fileManager, null, optionList, null, compilationUnits).call();
		System.out.println("compilation: " + result);
		fileManager.close();
	}

	private Properties createFromProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.connection.driver_class",
				"org.h2.Driver");
		properties.setProperty("hibernate.connection.username", "sa");
		properties.setProperty("hibernate.dialect",
				"org.hibernate.dialect.H2Dialect");
		properties.setProperty("hibernate.connection.url",
				"jdbc:h2:~/app-root/data/sakila");
		return properties;
	}

	private void exportNewEntities(JDBCMetaDataConfiguration jmdc) throws Exception {
		POJOExporter pj = new POJOExporter(jmdc, srcDir);
		Properties pojoProperties = new Properties();
		pojoProperties.setProperty("jdk5", "true");
		pojoProperties.setProperty("ejb3", "true");
		pj.setProperties(pojoProperties);
		ArtifactCollector artifacts = new ArtifactCollector();
		pj.setArtifactCollector(artifacts);
		pj.start();
	}

}
