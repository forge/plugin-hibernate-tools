package org.hibernate.forge.database;

import javax.enterprise.inject.Alternative;

import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.forge.shell.env.ScopedConfigurationAdapter;

@Alternative
public class MockConfiguration extends ScopedConfigurationAdapter 
{
   
   public static String CONNECTION_PROFILES;
   
   public Configuration getScopedConfiguration(ConfigurationScope scope) {
      return this;
   }
   
   public String getString(String key) {
      return CONNECTION_PROFILES;
   }
   
   public void setProperty(String name, Object value) {
      CONNECTION_PROFILES = (String)value;
   }
   
   public void clearProperty(String name) {
      CONNECTION_PROFILES = null;
   }
   
}
