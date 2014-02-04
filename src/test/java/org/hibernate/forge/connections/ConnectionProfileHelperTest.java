package org.hibernate.forge.connections;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import junit.framework.Assert;

import org.hibernate.forge.connections.ConnectionProfile;
import org.hibernate.forge.connections.ConnectionProfileHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.Root;
import org.jboss.seam.render.RenderRoot;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.SolderRoot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConnectionProfileHelperTest
{

   private static final String BEANS_XML =
            "<beans>                                                            " +
            "  <alternatives>                                                   " +
            "    <class>org.hibernate.forge.connections.MockConfiguration</class>" +
            "  </alternatives>                                                  " +
            "</beans>                                                           ";

   @Deployment
   public static JavaArchive getDeployment()
   {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
    		  .addPackages(true, Root.class.getPackage())
              .addPackages(true, RenderRoot.class.getPackage())
              .addPackages(true, SolderRoot.class.getPackage())
              .addClass(MockConfiguration.class)
              .addClass(ConnectionProfileHelper.class)
              .addAsManifestResource(
                  new ByteArrayAsset(
                      BEANS_XML.getBytes()),
                      ArchivePaths.create("beans.xml"));
   }

   public static String CONNECTION_PROFILES =
            "<connection-profiles>                 " +
            "  <connection-profile                 " +
            "      name='foo'              " +
            "      dialect='foo dialect'   " +
            "      driver='foo driver'     " +
            "      pathToDriver='foo path' " +
            "      url='foo url'           " +
            "      user='foo user' />      " +
            "</connection-profiles>                ";

   @Inject 
   private ConnectionProfileHelper connectionProfileHelper;
   
   @Before
   public void setup() {
      MockConfiguration.CONNECTION_PROFILES = CONNECTION_PROFILES;
   }

   @Test
   public void testLoadConnectionProfilesNonEmpty() {
      Map<String, ConnectionProfile> connectionProfiles = connectionProfileHelper.loadConnectionProfiles();
      Assert.assertEquals(1, connectionProfiles.size());
      ConnectionProfile connectionProfileDescriptor = connectionProfiles.get("foo");
      Assert.assertNotNull(connectionProfileDescriptor);
      Assert.assertEquals("foo", connectionProfileDescriptor.name);
      Assert.assertEquals("foo dialect", connectionProfileDescriptor.dialect);
      Assert.assertEquals("foo driver", connectionProfileDescriptor.driver);
      Assert.assertEquals("foo path", connectionProfileDescriptor.path);
      Assert.assertEquals("foo url", connectionProfileDescriptor.url);
      Assert.assertEquals("foo user", connectionProfileDescriptor.user);
   }

   @Test
   public void testLoadConnectionProfilesEmpty() {
      MockConfiguration.CONNECTION_PROFILES = null;
      Map<String, ConnectionProfile> connectionProfiles = connectionProfileHelper.loadConnectionProfiles();
      Assert.assertEquals(0, connectionProfiles.size());
   }

   @Test
   public void testSaveConnectionProfilesNonEmpty() {
      ConnectionProfile descriptor = new ConnectionProfile();
      descriptor.name = "bar";
      descriptor.dialect = "bar dialect";
      descriptor.driver = "bar driver";
      descriptor.path = "bar path";
      descriptor.url = "bar url";
      descriptor.user = "bar user";
      ArrayList<ConnectionProfile> descriptors = new ArrayList<ConnectionProfile>();
      descriptors.add(descriptor);
      connectionProfileHelper.saveConnectionProfiles(descriptors);
      String connectionProfiles = MockConfiguration.CONNECTION_PROFILES;
      Assert.assertTrue(connectionProfiles.contains("<connection-profiles>"));
      Assert.assertTrue(connectionProfiles.contains("<connection-profile"));
      Assert.assertTrue(connectionProfiles.contains("\"bar\""));
      Assert.assertTrue(connectionProfiles.contains("\"bar dialect\""));
      Assert.assertTrue(connectionProfiles.contains("\"bar driver\""));
      Assert.assertTrue(connectionProfiles.contains("\"bar path\""));
      Assert.assertTrue(connectionProfiles.contains("\"bar url\""));
      Assert.assertTrue(connectionProfiles.contains("\"bar user\""));
   }

   @Test
   public void testSaveConnectionProfilesEmpty() {
      connectionProfileHelper.saveConnectionProfiles(new ArrayList<ConnectionProfile>());
      Assert.assertNull(MockConfiguration.CONNECTION_PROFILES);      
   }

}
