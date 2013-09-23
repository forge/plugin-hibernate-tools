package org.hibernate.forge.addon.generate;

import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.forge.addon.connections.ConnectionProfile;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.cdi.CommandScoped;

@CommandScoped
public class GenerateEntitiesCommandDescriptor
{
   String targetPackage = "";
   String connectionProfileName = "";
   Project selectedProject;
   ConnectionProfile connectionProfile;
   JDBCMetaDataConfiguration jmdc;
}
