package org.hibernate.forge.addon.util;

import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.hibernate.cfg.JDBCMetaDataConfiguration;

public class HibernateToolsHelper
{
   
   private Driver driver;
   private InstantiationException instantiationException;
   private IllegalAccessException illegalAccessException;
   private ClassNotFoundException classNotFoundException;
   private SQLException sqlException;
   
   public synchronized Driver getDriver(final String driverName, URL[] urls) 
            throws InstantiationException, 
                   IllegalAccessException, 
                   ClassNotFoundException,
                   SQLException {
      reset();
      Driver result = null;
      UrlClassLoaderExecutor.execute(urls, new Runnable() {
         @Override
         public void run()
         {
            try
            {
               driver = (Driver) Class.forName(
                        driverName,
                        true,
                        Thread.currentThread().getContextClassLoader()).newInstance();
               DriverManager.registerDriver(new DelegatingDriver(driver));
            }
            catch (InstantiationException e)
            {
               instantiationException = e;
            }
            catch (IllegalAccessException e)
            {
               illegalAccessException = e;
            }
            catch (ClassNotFoundException e)
            {
               classNotFoundException = e;
            }
            catch (SQLException e)
            {
               sqlException = e;
            }
         }        
      });
      if (instantiationException != null) {
         throw instantiationException;
      }
      if (illegalAccessException != null) {
         throw illegalAccessException;
      }
      if (classNotFoundException != null) {
         throw classNotFoundException;
      }
      if (sqlException != null) {
         throw sqlException;
      }
      result = driver;
      return result;
   }
   
   public synchronized void buildMappings(
            URL[] urls, 
            final Driver driver, 
            final JDBCMetaDataConfiguration result)
   {
      reset();
      UrlClassLoaderExecutor.execute(urls, new Runnable() {
         @Override
         public void run()
         {
            try
            {
               DriverManager.registerDriver(new DelegatingDriver(driver));
               result.readFromJDBC();
               result.buildMappings();
            }
            catch (SQLException e)
            {
               // registering driver should not pose any problems at this point
            }
         }
     });
   }

   private void reset() {
      driver = null;
      instantiationException = null;
      illegalAccessException = null;
      classNotFoundException = null;
      sqlException = null;
   }

}
