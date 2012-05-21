package org.hibernate.forge.datasource;

import java.util.Collection;
import java.util.Map;

import javax.enterprise.inject.Alternative;

@Alternative
public class MockDataSourceHelper extends DataSourceHelper
{
   
   public static Map<String, DataSourceDescriptor> DATASOURCES;
   
   public Map<String, DataSourceDescriptor> loadDataSources() {
      return DATASOURCES;
   }

   public void saveDataSources(Collection<DataSourceDescriptor> datasources) {
      for (DataSourceDescriptor datasourceDescriptor : datasources) {
         DATASOURCES.put(datasourceDescriptor.name, datasourceDescriptor);
      }
   }
   
 
}
