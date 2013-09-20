package org.hibernate.forge.addon.generate;

import javax.inject.Inject;

import org.hibernate.forge.addon.connections.ConnectionProfile;
import org.hibernate.forge.addon.connections.ConnectionProfileManager;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

public class ConnectionProfileDetailsStep implements UIWizardStep
{

   private static String NAME = "Connection Profile Details";
   private static String DESCRIPTION = "Edit the connection profile details";

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
   private ConnectionProfileManager manager;

   @Inject
   private GenerateEntitiesCommandDescriptor descriptor;

   @Inject
   private GenerateEntitiesCommandExecutor executor;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      ConnectionProfile cp =
               manager.loadConnectionProfiles().get(
                        descriptor.connectionProfileName);
      if (cp != null)
      {
         jdbcUrl.setValue(cp.url);
         userName.setValue(cp.user);
         userPassword.setValue(cp.password);
         hibernateDialect.setValue(cp.dialect);
         driverLocation.setValue(cp.path);
         driverClass.setValue(cp.driver);
      }
      builder
               .add(jdbcUrl)
               .add(userName)
               .add(userPassword)
               .add(hibernateDialect)
               .add(driverLocation)
               .add(driverClass);
   }

   @Override
   public Result execute(UIContext context)
   {
      buildConnectionProfile();
      return executor.execute(context);
   }

   @Override
   public void validate(UIValidationContext context)
   {
   }

   private void buildConnectionProfile()
   {
      descriptor.connectionProfile = new ConnectionProfile();
      descriptor.connectionProfile.url = jdbcUrl.getValue();
      descriptor.connectionProfile.user = userName.getValue();
      descriptor.connectionProfile.password = userPassword.getValue();
      descriptor.connectionProfile.dialect = hibernateDialect.getValue();
      descriptor.connectionProfile.driver = driverClass.getValue();
      descriptor.connectionProfile.path = driverLocation.getValue();
   }

}
