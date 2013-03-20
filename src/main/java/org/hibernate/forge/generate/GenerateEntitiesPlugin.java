package org.hibernate.forge.generate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.forge.Constants;
import org.hibernate.forge.connections.ConnectionProfile;
import org.hibernate.forge.connections.ConnectionProfileHelper;
import org.hibernate.forge.connections.ConnectionProfileNameCompleter;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.Topic;
import org.jboss.forge.spec.javaee.PersistenceFacet;

@Topic("Project")
@RequiresProject
@Alias("generate-entities")
@RequiresFacet(PersistenceFacet.class)
@Help("Generate entities from a database.")
public class GenerateEntitiesPlugin implements Plugin, Constants {
	
	private static final String CONNECTION_PROFILE = "connection-profile";
	private static final String CONNECTION_PROFILE_HELP = "Name of the connection profile to use.";

	private static final String TABLE_ID = "table";
	private static final String TABLE_HELP = "Table pattern to include. 'CUSTOMER' for specific table, 'CUST*' for substring match and '*' for all (the default)";
	private static final String TABLE_DEFAULT = "*";

	private static final String SCHEMA_ID = "schema";
	private static final String SCHEMA_HELP = "Schema pattern to include. 'PRODUCTION' for specific schema, 'PR*' for substring match and '*' for all (the default)";
	private static final String SCHEMA_DEFAULT = "*";

	private static final String CATALOG_ID = "catalog";
	private static final String CATALOG_HELP = "Catalog pattern to include. 'MAIN' for specific schema, 'M*' for substring match and '*' for all (the default)";
	private static final String CATALOG_DEFAULT = "*";

	private static final String ENTITY_PACKAGE = "entityPackage";
	private static final String ENTITY_PACKAGE_HELP = "Package to use for generated entities.";
	private static final String ENTITY_PACKAGE_PROMPT = "In which package you'd like to generate the entities, or enter for default:";
	
	private static final String DETECT_MANY_TO_MANY_ID = "detectManyToMany";
	private static final String DETECT_MANY_TO_MANY_HELP = "Detect many-to-many associations between tables.";
//	private static final Boolean DETECT_MANY_TO_MANY_DEFAULT = Boolean.TRUE;
//
	private static final String DETECT_ONE_TO_ONE_ID = "detectOneToOne";
	private static final String DETECT_ONE_TO_ONE_HELP = "Detect one-to-one associations between tables.";
//	private static final Boolean DETECT_ONE_TO_ONE_DEFAULT = Boolean.TRUE;
//
	private static final String DETECT_OPTIMISTIC_LOCK_ID = "detectOptimisticLock";
	private static final String DETECT_OPTIMISTIC_LOCK_HELP = "Detect optimistic locking tables, i.e. if a table has a column named 'version' with a numeric type optimistic locking will be setup for that table.";
//	private static final Boolean DETECT_OPTIMISTIC_LOCK_DEFAULT = Boolean.TRUE;

	@Inject
	private Shell shell;

	@Inject
	private Project project;

	@Inject 
	private ConnectionProfileHelper connectionProfileHelper;
	
	@DefaultCommand
	public void generateEntities(
			@Option(name = CONNECTION_PROFILE, help = CONNECTION_PROFILE_HELP, required = false, completer = ConnectionProfileNameCompleter.class) String connectionProfileName,
			@Option(name = URL, help = URL_HELP, required = false) String url,
			@Option(name = USER, help = USER_HELP, required = false) String user,
			@Option(name = PASSWORD, help = PASSWORD_HELP, required = false) String password,
			@Option(name = DIALECT, help = DIALECT_HELP, required = false) String dialect,
			@Option(name = DRIVER, help = DRIVER_HELP, required = false) String driver,
			@Option(name = PATH_TO_DRIVER, help = PATH_TO_DRIVER_HELP, required = false) String path,
			@Option(name = TABLE_ID, help = TABLE_HELP, required = false, defaultValue = TABLE_DEFAULT) String tables,
			@Option(name = SCHEMA_ID, help = SCHEMA_HELP, required = false, defaultValue = SCHEMA_DEFAULT) String schemas,
			@Option(name = CATALOG_ID, help = CATALOG_HELP, required = false, defaultValue = CATALOG_DEFAULT) String catalogs,
	 		@Option(name = DETECT_MANY_TO_MANY_ID, help = DETECT_MANY_TO_MANY_HELP, required = false, defaultValue = "true") Boolean manyToMany,
	 		@Option(name = DETECT_ONE_TO_ONE_ID, help = DETECT_ONE_TO_ONE_HELP, required = false, defaultValue = "true") Boolean oneToOne,
	 		@Option(name = DETECT_OPTIMISTIC_LOCK_ID, help = DETECT_OPTIMISTIC_LOCK_HELP, required = false, defaultValue = "true") Boolean optimisticLock,
	        @Option(name = ENTITY_PACKAGE, help = ENTITY_PACKAGE_HELP, required = false) String packageName)
	{
		ConnectionProfile connectionProfile = getOrCreateConnectionProfile(connectionProfileName, url, user, password, dialect, driver, path);
		JDBCMetaDataConfiguration jmdc = configureMetaData(connectionProfile);
		jmdc.setReverseEngineeringStrategy(createReverseEngineeringStrategy(packageName, manyToMany, oneToOne, optimisticLock));
		try {
			doReverseEngineering(connectionProfile.driver, connectionProfile.path, jmdc);
		} catch (Throwable t) {
			ShellMessages.error(shell, "An unexpected error happened during reverse engineering.");
			t.printStackTrace();
			return;
		}
		exportNewEntities(jmdc);
		
	}
	
