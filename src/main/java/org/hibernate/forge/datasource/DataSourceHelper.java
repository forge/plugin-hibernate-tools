package org.hibernate.forge.datasource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.env.Configuration;
import org.jboss.forge.env.ConfigurationScope;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;

public class DataSourceHelper implements DataSourceConstants
{
   
   @Inject
   private Configuration configuration;
   
   public Map<String, DataSourceDescriptor> loadDataSources() {
      HashMap<String, DataSourceDescriptor> result = new HashMap<String, DataSourceDescriptor>();
      Configuration config = configuration.getScopedConfiguration(ConfigurationScope.USER);
      String datasources = config.getString("datasources");
      if (datasources != null) {
         Node node = XMLParser.parse(datasources); 
         for (Node child : node.getChildren()) {
            if (!child.getName().equals("datasource")) continue; // Only datasource elements are valid
            DataSourceDescriptor descriptor = new DataSourceDescriptor();
            descriptor.name = child.getAttribute(NAME);
            descriptor.dialect = child.getAttribute(DIALECT);
            descriptor.driverClass = child.getAttribute(DRIVER);
            descriptor.driverLocation = child.getAttribute(PATH_TO_DRIVER);
            descriptor.url = child.getAttribute(URL);
            descriptor.user = child.getAttribute(USER);
            result.put(descriptor.name, descriptor);
         }
      }
      return result;
   }

   public void saveDataSources(Collection<DataSourceDescriptor> datasources) {
      Node root = new Node("datasources");
      for (DataSourceDescriptor descriptor : datasources) {
         Node child = root.createChild("datasource"); 
         child.attribute(NAME, descriptor.name);
         child.attribute(DIALECT, descriptor.dialect);
         child.attribute(DRIVER, descriptor.driverClass);
         child.attribute(PATH_TO_DRIVER, descriptor.driverLocation);
         child.attribute(URL, descriptor.url);
         child.attribute(USER, descriptor.user);
      }
      Configuration config = configuration.getScopedConfiguration(ConfigurationScope.USER);
      if (root.getChildren().isEmpty()) {
         config.clearProperty("datasources");
      } else {
         config.setProperty("datasources", XMLParser.toXMLString(root));
      }  
   }

}
