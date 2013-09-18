package org.hibernate.forge.addon.generate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.hibernate.forge.addon.connections.ConnectionProfile;
import org.hibernate.forge.addon.connections.ConnectionProfileManager;
import org.hibernate.forge.addon.util.DelegatingDriver;
import org.hibernate.forge.addon.util.UrlClassLoaderExecutor;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

public class ConnectionProfileDetailsStep implements UIWizardStep
{

   private static String NAME = "Connection Profile Details";
   private static String DESCRIPTION = "Edit the connection profile details";

   @Inject
   @WithAttributes(
            label = "JDBC URL",
            description = "The jdbc url for the database tables",
            required = true)
   private UIInput<String> jdbcUrl;

   @Inject
   @WithAttributes(
            label = "User Name",
            description = "The user name for the database connection",
            required = true)
   private UIInput<String> userName;

   @Inject
   @WithAttributes(
            label = "User Password",
            description = "The password for the database connection",
            required = false,
            defaultValue = "")
   private UIInput<String> userPassword;

   @Inject
   @WithAttributes(
            label = "Hibernate Dialect",
            description = "The Hibernate dialect to use",
            required = true)
   private UIInput<String> hibernateDialect;

   @Inject
   @WithAttributes(
            label = "Driver Location",
            description = "The location of the jar file that contains the JDBC driver",
            required = true)
   private UIInput<String> driverLocation;

   @Inject
   @WithAttributes(
            label = "Driver Class",
            description = "The class name of the JDBC driver",
            required = true)
   private UIInput<String> driverClass;

   @Override
   public NavigationResult next(UIContext context) throws Exception
   {
      return null;
   }

   @Override
   public UICommandMetadata getMetadata()
   {
      return Metadata
               .forCommand(getClass())
               .name(NAME)
               .description(DESCRIPTION);
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      return true;
   }
   
   @Inject
   private ConnectionProfileManager manager;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      Object obj = builder.getUIContext().getAttribute("connectionProfile");
      if (obj != null && obj instanceof String) {
         String name  = (String)obj;
         ConnectionProfile cp = manager.loadConnectionProfiles().get(name);
         if (cp != null) {
            jdbcUrl.setValue(cp.url);
            userName.setValue(cp.user);
            userPassword.setValue(cp.password);
            hibernateDialect.setValue(cp.dialect);
            driverLocation.setValue(cp.path);
            driverClass.setValue(cp.driver);
         }
      }
      builder
            .add(jdbcUrl)
            .add(userName)
            .add(userPassword)
            .add(hibernateDialect)
            .add(driverLocation)
            .add(driverClass);
   }

   @Override
   public Result execute(UIContext context)
   {
      
      ConnectionProfile cp = buildConnectionProfile();
      JDBCMetaDataConfiguration jmdc = configureMetaData(cp);
      jmdc.setReverseEngineeringStrategy(createReverseEngineeringStrategy(context));
      try
      {
         doReverseEngineering(cp.driver, cp.path, jmdc);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         return Results.fail("An unexpected error happened during reverse engineering.");
      }
      exportNewEntities(jmdc, getSelectedProject(context));
      return Results.success("Entities are generated succesfully.");
   }

   @Override
   public void validate(UIValidationContext context)
   {
      // TODO Auto-generated method stub

   }

   private ConnectionProfile buildConnectionProfile()
   {
      ConnectionProfile result = new ConnectionProfile();
      result.url = jdbcUrl.getValue();
      result.user = userName.getValue();
      result.password = userPassword.getValue();
      result.dialect = hibernateDialect.getValue();
      result.driver = driverClass.getValue();
      result.path = driverLocation.getValue();
      return result;
   }

   private JDBCMetaDataConfiguration configureMetaData(
            ConnectionProfile cp)
   {
      JDBCMetaDataConfiguration jmdc = new JDBCMetaDataConfiguration();
      Properties properties = new Properties();
      properties.setProperty("hibernate.connection.driver_class", cp.driver);
      properties.setProperty("hibernate.connection.username", cp.user);
      properties.setProperty("hibernate.dialect", cp.dialect);
      properties.setProperty("hibernate.connection.password",
               cp.password == null ? "" : cp.password);
      properties.setProperty("hibernate.connection.url", cp.url);
      jmdc.setProperties(properties);
      return jmdc;
   }

   private ReverseEngineeringStrategy createReverseEngineeringStrategy(UIContext context)
   {
      String targetPackage = (String)context.getAttribute("targetPackage");
      ReverseEngineeringStrategy strategy = new DefaultReverseEngineeringStrategy();
      ReverseEngineeringSettings revengsettings =
               new ReverseEngineeringSettings(strategy)
                        .setDefaultPackageName(targetPackage)
                        .setDetectManyToMany(true)
                        .setDetectOneToOne(true)
                        .setDetectOptimisticLock(true);
      strategy.setSettings(revengsettings);
      return strategy;
   }

   private void doReverseEngineering(
            final String driver,
            final String path,
            final JDBCMetaDataConfiguration jmdc) throws Throwable
   {
      try
      {
         UrlClassLoaderExecutor.execute(getDriverUrls(path), new Runnable()
         {
            @Override
            public void run()
            {
               try
               {
                  Driver jdbcDriver = (Driver) Class.forName(
                           driver,
                           true,
                           Thread.currentThread().getContextClassLoader()).newInstance();
                  DriverManager.registerDriver(new DelegatingDriver(jdbcDriver));
                  jmdc.readFromJDBC();
                  jmdc.buildMappings();
               }
               catch (Exception e)
               {
                  e.printStackTrace();
                  throw new RuntimeException("Exception in runnable", e);
               }
            }
         });
      }
      catch (RuntimeException e)
      {
         e.printStackTrace();
         if ("Exception in runnable".equals(e.getMessage()) && e.getCause() != null)
         {
            throw e.getCause();
         }
      }
   }

   private URL[] getDriverUrls(String path) throws MalformedURLException
   {
      ArrayList<URL> urls = new ArrayList<URL>();
      urls.add(new File(path).toURI().toURL());
      return urls.toArray(new URL[urls.size()]);
   }
   
   private Project getSelectedProject(UIContext context) {
      return (Project)context.getAttribute("selectedProject");
   }

   private void exportNewEntities(JDBCMetaDataConfiguration jmdc, Project project)
   {
      Iterator<?> iter = jmdc.getTableMappings();
      int count = 0;
      while (iter.hasNext())
      {
         count++;
         iter.next();
      }
      System.out.println("Found " + count + " tables in datasource");
      JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
      POJOExporter pj = new POJOExporter(jmdc, java.getSourceFolder()
               .getUnderlyingResourceObject());
      Properties pojoProperties = new Properties();
      pojoProperties.setProperty("jdk5", "true");
      pojoProperties.setProperty("ejb3", "true");
      pj.setProperties(pojoProperties);

      ArtifactCollector artifacts = new ArtifactCollector()
      {
         @Override
         public void addFile(final File file, final String type)
         {
            System.out.println("Generated " + type + " at " + file.getPath());
            super.addFile(file, type);
         }
      };
      pj.setArtifactCollector(artifacts);
      pj.start();
      Set<?> fileTypes = artifacts.getFileTypes();
      for (Iterator<?> iterator = fileTypes.iterator(); iterator.hasNext();)
      {
         String type = (String) iterator.next();
         System.out.println("Generated " + artifacts.getFileCount(type) + " "
                  + type + " files.");
      }
   }

}
