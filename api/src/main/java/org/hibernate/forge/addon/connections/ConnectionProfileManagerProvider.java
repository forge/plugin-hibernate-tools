package org.hibernate.forge.addon.connections;

public interface ConnectionProfileManagerProvider {
	
	public void setConnectionProfileManager(ConnectionProfileManager manager);
	
	public ConnectionProfileManager getConnectionProfileManager();

}
