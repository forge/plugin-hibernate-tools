/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.hibernate.forge.addon.connections;

import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CreateConnectionProfileCommandTest
{

   private static final String BEANS_XML = 
            "<beans>                                                                                  " +
            "  <alternatives>                                                                         " +
            "    <class>org.hibernate.forge.addon.connections.MockConnectionProfileManagerImpl</class>" +
            "  </alternatives>                                                                        " +
            "</beans>                                                                                 ";    
   
   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.forge.addon:ui"),
            @AddonDependency(name = "org.jboss.forge.addon:configuration"),
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:hibernate-tools")
   })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap
               .create(ForgeArchive.class)
               .addBeansXML(
                        new ByteArrayAsset(
                                 BEANS_XML.getBytes()))
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:hibernate-tools"))
               .addClass(MockConnectionProfileManagerImpl.class);
      return archive;
   }

   @Inject
   private ConnectionProfileManager manager;
   
   @Test
   public void testConnectionProfileManager() throws Exception
   {
      Assert.assertNotNull(manager);
      Map<String, ConnectionProfile> profiles = manager.loadConnectionProfiles();
      Assert.assertNotNull(profiles);
//      Assert.assertEquals(1, profiles.size());
//      manager.saveConnectionProfiles(new ArrayList<ConnectionProfile>());
//      profiles = manager.loadConnectionProfiles();
//      Assert.assertEquals(0, profiles.size());
   }
}