	private ConnectionProfile getOrCreateConnectionProfile(
			String connectionProfile, 
			String url,
			String user,
			String password,
			String dialect,
			String driver,
			String path) {
		ConnectionProfile result = null;
		if (connectionProfile != null) {
			result = connectionProfileHelper.loadConnectionProfiles().get(connectionProfile);
		}
		if (result == null) {
			result = new ConnectionProfile();
		}
		result.url = connectionProfileHelper.determineURL(url, result);
		result.user = connectionProfileHelper.determineUser(user, result);
		result.password = connectionProfileHelper.determinePassword(password, result);
		result.dialect = connectionProfileHelper.determineDialect(dialect, result);
		result.driver = connectionProfileHelper.determineDriverClass(driver, result);
		result.path = connectionProfileHelper.determineDriverPath(path, result);
		return result;
	}
	
	private JDBCMetaDataConfiguration configureMetaData(
			ConnectionProfile descriptor) {
		JDBCMetaDataConfiguration jmdc = new JDBCMetaDataConfiguration();
		Properties properties = new Properties();
		properties.setProperty("hibernate.connection.driver_class", descriptor.driver);
		properties.setProperty("hibernate.connection.username", descriptor.user);
		properties.setProperty("hibernate.dialect", descriptor.dialect);
		properties.setProperty("hibernate.connection.password",
				descriptor.password == null ? "" : descriptor.password);
		properties.setProperty("hibernate.connection.url", descriptor.url);
		jmdc.setProperties(properties);
		return jmdc;		
	}
	
	private ReverseEngineeringStrategy createReverseEngineeringStrategy(
			String packageName,
			Boolean manyToMany,
			Boolean oneToOne,
			Boolean optimisticLock) {
		ReverseEngineeringStrategy strategy = new DefaultReverseEngineeringStrategy();
		ReverseEngineeringSettings revengsettings = 
				new ReverseEngineeringSettings(strategy)
					.setDefaultPackageName(determinePackageName(packageName))
					.setDetectManyToMany(manyToMany)
					.setDetectOneToOne(oneToOne)
					.setDetectOptimisticLock(optimisticLock);
		strategy.setSettings(revengsettings);
		return strategy;
	}
	
	private String determinePackageName(String packageName) {
		if (packageName != null) {
			return packageName;
		}
		PersistenceFacet jpa = project.getFacet(PersistenceFacet.class);
		return shell.promptCommon(
				ENTITY_PACKAGE_PROMPT,
				PromptType.JAVA_PACKAGE, 
				jpa.getEntityPackage());
	}
	
	private URL[] getDriverUrls(String path) {
		ArrayList<URL> urls = new ArrayList<URL>();
		if (path == null) {
			ShellMessages.info(shell, "No path was specified for the database driver.");
		}
		try {
			urls.add(new File(path).toURI().toURL());			
		} catch (MalformedURLException e) {
			ShellMessages.warn(shell, "The path to the database driver could not be resolved as a valid path.");
		}
		return urls.toArray(new URL[urls.size()]);
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
	
	private void doReverseEngineering(
			final String driver, 
			final String path, 
			final JDBCMetaDataConfiguration jmdc) throws Throwable {
		try {
			executeInNewUrlClassLoader(getDriverUrls(path), new Runnable() {
				@Override
				public void run() {
					try {
						Driver jdbcDriver = (Driver) Class.forName(driver, true,
								Thread.currentThread().getContextClassLoader())
								.newInstance();
						DriverManager.registerDriver(new DelegatingDriver(jdbcDriver));
						jmdc.readFromJDBC();
						jmdc.buildMappings();
					} catch (Exception e) {
						throw new RuntimeException("Exception in runnable", e);
					}
				}			
			});
		} catch (RuntimeException e) {
			if ("Exception in runnable".equals(e.getMessage()) && e.getCause() != null) {
				throw e.getCause();
			}
		}
	}
	
	private void exportNewEntities(JDBCMetaDataConfiguration jmdc) {
		Iterator<?> iter = jmdc.getTableMappings();
		int count = 0;
		while (iter.hasNext()) {
			count++;
			iter.next();
		}
		shell.println("Found " + count + " tables in datasource");
		JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
		POJOExporter pj = new POJOExporter(jmdc, java.getSourceFolder()
				.getUnderlyingResourceObject());
		Properties pojoProperties = new Properties();
		pojoProperties.setProperty("jdk5", "true");
		pojoProperties.setProperty("ejb3", "true");
		pj.setProperties(pojoProperties);

		ArtifactCollector artifacts = new ArtifactCollector() {
			@Override
			public void addFile(final File file, final String type) {
				shell.println("Generated " + type + " at " + file.getPath());
				super.addFile(file, type);
			}
		};
		pj.setArtifactCollector(artifacts);
		pj.start();
		Set<?> fileTypes = artifacts.getFileTypes();
		for (Iterator<?> iterator = fileTypes.iterator(); iterator.hasNext();) {
			String type = (String) iterator.next();
			shell.println("Generated " + artifacts.getFileCount(type) + " "
					+ type + " files.");
		}		
	}

}
	

