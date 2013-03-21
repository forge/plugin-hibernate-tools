package org.hibernate.forge.migrate;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.persistence.Entity;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.forge.common.UrlClassLoaderExecutor;
import org.hibernate.forge.connections.ConnectionProfile;
import org.hibernate.forge.generate.DelegatingDriver;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.jboss.forge.shell.Shell;

public class MigrationHelper {

	private File baseDir, srcDir, binDir;
	private String[] script;
	private JDBCMetaDataConfiguration metaDataCfg; 
	
	private void setUp() {
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
	
	private void tearDown() {
		if (baseDir != null && baseDir.exists()) {
			baseDir.delete();
		}
	}

	public void migrate(ConnectionProfile from, ConnectionProfile to, Shell shell) {
		try {
			setUp();
			buildMetaDataConfiguration(from);
			createSourceFiles();
			compileSourceFiles();
			generateScript(to);
			for (int i = 0; i < script.length; i++) {
				shell.println(script[i]);
			}
			tearDown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void buildMetaDataConfiguration(final ConnectionProfile cp) throws Exception {
		URL[] urls = new URL[] { new File(cp.path).toURI().toURL() };
		UrlClassLoaderExecutor.execute(urls, new Runnable() {
			@Override
			public void run() {
				try {
					Driver jdbcDriver = (Driver) Class.forName(cp.driver, true,
							Thread.currentThread().getContextClassLoader())
							.newInstance();
					DriverManager.registerDriver(new DelegatingDriver(jdbcDriver));
					metaDataCfg = new JDBCMetaDataConfiguration();
					metaDataCfg.setProperties(createProperties(cp));
					metaDataCfg.readFromJDBC();
					metaDataCfg.buildMappings();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}			
		});
	}
	
	private void generateScript(final ConnectionProfile cp)
			throws Exception {
		URL[] urls = new URL[] { binDir.toURI().toURL() };
		UrlClassLoaderExecutor.execute(urls, new Runnable() {
			@Override
			public void run() {
				script = metaDataCfg.generateSchemaCreationScript(new H2Dialect());
			}
		});
	}

	private void compileSourceFiles() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				null, null, null);
		File[] files = srcDir.listFiles();
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(files); 
		List<String> optionList = new ArrayList<String>();
		optionList.addAll(Arrays.asList("-cp", getPersistenceAPILocation()));
		optionList.addAll(Arrays.asList("-d", binDir.getAbsolutePath()));
		compiler.getTask(null, fileManager, null, optionList,
				null, compilationUnits).call();
		fileManager.close();
	}

	private Properties createProperties(ConnectionProfile cp) {
		Properties properties = new Properties();
		properties.setProperty("hibernate.connection.driver_class", cp.driver);
		properties.setProperty("hibernate.connection.username", cp.user);
		properties.setProperty("hibernate.connection.password", cp.password);
		properties.setProperty("hibernate.dialect", cp.dialect);
		properties.setProperty("hibernate.connection.url", cp.url);
		return properties;
	}
	
	private String getPersistenceAPILocation() throws Exception {
		ProtectionDomain pDomain = Entity.class.getProtectionDomain();
		CodeSource cSource = pDomain.getCodeSource();
		String result = cSource.getLocation().getFile();
		result = result.substring(5, result.length() - 2);
		System.out.println(result);
		return result;
	}

	private void createSourceFiles() {
		try {
			POJOExporter pj = new POJOExporter(metaDataCfg, srcDir);
			Properties pojoProperties = new Properties();
			pojoProperties.setProperty("jdk5", "true");
			pojoProperties.setProperty("ejb3", "true");
			pj.setProperties(pojoProperties);
			ArtifactCollector artifacts = new ArtifactCollector();
			pj.setArtifactCollector(artifacts);
			pj.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
