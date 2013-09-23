package org.hibernate.forge.addon.generate;

import java.net.URL;
import java.sql.Driver;
import java.util.Properties;

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
   URL[] urls;
   Driver driver;
   Properties connectionProperties;

}
