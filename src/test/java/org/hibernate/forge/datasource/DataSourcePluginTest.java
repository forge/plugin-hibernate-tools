package org.hibernate.forge.datasource;

import java.util.HashMap;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.Root;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.seam.render.RenderRoot;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.SolderRoot;
import org.junit.Before;
import org.junit.Test;

public class DataSourcePluginTest extends AbstractShellTest {
   
   private static final String BEANS_XML = 
            "<beans>                                                            " +
            "  <alternatives>                                                   " +
            "    <class>org.hibernate.forge.datasource.MockDataSourceHelper</class>" +
            "  </alternatives>                                                  " +
            "</beans>                                                           ";    
   
   @Deployment
   public static JavaArchive getDeployment()
   {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
               .addPackages(true, Root.class.getPackage())
               .addPackages(true, RenderRoot.class.getPackage())
               .addPackages(true, SolderRoot.class.getPackage())
               .addPackages(true, DataSourcePlugin.class.getPackage())
               .addAsManifestResource(
                        new ByteArrayAsset(
                                 BEANS_XML.getBytes()), 
                        ArchivePaths.create("beans.xml"));
   }
   
   private void createEmptyDataSource() {
      MockDataSourceHelper.DATASOURCES = new HashMap<String, DataSourceDescriptor>();
   }
   
   private void createSingletonDataSource() {
      createEmptyDataSource();
      MockDataSourceHelper.DATASOURCES.put("foo", createDataSourceDescriptor("foo"));      
   }
   
   private void createDoubleDataSource() {
      createSingletonDataSource();
      MockDataSourceHelper.DATASOURCES.put("bar", createDataSourceDescriptor("bar"));
   }
   
   private DataSourceDescriptor createDataSourceDescriptor(String id) {
      DataSourceDescriptor result = new DataSourceDescriptor();
      result.name = id;
      result.dialect = id + " dialect";
      result.driver = id + " driver";
      result.path =  id + " path";
      result.url = id + " url";
      result.user = id + " user";
      return result;
   }
   
   @Before
   public void setup() {
      MockDataSourceHelper.DATASOURCES = null;
   }
   
   @Test
   public void testShowDataSourceEmpty() throws Exception {
      createEmptyDataSource();
      getShell().execute("datasource info");
      Assert.assertTrue(getOutput().contains("There are no data sources configured for the current user."));
   }   
   
   @Test
   public void testShowNamedDataSource() throws Exception {
      createDoubleDataSource();
      getShell().execute("datasource info --name foo");
      Assert.assertTrue(getOutput().contains("Data source \"foo\":"));
      Assert.assertTrue(getOutput().contains("dialect:         foo dialect"));
      Assert.assertTrue(getOutput().contains("driver class:    foo driver"));
      Assert.assertTrue(getOutput().contains("driver location: foo path"));
      Assert.assertTrue(getOutput().contains("url:             foo url"));
      Assert.assertTrue(getOutput().contains("user:            foo user"));
      Assert.assertFalse(getOutput().contains("bar"));
   }
   
   @Test
   public void testShowUnexistingDataSource() throws Exception {
      createDoubleDataSource();
      getShell().execute("datasource info --name baz");
      Assert.assertTrue(getOutput().contains("There is no data source named \"baz\" configured for the current user."));
   }
   
   @Test
   public void testShowAllDataSources() throws Exception {
      createDoubleDataSource();
      getShell().execute("datasource info");
      Assert.assertTrue(getOutput().contains("Data source \"foo\":"));
      Assert.assertTrue(getOutput().contains("Data source \"bar\":"));
   }
   
   @Test
   public void testAddTypedDataSource() throws Exception {
      createSingletonDataSource();
      Assert.assertEquals(1, MockDataSourceHelper.DATASOURCES.size());
      getShell().execute(
               "datasource add" +
               "  --name baz" +
               "  --dialect baz\\ dialect" +
               "  --driver baz\\ driver" +
               "  --pathToDriver baz\\ location" +
               "  --url baz\\ url" +
               "  --user baz\\ user");
      Assert.assertEquals(2, MockDataSourceHelper.DATASOURCES.size());
      Assert.assertTrue(getOutput().contains("***SUCCESS***"));
      Assert.assertTrue(getOutput().contains("Data source \"baz\" was saved succesfully:"));
      Assert.assertTrue(getOutput().contains("dialect:         baz dialect"));
      Assert.assertTrue(getOutput().contains("driver class:    baz driver"));
      Assert.assertTrue(getOutput().contains("driver location: baz location"));
      Assert.assertTrue(getOutput().contains("url:             baz url"));
      Assert.assertTrue(getOutput().contains("user:            baz user"));
   }
   
   @Test
   public void testRemoveUnexistingDataSource() throws Exception {
      createSingletonDataSource();
      Assert.assertEquals(1, MockDataSourceHelper.DATASOURCES.size());
      getShell().execute("datasource remove --name baz");
      Assert.assertEquals(1, MockDataSourceHelper.DATASOURCES.size());
      Assert.assertTrue(getOutput().contains("***WARNING*** There is no data source named \"baz\" configured for the current user."));
   }
   
   @Test
   public void testRemoveExistingDataSource() throws Exception {
      createDoubleDataSource();
      Assert.assertEquals(2, MockDataSourceHelper.DATASOURCES.size());
      getShell().execute("datasource remove --name foo");
      Assert.assertEquals(1, MockDataSourceHelper.DATASOURCES.size());
      Assert.assertTrue(getOutput().contains("***SUCCESS*** Data source named \"foo\" is removed succesfully."));
   }
   
}
