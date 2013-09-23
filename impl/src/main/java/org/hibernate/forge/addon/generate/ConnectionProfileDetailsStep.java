package org.hibernate.forge.addon.generate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.inject.Inject;

import org.hibernate.forge.addon.connections.ConnectionProfile;
import org.hibernate.forge.addon.connections.ConnectionProfileManager;
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

   @Inject
   private ConnectionProfileManager manager;

   @Inject
   private GenerateEntitiesCommandDescriptor descriptor;
   
   @Override
   public UICommandMetadata getMetadata(UIContext context)
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

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      ConnectionProfile cp =
               manager.loadConnectionProfiles().get(
                        descriptor.connectionProfileName);
      if (cp != null)
      {
         jdbcUrl.setValue(cp.url);
         userName.setValue(cp.user);
         userPassword.setValue(cp.password);
         hibernateDialect.setValue(cp.dialect);
         driverLocation.setValue(cp.path);
         driverClass.setValue(cp.driver);
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
      return Results.success();
   }

   @Override
   public NavigationResult next(UIContext context) throws Exception
   {
      return Results.navigateTo(DatabaseTableSelectionStep.class);
   }

   @Override
   public void validate(UIValidationContext context)
   {
      File file = getDriverLocation(context);
      if (file == null) return;
      URL[] urls = getDriverUrls(file, context);
      if (urls == null) {
         return;
      } else {
         descriptor.urls = urls;
      } 
      Driver driver = getDriver(urls, context);
      if (driver == null) {
         return;
      } else {
         descriptor.driver = driver;
      }
      descriptor.connectionProperties = createConnectionProperties();
   }
   
   private Properties createConnectionProperties() {
      Properties result = new Properties();
      result.setProperty("hibernate.connection.driver_class", driverClass.getValue());
      result.setProperty("hibernate.connection.username", userName.getValue());
      result.setProperty("hibernate.dialect", hibernateDialect.getValue());
      result.setProperty("hibernate.connection.password",
               userPassword.getValue() == null ? "" : userPassword.getValue());
      result.setProperty("hibernate.connection.url", jdbcUrl.getValue());
      return result;
   }
   
   private File getDriverLocation(UIValidationContext context) {
      String path = driverLocation.getValue();
      File file = new File(path);
      if (!file.exists()) {
         context.addValidationError(driverLocation, "The location '" + path + "' does not exist");
         return null;
      }
      return file;
   }

   private URL[] getDriverUrls(File file, UIValidationContext context)
   {
      try {
         ArrayList<URL> result = new ArrayList<URL>(1);
         result.add(file.toURI().toURL());  
         return result.toArray(new URL[1]);
      } catch (MalformedURLException e) {
         context.addValidationError(driverLocation, 
                  "The location '" + 
                  driverLocation.getValue() + 
                  "' does not point to a valid file");         
         return null;
      }
   }
   
   @Inject 
   private GenerateEntitiesHelper helper;
   
   private Driver getDriver(URL[] urls, UIValidationContext context) {
      Driver result = null;
      String className = driverClass.getValue();
      try
      {
         result = helper.getDriver(className, urls);
      }
      catch (InstantiationException e)
      {
         context.addValidationError(
                  driverClass, 
                  "The class '" + className + "' cannot not be instantiated");
      }
      catch (IllegalAccessException e)
      {
         context.addValidationError(
                  driverClass, 
                  "Illegal access for class '" + className + "'");
      }
      catch (ClassNotFoundException e)
      {
         context.addValidationError(
                  driverClass, 
                  "The class '" + className + "' does not exist");
      }
      catch (SQLException e) {
         context.addValidationError(
                  driverClass, 
                  "An unexpected SQLException happened while registering class '" + className + "'");
      }
      return result;
   }
   
}
