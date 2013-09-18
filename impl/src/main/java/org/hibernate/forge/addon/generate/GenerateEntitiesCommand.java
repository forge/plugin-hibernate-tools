package org.hibernate.forge.addon.generate;

import java.util.ArrayList;

import javax.inject.Inject;

import org.hibernate.forge.addon.connections.ConnectionProfileManager;
import org.jboss.forge.addon.javaee.facets.PersistenceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;

public class GenerateEntitiesCommand extends AbstractProjectCommand implements UIWizard
{

   private static String[] COMMAND_CATEGORY = { "Java EE", "Generate" };
   private static String COMMAND_NAME = "Entities from Tables";
   private static String COMMAND_DESCRIPTION = "Command to generate Java EE entities from database tables.";

   @Inject
   @WithAttributes(
            label = "Target package",
            type = InputType.JAVA_PACKAGE_PICKER,
            description = "The name of the target package in which to generate the entities",
            required = true)
   private UIInput<String> targetPackage;
   
   @Inject
   @WithAttributes(
            label = "Connection Profile",
            description = "Select the database connection profile you want to use")
   private UISelectOne<String> connectionProfile;

   @Override
   public Metadata getMetadata()
   {
      return Metadata
               .from(super.getMetadata(), getClass())
               .name(COMMAND_NAME)
               .description(COMMAND_DESCRIPTION)
               .category(Categories.create(COMMAND_CATEGORY));
   }
   
   @Inject
   private ConnectionProfileManager manager;
   
   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      Project project = getSelectedProject(builder.getUIContext());
      MetadataFacet facet = project.getFacet(MetadataFacet.class);
      String topLevelPackage = facet.getTopLevelPackage();
      targetPackage.setDefaultValue(topLevelPackage);
      ArrayList<String> profileNames = new ArrayList<String>();
      profileNames.add("");
      profileNames.addAll(manager.loadConnectionProfiles().keySet());
      connectionProfile.setValueChoices(profileNames);
      connectionProfile.setValue("");
      builder.add(targetPackage).add(connectionProfile);
   }
   
   @Inject
   private GenerateEntitiesCommandDescriptor descriptor;

   @Override
   public Result execute(UIContext context)
   {
      return Results.success();
   }

   protected String getParameters()
   {
      return targetPackage.getValue();
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      Project project = getSelectedProject(context);
      if (project != null)
      {
         return project.hasFacet(PersistenceFacet.class) && super.isEnabled(context);
      }
      else
      {
         return false;
      }
   }

   @Override
   public NavigationResult next(UIContext context) throws Exception
   {
      descriptor.targetPackage = targetPackage.getValue();
      descriptor.connectionProfileName = connectionProfile.getValue();
      descriptor.selectedProject = getSelectedProject(context);
      return Results.navigateTo(ConnectionProfileDetailsStep.class);
   }

}
