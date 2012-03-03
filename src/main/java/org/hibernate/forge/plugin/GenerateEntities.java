package org.hibernate.forge.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
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
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
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
public class GenerateEntities implements Plugin
{

   private static final String TABLE_ID = "table";
   private static final String TABLE_HELP = "Table pattern to include. 'CUSTOMER' for specific table, 'CUST*' for substring match and '*' for all (the default)";
   private static final String TABLE_DEFAULT = "*";

   private static final String SCHEMA_ID = "schema";
   private static final String SCHEMA_HELP = "Schema pattern to include. Same syntax as for table";
   private static final String SCHEMA_DEFAULT = "*";

   private static final String CATALOG_ID = "catalog";
   private static final String CATALOG_HELP = "Catalog pattern to include. Same syntax as for table";
   private static final String CATALOG_DEFAULT = "*";

   private static final String ENTITY_PACKAGE_ID = "entityPackage";
   private static final String ENTITY_PACKAGE_HELP = "Package to use for generated entities.";

   private static final String DRIVER_ID = "driver";
   private static final String DRIVER_HELP = "Class name for JDBC driver";
   private static final String DRIVER_DEFAULT = "org.hsqldb.jdbcDriver";

   private static final String PATH_TO_DRIVER_ID = "pathToDriver";
   private static final String PATH_TO_DRIVER_HELP = "Path in the local file system to the jar file containing the JDBC driver";

   private static final String URL_ID = "url";
   private static final String URL_HELP = "URL for JDBC connection";
   private static final String URL_DEFAULT = "jdbc:hsqldb:localhost:9001";

   private static final String USER_ID = "user";
   private static final String USER_HELP = "Username for JDBC connection";
   private static final String USER_DEFAULT = "sa";

   private static final String PASSWORD_ID = "password";
   private static final String PASSWORD_HELP = "Password for JDBC connection";

   private static final String DIALECT_ID = "dialect";
   private static final String DIALECT_HELP = "Dialect to use for database";
   private static final String DIALECT_DEFAULT = "org.hibernate.dialect.HSQLDialect";

   private static final String DETECT_MANY_TO_MANY_ID = "detectManyToMany";
   private static final String DETECT_MANY_TO_MANY_HELP = "Detect many to many associations between tables.";
   private static final Boolean DETECT_MANY_TO_MANY_DEFAULT = Boolean.TRUE;

   private static final String DETECT_ONE_TO_ONE_ID = "detectOneToOne";
   private static final String DETECT_ONE_TO_ONE_HELP = "Detect one-to-one associations between tables.";
   private static final Boolean DETECT_ONE_TO_ONE_DEFAULT = Boolean.TRUE;

   private static final String DETECT_OPTIMISTIC_LOCK_ID = "detectOptimisticLock";
   private static final String DETECT_OPTIMISTIC_LOCK_HELP = "Detect optimistic locking tables, i.e. if a table has a column named 'version' with a numeric type optimistic locking will be setup for that table.";
   private static final Boolean DETECT_OPTIMISTIC_LOCK_DEFAULT = Boolean.TRUE;
   
   @Inject
   private Shell shell;

   @Inject
   private Project project;

   @Inject
   private Configuration configuration;

