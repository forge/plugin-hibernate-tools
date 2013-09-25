package org.hibernate.forge.addon.connections;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.inject.Inject;

import org.hibernate.forge.addon.util.HibernateToolsHelper;
import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;

public class ConnectionProfileDetailsPage
{

   @Inject
   @WithAttributes(
            label = "JDBC URL",
            description = "The jdbc url for the database tables",
            required = true)
   protected UIInput<String> jdbcUrl;

   @Inject
   @WithAttributes(
            label = "User Name",
            description = "The user name for the database connection",
            required = true)
   protected UIInput<String> userName;

   @Inject
   @WithAttributes(
            label = "User Password",
            description = "The password for the database connection",
            required = false,
            defaultValue = "")
   protected UIInput<String> userPassword;

   @Inject
   @WithAttributes(
            label = "Hibernate Dialect",
            description = "The Hibernate dialect to use",
            required = true)
   protected UISelectOne<HibernateDialect> hibernateDialect;

   @Inject
   @WithAttributes(
            label = "Driver Location",
            description = "The location of the jar file that contains the JDBC driver",
            required = true)
   protected UIInput<FileResource<?>> driverLocation;

   @Inject
   @WithAttributes(
            label = "Driver Class",
            description = "The class name of the JDBC driver",
            required = true)
   protected UIInput<String> driverClass;
   
   @Inject
   private HibernateToolsHelper helper;
   
   protected URL[] urls;
   protected Driver driver;

   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder
               .add(jdbcUrl)
               .add(userName)
               .add(userPassword)
               .add(hibernateDialect)
               .add(driverLocation)
               .add(driverClass);
      hibernateDialect.setItemLabelConverter(new Converter<HibernateDialect, String>()
      {
         @Override
         public String convert(HibernateDialect dialect)
         {
            return dialect == null ? null : dialect.getDatabaseName() + " : " + dialect.getClassName();
         }
      });

   }
   
   public void validate(UIValidationContext context)
   {
      File file = getDriverLocation(context);
      if (file == null) return;
      urls = getDriverUrls(file, context);
      if (urls == null) {
         return;
      } 
      driver = getDriver(urls, context);
      if (driver == null) {
         return;
      }
   }

   private File getDriverLocation(UIValidationContext context) {
      FileResource<?> resource = driverLocation.getValue();
      if (!resource.exists()) {
         context.addValidationError(driverLocation, "The location '" + resource.getFullyQualifiedName() + "' does not exist");
         return null;
      }
      return resource.getUnderlyingResourceObject();
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
