package org.hibernate.forge.plugin;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.facets.JavaSourceFacet;
import org.jboss.seam.forge.shell.PromptType;
import org.jboss.seam.forge.shell.Shell;
import org.jboss.seam.forge.shell.plugins.Alias;
import org.jboss.seam.forge.shell.plugins.DefaultCommand;
import org.jboss.seam.forge.shell.plugins.Help;
import org.jboss.seam.forge.shell.plugins.Option;
import org.jboss.seam.forge.shell.plugins.Plugin;
import org.jboss.seam.forge.shell.plugins.RequiresFacet;
import org.jboss.seam.forge.shell.plugins.RequiresProject;
import org.jboss.seam.forge.shell.plugins.Topic;
import org.jboss.seam.forge.spec.jpa.PersistenceFacet;
import org.jboss.shrinkwrap.descriptor.api.spec.jpa.persistence.PersistenceDescriptor;
import org.jboss.shrinkwrap.descriptor.api.spec.jpa.persistence.PersistenceUnitDef;
import org.jboss.shrinkwrap.descriptor.api.spec.jpa.persistence.Property;

@Topic("Project")
@RequiresProject
@Alias("generate-entities")
@RequiresFacet(PersistenceFacet.class)
@Help("Generate entities from a database.")
public class GenerateEntities implements Plugin
{

   private final Shell shell;
   private final Project project;

   @Inject
   public GenerateEntities(final Project project, final Shell shell)
   {
      this.project = project;
      this.shell = shell;
   }

   @DefaultCommand(help = "Generate entities from a datasource")
   public void newEntity(
            @Option(required = false,
                     name = "catalog",
                     description = "Catalog selection", defaultValue = "%") final String catalogFilter,
                     @Option(required = false,
                              name = "schema",
                              description = "Schema selection", defaultValue = "%") final String schemaFilter,
                     @Option(required = true,
                              name = "table",
                                 description = "Table selection", defaultValue = "%") final String tableFilter)
   {
      PersistenceFacet jpa = project.getFacet(PersistenceFacet.class);
      JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
      String entityPackage = shell.promptCommon(
               "In which package you'd like to create this @Entity, or enter for default:",
               PromptType.JAVA_PACKAGE, jpa.getEntityPackage());

      JDBCMetaDataConfiguration jmdc = new JDBCMetaDataConfiguration();

      Properties properties = new Properties();

      shell.println("getting persistence facet.");
      
      final PersistenceDescriptor pDescriptor = project.getFacet(PersistenceFacet.class).getConfig();
      final List<PersistenceUnitDef> list = pDescriptor.listUnits();
      for (final PersistenceUnitDef pud : list) {
        final List<Property> props = pud.getProperties();
        shell.println("Persistence Unit Name: " + pud.getName());
        for (final Property prop : props) {
          shell.println("prop name: " + prop.getName() + "... prop value: " + prop.getValue());
          if (prop.getName() != null && prop.getValue() != null) {
            properties.setProperty(prop.getName(), prop.getValue().toString());
          }
        }
        shell.println("...");
      }
/*
      String driverClass = (shell.getProperty("hibernate.connection.driver_class") == null ? "org.hsqldb.jdbcDriver"
               : shell
                        .getProperty("hibernate.connection.driver_class")).toString();
      String username = (shell.getProperty("hibernate.connection.username") == null ? "sa" : shell
               .getProperty("hibernate.connection.username")).toString();
      String password = (shell.getProperty("hibernate.connection.password") == null ? "" : shell
               .getProperty("hibernate.connection.password")).toString();
      String connectionUrl = (shell.getProperty("hibernate.connection.url") == null ? "jdbc:hsqldb:hsql://localhost:1701"
               : shell
                        .getProperty("hibernate.connection.url")).toString();


      properties.setProperty("hibernate.connection.driver_class", driverClass);
      properties.setProperty("hibernate.connection.username", username);
      properties.setProperty("hibernate.connection.password", password);
      properties.setProperty("hibernate.connection.url", connectionUrl);
*/
      jmdc.setProperties(properties);
      
      DefaultReverseEngineeringStrategy defaultStrategy = new DefaultReverseEngineeringStrategy();
      ReverseEngineeringStrategy strategy = defaultStrategy;

      ReverseEngineeringSettings revengsettings =
               new ReverseEngineeringSettings(strategy).setDefaultPackageName(entityPackage)
      // .setDetectManyToMany( detectManyToMany )
      // .setDetectOneToOne( detectOneToOne )
      // .setDetectOptimisticLock( detectOptimisticLock );
      ;

      defaultStrategy.setSettings(revengsettings);
      strategy.setSettings(revengsettings);
      jmdc.setReverseEngineeringStrategy(strategy);
      jmdc.readFromJDBC();

      Iterator<?> iter = jmdc.getTableMappings();
      int count = 0;
      while (iter.hasNext())
      {
         count++;
         iter.next();
      }

      shell.println("Found " + count + " tables in datasource");

      POJOExporter pj = new POJOExporter(jmdc, java.getSourceFolder().getUnderlyingResourceObject());
      ArtifactCollector artifacts = new ArtifactCollector()
      {
         @Override
         public void addFile(final File file, final String type)
         {
            shell.println("Generated " + type + " at " + file.getPath());
            super.addFile(file, type);
         }
      };
      pj.setArtifactCollector(artifacts);
      pj.start();
      shell.println("Generated " + artifacts.getFileCount("java") + " java files.");
   }
}