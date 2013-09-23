package org.hibernate.forge.addon.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.mapping.Table;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

public class DatabaseTableSelectionStep implements UIWizardStep
{

   private static String NAME = "Database Table Selection";
   private static String DESCRIPTION = "Select the database tables for which you want to generate entities";

   @Inject
   @WithAttributes(
            label = "Database Tables",
            description = "The database tables for which to generate entities",
            required = true)
   private UISelectMany<String> databaseTables;

   @Override
   public NavigationResult next(UIContext context) throws Exception
   {
      return null;
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata
               .forCommand(getClass())
               .name(NAME)
               .description(DESCRIPTION);
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      return true;
   }
   
   @Inject
   private GenerateEntitiesCommandDescriptor descriptor;
   
   @Inject
   private GenerateEntitiesHelper helper;
   
   private JDBCMetaDataConfiguration jmdc;

   @SuppressWarnings("unchecked")
   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      jmdc = new JDBCMetaDataConfiguration();
      jmdc.setProperties(descriptor.connectionProperties);
      jmdc.setReverseEngineeringStrategy(createReverseEngineeringStrategy());
      helper.buildMappings(descriptor.urls, descriptor.driver, jmdc);
      Iterator<Object> iterator = jmdc.getTableMappings();
      ArrayList<String> tables = new ArrayList<String>();
      while (iterator.hasNext()) {
         Object mapping = iterator.next();
         if (mapping instanceof Table) {
            Table table = (Table)mapping;
            tables.add(table.getName());
         }
      }
      databaseTables.setValueChoices(tables);
      databaseTables.setDefaultValue(tables);
      builder.add(databaseTables);
   }

   @Override
   public Result execute(UIContext context)
   { 
      exportNewEntities();
      return Results.success();
   }

   @Override
   public void validate(UIValidationContext context)
   {
   }

   private void exportNewEntities()
   {
      Iterator<?> iter = jmdc.getTableMappings();
      int count = 0;
      while (iter.hasNext())
      {
         count++;
         iter.next();
      }
      System.out.println("Found " + count + " tables in datasource");
      JavaSourceFacet java = descriptor.selectedProject.getFacet(JavaSourceFacet.class);
      POJOExporter pj = new POJOExporter(jmdc, java.getSourceFolder()
               .getUnderlyingResourceObject());
      Properties pojoProperties = new Properties();
      pojoProperties.setProperty("jdk5", "true");
      pojoProperties.setProperty("ejb3", "true");
      pj.setProperties(pojoProperties);

      ArtifactCollector artifacts = new ArtifactCollector()
      {
         @Override
         public void addFile(final File file, final String type)
         {
            System.out.println("Generated " + type + " at " + file.getPath());
            System.out.println("File name is : " + file.getName());
            super.addFile(file, type);
         }
      };
      pj.setArtifactCollector(artifacts);
      pj.start();
      Set<?> fileTypes = artifacts.getFileTypes();
      for (Iterator<?> iterator = fileTypes.iterator(); iterator.hasNext();)
      {
         String type = (String) iterator.next();
         System.out.println("Generated " + artifacts.getFileCount(type) + " "
                  + type + " files.");
      }
   }

   private ReverseEngineeringStrategy createReverseEngineeringStrategy()
   {
      ReverseEngineeringStrategy strategy = new DefaultReverseEngineeringStrategy();
      ReverseEngineeringSettings revengsettings =
               new ReverseEngineeringSettings(strategy)
                        .setDefaultPackageName(descriptor.targetPackage)
                        .setDetectManyToMany(true)
                        .setDetectOneToOne(true)
                        .setDetectOptimisticLock(true);
      strategy.setSettings(revengsettings);
      return strategy;
   }

}
