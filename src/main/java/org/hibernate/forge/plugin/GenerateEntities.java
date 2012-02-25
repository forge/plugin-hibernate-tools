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
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;
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
public class GenerateEntities implements Plugin
{

   @Inject
   private Shell shell;
   
   @Inject
   private Project project;

   @DefaultCommand(help = "Generate entities from a datasourc.")
   public void newEntity(
            @Option(name = "table",
                     help = "Table pattern to include. 'CUSTOMER' for specific table, 'CUST*' for substring match and '*' for all (the default)",
                     defaultValue = "*") final String tableFilter,
            @Option(required = false, name = "schema",
                     help = "Schema pattern to include. Same syntax as for table",
                     defaultValue = "*") final String schemaFilter,
            @Option(required = false, name = "catalog",
                     help = "Catalog pattern to include. Same syntax as for table",
                     defaultValue = "*") final String catalogFilter,
            @Option(name = "entityPackage", help = "Package to use for generated entities.") String entityPackage,
            @Option(name = "driver", help = "Class name for JDBC driver", defaultValue = "org.hsqldb.jdbcDriver") final String jdbcDriver,
            @Option(name = "pathToDriverJar", help = "Path in the local file system to the jar file containing the JDBC driver", required = false) final String pathToDriverJar,
            @Option(name = "url", help = "URL for JDBC connection", defaultValue = "jdbc:hsqldb:localhost:9001") final String jdbcURL,
            @Option(name = "user", help = "Username for JDBC connection", defaultValue = "sa") final String jdbcUsername,
            @Option(name = "dialect", help = "Dialect to use for database", required = false) String dialect,
            @Option(name = "password", help = "Password for JDBC connection") final String jdbcPassword,
            @Option(name = "detectManyToMany", help = "Detect many to many associations between tables.", defaultValue = "true") final boolean detectManyToMany,
            @Option(name = "detectOneToOne", help = "Detect one-to-one associations between tables.", defaultValue = "true") final boolean detectOneToOne,
            @Option(name = "detectOptimisticLock", help = "Detect optimistic locking tables, i.e. if a table has a column named 'version' with a numeric type optimistic locking will be setup for that table.", defaultValue = "true") final boolean detectOptimisticLock) throws Exception 
      {

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
      catch (Exception e) {
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

}
