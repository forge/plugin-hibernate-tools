package org.hibernate.forge.addon.connections;

import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public class CreateConnectionProfileCommand extends AbstractUICommand
{

   private static final String[] COMMAND_CATEGORY = { "Database", "Connections" };
   private static final String COMMAND_NAME = "Connection Profile: Create";
   private static final String COMMAND_DESCRIPTION = "Command to create a database connectin profile.";

   @Inject
   private ConnectionProfileManager connectionProfileHelper;

   @Inject
   @WithAttributes(
            label = "Connection Name",
            description = "The name you want to give to this database connection.",
            required = true)
   private UIInput<String> name;

   @Inject
   @WithAttributes(
            label = "JDBC URL",
            description = "The jdbc url for the database tables",
            required = true)
   private UIInput<String> jdbcUrl;

   @Inject
   @WithAttributes(
            label = "User Name",
            description = "The user name for the database connection",
            required = true)
   private UIInput<String> userName;

   @Inject
   @WithAttributes(
            label = "User Password",
            description = "The password for the database connection",
            required = false,
            defaultValue = "")
   private UIInput<String> userPassword;

   @Inject
   @WithAttributes(
            label = "Hibernate Dialect",
            description = "The Hibernate dialect to use",
            required = true)
   private UIInput<String> hibernateDialect;

   @Inject
   @WithAttributes(
            label = "Driver Location",
            description = "The location of the jar file that contains the JDBC driver",
            required = true)
   private UIInput<String> driverLocation;

   @Inject
   @WithAttributes(
            label = "Driver Class",
            description = "The class name of the JDBC driver",
            required = true)
   private UIInput<String> driverClass;

   @Override
   public Metadata getMetadata(UIContext context)
   {
      return Metadata
               .from(super.getMetadata(context), getClass())
               .name(COMMAND_NAME)
               .description(COMMAND_DESCRIPTION)
               .category(Categories.create(COMMAND_CATEGORY));
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder
               .add(name)
               .add(jdbcUrl)
               .add(userName)
               .add(userPassword)
               .add(hibernateDialect)
               .add(driverLocation)
               .add(driverClass);

   }

   @Override
   public Result execute(UIContext context) throws Exception
   {
      Map<String, ConnectionProfile> connectionProfiles =
               connectionProfileHelper.loadConnectionProfiles();
      ConnectionProfile connectionProfile = new ConnectionProfile();
      connectionProfile.name = name.getValue();
      connectionProfile.dialect = hibernateDialect.getValue();
      connectionProfile.driver = driverClass.getValue();
      connectionProfile.path = driverLocation.getValue();
      connectionProfile.url = jdbcUrl.getValue();
      connectionProfile.user = userName.getValue();
      connectionProfiles.put(name.getValue(), connectionProfile);
      connectionProfileHelper.saveConnectionProfiles(connectionProfiles.values());
      return Results.success(
               "Connection profile " +
                        connectionProfile.name +
                        " has been saved succesfully");
   }

}
