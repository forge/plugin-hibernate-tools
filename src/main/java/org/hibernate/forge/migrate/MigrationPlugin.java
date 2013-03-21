package org.hibernate.forge.migrate;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.forge.common.Constants;
import org.hibernate.forge.common.UrlClassLoaderExecutor;
import org.hibernate.forge.connections.ConnectionProfileHelper;
import org.hibernate.forge.connections.ConnectionProfileNameCompleter;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.Plugin;

@Alias("migrate-database")
@Help("Migrate a source database to a target database.")
public class MigrationPlugin  implements Plugin, Constants {
	
	private File baseDir, srcDir, binDir;
	private String[] script;
	
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

	@Inject
	private Shell shell;

	@Inject 
	private ConnectionProfileHelper connectionProfileHelper;
	
	@DefaultCommand
	public void migrateDatabase(
			@Option(name = FROM_CONNECTION_PROFILE, help = FROM_CONNECTION_PROFILE_HELP, required = false, completer = ConnectionProfileNameCompleter.class) String fromConnectionProfileName,
			@Option(name = FROM_URL, help = FROM_URL_HELP, required = false) String fromUrl,
			@Option(name = FROM_USER, help = FROM_USER_HELP, required = false) String fromUser,
			@Option(name = FROM_PASSWORD, help = FROM_PASSWORD_HELP, required = false) String fromPassword,
			@Option(name = FROM_DIALECT, help = FROM_DIALECT_HELP, required = false) String fromDialect,
			@Option(name = FROM_DRIVER, help = FROM_DRIVER_HELP, required = false) String fromDriver,
			@Option(name = FROM_PATH_TO_DRIVER, help = FROM_PATH_TO_DRIVER_HELP, required = false) String fromPath,
			@Option(name = TO_CONNECTION_PROFILE, help = TO_CONNECTION_PROFILE_HELP, required = false, completer = ConnectionProfileNameCompleter.class) String toConnectionProfileName,
			@Option(name = TO_URL, help = TO_URL_HELP, required = false) String toUrl,
			@Option(name = TO_USER, help = TO_USER_HELP, required = false) String toUser,
			@Option(name = TO_PASSWORD, help = TO_PASSWORD_HELP, required = false) String toPassword,
			@Option(name = TO_DIALECT, help = TO_DIALECT_HELP, required = false) String toDialect,
			@Option(name = TO_DRIVER, help = TO_DRIVER_HELP, required = false) String toDriver,
			@Option(name = TO_PATH_TO_DRIVER, help = TO_PATH_TO_DRIVER_HELP, required = false) String toPath) {
		
	}
	
	
	public void testSomething() {
		try {
			setUp();
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
	
	private void generateScript(final JDBCMetaDataConfiguration cfg) throws Exception {
		URL[] urls = new URL[] { binDir.toURI().toURL() };
		UrlClassLoaderExecutor.execute(urls, new Runnable() {
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
