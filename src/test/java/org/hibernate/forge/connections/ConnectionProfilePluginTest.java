package org.hibernate.forge.connections;

import java.util.HashMap;

import junit.framework.Assert;

import org.hibernate.forge.connections.ConnectionProfile;
import org.hibernate.forge.connections.ConnectionProfilePlugin;
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

public class ConnectionProfilePluginTest extends AbstractShellTest {
   
   private static final String BEANS_XML = 
            "<beans>                                                            " +
            "  <alternatives>                                                   " +
            "    <class>org.hibernate.forge.database.MockConnectionProfileHelper</class>" +
            "  </alternatives>                                                  " +
            "</beans>                                                           ";    
   
   @Deployment
   public static JavaArchive getDeployment()
   {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
               .addPackages(true, Root.class.getPackage())
               .addPackages(true, RenderRoot.class.getPackage())
               .addPackages(true, SolderRoot.class.getPackage())
               .addPackages(true, ConnectionProfilePlugin.class.getPackage())
               .addAsManifestResource(
                        new ByteArrayAsset(
                                 BEANS_XML.getBytes()), 
                        ArchivePaths.create("beans.xml"));
   }
   
   private void createEmptyConnectionProfile() {
      MockConnectionProfileHelper.CONNECTION_PROFILES = new HashMap<String, ConnectionProfile>();
   }
   
   private void createSingletonConnectionProfile() {
      createEmptyConnectionProfile();
      MockConnectionProfileHelper.CONNECTION_PROFILES.put("foo", createConnectionProfileDescriptor("foo"));      
   }
   
   private void createDoubleConnectionProfile() {
      createSingletonConnectionProfile();
      MockConnectionProfileHelper.CONNECTION_PROFILES.put("bar", createConnectionProfileDescriptor("bar"));
   }
   
   private ConnectionProfile createConnectionProfileDescriptor(String id) {
      ConnectionProfile result = new ConnectionProfile();
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
      MockConnectionProfileHelper.CONNECTION_PROFILES = null;
   }
   
   @Test
   public void testShowConnectionProfileEmpty() throws Exception {
      createEmptyConnectionProfile();
      getShell().execute("connection-profiles list");
      Assert.assertTrue(getOutput().contains("There are no connection profiles configured for the current user."));
   }   
   
   @Test
   public void testShowNamedConnectionProfile() throws Exception {
      createDoubleConnectionProfile();
      getShell().execute("connection-profiles list --name foo");
      Assert.assertTrue(getOutput().contains("Connection profile \"foo\":"));
      Assert.assertTrue(getOutput().contains("dialect:         foo dialect"));
      Assert.assertTrue(getOutput().contains("driver class:    foo driver"));
      Assert.assertTrue(getOutput().contains("driver location: foo path"));
      Assert.assertTrue(getOutput().contains("url:             foo url"));
      Assert.assertTrue(getOutput().contains("user:            foo user"));
      Assert.assertFalse(getOutput().contains("bar"));
   }
   
   @Test
   public void testShowUnexistingConnectionProfile() throws Exception {
      createDoubleConnectionProfile();
      getShell().execute("connection-profiles list --name baz");
      Assert.assertTrue(getOutput().contains("There is no connection profile named \"baz\" configured for the current user."));
   }
   
   @Test
   public void testShowAllConnectionProfiles() throws Exception {
      createDoubleConnectionProfile();
      getShell().execute("connection-profiles list");
      Assert.assertTrue(getOutput().contains("Connection profile \"foo\":"));
      Assert.assertTrue(getOutput().contains("Connection profile \"bar\":"));
   }
   
   @Test
   public void testAddTypedConnectionProfile() throws Exception {
      createSingletonConnectionProfile();
      Assert.assertEquals(1, MockConnectionProfileHelper.CONNECTION_PROFILES.size());
      getShell().execute(
               "connection-profiles create" +
               "  --name baz" +
               "  --dialect baz\\ dialect" +
               "  --driver baz\\ driver" +
               "  --pathToDriver baz\\ location" +
               "  --url baz\\ url" +
               "  --user baz\\ user");
      Assert.assertEquals(2, MockConnectionProfileHelper.CONNECTION_PROFILES.size());
      Assert.assertTrue(getOutput().contains("***SUCCESS***"));
      Assert.assertTrue(getOutput().contains("Connection profile \"baz\" was saved succesfully:"));
      Assert.assertTrue(getOutput().contains("dialect:         baz dialect"));
      Assert.assertTrue(getOutput().contains("driver class:    baz driver"));
      Assert.assertTrue(getOutput().contains("driver location: baz location"));
      Assert.assertTrue(getOutput().contains("url:             baz url"));
      Assert.assertTrue(getOutput().contains("user:            baz user"));
   }
   
   @Test
   public void testRemoveUnexistingConnectionProfile() throws Exception {
      createSingletonConnectionProfile();
      Assert.assertEquals(1, MockConnectionProfileHelper.CONNECTION_PROFILES.size());
      getShell().execute("connection-profiles remove --name baz");
      Assert.assertEquals(1, MockConnectionProfileHelper.CONNECTION_PROFILES.size());
      Assert.assertTrue(getOutput().contains("***WARNING*** There is no connection profile named \"baz\" configured for the current user."));
   }
   
   @Test
   public void testRemoveExistingConnectionProfile() throws Exception {
      createDoubleConnectionProfile();
      Assert.assertEquals(2, MockConnectionProfileHelper.CONNECTION_PROFILES.size());
      getShell().execute("connection-profiles remove --name foo");
      Assert.assertEquals(1, MockConnectionProfileHelper.CONNECTION_PROFILES.size());
      Assert.assertTrue(getOutput().contains("***SUCCESS*** Connection profile named \"foo\" is removed succesfully."));
   }
   
}
