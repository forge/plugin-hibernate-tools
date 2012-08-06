package org.hibernate.forge.generate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.forge.Constants;
import org.hibernate.forge.datasource.DataSourceDescriptor;
import org.hibernate.forge.datasource.DataSourceHelper;
import org.hibernate.forge.datasource.DataSourceNameCompleter;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;
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
import org.jboss.shrinkwrap.descriptor.api.spec.jpa.persistence.Property;

@Topic("Project")
@RequiresProject
@Alias("generate-entities")
@RequiresFacet(PersistenceFacet.class)
@Help("Generate entities from a database.")
public class GenerateEntitiesPlugin implements Plugin, Constants {

	private static final String DATASOURCE = "datasource";
	private static final String DATASOURCE_HELP = "Name of the data source to use.";

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
	private Configuration configuration;

	public void newEntity(
			@Option(name = TABLE_ID, help = TABLE_HELP, required = false) String tableFilter,
			@Option(name = SCHEMA_ID, help = SCHEMA_HELP, required = false) String schemaFilter,
			@Option(name = CATALOG_ID, help = CATALOG_HELP, required = false) String catalogFilter,
			@Option(name = ENTITY_PACKAGE, help = ENTITY_PACKAGE_HELP, required = false) String entityPackage,
			@Option(name = DRIVER, help = DRIVER_HELP, required = false) String jdbcDriver,
			@Option(name = PATH_TO_DRIVER, help = PATH_TO_DRIVER_HELP, required = false) String pathToDriverJar,
			@Option(name = URL, help = URL_HELP, required = false) String jdbcURL,
			@Option(name = USER, help = USER_HELP, required = false) String jdbcUsername,
			@Option(name = PASSWORD, help = PASSWORD_HELP, required = false) String jdbcPassword,
			@Option(name = DIALECT, help = DIALECT_HELP, required = false) String dialect) // ,
			// @Option(name = DETECT_MANY_TO_MANY_ID, help =
			// DETECT_MANY_TO_MANY_HELP, required = false, defaultValue =
			// "null") Boolean detectManyToMany,
			// @Option(name = DETECT_ONE_TO_ONE_ID, help =
			// DETECT_ONE_TO_ONE_HELP, required = false, defaultValue = "null")
			// Boolean detectOneToOne,
			// @Option(name = DETECT_OPTIMISTIC_LOCK_ID, help =
			// DETECT_OPTIMISTIC_LOCK_HELP, required = false, defaultValue =
			// "null") Boolean detectOptimisticLock)
			throws Exception {

		Configuration config = configuration
				.getScopedConfiguration(ConfigurationScope.PROJECT);
		tableFilter = tableFilter == null ? config.getString(TABLE_ID)
				: tableFilter;
		schemaFilter = schemaFilter == null ? config.getString(SCHEMA_ID)
				: schemaFilter;
		catalogFilter = catalogFilter == null ? config.getString(CATALOG_ID)
				: catalogFilter;
		entityPackage = entityPackage == null ? config
				.getString(ENTITY_PACKAGE) : entityPackage;
		jdbcDriver = jdbcDriver == null ? config.getString(DRIVER) : jdbcDriver;
		pathToDriverJar = pathToDriverJar == null ? config
				.getString(PATH_TO_DRIVER) : pathToDriverJar;
		jdbcURL = jdbcURL == null ? config.getString(URL) : jdbcURL;
		jdbcUsername = jdbcUsername == null ? config.getString(USER)
				: jdbcUsername;
		dialect = dialect == null ? config.getString(DIALECT) : dialect;
//		Boolean detectManyToMany = config.containsKey(DETECT_MANY_TO_MANY_ID) ? config
//				.getBoolean(DETECT_MANY_TO_MANY_ID) : false;
//		Boolean detectOneToOne = config.containsKey(DETECT_ONE_TO_ONE_ID) ? config
//				.getBoolean(DETECT_ONE_TO_ONE_ID) : false;
//		Boolean detectOptimisticLock = config
//				.containsKey(DETECT_OPTIMISTIC_LOCK_ID) ? config
//				.getBoolean(DETECT_OPTIMISTIC_LOCK_ID) : false;

		PersistenceFacet jpa = project.getFacet(PersistenceFacet.class);

		if (dialect == null) {
			if (jpa.getConfig().listUnits().size() > 0) {
				List<Property> properties = jpa.getConfig().listUnits().get(0)
						.getProperties();
				for (Property property : properties) {
					if (property.getName().equals("hibernate.dialect")) {
						dialect = (String) property.getValue();
						break;
					}
				}
			}
			if (dialect == null) {
				shell.println(ShellColor.RED, "Need to specify dialect.");
				return;
			}
		}

		JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

		if (entityPackage == null) {
			entityPackage = shell
					.promptCommon(
							"In which package you'd like to create this @Entity, or enter for default:",
							PromptType.JAVA_PACKAGE, jpa.getEntityPackage());
		}

		JDBCMetaDataConfiguration jmdc = new JDBCMetaDataConfiguration();

		Properties properties = new Properties();

		properties.setProperty("hibernate.connection.driver_class", jdbcDriver);
		properties.setProperty("hibernate.connection.username", jdbcUsername);

		properties.setProperty("hibernate.dialect", dialect);
		properties.setProperty("hibernate.connection.password",
				jdbcPassword == null ? "" : jdbcPassword);
		properties.setProperty("hibernate.connection.url", jdbcURL);

		jmdc.setProperties(properties);

		DefaultReverseEngineeringStrategy defaultStrategy = new DefaultReverseEngineeringStrategy();
		ReverseEngineeringStrategy strategy = defaultStrategy;

		ReverseEngineeringSettings revengsettings = new ReverseEngineeringSettings(
				strategy).setDefaultPackageName(entityPackage)
//				.setDetectManyToMany(detectManyToMany)
//				.setDetectOneToOne(detectOneToOne)
//				.setDetectOptimisticLock(detectOptimisticLock);
		;

		defaultStrategy.setSettings(revengsettings);
		strategy.setSettings(revengsettings);
		jmdc.setReverseEngineeringStrategy(strategy);

		ClassLoader savedClassLoader = Thread.currentThread()
				.getContextClassLoader();

		try {
			if (pathToDriverJar != null) {
				URL url = new File(pathToDriverJar).toURI().toURL();
				URLClassLoader newlassLoader = new URLClassLoader(
						new URL[] { url }, savedClassLoader);
				Thread.currentThread().setContextClassLoader(newlassLoader);
			}

			Driver driver = (Driver) Class.forName(jdbcDriver, true,
					Thread.currentThread().getContextClassLoader())
					.newInstance();
			DriverManager.registerDriver(driver);

			try {
				jmdc.readFromJDBC();
				jmdc.buildMappings();
			} catch (HibernateException e) {
				if (e.getMessage().contains(jdbcDriver)) {
					shell.println(
							ShellColor.RED,
							"Driver class: "
									+ jdbcDriver
									+ " could not be loaded. Check that the driver jar(s) are in $FORGE_HOME/lib.");
				}
				throw e;
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw e;
		} finally {
			Thread.currentThread().setContextClassLoader(savedClassLoader);
		}

		Iterator<?> iter = jmdc.getTableMappings();
		int count = 0;
		while (iter.hasNext()) {
			count++;
			iter.next();
		}

		shell.println("Found " + count + " tables in datasource");

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

//	@Command(value = "configure-settings", help = "Configure the Hibernate settings for future use.")
	public void configureSettings(
			@Option(name = TABLE_ID, help = TABLE_HELP, required = false) String tableFilter,
			@Option(name = SCHEMA_ID, help = SCHEMA_HELP, required = false) String schemaFilter,
			@Option(name = CATALOG_ID, help = CATALOG_HELP, required = false) String catalogFilter,
			@Option(name = ENTITY_PACKAGE, help = ENTITY_PACKAGE_HELP, required = false) String entityPackage,
			@Option(name = DRIVER, help = DRIVER_HELP, required = false) String jdbcDriver,
			@Option(name = PATH_TO_DRIVER, help = PATH_TO_DRIVER_HELP, required = false) String pathToDriverJar,
			@Option(name = URL, help = URL_HELP, required = false) String jdbcURL,
			@Option(name = USER, help = USER_HELP, required = false) String jdbcUsername,
			@Option(name = DIALECT, help = DIALECT_HELP, required = false) String dialect) {
		Configuration config = configuration
				.getScopedConfiguration(ConfigurationScope.PROJECT);
		tableFilter = tableFilter == null ? config.getString(TABLE_ID)
				: tableFilter;
		tableFilter = shell.prompt(TABLE_HELP,
				tableFilter == null ? TABLE_DEFAULT : tableFilter);
		schemaFilter = schemaFilter == null ? config.getString(SCHEMA_ID)
				: schemaFilter;
		schemaFilter = shell.prompt(SCHEMA_HELP,
				schemaFilter == null ? SCHEMA_DEFAULT : schemaFilter);
		catalogFilter = catalogFilter == null ? config.getString(CATALOG_ID)
				: catalogFilter;
		catalogFilter = shell.prompt(CATALOG_HELP,
				catalogFilter == null ? CATALOG_DEFAULT : catalogFilter);
		entityPackage = entityPackage == null ? config
				.getString(ENTITY_PACKAGE) : entityPackage;
		entityPackage = shell.prompt(ENTITY_PACKAGE_HELP, entityPackage);
		jdbcDriver = jdbcDriver == null ? config.getString(DRIVER) : jdbcDriver;
		jdbcDriver = shell.prompt(DRIVER_HELP,
				jdbcDriver == null ? DRIVER_DEFAULT : jdbcDriver);
		pathToDriverJar = pathToDriverJar == null ? config
				.getString(PATH_TO_DRIVER) : pathToDriverJar;
		pathToDriverJar = shell.prompt(PATH_TO_DRIVER_HELP, pathToDriverJar);
		jdbcURL = jdbcURL == null ? config.getString(URL) : jdbcURL;
		jdbcURL = shell.prompt(URL_HELP, jdbcURL == null ? URL_DEFAULT
				: jdbcURL);
		jdbcUsername = jdbcUsername == null ? config.getString(USER)
				: jdbcUsername;
		jdbcUsername = shell.prompt(USER_HELP,
				jdbcUsername == null ? USER_DEFAULT : jdbcUsername);
		dialect = dialect == null ? config.getString(DIALECT) : dialect;
		dialect = shell.prompt(DIALECT_HELP, dialect == null ? DIALECT_DEFAULT
				: dialect);
//		Boolean detectManyToMany = config.containsKey(DETECT_MANY_TO_MANY_ID) ? config
//				.getBoolean(DETECT_MANY_TO_MANY_ID)
//				: DETECT_MANY_TO_MANY_DEFAULT;
//		detectManyToMany = shell.promptBoolean(DETECT_MANY_TO_MANY_HELP,
//				detectManyToMany);
//		Boolean detectOneToOne = config.containsKey(DETECT_ONE_TO_ONE_ID) ? config
//				.getBoolean(DETECT_ONE_TO_ONE_ID) : DETECT_ONE_TO_ONE_DEFAULT;
//		detectOneToOne = shell.promptBoolean(DETECT_ONE_TO_ONE_HELP,
//				detectOneToOne);
//		Boolean detectOptimisticLock = config
//				.containsKey(DETECT_OPTIMISTIC_LOCK_ID) ? config
//				.getBoolean(DETECT_OPTIMISTIC_LOCK_ID)
//				: DETECT_OPTIMISTIC_LOCK_DEFAULT;
//		detectOptimisticLock = shell.promptBoolean(DETECT_OPTIMISTIC_LOCK_HELP,
//				detectOptimisticLock);
		config.setProperty(TABLE_ID, tableFilter);
		config.setProperty(SCHEMA_ID, schemaFilter);
		config.setProperty(CATALOG_ID, catalogFilter);
		config.setProperty(ENTITY_PACKAGE, entityPackage);
		config.setProperty(DRIVER, jdbcDriver);
		config.setProperty(PATH_TO_DRIVER, pathToDriverJar);
		config.setProperty(URL, jdbcURL);
		config.setProperty(USER, jdbcUsername);
		config.setProperty(DIALECT, dialect);
//		config.setProperty(DETECT_MANY_TO_MANY_ID, detectManyToMany);
//		config.setProperty(DETECT_ONE_TO_ONE_ID, detectOneToOne);
//		config.setProperty(DETECT_OPTIMISTIC_LOCK_ID, detectOptimisticLock);
	}

//	@Command(value = "list-settings", help = "List the Hibernate settings.")
	public void listSettings() throws Exception {
		Configuration config = configuration
				.getScopedConfiguration(ConfigurationScope.PROJECT);
		String table = config.getString(TABLE_ID);
		shell.println("table: " + (table == null ? TABLE_DEFAULT : table));
		String schema = config.getString(SCHEMA_ID);
		shell.println("schema: " + (schema == null ? SCHEMA_DEFAULT : schema));
		String catalog = config.getString(CATALOG_ID);
		shell.println("catalog: "
				+ (catalog == null ? CATALOG_DEFAULT : catalog));
		String entityPackage = config.getString(ENTITY_PACKAGE);
		shell.println("entity package: "
				+ (entityPackage == null ? null : entityPackage));
		String driver = config.getString(DRIVER);
		shell.println("driver: " + (driver == null ? DRIVER_DEFAULT : driver));
		String pathToDriver = config.getString(PATH_TO_DRIVER);
		shell.println("path to driver: "
				+ (pathToDriver == null ? null : pathToDriver));
		String url = config.getString(URL);
		shell.println("url: " + (url == null ? URL_DEFAULT : url));
		String user = config.getString(USER);
		shell.println("user: " + (user == null ? USER_DEFAULT : user));
		shell.println("password: *****");
		String dialect = config.getString(DIALECT);
		shell.println("dialect: "
				+ (dialect == null ? DIALECT_DEFAULT : dialect));
//		Boolean detectManyToMany = config.containsKey(DETECT_MANY_TO_MANY_ID) ? config
//				.getBoolean(DETECT_MANY_TO_MANY_ID)
//				: DETECT_MANY_TO_MANY_DEFAULT;
//		shell.println("detect many to many: " + detectManyToMany);
//		Boolean detectOneToOne = config.containsKey(DETECT_ONE_TO_ONE_ID) ? config
//				.getBoolean(DETECT_ONE_TO_ONE_ID) : DETECT_ONE_TO_ONE_DEFAULT;
//		shell.println("detect one to one: " + detectOneToOne);
//		Boolean detectOptimisticLock = config.containsKey(DETECT_ONE_TO_ONE_ID) ? config
//				.getBoolean(DETECT_OPTIMISTIC_LOCK_ID)
//				: DETECT_OPTIMISTIC_LOCK_DEFAULT;
//		shell.println("detect optimistic lock: " + detectOptimisticLock);
	}

	@Inject
	private DataSourceHelper dataSourceHelper;

	@DefaultCommand
	public void generateEntities(
			@Option(name = DATASOURCE, help = DATASOURCE_HELP, required = false, completer = DataSourceNameCompleter.class) String dataSource,
			@Option(name = URL, help = URL_HELP, required = false) String url,
			@Option(name = USER, help = USER_HELP, required = false) String user,
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
		DataSourceDescriptor dataSourceDescriptor = getOrCreateDataSourceDescriptor(dataSource, url, user, dialect, driver, path);
		JDBCMetaDataConfiguration jmdc = configureMetaData(dataSourceDescriptor);
		jmdc.setReverseEngineeringStrategy(createReverseEngineeringStrategy(packageName, manyToMany, oneToOne, optimisticLock));
		try {
			doReverseEngineering(dataSourceDescriptor.driver, dataSourceDescriptor.path, jmdc);
		} catch (Throwable t) {
			ShellMessages.error(shell, "An unexpected error happened during reverse engineering.");
			t.printStackTrace();
			return;
		}
		exportNewEntities(jmdc);

	}

	private DataSourceDescriptor getOrCreateDataSourceDescriptor(
			String dataSource,
			String url,
			String user,
			String dialect,
			String driver,
			String path) {
		DataSourceDescriptor result = null;
		if (dataSource != null) {
			result = dataSourceHelper.loadDataSources().get(dataSource);
		}
		if (result == null) {
			result = new DataSourceDescriptor();
		}
		result.url = dataSourceHelper.determineURL(url, result);
		result.user = dataSourceHelper.determineUser(user, result);
		result.password = dataSourceHelper.determinePassword();
		result.dialect = dataSourceHelper.determineDialect(dialect, result);
		result.driver = dataSourceHelper.determineDriverClass(driver, result);
		result.path = dataSourceHelper.determineDriverPath(path, result);
		return result;
	}

	private JDBCMetaDataConfiguration configureMetaData(
			DataSourceDescriptor descriptor) {
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
						DriverManager.registerDriver(jdbcDriver);
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


