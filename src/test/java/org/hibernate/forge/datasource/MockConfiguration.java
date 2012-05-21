package org.hibernate.forge.datasource;

import javax.enterprise.inject.Alternative;

import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.forge.shell.env.ScopedConfigurationAdapter;

@Alternative
public class MockConfiguration extends ScopedConfigurationAdapter 
{
   
   public static String DATASOURCES;
   
   public Configuration getScopedConfiguration(ConfigurationScope scope) {
      return this;
   }
   
   public String getString(String key) {
      return DATASOURCES;
   }
   
   public void setProperty(String name, Object value) {
      DATASOURCES = (String)value;
   }
   
   public void clearProperty(String name) {
      DATASOURCES = null;
   }
   
}
