package org.hibernate.forge.addon.connections;


import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public class RemoveConnectionProfileCommand extends AbstractUICommand
{

   private static final String[] COMMAND_CATEGORY = { "Database", "Connections" };
   private static final String COMMAND_NAME = "Connection Profile: Remove";
   private static final String COMMAND_DESCRIPTION = "Command to remove a database connectin profile.";

   @Inject
   private ConnectionProfileManager connectionProfileManager;
   
   private Map<String, ConnectionProfile> profiles;

   @Inject
   @WithAttributes(
            label = "Connection Name",
            description = "The name of the database connection provile you want to remove.",
            required = true)
   private UISelectOne<String> name;


   @Override
   public Metadata getMetadata()
   {
      return Metadata
               .from(super.getMetadata(), getClass())
               .name(COMMAND_NAME)
               .description(COMMAND_DESCRIPTION)
               .category(Categories.create(COMMAND_CATEGORY));
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      profiles = connectionProfileManager.loadConnectionProfiles();
      name.setValueChoices(profiles.keySet());
      builder.add(name);
   }

   @Override
   public Result execute(UIContext context) throws Exception
   {
      ConnectionProfile selectedProfile = profiles.get(name.getValue());
      profiles.remove(selectedProfile.name);
      connectionProfileManager.saveConnectionProfiles(profiles.values());
      return Results.success(
               "Connection profile " +
                        selectedProfile.name +
                        " has been removed succesfully");
   }

}
