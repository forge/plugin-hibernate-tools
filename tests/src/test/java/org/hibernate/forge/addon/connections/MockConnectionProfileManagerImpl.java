package org.hibernate.forge.addon.connections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative @Priority(0)
public class MockConnectionProfileManagerImpl extends ConnectionProfileManagerImpl implements ConnectionProfileManager
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
      profiles.clear();
      for (ConnectionProfile profile : connectionProfiles) {
         profiles.put(profile.name, profile);
      }
   }
   
}
