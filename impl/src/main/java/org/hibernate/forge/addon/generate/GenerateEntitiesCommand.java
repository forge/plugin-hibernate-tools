package org.hibernate.forge.addon.generate;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.ui.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public class GenerateEntitiesCommand extends AbstractUICommand
{

   private static String[] COMMAND_CATEGORY = { "Java EE", "Generation" };
   private static String COMMAND_NAME = "Entities from Tables";
   private static String COMMAND_DESCRIPTION = "Command to generate Java EE entities from database tables.";

   @Inject
   @WithAttributes(label = "Target package", type = InputType.JAVA_PACKAGE_PICKER)
   private UIInput<String> targetPackage;

   @Override
   public Metadata getMetadata()
   {
      return Metadata.from(super.getMetadata(), getClass()).name(COMMAND_NAME)
               .description(COMMAND_DESCRIPTION)
               .category(Categories.create(COMMAND_CATEGORY));
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
//      Project project = getSelectedProject(builder.getUIContext());
//      MetadataFacet facet = project.getFacet(MetadataFacet.class);
//      String topLevelPackage = facet.getTopLevelPackage();
//      targetPackage.setDefaultValue(topLevelPackage);
   }

   @Override
   public Result execute(UIContext context)
   {
      return Results.success("Paramters " + getParameters() + " are captured.");
   }

   protected String getParameters()
   {
      return targetPackage.getValue();
   }

//   @Override
//   protected boolean isProjectRequired()
//   {
//      return true;
//   }
}