   @DefaultCommand
   public void newEntity(
            @Option(name = TABLE_ID, help = TABLE_HELP, required = false) String tableFilter,
            @Option(name = SCHEMA_ID, help = SCHEMA_HELP, required = false) String schemaFilter,
            @Option(name = CATALOG_ID, help = CATALOG_HELP, required = false) String catalogFilter,
            @Option(name = ENTITY_PACKAGE_ID, help = ENTITY_PACKAGE_HELP, required = false) String entityPackage,
            @Option(name = DRIVER_ID, help = DRIVER_HELP, required = false) String jdbcDriver,
            @Option(name = PATH_TO_DRIVER_ID, help = PATH_TO_DRIVER_HELP, required = false) String pathToDriverJar,
            @Option(name = URL_ID, help = URL_HELP, required = false) String jdbcURL,
            @Option(name = USER_ID, help = USER_HELP, required = false) String jdbcUsername,
            @Option(name = PASSWORD_ID, help = PASSWORD_HELP, required = false) String jdbcPassword,
            @Option(name = DIALECT_ID, help = DIALECT_HELP, required = false) String dialect,
            @Option(name = DETECT_MANY_TO_MANY_ID, help = DETECT_MANY_TO_MANY_HELP, required = false) Boolean detectManyToMany,
            @Option(name = DETECT_ONE_TO_ONE_ID, help = DETECT_ONE_TO_ONE_HELP, required = false) Boolean detectOneToOne,
            @Option(name = DETECT_OPTIMISTIC_LOCK_ID, help = DETECT_OPTIMISTIC_LOCK_HELP, required = false) Boolean detectOptimisticLock)
            throws Exception
   {
      
      Configuration config = configuration.getScopedConfiguration(ConfigurationScope.PROJECT);
      tableFilter = tableFilter == null ? config.getString(TABLE_ID) : tableFilter;
      schemaFilter = schemaFilter == null ? config.getString(SCHEMA_ID) : schemaFilter;
      catalogFilter = catalogFilter == null ? config.getString(CATALOG_ID) : catalogFilter;
      entityPackage = entityPackage == null ? config.getString(ENTITY_PACKAGE_ID) : entityPackage;
      jdbcDriver = jdbcDriver == null ? config.getString(DRIVER_ID) : jdbcDriver;
      pathToDriverJar = pathToDriverJar == null ? config.getString(PATH_TO_DRIVER_ID) : pathToDriverJar;
      jdbcURL = jdbcURL == null ? config.getString(URL_ID) : jdbcURL;
      jdbcUsername = jdbcUsername == null ? config.getString(USER_ID) : jdbcUsername;
      dialect = dialect == null ? config.getString(DIALECT_ID) : dialect;
      detectManyToMany = detectManyToMany == null ? config.getBoolean(DETECT_MANY_TO_MANY_ID) : detectManyToMany;
      detectOneToOne = detectOneToOne == null ? config.getBoolean(DETECT_ONE_TO_ONE_ID) : detectOneToOne;
      detectOptimisticLock = detectOptimisticLock == null ? config.getBoolean(DETECT_OPTIMISTIC_LOCK_ID) : detectOptimisticLock;
      
      PersistenceFacet jpa = project.getFacet(PersistenceFacet.class);

      if (dialect == null)
      {
         if (jpa.getConfig().listUnits().size() > 0)
         {
            List<Property> properties = jpa.getConfig().listUnits().get(0).getProperties();
            for (Property property : properties)
            {
               if (property.getName().equals("hibernate.dialect"))
               {
                  dialect = (String) property.getValue();
                  break;
               }
            }
         }
         if (dialect == null)
         {
            shell.println(ShellColor.RED, "Need to specify dialect.");
            return;
         }
      }

      JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

      if (entityPackage == null)
      {
         entityPackage = shell.promptCommon(
                  "In which package you'd like to create this @Entity, or enter for default:", PromptType.JAVA_PACKAGE,
                  jpa.getEntityPackage());
      }

      JDBCMetaDataConfiguration jmdc = new JDBCMetaDataConfiguration();

      Properties properties = new Properties();

      properties.setProperty("hibernate.connection.driver_class", jdbcDriver);
      properties.setProperty("hibernate.connection.username", jdbcUsername);

      properties.setProperty("hibernate.dialect", dialect);
      properties.setProperty("hibernate.connection.password", jdbcPassword == null ? "" : jdbcPassword);
      properties.setProperty("hibernate.connection.url", jdbcURL);

      jmdc.setProperties(properties);

      DefaultReverseEngineeringStrategy defaultStrategy = new DefaultReverseEngineeringStrategy();
      ReverseEngineeringStrategy strategy = defaultStrategy;

      ReverseEngineeringSettings revengsettings = new ReverseEngineeringSettings(strategy)
               .setDefaultPackageName(entityPackage).setDetectManyToMany(detectManyToMany)
               .setDetectOneToOne(detectOneToOne).setDetectOptimisticLock(detectOptimisticLock);
      ;

      defaultStrategy.setSettings(revengsettings);
      strategy.setSettings(revengsettings);
      jmdc.setReverseEngineeringStrategy(strategy);

      ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();

      try
      {
         if (pathToDriverJar != null)
         {
            URL url = new File(pathToDriverJar).toURI().toURL();
            URLClassLoader newlassLoader = new URLClassLoader(new URL[] { url }, savedClassLoader);
            Thread.currentThread().setContextClassLoader(newlassLoader);
         }

         Driver driver = (Driver) Class.forName(jdbcDriver, true, Thread.currentThread().getContextClassLoader())
                  .newInstance();
         DriverManager.registerDriver(new DelegatingDriver(driver));
         DriverManager.getConnection(jdbcURL, jdbcUsername, "").getMetaData();

         try
         {

            jmdc.readFromJDBC();

         }
         catch (HibernateException e)
         {
            if (e.getMessage().contains(jdbcDriver))
            {
               shell.println(ShellColor.RED, "Driver class: " + jdbcDriver
                        + " could not be loaded. Check that the driver jar(s) are in $FORGE_HOME/lib.");
            }
            throw e;
         }
      }
      catch (Exception e)
      {
         e.printStackTrace(System.out);
         throw e;
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(savedClassLoader);
      }

      Iterator<?> iter = jmdc.getTableMappings();
      int count = 0;
      while (iter.hasNext())
      {
         count++;
         iter.next();
      }

      shell.println("Found " + count + " tables in datasource");

      POJOExporter pj = new POJOExporter(jmdc, java.getSourceFolder().getUnderlyingResourceObject());
      Properties pojoProperties = new Properties();
      pojoProperties.setProperty("java5", "true");
      pojoProperties.setProperty("ejb3", "true");
      pj.setProperties(pojoProperties);

      ArtifactCollector artifacts = new ArtifactCollector()
      {
         @Override
         public void addFile(final File file, final String type)
         {
            shell.println("Generated " + type + " at " + file.getPath());
            super.addFile(file, type);
         }
      };
      pj.setArtifactCollector(artifacts);
      pj.start();
      Set<?> fileTypes = artifacts.getFileTypes();
      for (Iterator<?> iterator = fileTypes.iterator(); iterator.hasNext();)
      {
         String type = (String) iterator.next();
         shell.println("Generated " + artifacts.getFileCount(type) + " " + type + " files.");
      }

   }

