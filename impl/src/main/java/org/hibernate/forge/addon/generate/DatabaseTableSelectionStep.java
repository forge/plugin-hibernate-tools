package org.hibernate.forge.addon.generate;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
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
   private UIInputMany<String> databaseTables;

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
   private GenerateEntitiesCommandExecutor executor;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder.add(databaseTables);
   }

   @Override
   public Result execute(UIContext context)
   { 
      return executor.execute(context);
   }

   @Override
   public void validate(UIValidationContext context)
   {
   }

}
