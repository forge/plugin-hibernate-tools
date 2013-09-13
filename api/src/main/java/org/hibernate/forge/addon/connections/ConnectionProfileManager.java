package org.hibernate.forge.addon.connections;

import java.util.Collection;
import java.util.Map;

import org.jboss.forge.furnace.services.Exported;

@Exported
public interface ConnectionProfileManager
{
   public Map<String, ConnectionProfile> loadConnectionProfiles();

   public void saveConnectionProfiles(Collection<ConnectionProfile> connectionProfiles);
}
