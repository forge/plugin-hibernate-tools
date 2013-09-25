package org.hibernate.forge.addon.connections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Alternative;

@Alternative
public class MockConnectionProfileManagerImpl implements ConnectionProfileManager
{
   
   private HashMap<String, ConnectionProfile> profiles;
   
   public MockConnectionProfileManagerImpl() {
      profiles = new HashMap<String, ConnectionProfile>();
      addDummyProfile();
   }
   
   private void addDummyProfile() {
      ConnectionProfile profile = new ConnectionProfile();
      profile.name = "dummy";
      profile.dialect = "dialect";
      profiles.put(profile.name, profile);
   }

   @Override
   public Map<String, ConnectionProfile> loadConnectionProfiles()
   {
      return profiles;
   }

   @Override
   public void saveConnectionProfiles(Collection<ConnectionProfile> connectionProfiles)
   {
      for (ConnectionProfile profile : connectionProfiles) {
         profiles.put(profile.name, profile);
      }
   }
   
}