   @Command(value = "configure-settings", help = "Configure the Hibernate settings for future use.")
   public void configureSettings(
            @Option(name = TABLE_ID, help = TABLE_HELP, required = false) String tableFilter,
            @Option(name = SCHEMA_ID, help = SCHEMA_HELP, required = false) String schemaFilter,
            @Option(name = CATALOG_ID, help = CATALOG_HELP, required = false) String catalogFilter,
            @Option(name = ENTITY_PACKAGE_ID, help = ENTITY_PACKAGE_HELP, required = false) String entityPackage,
            @Option(name = DRIVER_ID, help = DRIVER_HELP, required = false) String jdbcDriver,
            @Option(name = PATH_TO_DRIVER_ID, help = PATH_TO_DRIVER_HELP, required = false) String pathToDriverJar,
            @Option(name = URL_ID, help = URL_HELP, required = false) String jdbcURL,
            @Option(name = USER_ID, help = USER_HELP, required = false) String jdbcUsername,
            @Option(name = DIALECT_ID, help = DIALECT_HELP, required = false) String dialect,
            @Option(name = DETECT_MANY_TO_MANY_ID, help = DETECT_MANY_TO_MANY_HELP, required = false) Boolean detectManyToMany,
            @Option(name = DETECT_ONE_TO_ONE_ID, help = DETECT_ONE_TO_ONE_HELP, required = false) Boolean detectOneToOne,
            @Option(name = DETECT_OPTIMISTIC_LOCK_ID, help = DETECT_OPTIMISTIC_LOCK_HELP, required = false) Boolean detectOptimisticLock)
   {
      Configuration config = configuration.getScopedConfiguration(ConfigurationScope.PROJECT);
      tableFilter = tableFilter == null ? config.getString(TABLE_ID) : tableFilter;
      tableFilter = shell.prompt(TABLE_HELP, tableFilter == null ? TABLE_DEFAULT : tableFilter);
      schemaFilter = schemaFilter == null ? config.getString(SCHEMA_ID) : schemaFilter;
      schemaFilter = shell.prompt(SCHEMA_HELP, schemaFilter == null ? SCHEMA_DEFAULT : schemaFilter);
      catalogFilter = catalogFilter == null ? config.getString(CATALOG_ID) : catalogFilter;
      catalogFilter = shell.prompt(CATALOG_HELP, catalogFilter == null ? CATALOG_DEFAULT : catalogFilter);
      entityPackage = entityPackage == null ? config.getString(ENTITY_PACKAGE_ID) : entityPackage;
      entityPackage = shell.prompt(ENTITY_PACKAGE_HELP, entityPackage);
      jdbcDriver = jdbcDriver == null ? config.getString(DRIVER_ID) : jdbcDriver;
      jdbcDriver = shell.prompt(DRIVER_HELP, jdbcDriver == null ? DRIVER_DEFAULT : jdbcDriver);
      pathToDriverJar = pathToDriverJar == null ? config.getString(PATH_TO_DRIVER_ID) : pathToDriverJar;
      pathToDriverJar = shell.prompt(PATH_TO_DRIVER_HELP, pathToDriverJar);
      jdbcURL = jdbcURL == null ? config.getString(URL_ID) : jdbcURL;
      jdbcURL = shell.prompt(URL_HELP, jdbcURL == null ? URL_DEFAULT : jdbcURL);
      jdbcUsername = jdbcUsername == null ? config.getString(USER_ID) : jdbcUsername;
      jdbcUsername = shell.prompt(USER_HELP, jdbcUsername == null ? USER_DEFAULT : jdbcUsername);
      dialect = dialect == null ? config.getString(DIALECT_ID) : dialect;
      dialect = shell.prompt(DIALECT_HELP, dialect == null ? DIALECT_DEFAULT : dialect);
      detectManyToMany = 
               detectManyToMany == null ? 
               config.containsKey(DETECT_MANY_TO_MANY_ID) ? config.getBoolean(DETECT_MANY_TO_MANY_ID) : DETECT_MANY_TO_MANY_DEFAULT : 
               detectManyToMany;
      detectManyToMany = shell.promptBoolean(DETECT_MANY_TO_MANY_HELP, detectManyToMany);
      detectOneToOne = 
               detectOneToOne == null ? 
               config.containsKey(DETECT_ONE_TO_ONE_ID) ? config.getBoolean(DETECT_ONE_TO_ONE_ID) : DETECT_ONE_TO_ONE_DEFAULT : 
               detectOneToOne;
      detectOneToOne = shell.promptBoolean(DETECT_ONE_TO_ONE_HELP, detectOneToOne);
      detectOptimisticLock = 
               detectOptimisticLock == null ? 
               config.containsKey(DETECT_OPTIMISTIC_LOCK_ID) ? config.getBoolean(DETECT_OPTIMISTIC_LOCK_ID) : DETECT_OPTIMISTIC_LOCK_DEFAULT : 
               detectOptimisticLock;
      detectOptimisticLock = shell.promptBoolean(DETECT_OPTIMISTIC_LOCK_HELP, detectOptimisticLock);      
      config.setProperty(TABLE_ID, tableFilter);
      config.setProperty(SCHEMA_ID, schemaFilter );
      config.setProperty(CATALOG_ID, catalogFilter);
      config.setProperty(ENTITY_PACKAGE_ID, entityPackage);
      config.setProperty(DRIVER_ID, jdbcDriver);
      config.setProperty(PATH_TO_DRIVER_ID, pathToDriverJar);
      config.setProperty(URL_ID, jdbcURL);
      config.setProperty(USER_ID, jdbcUsername);
      config.setProperty(DIALECT_ID, dialect);
      config.setProperty(DETECT_MANY_TO_MANY_ID, detectManyToMany);
      config.setProperty(DETECT_ONE_TO_ONE_ID, detectOneToOne);
      config.setProperty(DETECT_OPTIMISTIC_LOCK_ID, detectOptimisticLock);
   }
   
