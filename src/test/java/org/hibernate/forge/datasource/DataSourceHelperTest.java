package org.hibernate.forge.datasource;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DataSourceHelperTest
{
   
   private static final String BEANS_XML = 
            "<beans>                                                            " +
            "  <alternatives>                                                   " +
            "    <class>org.hibernate.forge.datasource.MockConfiguration</class>" +
            "  </alternatives>                                                  " +
            "</beans>                                                           ";    
   
   @Deployment
   public static JavaArchive getDeployment()
   {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
               .addClass(DataSourceHelper.class)
               .addClass(MockConfiguration.class)
               .addAsManifestResource(
                        new ByteArrayAsset(
                                 BEANS_XML.getBytes()), 
                        ArchivePaths.create("beans.xml"));
   }
   
   public static String DATASOURCES = 
            "<datasources>                 " +
            "  <datasource                 " +
            "      name='foo'              " +
            "      dialect='foo dialect'   " +
            "      driver='foo driver'     " +
            "      pathToDriver='foo path' " +
            "      url='foo url'           " +
            "      user='foo user' />      " +
            "</datasources>                ";

   @Inject 
   private DataSourceHelper dataSourceHelper;
   
   @Before
   public void setup() {
      MockConfiguration.DATASOURCES = DATASOURCES;
   }
   
   @Test
   public void testLoadDataSourcesNonEmpty() {
      Map<String, DataSourceDescriptor> dataSources = dataSourceHelper.loadDataSources();
      Assert.assertEquals(1, dataSources.size());
      DataSourceDescriptor dataSourceDescriptor = dataSources.get("foo");
      Assert.assertNotNull(dataSourceDescriptor);
      Assert.assertEquals("foo", dataSourceDescriptor.name);
      Assert.assertEquals("foo dialect", dataSourceDescriptor.dialect);
      Assert.assertEquals("foo driver", dataSourceDescriptor.driver);
      Assert.assertEquals("foo path", dataSourceDescriptor.path);
      Assert.assertEquals("foo url", dataSourceDescriptor.url);
      Assert.assertEquals("foo user", dataSourceDescriptor.user);
   }
   
   @Test
   public void testLoadDataSourcesEmpty() {
      MockConfiguration.DATASOURCES = null;
      Map<String, DataSourceDescriptor> dataSources = dataSourceHelper.loadDataSources();
      Assert.assertEquals(0, dataSources.size());
   }
   
   @Test
   public void testSaveDataSourcesNonEmpty() {
      DataSourceDescriptor descriptor = new DataSourceDescriptor();
      descriptor.name = "bar";
      descriptor.dialect = "bar dialect";
      descriptor.driver = "bar driver";
      descriptor.path = "bar path";
      descriptor.url = "bar url";
      descriptor.user = "bar user";
      ArrayList<DataSourceDescriptor> descriptors = new ArrayList<DataSourceDescriptor>();
      descriptors.add(descriptor);
      dataSourceHelper.saveDataSources(descriptors);
      String dataSources = MockConfiguration.DATASOURCES;
      Assert.assertTrue(dataSources.contains("<datasources>"));
      Assert.assertTrue(dataSources.contains("<datasource"));
      Assert.assertTrue(dataSources.contains("\"bar\""));
      Assert.assertTrue(dataSources.contains("\"bar dialect\""));
      Assert.assertTrue(dataSources.contains("\"bar driver\""));
      Assert.assertTrue(dataSources.contains("\"bar path\""));
      Assert.assertTrue(dataSources.contains("\"bar url\""));
      Assert.assertTrue(dataSources.contains("\"bar user\""));
   }
   
   @Test
   public void testSaveDataSourcesEmpty() {
      dataSourceHelper.saveDataSources(new ArrayList<DataSourceDescriptor>());
      Assert.assertNull(MockConfiguration.DATASOURCES);      
   }

}