   @Command(value = "list-settings", help = "List the Hibernate settings.")
   public void listSettings() throws Exception
   {
      Configuration config = configuration.getScopedConfiguration(ConfigurationScope.PROJECT);
      String table = config.getString(TABLE_ID);
      shell.println("table: " + (table == null ? TABLE_DEFAULT : table));
      String schema = config.getString(SCHEMA_ID);
      shell.println("schema: " + (schema == null ? SCHEMA_DEFAULT : schema));
      String catalog = config.getString(CATALOG_ID);
      shell.println("catalog: " + (catalog == null ? CATALOG_DEFAULT : catalog));
      String entityPackage = config.getString(ENTITY_PACKAGE_ID);
      shell.println("entity package: " + (entityPackage == null ? null : entityPackage));
      String driver = config.getString(DRIVER_ID);
      shell.println("driver: " + (driver == null ? DRIVER_DEFAULT : driver));
      String pathToDriver = config.getString(PATH_TO_DRIVER_ID);
      shell.println("path to driver: " + (pathToDriver == null ? null : pathToDriver));
      String url = config.getString(URL_ID);
      shell.println("url: " + (url == null ? URL_DEFAULT : url));
      String user = config.getString(USER_ID);
      shell.println("user: " + (user == null ? USER_DEFAULT : user));
      shell.println("password: *****");
      String dialect = config.getString(DIALECT_ID);
      shell.println("dialect: " + (dialect == null ? DIALECT_DEFAULT : dialect));
      Boolean detectManyToMany = config.containsKey(DETECT_MANY_TO_MANY_ID) ? config.getBoolean(DETECT_MANY_TO_MANY_ID) : DETECT_MANY_TO_MANY_DEFAULT;
      shell.println("detect many to many: " + detectManyToMany);
      Boolean detectOneToOne = config.containsKey(DETECT_ONE_TO_ONE_ID) ? config.getBoolean(DETECT_ONE_TO_ONE_ID) : DETECT_ONE_TO_ONE_DEFAULT;
      shell.println("detect one to one: " + detectOneToOne);
      Boolean detectOptimisticLock = config.containsKey(DETECT_ONE_TO_ONE_ID) ? config.getBoolean(DETECT_OPTIMISTIC_LOCK_ID) : DETECT_OPTIMISTIC_LOCK_DEFAULT;
      shell.println("detect optimistic lock: " + detectOptimisticLock);
   }

}
